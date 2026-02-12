package www.luuzr.liaoluan.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import www.luuzr.liaoluan.data.repository.PlannerRepository
import www.luuzr.liaoluan.util.NotificationHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@HiltWorker
class TaskReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: PlannerRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val tasks = repository.tasks.first()
            val now = System.currentTimeMillis()

            tasks.forEach { task ->
                if (!task.completed) {
                    // 1. 检查截止日期提醒 (精确到秒)
                    if (task.dueTimestamp > 0 && now >= task.dueTimestamp) {
                        // 如果 lastReminded < dueTimestamp，说明还没针对截止时间提醒过
                        if (task.lastReminded < task.dueTimestamp) {
                            NotificationHelper.showTaskNotification(applicationContext, task)
                            repository.updateTask(task.copy(lastReminded = now))
                        }
                    } 
                    // 2. 检查循环提醒 (基于 reminderInterval)
                    else if (task.reminderInterval > 0) {
                        val intervalMillis = task.reminderInterval * 60 * 1000
                        val baseTime = if (task.lastReminded > 0) task.lastReminded else task.createdAt
                        
                        // 检查是否到达下一次提醒时间
                        if (now >= baseTime + intervalMillis) {
                            NotificationHelper.showTaskNotification(applicationContext, task)
                            repository.updateTask(task.copy(lastReminded = now))
                        }
                    }
                }
            }
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}

@HiltWorker
class HabitReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: PlannerRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val habits = repository.habits.first()
            val now = System.currentTimeMillis()
            val calendar = Calendar.getInstance().apply { timeInMillis = now }
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            val currentMinute = calendar.get(Calendar.MINUTE)
            val todayDate = www.luuzr.liaoluan.util.DateHandle.todayDate()
            
            // 当前是周几? (Calendar.DAY_OF_WEEK: Sun=1, Mon=2...Sat=7)
            // 我们的 habit.frequency: 0=Mon, 1=Tue...6=Sun
            // 转换: (CalendarDay - 2 + 7) % 7
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            val currentDayIndex = (dayOfWeek - 2 + 7) % 7

            habits.forEach { habit ->
                // 1. 检查是否完成
                if (habit.completed) return@forEach

                // 2. 检查今天是否需要执行 (frequency)
                if (!habit.frequency.contains(currentDayIndex)) return@forEach

                // 3. 检查时间窗口 (支持跨夜设置, e.g. 23:00 - 01:00)
                val startParts = habit.startTime.split(":")
                val endParts = habit.endTime.split(":")
                
                val startHour = startParts.getOrNull(0)?.toIntOrNull() ?: 9
                val startMinute = startParts.getOrNull(1)?.toIntOrNull() ?: 0
                val startMinutesOfDay = startHour * 60 + startMinute
                
                val endHour = endParts.getOrNull(0)?.toIntOrNull() ?: 23
                val endMinute = endParts.getOrNull(1)?.toIntOrNull() ?: 59
                val endMinutesOfDay = endHour * 60 + endMinute

                val currentMinutesOfDay = currentHour * 60 + currentMinute
                
                // 判断是否处于有效时间段
                // 情况A: start <= end (当日内, e.g. 09:00 - 21:00)
                // 情况B: start > end (跨夜, e.g. 22:00 - 02:00)
                val isCrossNight = startMinutesOfDay > endMinutesOfDay
                val inTimeWindow = if (isCrossNight) {
                    currentMinutesOfDay >= startMinutesOfDay || currentMinutesOfDay <= endMinutesOfDay
                } else {
                    currentMinutesOfDay in startMinutesOfDay..endMinutesOfDay
                }

                // 只有在时间窗口内才进行后续判断
                if (inTimeWindow) {

                    // 4. 检查提醒间隔
                    if (habit.reminderInterval > 0) {
                        // 循环提醒逻辑: 检查距离上次提醒是否超过间隔
                        val intervalMillis = habit.reminderInterval * 60 * 1000L
                        
                        // 特殊情况：如果是今天第一次进入窗口，且 lastRemindedDate 不是今天，则立即提醒
                        // 对于跨夜任务，我们需要更小心。
                        // 如果 lastRemindedDate 是昨天，且现在已经是今天的凌晨 (跨夜的后半段)，这也算"未提醒"吗？
                        // 应该算。因为每天都需要打卡。
                        
                        if (habit.lastRemindedDate != todayDate) {
                             NotificationHelper.showHabitNotification(applicationContext, habit)
                             repository.updateHabit(habit.copy(
                                 lastRemindedDate = todayDate,
                                 lastRemindedTime = now
                             ))
                        } else {
                            // 今天已经提醒过，检查间隔
                            if (now - habit.lastRemindedTime >= intervalMillis) {
                                NotificationHelper.showHabitNotification(applicationContext, habit)
                                repository.updateHabit(habit.copy(
                                    lastRemindedDate = todayDate,
                                    lastRemindedTime = now
                                ))
                            }
                        }
                    } else {
                        // 单次提醒逻辑 (reminderInterval == 0)
                        // 若处于时间窗口内，且今天从未提醒过 -> 提醒
                        if (habit.lastRemindedDate != todayDate) {
                            NotificationHelper.showHabitNotification(applicationContext, habit)
                            repository.updateHabit(habit.copy(
                                lastRemindedDate = todayDate,
                                lastRemindedTime = now
                            ))
                        }
                    }
                }
            }
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}
