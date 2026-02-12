package www.luuzr.liaoluan.data.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import www.luuzr.liaoluan.data.db.entity.NoteEntity

/**
 * 笔记表 DAO — 排序逻辑：置顶优先，然后按创建时间倒序
 */
@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY isPinned DESC, createdAt DESC")
    fun getAll(): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity)

    @Update
    suspend fun update(note: NoteEntity)

    @Delete
    suspend fun delete(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM notes")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notes: List<NoteEntity>)
}
