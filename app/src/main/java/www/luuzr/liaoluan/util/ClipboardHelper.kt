package www.luuzr.liaoluan.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.core.content.ContextCompat

object ClipboardHelper {
    fun copyToClipboard(context: Context, label: String, text: String) {
        val clipboard = ContextCompat.getSystemService(context, ClipboardManager::class.java)
        val clip = ClipData.newPlainText(label, text)
        clipboard?.setPrimaryClip(clip)
        // Android 13+ has its own overlay, but for custom UI we might still show a toast or rely on our BrutalToast
    }

    fun getFromClipboard(context: Context): String? {
        val clipboard = ContextCompat.getSystemService(context, ClipboardManager::class.java)
        val clip = clipboard?.primaryClip
        if (clip != null && clip.itemCount > 0) {
            return clip.getItemAt(0).text.toString()
        }
        return null
    }
}
