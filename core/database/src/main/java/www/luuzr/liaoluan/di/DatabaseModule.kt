package www.luuzr.liaoluan.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import www.luuzr.liaoluan.data.db.BrutalDatabase
import www.luuzr.liaoluan.data.db.dao.HabitDao
import www.luuzr.liaoluan.data.db.dao.NoteDao
import www.luuzr.liaoluan.data.db.dao.TaskDao
import www.luuzr.liaoluan.data.db.dao.HabitLogsDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BrutalDatabase {
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE habits ADD COLUMN habitType TEXT NOT NULL DEFAULT 'NORMAL'")
                database.execSQL("ALTER TABLE habits ADD COLUMN targetDuration INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE habits ADD COLUMN actualStartTime INTEGER NOT NULL DEFAULT 0")
            }
        }

        return Room.databaseBuilder(
            context,
            BrutalDatabase::class.java,
            "brutal_planner.db"
        )
        .addMigrations(MIGRATION_7_8)
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideTaskDao(db: BrutalDatabase): TaskDao = db.taskDao()

    @Provides
    fun provideHabitDao(db: BrutalDatabase): HabitDao = db.habitDao()

    @Provides
    fun provideNoteDao(db: BrutalDatabase): NoteDao = db.noteDao()

    @Provides
    fun provideHabitLogsDao(db: BrutalDatabase): HabitLogsDao = db.habitLogsDao()
}
