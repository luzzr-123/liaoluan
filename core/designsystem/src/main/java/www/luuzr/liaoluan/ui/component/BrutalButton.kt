package www.luuzr.liaoluan.ui.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import www.luuzr.liaoluan.ui.theme.BrutalColors

/**
 * Brutalist 按钮 — 硬阴影 + 按压时位移（原型中的 BrutalButton）
 * 效果：6px 黑色硬阴影 → hover/press 时阴影缩短 + 按钮位移
 */
@Composable
fun BrutalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = BrutalColors.White,
    textColor: Color = BrutalColors.Black,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 按压时阴影缩小+位移，模拟原型的 active:shadow-none + translate
    val offsetX by animateDpAsState(if (isPressed) 4.dp else 0.dp, label = "offsetX")
    val offsetY by animateDpAsState(if (isPressed) 4.dp else 0.dp, label = "offsetY")
    val shadowX by animateDpAsState(if (isPressed) 2.dp else 6.dp, label = "shadowX")
    val shadowY by animateDpAsState(if (isPressed) 2.dp else 6.dp, label = "shadowY")

    Box(modifier = modifier) {
        // 阴影层 — 纯黑矩形
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset { androidx.compose.ui.unit.IntOffset(shadowX.roundToPx(), shadowY.roundToPx()) }
                .background(BrutalColors.Black)
        )
        // 按钮主体
        Box(
            modifier = Modifier
                .offset { androidx.compose.ui.unit.IntOffset(offsetX.roundToPx(), offsetY.roundToPx()) }
                .background(backgroundColor)
                .border(4.dp, BrutalColors.Black)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    onClick = onClick
                )
                .padding(horizontal = 24.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text.uppercase(),
                color = textColor,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
