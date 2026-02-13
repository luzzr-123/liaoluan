package www.luuzr.liaoluan.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import www.luuzr.liaoluan.ui.theme.BrutalColors
import java.text.SimpleDateFormat
import java.util.*

/**
 * 周日历组件
 * 显示周一至周日，支持日期切换
 */
@Composable
fun WeekCalendar(
    selectedDate: String, // yyyy-MM-dd
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // 计算当前显示周的日期列表
    // 简单起见，我们总是显示“选中日期”这一周
    // 或者总是显示“本周”，但在本周内切换？
    // 需求：点击可以看到之前的习惯... implies sliding back weeks?
    // User request: "增加周一到周日的日历图" (Add Mon-Sun calendar image/view).
    // Let's implement a simple current week view that updates based on selection, 
    // but maybe just 7 days centered or Monday-Start week.
    // Given the brutalist minimalist style, a fixed Mon-Sun row is best.
    // If selected date is in a different week, the week view should probably shift?
    // Let's make it stateful derived from selectedDate unless we want explicit "Prev/Next Week" buttons.
    // For now, let's derive the Mon-Sun range from the selectedDate.
    
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val currentSelected = dateFormat.parse(selectedDate) ?: Date()
    
    calendar.time = currentSelected
    // Set to Monday of this week
    // interacting with Calendar.DAY_OF_WEEK (Sun=1, Mon=2...)
    // We want Monday as start.
    var dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    // Convert to 0-6 (Mon-Sun) logic or just adjust
    // If we want Monday start:
    // If Sunday (1), subtract 6 days. If Mon (2), subtract 0. Tues(3)-1...
    val offset = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - 2
    calendar.add(Calendar.DAY_OF_YEAR, -offset)
    
    val weekDates = (0..6).map {
        val date = calendar.time
        val dateStr = dateFormat.format(date)
        val dayNum = calendar.get(Calendar.DAY_OF_MONTH).toString()
        val dayName = when (it) {
            0 -> "一"
            1 -> "二"
            2 -> "三"
            3 -> "四"
            4 -> "五"
            5 -> "六"
            6 -> "日"
            else -> ""
        }
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        Triple(dateStr, dayName, dayNum)
    }

    val todayStr = www.luuzr.liaoluan.util.DateHandle.todayDate()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        weekDates.forEach { (dateStr, dayName, dayNum) ->
            val isSelected = dateStr == selectedDate
            val isToday = dateStr == todayStr
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onDateSelected(dateStr) }
            ) {
                // 星期几
                Text(
                    text = dayName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                
                // 日期圈/块
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = if (isSelected) BrutalColors.Black else if (isToday) BrutalColors.White else Color.Transparent,
                            shape = if (isToday && !isSelected) CircleShape else androidx.compose.ui.graphics.RectangleShape
                        )
                        .border(
                            width = 2.dp,
                            color = if (isSelected) BrutalColors.Black else if (isToday) BrutalColors.Black else Color.Transparent,
                            shape = if (isToday) CircleShape else androidx.compose.ui.graphics.RectangleShape // Circle for today
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = dayNum,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isSelected) BrutalColors.White else BrutalColors.Black
                    )
                }
                
                // 选中指示器 (Simple Dot or just the Box style above is enough)
            }
        }
    }
}
