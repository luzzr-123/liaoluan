package www.luuzr.liaoluan.data.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import www.luuzr.liaoluan.data.db.entity.TaskEntity

/**
 * 任务表 DAO — 提供完整的 CRUD 和排序查询
 */
@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY completed ASC, createdAt DESC")
    fun getAll(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity)

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM tasks")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<TaskEntity>)
}
