package www.luuzr.liaoluan.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import www.luuzr.liaoluan.MainActivity
import www.luuzr.liaoluan.R

class HabitTimerService : Service() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)
    
    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        
        const val EXTRA_HABIT_ID = "EXTRA_HABIT_ID"
        const val EXTRA_HABIT_NAME = "EXTRA_HABIT_NAME"
        const val EXTRA_ELAPSED_BASE = "EXTRA_ELAPSED_BASE" // H1 Fix: elapsedRealtime 基准
        const val EXTRA_PROGRESS = "EXTRA_PROGRESS"     // 已记录的进度 (分钟)
        
        private const val NOTIFICATION_ID = 9999
        private const val CHANNEL_ID = www.luuzr.liaoluan.util.NotificationHelper.CHANNEL_ID_HABIT
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val habitId = intent.getLongExtra(EXTRA_HABIT_ID, -1)
                val habitName = intent.getStringExtra(EXTRA_HABIT_NAME) ?: "专注习惯"
                // H1 Fix: 使用 elapsedRealtime 代替 System.currentTimeMillis
                val elapsedBase = intent.getLongExtra(EXTRA_ELAPSED_BASE, SystemClock.elapsedRealtime())
                val initialProgressMinutes = intent.getIntExtra(EXTRA_PROGRESS, 0)
                
                startForegroundTimer(habitId, habitName, elapsedBase, initialProgressMinutes)
            }
            ACTION_STOP -> {
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun startForegroundTimer(habitId: Long, habitName: String, elapsedBase: Long, initialProgressMinutes: Int) {
        
        val notification = createNotification(habitName, "00:00")
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID, 
                notification, 
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        
        // H1 Fix: 使用 elapsedRealtime 计算专注时长，抵御系统时间篡改
        scope.launch {
            while (isActive) {
                val elapsedMs = SystemClock.elapsedRealtime() - elapsedBase + (initialProgressMinutes * 60000L)
                val elapsedMin = (elapsedMs / 1000) / 60
                val elapsedSec = (elapsedMs / 1000) % 60
                val timeStr = "%02d:%02d".format(elapsedMin, elapsedSec)
                
                val updatedNotification = createNotification(habitName, "已专注时长: $timeStr")
                val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.notify(NOTIFICATION_ID, updatedNotification)
                
                delay(1000)
            }
        }
    }

    private fun createNotification(title: String, content: String): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        
        val pendingIntentFlags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, pendingIntentFlags)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_launcher) // Assuming this exists
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
