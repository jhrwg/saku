@file:OptIn(ExperimentalMaterial3Api::class)

package com.ve.bookkeeping.ui.pages

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.TrendingDown
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ve.bookkeeping.ui.DemoData
import com.ve.bookkeeping.ui.components.PageHeader
import com.ve.bookkeeping.ui.components.SectionCard
import com.ve.bookkeeping.ui.components.Sparkline
import com.ve.bookkeeping.ui.components.StatTile
import com.ve.bookkeeping.ui.components.TransactionRow
import com.ve.bookkeeping.ui.components.formatMoney
import com.ve.bookkeeping.ui.components.formatSignedMoney
import com.ve.bookkeeping.ui.model.EntryType
import com.ve.bookkeeping.ui.model.HomeSummary
import com.ve.bookkeeping.ui.model.LedgerRecord
import com.ve.bookkeeping.ui.model.summarizeRecords
import com.ve.bookkeeping.ui.theme.LocalAppExtraColors
import com.ve.bookkeeping.ui.theme.ZheErGenBookkeepingTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.ve.bookkeeping.ui.moneyText
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.IconButton
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.material.icons.rounded.Campaign

@Composable
fun HomePage(
    bookName: String,
    records: List<LedgerRecord>,
    modifier: Modifier = Modifier,
    onOpenDrawer: () -> Unit = {},
    onRecordClick: (LedgerRecord) -> Unit = {},
    noticeText: String = "",
    onNoticeClick: () -> Unit = {},
    onOpenRecords: () -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val bookRecords = records.filter { it.bookName == bookName }
    val summary = summarizeRecords(bookRecords)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        PageHeader(
            title = "首页",
            subtitle = "$bookName · 单位 ¥",
            trailing = if (noticeText.isNotBlank()) {
                { NoticePill(text = noticeText, onClick = onNoticeClick) }
            } else {
                null
            },
            leading = {
                Surface(
                    onClick = onOpenDrawer,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.size(44.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Menu,
                            contentDescription = "切换账本",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            },
        )
        OverviewCard(summary)
        Spacer(modifier = Modifier.height(14.dp))
        RecentTransactionsCard(bookRecords, onRecordClick, onOpenRecords)
        // 给浮动按钮留出空间
        Spacer(modifier = Modifier.height(96.dp))
    }
}

@Composable
private fun OverviewCard(summary: HomeSummary) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val extra = LocalAppExtraColors.current
    val now = Calendar.getInstance()
    val month = now.get(Calendar.MONTH) + 1
    val day = now.get(Calendar.DAY_OF_MONTH)

    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "概览",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.weight(1f))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
            ) {
                Text(
                    text = "$month.1 - $month.$day",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "本月支出",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.width(4.dp))
            // 隐私模式：一键把所有金额打码
            IconButton(
                onClick = {
                    val prefs = com.ve.bookkeeping.data.UiPrefs(ctx)
                    prefs.privacyMode = !prefs.privacyMode
                    com.ve.bookkeeping.data.UiState.privacyMode = prefs.privacyMode
                },
                modifier = Modifier.size(24.dp),
            ) {
                Icon(
                    imageVector = if (com.ve.bookkeeping.data.UiState.privacyMode) {
                        Icons.Rounded.VisibilityOff
                    } else {
                        Icons.Rounded.Visibility
                    },
                    contentDescription = "隐私模式",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "¥${moneyText(summary.monthExpense)}",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = extra.expense,
        )

        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            val isPositive = summary.monthBalance >= 0
            Surface(
                shape = RoundedCornerShape(50),
                color = if (isPositive) {
                    extra.income.copy(alpha = 0.12f)
                } else {
                    extra.expense.copy(alpha = 0.12f)
                },
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = if (isPositive) {
                            Icons.Rounded.TrendingUp
                        } else {
                            Icons.Rounded.TrendingDown
                        },
                        contentDescription = null,
                        tint = if (isPositive) extra.income else extra.expense,
                        modifier = Modifier.size(15.dp),
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = "本月结余 ${formatSignedMoney(summary.monthBalance)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isPositive) extra.income else extra.expense,
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "本月 ${summary.recordCount} 笔",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatTile(
                label = "本月收入",
                value = "¥${moneyText(summary.monthIncome)}",
                // 保持和默认主题一样的浅底：主色只做一层很淡的染色
                container = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )
            StatTile(
                label = "日均消费",
                value = "¥${moneyText(summary.dailyAverage)}",
                container = extra.expense.copy(alpha = 0.12f),
                contentColor = extra.expense,
                modifier = Modifier.weight(1f),
            )
            StatTile(
                label = "今日支出",
                value = "¥${moneyText(summary.todayExpense)}",
                container = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Sparkline(
            points = summary.trend,
            modifier = Modifier
                .fillMaxWidth()
                .height(84.dp),
            lineColor = extra.expense,
            fillColor = extra.expense,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf(9, 6, 3, 1, 0).forEach { offset ->
                Text(
                    text = dayLabel(offset),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun RecentTransactionsCard(
    records: List<LedgerRecord>,
    onRecordClick: (LedgerRecord) -> Unit,
    onOpenAll: () -> Unit = {},
) {
    val extra = LocalAppExtraColors.current
    val recent = records.sortedByDescending { it.occurredAt }.take(5)

    SectionCard(
        title = "最近交易",
        trailing = {
            TextButton(onClick = onOpenAll) {
                Text(text = "全部", style = MaterialTheme.typography.labelLarge)
            }
        },
    ) {
        if (recent.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Rounded.ReceiptLong,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(28.dp),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "还没有记账，点右下角「+」记一笔",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            recent.forEachIndexed { index, record ->
                val tint = when (record.type) {
                    EntryType.Expense -> extra.chart[index % extra.chart.size]
                    EntryType.Income -> extra.income
                    EntryType.Transfer -> MaterialTheme.colorScheme.primary
                }
                val title = when (record.type) {
                    EntryType.Transfer ->
                        "${record.fromAccount.name} → ${record.toAccount?.name ?: "—"}"

                    else -> record.category.label
                }
                val subtitle = buildString {
                    append(formatDateTime(record.occurredAt))
                    if (record.note.isNotBlank()) {
                        append(" · ")
                        append(record.note)
                    }
                }
                val amountText = when (record.type) {
                    EntryType.Expense -> "-${record.currency.symbol}${moneyText(record.amount)}"
                    EntryType.Income -> "+${record.currency.symbol}${moneyText(record.amount)}"
                    EntryType.Transfer -> "${record.currency.symbol}${moneyText(record.amount)}"
                }
                val amountColor: Color = when (record.type) {
                    EntryType.Expense -> extra.expense
                    EntryType.Income -> extra.income
                    EntryType.Transfer -> MaterialTheme.colorScheme.onSurface
                }
                val accountLabel = when (record.type) {
                    EntryType.Transfer ->
                        if (record.fee > 0) "手续费 ¥${moneyText(record.fee)}" else "转账"

                    else -> record.fromAccount.name
                }

                TransactionRow(
                    icon = record.category.icon,
                    tint = tint,
                    title = title,
                    subtitle = subtitle,
                    amount = amountText,
                    amountColor = amountColor,
                    account = accountLabel,
                    onClick = { onRecordClick(record) },
                )
            }
        }
    }
}

private fun formatDateTime(millis: Long): String =
    SimpleDateFormat("MM-dd HH:mm", Locale.CHINA).format(Date(millis))

private fun dayLabel(daysAgo: Int): String {
    val calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -daysAgo) }
    return "${calendar.get(Calendar.MONTH) + 1}.${calendar.get(Calendar.DAY_OF_MONTH)}"
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun HomePagePreview() {
    ZheErGenBookkeepingTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            HomePage(
                bookName = DemoData.ledgerBooks.first(),
                records = DemoData.seedRecords(),
            )
        }
    }
}

/**
 * 首页右上角的公告胶囊：单行跑马灯滚动，点击打开链接。
 *
 * 文字放在自己的 Box 里再 clip，避免滚动时盖到喇叭图标上；
 * 首尾各留 2dp，字形不会被切一半。
 */
@Composable
private fun NoticePill(text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.widthIn(max = 168.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.Campaign,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(14.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clipToBounds(),
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    modifier = Modifier
                        .padding(horizontal = 2.dp)
                        .basicMarquee(
                            iterations = Int.MAX_VALUE,
                            initialDelayMillis = 900,
                            repeatDelayMillis = 1200,
                            velocity = 28.dp,
                        ),
                )
            }
        }
    }
}


