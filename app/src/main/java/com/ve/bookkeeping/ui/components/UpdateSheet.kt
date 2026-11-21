@file:OptIn(ExperimentalMaterial3Api::class)

package com.ve.bookkeeping.ui.components

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.ve.bookkeeping.data.UpdateInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface

/**
 * 检查到新版本时从底部弹出的面板：版本号、更新日志、下载通道。
 *
 * 下载在面板内完成：开始后隐藏其它按钮、只留进度条，下完自动拉起系统安装器。
 */
@Composable
fun UpdateSheet(
    info: UpdateInfo,
    currentVersion: String,
    onOpenChangelog: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    var downloading by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var statusText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var apkFile by remember { mutableStateOf<File?>(null) }

    fun install(file: File) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !context.packageManager.canRequestPackageInstalls()
        ) {
            runCatching {
                context.startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:${context.packageName}"),
                    ),
                )
            }
            statusText = "请先允许「安装未知应用」，再回来点安装"
            error = "缺少安装权限"
            return
        }
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
        runCatching {
            context.startActivity(
                Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/vnd.android.package-archive")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                },
            )
        }.onFailure { error = "无法调起安装器：${it.message}" }
    }

    fun startDownload(url: String) {
        if (downloading) return
        downloading = true
        error = null
        progress = 0f
        statusText = "正在连接…"
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    val dir = File(context.cacheDir, "apk").apply { mkdirs() }
                    val target = File(dir, "vebk-${info.version}.apk")
                    val client = OkHttpClient()
                    client.newCall(Request.Builder().url(url).build()).execute().use { response ->
                        if (!response.isSuccessful) {
                            throw IllegalStateException("HTTP ${response.code}")
                        }
                        val body = response.body ?: throw IllegalStateException("响应为空")
                        val total = body.contentLength()
                        var received = 0L
                        body.byteStream().use { input ->
                            target.outputStream().use { output ->
                                val buffer = ByteArray(64 * 1024)
                                var read: Int
                                while (input.read(buffer).also { read = it } != -1) {
                                    output.write(buffer, 0, read)
                                    received += read
                                    progress = if (total > 0) {
                                        (received.toFloat() / total).coerceIn(0f, 1f)
                                    } else {
                                        0f
                                    }
                                    statusText = if (total > 0) {
                                        "正在下载 ${received / 1024 / 1024}MB / ${total / 1024 / 1024}MB"
                                    } else {
                                        "正在下载 ${received / 1024 / 1024}MB"
                                    }
                                }
                            }
                        }
                    }
                    target
                }
            }.onSuccess { file ->
                apkFile = file
                progress = 1f
                statusText = "下载完成，正在安装…"
                downloading = false
                install(file)
            }.onFailure { throwable ->
                downloading = false
                error = throwable.message ?: "下载失败"
                statusText = "下载失败"
            }
        }
    }

    AppSheet(
        onDismiss = { if (!downloading) onDismiss() },
        footer = {
            if (downloading || apkFile != null) {
                // 下载中：进度固定在底部，避免按钮被误点
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (apkFile != null) Icons.Rounded.CheckCircle else Icons.Rounded.Download,
                            contentDescription = null,
                            tint = if (apkFile != null) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                    )
                    if (error != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = { apkFile?.let { install(it) } },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text(text = "重新安装") }
                    }
                }
            } else {
                // 三个下载通道：悬浮固定在底部，不随更新日志滚动
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { info.directUrl?.let { startDownload(it) } },
                        enabled = !info.directUrl.isNullOrBlank(),
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) { Text(text = "GitHub", style = MaterialTheme.typography.labelLarge) }
                    OutlinedButton(
                        onClick = {
                            val url = info.mirrorUrl ?: info.directUrl
                            url?.let { startDownload(it) }
                        },
                        enabled = !(info.mirrorUrl ?: info.directUrl).isNullOrBlank(),
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) { Text(text = "在线", style = MaterialTheme.typography.labelLarge) }
                    Button(
                        onClick = { info.giteeUrl?.let { startDownload(it) } },
                        enabled = !info.giteeUrl.isNullOrBlank(),
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) { Text(text = "Gitee", style = MaterialTheme.typography.labelLarge) }
                }
            }
        },
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "发现新版本",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "现版 v$currentVersion → 新版 v${info.version}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(10.dp))
            // 日志区：固定高度、内部滚动，不影响底部按钮
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant,
                ),
                modifier = Modifier.fillMaxWidth().height(176.dp),
            ) {
                Text(
                    text = info.changelog.ifBlank { "本次更新没有写日志" },
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 21.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(14.dp),
                )
            }

            error?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.ErrorOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            // 次要操作：历史日志 / 稍后（不能挤占底部三个下载按钮）
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onOpenChangelog) {
                    Text(text = "查看历史更新日志", style = MaterialTheme.typography.labelMedium)
                }
                if (!info.force) {
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = onDismiss) {
                        Text(text = "稍后再说", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}
