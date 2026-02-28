package www.luuzr.liaoluan.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import www.luuzr.liaoluan.data.db.entity.HabitLogEntity
import www.luuzr.liaoluan.data.model.Habit
import www.luuzr.liaoluan.ui.theme.BrutalColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

import androidx.hilt.navigation.compose.hiltViewModel
import www.luuzr.liaoluan.ui.viewmodel.StatsViewModel

@Composable
fun StatsScreen(
    onBack: () -> Unit,
    viewModel: StatsViewModel = hiltViewModel()
) {
    BackHandler { onBack() }
    
    val viewState by viewModel.viewState.collectAsState()
    val habits = viewState.habits
    val logsForMonth = viewState.logsForMonth
    var selectedDate by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrutalColors.White)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
            .statusBarsPadding()
    ) {
        // 顶部导航栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BrutalColors.Black)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "返回",
                tint = BrutalColors.White,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onBack() }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "年度数据记录",
                color = BrutalColors.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
        }

        HorizontalDivider(thickness = 4.dp, color = BrutalColors.Black)

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 热力图
            item {
                HeatmapSection(
                    logs = logsForMonth,
                    selectedDate = selectedDate,
                    onDateSelect = { date -> selectedDate = date },
                    currentYear = viewState.viewingYear,
                    currentMonth = viewState.viewingMonth,
                    onPrevious = { viewModel.previousMonth() },
                    onNext = { viewModel.nextMonth() },
                    onJump = { y, m -> viewModel.setViewingMonth(y, m) }
                )
            }

            // 每日交互详情
            if (selectedDate.isNotEmpty()) {
                item {
                    val details = remember(selectedDate, habits, logsForMonth) {
                        calculateDailyStatsDetails(selectedDate, habits, logsForMonth)
                    }
                    DailyStatsCard(date = selectedDate, details = details, habits = habits)
                }
            }

            // 最高连胜等卡片
            item {
                StatsSummaryCard(habits)
            }
        }
    }
}

// 定义一个数据类，携带某日的情况
data class DailyStatsDetails(
    val completedHabits: List<HabitLogEntity>,
    val missedHabits: List<Habit>
)

fun calculateDailyStatsDetails(dateStr: String, currentHabits: List<Habit>, allHabitLogs: List<HabitLogEntity>): DailyStatsDetails {
    val parsedTime = www.luuzr.liaoluan.util.DateHandle.parseDate(dateStr)
    val logsForDate = allHabitLogs.filter { it.date == dateStr && it.completed }
    
    val dayIndex = www.luuzr.liaoluan.util.DateHandle.getDayOfWeekIndex(dateStr)
    val shouldHappen = currentHabits.filter { habit -> 
        habit.frequency.contains(dayIndex) && habit.createdAt <= parsedTime + 86400000L
    }

    val completedHabitIds = logsForDate.map { it.habitId }.toSet()
    val missed = shouldHappen.filter { !completedHabitIds.contains(it.id) }
    
    return DailyStatsDetails(
        completedHabits = logsForDate,
        missedHabits = missed
    )
}

@Composable
private fun DailyStatsCard(date: String, details: DailyStatsDetails, habits: List<Habit>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BrutalColors.White)
            .border(4.dp, BrutalColors.Black)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val displayDate = try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val parsed = sdf.parse(date)
            parsed?.let { SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault()).format(it) } ?: date
        } catch (e: Exception) { date }

        Text(
            text = "📅 $displayDate 记录",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = BrutalColors.Black
        )

        // 成功区
        if (details.completedHabits.isNotEmpty()) {
            Text("✅ 完成的内容：", fontWeight = FontWeight.Bold, color = BrutalColors.CheckGreen)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                details.completedHabits.forEach { log ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BrutalColors.LightGray)
                            .border(2.dp, BrutalColors.Black)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val habitTitle = habits.find { it.id == log.habitId }?.text ?: "未知任务"
                        Text(habitTitle, fontWeight = FontWeight.Bold, color = BrutalColors.Black)
                        if (log.progress > 0) {
                            Text("${log.progress} 分钟", fontWeight = FontWeight.Bold)
                        } else {
                            Text("已打卡", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            Text("✅ 没有完成任何习惯", color = BrutalColors.Black.copy(alpha=0.5f))
        }

        // 遗憾区
        if (details.missedHabits.isNotEmpty()) {
             Text("❌ 遗憾未做：", fontWeight = FontWeight.Bold, color = BrutalColors.AlarmRed)
             Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                details.missedHabits.forEach { habit ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFCCCC))
                            .border(2.dp, BrutalColors.Black)
                            .padding(8.dp)
                    ) {
                        Text(habit.text, fontWeight = FontWeight.Bold, color = BrutalColors.Black, textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                    }
                }
            }
        } else {
            Text("🎉 当日计划全满！", color = BrutalColors.Black.copy(alpha=0.5f))
        }
    }
}

