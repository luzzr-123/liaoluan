package www.luuzr.liaoluan.data.model

import kotlinx.serialization.Serializable

/**
 * 笔记领域模型 — 纯文本，支持置顶和心情标记
 */
@Serializable
data class Note(
    val id: Long = System.currentTimeMillis(),
    val title: String = "",
    val text: String = "",
    val mood: Mood = Mood.NEUTRAL,
    val isPinned: Boolean = false,
    val images: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)
