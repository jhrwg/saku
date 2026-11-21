@file:OptIn(ExperimentalMaterial3Api::class)

package com.ve.bookkeeping.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 数据导入导出面板：导出全部账目为 CSV，或从 CSV 追加导入。
 */
@Composable
fun DataTransferSheet(
    recordCount: Int,
    busy: Boolean,
    statusText: String?,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    AppSheet(onDismiss = onDismiss) {
        Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
        ) {
            Text(
                text = "导入 / 导出",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "当前账本共 $recordCount 条账目，格式为 CSV，可用 Excel / Numbers 打开。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onExport,
                enabled = !busy && recordCount > 0,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text(text = if (busy) "处理中…" else "导出全部账目（CSV）")
            }

            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = onImport,
                enabled = !busy,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text(text = "从 CSV 导入（追加）")
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "导入会按「分类名 / 账户名」新建或复用，不会删除现有记录；" +
                    "建议先导出一份备份再导入。",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (busy || statusText != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (busy) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                    }
                    Text(
                        text = statusText ?: "请稍候…",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
