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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ve.bookkeeping.data.ChangelogGroups
import com.ve.bookkeeping.data.UpdateInfo

/**
 * 更新日志（只读）：把 `Changelog.json` 里的所有版本按「相对本机版本」分成三组。
 *
 * 这里**不放下载/更新按钮**，只用来查看；真正的升级入口还是「检查更新」弹窗。
 */
@Composable
fun ChangelogSheet(
    groups: ChangelogGroups,
    localVersion: String,
    loading: Boolean,
    error: String?,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    AppSheet(
        onDismiss = onDismiss,
        footer = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text(text = "关闭")
            }
        },
    ) {
        Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "更新日志",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "当前设备版本 v$localVersion",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            when {
                loading && groups.isEmpty -> Text(
                    text = "正在读取…",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                error != null -> Column {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = onRetry) { Text(text = "重试") }
                }

                groups.isEmpty -> Text(
                    text = "还没有更新日志",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                else -> Column(
                    modifier = Modifier
                        .heightIn(max = 520.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    if (groups.newer.isNotEmpty()) {
                        SectionTitle("新版本", "比本机新，可升级")
                        groups.newer.forEach { info ->
                            ReleaseCard(info = info, badge = "新版本", highlight = true)
                        }
                    }
                    groups.current?.let { info ->
                        SectionTitle("当前版本", "本机正在使用")
                        ReleaseCard(info = info, badge = "当前", highlight = false)
                    }
                    if (groups.older.isNotEmpty()) {
                        SectionTitle("历史版本", "比本机旧")
                        groups.older.forEach { info ->
                            ReleaseCard(info = info, badge = "历史", highlight = false)
                        }
                    }
                }
            }

        }
    }
}

@Composable
private fun SectionTitle(title: String, hint: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = hint,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ReleaseCard(info: UpdateInfo, badge: String, highlight: Boolean) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (highlight) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f)
        },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "v${info.version}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (!info.date.isNullOrBlank()) {
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = info.date,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (highlight) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                ) {
                    Text(
                        text = badge,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (highlight) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }

            if (info.changelog.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                val sectionNames = setOf("新增", "修复", "优化", "调整")
                info.changelog.lines()
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .forEach { raw ->
                        if (raw in sectionNames) {
                            // 分区标题：单独一行，不加圆点
                            Text(
                                text = raw,
                                modifier = Modifier.padding(top = 8.dp, bottom = 2.dp),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            return@forEach
                        }
                        val line = raw.removePrefix("•").trim()
                        Row(modifier = Modifier.padding(vertical = 1.dp)) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 7.dp, end = 6.dp)
                                    .size(4.dp)
                                    .background(
                                        color = if (highlight) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        },
                                        shape = RoundedCornerShape(50),
                                    ),
                            )
                            Text(
                                text = line,
                                style = MaterialTheme.typography.bodySmall,
                                lineHeight = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
            }
        }
    }
}
