package www.luuzr.liaoluan.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log
import www.luuzr.liaoluan.data.model.BackupData
import www.luuzr.liaoluan.data.db.entity.HabitLogEntity
import www.luuzr.liaoluan.data.model.Habit
import www.luuzr.liaoluan.data.model.Note
import www.luuzr.liaoluan.data.model.Task
import www.luuzr.liaoluan.data.repository.PlannerRepository
import www.luuzr.liaoluan.ui.screen.DailyStatsDetails
import www.luuzr.liaoluan.util.DateHandle
import www.luuzr.liaoluan.util.ExactAlarmHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

data class ToastData(
    val message: String,
    val onUndo: (() -> Unit)? = null
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val app: Application,
    private val repository: PlannerRepository,
    private val json: kotlinx.serialization.json.Json
) : ViewModel() {

    private val _viewState = MutableStateFlow(MainViewState())
    val viewState: StateFlow<MainViewState> = _viewState.asStateFlow()

    init {
        observeData()
        checkCrossDayReset()
    }

    private fun observeData() {
        viewModelScope.launch {
            repository.tasks.collect { t ->
                _viewState.update { it.copy(tasks = t) }
            }
        }
        viewModelScope.launch {
            repository.notes.collect { n ->
                _viewState.update { it.copy(notes = n, filteredNotes = filterNotes(n, it.noteSearchQuery)) }
            }
        }
        viewModelScope.launch {
            repository.habits.collect { h ->
                _viewState.update { it.copy(habits = h, visibleHabits = computeVisibleHabits(h, it.selectedDate, it.selectedDateLogs)) }
            }
        }
        
        // Listen to selectedDate changes to fetch corresponding logs
        viewModelScope.launch {
            _viewState.map { it.selectedDate }.distinctUntilChanged().collectLatest { date ->
                repository.getHabitLogs(date).collect { logs ->
                    _viewState.update { 
                        it.copy(
                            selectedDateLogs = logs, 
                            visibleHabits = computeVisibleHabits(it.habits, it.selectedDate, logs)
                        ) 
                    }
                }
            }
        }
    }

    private fun filterNotes(allNotes: List<Note>, query: String): List<Note> {
        if (query.isBlank()) return allNotes
        return allNotes.filter {
            it.title.contains(query, ignoreCase = true) || it.text.contains(query, ignoreCase = true)
        }
    }

    private fun computeVisibleHabits(allHabits: List<Habit>, date: String, allLogs: List<HabitLogEntity>): List<Habit> {
        val today = DateHandle.todayDate()
        val isToday = date == today
        val isFuture = date > today
        
        val dayIndex = DateHandle.getDayOfWeekIndex(date)
        val logsForDate = allLogs.filter { it.date == date }
        
        return allHabits.filter { it.frequency.contains(dayIndex) }
            .map { habit ->
                if (isToday) {
                    habit // 今天直接显示实时状态
                } else if (isFuture) {
                    // 未来：重置进度，只读
                    habit.copy(progress = 0, completed = false)
                } else {
                    // 过去：从 logs 查找
                    val log = logsForDate.find { it.habitId == habit.id }
                    if (log != null) {
                        habit.copy(progress = log.progress, completed = log.completed)
                    } else {
                        habit.copy(progress = 0, completed = false)
                    }
                }
            }
    }

    private fun updateNoteSearchQuery(query: String) {
        _viewState.update { it.copy(noteSearchQuery = query, filteredNotes = filterNotes(it.notes, query)) }
    }

    fun processIntent(intent: MainIntent) {
        when (intent) {
            is MainIntent.SwitchTab -> switchTab(intent.index)
            is MainIntent.SelectDate -> selectDate(intent.date)
            
            is MainIntent.OpenNewModal -> openNewModal()
            is MainIntent.OpenEditTaskModal -> openEditModal(intent.task)
            is MainIntent.OpenEditHabitModal -> openEditModal(intent.habit)
            is MainIntent.OpenEditNoteModal -> openEditModal(intent.note)
            is MainIntent.CloseModal -> closeModal()
            
            is MainIntent.ToggleSettings -> toggleSettings()
            is MainIntent.ToggleHabitManagement -> toggleHabitManagement()
            is MainIntent.ToggleStats -> toggleStats()
            is MainIntent.CheckCrossDayReset -> checkCrossDayReset()
            is MainIntent.HandleBackPress -> { /* Kept as separate returning boolean */ }
            
            is MainIntent.RequestExactAlarmPermission -> {
                _viewState.update { it.copy(showExactAlarmPermissionDialog = true) }
            }
            is MainIntent.DismissExactAlarmPermissionDialog -> {
                _viewState.update { it.copy(showExactAlarmPermissionDialog = false) }
            }
            
            is MainIntent.SaveTask -> saveTask(intent.task)
            is MainIntent.ToggleTaskComplete -> toggleTask(intent.taskId, intent.completed)
            is MainIntent.DeleteTask -> deleteTask(intent.taskId)
            
            is MainIntent.SaveHabit -> saveHabit(intent.habit)
            is MainIntent.ProgressHabit -> progressHabit(intent.habitId)
            is MainIntent.StartHabitDuration -> startHabitDuration(intent.habit)
            is MainIntent.EndHabitDuration -> endHabitDuration(intent.habit)
            is MainIntent.DeleteHabit -> deleteHabit(intent.habitId)
            
            is MainIntent.SaveNote -> saveNote(intent.note)
            is MainIntent.DeleteNote -> deleteNote(intent.noteId)
            is MainIntent.ToggleNotePin -> toggleNotePin(intent.noteId)
            is MainIntent.SearchNotes -> updateNoteSearchQuery(intent.query)
            
            is MainIntent.ImportBackup -> importData(intent.backup)
            is MainIntent.ImportSingleItem -> importSingleItem(intent.jsonStr)
            
            is MainIntent.ShowToast -> showToast(intent.message, intent.onUndo)
            is MainIntent.DismissToast -> dismissToast()
            is MainIntent.TriggerParticles -> triggerParticles()
        }
    }

    private fun selectDate(date: String) {
        _viewState.update { it.copy(selectedDate = date) } // visibleHabits will be updated via collectLatest on selectedDate
    }

    private fun checkCrossDayReset() {
        viewModelScope.launch {
             // 只获取一次当前的 snapshot
             val habitsSnapshot = _viewState.value.habits
             if (habitsSnapshot.isEmpty()) return@launch
             
             val today = DateHandle.todayDate()
             habitsSnapshot.forEach { habit ->
                 if (habit.lastCompletedDate.isNotEmpty() && habit.lastCompletedDate != today && habit.progress > 0) {
                     val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }.time
                     val yesterdayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(yesterday)
                     val isBroken = habit.lastCompletedDate != yesterdayStr && habit.lastCompletedDate != today
                     val newStreak = if (isBroken) 0 else habit.streakDays

                     repository.updateHabit(
                         habit.copy(
                             progress = 0,
                             completed = false,
                             streakDays = newStreak,
                             actualStartTime = 0L
                         )
                     )
                 } else if (habit.lastCompletedDate.isNotEmpty() && habit.lastCompletedDate != today && habit.completed) {
                       val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }.time
                       val yesterdayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(yesterday)
                       val isBroken = habit.lastCompletedDate != yesterdayStr
                      
                      repository.updateHabit(
                          habit.copy(
                              progress = 0,
                              completed = false,
                              streakDays = if (isBroken) 0 else habit.streakDays,
                              actualStartTime = 0L
                          )
                      )
                 }
             }

             // 同时刷新选中的日期为今天
             selectDate(today)
        }
    }

    // ==================== Tab 切换 ====================
    private fun switchTab(index: Int) {
        _viewState.update { it.copy(currentTab = index) }
    }

    // ==================== Modal 控制 ====================
    private fun openNewModal() {
        _viewState.update {
            it.copy(
                showModal = true,
                editingTask = null,
                editingHabit = null,
                editingNote = null
            )
        }
    }

    private fun openEditModal(task: Task) {
        _viewState.update { it.copy(showModal = true, editingTask = task) }
    }

    private fun openEditModal(habit: Habit) {
        _viewState.update { it.copy(showModal = true, editingHabit = habit) }
    }

    private fun openEditModal(note: Note) {
        _viewState.update { it.copy(showModal = true, editingNote = note) }
    }

    private fun closeModal() {
        _viewState.update {
            it.copy(
                showModal = false,
                editingTask = null,
                editingHabit = null,
                editingNote = null
            )
        }
    }

    private fun toggleSettings() {
        _viewState.update { it.copy(showSettings = !it.showSettings) }
    }

    private fun toggleHabitManagement() {
        _viewState.update { it.copy(showHabitManagement = !it.showHabitManagement) }
    }

    private fun toggleStats() {
        _viewState.update { it.copy(showStats = !it.showStats) }
    }

    // ==================== 任务操作 ====================
    private fun saveTask(task: Task) {
        viewModelScope.launch {
            val isEdit = _viewState.value.editingTask != null
            if (isEdit) {
                repository.updateTask(task)
            } else {
                repository.insertTask(task)
            }
            closeModal()
        }
    }

    private fun toggleTask(taskId: Long, completed: Boolean) {
        viewModelScope.launch {
            val task = _viewState.value.tasks.find { it.id == taskId } ?: return@launch
            // 用户反馈：不允许反选 (一旦完成不可撤销)
            if (task.completed && !completed) return@launch
            
            repository.updateTask(task.copy(completed = completed))
            if (completed) triggerParticles()
        }
    }

    private fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            val task = _viewState.value.tasks.find { it.id == taskId } ?: return@launch
            repository.deleteTaskById(taskId)
            showToast(
                message = "${task.text} 已粉碎",
                onUndo = {
                    viewModelScope.launch {
                        repository.insertTask(task)
                        dismissToast()
                    }
                }
            )
        }
    }

    // ==================== 习惯操作 ====================
    private fun saveHabit(habit: Habit) {
        viewModelScope.launch {
            val isEdit = _viewState.value.editingHabit != null
            if (isEdit) {
                repository.updateHabit(habit)
            } else {
                repository.insertHabit(habit)
            }
            if (habit.habitType == "DURATION") {
                val success = ExactAlarmHelper.scheduleHabitPreAlarm(app, habit)
                if (!success) {
                    _viewState.update { it.copy(showExactAlarmPermissionDialog = true) }
                }
            } else {
                ExactAlarmHelper.cancelAllHabitAlarms(app, habit.id)
            }
            closeModal()
        }
    }

    private fun progressHabit(habitId: Long) {
        viewModelScope.launch {
            val habit = _viewState.value.habits.find { it.id == habitId } ?: return@launch
            // 如果已经完成就不再累加
            if (habit.completed) return@launch
            val newProgress = (habit.progress + habit.stepValue).coerceAtMost(habit.targetValue)
            val isCompleted = newProgress >= habit.targetValue
            val today = DateHandle.todayDate()
            // 完成时记录日期和更新连续天数
            val newStreak = if (isCompleted) {
                // 如果今天第一次完成，连续天数 + 1
                if (habit.lastCompletedDate != today) habit.streakDays + 1 else habit.streakDays
            } else {
                habit.streakDays
            }
            repository.updateHabit(
                habit.copy(
                    progress = newProgress,
                    completed = isCompleted,
                    lastCompletedDate = if (isCompleted) today else habit.lastCompletedDate,
                    streakDays = newStreak
                )
            )

            // BUG-10 Fix: insertLog 已使用 OnConflictStrategy.REPLACE，无需先 delete
            repository.insertHabitLog(
                HabitLogEntity(
                    habitId = habit.id,
                    date = today,
                    progress = newProgress,
                    completed = isCompleted
                )
            )

            triggerParticles()
        }
    }

    private fun startHabitDuration(habit: Habit) {
        viewModelScope.launch {
            if (habit.actualStartTime == 0L && !habit.completed) {
                // BUG-1 Fix: 同时记录 elapsedRealtime 基准和壁钟时间
                val elapsedBase = android.os.SystemClock.elapsedRealtime()
                val updatedHabit = habit.copy(actualStartTime = elapsedBase)
                
                // Check permissions early before spawning service
                val p1 = ExactAlarmHelper.scheduleHabitDurationProcess(app, updatedHabit, habit.reminderInterval)
                val p2 = ExactAlarmHelper.scheduleHabitDurationEnd(app, updatedHabit)
                
                if (!p1 || !p2) {
                    _viewState.update { it.copy(showExactAlarmPermissionDialog = true) }
                    return@launch
                }

                repository.updateHabit(updatedHabit)
                
                // 开启前台保活服务
                val intent = android.content.Intent(app, www.luuzr.liaoluan.service.HabitTimerService::class.java).apply {
                    action = www.luuzr.liaoluan.service.HabitTimerService.ACTION_START
                    putExtra(www.luuzr.liaoluan.service.HabitTimerService.EXTRA_HABIT_ID, updatedHabit.id)
                    putExtra(www.luuzr.liaoluan.service.HabitTimerService.EXTRA_HABIT_NAME, updatedHabit.text)
                    putExtra(www.luuzr.liaoluan.service.HabitTimerService.EXTRA_ELAPSED_BASE, elapsedBase)
                    putExtra(www.luuzr.liaoluan.service.HabitTimerService.EXTRA_PROGRESS, updatedHabit.progress)
                }
                app.startForegroundService(intent)
            }
        }
    }

    private fun endHabitDuration(habit: Habit) {
        viewModelScope.launch {
            if (habit.actualStartTime == 0L) return@launch
            
            val today = DateHandle.todayDate()
            // BUG-1 Fix: actualStartTime 现在存的是 elapsedRealtime 基准值
            val elapsedMinutes = ((android.os.SystemClock.elapsedRealtime() - habit.actualStartTime) / 60000).toInt()
            val newProgress = (habit.progress + elapsedMinutes).coerceAtMost(habit.targetDuration)
            val isCompleted = newProgress >= habit.targetDuration
            
            val newStreak = if (isCompleted && habit.lastCompletedDate != today) {
                habit.streakDays + 1
            } else {
                habit.streakDays
            }

            repository.updateHabit(
                habit.copy(
                    progress = newProgress,
                    completed = isCompleted,
                    lastCompletedDate = if (isCompleted) today else habit.lastCompletedDate,
                    streakDays = newStreak,
                    actualStartTime = 0L // 重置以便下次可以继续（如果还没完成）
                )
            )

            // BUG-10 Fix: insertLog 使用 REPLACE 策略，无需先 delete
            repository.insertHabitLog(
                HabitLogEntity(
                    habitId = habit.id,
                    date = today,
                    progress = newProgress,
                    completed = isCompleted
                )
            )

            // Clear process alarms
            ExactAlarmHelper.cancelAllHabitAlarms(app, habit.id)
            // Restore pre alarm
            ExactAlarmHelper.scheduleHabitPreAlarm(app, habit)

            // 关闭前台服务
            val stopIntent = android.content.Intent(app, www.luuzr.liaoluan.service.HabitTimerService::class.java).apply {
                action = www.luuzr.liaoluan.service.HabitTimerService.ACTION_STOP
            }
            app.startService(stopIntent)

            if (isCompleted) {
                triggerParticles()
            } else {
                showToast("已记录 ${elapsedMinutes} 分钟，总进度: $newProgress / ${habit.targetDuration} 分钟")
            }
        }
    }

    private fun deleteHabit(habitId: Long) {
        viewModelScope.launch {
            val habit = _viewState.value.habits.find { it.id == habitId } ?: return@launch
            repository.deleteHabitById(habitId)
            ExactAlarmHelper.cancelAllHabitAlarms(app, habitId)
            showToast(
                message = "${habit.text} 已粉碎",
                onUndo = {
                    viewModelScope.launch {
                        repository.insertHabit(habit)
                        // BUG-4 Fix: 撤销时恢复闹钟
                        if (habit.habitType == "DURATION") {
                            ExactAlarmHelper.scheduleHabitPreAlarm(app, habit)
                        }
                        dismissToast()
                    }
                }
            )
        }
    }

    // ==================== 笔记操作 ====================
    private fun saveNote(note: Note) {
        viewModelScope.launch {
            val isEdit = _viewState.value.editingNote != null
            if (isEdit) {
                repository.updateNote(note)
            } else {
                repository.insertNote(note)
            }
            closeModal()
        }
    }

    private fun deleteNote(noteId: Long) {
        viewModelScope.launch {
            val note = _viewState.value.notes.find { it.id == noteId } ?: return@launch
            repository.deleteNoteById(noteId)
            showToast(
                message = "${note.title.ifEmpty { "笔记" }} 已粉碎",
                onUndo = {
                    viewModelScope.launch {
                        repository.insertNote(note)
                        dismissToast()
                    }
                }
            )
        }
    }

    private fun toggleNotePin(noteId: Long) {
        viewModelScope.launch {
            val note = _viewState.value.notes.find { it.id == noteId } ?: return@launch
            repository.updateNote(note.copy(isPinned = !note.isPinned))
        }
    }

    // ==================== 导入/导出 ====================
    suspend fun exportData(): BackupData {
        return BackupData(
            tasks = _viewState.value.tasks,
            habits = _viewState.value.habits,
            habitLogs = withContext(Dispatchers.IO) { repository.getAllHabitLogs() },
            notes = _viewState.value.notes
        )
    }

    suspend fun getBackupJson(): String {
        return try {
            val backup = BackupData(
                tasks = _viewState.value.tasks,
                habits = _viewState.value.habits,
                habitLogs = withContext(Dispatchers.IO) { repository.getAllHabitLogs() },
                notes = _viewState.value.notes
            )
            json.encodeToString(BackupData.serializer(), backup)
        } catch (e: Exception) {
            Log.w("MainViewModel", "getBackupJson failed", e)
            // UX-3 Fix: 失败时提示用户而非静默返回空字符串
            showToast("导出失败: ${e.message}")
            ""
        }
    }

    fun getNoteJson(note: Note): String {
        return try {
            val backup = BackupData(
                notes = listOf(note),
                dataType = "note"
            )
            json.encodeToString(BackupData.serializer(), backup)
        } catch (e: Exception) {
            Log.w("MainViewModel", "getNoteJson failed", e)
            ""
        }
    }

    fun getTaskJson(task: Task): String {
        return try {
            val backup = BackupData(
                tasks = listOf(task),
                dataType = "task"
            )
            json.encodeToString(BackupData.serializer(), backup)
        } catch (e: Exception) {
            Log.w("MainViewModel", "getTaskJson failed", e)
            ""
        }
    }

    fun getHabitJson(habit: Habit): String {
        return try {
            val backup = BackupData(
                habits = listOf(habit),
                dataType = "habit"
            )
            json.encodeToString(BackupData.serializer(), backup)
        } catch (e: Exception) {
            Log.w("MainViewModel", "getHabitJson failed", e)
            ""
        }
    }

    private fun importData(backup: BackupData) {
        viewModelScope.launch {
            if (backup.tasks != null || backup.habits != null || backup.notes != null) {
                // C3 Fix: 使用事务保护的全量替换，避免中途崩溃致数据清零
                repository.replaceAllData(
                    tasks = backup.tasks ?: emptyList(),
                    habits = backup.habits ?: emptyList(),
                    notes = backup.notes ?: emptyList(),
                    habitLogs = backup.habitLogs ?: emptyList()
                )
                showToast("全量数据恢复成功")
            }
            _viewState.update { it.copy(showSettings = false) }
        }
    }

    private fun importSingleItem(jsonStr: String) {
        viewModelScope.launch {
            try {
                val data = json.decodeFromString<BackupData>(jsonStr)
                when {
                    data.dataType == "note" -> {
                        val note = data.notes?.firstOrNull()
                        if (note != null) {
                            repository.insertNote(note.copy(id = System.currentTimeMillis()))
                            showToast("单条笔记导入成功")
                        }
                    }
                    data.dataType == "habit" -> {
                        val habit = data.habits?.firstOrNull()
                        if (habit != null) {
                            repository.insertHabit(habit.copy(id = System.currentTimeMillis()))
                            showToast("单条习惯导入成功")
                        }
                    }
                    data.dataType == "task" -> {
                        val task = data.tasks?.firstOrNull()
                        if (task != null) {
                            repository.insertTask(task.copy(id = System.currentTimeMillis()))
                            showToast("单条任务导入成功")
                        }
                    }
                    data.tasks != null || data.habits != null || data.notes != null -> {
                        importData(data)
                        return@launch
                    }
                }
                _viewState.update { it.copy(showSettings = false) }
                return@launch
            } catch (e: Exception) {
                Log.w("MainViewModel", "importSingleItem: BackupData parse failed", e)
            }

            try {
                val note = json.decodeFromString<Note>(jsonStr)
                repository.insertNote(note.copy(id = System.currentTimeMillis()))
                showToast("单条笔记导入成功")
                _viewState.update { it.copy(showSettings = false) }
                return@launch
            } catch (e: Exception) { Log.w("MainViewModel", "importSingleItem: Note parse failed", e) }

            try {
                val habit = json.decodeFromString<Habit>(jsonStr)
                repository.insertHabit(habit.copy(id = System.currentTimeMillis()))
                showToast("单条习惯导入成功")
                _viewState.update { it.copy(showSettings = false) }
                return@launch
            } catch (e: Exception) { Log.w("MainViewModel", "importSingleItem: Habit parse failed", e) }

            try {
                val task = json.decodeFromString<Task>(jsonStr)
                repository.insertTask(task.copy(id = System.currentTimeMillis()))
                showToast("单条任务导入成功")
                _viewState.update { it.copy(showSettings = false) }
                return@launch
            } catch (e: Exception) { Log.w("MainViewModel", "importSingleItem: Task parse failed", e) }

            showToast("未知数据类型，导入失败")
        }
    }

    // ==================== Toast ====================
    private fun showToast(message: String, onUndo: (() -> Unit)? = null) {
        _viewState.update { it.copy(toast = ToastData(message, onUndo)) }
        viewModelScope.launch {
            kotlinx.coroutines.delay(4000)
            if (_viewState.value.toast?.message == message) {
                dismissToast()
            }
        }
    }

    private fun dismissToast() {
        _viewState.update { it.copy(toast = null) }
    }

    fun getGoalHabits(): List<Habit> = _viewState.value.habits.filter { it.habitType == "DURATION" }

    /**
     * 处理系统返回键。如果关闭了某个弹窗/二级页面，返回 true。否则返回 false
     */
    fun handleBackPress(): Boolean {
        val currentState = _viewState.value
        
        if (currentState.showHabitManagement) {
            _viewState.update { it.copy(showHabitManagement = false) }
            return true
        }
        if (currentState.showStats) {
            _viewState.update { it.copy(showStats = false) }
            return true
        }
        if (currentState.showSettings) {
            _viewState.update { it.copy(showSettings = false) }
            return true
        }
        if (currentState.showModal) {
            closeModal()
            return true
        }
        return false
    }

    // ==================== 粒子动画 ====================
    private fun triggerParticles() {
        _viewState.update { it.copy(showParticles = true) }
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            _viewState.update { it.copy(showParticles = false) }
        }
    }


}
