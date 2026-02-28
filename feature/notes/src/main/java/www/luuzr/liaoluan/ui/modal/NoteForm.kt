package www.luuzr.liaoluan.ui.modal

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import www.luuzr.liaoluan.data.model.Mood
import www.luuzr.liaoluan.data.model.Note
import www.luuzr.liaoluan.ui.component.BrutalButton
import www.luuzr.liaoluan.ui.component.BrutalInput
import www.luuzr.liaoluan.ui.theme.BrutalColors

/**
 * 笔记表单 — 对应原型的 NoteForm
 * 包含：标题/心情选择/置顶开关/纯文本编辑区/插入图片
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun NoteForm(
    initialNote: Note?,
    onConfirm: (Note) -> Unit,
    onCancel: () -> Unit,
    onExport: ((Note) -> Unit)? = null
) {
    var title by remember { mutableStateOf(initialNote?.title ?: "") }
    var textValue by remember { mutableStateOf(TextFieldValue(initialNote?.text ?: "")) }
    var mood by remember { mutableStateOf(initialNote?.mood ?: Mood.NEUTRAL) }
    var isPinned by remember { mutableStateOf(initialNote?.isPinned ?: false) }

    // 直接从 Note.images 加载
    val imageUris = remember { 
        mutableStateListOf<Uri>().apply {
            initialNote?.images?.forEach { uriString ->
                try { add(Uri.parse(uriString)) } catch (_: Exception) {}
            }
        } 
    }
    val context = LocalContext.current

    // 图片选择器
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
                // 忽略权限问题
            }
            imageUris.add(uri)
            
            // 在光标位置插入标记 (Updated: No forced newlines)
            val marker = "[IMG:$uri]"
            val newText = textValue.text.substring(0, textValue.selection.start) + 
                          marker + 
                          textValue.text.substring(textValue.selection.end)
            textValue = textValue.copy(
                text = newText,
                selection = androidx.compose.ui.text.TextRange(textValue.selection.start + marker.length)
            )
        }
    }

    val moods = listOf(
        Triple(Mood.HAPPY, Icons.Default.SentimentSatisfied, BrutalColors.MoodHappy),
        Triple(Mood.NEUTRAL, Icons.Default.SentimentNeutral, BrutalColors.MoodNeutral),
        Triple(Mood.SAD, Icons.Default.SentimentDissatisfied, BrutalColors.MoodSad),
        Triple(Mood.STORMY, Icons.Default.Cloud, BrutalColors.MoodStormy)
    )

    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 标题 + 置顶开关
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BrutalInput(
                value = title,
                onValueChange = { title = it },
                placeholder = "标题...",
                modifier = Modifier.weight(1f)
            )

            // 置顶按钮
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(if (isPinned) BrutalColors.Black else BrutalColors.White)
                    .border(4.dp, BrutalColors.Black)
                    .clickable { isPinned = !isPinned },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.PushPin,
                    contentDescription = "置顶",
                    tint = if (isPinned) BrutalColors.White else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // 心情选择器
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BrutalColors.White)
                .border(4.dp, BrutalColors.Black)
        ) {
            moods.forEachIndexed { index, (m, icon, color) ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .background(if (mood == m) color else BrutalColors.White)
                        .clickable { mood = m },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = m.name, modifier = Modifier.size(28.dp))
                }
                if (index < moods.lastIndex) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(52.dp)
                            .background(BrutalColors.Black)
                    )
                }
            }
        }

        // 纯文本编辑区
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
            ) {
                // 工具栏
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BrutalColors.NoteYellow)
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 装饰圆点
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(BrutalColors.White, shape = androidx.compose.foundation.shape.CircleShape)
                                .border(2.dp, BrutalColors.Black, shape = androidx.compose.foundation.shape.CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(BrutalColors.Black, shape = androidx.compose.foundation.shape.CircleShape)
                                .border(2.dp, BrutalColors.Black, shape = androidx.compose.foundation.shape.CircleShape)
                        )
                    }

                    // 功能按钮区
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 插入图片按钮
                        Icon(
                            Icons.Default.Image,
                            contentDescription = "插入图片",
                            modifier = Modifier
                                .size(20.dp)
                                .clickable {
                                    imagePickerLauncher.launch("image/*")
                                }
                        )

                        // 导出当前笔记
                        if (onExport != null) {
                            Icon(
                                Icons.Default.Download,
                                contentDescription = "导出当前笔记",
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable {
                                        val currentNote = (initialNote ?: Note()).copy(
                                            title = title,
                                            text = textValue.text,
                                            mood = mood,
                                            isPinned = isPinned,
                                            images = imageUris.map { it.toString() }
                                        )
                                        onExport(currentNote)
                                    }
                            )
                        }
                    }
                }

                // 底部分隔线
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(BrutalColors.Black)
                )

                // 文本输入区
                BasicTextField(
                    value = textValue,
                    onValueChange = { textValue = it },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        fontSize = 20.sp,
                        lineHeight = 32.sp,
                        color = BrutalColors.Black
                    ),
                    cursorBrush = SolidColor(BrutalColors.Black),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp)
                        .bringIntoViewRequester(bringIntoViewRequester)
                        .padding(16.dp),
                    visualTransformation = remember { ImgTagVisualTransformation() },
                    decorationBox = { inner ->
                        Box {
                            if (textValue.text.isEmpty()) Text(
                                "在此记录灵感...",
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                fontSize = 20.sp,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = Color.LightGray
                            )
                            inner()
                        }
                    }
                )

                // 已插入图片的预览
                if (imageUris.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Text removed
                        imageUris.forEachIndexed { index, uri ->
                            Box {
                                // 硬阴影
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .offset(x = 4.dp, y = 4.dp)
                                        .background(BrutalColors.Black)
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(4.dp, BrutalColors.Black)
                                        .background(BrutalColors.White)
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(uri)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "插入的图片 ${index + 1}",
                                        contentScale = ContentScale.FillWidth,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 200.dp)
                                            .padding(4.dp)
                                    )
                                    // 删除按钮
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .size(24.dp)
                                            .background(BrutalColors.TaskRed)
                                            .border(2.dp, BrutalColors.Black)
                                            .clickable {
                                                imageUris.removeAt(index)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "删除图片",
                                            tint = BrutalColors.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 操作按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BrutalButton(
                text = "丢弃",
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            )
            BrutalButton(
                text = if (initialNote != null) "保存修改" else "保存笔记",
                onClick = {
                    if (title.isBlank() && textValue.text.isBlank()) return@BrutalButton
                    onConfirm(
                        (initialNote ?: Note()).copy(
                            title = title,
                            text = textValue.text,
                            mood = mood,
                            isPinned = isPinned,
                            images = imageUris.map { it.toString() }
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

private class ImgTagVisualTransformation : androidx.compose.ui.text.input.VisualTransformation {
    override fun filter(text: androidx.compose.ui.text.AnnotatedString): androidx.compose.ui.text.input.TransformedText {
        val originalText = text.text
        val builder = androidx.compose.ui.text.AnnotatedString.Builder()
        val pattern = Regex("\\[IMG:(.*?)]", RegexOption.DOT_MATCHES_ALL)
        var lastIndex = 0
        
        val matches = pattern.findAll(originalText).toList()
        
        // Map transformed offset -> original offset
        val transformedToOriginalMapping = mutableListOf<Int>()
        
        // Replacement text length
        val replacementText = "[图片]"
        val replacementLen = replacementText.length // 4
        
        for (match in matches) {
            // 1. Append text before match
            val before = originalText.substring(lastIndex, match.range.first)
            builder.append(before)
            repeat(before.length) { 
                transformedToOriginalMapping.add(lastIndex + it)
            }
            
            // 2. Append replacement
            builder.append(replacementText)
            repeat(replacementLen) {
                // Map all chars of replacement to the START of the original tag
                transformedToOriginalMapping.add(match.range.first)
            }
            
            lastIndex = match.range.last + 1
        }
        
        // 3. Append remaining text
        val remaining = originalText.substring(lastIndex)
        builder.append(remaining)
        repeat(remaining.length) {
            transformedToOriginalMapping.add(lastIndex + it)
        }
        
        val transformedText = builder.toAnnotatedString()
        
        val offsetMapping = object : androidx.compose.ui.text.input.OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                var transformedOffset = 0
                var currentOriginal = 0
                
                for (match in matches) {
                    // Start of tag
                    val start = match.range.first
                    // End of tag (exclusive for substring, inclusive for range is last)
                    val end = match.range.last + 1
                    
                    if (offset <= start) {
                        return transformedOffset + (offset - currentOriginal)
                    }
                    
                    // Add length of text before this match
                    transformedOffset += (start - currentOriginal)
                    currentOriginal = start
                    
                    if (offset < end) {
                        // Offset is INSIDE the tag [IMG:...]
                        // Map to the start of replacement
                        return transformedOffset
                    }
                    
                    // Offset is at or after the end of tag
                    // Add length of replacement
                    transformedOffset += replacementLen 
                    currentOriginal = end
                }
                
                return transformedOffset + (offset - currentOriginal)
            }

            override fun transformedToOriginal(offset: Int): Int {
                 if (offset < 0) return 0
                 if (offset >= transformedToOriginalMapping.size) return originalText.length
                 return transformedToOriginalMapping[offset]
            }
        }

        return androidx.compose.ui.text.input.TransformedText(transformedText, offsetMapping)
    }
}
