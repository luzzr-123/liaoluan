package www.luuzr.liaoluan.ui.viewmodel

import www.luuzr.liaoluan.data.model.BackupData
import www.luuzr.liaoluan.data.model.Habit
import www.luuzr.liaoluan.data.model.Note
import www.luuzr.liaoluan.data.model.Task

sealed interface MainIntent {
    // 导航与全屏 Modal
    data class SwitchTab(val index: Int) : MainIntent
    object OpenNewModal : MainIntent
    data class OpenEditTaskModal(val task: Task) : MainIntent
    data class OpenEditHabitModal(val habit: Habit) : MainIntent
    data class OpenEditNoteModal(val note: Note) : MainIntent
    object CloseModal : MainIntent
    object ToggleSettings : MainIntent
    object ToggleHabitManagement : MainIntent
    object ToggleStats : MainIntent
    object HandleBackPress : MainIntent
    object CheckCrossDayReset : MainIntent
    
    // 权限处理
    object RequestExactAlarmPermission : MainIntent
    object DismissExactAlarmPermissionDialog : MainIntent

    // 任务操作
    data class SaveTask(val task: Task) : MainIntent
    data class ToggleTaskComplete(val taskId: Long, val completed: Boolean) : MainIntent
    data class DeleteTask(val taskId: Long) : MainIntent

    // 习惯操作
    data class SaveHabit(val habit: Habit) : MainIntent
    data class ProgressHabit(val habitId: Long) : MainIntent
    data class StartHabitDuration(val habit: Habit) : MainIntent
    data class EndHabitDuration(val habit: Habit) : MainIntent
    data class DeleteHabit(val habitId: Long) : MainIntent

    // 笔记操作
    data class SaveNote(val note: Note) : MainIntent
    data class DeleteNote(val noteId: Long) : MainIntent
    data class ToggleNotePin(val noteId: Long) : MainIntent
    data class SearchNotes(val query: String) : MainIntent

    // 日期与统计
    data class SelectDate(val date: String) : MainIntent

    // 导入与导出
    data class ImportBackup(val backup: BackupData) : MainIntent
    data class ImportSingleItem(val jsonStr: String) : MainIntent

    // 提示及效果交互
    data class ShowToast(val message: String, val onUndo: (() -> Unit)? = null) : MainIntent
    object DismissToast : MainIntent
    object TriggerParticles : MainIntent
}
