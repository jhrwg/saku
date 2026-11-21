@file:OptIn(ExperimentalMaterial3Api::class)

package com.ve.bookkeeping.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 首页侧边栏：当前账本列表 + 新增账本入口。
 * 纯 UI：账本只存在内存里，切换后首页副标题会跟着变。
 */
@Composable
fun LedgerDrawerSheet(
    books: List<String>,
    selectedBook: String,
    proEnabled: Boolean,
    onSelectBook: (String) -> Unit,
    onAddBook: () -> Unit,
) {
    ModalDrawerSheet(modifier = Modifier.width(272.dp)) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 20.dp),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                    Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "折",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "折耳根记账",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "本地账本 · 单位 ¥",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "账本",
                modifier = Modifier.padding(start = 12.dp, bottom = 4.dp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            books.forEach { book ->
                val badge: (@Composable () -> Unit)? = if (book == selectedBook) {
                    { Text(text = "当前", style = MaterialTheme.typography.labelSmall) }
                } else {
                    null
                }
                NavigationDrawerItem(
                    label = { Text(text = book) },
                    selected = book == selectedBook,
                    onClick = { onSelectBook(book) },
                    icon = { Icon(imageVector = Icons.Rounded.MenuBook, contentDescription = null) },
                    badge = badge,
                    modifier = Modifier.padding(vertical = 2.dp),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            NavigationDrawerItem(
                label = { Text(text = "新增账本") },
                selected = false,
                onClick = onAddBook,
                icon = { Icon(imageVector = Icons.Rounded.Add, contentDescription = null) },
                modifier = Modifier.padding(vertical = 2.dp),
            )
            if (!proEnabled) {
                Text(
                    text = "免费版仅支持 1 个账本，Pro 可无限创建",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "账本与账目都保存在本机",
                modifier = Modifier.padding(horizontal = 12.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
    }
}

/** 新增账本弹窗。 */
@Composable
fun AddBookDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "新增账本") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { if (it.length <= 12) name = it },
                label = { Text(text = "账本名称") },
                placeholder = { Text(text = "例如：装修账本") },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.trim()) },
                enabled = name.isNotBlank(),
            ) {
                Text(text = "创建")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "取消")
            }
        },
    )
}
