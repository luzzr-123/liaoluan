package www.luuzr.liaoluan.ui.screen

import android.content.Context
import androidx.compose.runtime.snapshotFlow
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
import www.luuzr.liaoluan.ui.viewmodel.MainViewModel
import www.luuzr.liaoluan.util.BatteryOptHelper
import www.luuzr.liaoluan.ui.modal.BatterySetupDialog
import www.luuzr.liaoluan.ui.theme.BrutalColors

/**
 * 主界面 — 对应原型的 BrutalPlanner 根组件
 * 包含：Tab 栏 / HorizontalPager / 悬浮按钮 / Toast / 粒子 / 模态
 */
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val viewState by viewModel.viewState.collectAsState()
    val uiState = viewState
    val tasks = viewState.tasks
    val habits = viewState.habits
    val filteredNotes = viewState.filteredNotes
    val noteSearchQuery = viewState.noteSearchQuery

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
                                viewModel.processIntent(www.luuzr.liaoluan.ui.viewmodel.MainIntent.ToggleTaskComplete(taskToComplete.id, true))
                                viewModel.processIntent(www.luuzr.liaoluan.ui.viewmodel.MainIntent.ShowToast("太棒了！任务已标记完成"))
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
                if (success) viewModel.processIntent(www.luuzr.liaoluan.ui.viewmodel.MainIntent.ShowToast("导出成功")) 
                else viewModel.processIntent(www.luuzr.liaoluan.ui.viewmodel.MainIntent.ShowToast("导出失败"))
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
                    viewModel.processIntent(www.luuzr.liaoluan.ui.viewmodel.MainIntent.ImportSingleItem(text))
                } else {
                    viewModel.processIntent(www.luuzr.liaoluan.ui.viewmodel.MainIntent.ShowToast("无法读取文件"))
                }
            }
        }
    }

    // ==================== 权限检查与弹窗 ====================
    var showBatterySetup by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!BatteryOptHelper.isIgnoringBatteryOptimizations(context) || !BatteryOptHelper.hasExactAlarmPermission(context)) {
            showBatterySetup = true
        }
    }

    // 精准闹钟权限弹窗 (拦截被拒绝或被系统收回后的异常处理)
    if (uiState.showExactAlarmPermissionDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { viewModel.processIntent(www.luuzr.liaoluan.ui.viewmodel.MainIntent.DismissExactAlarmPermissionDialog) },
            title = { Text("服务受限预警", fontWeight = FontWeight.Black, fontSize = 20.sp) },
            text = { Text("为保证时长习惯完美计时并且不被系统静默抛弃，必须开启系统的「允许设置闹钟和提醒」权限。现在为您跳转至设置。") },
            confirmButton = {
                BrutalButton(
                    text = "去开启",
                    onClick = {
                        viewModel.processIntent(www.luuzr.liaoluan.ui.viewmodel.MainIntent.DismissExactAlarmPermissionDialog)
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                            val intent = android.content.Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                data = android.net.Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        }
                    },
                    modifier = Modifier.width(100.dp)
                )
            },
            dismissButton = {
                BrutalButton(
                    text = "放弃",
                    onClick = { viewModel.processIntent(www.luuzr.liaoluan.ui.viewmodel.MainIntent.DismissExactAlarmPermissionDialog) },
                    backgroundColor = BrutalColors.White,
                    modifier = Modifier.width(80.dp)
                )
            },
            containerColor = BrutalColors.NoteYellow,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp),
            modifier = Modifier.border(4.dp, BrutalColors.Black)
        )
    }

    // C5 Fix v2: pagerState 为唯一数据源
    // 仅在 settledPage 变化时同步回 ViewModel（供业务逻辑使用）
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { settledPage ->
            viewModel.processIntent(www.luuzr.liaoluan.ui.viewmodel.MainIntent.SwitchTab(settledPage))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ==================== 顶部 Tab 栏 ====================
            TabBar(
                currentTab = pagerState.currentPage,  // 直读 pagerState，不读 ViewModel
                onTabSelected = { index ->
                    // 直接操作 pagerState，无需经过 ViewModel
                    scope.launch { pagerState.scrollToPage(index) }
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
                        onToggle = { id, completed -> viewModel.processIntent(www.luuzr.liaoluan.ui.viewmodel.MainIntent.ToggleTaskComplete(id, completed)) },
                        onDelete = { id -> viewModel.processIntent(www.luuzr.liaoluan.ui.viewmodel.MainIntent.DeleteTask(id)) },
                        onEdit = { task -> viewModel.processIntent(www.luuzr.liaoluan.ui.viewmodel.MainIntent.OpenEditTaskModal(task)) }
                    )
                    1 -> HabitScreen(
                        selectedDate = uiState.selectedDate,
                        habitsForDate = uiState.visibleHabits,
                        onDateSelected = { date -> viewModel.processIntent(www.luuzr.liaoluan.ui.viewmodel.MainIntent.SelectDate(date)) },
                        onProgress = { id -> viewModel.processIntent(www.luuzr.liaoluan.ui.viewmodel.MainIntent.ProgressHabit(id)) },
                        onStartDuration = { habit -> viewModel.processIntent(www.luuzr.liaoluan.ui.viewmodel.MainIntent.StartHabitDuration(habit)) },
                        onEndDuration = { habit -> viewModel.processIntent(www.luuzr.liaoluan.ui.viewmodel.MainIntent.EndHabitDuration(habit)) },
                        onDelete = { id -> viewModel.processIntent(www.luuzr.liaoluan.ui.viewmodel.MainIntent.DeleteHabit(id)) },
                        onEdit = { habit -> viewModel.processIntent(www.luuzr.liaoluan.ui.viewmodel.MainIntent.OpenEditHabitModal(habit)) }
                    )
                    2 -> NoteScreen(
                        notes = filteredNotes,
                        searchQuery = noteSearchQuery,
                        onSearchQueryChange = { q -> viewModel.processIntent(www.luuzr.liaoluan.ui.viewmodel.MainIntent.SearchNotes(q)) },
                        onDelete = { id -> viewModel.processIntent(www.luuzr.liaoluan.ui.viewmodel.MainIntent.DeleteNote(id)) },
                        onEdit = { note -> viewModel.processIntent(www.luuzr.liaoluan.ui.viewmodel.MainIntent.OpenEditNoteModal(note)) },
                        onExportNote = { note ->
                             pendingExportJson = viewModel.getNoteJson(note)
                             exportLauncher.launch("note_${note.id}.json")
                        },
                        onTogglePin = { id -> viewModel.processIntent(www.luuzr.liaoluan.ui.viewmodel.MainIntent.ToggleNotePin(id)) },
                        onOpenSettings = { viewModel.processIntent(www.luuzr.liaoluan.ui.viewmodel.MainIntent.ToggleSettings) }
                    )
                }
            }
        }

        // ==================== 悬浮操作按钮 ====================
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 32.dp, end = 24.dp)
        ) {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                
                // Stats Button
                Box(contentAlignment = Alignment.Center) {
                    Box(modifier = Modifier.size(48.dp).offset(x = 4.dp, y = 4.dp).background(BrutalColors.Black))
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(BrutalColors.White)
                            .border(3.dp, BrutalColors.Black)
                            .clickable { viewModel.processIntent(www.luuzr.liaoluan.ui.viewmodel.MainIntent.ToggleStats) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📊", fontSize = 24.sp)
                    }
                }

                // 添加按钮
                Box(contentAlignment = Alignment.Center) {
                    // 阴影
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .offset(x = 4.dp, y = 4.dp)
                            .background(BrutalColors.Black)
                    )
                
                val fabColor by androidx.compose.animation.animateColorAsState(
                    targetValue = when (pagerState.currentPage) {  // BUG-3 Fix: 直读 pagerState
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
                        .clickable { viewModel.processIntent(www.luuzr.liaoluan.ui.viewmodel.MainIntent.OpenNewModal) },
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
                    onClose = { viewModel.processIntent(www.luuzr.liaoluan.ui.viewmodel.MainIntent.DismissToast) },
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
                currentTab = pagerState.currentPage,  // BUG-3 Fix: 直读 pagerState
                editingTask = uiState.editingTask,
                editingHabit = uiState.editingHabit,
                editingNote = uiState.editingNote,
                onSaveTask = { task -> viewModel.processIntent(www.luuzr.liaoluan.ui.viewmodel.MainIntent.SaveTask(task)) },
                onSaveHabit = { habit -> viewModel.processIntent(www.luuzr.liaoluan.ui.viewmodel.MainIntent.SaveHabit(habit)) },
                onSaveNote = { note -> viewModel.processIntent(www.luuzr.liaoluan.ui.viewmodel.MainIntent.SaveNote(note)) },
                onExportNote = { note ->
                    pendingExportJson = viewModel.getNoteJson(note)
                    exportLauncher.launch("note_${note.id}.json")
                },
                onClose = { viewModel.processIntent(www.luuzr.liaoluan.ui.viewmodel.MainIntent.CloseModal) }
            )
        }

        // ==================== 导入弹窗 ====================
        var showImportDialog by remember { mutableStateOf(false) }

        if (showImportDialog) {
            www.luuzr.liaoluan.ui.modal.ImportDialog(
                onDismiss = { showImportDialog = false },
                onConfirm = { json ->
                    viewModel.processIntent(www.luuzr.liaoluan.ui.viewmodel.MainIntent.ImportSingleItem(json))
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
                onClose = { viewModel.processIntent(www.luuzr.liaoluan.ui.viewmodel.MainIntent.ToggleSettings) },
                onExport = {
                    scope.launch {
                        pendingExportJson = viewModel.getBackupJson()
                        exportLauncher.launch("brutal_backup_${System.currentTimeMillis()}.json")
                        viewModel.processIntent(www.luuzr.liaoluan.ui.viewmodel.MainIntent.ToggleSettings)
                    }
                },
                onImport = {
                    showImportDialog = true
                    viewModel.processIntent(www.luuzr.liaoluan.ui.viewmodel.MainIntent.ToggleSettings)
                },
                onManageHabits = {
                    viewModel.processIntent(www.luuzr.liaoluan.ui.viewmodel.MainIntent.ToggleSettings)
                    viewModel.processIntent(www.luuzr.liaoluan.ui.viewmodel.MainIntent.ToggleHabitManagement)
                }
            )
        }

        AnimatedVisibility(
            visible = uiState.showHabitManagement,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.zIndex(100f)
        ) {
            www.luuzr.liaoluan.ui.screen.HabitManagementScreen(
                habits = habits,
                onEdit = { habit ->
                    // UX-4 Fix: 先关闭管理页面再打开编辑弹窗，避免层级叠加
                    viewModel.processIntent(www.luuzr.liaoluan.ui.viewmodel.MainIntent.ToggleHabitManagement)
                    viewModel.processIntent(www.luuzr.liaoluan.ui.viewmodel.MainIntent.OpenEditHabitModal(habit))
                },
                onDelete = { id -> viewModel.processIntent(www.luuzr.liaoluan.ui.viewmodel.MainIntent.DeleteHabit(id)) },
                onBack = { viewModel.processIntent(www.luuzr.liaoluan.ui.viewmodel.MainIntent.ToggleHabitManagement) }
            )
        }

        AnimatedVisibility(
            visible = uiState.showStats,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.zIndex(90f)
        ) {
            www.luuzr.liaoluan.ui.screen.StatsScreen(
                onBack = { viewModel.processIntent(www.luuzr.liaoluan.ui.viewmodel.MainIntent.ToggleStats) }
            )
        }

        // ==================== 电池优化与权限引导 ====================
        AnimatedVisibility(
            visible = showBatterySetup,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.zIndex(200f)
        ) {
            BatterySetupDialog(
                context = context,
                onClose = { showBatterySetup = false }
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
                        if (isSelected) Modifier.height(72.dp) else Modifier.height(64.dp)
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
                        .height(if (isSelected) 72.dp else 64.dp)
                        .background(BrutalColors.Black)
                )
            }
        }
    }
}
