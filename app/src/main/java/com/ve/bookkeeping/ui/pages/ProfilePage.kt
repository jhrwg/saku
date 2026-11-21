@file:OptIn(ExperimentalMaterial3Api::class)

package com.ve.bookkeeping.ui.pages

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.House
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.SyncAlt
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.Feedback
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ve.bookkeeping.ui.components.SectionCard
import com.ve.bookkeeping.ui.components.SettingsRow
import com.ve.bookkeeping.ui.model.LedgerRecord
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Sell
import androidx.compose.material.icons.rounded.AccountBalanceWallet

/**
 * 我的：
 * 顶部标题 + 外观/设置 → 资料卡（头像 + 三格统计）→ PRO 区 → 功能区宫格 → 更多。
 * 其余设置项收进右上角「设置」页。
 */
@Composable
fun ProfilePage(
    records: List<LedgerRecord>,
    books: List<String>,
    categoryCount: Int,
    recurringCount: Int,
    proEnabled: Boolean,
    appVersion: String,
    updateStatus: String?,
    onCheckUpdate: () -> Unit,
    onOpenChangelog: () -> Unit,
    onOpenRecords: () -> Unit,
    onOpenBudget: () -> Unit,
    onOpenTransfer: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenSecurity: () -> Unit,
    onOpenTrash: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenLoans: () -> Unit,
    onOpenCrashLog: () -> Unit,
    crashCount: Int,
    budgetSummary: String,
    onOpenCategories: () -> Unit,
    onOpenWebDav: () -> Unit,
    onOpenAppearance: () -> Unit,
    themeModeLabel: String,
    onCycleThemeMode: () -> Unit,
    fontSizeLabel: String,
    onCycleFontScale: () -> Unit,
    materialYouLabel: String,
    onToggleMaterialYou: () -> Unit,
    privacyLabel: String,
    onTogglePrivacy: () -> Unit,
    onOpenPro: () -> Unit,
    onOpenRecurring: () -> Unit,
    onOpenFeedback: () -> Unit,
    onOpenTags: () -> Unit,
    onOpenDebts: () -> Unit,
    modifier: Modifier = Modifier,
    avatarPath: String = "",
    onAvatarPicked: (String) -> Unit = {},
    userName: String = "折耳根记账",
    userBio: String = "记录你的一点一滴",
    onSaveProfile: (String, String, String) -> Unit,
) {
    val activeDays = activeDayCount(records)
    val total = records.size
    var showEditSheet by remember { mutableStateOf(false) }
    var expandedFunctions by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        // 标题 + 外观 + 设置
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "我的",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onOpenWebDav) {
                Icon(
                    imageVector = Icons.Rounded.CloudSync,
                    contentDescription = "云备份",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp),
                )
            }
            IconButton(onClick = onOpenAppearance) {
                Icon(
                    imageVector = Icons.Rounded.Palette,
                    contentDescription = "外观",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp),
                )
            }
        }

        ProfileCard(
            avatarPath = avatarPath,
            onAvatarPicked = onAvatarPicked,
            userName = userName,
            subtitle = userBio,
            onEditProfile = { showEditSheet = true },
            activeDays = activeDays,
            bookCount = books.size,
            recordCount = total,
        )

        if (showEditSheet) {
            ProfileEditSheet(
                avatarPath = avatarPath,
                userName = userName,
                userBio = userBio,
                onDismiss = { showEditSheet = false },
                onSave = { name, bio, path ->
                    onSaveProfile(name, bio, path)
                    showEditSheet = false
                },
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        ProBanner(proEnabled = proEnabled, onClick = onOpenPro)

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "功能",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
        )
        SectionCard {
            FunctionTileRow(
                tiles = listOf(
                    Triple(Icons.Rounded.Category, "分类", "$categoryCount 个") to onOpenCategories,
                    Triple(Icons.Rounded.Repeat, "周期记账", if (recurringCount > 0) "$recurringCount 条" else "未设置") to onOpenRecurring,
                    Triple(
                        Icons.Rounded.Savings,
                        "预算",
                        if (budgetSummary == "还没设置") "未设置" else "已设置",
                    ) to onOpenBudget,
                    Triple(Icons.Rounded.House, "车贷房贷", "月供记账") to onOpenLoans,
                ),
            )
            Spacer(modifier = Modifier.height(8.dp))
            FunctionTileRow(
                tiles = listOf(
                    Triple(Icons.Rounded.ReceiptLong, "全部记录", "$total 笔") to onOpenRecords,
                    Triple(Icons.Rounded.Sell, "标签", "打标签") to onOpenTags,
                    Triple(Icons.Rounded.SyncAlt, "导入导出", "CSV") to onOpenTransfer,
                    Triple(Icons.Rounded.DeleteSweep, "回收站", "可恢复") to onOpenTrash,
                ),
            )

            if (expandedFunctions) {
                Spacer(modifier = Modifier.height(8.dp))
                FunctionTileRow(
                    tiles = listOf(
                        Triple(Icons.Rounded.NotificationsActive, "通知", "提醒") to onOpenNotifications,
                        Triple(Icons.Rounded.Lock, "安全", "密码") to onOpenSecurity,
                        Triple(Icons.Rounded.FormatSize, "字体", fontSizeLabel) to onCycleFontScale,
                        Triple(
                            Icons.Rounded.BugReport,
                            "日志",
                            if (crashCount > 0) "$crashCount 条" else "无",
                        ) to onOpenCrashLog,
                    ),
                )
                Spacer(modifier = Modifier.height(8.dp))
                FunctionTileRow(
                    tiles = listOf(
                        Triple(Icons.Rounded.AccountBalanceWallet, "借入借出", "待收待还") to onOpenDebts,
                    ),
                )
            }
        }

        // 固定显示 8 个，点小三角展开更多功能
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            IconButton(onClick = { expandedFunctions = !expandedFunctions }) {
                Icon(
                    imageVector = if (expandedFunctions) {
                        Icons.Rounded.KeyboardArrowUp
                    } else {
                        Icons.Rounded.KeyboardArrowDown
                    },
                    contentDescription = if (expandedFunctions) "收起功能" else "展开更多功能",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "更多",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
        )
        SectionCard {
            SettingsRow(
                icon = Icons.Rounded.SystemUpdate,
                title = "检查更新",
                subtitle = updateStatus ?: "当前版本 v$appVersion",
                onClick = onCheckUpdate,
            )
            SettingsRow(
                icon = Icons.Rounded.History,
                title = "更新日志",
                onClick = onOpenChangelog,
            )
            SettingsRow(
                icon = Icons.Rounded.Feedback,
                title = "意见反馈",
                onClick = onOpenFeedback,
            )
            SettingsRow(
                icon = Icons.Rounded.Info,
                title = "关于",
                onClick = onOpenAbout,
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "折耳根记账 v$appVersion",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun FunctionTileRow(
    tiles: List<Pair<Triple<androidx.compose.ui.graphics.vector.ImageVector, String, String>, () -> Unit>>,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        tiles.forEach { (data, action) ->
            val (icon, title, subtitle) = data
            ToolTile(
                icon = icon,
                title = title,
                subtitle = subtitle,
                modifier = Modifier.weight(1f),
                onClick = action,
            )
        }
        // 不满 4 个时补空位，保持列宽一致
        repeat((4 - tiles.size).coerceAtLeast(0)) {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

/** 顶部资料卡：头像 + 名称 + 一句话 + 三格统计。 */
@Composable
private fun ProfileCard(
    avatarPath: String,
    onAvatarPicked: (String) -> Unit,
    userName: String,
    subtitle: String,
    onEditProfile: () -> Unit,
    activeDays: Int,
    bookCount: Int,
    recordCount: Int,
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarBadge(avatarPath = avatarPath, onPicked = onAvatarPicked)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        androidx.compose.material3.IconButton(
                            onClick = onEditProfile,
                            modifier = Modifier.size(26.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = "编辑资料",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(15.dp),
                            )
                        }
                    }
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatPill(label = "天数", value = "$activeDays", modifier = Modifier.weight(1f))
                StatPill(label = "账本", value = "$bookCount", modifier = Modifier.weight(1f))
                StatPill(label = "记录数", value = "$recordCount", modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatPill(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** 头像：点击从相册选图，复制到应用私有目录。 */
@Composable
private fun AvatarBadge(avatarPath: String, onPicked: (String) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        if (uri != null) {
            val target = java.io.File(context.filesDir, "avatar_${System.currentTimeMillis()}.jpg")
            runCatching {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    target.outputStream().use { output -> input.copyTo(output) }
                }
            }.onSuccess { onPicked(target.absolutePath) }
        }
    }

    val bitmap = remember(avatarPath) {
        if (avatarPath.isBlank()) {
            null
        } else {
            runCatching { android.graphics.BitmapFactory.decodeFile(avatarPath) }.getOrNull()
        }
    }

    Box {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(56.dp)
                .clickable { launcher.launch("image/*") },
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "头像",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "折",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(18.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.PhotoCamera,
                contentDescription = "更换头像",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(3.dp),
            )
        }
    }
}

/** PRO 横幅：未开通是招募卡，已开通显示状态。 */
@Composable
private fun ProBanner(proEnabled: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.WorkspacePremium,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(26.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (proEnabled) "折耳根 PRO 已开通" else "开通 折耳根 PRO",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Text(
                    text = if (proEnabled) {
                        "全部 13 项权益已解锁"
                    } else {
                        "云备份、预算、多币种、指纹等 13 项权益"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                )
            }
            Text(
                text = if (proEnabled) "查看" else "去看看",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}

@Composable
private fun ToolTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(78.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** 活跃天数：有记录的不同天数。 */
private fun activeDayCount(records: List<LedgerRecord>): Int {
    if (records.isEmpty()) return 0
    return records.map { it.occurredAt / (24L * 60L * 60L * 1000L) }.distinct().size
}

/** 编辑资料：头像 / 名称 / 个性签名。 */
@Composable
private fun ProfileEditSheet(
    avatarPath: String,
    userName: String,
    userBio: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var name by remember { mutableStateOf(userName) }
    var bio by remember { mutableStateOf(userBio) }
    var path by remember { mutableStateOf(avatarPath) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        if (uri != null) {
            val target = java.io.File(context.filesDir, "avatar_${System.currentTimeMillis()}.jpg")
            runCatching {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    target.outputStream().use { output -> input.copyTo(output) }
                }
            }.onSuccess { path = target.absolutePath }
        }
    }

    val bitmap = remember(path) {
        if (path.isBlank()) null else runCatching { android.graphics.BitmapFactory.decodeFile(path) }.getOrNull()
    }

    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
        ) {
            Text(
                text = "编辑资料",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(64.dp)
                        .clickable { launcher.launch("image/*") },
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "头像",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "折",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "点击头像更换",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "从相册选择一张图片",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            androidx.compose.material3.OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(12) },
                label = { Text(text = "名称") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
            )
            Spacer(modifier = Modifier.height(10.dp))
            androidx.compose.material3.OutlinedTextField(
                value = bio,
                onValueChange = { bio = it.take(30) },
                label = { Text(text = "个性签名") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                androidx.compose.material3.OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                ) { Text(text = "取消") }
                androidx.compose.material3.Button(
                    onClick = {
                        com.ve.bookkeeping.data.AppLog.info("资料", "点击保存 name=$name bio=$bio")
                        onSave(
                            name.trim().ifBlank { "折耳根记账" },
                            bio.trim().ifBlank { "记录你的一点一滴" },
                            path,
                        )
                    },
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                ) { Text(text = "保存") }
            }
        }
    }
}
