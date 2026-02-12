package www.luuzr.liaoluan.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 笔记表 — 纯文本存储，心情存为枚举名称字符串
 */
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: Long,
    val title: String = "",
    val text: String = "",
    val mood: String = "NEUTRAL", // Mood 枚举的 name
    val isPinned: Boolean = false,
    val images: String = "[]",    // JSON 列表存储图片 URI
    val createdAt: Long = System.currentTimeMillis()
)
