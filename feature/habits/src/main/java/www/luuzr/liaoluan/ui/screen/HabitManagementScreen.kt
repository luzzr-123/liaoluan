package www.luuzr.liaoluan.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import www.luuzr.liaoluan.data.model.Habit
import www.luuzr.liaoluan.ui.component.BrutalButton
import www.luuzr.liaoluan.ui.theme.BrutalColors

/**
 * 习惯管理全局页面
 * 用于展示、编辑、删除所有已创建的习惯。
 */
@Composable
fun HabitManagementScreen(
    habits: List<Habit>,
    onEdit: (Habit) -> Unit,
    onDelete: (Long) -> Unit,
    onBack: () -> Unit
) {

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
                text = "全部习惯管理",
                color = BrutalColors.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
        }

        HorizontalDivider(thickness = 4.dp, color = BrutalColors.Black)

        // 习惯列表
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(habits, key = { it.id }) { habit ->
                HabitManagementCard(
                    habit = habit,
                    onEdit = { onEdit(habit) },
                    onDelete = { onDelete(habit.id) }
                )
            }
        }
    }
}

@Composable
fun HabitManagementCard(
    habit: Habit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        // 阴影
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 6.dp, y = 6.dp)
                .background(BrutalColors.Black)
        )

        // 卡片内容
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BrutalColors.HabitTeal)
                .border(4.dp, BrutalColors.Black)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = habit.text,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = BrutalColors.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (habit.habitType == "DURATION") "时长模式 | 目标: ${habit.targetDuration} 分钟" 
                             else "普通模式 | 目标: ${habit.targetValue} ${habit.targetUnit}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrutalColors.Black.copy(alpha = 0.7f)
                    )
                }

                // 连续天数展示
                Box(
                    modifier = Modifier
                        .background(BrutalColors.White)
                        .border(2.dp, BrutalColors.Black)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "连续: ${habit.streakDays} 天",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BrutalButton(
                    text = "删除",
                    onClick = onDelete,
                    backgroundColor = BrutalColors.TaskRed,
                    textColor = BrutalColors.White,
                    modifier = Modifier.height(40.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                BrutalButton(
                    text = "修改",
                    onClick = onEdit,
                    backgroundColor = BrutalColors.NoteYellow,
                    textColor = BrutalColors.Black,
                    modifier = Modifier.height(40.dp)
                )
            }
        }
    }
}
