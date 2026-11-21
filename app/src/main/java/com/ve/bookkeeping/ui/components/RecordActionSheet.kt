@file:OptIn(ExperimentalMaterial3Api::class)

package com.ve.bookkeeping.ui.components

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
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ve.bookkeeping.ui.model.EntryType
import com.ve.bookkeeping.ui.model.LedgerRecord
import com.ve.bookkeeping.ui.model.RecordFlag
import com.ve.bookkeeping.ui.theme.LocalAppExtraColors
import androidx.compose.material3.TextButton

/** 点击一条记录后的操作面板：编辑 / 删除。 */
@Composable
fun RecordActionSheet(
    record: LedgerRecord,
    proEnabled: Boolean,
    refundTargets: List<LedgerRecord> = emptyList(),
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSetFlag: (RecordFlag) -> Unit,
    onLinkRefund: (Long?) -> Unit = {},
    onNeedPro: () -> Unit,
    onDismiss: () -> Unit,
) {
    val extra = LocalAppExtraColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val amountColor = when (record.type) {
        EntryType.Expense -> extra.expense
        EntryType.Income -> extra.income
        EntryType.Transfer -> MaterialTheme.colorScheme.onSurface
    }
    val amountText = when (record.type) {
        EntryType.Expense -> "-${record.currency.symbol}${formatMoney(record.amount)}"
        EntryType.Income -> "+${record.currency.symbol}${formatMoney(record.amount)}"
        EntryType.Transfer -> "${record.currency.symbol}${formatMoney(record.amount)}"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (record.type == EntryType.Transfer) {
                            "${record.fromAccount.name} → ${record.toAccount?.name ?: "—"}"
                        } else {
                            record.category.label
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = record.note.ifBlank { record.type.label },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = amountText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = amountColor,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            ActionRow(
                icon = Icons.Rounded.Edit,
                label = "编辑这条记录",
                tint = MaterialTheme.colorScheme.onSurface,
                onClick = onEdit,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "标记（退款 / 报销不计入收支统计）",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                RecordFlag.entries.forEach { flag ->
                    FilterChip(
                        selected = record.flag == flag,
                        onClick = {
                            if (flag.isFlagged && !proEnabled) onNeedPro() else onSetFlag(flag)
                        },
                        label = {
                            Text(
                                text = if (flag.isFlagged && !proEnabled) {
                                    "${flag.label} · Pro"
                                } else {
                                    flag.label
                                },
                            )
                        },
                    )
                }
            }

            if (record.flag == RecordFlag.Refund && refundTargets.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "关联原消费（退款会从原支出里扣掉）",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(6.dp))
                refundTargets.take(4).forEach { target ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${target.category.label} · ¥${formatMoney(target.amount)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(
                            onClick = {
                                onLinkRefund(
                                    if (record.refundOfId == target.id) null else target.id,
                                )
                            },
                        ) {
                            Text(
                                text = if (record.refundOfId == target.id) "取消关联" else "关联",
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            ActionRow(
                icon = Icons.Rounded.Delete,
                label = "删除这条记录",
                tint = MaterialTheme.colorScheme.error,
                onClick = onDelete,
            )
        }
    }
}

@Composable
private fun ActionRow(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = tint,
            )
        }
    }
}

