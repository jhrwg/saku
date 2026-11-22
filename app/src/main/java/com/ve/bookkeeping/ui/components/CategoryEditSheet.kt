@file:OptIn(ExperimentalMaterial3Api::class)

package com.ve.bookkeeping.ui.components

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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ve.bookkeeping.ui.model.EntryCategory
import com.ve.bookkeeping.ui.model.IconCatalog

/**
 * 分类编辑：改名字 + 换图标。
 *
 * 规范：**二级分类只能改名，不能换图标**（[allowIcon] = false）。
 */
@Composable
fun CategoryEditSheet(
    category: EntryCategory,
    onDismiss: () -> Unit,
    onSave: (String, String?) -> Unit,
) {
    val allowIcon = category.parentId == null
    var name by remember(category.id) { mutableStateOf(category.label) }
    var iconId by remember(category.id) { mutableStateOf<String?>(null) }

    AppSheet(
        onDismiss = onDismiss,
        footer = {
            AppSheetFooter {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                ) { Text(text = "取消") }
                Button(
                    onClick = { onSave(name.trim(), iconId) },
                    enabled = name.isNotBlank(),
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                ) { Text(text = "保存") }
            }
        },
    ) {
        Text(
            text = if (allowIcon) "编辑分类" else "编辑子分类",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { if (it.length <= 8) name = it },
            singleLine = true,
            label = { Text(text = "分类名称") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
        )

        Spacer(modifier = Modifier.height(12.dp))
        if (allowIcon) {
            Text(
                text = "图标（点一下替换）",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            val ctx = androidx.compose.ui.platform.LocalContext.current
            val icons = androidx.compose.runtime.remember(IconPrefsVersion()) {
                com.ve.bookkeeping.ui.pages.RemoteIcons.localIcons(ctx, "category")
            }
            icons.chunked(6).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    row.forEach { item ->
                        val selected = iconId == item.id || (iconId == null && item.id == category.id)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .background(
                                    if (selected) {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f)
                                    },
                                    RoundedCornerShape(12.dp),
                                )
                                .clickable { iconId = item.id },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (item.vector != null) {
                                Icon(
                                    imageVector = item.vector,
                                    contentDescription = item.label,
                                    tint = if (selected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                    modifier = Modifier.size(20.dp),
                                )
                            } else {
                                val bmp = androidx.compose.runtime.remember(item.id) {
                                    com.ve.bookkeeping.ui.pages.RemoteIcons.load(ctx, item.id, 128)
                                }
                                if (bmp != null) {
                                    androidx.compose.foundation.Image(
                                        bitmap = bmp,
                                        contentDescription = item.label,
                                        modifier = Modifier.size(24.dp),
                                    )
                                }
                            }
                        }
                    }
                    repeat(6 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
                }
            }
        } else {
            Text(
                text = "子分类只能改名字，图标跟随上级分类。",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** 读一下全局图标版本号，下载/换了图标后自动重组。 */
@Composable
private fun IconPrefsVersion(): Int = com.ve.bookkeeping.data.IconPrefs.version
