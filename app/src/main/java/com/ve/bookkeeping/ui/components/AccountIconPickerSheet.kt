@file:OptIn(ExperimentalMaterial3Api::class)

package com.ve.bookkeeping.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ve.bookkeeping.data.IconPrefs
import com.ve.bookkeeping.ui.model.AppIcon
import com.ve.bookkeeping.ui.pages.RemoteIcons

/**
 * 账户图标选择：内置矢量图标 + 图标库里已下载的银行/支付图标。
 * 选中后写入 [IconPrefs]，立即生效。
 */
@Composable
fun AccountIconPickerSheet(
    accountId: String,
    currentIconId: String,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val version = IconPrefs.version
    val icons: List<AppIcon> = remember(version) {
        RemoteIcons.localIcons(context).filter { it.scope == "account" || it.vector != null }
    }
    val banks = icons.filter { it.vector == null }
    val vectors = icons.filter { it.vector != null }

    AppSheet(onDismiss = onDismiss) {
        Text(
            text = "更换图标",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(10.dp))
        if (banks.isNotEmpty()) {
            Text(
                text = "银行 / 支付（已下载）",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(6.dp))
            banks.chunked(5).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    row.forEach { item ->
                        Box(modifier = Modifier.weight(1f)) {
                            Cell(
                                selected = currentIconId == item.id,
                                onClick = {
                                    IconPrefs.setAccountIcon(context, accountId, item.id)
                                    onDismiss()
                                },
                            ) {
                                val bmp = remember(item.id) { RemoteIcons.load(context, item.id, 128) }
                                if (bmp != null) {
                                    Image(
                                        bitmap = bmp,
                                        contentDescription = item.label,
                                        modifier = Modifier.size(24.dp),
                                    )
                                }
                            }
                        }
                    }
                    repeat(5 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
        Text(
            text = "内置图标",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(6.dp))
        vectors.chunked(6).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { item ->
                    Box(modifier = Modifier.weight(1f)) {
                        Cell(
                            selected = currentIconId == item.id,
                            onClick = {
                                IconPrefs.setAccountIcon(context, accountId, item.id)
                                onDismiss()
                            },
                        ) {
                            item.vector?.let { vec ->
                                Icon(
                                    imageVector = vec,
                                    contentDescription = item.label,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }
                }
                repeat(6 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun Cell(
    selected: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f)
                },
                RoundedCornerShape(12.dp),
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) { content() }
}
