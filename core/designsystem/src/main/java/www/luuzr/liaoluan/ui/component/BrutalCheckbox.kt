package www.luuzr.liaoluan.ui.component

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import www.luuzr.liaoluan.ui.theme.BrutalColors

/**
 * Brutalist 复选框 — 长按填充动画
 * 交互：按住时黑色从左到右填充，填满100%后触发完成
 * 已完成时显示白色对勾 + 黑色背景，单击可取消
 */
@Composable
fun BrutalCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    // 填充进度 0f..1f
    var fillProgress by remember { mutableFloatStateOf(0f) }
    var isPressing by remember { mutableStateOf(false) }

    // 长按填充动画逻辑
    LaunchedEffect(isPressing) {
        if (isPressing && !checked) {
            fillProgress = 0f
            // 每 40ms 增加 20%，共 200ms 填满（对应原型的 40ms interval × 5 steps）
            while (fillProgress < 1f) {
                delay(40)
                fillProgress = (fillProgress + 0.2f).coerceAtMost(1f)
            }
            onCheckedChange(true)
            isPressing = false
            fillProgress = 0f
        } else if (!isPressing) {
            fillProgress = 0f
        }
    }

    // 完成态缩放动画
    val checkScale by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 800f),
        label = "checkScale"
    )

    Box(
        modifier = modifier
            .size(48.dp)
            .border(4.dp, BrutalColors.Black)
            .background(BrutalColors.White)
            .pointerInput(checked) {
                detectTapGestures(
                    onPress = {
                        if (!checked) {
                            isPressing = true
                            // 等待手指抬起
                            tryAwaitRelease()
                            isPressing = false
                        }
                    },
                    onTap = {
                        // 已完成时点击可取消
                        if (checked) onCheckedChange(false)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // 填充进度条（从左到右）
        if (!checked) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fillProgress)
                    .align(Alignment.CenterStart)
                    .background(BrutalColors.Black)
            )
        }

        // 完成态：黑底白勾
        if (checked) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BrutalColors.Black)
                    .scale(checkScale),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "已完成",
                    tint = BrutalColors.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
