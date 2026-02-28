package www.luuzr.liaoluan.ui.viewmodel

import androidx.compose.runtime.Immutable
import www.luuzr.liaoluan.data.model.Habit
import www.luuzr.liaoluan.data.model.Note
import www.luuzr.liaoluan.data.model.Task
import www.luuzr.liaoluan.data.db.entity.HabitLogEntity

@Immutable
data class MainViewState(
    // 导航与页面状态
    val currentTab: Int = 1,          // 0=任务 1=习惯 2=笔记
    val showModal: Boolean = false,
    val showSettings: Boolean = false,
    val showHabitManagement: Boolean = false,
    val showStats: Boolean = false,
    val showExactAlarmPermissionDialog: Boolean = false,
    
    // 弹窗编辑状态
    val editingTask: Task? = null,
    val editingHabit: Habit? = null,
    val editingNote: Note? = null,
    
    // 交互反馈
    val toast: ToastData? = null,
    val showParticles: Boolean = false,

    // 核心数据 (从 Repository 映射)
    val tasks: List<Task> = emptyList(),
    val habits: List<Habit> = emptyList(),
    val notes: List<Note> = emptyList(),
    val selectedDateLogs: List<HabitLogEntity> = emptyList(),
    
    // 衍生数据与查询状态
    val noteSearchQuery: String = "",
    val filteredNotes: List<Note> = emptyList(),
    val selectedDate: String = www.luuzr.liaoluan.util.DateHandle.todayDate(),
    val visibleHabits: List<Habit> = emptyList()
)
