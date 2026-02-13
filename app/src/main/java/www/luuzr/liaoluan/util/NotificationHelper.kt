package www.luuzr.liaoluan.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
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
            // 获取系统默认提醒铃声，用于通知弹出时播放
            val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val habitChannel = NotificationChannel(
                CHANNEL_ID_HABIT,
                "习惯提醒",
                NotificationManager.IMPORTANCE_HIGH // HIGH 级别才能弹出横幅通知
            ).apply {
                description = "每日习惯提醒 — 弹窗通知"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500) // 强震动模式
                setSound(alarmSound, audioAttributes) // 使用闹钟铃声
                setBypassDnd(true) // 穿透免打扰模式
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC // 锁屏可见
            }

            val taskChannel = NotificationChannel(
                CHANNEL_ID_TASK,
                "任务提醒",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "任务间隔提醒 — 弹窗通知"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                setSound(alarmSound, audioAttributes)
                setBypassDnd(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            // 删除旧渠道再重建，确保新配置生效（渠道一旦创建，部分属性无法修改）
            notificationManager.deleteNotificationChannel(CHANNEL_ID_HABIT)
            notificationManager.deleteNotificationChannel(CHANNEL_ID_TASK)
            notificationManager.createNotificationChannel(habitChannel)
            notificationManager.createNotificationChannel(taskChannel)
        }
    }

    fun showHabitNotification(context: Context, habit: Habit) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, habit.id.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = if (habit.motivation.isNotBlank()) habit.motivation else "是时候完成习惯：${habit.text}"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_HABIT)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("⚡ 坚持一下！")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText)) // 展开显示完整文字
            .setPriority(NotificationCompat.PRIORITY_MAX) // 最高优先级，确保弹出
            .setCategory(NotificationCompat.CATEGORY_ALARM) // 标记为闹钟类别
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // 锁屏完整显示
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true) // 全屏意图：锁屏/息屏时直接亮屏弹出
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL) // 默认声音+震动+灯光

        try {
            NotificationManagerCompat.from(context).notify(habit.id.toInt(), builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun showTaskNotification(context: Context, task: Task) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("show_complete_dialog_task_id", task.id)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, task.id.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = if (task.reminderText.isNotBlank()) task.reminderText else "任务「${task.text}」完成了没有？"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_TASK)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("🔥 任务进度确认")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .addAction(0, "去确认", pendingIntent)

        try {
            NotificationManagerCompat.from(context).notify(task.id.toInt(), builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}
