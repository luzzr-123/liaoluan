package www.luuzr.liaoluan.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import www.luuzr.liaoluan.data.db.BrutalDatabase
import www.luuzr.liaoluan.data.db.dao.HabitDao
import www.luuzr.liaoluan.data.db.dao.NoteDao
import www.luuzr.liaoluan.data.db.dao.TaskDao
import javax.inject.Singleton

/**
 * Hilt 依赖注入模块 — 提供 Database 和 DAO 实例
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BrutalDatabase =
        Room.databaseBuilder(
            context,
            BrutalDatabase::class.java,
            "brutal_planner.db"
        )
        // 数据库 v1→v2 新增 lastCompletedDate / streakDays 字段
        // 使用 destructive migration 简化处理（App 首次升级不保留旧数据）
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    fun provideTaskDao(db: BrutalDatabase): TaskDao = db.taskDao()

    @Provides
    fun provideHabitDao(db: BrutalDatabase): HabitDao = db.habitDao()

    @Provides
    fun provideNoteDao(db: BrutalDatabase): NoteDao = db.noteDao()

    @Provides
    @Singleton
    fun provideJson(): kotlinx.serialization.json.Json = kotlinx.serialization.json.Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        coerceInputValues = true
    }
}
