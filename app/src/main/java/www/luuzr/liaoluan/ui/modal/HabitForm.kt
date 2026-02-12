package www.luuzr.liaoluan.ui.modal


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import www.luuzr.liaoluan.data.model.Habit
import www.luuzr.liaoluan.ui.component.BrutalButton
import www.luuzr.liaoluan.ui.component.BrutalInput
import www.luuzr.liaoluan.ui.theme.BrutalColors

/**
 * 习惯表单 — 对应原型的 HabitForm
 * 包含：名称/激励语/目标值+单位+步进/频率选择器/提醒时间
 */
@Composable
fun HabitForm(
    initialHabit: Habit?,
    onConfirm: (Habit) -> Unit,
    onCancel: () -> Unit
) {
    var text by remember { mutableStateOf(initialHabit?.text ?: "") }
    var motivation by remember { mutableStateOf(initialHabit?.motivation ?: "") }
    var freq by remember { mutableStateOf(initialHabit?.frequency ?: listOf(0, 1, 2, 3, 4)) }
    var startTime by remember { mutableStateOf(initialHabit?.startTime ?: "09:00") }
    var endTime by remember { mutableStateOf(initialHabit?.endTime ?: "23:59") }
    var reminderInterval by remember { mutableIntStateOf(initialHabit?.reminderInterval ?: 0) }
    var targetVal by remember { mutableStateOf((initialHabit?.targetValue ?: 1).toString()) }
    var targetUnit by remember { mutableStateOf(initialHabit?.targetUnit ?: "次") }
    var stepVal by remember { mutableStateOf((initialHabit?.stepValue ?: 1).toString()) }

    var showTimePickerStart by remember { mutableStateOf(false) }
    var showTimePickerEnd by remember { mutableStateOf(false) }

    val days = listOf("一", "二", "三", "四", "五", "六", "日")

    if (showTimePickerStart) {
        val parts = startTime.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 9
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val cal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, h)
            set(java.util.Calendar.MINUTE, m)
            set(java.util.Calendar.SECOND, 0)
        }
        
        www.luuzr.liaoluan.ui.component.BrutalDateTimePicker(
            initialTimestamp = cal.timeInMillis,
            showDate = false,
            onConfirm = { ts ->
                val newCal = java.util.Calendar.getInstance().apply { timeInMillis = ts }
                startTime = "%02d:%02d".format(
                    newCal.get(java.util.Calendar.HOUR_OF_DAY),
                    newCal.get(java.util.Calendar.MINUTE)
                )
                showTimePickerStart = false
            },
            onDismiss = { showTimePickerStart = false }
        )
    }

    if (showTimePickerEnd) {
        val parts = endTime.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 23
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 59
        val cal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, h)
            set(java.util.Calendar.MINUTE, m)
            set(java.util.Calendar.SECOND, 0)
        }

        www.luuzr.liaoluan.ui.component.BrutalDateTimePicker(
            initialTimestamp = cal.timeInMillis,
            showDate = false,
            onConfirm = { ts ->
                val newCal = java.util.Calendar.getInstance().apply { timeInMillis = ts }
                endTime = "%02d:%02d".format(
                    newCal.get(java.util.Calendar.HOUR_OF_DAY),
                    newCal.get(java.util.Calendar.MINUTE)
                )
                showTimePickerEnd = false
            },
            onDismiss = { showTimePickerEnd = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // ... (省略之前的代码: 名称、激励语、目标设置、频率选择) ...
        // 为了确保 regex 匹配正确，我需要包含上下文，但这里我会假设之前的代码已通过 replace_file_content 替换。
        // 下面是针对 "每日开始时间 & 循环间隔" 这一块的整体替换

        // 名称 + 激励语
        Box {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 8.dp, y = 8.dp)
                    .background(BrutalColors.Black)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BrutalColors.White)
                    .border(4.dp, BrutalColors.Black)
                    .padding(16.dp)
            ) {
                BasicTextField(
                    value = text,
                    onValueChange = { text = it },
                    textStyle = TextStyle(
                        fontWeight = FontWeight.Black,
                        fontSize = 28.sp,
                        color = BrutalColors.Black
                    ),
                    cursorBrush = SolidColor(BrutalColors.Black),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { inner ->
                        Box {
                            if (text.isEmpty()) Text(
                                "习惯名称",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.LightGray
                            )
                            inner()
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                BasicTextField(
                    value = motivation,
                    onValueChange = { motivation = it },
                    textStyle = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = BrutalColors.Black
                    ),
                    cursorBrush = SolidColor(BrutalColors.Black),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BrutalColors.LightGray)
                        .border(2.dp, BrutalColors.Black)
                        .padding(8.dp),
                    decorationBox = { inner ->
                        Box {
                            if (motivation.isEmpty()) Text(
                                "自定义提醒语: 给自己一点动力吧...",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            inner()
                        }
                    }
                )
            }
        }

        // 目标设置区 (保持不变)
        Column {
            Text(
                text = "目标设置",
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                letterSpacing = 2.sp,
                color = BrutalColors.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 4.dp, y = 4.dp)
                        .background(BrutalColors.Black)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BrutalColors.White)
                        .border(4.dp, BrutalColors.Black)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ... (目标值、单位、步进值 UI 代码保持原样) ...
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("每日总目标", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            BasicTextField(
                                value = targetVal,
                                onValueChange = { targetVal = it },
                                textStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                                cursorBrush = SolidColor(BrutalColors.Black),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(2.dp, BrutalColors.Black)
                                    .padding(8.dp)
                            )
                        }
                        Column(modifier = Modifier.width(80.dp)) {
                            Text("单位", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            BasicTextField(
                                value = targetUnit,
                                onValueChange = { targetUnit = it },
                                textStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                                cursorBrush = SolidColor(BrutalColors.Black),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(2.dp, BrutalColors.Black)
                                    .padding(8.dp)
                            )
                        }
                    }

                    val targetInt = targetVal.toIntOrNull() ?: 1
                    if (targetInt > 1) {
                         Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("单次打卡增量", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Box(
                                    modifier = Modifier
                                        .background(BrutalColors.NoteYellow)
                                        .border(1.dp, BrutalColors.Black)
                                        .padding(horizontal = 4.dp)
                                ) {
                                    Text("新", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                BasicTextField(
                                    value = stepVal,
                                    onValueChange = { stepVal = it },
                                    textStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                                    cursorBrush = SolidColor(BrutalColors.Black),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(2.dp, BrutalColors.Black)
                                        .padding(8.dp)
                                )
                                Text("/ 按一次", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text = "* 例如总目标30分钟，设置增量15，则按两次完成。",
                                fontSize = 10.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // 频率选择器 (保持不变)
        Column {
            Text(
                text = "执行频率",
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                letterSpacing = 2.sp,
                color = BrutalColors.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(4.dp, BrutalColors.Black)
                    .background(BrutalColors.White)
            ) {
                days.forEachIndexed { index, day ->
                    val isActive = freq.contains(index)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .then(
                                if (index < 6) Modifier.border(
                                    width = 0.dp,
                                    color = Color.Transparent
                                ) else Modifier
                            )
                            .background(if (isActive) BrutalColors.Black else BrutalColors.White)
                            .clickable {
                                freq = if (isActive) freq - index else (freq + index).sorted()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = if (isActive) BrutalColors.White else BrutalColors.Black
                        )
                    }
                    if (index < 6) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(48.dp)
                                .background(BrutalColors.Black)
                        )
                    }
                }
            }
        }

        // 每日提醒计划
        Column {
            Text(
                text = "每日提醒计划 (时间段内的循环提醒)",
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                letterSpacing = 2.sp,
                color = BrutalColors.White,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                // 阴影
                Box(modifier = Modifier.matchParentSize().offset(4.dp, 4.dp).background(BrutalColors.Black))
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BrutalColors.White)
                        .border(4.dp, BrutalColors.Black)
                        .padding(16.dp)
                ) {
                    // Start Time & End Time
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 开始时间
                        Column(modifier = Modifier.weight(1f)) {
                            Text("开始时间", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(2.dp, BrutalColors.Black)
                                    .clickable { showTimePickerStart = true }
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = startTime,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                        }
                        
                        // 结束时间
                        Column(modifier = Modifier.weight(1f)) {
                            Text("结束时间", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(2.dp, BrutalColors.Black)
                                    .clickable { showTimePickerEnd = true }
                                    .padding(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = endTime,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                    if (endTime < startTime) {
                                        Text(
                                            text = "(次日)",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            color = BrutalColors.HabitBlue
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 循环间隔
                    Column {
                        Text("每隔多少分钟提醒一次 (0 = 仅提醒一次)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        BasicTextField(
                            value = reminderInterval.toString(),
                            onValueChange = { 
                                if (it.isEmpty()) reminderInterval = 0
                                else it.toIntOrNull()?.let { v -> reminderInterval = v }
                            },
                            textStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                            cursorBrush = SolidColor(BrutalColors.Black),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(2.dp, BrutalColors.Black)
                                .padding(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (reminderInterval > 0) 
                            "提醒逻辑: 在 $startTime 至 $endTime 期间，每 $reminderInterval 分钟提醒一次。" 
                        else 
                            "提醒逻辑: 仅在 $startTime 提醒一次。",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        // 操作按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BrutalButton(
                text = "稍后",
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            )
            BrutalButton(
                text = if (initialHabit != null) "保存" else "锁定目标",
                onClick = {
                    if (text.isBlank()) return@BrutalButton
                    onConfirm(
                        (initialHabit ?: Habit()).copy(
                            text = text,
                            motivation = motivation,
                            targetValue = targetVal.toIntOrNull() ?: 1,
                            targetUnit = targetUnit,
                            stepValue = stepVal.toIntOrNull() ?: 1,
                            frequency = freq,
                            startTime = startTime,
                            endTime = endTime,
                            reminderInterval = reminderInterval
                        )
                    )
                },
                backgroundColor = BrutalColors.Black,
                textColor = BrutalColors.White,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
