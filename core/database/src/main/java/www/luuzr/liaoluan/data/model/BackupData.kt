package www.luuzr.liaoluan.data.model

import kotlinx.serialization.Serializable

/**
 * 全量备份数据结构 — 用于 JSON 导入/导出
 */
@Serializable
data class BackupData(
    val tasks: List<Task>? = null,
    val habits: List<Habit>? = null,
    val habitLogs: List<www.luuzr.liaoluan.data.db.entity.HabitLogEntity>? = null, // Add this
    val notes: List<Note>? = null,
    val dataType: String? = null // 单条导入时标识类型: "task" / "habit" / "note"
)
