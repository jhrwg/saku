@file:OptIn(ExperimentalMaterial3Api::class)

package com.ve.bookkeeping.ui.pages

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Refresh
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.ve.bookkeeping.ui.components.LogCenterSheet
import com.ve.bookkeeping.ui.components.PageScaffold

/** 意见反馈问卷地址（腾讯问卷，匿名填写，无需登录）。 */
const val FEEDBACK_FORM_URL = "https://wj.qq.com/s2/28766787/ya5f/"

/**
 * 意见反馈：问卷直接内嵌在 App 里打开。
 *
 * - 底部「日志」打开的弹窗和「我的 → 日志」是同一个（可筛选、可导出 txt）；
 * - 问卷里的「上传附件」按钮会拉起系统文件选择器，选完直接回填到网页里。
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
    var showLogs by remember { mutableStateOf(false) }

    // <input type="file"> 的回调必须由 App 自己接，WebView 默认什么都不做
    var fileCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }
    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val callback = fileCallback
        fileCallback = null
        callback?.onReceiveValue(
            WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data),
        )
    }

    // 返回键：先让 WebView 后退，退到首屏再退出页面
    BackHandler {
        val view = webView
        if (view != null && view.canGoBack()) view.goBack() else onBack()
    }

    PageScaffold(
        title = "意见反馈",
        subtitle = "问卷在应用内打开；需要附日志时点「日志 → 导出 txt」再上传",
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
                    onClick = { showLogs = true },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.BugReport,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "日志 · 导出 txt")
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
                    settings.allowFileAccess = true
                    settings.allowContentAccess = true
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
                    webChromeClient = object : WebChromeClient() {
                        override fun onShowFileChooser(
                            view: WebView?,
                            callback: ValueCallback<Array<Uri>>?,
                            params: FileChooserParams?,
                        ): Boolean {
                            if (callback == null) return false
                            fileCallback?.onReceiveValue(null)
                            fileCallback = callback
                            val launcher = params?.createIntent()
                            if (launcher == null) {
                                fileCallback = null
                                return false
                            }
                            return runCatching {
                                filePicker.launch(launcher)
                                true
                            }.getOrDefault(false)
                        }
                    }
                    loadUrl(FEEDBACK_FORM_URL)
                    webView = this
                }
            },
            update = { view ->
                if (reloadKey > 0) view.reload()
            },
        )
    }

    if (showLogs) {
        LogCenterSheet(
            onDismiss = { showLogs = false },
            withDeviceInfo = true,
            appVersion = appVersion,
            proEnabled = proEnabled,
        )
    }
}
