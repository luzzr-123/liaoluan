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
@Database(
    entities = [TaskEntity::class, HabitEntity::class, NoteEntity::class],
    version = 6,
    exportSchema = false
)
abstract class BrutalDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun habitDao(): HabitDao
    abstract fun noteDao(): NoteDao
}
