package www.luuzr.liaoluan.ui.modal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import www.luuzr.liaoluan.ui.component.BrutalButton
import www.luuzr.liaoluan.ui.theme.BrutalColors

/**
 * JSON 导入弹窗 — 简单的多行文本输入框
 */
@Composable
fun ImportDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    onSelectFile: () -> Unit
) {
    var jsonText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Shadow
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 8.dp, y = 8.dp)
                    .background(BrutalColors.Black)
            )
            // Content
            Column(
                modifier = Modifier
                    .background(BrutalColors.White)
                    .border(4.dp, BrutalColors.Black)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "导入数据 (JSON)",
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp
                )

                BrutalButton(
                    text = "选择 JSON 文件",
                    onClick = onSelectFile,
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = BrutalColors.NoteYellow,
                    textColor = BrutalColors.Black
                )

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("- 或粘贴内容 -", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                }

                BasicTextField(
                    value = jsonText,
                    onValueChange = { jsonText = it },
                    textStyle = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = BrutalColors.Black
                    ),
                    cursorBrush = SolidColor(BrutalColors.Black),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .border(2.dp, BrutalColors.Black)
                        .padding(8.dp),
                    decorationBox = { inner ->
                        Box {
                            if (jsonText.isEmpty()) {
                                Text(
                                    "粘贴 JSON 数据...",
                                    color = Color.Gray,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            inner()
                        }
                    }
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    BrutalButton(
                        text = "取消",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )
                    BrutalButton(
                        text = "确认导入",
                        onClick = { onConfirm(jsonText) },
                        backgroundColor = BrutalColors.Black,
                        textColor = BrutalColors.White,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
