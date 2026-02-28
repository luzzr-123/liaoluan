package www.luuzr.liaoluan.data.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

/**
 * 习惯领域模型 — 包含进度打卡机制 (target × step)
 * lastCompletedDate: 上次完成日期 (yyyy-MM-dd) — 用于每日重置
 * streakDays: 连续完成天数 — 每日重置时累计
 */
@Immutable
@Serializable
data class Habit(
    val id: Long = System.currentTimeMillis(),
    val text: String = "",
    val motivation: String = "",
    val targetValue: Int = 1,
    val targetUnit: String = "次",
    val stepValue: Int = 1,
    val progress: Int = 0,
    val completed: Boolean = false,
    val frequency: List<Int> = listOf(0, 1, 2, 3, 4), // 0=周一 ... 6=周日
    val startTime: String = "09:00",
    val endTime: String = "23:59",        // 每日结束提醒时间
    val reminderInterval: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val lastCompletedDate: String = "",   // yyyy-MM-dd 格式
    val lastRemindedDate: String = "",    // yyyy-MM-dd
    val lastRemindedTime: Long = 0L,      // 上次提醒的精确时间戳
    val streakDays: Int = 0,              // 连续完成天数
    val habitType: String = "NORMAL",     // "NORMAL" 或 "DURATION"
    val targetDuration: Int = 0,          // 目标持续时长 (分钟), 仅用于 DURATION 类型
    val actualStartTime: Long = 0L,       // 实际点击“开始”的时间戳, 仅用于 DURATION 类型
    val reminder: String = ""             // 为了兼容旧版 JSON 导入
)
