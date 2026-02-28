package www.luuzr.liaoluan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import www.luuzr.liaoluan.ui.screen.MainScreen
import www.luuzr.liaoluan.ui.theme.BrutalTheme
import androidx.activity.viewModels
import www.luuzr.liaoluan.ui.viewmodel.MainViewModel

/**
 * 单 Activity 架构 — 所有界面由 Compose 管理
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Android 13+ 请求通知权限
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val permission = android.Manifest.permission.POST_NOTIFICATIONS
            if (checkSelfPermission(permission) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(permission), 101)
            }
        }

        // M4 Fix: 使用 OnBackPressedDispatcher 替代废弃的 onBackPressed()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!viewModel.handleBackPress()) {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        })

        setContent {
            BrutalTheme {
                MainScreen()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 跨天自愈：从后台返回时如果日期变了，触发 ViewModel 的重置逻辑
        viewModel.processIntent(www.luuzr.liaoluan.ui.viewmodel.MainIntent.CheckCrossDayReset)
    }
}
