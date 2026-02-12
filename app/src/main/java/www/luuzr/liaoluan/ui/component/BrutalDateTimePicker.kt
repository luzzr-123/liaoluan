package www.luuzr.liaoluan.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import www.luuzr.liaoluan.ui.theme.BrutalColors
import java.text.SimpleDateFormat
import java.util.*

/**
 * Brutal Style Date Time Picker
 * 精确到秒的时间选择器，拒绝原生样式的平庸
 */
@Composable
fun BrutalDateTimePicker(
    initialTimestamp: Long,
    showDate: Boolean = true,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val calendar = Calendar.getInstance().apply { timeInMillis = initialTimestamp }
    
    var year by remember { mutableIntStateOf(calendar.get(Calendar.YEAR)) }
    var month by remember { mutableIntStateOf(calendar.get(Calendar.MONTH) + 1) } // 1-12
    var day by remember { mutableIntStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }
    var hour by remember { mutableIntStateOf(calendar.get(Calendar.HOUR_OF_DAY)) }
    var minute by remember { mutableIntStateOf(calendar.get(Calendar.MINUTE)) }
    var second by remember { mutableIntStateOf(calendar.get(Calendar.SECOND)) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxWidth()
                .wrapContentHeight() // 自适应高度
        ) {
            // Shadow
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 8.dp, y = 8.dp)
                    .background(BrutalColors.Black)
            )
            
            Column(
                modifier = Modifier
                    .background(BrutalColors.White)
                    .border(4.dp, BrutalColors.Black)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Text(
                    text = "选择时间",
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp,
                    color = BrutalColors.Black,
                    modifier = Modifier
                        .background(BrutalColors.NoteYellow)
                        .border(2.dp, BrutalColors.Black)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )

                // Date Section
                if (showDate) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        NumberPicker(
                            value = year,
                            range = 2024..2030,
                            onValueChange = { year = it },
                            label = "年"
                        )
                        NumberPicker(
                            value = month,
                            range = 1..12,
                            onValueChange = { month = it },
                            label = "月"
                        )
                        NumberPicker(
                            value = day,
                            range = 1..31, // 简化处理，实际可以通过 year/month 计算 maxDay
                            onValueChange = { day = it },
                            label = "日"
                        )
                    }
                }

                // Time Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    NumberPicker(
                        value = hour,
                        range = 0..23,
                        onValueChange = { hour = it },
                        label = "时"
                    )
                    Text(":", fontSize = 32.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterVertically))
                    NumberPicker(
                        value = minute,
                        range = 0..59,
                        onValueChange = { minute = it },
                        label = "分"
                    )
                    Text(":", fontSize = 32.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterVertically))
                    NumberPicker(
                        value = second,
                        range = 0..59,
                        onValueChange = { second = it },
                        label = "秒"
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Actions
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    BrutalButton(
                        text = "取消",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        backgroundColor = BrutalColors.White,
                        textColor = BrutalColors.Black
                    )
                    BrutalButton(
                        text = "确认",
                        onClick = {
                            val newCalendar = Calendar.getInstance().apply {
                                set(year, month - 1, day, hour, minute, second)
                                set(Calendar.MILLISECOND, 0)
                            }
                            onConfirm(newCalendar.timeInMillis)
                        },
                        modifier = Modifier.weight(1f),
                        backgroundColor = BrutalColors.Black,
                        textColor = BrutalColors.White
                    )
                }
            }
        }
    }
}

@Composable
fun NumberPicker(
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(60.dp)
    ) {
        // Up Button
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(BrutalColors.Black)
                .clickable {
                    val newValue = if (value < range.last) value + 1 else range.first
                    onValueChange(newValue)
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Up", tint = BrutalColors.White)
        }

        // Value Display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .border(2.dp, BrutalColors.Black)
                .background(BrutalColors.White)
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = String.format("%02d", value),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Down Button
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(BrutalColors.Black)
                .clickable {
                    val newValue = if (value > range.first) value - 1 else range.last
                    onValueChange(newValue)
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Down", tint = BrutalColors.White)
        }
        
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
