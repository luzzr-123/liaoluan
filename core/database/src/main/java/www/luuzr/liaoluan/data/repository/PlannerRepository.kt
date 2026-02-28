package www.luuzr.liaoluan.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.room.Transaction
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import www.luuzr.liaoluan.data.db.dao.HabitDao
import www.luuzr.liaoluan.data.db.dao.NoteDao
import www.luuzr.liaoluan.data.db.dao.TaskDao
import www.luuzr.liaoluan.data.db.entity.HabitEntity
import www.luuzr.liaoluan.data.db.entity.NoteEntity
import www.luuzr.liaoluan.data.db.entity.TaskEntity
import www.luuzr.liaoluan.data.db.entity.HabitLogEntity
import www.luuzr.liaoluan.data.db.dao.HabitLogsDao // Add this
import www.luuzr.liaoluan.data.model.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 数据仓库 — 负责 Entity ↔ Domain Model 转换，向上层暴露 Flow 数据流
 */
@Singleton
class PlannerRepository @Inject constructor(
    private val taskDao: TaskDao,
    private val habitDao: HabitDao,
    private val noteDao: NoteDao,
    private val habitLogsDao: HabitLogsDao, // Inject HabitLogsDao
    private val json: Json
) {
    // ... (keep flow definitions) ... 
    // ==================== 任务 ====================

    val tasks: Flow<List<Task>> = taskDao.getAll().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun insertTask(task: Task) = taskDao.insert(task.toEntity())
    suspend fun updateTask(task: Task) = taskDao.update(task.toEntity())
    suspend fun deleteTaskById(id: Long) = taskDao.deleteById(id)
    suspend fun deleteAllTasks() = taskDao.deleteAll()
    suspend fun insertAllTasks(tasks: List<Task>) =
        taskDao.insertAll(tasks.map { it.toEntity() })
    
    // ... (keep habit/note methods) ...
    // ==================== 习惯 ====================

    val habits: Flow<List<Habit>> = habitDao.getAll().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun insertHabit(habit: Habit) = habitDao.insert(habit.toEntity())
    suspend fun updateHabit(habit: Habit) = habitDao.update(habit.toEntity())
    suspend fun deleteHabitById(id: Long) = habitDao.deleteById(id)
    suspend fun deleteAllHabits() = habitDao.deleteAll()
    suspend fun insertAllHabits(habits: List<Habit>) =
        habitDao.insertAll(habits.map { it.toEntity() })

    // ==================== 笔记 ====================

    val notes: Flow<List<Note>> = noteDao.getAll().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun insertNote(note: Note) = noteDao.insert(note.toEntity())
    suspend fun updateNote(note: Note) = noteDao.update(note.toEntity())
    suspend fun deleteNoteById(id: Long) = noteDao.deleteById(id)
    suspend fun deleteAllNotes() = noteDao.deleteAll()
    suspend fun insertAllNotes(notes: List<Note>) =
        noteDao.insertAll(notes.map { it.toEntity() })

    // ==================== 习惯日志 (Habit Logs) ====================

    val habitLogs: Flow<List<HabitLogEntity>> = habitLogsDao.getAllLogsFlow()

    fun getHabitLogs(date: String): Flow<List<HabitLogEntity>> = habitLogsDao.getLogsByDate(date)

    fun getHabitLogsBetween(startDate: String, endDate: String): Flow<List<HabitLogEntity>> = 
        habitLogsDao.getLogsBetweenDates(startDate, endDate)

    suspend fun insertHabitLog(log: HabitLogEntity) = habitLogsDao.insertLog(log)
    
    suspend fun deleteHabitLog(habitId: Long, date: String) = habitLogsDao.deleteLog(habitId, date)

    suspend fun getAllHabitLogs() = habitLogsDao.getAllLogs()
    suspend fun insertAllHabitLogs(logs: List<HabitLogEntity>) = habitLogsDao.insertAllLogs(logs)
    suspend fun deleteAllHabitLogs() = habitLogsDao.deleteAllLogs()

    // C3 Fix: 事务保护的全量数据替换，避免 deleteAll 后崩溃导致数据清零
    @Transaction
    suspend fun replaceAllData(
        tasks: List<Task>,
        habits: List<Habit>,
        notes: List<Note>,
        habitLogs: List<HabitLogEntity>
    ) {
        taskDao.deleteAll()
        habitDao.deleteAll()
        noteDao.deleteAll()
        habitLogsDao.deleteAllLogs()
        taskDao.insertAll(tasks.map { it.toEntity() })
        habitDao.insertAll(habits.map { it.toEntity() })
        noteDao.insertAll(notes.map { it.toEntity() })
        habitLogsDao.insertAllLogs(habitLogs)
    }

    // ==================== 映射扩展函数 ====================

    // --- Task ---
    private fun TaskEntity.toDomain() = Task(
        id = id, text = text, description = description,
        tag = tag, priority = Priority.valueOf(priority),
        dueTimestamp = dueTimestamp, completed = completed,
        reminderInterval = reminderInterval, lastReminded = lastReminded,
        reminderText = reminderText,
        createdAt = createdAt
    )

    private fun Task.toEntity(): TaskEntity {
        // 兼容旧版 JSON 导入：如果 dueTimestamp 为 0，尝试解析 dueDate
        val finalTimestamp = if (dueTimestamp > 0L) dueTimestamp else {
            if (dueDate.isNotEmpty()) {
                try {
                    java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).parse(dueDate)?.time ?: 0L
                } catch (e: Exception) { 0L }
            } else 0L
        }

        return TaskEntity(
            id = id, text = text, description = description,
            tag = tag, priority = priority.name,
            dueTimestamp = finalTimestamp, completed = completed,
            reminderInterval = reminderInterval, lastReminded = lastReminded,
            reminderText = reminderText,
            createdAt = createdAt
        )
    }

    // --- Habit ---
    private fun HabitEntity.toDomain() = Habit(
        id = id, text = text, motivation = motivation,
        targetValue = targetValue, targetUnit = targetUnit,
        stepValue = stepValue, progress = progress,
        completed = completed,
        frequency = frequency.split(",").mapNotNull { it.trim().toIntOrNull() },
        startTime = startTime, endTime = endTime,
        reminderInterval = reminderInterval,
        createdAt = createdAt,
        lastCompletedDate = lastCompletedDate,
        lastRemindedDate = lastRemindedDate,
        lastRemindedTime = lastRemindedTime,
        streakDays = streakDays,
        habitType = habitType,
        targetDuration = targetDuration,
        actualStartTime = actualStartTime
    )

    private fun Habit.toEntity(): HabitEntity {
        // 兼容旧版 JSON 导入：如果 startTime 是默认值且 reminder 有值，优先使用 reminder
        val finalStartTime = if (habitReminderFallback(startTime, reminder)) reminder else startTime

        return HabitEntity(
            id = id, text = text, motivation = motivation,
            targetValue = targetValue, targetUnit = targetUnit,
            stepValue = stepValue, progress = progress,
            completed = completed,
            frequency = frequency.joinToString(","),
            startTime = finalStartTime, endTime = endTime,
            reminderInterval = reminderInterval,
            createdAt = createdAt,
            lastCompletedDate = lastCompletedDate,
            lastRemindedDate = lastRemindedDate,
            lastRemindedTime = lastRemindedTime,
            streakDays = streakDays,
            habitType = habitType,
            targetDuration = targetDuration,
            actualStartTime = actualStartTime
        )
    }

    private fun habitReminderFallback(startTime: String, reminder: String): Boolean {
        // startTime == "09:00" && reminder.isNotEmpty()
        return startTime == "09:00" && reminder.isNotEmpty()
    }
    
    // --- Note ---
    private fun NoteEntity.toDomain(): Note {
        val imageList = try {
            json.decodeFromString<List<String>>(images)
        } catch (e: Exception) {
            emptyList()
        }
        return Note(
            id = id, title = title, text = text,
            mood = Mood.valueOf(mood), isPinned = isPinned,
            images = imageList,
            createdAt = createdAt
        )
    }

    private fun Note.toEntity(): NoteEntity {
        val imageJson = json.encodeToString(images)
        return NoteEntity(
            id = id, title = title, text = text,
            mood = mood.name, isPinned = isPinned,
            images = imageJson,
            createdAt = createdAt
        )
    }

}
