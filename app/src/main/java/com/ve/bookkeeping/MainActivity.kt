package com.ve.bookkeeping

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ve.bookkeeping.data.AppLog
import com.ve.bookkeeping.ui.ZheErGenApp

/**
 * 注意：这里必须是 ComponentActivity。
 * 用 FragmentActivity 会和 Compose 的 ActivityResult 注册表冲突
 * （选图/导出时抛 "Can only use lower 16 bits for requestCode"）。
 * 指纹解锁改用系统 BiometricPrompt，不需要 FragmentActivity。
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppLog.install(this)
        AppLog.info("启动", "App 冷启动")
        // 主线程卡死（ANR）也留一份日志，下次启动时和崩溃一样可一键提交
        runCatching { com.ve.bookkeeping.data.AnrWatchdog(this).start() }
        // 先把界面偏好读进 UiState，再让主题参与首帧组合：
        // 否则第一帧会用「跟随系统」渲染，看上去像先闪一下系统深色/浅色。
        applyUiPrefs()
        enableEdgeToEdge()
        setContent {
            val density = androidx.compose.ui.platform.LocalDensity.current
            val factor = when (com.ve.bookkeeping.data.UiState.fontScale) {
                com.ve.bookkeeping.data.UiPrefs.FONT_SMALL -> 0.9f
                com.ve.bookkeeping.data.UiPrefs.FONT_LARGE -> 1.15f
                com.ve.bookkeeping.data.UiPrefs.FONT_XLARGE -> 1.3f
                else -> 1f
            }
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.ui.platform.LocalDensity provides
                    androidx.compose.ui.unit.Density(density.density, density.fontScale * factor),
            ) {
                ZheErGenApp()
            }
        }
    }

    /** 同步读取界面偏好：主题模式 / 字体 / 头像。 */
    private fun applyUiPrefs() {
        val prefs = com.ve.bookkeeping.data.UiPrefs(this)
        com.ve.bookkeeping.data.UiState.themeMode = prefs.themeMode
        com.ve.bookkeeping.data.UiState.fontScale = prefs.fontScale
        com.ve.bookkeeping.data.UiState.privacyMode = prefs.privacyMode
        com.ve.bookkeeping.data.UiState.avatarPath = prefs.avatarPath
        com.ve.bookkeeping.data.UiState.betaUpdates = prefs.betaUpdates
        com.ve.bookkeeping.data.UiState.userName = prefs.userName
        com.ve.bookkeeping.data.UiState.userBio = prefs.userBio
    }
}

