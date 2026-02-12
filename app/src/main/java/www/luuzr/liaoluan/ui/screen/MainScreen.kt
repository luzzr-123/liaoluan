package www.luuzr.liaoluan.ui.screen

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import www.luuzr.liaoluan.ui.component.BrutalButton
import www.luuzr.liaoluan.ui.component.BrutalToast
import www.luuzr.liaoluan.ui.component.ParticleSystem
import www.luuzr.liaoluan.ui.modal.ItemModal
import www.luuzr.liaoluan.ui.modal.SettingsModal
import www.luuzr.liaoluan.ui.theme.BrutalColors
import www.luuzr.liaoluan.ui.viewmodel.MainViewModel

/**
 * 主界面 — 对应原型的 BrutalPlanner 根组件
 * 包含：Tab 栏 / HorizontalPager / 悬浮按钮 / Toast / 粒子 / 模态
 */
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val habits by viewModel.habits.collectAsState()
    val notes by viewModel.notes.collectAsState()

    val pagerState = rememberPagerState(
        initialPage = uiState.currentTab,
        pageCount = { 3 }
    )
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    // ==================== 通知跳转 ====================
    var showTaskCompleteDialogId by remember { mutableStateOf<Long?>(null) }
    
    LaunchedEffect(Unit) {
        (context as? android.app.Activity)?.intent?.let { intent ->
            val taskId = intent.getLongExtra("show_complete_dialog_task_id", -1L)
            if (taskId != -1L) {
                showTaskCompleteDialogId = taskId
                intent.removeExtra("show_complete_dialog_task_id")
            }
        }
    }
    
    // 确认弹窗
    if (showTaskCompleteDialogId != null) {
         androidx.compose.material3.AlertDialog(
            onDismissRequest = { showTaskCompleteDialogId = null },
            title = { Text("任务进度确认", fontWeight = FontWeight.Bold) },
            text = { Text("您是否已经完成了该任务？") },
            confirmButton = {
                BrutalButton(
                    text = "是的，已完成",
                    onClick = {
                        showTaskCompleteDialogId?.let { id ->
                            val taskToComplete = tasks.find { it.id == id }
                            if (taskToComplete != null) {
                                viewModel.toggleTask(taskToComplete.id, true)
                                viewModel.showToast("太棒了！任务已标记完成")
                            }
                        }
                        showTaskCompleteDialogId = null
                    },
                    modifier = Modifier.width(120.dp)
                )
            },
            dismissButton = {
                BrutalButton(
                     text = "还没",
                     onClick = { showTaskCompleteDialogId = null },
                     backgroundColor = BrutalColors.White,
                     modifier = Modifier.width(80.dp)
                )
            },
            containerColor = BrutalColors.White,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp)
        )
    }

    // ==================== 导出功能 (SAF) ====================
    var pendingExportJson by remember { mutableStateOf("") }
    
    val exportLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            scope.launch {
                val success = www.luuzr.liaoluan.util.FileHelper.writeTextToUri(context, it, pendingExportJson)
                if (success) viewModel.showToast("导出成功") else viewModel.showToast("导出失败")
            }
        }
    }

    // 导入 launcher
    val importLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            scope.launch {
                val text = www.luuzr.liaoluan.util.FileHelper.readTextFromUri(context, it)
                if (text != null) {
                    viewModel.importSingleItem(text)
                } else {
                    viewModel.showToast("无法读取文件")
                }
            }
        }
    }

    LaunchedEffect(uiState.currentTab) {
        if (pagerState.currentPage != uiState.currentTab) {
            pagerState.animateScrollToPage(uiState.currentTab)
        }
    }
    LaunchedEffect(pagerState.currentPage) {
        viewModel.switchTab(pagerState.currentPage)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ==================== 顶部 Tab 栏 ====================
            TabBar(
                currentTab = uiState.currentTab,
                onTabSelected = { index ->
                    viewModel.switchTab(index)
                    scope.launch { pagerState.animateScrollToPage(index) }
                }
            )

            // ==================== 页面内容 ====================
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> TaskScreen(
                        tasks = tasks,
                        onToggle = viewModel::toggleTask,
                        onDelete = viewModel::deleteTask,
                        onEdit = viewModel::openEditModal
                    )
                    1 -> HabitScreen(
                        habits = habits,
                        onProgress = viewModel::progressHabit,
                        onDelete = viewModel::deleteHabit,
                        onEdit = viewModel::openEditModal
                    )
                    2 -> NoteScreen(
                        notes = notes,
                        onDelete = viewModel::deleteNote,
                        onEdit = viewModel::openEditModal,
                        onExportNote = { note ->
                             pendingExportJson = viewModel.getNoteJson(note)
                             exportLauncher.launch("note_${note.id}.json")
                        },
                        onTogglePin = viewModel::toggleNotePin,
                        onOpenSettings = viewModel::toggleSettings
                    )
                }
            }
        }

        // ==================== 悬浮操作按钮 ====================
        // ==================== 悬浮操作按钮 (Design v2) ====================
        // ==================== 悬浮操作按钮 ====================
        // ==================== 悬浮操作按钮 (Design v2) - Settings moved to NoteScreen
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 32.dp, end = 24.dp)
        ) {
            // 添加按钮 (NoteYellow)
            Box(
                contentAlignment = Alignment.Center
            ) {
                // 阴影
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .offset(x = 4.dp, y = 4.dp)
                        .background(BrutalColors.Black)
                )
                
                val fabColor by androidx.compose.animation.animateColorAsState(
                    targetValue = when (uiState.currentTab) {
                        0 -> BrutalColors.TaskRed
                        1 -> BrutalColors.HabitTeal
                        2 -> BrutalColors.NoteYellow
                        else -> BrutalColors.NoteYellow
                    },
                    label = "fabColor"
                )

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(fabColor)
                        .border(3.dp, BrutalColors.Black)
                        .clickable { viewModel.openNewModal() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "添加",
                        tint = BrutalColors.Black,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        // ==================== Toast 通知 ====================
        AnimatedVisibility(
            visible = uiState.toast != null,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp)
        ) {
            uiState.toast?.let { toast ->
                BrutalToast(
                    message = toast.message,
                    onUndo = toast.onUndo,
                    onClose = viewModel::dismissToast,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        // ==================== 粒子效果 ====================
        if (uiState.showParticles) {
            ParticleSystem(
                active = true,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // ==================== 模态弹窗 ====================
        AnimatedVisibility(
            visible = uiState.showModal,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            ItemModal(
                currentTab = uiState.currentTab,
                editingTask = uiState.editingTask,
                editingHabit = uiState.editingHabit,
                editingNote = uiState.editingNote,
                onSaveTask = viewModel::saveTask,
                onSaveHabit = viewModel::saveHabit,
                onSaveNote = viewModel::saveNote,
                onExportNote = { note ->
                    pendingExportJson = viewModel.getNoteJson(note)
                    exportLauncher.launch("note_${note.id}.json")
                },
                onClose = viewModel::closeModal
            )
        }

        // ==================== 导入弹窗 ====================
        var showImportDialog by remember { mutableStateOf(false) }

        if (showImportDialog) {
            www.luuzr.liaoluan.ui.modal.ImportDialog(
                onDismiss = { showImportDialog = false },
                onConfirm = { json ->
                    viewModel.importSingleItem(json)
                    showImportDialog = false
                },
                onSelectFile = {
                    importLauncher.launch("application/json")
                    showImportDialog = false
                }
            )
        }

        // ==================== 设置弹窗 ====================
        
        AnimatedVisibility(
            visible = uiState.showSettings,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            SettingsModal(
                onClose = viewModel::toggleSettings,
                onExport = {
                    pendingExportJson = viewModel.getBackupJson()
                    exportLauncher.launch("brutal_backup_${System.currentTimeMillis()}.json")
                    viewModel.toggleSettings()
                },
                onImport = {
                    showImportDialog = true
                    viewModel.toggleSettings()
                }
            )
        }
    }
}

/**
 * 顶部 Tab 栏 — 对应原型的三色 Tab 切换
 */
@Composable
private fun TabBar(currentTab: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf(
        Triple("任务", BrutalColors.TaskRed, "✓"),
        Triple("习惯", BrutalColors.HabitTeal, "⟳"),
        Triple("笔记", BrutalColors.NoteYellow, "✎")
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .border(width = 4.dp, color = BrutalColors.Black)
    ) {
        tabs.forEachIndexed { index, (label, color, icon) ->
            val isSelected = currentTab == index
            Box(
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (isSelected) Modifier.height(64.dp) else Modifier.height(56.dp)
                    )
                    .background(if (isSelected) color else BrutalColors.LightGray)
                    .clickable { onTabSelected(index) },
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = icon,
                            fontSize = 20.sp
                        )
                        Text(
                            text = label,
                            fontWeight = FontWeight.Bold,
                            fontFamily = androidx.compose.ui.text.font.FontFamily(
                                androidx.compose.ui.text.font.Font(www.luuzr.liaoluan.R.font.kaiti)
                            ),
                            fontSize = 16.sp,
                            letterSpacing = 2.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Text(
                        text = label,
                        fontWeight = FontWeight.Bold,
                        fontFamily = androidx.compose.ui.text.font.FontFamily(
                            androidx.compose.ui.text.font.Font(www.luuzr.liaoluan.R.font.kaiti)
                        ),
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
            // Tab 间分隔线
            if (index < tabs.lastIndex) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(if (isSelected) 64.dp else 56.dp)
                        .background(BrutalColors.Black)
                )
            }
        }
    }
}