@Composable
fun HeatmapSection(
    logs: List<HabitLogEntity>,
    selectedDate: String,
    onDateSelect: (String) -> Unit,
    currentYear: Int,
    currentMonth: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onJump: (Int, Int) -> Unit
) {
    var showJumpDialog by remember { mutableStateOf(false) }

    val monthDays = remember(logs, currentYear, currentMonth) { generateMonthDays(currentYear, currentMonth, logs) }

    if (showJumpDialog) {
        JumpMonthDialog(
            initialYear = currentYear,
            initialMonth = currentMonth,
            onDismiss = { showJumpDialog = false },
            onConfirm = { y, m ->
                onJump(y, m)
                showJumpDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BrutalColors.HabitTeal)
            .border(4.dp, BrutalColors.Black)
            .padding(16.dp)
    ) {
        // Navigation Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(BrutalColors.White)
                    .border(2.dp, BrutalColors.Black)
                    .clickable { onPrevious() },
                contentAlignment = Alignment.Center
            ) {
                Text("<", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = BrutalColors.Black)
            }
            
            Text(
                text = "${currentYear}年 ${String.format(Locale.getDefault(), "%02d", currentMonth)}月",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = BrutalColors.Black,
                modifier = Modifier
                    .clickable { showJumpDialog = true }
                    .padding(horizontal = 8.dp)
            )

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(BrutalColors.White)
                    .border(2.dp, BrutalColors.Black)
                    .clickable { onNext() },
                contentAlignment = Alignment.Center
            ) {
                Text(">", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = BrutalColors.Black)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Week Headers
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            val days = listOf("日", "一", "二", "三", "四", "五", "六")
            days.forEach { day ->
                Text(
                    text = day,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = BrutalColors.Black.copy(alpha = 0.6f),
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Calendar Grid
        for (row in 0 until 6) {
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                for (col in 0 until 7) {
                    val index = row * 7 + col
                    val dayInfo = monthDays.getOrNull(index)
                    if (dayInfo != null) {
                        val color = if (!dayInfo.inMonth) {
                            Color.Transparent
                        } else {
                            when {
                                dayInfo.count == 0 -> Color.White
                                dayInfo.count in 1..2 -> Color(0xFFC6E48B)
                                dayInfo.count in 3..4 -> Color(0xFF7BC96F)
                                dayInfo.count in 5..6 -> Color(0xFF239A3B)
                                else -> Color(0xFF196127)
                            }
                        }
                        
                        val isSelected = dayInfo.date == selectedDate && dayInfo.inMonth
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                        ) {
                            if (dayInfo.inMonth) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(color)
                                        .border(if (isSelected) 3.dp else 2.dp, if (isSelected) BrutalColors.NoteYellow else BrutalColors.Black)
                                        .clickable { onDateSelect(dayInfo.date) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    val dayNum = dayInfo.date.split("-").last().toInt()
                                    Text(
                                        text = dayNum.toString(),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (dayInfo.count > 0 && color != Color.White) Color.White else BrutalColors.Black
                                    )
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f).aspectRatio(1f).padding(2.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun JumpMonthDialog(initialYear: Int, initialMonth: Int, onDismiss: () -> Unit, onConfirm: (Int, Int) -> Unit) {
    var inputYear by remember { mutableStateOf(initialYear.toString()) }
    var inputMonth by remember { mutableStateOf(initialMonth.toString()) }
    
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .background(BrutalColors.White)
                .border(4.dp, BrutalColors.Black)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("按需跳转时光", fontSize = 20.sp, fontWeight = FontWeight.Black, color = BrutalColors.Black)
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.foundation.text.BasicTextField(
                    value = inputYear,
                    onValueChange = { if (it.length <= 4) inputYear = it },
                    modifier = Modifier.width(60.dp).background(BrutalColors.LightGray).border(2.dp, BrutalColors.Black).padding(8.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontWeight = FontWeight.Bold, color = BrutalColors.Black)
                )
                Text(" 年 ", fontWeight = FontWeight.Bold, color = BrutalColors.Black)
                androidx.compose.foundation.text.BasicTextField(
                    value = inputMonth,
                    onValueChange = { if (it.length <= 2) inputMonth = it },
                    modifier = Modifier.width(40.dp).background(BrutalColors.LightGray).border(2.dp, BrutalColors.Black).padding(8.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontWeight = FontWeight.Bold, color = BrutalColors.Black)
                )
                Text(" 月", fontWeight = FontWeight.Bold, color = BrutalColors.Black)
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.align(Alignment.End)) {
                Box(modifier = Modifier.clickable { onDismiss() }.border(2.dp, BrutalColors.Black).padding(8.dp)) {
                    Text("取消", fontWeight = FontWeight.Bold, color = BrutalColors.Black)
                }
                Box(modifier = Modifier.clickable { 
                    val y = inputYear.toIntOrNull()
                    val m = inputMonth.toIntOrNull()
                    if (y != null && m != null && m in 1..12) {
                        onConfirm(y, m)
                    }
                }.background(BrutalColors.NoteYellow).border(2.dp, BrutalColors.Black).padding(8.dp)) {
                    Text("穿越", fontWeight = FontWeight.Black, color = BrutalColors.Black)
                }
            }
        }
    }
}

@Composable
fun StatsSummaryCard(habits: List<Habit>) {
    val totalActiveHabits = habits.size
    val maxStreak = habits.maxOfOrNull { it.streakDays } ?: 0
    val totalPerfectHabits = habits.count { it.streakDays >= 21 }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        StatBox(modifier = Modifier.weight(1f), title = "最高连胜", value = "$maxStreak 天", color = BrutalColors.NoteYellow)
        StatBox(modifier = Modifier.weight(1f), title = "坚持 >21天", value = "$totalPerfectHabits", color = BrutalColors.CheckGreen)
    }
}

@Composable
fun StatBox(modifier: Modifier = Modifier, title: String, value: String, color: Color) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 4.dp, y = 4.dp)
                .background(BrutalColors.Black)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(color)
                .border(2.dp, BrutalColors.Black)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrutalColors.Black.copy(alpha=0.7f))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Black)
        }
    }
}

data class DayInfo(val date: String, val count: Int, val inMonth: Boolean)

fun generateMonthDays(year: Int, month: Int, logs: List<HabitLogEntity>): List<DayInfo> {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val logMap = logs.groupBy { it.date }.mapValues { it.value.filter { log -> log.completed }.size }
    
    val list = mutableListOf<DayInfo>()
    val cal = Calendar.getInstance()
    cal.set(Calendar.YEAR, year)
    cal.set(Calendar.MONTH, month - 1)
    cal.set(Calendar.DAY_OF_MONTH, 1)
    
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon...
    val offset = firstDayOfWeek - Calendar.SUNDAY // 0 if Sunday, 1 if Mon...
    
    cal.add(Calendar.DAY_OF_MONTH, -offset)
    
    for (i in 0 until 42) {
        val currentM = cal.get(Calendar.MONTH) + 1
        val inMonth = currentM == month
        val dateStr = sdf.format(cal.time)
        val count = logMap[dateStr] ?: 0
        list.add(DayInfo(dateStr, count, inMonth))
        cal.add(Calendar.DAY_OF_MONTH, 1)
    }
    return list
}
