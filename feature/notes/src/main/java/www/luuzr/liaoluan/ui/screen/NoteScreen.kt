package www.luuzr.liaoluan.ui.screen

import android.net.Uri
import androidx.compose.ui.draw.clipToBounds // Add this
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import www.luuzr.liaoluan.data.model.Mood
import www.luuzr.liaoluan.data.model.Note
import www.luuzr.liaoluan.ui.component.EditableCard
import www.luuzr.liaoluan.ui.theme.BrutalColors
import java.text.SimpleDateFormat
import java.util.*

/**
 * 笔记页面 — 黄色背景 (#FFE66D)
 * 对应原型的 NoteScreen，排序：置顶优先 → 日期倒序
 * 支持 [IMG:uri] 标记的图片渲染
 */
@Composable
fun NoteScreen(
    notes: List<Note>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onDelete: (Long) -> Unit,
    onEdit: (Note) -> Unit,
    onExportNote: (Note) -> Unit,
    onTogglePin: (Long) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 排序逻辑 — 置顶优先 → createdAt 倒序
    val sortedNotes = remember(notes) {
        notes.sortedWith(compareByDescending<Note> { it.isPinned }.thenByDescending { it.createdAt })
    }

    // 详情页模态框
    // 详情页模态框 (Overlay)
    var selectedNote by remember { mutableStateOf<Note?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BrutalColors.NoteYellow)
    ) {
        // 1. 笔记列表内容
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
             // 搜索栏
             Spacer(modifier = Modifier.height(16.dp))
             Row(
                 modifier = Modifier
                     .fillMaxWidth()
                     .padding(end = 56.dp) // Leave space for Settings button
                     .background(BrutalColors.White)
                     .border(4.dp, BrutalColors.Black),
                 verticalAlignment = Alignment.CenterVertically
             ) {
                 Icon(
                     Icons.Default.Search,
                     contentDescription = "Search",
                     modifier = Modifier.padding(start = 12.dp),
                     tint = BrutalColors.Black
                 )
                 androidx.compose.foundation.text.BasicTextField(
                     value = searchQuery,
                     onValueChange = onSearchQueryChange,
                     modifier = Modifier
                         .fillMaxWidth()
                         .padding(16.dp),
                     textStyle = androidx.compose.ui.text.TextStyle(
                         color = BrutalColors.Black,
                         fontSize = 18.sp,
                         fontWeight = FontWeight.Bold
                     ),
                     decorationBox = { innerTextField ->
                         if (searchQuery.isEmpty()) {
                             Text(
                                 text = "搜索关键字...",
                                 color = BrutalColors.Black.copy(alpha = 0.5f),
                                 fontSize = 18.sp,
                                 fontWeight = FontWeight.Bold
                             )
                         }
                         innerTextField()
                     }
                 )
             }
             Spacer(modifier = Modifier.height(16.dp))

             if (sortedNotes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "空空如也",
                        fontWeight = FontWeight.Black,
                        fontSize = 48.sp,
                        color = BrutalColors.Black.copy(alpha = 0.2f),
                        modifier = Modifier.rotate(12f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 16.dp),
                    contentPadding = PaddingValues(bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    items(sortedNotes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            onDelete = onDelete,
                            onEdit = onEdit,
                            onExportNote = onExportNote,
                            onTogglePin = onTogglePin,
                            onClick = { selectedNote = note }
                        )
                    }
                }
            }
        }

        // 2. Settings Button (Moved to end for Z-Index)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp) // Fix padding
                .size(40.dp)
                .background(BrutalColors.White)
                .border(3.dp, BrutalColors.Black)
                .clickable { onOpenSettings() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Settings,
                contentDescription = "设置",
                tint = BrutalColors.Black,
                modifier = Modifier.size(24.dp)
            )
        }
        
        // 3. 详情页 Overlay (使用 AnimatedVisibility)
        androidx.compose.animation.AnimatedVisibility(
            visible = selectedNote != null,
            enter = androidx.compose.animation.slideInVertically(initialOffsetY = { it }) + androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { it }) + androidx.compose.animation.fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            selectedNote?.let { note ->
                www.luuzr.liaoluan.ui.modal.NoteDetailModal(
                    note = note,
                    onClose = { selectedNote = null }
                )
            }
        }
    }
}

