@file:OptIn(ExperimentalMaterial3Api::class)

package com.ve.bookkeeping.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ve.bookkeeping.data.AppLog
import com.ve.bookkeeping.data.LogFilter
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 日志中心（全 App 唯一入口）。
 *
 * 「我的 → 日志」和「意见反馈」都用这一个弹窗：分类筛选、查看、复制、导出 txt、清空。
 * 导出会把日志和机型信息写进一个 .txt，再通过系统分享面板发出去，
 * 方便在问卷、微信、邮件里直接当作附件上传。
 */
@Composable
fun LogCenterSheet(
    onDismiss: () -> Unit,
    withDeviceInfo: Boolean = false,
    appVersion: String = "",
    proEnabled: Boolean = false,
) {
    val context = LocalContext.current
    var filter by remember { mutableStateOf(LogFilter.All) }
    var revision by remember { mutableIntStateOf(0) }
    val logText = remember(filter, revision) { AppLog.text(context, filter) }
    val count = remember(filter, revision) { AppLog.count(context, filter) }

    fun header(): String = buildString {
        if (withDeviceInfo) {
            appendLine("—— 环境信息 ——")
            appendLine("App 版本：v$appVersion")
            appendLine("机型：${android.os.Build.BRAND} ${android.os.Build.MODEL}")
            appendLine("系统：Android ${android.os.Build.VERSION.RELEASE ?: "未知"}")
            appendLine("Pro：${if (proEnabled) "已开通" else "未开通"}")
            appendLine()
        }
        appendLine("—— 日志（${filter.label}）——")
    }

    fun exportTxt() {
        val body = header() + logText.ifBlank { "（这个分类还没有日志）" }
        runCatching {
            val dir = File(context.cacheDir, "logs").apply { mkdirs() }
            val stamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.CHINA).format(Date())
            val file = File(dir, "vebk-log-$stamp.txt")
            file.writeText(body)
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file,
            )
            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(android.content.Intent.EXTRA_SUBJECT, "折耳根记账 日志")
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(android.content.Intent.createChooser(intent, "导出日志 txt"))
        }
    }

    AppSheet(
        onDismiss = onDismiss,
        footer = {
            Column {
                Button(
                    onClick = { exportTxt() },
                    enabled = logText.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(text = "导出 txt 文件")
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE)
                                as? android.content.ClipboardManager
                            clipboard?.setPrimaryClip(
                                android.content.ClipData.newPlainText("日志", header() + logText),
                            )
                        },
                        enabled = logText.isNotBlank(),
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(text = "复制全文")
                    }
                    OutlinedButton(
                        onClick = {
                            AppLog.clear(context)
                            revision += 1
                        },
                        enabled = count > 0,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(text = "清空日志", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
    ) {
            Text(
                text = "日志",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (count > 0) {
                    "当前分类 $count 个日志文件（运行日志按天存，崩溃单独一份）"
                } else {
                    "这个分类还没有日志"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                var menuOpen by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(onClick = { menuOpen = true }, shape = RoundedCornerShape(14.dp)) {
                        Text(text = "查看：${filter.label}")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "▾")
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        LogFilter.entries.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(text = item.label) },
                                onClick = {
                                    menuOpen = false
                                    filter = item
                                },
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = { revision += 1 }) { Text(text = "刷新") }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = logText.ifBlank { "没有日志。运行过程与崩溃信息都会自动记在这里。" },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )

        }
}
