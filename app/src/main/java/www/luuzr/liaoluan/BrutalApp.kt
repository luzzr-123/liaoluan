package www.luuzr.liaoluan

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import www.luuzr.liaoluan.util.NotificationHelper
import www.luuzr.liaoluan.worker.HabitReminderWorker
import www.luuzr.liaoluan.worker.TaskReminderWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Hilt 入口 — 标记整个应用使用依赖注入
 * 配置 WorkManager
 */
@HiltAndroidApp
class BrutalApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() {
            // 防御性编程：防止 Hilt 尚未注入时 WorkManager 尝试获取配置导致崩溃
            val factory = if (::workerFactory.isInitialized) {
                workerFactory
            } else {
                null
            }
            
            val builder = Configuration.Builder()
            if (factory != null) {
                builder.setWorkerFactory(factory)
            }
            return builder.build()
        }

    override fun onCreate() {
        super.onCreate()

        // C4 Fix: 全局异常捕获增加崩溃日志本地持久化
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            // 写入崩溃日志到本地文件
            try {
                val crashDir = java.io.File(filesDir, "crash_logs")
                if (!crashDir.exists()) crashDir.mkdirs()
                // 保留最后 5 次崩溃
                val existingLogs = crashDir.listFiles()?.sortedBy { it.lastModified() } ?: emptyList()
                if (existingLogs.size >= 5) {
                    existingLogs.take(existingLogs.size - 4).forEach { it.delete() }
                }
                val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US).format(java.util.Date())
                val logFile = java.io.File(crashDir, "crash_$timestamp.txt")
                logFile.writeText("Thread: ${thread.name}\n${android.util.Log.getStackTraceString(throwable)}")
            } catch (_: Exception) { /* 写日志本身不能再抛异常 */ }

            android.os.Handler(android.os.Looper.getMainLooper()).post {
                try {
                    android.widget.Toast.makeText(
                        this,
                        "CRASH: ${throwable.message}",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                } catch (e: Exception) {
                    // Ignore
                }
            }
            // 给 Toast 一点时间显示，然后退出
            try { Thread.sleep(2000) } catch (e: InterruptedException) {}
            android.os.Process.killProcess(android.os.Process.myPid())
            System.exit(10)
        }

        try {
            // 1. 创建通知渠道
            NotificationHelper.createNotificationChannels(this)

            // 恢复后台调度 (使用 UPDATE 覆盖旧任务，确保 Worker 类名正确)
            // 任务提醒: 每 15 分钟检查一次
            val taskReq = PeriodicWorkRequestBuilder<TaskReminderWorker>(15, TimeUnit.MINUTES)
                .build()
            
            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "TaskReminderWork",
                ExistingPeriodicWorkPolicy.UPDATE,
                taskReq
            )

            // 习惯提醒: 每 15 分钟检查一次 (检查是否到了设定时间)
            val habitReq = PeriodicWorkRequestBuilder<HabitReminderWorker>(15, TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "HabitReminderWork",
                ExistingPeriodicWorkPolicy.UPDATE,
                habitReq
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
