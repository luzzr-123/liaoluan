package www.luuzr.liaoluan.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar // Add this

object DateHandle {
    // ThreadLocal ensures thread safety for SimpleDateFormat
    private val dateFormat = ThreadLocal.withInitial {
        SimpleDateFormat("yyyy-MM-dd", Locale.US)
    }

    private val timeFormat = ThreadLocal.withInitial {
        SimpleDateFormat("HH:mm", Locale.US)
    }

    private val dateTimeFormat = ThreadLocal.withInitial {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    }

    fun todayDate(): String {
        return dateFormat.get().format(Date())
    }

    fun formatDate(timestamp: Long): String {
        return dateFormat.get()?.format(Date(timestamp)) ?: ""
    }

    fun formatTime(timestamp: Long): String {
        return timeFormat.get()?.format(Date(timestamp)) ?: ""
    }

    fun formatDateTime(timestamp: Long): String {
        return dateTimeFormat.get()?.format(Date(timestamp)) ?: ""
    }
    
    fun getDayOfWeekIndex(dateStr: String): Int {
        val date = dateFormat.get().parse(dateStr) ?: return 0
        val cal = Calendar.getInstance()
        cal.time = date
        // Calendar.SUNDAY=1, MONDAY=2...
        // We want 0=Mon, ... 6=Sun
        val day = cal.get(Calendar.DAY_OF_WEEK)
        return if (day == Calendar.SUNDAY) 6 else day - 2
    }
    
    fun parseDate(dateStr: String): Long {
        return try {
            dateFormat.get()?.parse(dateStr)?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
}
