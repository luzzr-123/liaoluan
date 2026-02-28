package www.luuzr.liaoluan.ui.component

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import www.luuzr.liaoluan.ui.theme.BrutalColors

/**
 * Brutalist Toast — 底部黑底白字通知 + 撤销按钮
 * 对应原型的 BrutalToast 组件
 */
@Composable
fun BrutalToast(
    message: String,
    onUndo: (() -> Unit)? = null,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BrutalColors.Black)
                .border(4.dp, BrutalColors.White)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 消息文本
            Text(
                text = message.uppercase(),
                color = BrutalColors.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                letterSpacing = 1.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            // 撤销按钮
            if (onUndo != null) {
                Row(
                    modifier = Modifier
                        .background(BrutalColors.NoteYellow)
                        .border(2.dp, BrutalColors.White)
                        .clickable { onUndo() }
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Undo,
                        contentDescription = "撤销",
                        tint = BrutalColors.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "撤销",
                        color = BrutalColors.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }
            }

            // 关闭按钮
            Icon(
                Icons.Default.Close,
                contentDescription = "关闭",
                tint = BrutalColors.White,
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onClose() }
            )
        }
    }
}
