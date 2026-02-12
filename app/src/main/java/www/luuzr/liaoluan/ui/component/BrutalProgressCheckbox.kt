package www.luuzr.liaoluan.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import www.luuzr.liaoluan.ui.theme.BrutalColors

/**
 * Brutalist 进度复选框 — 用于习惯打卡，支持多次按压递增
 * 交互：按住时黑色从底部向上填充，100% 后增加 step 值
 * 当 progress >= target 时显示完成态
 */
@Composable
fun BrutalProgressCheckbox(
    completed: Boolean,
    progress: Int,
    target: Int,
    onProgress: () -> Unit,
    modifier: Modifier = Modifier
) {
    var fillProgress by remember { mutableFloatStateOf(0f) }
    var isPressing by remember { mutableStateOf(false) }

    // 已完成的比例（背景半透明显示）
    val completedRatio = if (target > 1) (progress.toFloat() / target).coerceIn(0f, 1f) else 0f

    // 长按填充逻辑
    LaunchedEffect(isPressing) {
        if (isPressing && !completed) {
            fillProgress = 0f
            while (fillProgress < 1f) {
                delay(50)
                fillProgress = (fillProgress + 0.2f).coerceAtMost(1f)
            }
            onProgress()
            isPressing = false
            fillProgress = 0f
        } else if (!isPressing) {
            fillProgress = 0f
        }
    }

    val checkScale by animateFloatAsState(
        targetValue = if (completed) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 800f),
        label = "checkScale"
    )

    Box(
        modifier = modifier
            .size(56.dp)
            .border(4.dp, BrutalColors.Black)
            .background(BrutalColors.White)
            .pointerInput(completed) {
                detectTapGestures(
                    onPress = {
                        if (!completed) {
                            isPressing = true
                            tryAwaitRelease()
                            isPressing = false
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // 已完成比例的背景（半透明）
        if (!completed && target > 1) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(completedRatio)
                    .align(Alignment.BottomCenter)
                    .background(BrutalColors.Black.copy(alpha = 0.2f))
            )
        }

        // 当前按压的填充进度（从底部向上）
        if (!completed) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(fillProgress)
                    .align(Alignment.BottomCenter)
                    .background(BrutalColors.Black)
            )
        }

        // 进度文字 (仅 target > 1 时显示)
        if (!completed && target > 1) {
            Text(
                text = "$progress/$target",
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                color = BrutalColors.Black
            )
        }

        // 完成态
        if (completed) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BrutalColors.Black)
                    .scale(checkScale),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "完成",
                    tint = BrutalColors.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
