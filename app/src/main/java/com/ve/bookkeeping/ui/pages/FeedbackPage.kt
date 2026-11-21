@file:OptIn(ExperimentalMaterial3Api::class)

package com.ve.bookkeeping.ui.pages

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.ve.bookkeeping.data.AppLog
import com.ve.bookkeeping.data.LogFilter
import com.ve.bookkeeping.ui.components.PageScaffold
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.TextButton

/** 意见反馈问卷地址（腾讯问卷，匿名填写，无需登录）。 */
const val FEEDBACK_FORM_URL = "https://wj.qq.com/s2/28766787/ya5f/"

/**
 * 意见反馈：问卷直接内嵌在 App 里打开；底部可一键导出日志/环境信息。
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun FeedbackPage(
    appVersion: String,
    proEnabled: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var webView by remember { mutableStateOf<WebView?>(null) }
    var reloadKey by remember { mutableIntStateOf(0) }
    var copied by remember { mutableStateOf(false) }

    val deviceInfo = remember {
        buildString {
            append("App 版本：v").append(appVersion)
            append("\n机型：").append(Build.BRAND).append(' ').append(Build.MODEL)
            append("\n系统：Android ").append(Build.VERSION.RELEASE ?: "未知")
            append("\nPro：").append(if (proEnabled) "已开通" else "未开通")
        }
    }

    fun shareLogs() {
        val logs = AppLog.text(context, LogFilter.All).ifBlank { "（还没有日志）" }
        val text = buildString {
            appendLine("—— 环境信息 ——")
            appendLine(deviceInfo)
            appendLine()
            appendLine("—— 最近日志 ——")
            append(logs.take(20000))
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "折耳根记账 反馈日志")
            putExtra(Intent.EXTRA_TEXT, text)
        }
        runCatching {
            context.startActivity(Intent.createChooser(intent, "导出反馈日志"))
        }
    }

    // 返回键：先让 WebView 后退，退到首屏再退出页面
    BackHandler {
        val view = webView
        if (view != null && view.canGoBack()) view.goBack() else onBack()
    }

    PageScaffold(
        title = "意见反馈",
        subtitle = "问卷在应用内打开，匿名填写，不需要登录",
        onBack = onBack,
        scrollable = false,
        modifier = modifier,
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedButton(
                    onClick = { shareLogs() },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.BugReport,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "导出日志")
                }
                Button(
                    onClick = { reloadKey++ },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "刷新问卷")
                }
            }
        },
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadsImagesAutomatically = true
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            url: String?,
                        ): Boolean {
                            if (url == null) return false
                            // 问卷站内跳转留在 WebView，外部链接交给浏览器
                            if (url.contains("wj.qq.com")) return false
                            runCatching {
                                ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            }
                            return true
                        }
                    }
                    loadUrl(FEEDBACK_FORM_URL)
                    webView = this
                }
            },
            update = { view ->
                view.loadUrl("javascript:void(0)")
                if (reloadKey > 0) view.reload()
            },
        )
    }
}

