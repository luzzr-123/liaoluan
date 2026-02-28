package www.luuzr.liaoluan.data.model

import kotlinx.serialization.Serializable

/**
 * 心情枚举 — 笔记页的心情选择器
 */
@Serializable
enum class Mood {
    HAPPY,   // 😊
    NEUTRAL, // 😐
    SAD,     // 😞
    STORMY   // 🌧
}
