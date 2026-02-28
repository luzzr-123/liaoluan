package www.luuzr.liaoluan.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import www.luuzr.liaoluan.ui.theme.BrutalColors

/**
 * 印章覆盖层 — 完成任务/习惯时的"DONE!"/"NICE!" 印章动画
 * 动画：scale(3)→scale(0.8)→scale(1) + 旋转 -12°
 * 对应原型的 Stamp 组件和 @keyframes stamp-in
 */
@Composable
fun StampOverlay(
    text: String,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    if (!visible) return

    // 印章弹入动画 — 模拟 cubic-bezier(0.175, 0.885, 0.32, 1.275)
    val animScale = remember { Animatable(3f) }

    LaunchedEffect(visible) {
        if (visible) {
            animScale.snapTo(3f)
            animScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.5f,    // 有弹性的过冲效果
                    stiffness = 400f
                )
            )
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .scale(animScale.value)
                .rotate(-12f)
                .background(BrutalColors.Black.copy(alpha = 0.5f))
                .border(8.dp, BrutalColors.White)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = text,
                color = BrutalColors.White,
                fontWeight = FontWeight.Black,
                fontSize = 42.sp
            )
        }
    }
}
