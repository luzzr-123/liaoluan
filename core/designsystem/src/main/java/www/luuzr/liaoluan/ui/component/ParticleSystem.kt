package www.luuzr.liaoluan.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import www.luuzr.liaoluan.ui.theme.BrutalColors
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * 粒子爆炸系统 — 完成任务/习惯时触发
 * 12 个小方块/圆形向四周飞散并消失
 * 对应原型的 ParticleSystem 和 @keyframes explode
 */
@Composable
fun ParticleSystem(
    active: Boolean,
    modifier: Modifier = Modifier,
    particleCount: Int = 12
) {
    if (!active) return

    Box(modifier = modifier.fillMaxSize()) {
        repeat(particleCount) { i ->
            val angle = (i.toFloat() / particleCount) * 360f
            val distance = Random.nextFloat() * 80f + 40f
            val targetX = cos(Math.toRadians(angle.toDouble())).toFloat() * distance
            val targetY = sin(Math.toRadians(angle.toDouble())).toFloat() * distance
            val targetRot = Random.nextFloat() * 360f
            val delayMs = (Random.nextFloat() * 100).toInt()

            SingleParticle(
                targetX = targetX,
                targetY = targetY,
                targetRotation = targetRot,
                delayMs = delayMs,
                isCircle = i % 2 != 0
            )
        }
    }
}

@Composable
private fun SingleParticle(
    targetX: Float,
    targetY: Float,
    targetRotation: Float,
    delayMs: Int,
    isCircle: Boolean
) {
    // 动画进度 0f→1f，600ms，带初始延迟
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delayMs.toLong())
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 600,
                easing = FastOutSlowInEasing
            )
        )
    }



    Box(
        modifier = Modifier
            .offset {
                val p = progress.value
                androidx.compose.ui.unit.IntOffset(
                    (targetX * p).dp.roundToPx(),
                    (targetY * p).dp.roundToPx()
                )
            }
            .graphicsLayer {
                val p = progress.value
                rotationZ = targetRotation * p
                scaleX = 1f - p
                scaleY = 1f - p
                alpha = 1f - p
            }
            .size(12.dp)
            .background(
                color = BrutalColors.Black,
                shape = if (isCircle) CircleShape else RectangleShape
            )
    )
}
