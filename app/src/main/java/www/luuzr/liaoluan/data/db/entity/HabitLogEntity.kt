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
@Entity(
    tableName = "habit_logs",
    indices = [Index(value = ["habitId"]), Index(value = ["date"])]
)
data class HabitLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val date: String,      // 格式: "yyyy-MM-dd"
    val progress: Int,
    val completed: Boolean
)
