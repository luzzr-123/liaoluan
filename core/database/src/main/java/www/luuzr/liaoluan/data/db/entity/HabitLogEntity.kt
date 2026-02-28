package www.luuzr.liaoluan.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable // Add this

/**
 * 习惯历史记录表 (v7 新增)
 * 用于记录习惯在每一天的完成情况 (Progress/Completed)
 * 之前的 HabitEntity 只存 "当前状态" (Today)，
 * 这个表存 "过去状态" (History)。
 */
@Serializable // Add this
@androidx.compose.runtime.Immutable
@Entity(
    tableName = "habit_logs",
    // M1 Fix: 唯一复合索引，大幅提升 getLog(habitId, date) 查询性能
    indices = [Index(value = ["habitId", "date"], unique = true)]
)
data class HabitLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val date: String,      // 格式: "yyyy-MM-dd"
    val progress: Int,
    val completed: Boolean
)
