package www.luuzr.liaoluan.data.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import www.luuzr.liaoluan.data.db.entity.HabitEntity

/**
 * 习惯表 DAO
 */
@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY completed ASC, createdAt DESC")
    fun getAll(): Flow<List<HabitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(habit: HabitEntity)

    @Update
    suspend fun update(habit: HabitEntity)

    @Delete
    suspend fun delete(habit: HabitEntity)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM habits")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(habits: List<HabitEntity>)
}
