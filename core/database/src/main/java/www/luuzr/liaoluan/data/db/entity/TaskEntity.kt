package www.luuzr.liaoluan.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 任务表 — 对应原型中的 Task 对象
 */
// M2 Fix: 排序辅助索引，消除 ORDER BY 临时排序开销
@Entity(
    tableName = "tasks",
    indices = [androidx.room.Index(value = ["completed", "createdAt"])]
)
data class TaskEntity(
    @PrimaryKey val id: Long,
    val text: String,
    val description: String = "",
    val tag: String = "工作",
    val priority: String = "MEDIUM", // Priority 枚举的 name
    val dueTimestamp: Long = 0,      // 精确到秒的时间戳 (0 = 无截止)
    val completed: Boolean = false,
    val reminderInterval: Long = 0, // 分钟，0=不提醒
    val lastReminded: Long = 0,     // 上次提醒时间戳
    val reminderText: String = "",  // 自定义提醒语
    val createdAt: Long = System.currentTimeMillis()
)
