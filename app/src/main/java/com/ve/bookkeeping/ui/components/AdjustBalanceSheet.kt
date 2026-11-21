@file:OptIn(ExperimentalMaterial3Api::class)

package com.ve.bookkeeping.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** 修改余额的四种方式。 */
enum class BalanceAdjustMode(val label: String, val hint: String, val icon: ImageVector) {
    DifferenceRecord(
        label = "差额补记收支",
        hint = "差额补一笔收入或支出，余额对齐到目标值；这笔会算进收支统计",
        icon = Icons.Rounded.EditNote,
    ),
    DifferenceTransfer(
        label = "差额补记转账",
        hint = "补一笔与「无账户」之间的转账，只调整余额，不算收入也不算支出",
        icon = Icons.Rounded.SwapHoriz,
    ),
    ResetCurrent(
        label = "更改当前余额",
        hint = "直接把余额设成目标值，从此刻重新起算；历史流水仍保留，但不再影响这个账户的余额",
        icon = Icons.Rounded.RestartAlt,
    ),
    SetInitial(
        label = "设置初始余额",
        hint = "只改初始余额，所有流水都保留，余额会按历史账目重新推算",
        icon = Icons.Rounded.Flag,
    ),
}

/**
 * 修改余额面板：先选方式，再填目标余额。
 *
 * 四种方式的区别写在每一条下面，避免用户点完不知道发生了什么。
 */
@Composable
fun AdjustBalanceSheet(
    accountName: String,
    currentBalance: Double,
    initialBalance: Double,
    onConfirm: (BalanceAdjustMode, Double) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var mode by remember { mutableStateOf(BalanceAdjustMode.DifferenceRecord) }
    var amountText by remember { mutableStateOf("") }
    val target = amountText.trim().toDoubleOrNull()
    val diff = target?.minus(currentBalance)

    AppSheet(
        onDismiss = onDismiss,
        footer = {
            Button(
                onClick = { target?.let { onConfirm(mode, it) } },
                enabled = target != null,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text(text = "确认修改")
            }
        },
    ) {
            Text(
                text = "修改余额 · $accountName",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "当前余额 ¥${formatMoney(currentBalance)} · 初始余额 ¥${formatMoney(initialBalance)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = amountText,
                onValueChange = { input ->
                    amountText = input.filter { it.isDigit() || it == '.' || it == '-' }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                label = { Text(text = "目标余额") },
                trailingIcon = {
                    if (diff != null && kotlin.math.abs(diff) >= 0.005) {
                        Text(
                            text = if (diff > 0) "+${formatMoney(diff)}" else formatMoney(diff),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 12.dp),
                        )
                    }
                },
            )

            Spacer(modifier = Modifier.height(14.dp))
            BalanceAdjustMode.entries.forEach { item ->
                val selected = item == mode
                Surface(
                    onClick = { mode = item },
                    shape = RoundedCornerShape(16.dp),
                    color = if (selected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = if (selected) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.size(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = item.hint,
                                style = MaterialTheme.typography.labelSmall,
                                lineHeight = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }
}
