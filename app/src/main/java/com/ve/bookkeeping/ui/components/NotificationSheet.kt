@file:OptIn(ExperimentalMaterial3Api::class)

package com.ve.bookkeeping.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 通知设置：总开关、每日提醒、提醒时间、权限引导。
 */
@Composable
fun NotificationSheet(
    notificationsEnabled: Boolean,
    permissionGranted: Boolean,
    reminderEnabled: Boolean,
    hour: Int,
    minute: Int,
    onToggleNotifications: (Boolean) -> Unit,
    onToggleReminder: (Boolean) -> Unit,
    onTimeChange: (Int, Int) -> Unit,
    onRequestPermission: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val presets = listOf(8 to 0, 12 to 0, 18 to 0, 20 to 0, 21 to 30)

    AppSheet(
        onDismiss = onDismiss,
        footer = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text(text = "完成")
            }
        },
    ) {
        Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
        ) {
            Text(
                text = "通知",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "周期记账补记、云备份结果会发通知；每日提醒可以自己定时间。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "允许通知",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = if (permissionGranted) "系统权限已开启" else "系统权限未开启，点这里申请",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = notificationsEnabled && permissionGranted,
                    onCheckedChange = { enabled ->
                        if (enabled && !permissionGranted) onRequestPermission()
                        onToggleNotifications(enabled)
                    },
                )
            }
            if (!permissionGranted) {
                TextButton(onClick = onRequestPermission) { Text(text = "去申请通知权限") }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "每日记账提醒",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "每天 %02d:%02d 提醒一次".format(hour, minute),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = reminderEnabled,
                    enabled = notificationsEnabled && permissionGranted,
                    onCheckedChange = onToggleReminder,
                )
            }

            if (reminderEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "提醒时间",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    presets.forEach { (h, m) ->
                        FilterChip(
                            selected = hour == h && minute == m,
                            onClick = { onTimeChange(h, m) },
                            label = { Text("%02d:%02d".format(h, m)) },
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "国产 ROM 可能限制后台任务，若提醒不准，请在系统设置里允许「自启动 / 后台运行」。",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

        }
    }
}
