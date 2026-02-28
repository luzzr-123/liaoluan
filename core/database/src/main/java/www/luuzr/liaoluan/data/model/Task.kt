package www.luuzr.liaoluan.data.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

/**
 * 任务领域模型 — 用于 UI 层和序列化导出
 */
@Immutable
@Serializable
data class Task(
    val id: Long = System.currentTimeMillis(),
    val text: String = "",
    val description: String = "",
    val tag: String = "工作",
    val priority: Priority = Priority.MEDIUM,
    val dueTimestamp: Long = 0,
    val completed: Boolean = false,
    val reminderInterval: Long = 0,
    val lastReminded: Long = 0,
    val reminderText: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val dueDate: String = "" // 为了兼容旧版 JSON 导入，保留此字段但不建议使用
)
