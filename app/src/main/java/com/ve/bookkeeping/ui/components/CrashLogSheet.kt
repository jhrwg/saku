@file:OptIn(ExperimentalMaterial3Api::class)

package com.ve.bookkeeping.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ve.bookkeeping.data.LogFilter

/**
 * 日志面板：可切换「全部 / 崩溃 / 运行」三类日志，查看、复制、导出 txt、清空。
 *
 * 以前这里只有崩溃日志；现在运行过程（记账、删除、备份、更新检查…）也一起记，
 * 排查「数据没保存」「返回跳错页」这类问题才有线索。
 */
@Composable
fun CrashLogSheet(
    logText: String?,
    count: Int,
    filter: LogFilter,
    onFilterChange: (LogFilter) -> Unit,
    onShare: () -> Unit,
    onShareTxt: () -> Unit,
    onCopy: () -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var menuOpen by remember { mutableStateOf(false) }

    AppSheet(onDismiss = onDismiss) {
        Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
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
            Box {
                OutlinedButton(
                    onClick = { menuOpen = true },
                    shape = RoundedCornerShape(14.dp),
                ) {
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
                                onFilterChange(item)
                            },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = logText ?: "没有日志。运行过程与崩溃信息都会自动记在这里。",
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceContainerHigh,
                        RoundedCornerShape(14.dp),
                    )
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onCopy,
                    enabled = logText != null,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(text = "复制")
                }
                Button(
                    onClick = onShareTxt,
                    enabled = logText != null,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(text = "导出并发送")
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row {
                TextButton(onClick = onShare, enabled = logText != null) {
                    Text(text = "直接分享文本")
                }
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = onClear, enabled = count > 0) {
                    Text(text = "清空日志", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
