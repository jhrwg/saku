@file:OptIn(ExperimentalMaterial3Api::class)

package com.ve.bookkeeping.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.ve.bookkeeping.data.RemoteBackup
import com.ve.bookkeeping.data.WebDavConfig
import com.ve.bookkeeping.data.WebDavResult
import kotlinx.coroutines.launch
import androidx.compose.material3.FilterChip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Switch

/**
 * WebDAV 备份配置面板。
 *
 * 「保存配置」会先落盘、随后自动测一次连接，并把结果（成功 / 失败原因）显示在面板里；
 * 配置可用时可以直接点「云备份」。
 */
@Composable
fun WebDavSheet(
    config: WebDavConfig,
    lastBackupDay: String?,
    schedule: String,
    intervalHours: Int,
    onScheduleChange: (String, Int) -> Unit,
    backupBeforeUpdate: Boolean,
    onToggleBackupBeforeUpdate: (Boolean) -> Unit,
    onSave: (WebDavConfig) -> Unit,
    onTest: suspend (WebDavConfig) -> WebDavResult,
    onBackup: suspend (WebDavConfig) -> WebDavResult,
    onBackupDone: (String) -> Unit,
    onListBackups: suspend () -> List<RemoteBackup>,
    onLoadBackup: suspend (String) -> String?,
    onPreviewBackup: (String) -> Pair<Int, Int>?,
    onApplyBackup: (String) -> Boolean,
    onDismiss: () -> Unit,
) {
    var address by remember { mutableStateOf(config.address) }
    var account by remember { mutableStateOf(config.account) }
    var token by remember { mutableStateOf(config.token) }
    var folder by remember { mutableStateOf(config.folder) }
    var statusText by remember { mutableStateOf<String?>(null) }
    var statusOk by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var savedConfig by remember { mutableStateOf(config) }
    var customHours by remember { mutableStateOf(intervalHours.toString()) }
    // 本地状态：开关点击后立即反馈，不等页面重建
    var beforeUpdate by remember { mutableStateOf(backupBeforeUpdate) }

    // 恢复流程的状态
    var showBackupList by remember { mutableStateOf(false) }
    var backups by remember { mutableStateOf<List<RemoteBackup>>(emptyList()) }
    var pendingJson by remember { mutableStateOf<String?>(null) }
    var pendingName by remember { mutableStateOf<String?>(null) }
    var pendingCounts by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var restoreBusy by remember { mutableStateOf(false) }
    // 覆盖云端备份前的确认
    var confirmOverwrite by remember { mutableStateOf<RemoteBackup?>(null) }

    val scope = rememberCoroutineScope()
    val canSave = address.isNotBlank() && account.isNotBlank() && token.isNotBlank()

    fun persist(): WebDavConfig {
        val next = WebDavConfig(
            address = address,
            account = account,
            token = token,
            folder = folder.ifBlank { WebDavConfig.DEFAULT_FOLDER },
        )
        onSave(next)
        savedConfig = next
        return next
    }

    /** 真正执行备份（确认覆盖后也会走这里）。 */
    suspend fun runBackup(cfg: WebDavConfig) {
        busy = true
        statusText = null
        val result = onBackup(cfg)
        busy = false
        statusOk = result is WebDavResult.Success
        statusText = when (result) {
            is WebDavResult.Success -> {
                onBackupDone(todayStamp())
                "✓ ${result.message}"
            }

            is WebDavResult.Failure -> "✕ ${result.message}"
        }
    }

    androidx.activity.compose.BackHandler(onBack = onDismiss)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(androidx.compose.foundation.rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "云备份",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "备份文件会放在「${folder.ifBlank { WebDavConfig.DEFAULT_FOLDER }}」文件夹里",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(14.dp))
            OutlinedTextField(
                value = address,
                onValueChange = { address = it.trim() },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "WebDAV 地址") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = account,
                onValueChange = { account = it.trim() },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "账号") },
                placeholder = { Text(text = "坚果云登录邮箱") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = token,
                onValueChange = { token = it.trim() },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "授权码") },
                placeholder = { Text(text = "坚果云「应用密码」") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(14.dp),
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = folder,
                onValueChange = { folder = it.trim() },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "备份文件夹名") },
                placeholder = { Text(text = WebDavConfig.DEFAULT_FOLDER) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
            )

            if (lastBackupDay != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "上次成功备份：$lastBackupDay",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // 自动备份周期
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "自动备份",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = schedule == "startup",
                    onClick = { onScheduleChange("startup", intervalHours) },
                    label = { Text(text = "每次启动") },
                )
                FilterChip(
                    selected = schedule == "hours",
                    onClick = { onScheduleChange("hours", intervalHours) },
                    label = { Text(text = "每 ${intervalHours} 小时") },
                )
                FilterChip(
                    selected = schedule == "daily",
                    onClick = { onScheduleChange("daily", intervalHours) },
                    label = { Text(text = "每天一次") },
                )
            }
            if (schedule == "hours") {
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(2, 6, 12, 24).forEach { hours ->
                        FilterChip(
                            selected = intervalHours == hours,
                            onClick = { onScheduleChange("hours", hours) },
                            label = { Text(text = "$hours 小时") },
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = customHours,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }.take(3)
                        customHours = digits
                        digits.toIntOrNull()?.let { hours ->
                            onScheduleChange("hours", hours.coerceIn(1, 168))
                        }
                    },
                    label = { Text(text = "自定义间隔（1~168 小时）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )
            }

            // 更新前备份
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "更新软件前先备份",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "检测到新版本并准备更新时，先自动备份一次",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = beforeUpdate,
                    onCheckedChange = {
                        beforeUpdate = it
                        onToggleBackupBeforeUpdate(it)
                    },
                )
            }

                        // 按钮放在状态文字上面，避免贴到系统手势区
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(
                    onClick = {
                        val cfg = persist()
                        busy = true
                        statusText = null
                        scope.launch {
                            // 云端已有备份时先确认，避免误覆盖别人/别的设备的备份
                            val existing = runCatching { onListBackups() }.getOrDefault(emptyList())
                            if (existing.isNotEmpty()) {
                                busy = false
                                // 优先显示带时间戳的那份，比 vebk-latest.json 更有信息量
                                confirmOverwrite = existing.firstOrNull {
                                    it.name != "vebk-latest.json"
                                } ?: existing.first()
                            } else {
                                runBackup(cfg)
                            }
                        }
                    },
                    enabled = canSave && !busy,
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(text = "云备份")
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        // 保存配置后自动测一次连接，结果直接显示在面板里
                        val cfg = persist()
                        busy = true
                        statusText = null
                        scope.launch {
                            val result = onTest(cfg)
                            busy = false
                            statusOk = result is WebDavResult.Success
                            statusText = when (result) {
                                is WebDavResult.Success -> "✓ ${result.message}"
                                is WebDavResult.Failure -> "✕ ${result.message}"
                            }
                        }
                    },
                    enabled = canSave && !busy,
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(text = "保存配置")
                }
            }

            // 测 / 备份结果
            if (busy || statusText != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (busy) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = statusText ?: "正在连接…",
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            busy -> MaterialTheme.colorScheme.onSurfaceVariant
                            statusOk -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.error
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            OutlinedButton(
                onClick = {
                    val cfg = persist()
                    restoreBusy = true
                    statusText = null
                    scope.launch {
                        backups = onListBackups()
                        restoreBusy = false
                        if (backups.isEmpty()) {
                            statusOk = false
                            statusText = "✕ 云端没有找到备份文件"
                        } else {
                            showBackupList = true
                        }
                    }
                },
                enabled = canSave && !busy && !restoreBusy,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text(text = if (restoreBusy) "正在读取云端…" else "从云端恢复备份")
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "保存配置后会自动测一次连接；已配置时每天首次启动会自动备份一次。",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    // 1) 选择一份备份
    if (showBackupList) {
        AlertDialog(
            onDismissRequest = { showBackupList = false },
            title = { Text(text = "选择要恢复的备份") },
            text = {
                Column {
                    Text(
                        text = "恢复会用云端数据覆盖本机的账目、账户与周期记账。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(backups) { backup ->
                            Text(
                                text = backup.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showBackupList = false
                                        restoreBusy = true
                                        scope.launch {
                                            val json = onLoadBackup(backup.name)
                                            restoreBusy = false
                                            if (json == null) {
                                                statusOk = false
                                                statusText = "✕ 下载失败：${backup.name}"
                                            } else {
                                                pendingJson = json
                                                pendingName = backup.name
                                                pendingCounts = onPreviewBackup(json)
                                            }
                                        }
                                    }
                                    .padding(vertical = 10.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBackupList = false }) { Text(text = "取消") }
            },
        )
    }

    // 2) 确认覆盖
    confirmOverwrite?.let { remote ->
        AlertDialog(
            onDismissRequest = { confirmOverwrite = null },
            title = { Text(text = "覆盖云端备份？") },
            text = {
                Text(
                    text = "云端已有备份：${remote.name}\n继续备份会用本机数据覆盖它。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val cfg = persist()
                        confirmOverwrite = null
                        scope.launch { runBackup(cfg) }
                    },
                ) { Text(text = "覆盖并备份") }
            },
            dismissButton = {
                TextButton(onClick = { confirmOverwrite = null }) { Text(text = "取消") }
            },
        )
    }

    pendingJson?.let { json ->
        val counts = pendingCounts
        AlertDialog(
            onDismissRequest = { pendingJson = null; pendingName = null },
            title = { Text(text = "确认恢复") },
            text = {
                Column {
                    Text(
                        text = "文件：${pendingName ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (counts != null) {
                            "包含 ${counts.first} 条账目 · ${counts.second} 个账户\n" +
                                "确认后会用这份数据覆盖本机现有数据。"
                        } else {
                            "这个文件读不出有效数据，可能已损坏。"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = counts != null,
                    onClick = {
                        val ok = onApplyBackup(json)
                        pendingJson = null
                        pendingName = null
                        pendingCounts = null
                        statusOk = ok
                        statusText = if (ok) "✓ 已从云端恢复" else "✕ 恢复失败：数据格式不对"
                    },
                ) { Text(text = "覆盖恢复") }
            },
            dismissButton = {
                TextButton(onClick = { pendingJson = null; pendingName = null }) {
                    Text(text = "取消")
                }
            },
        )
    }
}

/** yyyyMMdd，用于判断「今天是否已经备份过」。 */
fun todayStamp(): String =
    java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.CHINA).format(java.util.Date())






