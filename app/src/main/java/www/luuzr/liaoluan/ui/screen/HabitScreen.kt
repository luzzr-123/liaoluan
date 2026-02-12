package www.luuzr.liaoluan.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import www.luuzr.liaoluan.data.model.Habit
import www.luuzr.liaoluan.ui.component.BrutalProgressCheckbox
import www.luuzr.liaoluan.ui.component.EditableCard
import www.luuzr.liaoluan.ui.component.StampOverlay
import www.luuzr.liaoluan.ui.theme.BrutalColors

/**
 * 习惯页面 — 青色背景 (#4ECDC4)
 * 对应原型的 HabitScreen
 */
@Composable
fun HabitScreen(
    habits: List<Habit>,
    onProgress: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onEdit: (Habit) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BrutalColors.HabitTeal)
            .padding(horizontal = 16.dp)
    ) {
        // 标题 — 右上角黑底白字
        // Header removed

        if (habits.isEmpty()) {
            Text(
                text = "空空如也",
                fontWeight = FontWeight.Black,
                fontSize = 48.sp,
                color = BrutalColors.Black.copy(alpha = 0.2f),
                modifier = Modifier
                    .align(Alignment.Center)
                    .rotate(12f)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp),
                contentPadding = PaddingValues(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(habits, key = { it.id }) { habit ->
                    HabitCard(habit, onProgress, onDelete, onEdit)
                }
            }
        }
    }
}

@Composable
private fun HabitCard(
    habit: Habit,
    onProgress: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onEdit: (Habit) -> Unit
) {
    val bgColor by animateColorAsState(
        if (habit.completed) BrutalColors.Black else BrutalColors.White, label = "habitBg"
    )
    val textColor by animateColorAsState(
        if (habit.completed) BrutalColors.White else BrutalColors.Black, label = "habitTxt"
    )

    EditableCard(onEdit = { onEdit(habit) }) {
        // 外层 Box — 用于阴影和内容的 Z 轴排列
        Box {
            // 硬阴影 — 必须先绘制，否则会覆盖内容
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 8.dp, y = 8.dp)
                    .background(BrutalColors.Black)
            )

            // 卡片主体
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bgColor)
                    .border(4.dp, BrutalColors.Black)
                    .padding(16.dp)
            ) {
                // 大字母背景装饰（对应原型中的巨大首字母）
                Text(
                    text = habit.text.firstOrNull()?.toString() ?: "",
                    fontSize = 80.sp,
                    fontWeight = FontWeight.Black,
                    color = if (habit.completed) Color(0xFF1F2937) else BrutalColors.LightGray,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 8.dp, y = 8.dp)
                )

                // 完成印章
                if (habit.completed) {
                    StampOverlay(text = "NICE!", visible = true)
                }

                // 内容层
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // 标题行 + 连续天数/进度
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = habit.text,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = textColor,
                            textDecoration = if (habit.completed) TextDecoration.LineThrough else TextDecoration.None,
                            modifier = Modifier.weight(0.7f)
                        )

                        Column(horizontalAlignment = Alignment.End) {
                            // 连续天数 badge
                            Box(
                                modifier = Modifier
                                    .rotate(2f)
                                    .background(if (habit.completed) BrutalColors.White else BrutalColors.Black)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "连续: ${habit.streakDays}",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (habit.completed) BrutalColors.Black else BrutalColors.White
                                )
                            }

                            // 进度 badge（仅 target > 1 时）
                            if (habit.targetValue > 1) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .border(
                                            2.dp,
                                            if (habit.completed) BrutalColors.White else BrutalColors.Black
                                        )
                                        .background(if (habit.completed) Color.Transparent else BrutalColors.NoteYellow)
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "进度: ${habit.progress}/${habit.targetValue} ${habit.targetUnit}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (habit.completed) BrutalColors.White else BrutalColors.Black
                                    )
                                }
                            }
                        }
                    }

                    // 激励语
                    if (habit.motivation.isNotEmpty()) {
                        Text(
                            text = "\"${habit.motivation}\"",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Default,
                            color = if (habit.completed) Color(0xFF9CA3AF) else Color(0xFF6B7280)
                        )
                    }

                    // 每周频率指示器
                    val days = listOf("一", "二", "三", "四", "五", "六", "日")
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        days.forEachIndexed { index, day ->
                            val isActive = habit.frequency.contains(index)
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .border(
                                        2.dp,
                                        if (habit.completed) Color(0xFF4B5563) else BrutalColors.Black
                                    )
                                    .background(
                                        when {
                                            isActive && habit.completed -> BrutalColors.White
                                            isActive -> BrutalColors.Black
                                            else -> Color.Transparent
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = when {
                                        isActive && habit.completed -> BrutalColors.Black
                                        isActive -> BrutalColors.White
                                        habit.completed -> Color(0xFF4B5563)
                                        else -> BrutalColors.Black
                                    }
                                )
                            }
                        }
                    }

                    // 底部操作栏
                    HorizontalDivider(thickness = 4.dp, color = textColor)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 提醒时间
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = textColor,
                                modifier = Modifier.size(14.dp)
                            )
                            // 提醒时间显示逻辑修复
                            val displayTime = if (habit.reminderInterval > 0) {
                                "${habit.startTime} - ${habit.endTime}"
                            } else {
                                "每日 ${habit.startTime}"
                            }
                            Text(
                                text = displayTime,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }

                        // 删除 + 箭头 + 进度复选框
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 删除按钮
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "删除",
                                tint = textColor,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { onDelete(habit.id) }
                            )
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = if (habit.completed) Color(0xFF4B5563) else BrutalColors.Black,
                                modifier = Modifier.size(32.dp)
                            )
                            BrutalProgressCheckbox(
                                completed = habit.completed,
                                progress = habit.progress,
                                target = habit.targetValue,
                                onProgress = { onProgress(habit.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

