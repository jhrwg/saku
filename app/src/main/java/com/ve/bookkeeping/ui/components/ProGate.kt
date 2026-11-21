@file:OptIn(ExperimentalMaterial3Api::class)

package com.ve.bookkeeping.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 无 Pro 权限时的统一提示（见 docs/dev-spec.md）。
 *
 * 规范：底部弹窗、文案固定「该功能需开启 Pro 权益」、
 * 两个按钮（前往 Pro / 以后再说），不再解释付款方式。
 */
@Composable
fun ProGateSheet(
    visible: Boolean,
    onUpgrade: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (!visible) return

    AppSheet(
        onDismiss = onDismiss,
        footer = {
            AppSheetFooter {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(text = "以后再说")
                }
                Button(
                    onClick = onUpgrade,
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(text = "前往 Pro")
                }
            }
        },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.WorkspacePremium,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "该功能需开启 Pro 权益",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "开启后即可使用该功能，也可以在 Pro 页查看全部权益。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
