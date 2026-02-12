package www.luuzr.liaoluan.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateHandle {
    // ThreadLocal ensures thread safety for SimpleDateFormat
    private val dateFormat = ThreadLocal.withInitial {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    }

    private val timeFormat = ThreadLocal.withInitial {
        SimpleDateFormat("HH:mm", Locale.getDefault())
    }

    private val dateTimeFormat = ThreadLocal.withInitial {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    }

    fun todayDate(): String {
        return dateFormat.get()?.format(Date()) ?: ""
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
    
    fun parseDate(dateStr: String): Long {
        return try {
            dateFormat.get()?.parse(dateStr)?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
}
