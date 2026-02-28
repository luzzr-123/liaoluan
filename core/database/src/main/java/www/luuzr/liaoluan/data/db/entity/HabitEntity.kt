package www.luuzr.liaoluan.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 习惯表 — frequency 存为逗号分隔字符串 (如 "0,1,2,3,4")
 * lastCompletedDate: 上次完成日期 — 用于每日重置进度
 * streakDays: 连续完成天数
 */
// M2 Fix: 排序辅助索引
@Entity(
    tableName = "habits",
    indices = [androidx.room.Index(value = ["completed", "createdAt"])]
)
data class HabitEntity(
    @PrimaryKey val id: Long,
    val text: String,
    val motivation: String = "",
    val targetValue: Int = 1,
    val targetUnit: String = "次",
    val stepValue: Int = 1,
    val progress: Int = 0,
    val completed: Boolean = false,
    val frequency: String = "0,1,2,3,4", // 逗号分隔的星期索引
    val startTime: String = "09:00",     // 每日开始提醒的时间
    val endTime: String = "23:59",       // 每日结束提醒的时间
    val reminderInterval: Int = 0,       // 提醒间隔(分钟), 0=仅提醒一次, >0=循环提醒直到24:00
    val createdAt: Long = System.currentTimeMillis(),
    val lastCompletedDate: String = "",   // yyyy-MM-dd 格式
    val lastRemindedDate: String = "",    // 上次提醒日期 (yyyy-MM-dd)
    val lastRemindedTime: Long = 0L,      // 上次提醒精确时间戳
    val streakDays: Int = 0,              // 连续完成天数
    val habitType: String = "NORMAL",     // "NORMAL" 或 "DURATION"
    val targetDuration: Int = 0,          // 目标持续时长 (分钟), 仅用于 DURATION 类型
    val actualStartTime: Long = 0L        // 实际点击“开始”的时间戳, 仅用于 DURATION 类型
)
