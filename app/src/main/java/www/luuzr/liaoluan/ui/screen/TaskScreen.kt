package www.luuzr.liaoluan.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import www.luuzr.liaoluan.data.model.Priority
import www.luuzr.liaoluan.data.model.Task
import www.luuzr.liaoluan.ui.component.BrutalCheckbox
import www.luuzr.liaoluan.ui.component.EditableCard
import www.luuzr.liaoluan.ui.component.StampOverlay
import www.luuzr.liaoluan.ui.theme.BrutalColors

/**
 * 任务页面 — 红色背景 (#FF6B6B)
 * 对应原型的 TaskScreen
 */
@Composable
fun TaskScreen(
    tasks: List<Task>,
    onToggle: (Long, Boolean) -> Unit,
    onDelete: (Long) -> Unit,
    onEdit: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    // 过滤已完成任务
    val activeTasks = tasks.filter { !it.completed }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BrutalColors.TaskRed)
            .padding(horizontal = 16.dp)
    ) {
        // 标题标签 — 倾斜的白色标签
        // Header removed

        // 任务列表
        if (activeTasks.isEmpty()) {
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
                items(activeTasks, key = { it.id }) { task ->
                    TaskCard(task, onToggle, onDelete, onEdit)
                }
            }
        }
    }
}

@Composable
private fun TaskCard(
    task: Task,
    onToggle: (Long, Boolean) -> Unit,
    onDelete: (Long) -> Unit,
    onEdit: (Task) -> Unit
) {
    val bgColor by animateColorAsState(
        if (task.completed) BrutalColors.Black else BrutalColors.White, label = "taskBg"
    )
    val textColor by animateColorAsState(
        if (task.completed) BrutalColors.White else BrutalColors.Black, label = "taskTxt"
    )

    EditableCard(onEdit = { onEdit(task) }) {
        Box {
            // 硬阴影层
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 8.dp, y = 8.dp)
                    .background(BrutalColors.Black)
            )
            // 卡片主体
            Column(
                modifier = Modifier
                    .background(bgColor)
                    .border(4.dp, BrutalColors.Black)
                    .padding(12.dp)
            ) {
                // 优先级 + 标签行
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (task.priority) {
                        Priority.HIGH -> PriorityBadge(
                            "!!!",
                            if (task.completed) BrutalColors.White else BrutalColors.PriorityHigh,
                            if (task.completed) BrutalColors.Black else BrutalColors.White
                        )
                        Priority.MEDIUM -> PriorityBadge(
                            "!!",
                            if (task.completed) BrutalColors.White else BrutalColors.PriorityMedium,
                            BrutalColors.Black
                        )
                        Priority.LOW -> {}
                    }
                    Box(
                        modifier = Modifier
                            .rotate(-2f)
                            .background(if (task.completed) BrutalColors.White else BrutalColors.Black)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = task.tag.ifEmpty { "默认" },
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (task.completed) BrutalColors.Black else BrutalColors.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 任务名称
                Text(
                    text = task.text,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = textColor,
                    textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None
                )

                // 描述
                if (task.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (task.completed) Color(0xFF1F2937) else BrutalColors.LightGray)
                            .border(2.dp, if (task.completed) Color(0xFF4B5563) else BrutalColors.Black)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = task.description,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (task.completed) Color(0xFF9CA3AF) else Color(0xFF4B5563),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // 分隔线
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(thickness = 4.dp, color = textColor)
                Spacer(modifier = Modifier.height(12.dp))

                // 底部操作栏
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = textColor.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = task.dueDate.ifEmpty { "无截止" },
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = textColor.copy(alpha = 0.8f)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BrutalCheckbox(
                            checked = task.completed,
                            onCheckedChange = { onToggle(task.id, it) }
                        )
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "删除",
                            tint = textColor,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { onDelete(task.id) }
                        )
                    }
                }

                // 完成印章
                if (task.completed) {
                    Box(modifier = Modifier.fillMaxWidth().height(80.dp)) {
                        StampOverlay(text = "DONE!", visible = true)
                    }
                }
            }
        }
    }
}

@Composable
private fun PriorityBadge(text: String, bgColor: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .background(bgColor)
            .border(2.dp, BrutalColors.Black)
            .padding(horizontal = 4.dp)
    ) {
        Text(text = text, fontWeight = FontWeight.Black, fontSize = 12.sp, color = textColor)
    }
}
