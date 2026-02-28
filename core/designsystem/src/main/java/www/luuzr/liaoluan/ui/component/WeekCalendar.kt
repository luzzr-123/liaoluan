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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import www.luuzr.liaoluan.ui.theme.BrutalColors
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WeekCalendar(
    selectedDate: String, // yyyy-MM-dd
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val weekDates = remember(selectedDate) {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentSelected = try {
            if (selectedDate.isNotEmpty()) dateFormat.parse(selectedDate) ?: Date() else Date()
        } catch (e: Exception) {
            Date()
        }
        
        calendar.time = currentSelected
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
                Text(
                    text = dayName,
                    style = androidx.compose.ui.text.TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    ),
                    color = BrutalColors.Black
                )
                
                Box(
                    modifier = Modifier
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
                            fontWeight = FontWeight.Black,
                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                        ),
                        modifier = Modifier.offset(y = (-2).dp),
                        color = if (isSelected) BrutalColors.White else BrutalColors.Black
                    )
                }
            }
        }
    }
}
