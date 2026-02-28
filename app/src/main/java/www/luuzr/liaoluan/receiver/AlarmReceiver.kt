package www.luuzr.liaoluan.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import www.luuzr.liaoluan.data.repository.PlannerRepository
import www.luuzr.liaoluan.util.ExactAlarmHelper
import www.luuzr.liaoluan.util.NotificationHelper
import javax.inject.Inject

/**
 * 接收 AlarmManager 的精确闹钟广播
 */
@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: PlannerRepository

    companion object {
        const val ACTION_HABIT_PRE_ALARM = "www.luuzr.liaoluan.ACTION_HABIT_PRE_ALARM"
        const val ACTION_HABIT_PROCESS_ALARM = "www.luuzr.liaoluan.ACTION_HABIT_PROCESS_ALARM"
        const val ACTION_HABIT_END_ALARM = "www.luuzr.liaoluan.ACTION_HABIT_END_ALARM"
        const val EXTRA_HABIT_ID = "extra_habit_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return

        // C2 Fix: goAsync() 延长 Receiver 生命周期，确保协程完成后再释放
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val habits = repository.habits.firstOrNull() ?: return@launch
                
                when (action) {
                    Intent.ACTION_DATE_CHANGED, Intent.ACTION_TIMEZONE_CHANGED -> {
                         val today = www.luuzr.liaoluan.util.DateHandle.todayDate()
                         
                         habits.forEach { h -> 
                             if (h.lastCompletedDate.isNotEmpty() && h.lastCompletedDate != today) {
                                 // P1-2 Fix: 正确计算这期间是否错过了任何应该打卡的日子
                                 val isBroken = isStreakBroken(h, today)
                                 
                                 if (h.progress > 0 || h.completed) {
                                     repository.updateHabit(
                                         h.copy(
                                             progress = 0, 
                                             completed = false, 
                                             streakDays = if (isBroken) 0 else h.streakDays, 
                                             actualStartTime = 0L
                                         )
                                     )
                                 }
                             }
                         }
                    }
                    ACTION_HABIT_PRE_ALARM, ACTION_HABIT_PROCESS_ALARM, ACTION_HABIT_END_ALARM -> {
                        val habitId = intent.getLongExtra(EXTRA_HABIT_ID, -1L)
                        if (habitId == -1L) return@launch
                        val habit = habits.find { it.id == habitId } ?: return@launch

                        if (habit.completed) return@launch

                        when (action) {
                            ACTION_HABIT_PRE_ALARM -> {
                                val calendar = java.util.Calendar.getInstance()
                                val dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK)
                                val currentDayIndex = (dayOfWeek - 2 + 7) % 7
                                if (habit.frequency.contains(currentDayIndex) && habit.habitType == "DURATION") {
                                    val fakeHabitForNotification = habit.copy(
                                        motivation = "预热! 你的习惯 '${habit.text}' 将在 5 分钟后开始！"
                                    )
                                    NotificationHelper.showHabitNotification(context, fakeHabitForNotification)
                                }
                                ExactAlarmHelper.scheduleHabitPreAlarm(context, habit)
                            }
                            ACTION_HABIT_PROCESS_ALARM -> {
                                if (habit.actualStartTime > 0L) {
                                    val fakeHabitForNotification = habit.copy(
                                        motivation = "保持专注! '${habit.text}' 正在进行中..."
                                    )
                                    NotificationHelper.showHabitNotification(context, fakeHabitForNotification)
                                    ExactAlarmHelper.scheduleHabitDurationProcess(context, habit, habit.reminderInterval)
                                }
                            }
                            ACTION_HABIT_END_ALARM -> {
                                if (habit.actualStartTime > 0L) {
                                    val fakeHabitForNotification = habit.copy(
                                        motivation = "太棒了! '${habit.text}' 的时长目标已达成！点击结束记录。"
                                    )
                                    NotificationHelper.showHabitNotification(context, fakeHabitForNotification)
                                }
                            }
                        }
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun isStreakBroken(habit: www.luuzr.liaoluan.data.model.Habit, todayStr: String): Boolean {
        if (habit.lastCompletedDate.isEmpty() || habit.lastCompletedDate >= todayStr) return false
        val todayMs = www.luuzr.liaoluan.util.DateHandle.parseDate(todayStr)
        val lastCompletedMs = www.luuzr.liaoluan.util.DateHandle.parseDate(habit.lastCompletedDate)
        if (lastCompletedMs >= todayMs) return false
        
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = todayMs
        cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
        
        while (cal.timeInMillis > lastCompletedMs) {
            val dayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK)
            val dayIndex = (dayOfWeek - 2 + 7) % 7
            if (habit.frequency.contains(dayIndex)) return true
            cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
        }
        return false
    }
}
