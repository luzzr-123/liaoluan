package www.luuzr.liaoluan.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import www.luuzr.liaoluan.MainActivity
import www.luuzr.liaoluan.R
import www.luuzr.liaoluan.data.model.Habit
import www.luuzr.liaoluan.data.model.Task

object NotificationHelper {

    const val CHANNEL_ID_HABIT = "habit_channel"
    const val CHANNEL_ID_TASK = "task_channel"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val habitChannel = NotificationChannel(
                CHANNEL_ID_HABIT,
                "习惯提醒",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "每日习惯提醒"
                enableVibration(true)
            }

            val taskChannel = NotificationChannel(
                CHANNEL_ID_TASK,
                "任务提醒",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "任务间隔提醒"
                enableVibration(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(habitChannel)
            notificationManager.createNotificationChannel(taskChannel)
        }
    }

    fun showHabitNotification(context: Context, habit: Habit) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_HABIT)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // 替换为应用图标
            .setContentTitle("坚持一下！")
            .setContentText(if (habit.motivation.isNotBlank()) habit.motivation else "是时候完成习惯：${habit.text}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(habit.id.toInt(), builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun showTaskNotification(context: Context, task: Task) {
        // 点击通知打开 App 并触发确认对话框
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("show_complete_dialog_task_id", task.id)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, task.id.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_TASK)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("任务进度确认")
            .setContentText(if (task.reminderText.isNotBlank()) task.reminderText else "任务「${task.text}」完成了没有？")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(0, "去确认", pendingIntent)

        try {
            NotificationManagerCompat.from(context).notify(task.id.toInt(), builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}
