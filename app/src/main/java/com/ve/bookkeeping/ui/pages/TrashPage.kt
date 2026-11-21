@file:OptIn(ExperimentalMaterial3Api::class)

package com.ve.bookkeeping.ui.pages

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ve.bookkeeping.ui.components.PageScaffold
import com.ve.bookkeeping.ui.components.SectionCard
import com.ve.bookkeeping.ui.model.EntryType
import com.ve.bookkeeping.ui.model.LedgerRecord
import com.ve.bookkeeping.ui.moneyText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 回收站：删除的账目可恢复或彻底删除。
 * 页面骨架走 [PageScaffold]，与其它页面完全一致。
 */
@Composable
fun TrashPage(
    records: List<LedgerRecord>,
    onRestore: (LedgerRecord) -> Unit,
    onDeleteForever: (LedgerRecord) -> Unit,
    onClear: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onBack)
    val dateFormat = remember { SimpleDateFormat("MM-dd HH:mm", Locale.CHINA) }

    PageScaffold(
        title = "回收站",
        subtitle = "删除的账目先放这里，恢复后回到流水",
        onBack = onBack,
        actions = if (records.isNotEmpty()) {
            {
                TextButton(onClick = onClear) {
                    Text(
                        text = "清空",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        } else {
            null
        },
        scrollable = false,
        modifier = modifier,
    ) {
        if (records.isEmpty()) {
            SectionCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteSweep,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "回收站是空的",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "在流水里左滑删除的账目会出现在这里",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        } else {
            Text(
                text = "共 ${records.size} 条",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                itemsIndexed(records, key = { index, record -> "trash-${record.id}-$index" }) { _, record ->
                    TrashRow(
                        record = record,
                        dateText = dateFormat.format(Date(record.occurredAt)),
                        onRestore = { onRestore(record) },
                        onDeleteForever = { onDeleteForever(record) },
                    )
                }
            }
        }
    }
}

@Composable
private fun TrashRow(
    record: LedgerRecord,
    dateText: String,
    onRestore: () -> Unit,
    onDeleteForever: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = record.category.label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = buildString {
                            append(dateText)
                            append(" · ")
                            append(record.fromAccount.name)
                            if (record.note.isNotBlank()) {
                                append(" · ")
                                append(record.note)
                            }
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
                Text(
                    text = when (record.type) {
                        EntryType.Expense -> "-¥${moneyText(record.amount)}"
                        EntryType.Income -> "+¥${moneyText(record.amount)}"
                        EntryType.Transfer -> "¥${moneyText(record.amount)}"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onRestore) {
                    Icon(
                        imageVector = Icons.Rounded.Restore,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "恢复")
                }
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = onDeleteForever) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteForever,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "彻底删除", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}


