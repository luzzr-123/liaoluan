package www.luuzr.liaoluan.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import www.luuzr.liaoluan.data.model.Habit
import www.luuzr.liaoluan.receiver.AlarmReceiver
import java.util.Calendar

/**
 * 负责精确调度 AlarmManager
 */
object ExactAlarmHelper {

    fun scheduleHabitPreAlarm(context: Context, habit: Habit): Boolean {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                // 没有权限则跳过并返回 false 交由上层处理
                return false
            }
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_HABIT_PRE_ALARM
            putExtra(AlarmReceiver.EXTRA_HABIT_ID, habit.id)
        }
        
        // H2 Fix: 安全 requestCode，避免 Long→Int 溢出碰撞
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (habit.id % Int.MAX_VALUE).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 解析 startTime，提早 5 分钟
        val parts = habit.startTime.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 9
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, h)
            set(Calendar.MINUTE, m)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.MINUTE, -5)
        }

        // 如果提前 5 分钟的时间已经过了，并且是今天，如果允许则定到明天
        if (cal.timeInMillis <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            cal.timeInMillis,
            pendingIntent
        )
        return true
    }
    
    fun scheduleHabitDurationProcess(context: Context, habit: Habit, intervalMinutes: Int): Boolean {
        if (intervalMinutes <= 0) return true
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) return false
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_HABIT_PROCESS_ALARM
            putExtra(AlarmReceiver.EXTRA_HABIT_ID, habit.id)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ((habit.id + 100000) % Int.MAX_VALUE).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = System.currentTimeMillis() + (intervalMinutes * 60000L)
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
        return true
    }

    fun scheduleHabitDurationEnd(context: Context, habit: Habit): Boolean {
        if (habit.targetDuration <= 0) return true
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) return false
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_HABIT_END_ALARM
            putExtra(AlarmReceiver.EXTRA_HABIT_ID, habit.id)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ((habit.id + 200000) % Int.MAX_VALUE).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val remainingMinutes = habit.targetDuration - habit.progress
        if (remainingMinutes <= 0) return true
        
        // BUG-1 Fix: actualStartTime 现在存的是 elapsedRealtime 基准
        // 使用 ELAPSED_REALTIME_WAKEUP 来匹配
        val triggerTime = habit.actualStartTime + (remainingMinutes * 60000L)

        alarmManager.cancel(pendingIntent)
        
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            triggerTime,
            pendingIntent
        )
        return true
    }

    fun cancelAllHabitAlarms(context: Context, habitId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val intentPre = Intent(context, AlarmReceiver::class.java).apply { action = AlarmReceiver.ACTION_HABIT_PRE_ALARM }
        val intentProcess = Intent(context, AlarmReceiver::class.java).apply { action = AlarmReceiver.ACTION_HABIT_PROCESS_ALARM }
        val intentEnd = Intent(context, AlarmReceiver::class.java).apply { action = AlarmReceiver.ACTION_HABIT_END_ALARM }

        val piPre = PendingIntent.getBroadcast(context, (habitId % Int.MAX_VALUE).toInt(), intentPre, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val piProcess = PendingIntent.getBroadcast(context, ((habitId + 100000) % Int.MAX_VALUE).toInt(), intentProcess, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val piEnd = PendingIntent.getBroadcast(context, ((habitId + 200000) % Int.MAX_VALUE).toInt(), intentEnd, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        alarmManager.cancel(piPre)
        alarmManager.cancel(piProcess)
        alarmManager.cancel(piEnd)
    }
}
