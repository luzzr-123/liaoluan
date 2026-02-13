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
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
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
    
    // 优化：记得状态，避免重组
    val weekDates = remember(selectedDate) {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentSelected = dateFormat.parse(selectedDate) ?: Date()
        
        calendar.time = currentSelected
        // Back to Monday
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val offset = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - 2
        calendar.add(Calendar.DAY_OF_YEAR, -offset)
        
        (0..6).map {
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
                // 星期几 (改为黑色)
                Text(
                    text = dayName,
                    style = androidx.compose.ui.text.TextStyle(
                        fontSize = 12.sp,
                        // lineHeight = 12.sp, // Removed explicit lineHeight
                        fontWeight = FontWeight.Bold,
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    ),
                    color = BrutalColors.Black
                )
                
                // 日期圈/块 (改为圆形 CircleShape)
                Box(
                    modifier = Modifier
                        // Increase size from 36.dp to 40.dp to prevent clipping
                        .size(40.dp)
                        .background(
                            color = if (isSelected) BrutalColors.Black else if (isToday) BrutalColors.White else Color.Transparent,
                            shape = CircleShape
                        )
                        .border(
                            width = 2.dp,
                            color = if (isSelected) BrutalColors.Black else if (isToday) BrutalColors.Black else Color.Transparent,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = dayNum,
                        style = androidx.compose.ui.text.TextStyle(
                            fontSize = 16.sp,
                            // lineHeight = 16.sp, // Removed explicit lineHeight to avoid clipping
                            fontWeight = FontWeight.Black,
                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                        ),
                        modifier = Modifier.offset(y = (-2).dp), // Manual visual correction for "low" text
                        color = if (isSelected) BrutalColors.White else BrutalColors.Black
                    )
                }
            }
        }
    }
}
