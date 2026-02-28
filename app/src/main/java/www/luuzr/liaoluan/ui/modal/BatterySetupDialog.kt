package www.luuzr.liaoluan.ui.modal

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import www.luuzr.liaoluan.ui.component.BrutalButton
import www.luuzr.liaoluan.ui.theme.BrutalColors
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import www.luuzr.liaoluan.util.BatteryOptHelper

@Composable
fun BatterySetupDialog(
    context: Context,
    onClose: () -> Unit
) {
    val needsBatteryOpt = !BatteryOptHelper.isIgnoringBatteryOptimizations(context)
    val needsExactAlarm = !BatteryOptHelper.hasExactAlarmPermission(context)

    // If both are already fine, just close it (though we shouldn't have shown it ideally)
    if (!needsBatteryOpt && !needsExactAlarm) {
        onClose()
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrutalColors.Black.copy(alpha = 0.8f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            // Shadow
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 12.dp, y = 12.dp)
                    .background(BrutalColors.AlarmRed)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BrutalColors.White)
                    .border(4.dp, BrutalColors.Black)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BrutalColors.Black)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "稳定提醒保障",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = BrutalColors.White,
                        letterSpacing = 2.sp
                    )
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "关闭",
                        tint = BrutalColors.White,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onClose() }
                    )
                }

                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "为了确保你的「时长习惯」和「预热提醒」能够一秒不差地准时触发，我们需要进行两项系统级授权。",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrutalColors.Black
                    )

                    if (needsBatteryOpt) {
                        BrutalButton(
                            text = "1. 将 App 移出电池优化白名单",
                            onClick = { BatteryOptHelper.requestIgnoreBatteryOptimizations(context) },
                            backgroundColor = BrutalColors.NoteYellow,
                            modifier = Modifier.fillMaxWidth().height(56.dp)
                        )
                    } else {
                        BrutalButton(
                            text = "1. 电池优化已配置 ✓",
                            onClick = { },
                            backgroundColor = BrutalColors.CheckGreen,
                            modifier = Modifier.fillMaxWidth().height(56.dp)
                        )
                    }

                    if (needsExactAlarm) {
                        BrutalButton(
                            text = "2. 授予精准闹钟权限",
                            onClick = { BatteryOptHelper.requestExactAlarmPermission(context) },
                            backgroundColor = BrutalColors.NoteYellow,
                            modifier = Modifier.fillMaxWidth().height(56.dp)
                        )
                    } else {
                        BrutalButton(
                            text = "2. 精准闹钟已授权 ✓",
                            onClick = { },
                            backgroundColor = BrutalColors.CheckGreen,
                            modifier = Modifier.fillMaxWidth().height(56.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "* 我们保证这些权限仅用于时间提醒，且极低耗电。完成授权后即可关闭本向导。",
                        fontSize = 12.sp,
                        color = BrutalColors.MediumGray,
                        fontWeight = FontWeight.Bold
                    )

                    BrutalButton(
                        text = "我已完成授权",
                        onClick = onClose,
                        backgroundColor = BrutalColors.TaskRed,
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    )
                }
            }
        }
    }
}
