package www.luuzr.liaoluan.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import www.luuzr.liaoluan.ui.theme.BrutalColors

/**
 * 可编辑卡片容器 — 长按 600ms 触发编辑模式
 * 交互：按压时缩小 + 光环 + "编辑模式..." 提示
 * 对应原型的 EditableCard 和 useLongPress hook
 */
@Composable
fun EditableCard(
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    var isPressing by remember { mutableStateOf(false) }
    var longPressTriggered by remember { mutableStateOf(false) }

    // 长按检测
    LaunchedEffect(isPressing) {
        if (isPressing) {
            longPressTriggered = false
            delay(600) // 600ms 长按阈值
            longPressTriggered = true
            onEdit()
            isPressing = false
        }
    }

    // 按压时的缩放效果
    val scale by animateFloatAsState(
        targetValue = if (isPressing) 0.95f else 1f,
        label = "editableScale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressing = true
                        tryAwaitRelease()
                        isPressing = false
                    }
                )
            }
    ) {
        content()

        // 按压时的编辑模式提示
        if (isPressing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(50f)
                    .background(BrutalColors.Black.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .background(BrutalColors.White)
                        .border(2.dp, BrutalColors.Black)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "编辑模式...",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = BrutalColors.Black
                    )
                }
            }
        }
    }
}
