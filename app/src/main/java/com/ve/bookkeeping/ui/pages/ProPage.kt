@file:OptIn(ExperimentalMaterial3Api::class)

package com.ve.bookkeeping.ui.pages

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.CurrencyExchange
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ve.bookkeeping.R
import com.ve.bookkeeping.ui.components.SectionCard
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.House
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlin.random.Random
import androidx.compose.material3.HorizontalDivider

private data class ProBenefit(val icon: ImageVector, val title: String, val desc: String)

/** 常驻底栏：开通 / 关闭 PRO，不随内容滚动。 */
@Composable
private fun ProActionBar(
    proEnabled: Boolean,
    onActivate: () -> Unit,
    onDeactivate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    var activating by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (proEnabled) {
            Button(
                onClick = onDeactivate,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 3.dp,
                ),
            ) {
                Text(
                    text = "关闭 Pro（仅本机）",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        } else {
            Button(
                onClick = {
                    if (activating) return@Button
                    activating = true
                    // 伪网络延迟 50~3000ms，期间转圈，模拟真实校验
                    scope.launch {
                        kotlinx.coroutines.delay(Random.nextLong(1500L, 4000L))
                        onActivate()
                        activating = false
                    }
                },
                enabled = !activating,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 3.dp,
                ),
            ) {
                if (activating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(
                        text = "正在校验…",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                } else {
                    Text(
                        text = "开启 Pro（免费）",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

/** 权益小卡：两列宫格用，高度不够时描述最多两行。 */
@Composable
private fun BenefitTile(benefit: ProBenefit, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.55f),
        modifier = modifier.fillMaxWidth().height(112.dp),
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = benefit.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = benefit.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = benefit.desc,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private val PRO_BENEFITS = listOf(
    ProBenefit(Icons.Rounded.Insights, "账本不限量", "多账本随意建，账目独立"),
    ProBenefit(Icons.Rounded.Cloud, "WebDAV 备份", "连接测试、手动 / 每日自动备份"),
    ProBenefit(Icons.Rounded.Palette, "自定义主题", "9 套单色 + 8 张背景 + 自选色 / 自选图"),
    ProBenefit(Icons.Rounded.Savings, "预算全套", "周 / 月 / 年预算、滚动结转、每日额度"),
    ProBenefit(Icons.Rounded.Flag, "储蓄目标", "本期结余 / 目标进度"),
    ProBenefit(Icons.Rounded.ReceiptLong, "退款 · 报销标记", "标记后不计入收支与预算"),
    ProBenefit(Icons.Rounded.BarChart, "图表增强", "近 6 月对比、收入构成、资产走势"),
    ProBenefit(Icons.Rounded.CurrencyExchange, "多币种 · 汇率", "153 种货币 + 实时汇率，可离线换算"),
    ProBenefit(Icons.Rounded.CreditCard, "信用卡账单", "本期应还、最低还款、还款提醒"),
    ProBenefit(Icons.Rounded.House, "车贷 · 房贷", "等额本息月供与还款记账"),
    ProBenefit(Icons.Rounded.Fingerprint, "指纹解锁", "不用输密码，一按就进"),
    ProBenefit(Icons.Rounded.SystemUpdate, "关闭启动自检", "启动时不再检查更新"),
    ProBenefit(Icons.Rounded.NewReleases, "新功能优先", "后续新功能优先给你"),
)

/**
 * 升级 PRO：权益说明 + 收款码（占位/真实图）+ 诚信付款开通。
 */
@Composable
fun ProPage(
    proEnabled: Boolean,
    autoUpdateCheck: Boolean,
    betaUpdates: Boolean,
    onToggleBetaUpdates: (Boolean) -> Unit,
    onToggleAutoUpdate: (Boolean) -> Unit,
    onActivate: () -> Unit,
    onDeactivate: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onBack)
    var zoomed by remember { mutableStateOf<Int?>(null) }
    // 开启测试版前先提示风险
    var showBetaWarning by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 108.dp),
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "升级 Pro",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "喜欢就继续用，打赏随意",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(14.dp))
        HeroCard(proEnabled = proEnabled)

        Spacer(modifier = Modifier.height(14.dp))
        SectionCard(title = "Pro 权益") {
            PRO_BENEFITS.chunked(3).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    row.forEach { benefit ->
                        BenefitTile(benefit = benefit, modifier = Modifier.weight(1f))
                    }
                    repeat(3 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        SectionCard(title = "开通说明") {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Rounded.Verified,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.size(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    listOf(
                        "本软件由 AI 辅助开发，纯属做一个自己喜欢的记账软件，毕竟现在是 AI 时代了。",
                        "软件不可能弄成付费的，如果用的习惯，随便打赏一点就好，几毛几分都无所谓。",
                        "无论是否打赏，均可通过点击下方底部按钮开启 Pro 模式。",
                    ).forEachIndexed { index, paragraph ->
                        if (index > 0) Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = paragraph,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 19.sp,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        SectionCard(title = "收款码", trailing = {
            Text(
                text = "点击图片放大",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QrThumb(
                    res = R.drawable.qr_alipay,
                    label = "支付宝",
                    modifier = Modifier.weight(1f),
                    onClick = { zoomed = R.drawable.qr_alipay },
                )
                QrThumb(
                    res = R.drawable.qr_wechat,
                    label = "微信支付",
                    modifier = Modifier.weight(1f),
                    onClick = { zoomed = R.drawable.qr_wechat },
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        SectionCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "启动时检查更新",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = if (proEnabled) {
                            "每次打开软件自动检查一次，有新版会弹窗提示"
                        } else {
                            "Pro 用户可以关闭启动自检"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = autoUpdateCheck,
                    onCheckedChange = { onToggleAutoUpdate(it) },
                    enabled = proEnabled,
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "测试版检查更新",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = if (!proEnabled) {
                            "Pro 用户可选择优先接收测试版"
                        } else if (betaUpdates) {
                            "已开启：启动自检与手动检查都会找测试版"
                        } else {
                            "关闭时只接收正式版更新"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = betaUpdates,
                    onCheckedChange = { wanted ->
                        if (wanted && !betaUpdates) {
                            showBetaWarning = true
                        } else {
                            onToggleBetaUpdates(wanted)
                        }
                    },
                    enabled = proEnabled,
                )
            }
        }

    }

    ProActionBar(
        proEnabled = proEnabled,
        onActivate = onActivate,
        onDeactivate = onDeactivate,
        modifier = Modifier.align(Alignment.BottomCenter),
    )
    }

    zoomed?.let { res ->
        Dialog(onDismissRequest = { zoomed = null }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .clickable { zoomed = null },
            ) {
                Image(
                    painter = painterResource(res),
                    contentDescription = "收款码",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }

    if (showBetaWarning) {
        ModalBottomSheet(onDismissRequest = { showBetaWarning = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 28.dp),
            ) {
                Text(
                    text = "开启测试版前请先了解风险",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "因测试版全由 AI 自动开发提交，会出现：频繁发版、不稳定、闪退等问题，" +
                        "请您了解风险后再开启此功能，建议更新前备份当前数据。",
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { showBetaWarning = false }) {
                        Text(text = "先不开")
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = {
                            showBetaWarning = false
                            onToggleBetaUpdates(true)
                        },
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(text = "我已了解，开启")
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroCard(proEnabled: Boolean) {
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        primary,
                        primary.copy(alpha = 0.86f),
                        tertiary.copy(alpha = 0.9f),
                    ),
                ),
            )
            .padding(horizontal = 20.dp, vertical = 20.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.WorkspacePremium,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp),
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "折耳根 Pro",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Spacer(modifier = Modifier.weight(1f))
                if (proEnabled) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.22f),
                    ) {
                        Text(
                            text = "已开通",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "解锁全部功能",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "也帮这个小软件继续更新下去",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.85f),
            )
        }
    }
}

@Composable
private fun QrThumb(
    res: Int,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(14.dp),
                )
                .background(Color.White)
                .clickable { onClick() },
        ) {
            Image(
                painter = painterResource(res),
                contentDescription = label,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize().padding(6.dp),
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}






