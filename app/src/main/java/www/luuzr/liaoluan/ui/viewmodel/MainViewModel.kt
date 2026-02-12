package www.luuzr.liaoluan.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import www.luuzr.liaoluan.data.model.*
import www.luuzr.liaoluan.data.repository.PlannerRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * 主 ViewModel — 统一管理三大模块的状态
 * 使用 StateFlow 驱动 Compose UI 重组
 */

// Toast 数据
data class ToastData(
    val message: String,
    val onUndo: (() -> Unit)? = null
)

// 全局 UI 状态
data class MainUiState(
    val currentTab: Int = 1,          // 0=任务 1=习惯 2=笔记
    val showModal: Boolean = false,
    val showSettings: Boolean = false,
    val editingTask: Task? = null,
    val editingHabit: Habit? = null,
    val editingNote: Note? = null,
    val toast: ToastData? = null,
    val showParticles: Boolean = false
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: PlannerRepository,
    private val json: kotlinx.serialization.json.Json
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    // 三大数据流
    val tasks: StateFlow<List<Task>> = repository.tasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val habits: StateFlow<List<Habit>> = repository.habits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<Note>> = repository.notes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // 每日重置 — App 启动时检查所有习惯，若上次完成日期不是今天则重置 progress
        viewModelScope.launch {
             // 仅订阅一次 habits 流，获取当前快照进行检查
             val habitsSnapshot = habits.firstOrNull { it.isNotEmpty() } ?: return@launch
             
             val today = www.luuzr.liaoluan.util.DateHandle.todayDate()
             habitsSnapshot.forEach { habit ->
                 if (habit.lastCompletedDate.isNotEmpty() && habit.lastCompletedDate != today && habit.progress > 0) {
                     // 上次完成不是今天 -> 重置进度
                     // 1. 如果昨天已完成 (completed=true)，streakDays 保持，等待今天再次完成
                     // 2. 如果昨天未完成 (completed=false)，streakDays 归零 ? 
                     //    注意：这里逻辑比较复杂。如果用户昨天没打开App，今天打开，昨天就算断签。
                     //    目前逻辑：如果 lastCompletedDate != today，就重置 progress = 0, completed = false.
                     
                     // 检查断签逻辑：如果 lastCompletedDate 不是“昨天”，说明断签了。
                     val yesterday = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, -1) }.time
                     val yesterdayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(yesterday)
                     
                     val isBroken = habit.lastCompletedDate != yesterdayStr && habit.lastCompletedDate != today
                     
                     val newStreak = if (isBroken) 0 else habit.streakDays

                     repository.updateHabit(
                         habit.copy(
                             progress = 0,
                             completed = false,
                             streakDays = newStreak
                         )
                     )
                 } else if (habit.lastCompletedDate.isNotEmpty() && habit.lastCompletedDate != today && habit.completed) {
                      // 即使昨天完成了，今天也要重置状态
                      // 如果是昨天完成的，streak 不变
                      // 如果是更早之前完成的，streak 归零
                      
                      // 简化判读：
                      val yesterday = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, -1) }.time
                      val yesterdayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(yesterday)
                      
                      val isBroken = habit.lastCompletedDate != yesterdayStr
                      
                      repository.updateHabit(
                          habit.copy(
                              progress = 0,
                              completed = false,
                              streakDays = if (isBroken) 0 else habit.streakDays
                          )
                      )
                 }
             }
        }
    }

    // ==================== Tab 切换 ====================

    fun switchTab(index: Int) {
        _uiState.update { it.copy(currentTab = index) }
    }

    // ==================== Modal 控制 ====================

    fun openNewModal() {
        _uiState.update {
            it.copy(
                showModal = true,
                editingTask = null,
                editingHabit = null,
                editingNote = null
            )
        }
    }

    fun openEditModal(task: Task) {
        _uiState.update { it.copy(showModal = true, editingTask = task) }
    }

    fun openEditModal(habit: Habit) {
        _uiState.update { it.copy(showModal = true, editingHabit = habit) }
    }

    fun openEditModal(note: Note) {
        _uiState.update { it.copy(showModal = true, editingNote = note) }
    }

    fun closeModal() {
        _uiState.update {
            it.copy(
                showModal = false,
                editingTask = null,
                editingHabit = null,
                editingNote = null
            )
        }
    }

    fun toggleSettings() {
        _uiState.update { it.copy(showSettings = !it.showSettings) }
    }

    // ==================== 任务操作 ====================

    fun saveTask(task: Task) {
        viewModelScope.launch {
            val isEdit = _uiState.value.editingTask != null
            if (isEdit) {
                repository.updateTask(task)
            } else {
                repository.insertTask(task)
            }
            closeModal()
        }
    }

    fun toggleTask(taskId: Long, completed: Boolean) {
        viewModelScope.launch {
            val task = tasks.value.find { it.id == taskId } ?: return@launch
            // 用户反馈：不允许反选 (一旦完成不可撤销)
            if (task.completed && !completed) return@launch
            
            repository.updateTask(task.copy(completed = completed))
            if (completed) triggerParticles()
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            val task = tasks.value.find { it.id == taskId } ?: return@launch
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

    fun saveHabit(habit: Habit) {
        viewModelScope.launch {
            val isEdit = _uiState.value.editingHabit != null
            if (isEdit) {
                repository.updateHabit(habit)
            } else {
                repository.insertHabit(habit)
            }
            closeModal()
        }
    }

    fun progressHabit(habitId: Long) {
        viewModelScope.launch {
            val habit = habits.value.find { it.id == habitId } ?: return@launch
            // 如果已经完成就不再累加
            if (habit.completed) return@launch
            val newProgress = (habit.progress + habit.stepValue).coerceAtMost(habit.targetValue)
            val isCompleted = newProgress >= habit.targetValue
            val today = www.luuzr.liaoluan.util.DateHandle.todayDate()
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
            triggerParticles()
        }
    }

    fun deleteHabit(habitId: Long) {
        viewModelScope.launch {
            val habit = habits.value.find { it.id == habitId } ?: return@launch
            repository.deleteHabitById(habitId)
            showToast(
                message = "${habit.text} 已粉碎",
                onUndo = {
                    viewModelScope.launch {
                        repository.insertHabit(habit)
                        dismissToast()
                    }
                }
            )
        }
    }

    // ==================== 笔记操作 ====================

    fun saveNote(note: Note) {
        viewModelScope.launch {
            val isEdit = _uiState.value.editingNote != null
            if (isEdit) {
                repository.updateNote(note)
            } else {
                repository.insertNote(note)
            }
            closeModal()
        }
    }

    fun deleteNote(noteId: Long) {
        viewModelScope.launch {
            val note = notes.value.find { it.id == noteId } ?: return@launch
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

    fun toggleNotePin(noteId: Long) {
        viewModelScope.launch {
            val note = notes.value.find { it.id == noteId } ?: return@launch
            repository.updateNote(note.copy(isPinned = !note.isPinned))
        }
    }

    // ==================== 导入/导出 ====================

    fun exportData(): BackupData {
        return BackupData(
            tasks = tasks.value,
            habits = habits.value,
            notes = notes.value
        )
    }

    fun getBackupJson(): String {
        return try {
            val backup = BackupData(
                tasks = tasks.value,
                habits = habits.value,
                notes = notes.value
            )
            json.encodeToString(BackupData.serializer(), backup)
        } catch (e: Exception) {
            ""
        }
    }

    /** 单条笔记导出 — JSON 格式 */
    fun getNoteJson(note: Note): String {
        return try {
            val backup = BackupData(
                notes = listOf(note),
                dataType = "note"
            )
            json.encodeToString(BackupData.serializer(), backup)
        } catch (e: Exception) {
            ""
        }
    }

    /** 单条任务导出 — JSON 格式（备用） */
    fun getTaskJson(task: Task): String {
        return try {
            val backup = BackupData(
                tasks = listOf(task),
                dataType = "task"
            )
            json.encodeToString(BackupData.serializer(), backup)
        } catch (e: Exception) {
            ""
        }
    }

    /** 单条习惯导出 — JSON 格式（备用） */
    fun getHabitJson(habit: Habit): String {
        return try {
            val backup = BackupData(
                habits = listOf(habit),
                dataType = "habit"
            )
            json.encodeToString(BackupData.serializer(), backup)
        } catch (e: Exception) {
            ""
        }
    }

    fun importData(backup: BackupData) {
        viewModelScope.launch {
            // 全量备份恢复
            if (backup.tasks != null || backup.habits != null || backup.notes != null) {
                backup.tasks?.let {
                    repository.deleteAllTasks()
                    repository.insertAllTasks(it)
                }
                backup.habits?.let {
                    repository.deleteAllHabits()
                    repository.insertAllHabits(it)
                }
                backup.notes?.let {
                    repository.deleteAllNotes()
                    repository.insertAllNotes(it)
                }
                showToast("全量数据恢复成功")
            }
            _uiState.update { it.copy(showSettings = false) }
        }
    }

    /**
     * 导入单条或全量数据 — 修复 bug：
     * 旧逻辑中 dataType="note" 的单条导出（包含 notes 列表）会被第一个 when 分支
     * 拦截走全量恢复。现在优先检查 dataType 字段来区分单条导入。
     */
    fun importSingleItem(jsonStr: String) {
        viewModelScope.launch {
            // 1. 尝试作为标准的 BackupData 解析 (包含 dataType 和列表)
            try {
                val data = json.decodeFromString<BackupData>(jsonStr)
                // 如果解析成功，根据 dataType 或内容进行分发
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
                    // 无明确 dataType 但有数据列表 → 全量备份恢复
                    data.tasks != null || data.habits != null || data.notes != null -> {
                        importData(data)
                        return@launch
                    }
                }
                _uiState.update { it.copy(showSettings = false) }
                return@launch
            } catch (e: Exception) {
                // Ignore, try next format
            }

            // 2. 尝试解析为旧版/单条 Note 对象
            try {
                val note = json.decodeFromString<Note>(jsonStr)
                repository.insertNote(note.copy(id = System.currentTimeMillis()))
                showToast("单条笔记导入成功")
                _uiState.update { it.copy(showSettings = false) }
                return@launch
            } catch (e: Exception) { }

            // 3. 尝试解析为旧版/单条 Habit 对象
            try {
                val habit = json.decodeFromString<Habit>(jsonStr)
                repository.insertHabit(habit.copy(id = System.currentTimeMillis()))
                showToast("单条习惯导入成功")
                _uiState.update { it.copy(showSettings = false) }
                return@launch
            } catch (e: Exception) { }

            // 4. 尝试解析为旧版/单条 Task 对象
            try {
                val task = json.decodeFromString<Task>(jsonStr)
                repository.insertTask(task.copy(id = System.currentTimeMillis()))
                showToast("单条任务导入成功")
                _uiState.update { it.copy(showSettings = false) }
                return@launch
            } catch (e: Exception) { }

            showToast("未知数据类型，导入失败")
        }
    }

    // ==================== Toast ====================

    fun showToast(message: String, onUndo: (() -> Unit)? = null) {
        _uiState.update { it.copy(toast = ToastData(message, onUndo)) }
        // 4 秒后自动消失
        viewModelScope.launch {
            kotlinx.coroutines.delay(4000)
            if (_uiState.value.toast?.message == message) {
                dismissToast()
            }
        }
    }

    fun dismissToast() {
        _uiState.update { it.copy(toast = null) }
    }

    // ==================== 粒子动画 ====================

    private fun triggerParticles() {
        _uiState.update { it.copy(showParticles = true) }
        viewModelScope.launch {
            kotlinx.coroutines.delay(1000)
            _uiState.update { it.copy(showParticles = false) }
        }
    }
}
