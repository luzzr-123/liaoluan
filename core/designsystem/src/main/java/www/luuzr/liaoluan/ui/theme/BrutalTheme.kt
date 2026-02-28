package www.luuzr.liaoluan.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Brutalist 主题包装 — 粗犷的新粗野主义风格
 * 特点：极粗字重、大写字母、宽字距
 */

// 自定义排版样式（不使用 Material Typography，因为 Brutalist 更极端）
object BrutalTypography {
    val displayLarge = TextStyle(
        fontWeight = FontWeight.Black,
        fontSize = 48.sp,
        letterSpacing = (-1).sp,
        fontStyle = FontStyle.Italic
    )
    val displayMedium = TextStyle(
        fontWeight = FontWeight.Black,
        fontSize = 36.sp,
        letterSpacing = (-0.5).sp
    )
    val titleLarge = TextStyle(
        fontWeight = FontWeight.Black,
        fontSize = 28.sp,
        letterSpacing = 1.sp
    )
    val titleMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp
    )
    val bodyLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        fontFamily = FontFamily.Monospace
    )
    val bodyMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        fontFamily = FontFamily.Monospace
    )
    val labelLarge = TextStyle(
        fontWeight = FontWeight.Black,
        fontSize = 14.sp,
        letterSpacing = 2.sp
    )
    val labelSmall = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        fontFamily = FontFamily.Monospace
    )
}

private val BrutalColorScheme = lightColorScheme(
    primary = BrutalColors.Black,
    onPrimary = BrutalColors.White,
    secondary = BrutalColors.NoteYellow,
    onSecondary = BrutalColors.Black,
    background = BrutalColors.White,
    onBackground = BrutalColors.Black,
    surface = BrutalColors.White,
    onSurface = BrutalColors.Black,
    error = BrutalColors.TaskRed,
    onError = BrutalColors.White
)

@Composable
fun BrutalTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BrutalColorScheme,
        content = content
    )
}
