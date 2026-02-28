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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

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
    selectedDate: String,
    habitsForDate: List<Habit>,
    onDateSelected: (String) -> Unit,
    onProgress: (Long) -> Unit,
    onStartDuration: (Habit) -> Unit,
    onEndDuration: (Habit) -> Unit,
    onDelete: (Long) -> Unit,
    onEdit: (Habit) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = www.luuzr.liaoluan.util.DateHandle.todayDate()
    val isToday = selectedDate == today

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BrutalColors.HabitTeal)
            .padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 周日历
            www.luuzr.liaoluan.ui.component.WeekCalendar(
                selectedDate = selectedDate,
                onDateSelected = onDateSelected
            )

            if (habitsForDate.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if(isToday) "暂无习惯" else "无记录",
                        fontWeight = FontWeight.Black,
                        fontSize = 32.sp,
                        color = BrutalColors.Black.copy(alpha = 0.2f),
                        modifier = Modifier.rotate(12f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(habitsForDate, key = { it.id }) { habit ->
                        HabitCard(
                            habit = habit,
                            onProgress = { onProgress(it) },
                            onStartDuration = { onStartDuration(it) },
                            onEndDuration = { onEndDuration(it) },
                            onDelete = { onDelete(it) },
                            onEdit = { onEdit(it) },
                            isToday = isToday
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HabitCard(
    habit: Habit,
    onProgress: (Long) -> Unit,
    onStartDuration: (Habit) -> Unit,
    onEndDuration: (Habit) -> Unit,
    onDelete: (Long) -> Unit,
    onEdit: (Habit) -> Unit,
    isToday: Boolean
) {
    val bgColor by animateColorAsState(
        if (habit.completed) BrutalColors.Black else BrutalColors.White, label = "habitBg"
    )
    val textColor by animateColorAsState(
        if (habit.completed) BrutalColors.White else BrutalColors.Black, label = "habitTxt"
    )

    // 如果不是今天，禁用点击详情/编辑，或者允许查看但不允许操作进度
    // 需求: "点击就可以看到之前的习惯的完成情况" -> 应该是只读。
    // EditableCard 允许点击编辑。历史记录是否允许编辑？通常不允许。
    // 所以如果 !isToday，这里的 onEdit 应该禁用或者变成“查看详情”
    // 这里简单处理：禁用 EditableCard 的点击（如果 EditableCard 支持 enabled），这里它不支持，我们拦截 onEdit
    
    val finalOnEdit: () -> Unit = { 
        if (isToday) onEdit(habit) 
        // else: show toast "History is read-only"? Or just do nothing.
    }

    EditableCard(onEdit = finalOnEdit) {
        // ... (Keep existing Box structure) ...
        Box {
            // 硬阴影
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
                // ... (Keep Letter decoration) ...
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
                    // ... (Keep Title/Streak Row) ...
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
                            // ... (Progress Badge) ...
                            if (habit.habitType == "DURATION") {
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
                                        text = "目标: ${habit.targetDuration} 分钟",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (habit.completed) BrutalColors.White else BrutalColors.Black
                                    )
                                }
                            } else if (habit.targetValue > 1) {
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

                    // 每周频率指示器 - FIX: Increase size to 24.dp
                    val days = listOf("一", "二", "三", "四", "五", "六", "日")
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        days.forEachIndexed { index, day ->
                            val isActive = habit.frequency.contains(index)
                            Box(
                                modifier = Modifier
                                    .size(24.dp) // Increased from 20.dp
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
                            // 仅今日允许删除？或者都可以？
                            // 历史记录不应该能删除习惯本身（因为是元数据），所以隐藏删除按钮
                            if (isToday) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "删除",
                                    tint = textColor,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable { onDelete(habit.id) }
                                )
                            }
                            
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = if (habit.completed) Color(0xFF4B5563) else BrutalColors.Black,
                                modifier = Modifier.size(32.dp)
                            )
                            
                            // Action Button / Checkbox: 仅 today 可操作
                            if (!isToday) {
                                if (habit.habitType == "DURATION") {
                                     Text("不可操作", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                } else {
                                     BrutalProgressCheckbox(
                                        completed = habit.completed,
                                        progress = habit.progress,
                                        target = habit.targetValue,
                                        onProgress = { /* Read only */ }
                                    )
                                }
                            } else {
                                if (habit.habitType == "DURATION") {
                                    if (habit.completed) {
                                        Text("已完成", fontSize = 14.sp, fontWeight = FontWeight.Black, color = textColor)
                                    } else {
                                        if (habit.actualStartTime == 0L) {
                                            // Start Button
                                            Box(
                                                modifier = Modifier
                                                    .background(BrutalColors.Black)
                                                    .border(2.dp, BrutalColors.Black)
                                                    .clickable { onStartDuration(habit) }
                                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                            ) {
                                                Text("开始", color = BrutalColors.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                                            }
                                        } else {
                                            // Started, ticking
                                            var currentElapsed by remember { mutableStateOf(android.os.SystemClock.elapsedRealtime()) }
                                            androidx.compose.runtime.LaunchedEffect(Unit) {
                                                while(true) {
                                                    currentElapsed = android.os.SystemClock.elapsedRealtime()
                                                    delay(1000)
                                                }
                                            }
                                            val elapsedMs = currentElapsed - habit.actualStartTime + (habit.progress * 60000L)
                                            val elapsedMin = (elapsedMs / 1000) / 60
                                            val elapsedSec = (elapsedMs / 1000) % 60
                                            val timeStr = "%02d:%02d".format(elapsedMin, elapsedSec)
                                            
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically, 
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(timeStr, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                                Box(
                                                    modifier = Modifier
                                                        .background(BrutalColors.AlarmRed)
                                                        .border(2.dp, BrutalColors.Black)
                                                        .clickable { onEndDuration(habit) }
                                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                                ) {
                                                    Text("结束", color = BrutalColors.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                                                }
                                            }
                                        }
                                    }
                                } else {
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
    }
}

