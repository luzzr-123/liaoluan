package www.luuzr.liaoluan.ui.modal


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import www.luuzr.liaoluan.data.model.Priority
import www.luuzr.liaoluan.data.model.Task
import www.luuzr.liaoluan.ui.component.BrutalButton
import www.luuzr.liaoluan.ui.component.BrutalInput
import www.luuzr.liaoluan.ui.theme.BrutalColors
import java.util.Calendar

/**
 * 任务表单 — 对应原型的 TaskForm
 * 包含：名称/描述/优先级选择/标签选择/截止日期/提醒间隔
 */
@Composable
fun TaskForm(
    initialTask: Task?,
    onConfirm: (Task) -> Unit,
    onCancel: () -> Unit
) {
    var text by remember { mutableStateOf(initialTask?.text ?: "") }
    var desc by remember { mutableStateOf(initialTask?.description ?: "") }
    var tag by remember { mutableStateOf(initialTask?.tag ?: "工作") }
    var priority by remember { mutableStateOf(initialTask?.priority ?: Priority.MEDIUM) }
    // 使用 Long 类型的时间戳
    var dueTimestamp by remember { mutableLongStateOf(initialTask?.dueTimestamp ?: 0L) }
    var interval by remember { mutableLongStateOf(initialTask?.reminderInterval ?: 0L) }
    var reminderText by remember { mutableStateOf(initialTask?.reminderText ?: "") }
    
    // 控制时间选择器显示
    var showDatePicker by remember { mutableStateOf(false) }

    val tags = listOf("工作", "生活", "紧急", "学习")
    val priorities = listOf(
        Triple(Priority.LOW, "!", BrutalColors.PriorityLow),
        Triple(Priority.MEDIUM, "!!", BrutalColors.PriorityMedium),
        Triple(Priority.HIGH, "!!!", BrutalColors.PriorityHigh)
    )

    if (showDatePicker) {
        www.luuzr.liaoluan.ui.component.BrutalDateTimePicker(
            initialTimestamp = if (dueTimestamp > 0) dueTimestamp else System.currentTimeMillis(),
            onConfirm = { 
                dueTimestamp = it 
                showDatePicker = false 
            },
            onDismiss = { showDatePicker = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // ... (保持名称、描述、优先级、标签部分不变) ...
        // 名称 + 描述输入框
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
                                "任务名称...",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.LightGray
                            )
                            inner()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(BrutalColors.Black)
                )
                Spacer(modifier = Modifier.height(8.dp))

                BasicTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    textStyle = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = BrutalColors.Black
                    ),
                    cursorBrush = SolidColor(BrutalColors.Black),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 80.dp),
                    decorationBox = { inner ->
                        Box {
                            if (desc.isEmpty()) Text(
                                "添加详细备注 (可选)...",
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

        // 优先级选择
        Column {
            Text(
                text = "优先级",
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                letterSpacing = 2.sp,
                color = BrutalColors.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                priorities.forEach { (p, label, color) ->
                    val isSelected = priority == p
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .then(
                                if (isSelected) Modifier
                                    .offset(x = 2.dp, y = 2.dp)
                                    .background(color)
                                    .border(4.dp, BrutalColors.Black)
                                else Modifier
                                    .background(BrutalColors.White)
                                    .border(4.dp, BrutalColors.Black)
                            )
                            .clickable { priority = p },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        )
                    }
                }
            }
        }

        // 标签选择
        Column {
            Text(
                text = "标签类型",
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                letterSpacing = 2.sp,
                color = BrutalColors.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                tags.forEach { t ->
                    val isSelected = tag == t
                    Box(
                        modifier = Modifier
                            .then(
                                if (isSelected) Modifier
                                    .offset(x = 2.dp, y = 2.dp)
                                    .background(BrutalColors.Black)
                                    .border(4.dp, BrutalColors.Black)
                                else Modifier
                                    .background(BrutalColors.White)
                                    .border(4.dp, BrutalColors.Black)
                            )
                            .clickable { tag = t }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = t,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (isSelected) BrutalColors.White else BrutalColors.Black
                        )
                    }
                }
            }
        }

        // 截止日期 — 点击弹出 BrutalDateTimePicker
        val context = LocalContext.current
        Column {
            Text(
                text = "精确截止时间",
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                letterSpacing = 2.sp,
                color = BrutalColors.White,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Box {
                // 阴影层
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 4.dp, y = 4.dp)
                        .background(BrutalColors.Black)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BrutalColors.White)
                        .border(4.dp, BrutalColors.Black)
                        .clickable { showDatePicker = true }
                        .padding(12.dp)
                ) {
                    val displayTime = if (dueTimestamp > 0) {
                        www.luuzr.liaoluan.util.DateHandle.formatDateTime(dueTimestamp)
                    } else {
                        "点击设定截止倒计时"
                    }
                    Text(
                        text = displayTime,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (dueTimestamp == 0L) Color.Gray.copy(alpha = 0.4f) else BrutalColors.Black
                    )
                }
            }
        }

        // 提醒间隔
        Column {
            Text(
                text = "提醒间隔 (创建/上次提醒后)",
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                letterSpacing = 2.sp,
                color = BrutalColors.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            // 简单横向滚动选择
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val intervals = listOf(
                    0L to "不提醒",
                    30L to "30分钟",
                    60L to "1小时",
                    180L to "3小时",
                    360L to "6小时",
                    720L to "12小时",
                    1440L to "24小时"
                )
                intervals.forEach { (value, label) ->
                    val isSelected = interval == value
                    Box(
                        modifier = Modifier
                            .then(
                                if (isSelected) Modifier
                                    .background(BrutalColors.Black)
                                    .border(2.dp, BrutalColors.Black)
                                else Modifier
                                    .background(BrutalColors.White)
                                    .border(2.dp, BrutalColors.Black)
                            )
                            .clickable { interval = value }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = label,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (isSelected) BrutalColors.White else BrutalColors.Black
                        )
                    }
                }
            }
        }

        // 自定义提醒语
        Column {
            Text(
                text = "自定义提醒语 (通知显示内容)",
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                letterSpacing = 2.sp,
                color = BrutalColors.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Box {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 4.dp, y = 4.dp)
                        .background(BrutalColors.Black)
                )
                BasicTextField(
                    value = reminderText,
                    onValueChange = { reminderText = it },
                    textStyle = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = BrutalColors.Black
                    ),
                    cursorBrush = SolidColor(BrutalColors.Black),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BrutalColors.White)
                        .border(4.dp, BrutalColors.Black)
                        .padding(12.dp),
                    decorationBox = { inner ->
                        Box {
                            if (reminderText.isEmpty()) Text(
                                "输入你想在通知里看到的对未来的寄语...",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray.copy(alpha = 0.5f)
                            )
                            inner()
                        }
                    }
                )
            }
        }

        // 操作按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BrutalButton(
                text = "取消",
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            )
            BrutalButton(
                text = if (initialTask != null) "更新" else "确认发布",
                onClick = {
                    // 空表单验证 — 名称不能为空
                    if (text.isBlank()) return@BrutalButton
                    // 校验：新建任务不允许设置过去的时间作为截止日期
                    if (initialTask == null && dueTimestamp != 0L && dueTimestamp < System.currentTimeMillis()) {
                        android.widget.Toast.makeText(context, "不能回到过去！请设置未来的时间。", android.widget.Toast.LENGTH_SHORT).show()
                        return@BrutalButton
                    }

                    onConfirm(
                        (initialTask ?: Task()).copy(
                            text = text,
                            description = desc,
                            tag = tag,
                            priority = priority,
                            dueTimestamp = dueTimestamp, // 更新字段
                            reminderInterval = interval,
                            reminderText = reminderText
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
