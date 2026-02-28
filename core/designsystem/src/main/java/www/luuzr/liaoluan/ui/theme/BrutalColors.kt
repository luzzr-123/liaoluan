package www.luuzr.liaoluan.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Brutalist 调色板 — 对应原型中的色彩系统
 * 高对比度、大色块、黑白为主体
 */
object BrutalColors {
    // 主色：三大模块背景色
    val TaskRed = Color(0xFFFF6B6B)      // 任务页背景
    val HabitTeal = Color(0xFF4ECDC4)    // 习惯页背景
    val NoteYellow = Color(0xFFFFE66D)   // 笔记页背景
    val HabitBlue = Color(0xFF3B82F6)    // 习惯辅助蓝


    // 基础色
    val Black = Color(0xFF000000)
    val White = Color(0xFFFFFFFF)
    val DarkGray = Color(0xFF333333)

    // 优先级色
    val PriorityHigh = Color(0xFFEF4444)   // 红色 !!!
    val PriorityMedium = Color(0xFFFBBF24) // 黄色 !!
    val PriorityLow = Color(0xFF4ADE80)    // 绿色 !

    // 心情色
    val MoodHappy = Color(0xFF4ADE80)
    val MoodNeutral = Color(0xFFFBBF24)
    val MoodSad = Color(0xFF60A5FA)
    val MoodStormy = Color(0xFF9CA3AF)

    // 辅助色
    val LightGray = Color(0xFFF3F4F6)
    val MediumGray = Color(0xFF9CA3AF)
    val BorderGray = Color(0xFFD1D5DB)
    val CheckGreen = Color(0xFF10B981)
    val AlarmRed = Color(0xFFFF4B4B) // Used for alerts and critical actions
}
