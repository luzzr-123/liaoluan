package www.luuzr.liaoluan.util

import android.content.Context
import android.content.Intent

/**
 * 系统分享工具 — 使用 Android 原生分享面板 (Intent.ACTION_SEND)
 * 替代直接复制到剪贴板的方式，让用户选择分享目标(微信/QQ/文件管理器等)
 */
object ShareHelper {

    /**
     * 分享纯文本数据 — 弹出系统分享面板
     * @param title 分享面板的标题（如"导出笔记"）
     * @param content 要分享的文本内容（JSON 或纯文本）
     */
    fun shareText(context: Context, title: String, content: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, content)
        }
        // 创建选择器，让用户选择分享方式
        val chooser = Intent.createChooser(intent, title)
        context.startActivity(chooser)
    }
}
