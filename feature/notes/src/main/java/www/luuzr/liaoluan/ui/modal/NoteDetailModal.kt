package www.luuzr.liaoluan.ui.modal

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// import androidx.compose.ui.window.Dialog (Removed)
// import androidx.compose.ui.window.DialogProperties (Removed)
import coil.compose.AsyncImage
import coil.request.ImageRequest
import www.luuzr.liaoluan.data.model.Note
import www.luuzr.liaoluan.ui.theme.BrutalColors

@Composable
fun NoteDetailModal(
    note: Note,
    onClose: () -> Unit
) {
    // 移除 Dialog，改为纯 Composable (将在 NoteScreen 中配合 AnimatedVisibility 使用)
    // 全屏遮罩 + 内容
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrutalColors.White) // 全白背景，覆盖原界面，解决状态栏灰色问题
            .clickable(enabled = true, onClick = {}) // 拦截点击
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Paper-like Frame
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFCFBF4)) // Warm paper color
                .border(4.dp, BrutalColors.Black)
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.title.ifEmpty { "无标题" },
                    fontWeight = FontWeight.Black,
                    fontSize = 28.sp,
                    color = BrutalColors.Black,
                    modifier = Modifier.weight(1f)
                )
                
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(BrutalColors.Black)
                        .border(2.dp, BrutalColors.Black)
                        .clickable { onClose() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = BrutalColors.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(2.dp))
            // Decorative line under title
            Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(BrutalColors.Black))
            Spacer(modifier = Modifier.height(20.dp))
            
            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                NoteContentRendererFull(note)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                 // Date at bottom
                Text(
                    text = "创建于: ${www.luuzr.liaoluan.util.DateHandle.formatDateTime(note.createdAt)}",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = BrutalColors.Black.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
private fun NoteContentRendererFull(note: Note) {
    val context = LocalContext.current
    val noteText = note.text
    val imgPattern = Regex("\\[IMG:(.*?)]")

    val parts = remember(noteText) {
        val result = mutableListOf<Pair<String, String?>>()
        var lastIndex = 0
        // Update regex to handle potential newlines within the tag
        val imgPattern = Regex("\\[IMG:(.*?)]", RegexOption.DOT_MATCHES_ALL)
        
        imgPattern.findAll(noteText).forEach { match ->
            if (match.range.first > lastIndex) {
                val textBefore = noteText.substring(lastIndex, match.range.first)
                if (textBefore.isNotEmpty()) {
                    result.add(Pair(textBefore, null))
                }
            }
            result.add(Pair("", match.groupValues[1]))
            lastIndex = match.range.last + 1
        }
        if (lastIndex < noteText.length) {
            val remaining = noteText.substring(lastIndex)
            if (remaining.isNotEmpty()) {
                result.add(Pair(remaining, null))
            }
        }
        if (result.isEmpty() && noteText.isNotEmpty()) {
            result.add(Pair(noteText, null))
        }
        result
    }

    // Identify images that are NOT in the text markers (fallback)
    val referencedUris = parts.mapNotNull { it.second }.toSet()
    val remainingImages = note.images.filter { it !in referencedUris }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        parts.forEach { (text, imageUri) ->
            if (imageUri != null) {
                NoteImage(imageUri, context)
            } else if (text.isNotEmpty()) {
                Text(
                    text = text,
                    fontSize = 18.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight.Medium,
                    color = BrutalColors.Black
                )
            }
        }
        
        // Render fallback images at the end
        remainingImages.forEach { imageUri ->
            NoteImage(imageUri, context)
        }
    }
}

@Composable
private fun NoteImage(imageUri: String, context: android.content.Context) {
    Box {
        // Shadow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp, max = 400.dp)
                .offset(4.dp, 4.dp)
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
                .heightIn(max = 400.dp)
                .background(BrutalColors.White)
                .border(3.dp, BrutalColors.Black)
        )
    }
}
