@file:OptIn(ExperimentalMaterial3Api::class)

package com.ve.bookkeeping.ui.components

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
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.ve.bookkeeping.ui.model.AccountOption
import com.ve.bookkeeping.ui.theme.LocalAppExtraColors
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.Image

/** 点击资产页账户后的操作面板：编辑 / 删除。 */
@Composable
fun AccountActionSheet(
    account: AccountOption,
    isDefault: Boolean,
    initialBalance: Double = account.balance,
    onSetDefault: () -> Unit,
    onEdit: () -> Unit,
    onMoveUp: () -> Unit = {},
    onMoveDown: () -> Unit = {},
    onChangeIcon: () -> Unit = {},
    onAdjustBalance: () -> Unit = {},
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    val extra = LocalAppExtraColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    AppSheet(onDismiss = onDismiss) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = account.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = account.type.label,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "¥${formatMoney(account.balance)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (account.balance < 0) extra.expense else MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "当前余额 · 初始 ¥${formatMoney(initialBalance)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            ActionRow(
                icon = Icons.Rounded.Star,
                label = if (isDefault) "取消默认付款账户" else "设为默认付款账户",
                tint = MaterialTheme.colorScheme.primary,
                onClick = onSetDefault,
            )
            ActionRow(
                icon = Icons.Rounded.EditNote,
                label = "修改余额",
                tint = MaterialTheme.colorScheme.onSurface,
                onClick = onAdjustBalance,
            )
            ActionRow(
                icon = Icons.Rounded.Image,
                label = "更换图标",
                tint = MaterialTheme.colorScheme.onSurface,
                onClick = onChangeIcon,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionRow(
                    icon = Icons.Rounded.ArrowUpward,
                    label = "上移",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    onClick = onMoveUp,
                )
                ActionRow(
                    icon = Icons.Rounded.ArrowDownward,
                    label = "下移",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    onClick = onMoveDown,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            ActionRow(
                icon = Icons.Rounded.Edit,
                label = "编辑账户",
                tint = MaterialTheme.colorScheme.onSurface,
                onClick = onEdit,
            )
            Spacer(modifier = Modifier.height(8.dp))
            ActionRow(
                icon = Icons.Rounded.Delete,
                label = "删除账户",
                tint = MaterialTheme.colorScheme.error,
                onClick = onDelete,
            )
    }
}

@Composable
private fun ActionRow(
    icon: ImageVector,
    label: String,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f),
        modifier = modifier.fillMaxWidth(),
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
            Text(text = label, style = MaterialTheme.typography.bodyLarge, color = tint)
        }
    }
}





