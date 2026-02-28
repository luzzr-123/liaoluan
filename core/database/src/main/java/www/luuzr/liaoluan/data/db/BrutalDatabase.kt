package www.luuzr.liaoluan.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import www.luuzr.liaoluan.data.db.dao.HabitDao
import www.luuzr.liaoluan.data.db.dao.NoteDao
import www.luuzr.liaoluan.data.db.dao.TaskDao
import www.luuzr.liaoluan.data.db.entity.HabitEntity
import www.luuzr.liaoluan.data.db.entity.NoteEntity
import www.luuzr.liaoluan.data.db.entity.TaskEntity

/**
 * Room 数据库 — 包含任务、习惯、笔记三张表
 */
import www.luuzr.liaoluan.data.db.dao.HabitLogsDao
import www.luuzr.liaoluan.data.db.entity.HabitLogEntity

@Database(
    entities = [TaskEntity::class, HabitEntity::class, NoteEntity::class, HabitLogEntity::class], // v7 Added HabitLogEntity
    version = 9, // v9: M1/M2 索引优化
    exportSchema = false
)
abstract class BrutalDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun habitDao(): HabitDao
    abstract fun noteDao(): NoteDao
    abstract fun habitLogsDao(): www.luuzr.liaoluan.data.db.dao.HabitLogsDao
}
