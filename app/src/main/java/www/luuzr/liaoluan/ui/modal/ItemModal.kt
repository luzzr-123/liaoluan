package www.luuzr.liaoluan.ui.modal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import www.luuzr.liaoluan.data.model.*
import www.luuzr.liaoluan.ui.theme.BrutalColors

/**
 * 全屏表单弹窗 — 对应原型的 ItemModal
 * 根据当前 Tab 显示对应的表单 (TaskForm / HabitForm / NoteForm)
 */
@Composable
fun ItemModal(
    currentTab: Int,
    editingTask: Task?,
    editingHabit: Habit?,
    editingNote: Note?,
    onSaveTask: (Task) -> Unit,
    onSaveHabit: (Habit) -> Unit,
    onSaveNote: (Note) -> Unit,
    onExportNote: ((Note) -> Unit)? = null,
    onClose: () -> Unit
) {
    // 背景色和标题
    val bgColor = when (currentTab) {
        0 -> BrutalColors.TaskRed
        1 -> BrutalColors.HabitTeal
        else -> BrutalColors.NoteYellow
    }
    val titles = listOf("任务", "习惯", "笔记")
    val isEdit = editingTask != null || editingHabit != null || editingNote != null
    val actionText = if (isEdit) "编辑" else "新建"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 标题栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$actionText${titles.getOrElse(currentTab) { "" }}",
                    fontWeight = FontWeight.Black,
                    fontSize = 40.sp,
                    color = BrutalColors.White
                )

                // 关闭按钮 — 对应原型 border-4 + shadow-[4px_4px_0px_black]
                Box {
                    // 硬阴影层
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .offset(x = 4.dp, y = 4.dp)
                            .background(BrutalColors.Black)
                    )
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(BrutalColors.White)
                            .border(4.dp, BrutalColors.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = onClose) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "关闭",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 根据 Tab 显示对应表单
            when (currentTab) {
                0 -> TaskForm(
                    initialTask = editingTask,
                    onConfirm = onSaveTask,
                    onCancel = onClose
                )
                1 -> HabitForm(
                    initialHabit = editingHabit,
                    onConfirm = onSaveHabit,
                    onCancel = onClose
                )
                2 -> NoteForm(
                    initialNote = editingNote,
                    onConfirm = onSaveNote,
                    onCancel = onClose,
                    onExport = onExportNote
                )
            }
        }
    }
}