@Composable
private fun NoteCard(
    note: Note,
    onDelete: (Long) -> Unit,
    onEdit: (Note) -> Unit,
    onExportNote: (Note) -> Unit,
    onTogglePin: (Long) -> Unit,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    // 可点击卡片
    Box {
        // EditableCard 逻辑需调整，不再直接包裹内容，而是作为内容的一部分
        // 但为了复用 EditableCard 的样式，我们保留它，但在内部处理点击
        
         EditableCard(onEdit = { onEdit(note) }) {
        
            Box(
                modifier = Modifier.clickable { onClick() }
            ) {
                // 硬阴影
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 4.dp, y = 4.dp)
                        .background(BrutalColors.Black)
                )

                Column(
                    modifier = Modifier
                        .background(BrutalColors.White)
                        .border(4.dp, BrutalColors.Black)
                        .padding(16.dp)
                ) {
                    // ... (Card content) ...
                    // 置顶图标
                    Box(
                        modifier = Modifier
                            .align(Alignment.End)
                            .offset(x = 12.dp, y = (-12).dp)
                            .rotate(if (note.isPinned) 12f else 0f)
                            .background(if (note.isPinned) BrutalColors.Black else BrutalColors.LightGray)
                            .border(2.dp, if (note.isPinned) BrutalColors.White else BrutalColors.Black)
                            .clickable { onTogglePin(note.id) }
                            .padding(4.dp)
                    ) {
                        Icon(
                            Icons.Default.PushPin,
                            contentDescription = if (note.isPinned) "取消置顶" else "置顶",
                            tint = if (note.isPinned) BrutalColors.White else BrutalColors.Black,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // 标题栏
                     Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 心情图标
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .border(2.dp, BrutalColors.Black)
                                    .background(BrutalColors.LightGray),
                                contentAlignment = Alignment.Center
                            ) {
                                val moodIcon = when (note.mood) {
                                    Mood.HAPPY -> Icons.Default.SentimentSatisfied
                                    Mood.NEUTRAL -> Icons.Default.SentimentNeutral
                                    Mood.SAD -> Icons.Default.SentimentDissatisfied
                                    Mood.STORMY -> Icons.Default.Cloud
                                }
                                Icon(
                                    moodIcon,
                                    contentDescription = note.mood.name,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Text(
                                text = note.title.ifEmpty { "无标题" },
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 120.dp)
                            )
                        }

                        // 操作按钮 (Share/Delete - 这些需要消费点击事件，避免触发详情)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = "分享",
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { onExportNote(note) }
                            )
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "删除",
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { onDelete(note.id) }
                            )
                             Icon(
                                Icons.Default.Edit,
                                contentDescription = "编辑",
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { onEdit(note) }
                            )
                        }
                    }

                    // 分隔线
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(BrutalColors.Black)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 笔记内容 (Preview - max lines or truncated)
                    // NoteContentRenderer(noteText = note.text)
                    // We can keep using NoteContentRenderer but maybe rely on max height of card to limit it?
                    // Or let it render normally, but clicking opens detail.
                    // 笔记内容 (Preview Logic)
                    NoteContentRenderer(
                        noteText = note.text,
                        expanded = expanded,
                        onToggleExpand = { expanded = !expanded }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 日期标签
                    Box(
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(BrutalColors.Black)
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = formatDate(note.createdAt),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrutalColors.White
                            )
                        }
                    }
                }
            }
         }
    }
}

/**
 * 笔记内容渲染器 — 支持展开/收起
 * 默认显示前三行（若有图片，只显示图片和图片前的内容）
 */
@Composable
private fun NoteContentRenderer(
    noteText: String,
    expanded: Boolean,
    onToggleExpand: () -> Unit
) {
    val context = LocalContext.current
    
    // 解析内容
    val parts = remember(noteText) {
        val result = mutableListOf<Pair<String, String?>>()
        var lastIndex = 0
        val imgPattern = Regex("\\[IMG:(.*?)]", RegexOption.DOT_MATCHES_ALL)
        
        imgPattern.findAll(noteText).forEach { match ->
            if (match.range.first > lastIndex) {
                val textBefore = noteText.substring(lastIndex, match.range.first)
                if (textBefore.isNotEmpty()) result.add(Pair(textBefore, null))
            }
            result.add(Pair("", match.groupValues[1]))
            lastIndex = match.range.last + 1
        }
        if (lastIndex < noteText.length) {
            val remaining = noteText.substring(lastIndex)
            if (remaining.isNotEmpty()) result.add(Pair(remaining, null))
        }
        if (result.isEmpty() && noteText.isNotEmpty()) {
            result.add(Pair(noteText, null))
        }
        result
    }

    // 预览模式下的截断逻辑
    val displayParts = if (expanded) {
        parts
    } else {
        // 找到第一个图片的位置
        val firstImgIndex = parts.indexOfFirst { it.second != null }
        if (firstImgIndex != -1) {
            // 如果有图片，只显示到第一个图片（包含第一个图片）
            parts.take(firstImgIndex + 1)
        } else {
            // 如果没有图片，全部显示（但文本控件会限制行数）
            // 或者只显示第一段文本？
            // 需求：前三行。如果只有一个纯文本 part，Text 的 maxLines 会处理。
            // 如果有多个文本 parts (e.g. split by regex but no images? won't happen logic wise)
            // Just return all parts, Text composable will handle maxLines.
            parts
        }
    }

    // 判断是否需要显示“展开/收起”按钮
    // 简易判断：如果有图片且图片不止一张，或者文本被截断（这个很难精确判断，暂且只判断 parts 数量或者 expanded 状态）
    // 更好的方式：既然需求明确“默认只展示前三行”，我们总是显示展开按钮（或者只在内容较长时显示）
    // 为了简单且符合 brutal 风格，我们给一个 explicit toggle。
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        displayParts.forEachIndexed { index, (text, imageUri) ->
            if (imageUri != null) {
                // 图片
                Box {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .offset(x = 3.dp, y = 3.dp)
                            .background(BrutalColors.Black)
                    )
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(Uri.parse(imageUri))
                            .crossfade(true)
                            .build(),
                        contentDescription = "笔记图片",
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .border(3.dp, BrutalColors.Black)
                            .background(BrutalColors.White)
                            .clipToBounds() // Use imported extension
                    )
                }
            } else if (text.isNotEmpty()) {
                // 文本
                Text(
                    text = text,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF374151),
                    lineHeight = 24.sp,
                    maxLines = if (expanded) Int.MAX_VALUE else 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // 展开/收起按钮 - 黑色描边的白色小长条
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 4.dp)
                .width(40.dp) // 小一点
                .height(12.dp)
                .background(BrutalColors.White)
                .border(2.dp, BrutalColors.Black)
                .clickable { onToggleExpand() }
        ) {
             // 可以加个小图标或文字，或者保持极简空白长条
        }
    }
}

private fun formatDate(timestamp: Long): String {
    return www.luuzr.liaoluan.util.DateHandle.formatDate(timestamp)
}
