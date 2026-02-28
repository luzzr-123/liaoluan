package www.luuzr.liaoluan.ui.modal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import www.luuzr.liaoluan.ui.component.BrutalButton
import www.luuzr.liaoluan.ui.theme.BrutalColors
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.activity.compose.BackHandler

/**
 * 设置弹窗 — 对应原型的 SettingsModal
 * 包含：全量备份/导入、深色模式/音效/触感反馈开关
 *
 * 开关状态由外部传入（通过 MutableState），确保关闭面板后状态不重置。
 * 持久化逻辑由调用方负责（SharedPreferences / DataStore）。
 */
@Composable
fun SettingsModal(
    onClose: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onManageHabits: () -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrutalColors.White) // Changed from Black alpha 0.8f to White opaque
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // Disable ripple "wave" effect on background click
                onClick = {}
            )
    ) {
        // 设置面板 — 居中白色卡片
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            // 阴影层
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 12.dp, y = 12.dp)
                    .background(BrutalColors.HabitTeal)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BrutalColors.White)
                    .border(4.dp, BrutalColors.Black)
            ) {
                // 标题栏 — 黑底
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BrutalColors.Black)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "系统配置",
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
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
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // 数据管理区域
                    Text(
                        text = "数据管理",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        letterSpacing = 2.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 全量备份 — 带硬阴影
                        Box(modifier = Modifier.weight(1f)) {
                            // 硬阴影
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .offset(x = 4.dp, y = 4.dp)
                                    .background(BrutalColors.CheckGreen)
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(4.dp, BrutalColors.Black)
                                    .background(BrutalColors.NoteYellow)
                                    .clickable { onExport() }
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Download,
                                    contentDescription = "备份",
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "全量备份",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // 恢复/导入 — 带硬阴影
                        Box(modifier = Modifier.weight(1f)) {
                            // 硬阴影
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .offset(x = 4.dp, y = 4.dp)
                                    .background(BrutalColors.Black)
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(4.dp, BrutalColors.Black)
                                    .background(BrutalColors.HabitTeal)
                                    .clickable { onImport() }
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Upload,
                                    contentDescription = "导入",
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "恢复/导入",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Text(
                        text = "* 支持导入单条笔记/任务/习惯或全量备份",
                        fontSize = 12.sp,
                        color = BrutalColors.MediumGray,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "习惯与任务管理",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        letterSpacing = 2.sp
                    )

                    BrutalButton(
                        text = "进入全局习惯管理",
                        onClick = onManageHabits,
                        backgroundColor = BrutalColors.CheckGreen,
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    BrutalButton(
                        text = "关闭菜单",
                        onClick = onClose,
                        backgroundColor = BrutalColors.TaskRed,
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    )
                }
            }
        }
    }
}
