@file:OptIn(ExperimentalMaterial3Api::class)

package com.ve.bookkeeping.ui.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ve.bookkeeping.ui.DemoData
import com.ve.bookkeeping.ui.components.BarTrend
import com.ve.bookkeeping.ui.components.DonutChart
import com.ve.bookkeeping.ui.components.SectionCard
import com.ve.bookkeeping.ui.components.formatMoney
import com.ve.bookkeeping.ui.model.LedgerRecord
import com.ve.bookkeeping.ui.model.StatsRange
import com.ve.bookkeeping.ui.model.statsFor
import androidx.compose.ui.text.style.TextOverflow
import com.ve.bookkeeping.ui.theme.LocalAppExtraColors
import com.ve.bookkeeping.ui.theme.ZheErGenBookkeepingTheme
import java.util.Calendar
import androidx.compose.material3.LinearProgressIndicator
import com.ve.bookkeeping.ui.model.accountBalances
import com.ve.bookkeeping.ui.moneyText

/**
 * 统计页：仪表盘风格。
 *
 * 顶部是区间切换，下面 2×2 指标卡 → 分类占比环图 → 支出趋势柱状。
 */
@Composable
fun StatsPage(
    records: List<LedgerRecord>,
    modifier: Modifier = Modifier,
    accounts: List<com.ve.bookkeeping.ui.model.AccountOption> = emptyList(),
    proEnabled: Boolean = true,
    onNeedPro: () -> Unit = {},
    onTagClick: (String) -> Unit = {},
) {
    var tabIndex by rememberSaveable { mutableIntStateOf(1) }
    val ranges = remember { StatsRange.entries }
    val range = ranges[tabIndex]
    val stats = statsFor(records, range)
    val extra = LocalAppExtraColors.current
    val now = Calendar.getInstance()

    val periodText = when (range) {
        StatsRange.Last7Days -> "近 7 天"
        StatsRange.Month -> "${now.get(Calendar.YEAR)} 年 ${now.get(Calendar.MONTH) + 1} 月"
        StatsRange.Year -> "${now.get(Calendar.YEAR)} 年"
    }

    // 日均：按区间已过天数计算
    val days = when (range) {
        StatsRange.Last7Days -> 7
        StatsRange.Month -> now.get(Calendar.DAY_OF_MONTH)
        StatsRange.Year -> now.get(Calendar.DAY_OF_YEAR)
    }.coerceAtLeast(1)
    val dailyAverage = stats.expense / days

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "统计",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.weight(1f))
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                Text(
                    text = periodText,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ranges.forEachIndexed { index, item ->
                val selected = index == tabIndex
                Surface(
                    onClick = { tabIndex = index },
                    shape = RoundedCornerShape(50),
                    color = if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    },
                ) {
                    Text(
                        text = item.label,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile(
                label = "支出",
                value = stats.expense,
                valueColor = extra.expense,
                hint = "${stats.expenseCount} 笔",
                modifier = Modifier.weight(1f),
            )
            StatTile(
                label = "收入",
                value = stats.income,
                valueColor = extra.income,
                hint = "${stats.incomeCount} 笔",
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile(
                label = "结余",
                value = stats.balance,
                valueColor = MaterialTheme.colorScheme.onSurface,
                hint = if (stats.balance >= 0) "正结余" else "超支",
                modifier = Modifier.weight(1f),
            )
            StatTile(
                label = "日均支出",
                value = dailyAverage,
                valueColor = MaterialTheme.colorScheme.primary,
                hint = "$days 天",
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        CategoryCard(stats.categories, stats.expense)

        if (stats.tags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            TagCard(stats.tags, stats.expense, onTagClick)
        }

        if (stats.incomeCategories.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            IncomeCard(stats.incomeCategories, stats.income)
        }

        Spacer(modifier = Modifier.height(12.dp))
        TrendCard(stats.bars, stats.barLabels)

        if (stats.weekdayBars.any { it > 0f }) {
            Spacer(modifier = Modifier.height(12.dp))
            WeekdayCard(stats.weekdayBars, stats.expense)
        }

        Spacer(modifier = Modifier.height(12.dp))
        AssetTrendCard(records = records, accounts = accounts, proEnabled = proEnabled, onNeedPro = onNeedPro)

        Spacer(modifier = Modifier.height(12.dp))
                SixMonthCard(
            records = records,
            proEnabled = proEnabled,
            onNeedPro = onNeedPro,
        )
        Spacer(modifier = Modifier.height(96.dp))
    }
}

@Composable
private fun StatTile(
    label: String,
    value: Double,
    valueColor: Color,
    hint: String,
    modifier: Modifier = Modifier,
) {
    SectionCard(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "¥${moneyText(value)}",
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 19.sp),
            fontWeight = FontWeight.Bold,
            color = valueColor,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = hint,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        )
    }
}

@Composable
private fun CategoryCard(
    categories: List<com.ve.bookkeeping.ui.model.CategoryStat>,
    totalExpense: Double,
) {
    val extra = LocalAppExtraColors.current

    SectionCard(
        title = "分类占比",
        trailing = {
            Text(
                text = if (categories.isEmpty()) "—" else "TOP ${categories.size}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    ) {
        if (categories.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "这个期间还没有支出",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            return@SectionCard
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            DonutChart(
                slices = categories.map { it.share },
                colors = extra.chart,
                modifier = Modifier.size(126.dp),
                strokeWidth = 18.dp,
                trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "支出",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = moneyText(totalExpense),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            Spacer(modifier = Modifier.size(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                categories.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .background(
                                    color = extra.chart[index % extra.chart.size],
                                    shape = CircleShape,
                                ),
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = item.category.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = "${(item.share * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrendCard(
    bars: List<Float>,
    labels: List<String>,
) {
    val extra = LocalAppExtraColors.current

    SectionCard(
        title = "支出趋势",
        trailing = {
            Text(
                text = if (bars.isEmpty()) "—" else "共 ${bars.size} 段",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    ) {
        BarTrend(
            values = bars,
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp),
            barColor = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            labels.forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun TagCard(
    tags: List<com.ve.bookkeeping.ui.model.TagStat>,
    total: Double,
    onTagClick: (String) -> Unit,
) {
    SectionCard(
        title = "标签支出",
        trailing = {
            Text(
                text = "标签合计 ¥${moneyText(tags.sumOf { it.amount })}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    ) {
        tags.forEach { stat ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTagClick(stat.tag) }
                    .padding(vertical = 4.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "#${stat.tag}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "¥${moneyText(stat.amount)} · ${(stat.share * 100).toInt()}% · ${stat.count} 笔",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                if (stat.share >= 0.02f) {
                    LinearProgressIndicator(
                        progress = { stat.share.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    )
                }
            }
        }
    }
}

@Composable
private fun WeekdayCard(values: List<Float>, total: Double) {
    val labels = listOf("一", "二", "三", "四", "五", "六", "日")
    val maxIndex = values.indices.maxByOrNull { values[it] } ?: 0
    val peak = values.getOrElse(maxIndex) { 0f }
    SectionCard(
        title = "星期分布",
        trailing = {
            Text(
                text = if (peak > 0f) "周${labels[maxIndex]}最多" else "—",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    ) {
        BarTrend(
            values = values,
            modifier = Modifier
                .fillMaxWidth()
                .height(88.dp),
            barColor = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            labels.forEachIndexed { index, label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (index == maxIndex && peak > 0f) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    textAlign = TextAlign.Center,
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "合计 ¥${moneyText(total)}，周末花得多可以看看外卖和娱乐",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun IncomeCard(categories: List<com.ve.bookkeeping.ui.model.CategoryStat>, total: Double) {
    val extra = LocalAppExtraColors.current
    SectionCard(
        title = "收入构成",
        trailing = {
            Text(
                text = "合计 ¥${moneyText(total)}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    ) {
        categories.forEach { stat ->
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stat.category.label,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "¥${moneyText(stat.amount)} · ${(stat.share * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { stat.share.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = extra.income,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                )
            }
        }
    }
}

@Composable
private fun AssetTrendCard(
    records: List<LedgerRecord>,
    accounts: List<com.ve.bookkeeping.ui.model.AccountOption>,
    proEnabled: Boolean,
    onNeedPro: () -> Unit,
) {
    SectionCard(
        title = "资产走势（近 30 天）",
        trailing = {
            Text(
                text = if (proEnabled) "净值变化" else "Pro",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    ) {
        if (!proEnabled) {
            Column {
                Text(
                    text = "资产走势是 Pro 权益：按天看净值变化。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onNeedPro, shape = RoundedCornerShape(14.dp)) {
                    Text(text = "开通 Pro 查看")
                }
            }
            return@SectionCard
        }
        if (accounts.isEmpty()) {
            Text(
                text = "还没有账户数据",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return@SectionCard
        }

        val rates = com.ve.bookkeeping.ui.DemoData.currencies.associate {
        it.code to com.ve.bookkeeping.data.ExchangeRates.rateToCny(it.code, it.rateToCny)
    }
        val now = Calendar.getInstance()
        val todayStart = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val series = (29 downTo 0).map { offset ->
            val dayEnd = todayStart - offset * 86_400_000L + 86_400_000L
            val upto = records.filter { it.occurredAt < dayEnd }
            accountBalances(upto, accounts).sumOf { account ->
                account.balance * (rates[account.currencyCode] ?: 1.0)
            }
        }

        val min = series.minOrNull() ?: 0.0
        val max = series.maxOrNull() ?: 0.0
        val normalized = series.map { (it - min).toFloat() }
        val current = series.lastOrNull() ?: 0.0
        val change = current - (series.firstOrNull() ?: 0.0)

        BarTrend(
            values = normalized,
            modifier = Modifier.fillMaxWidth().height(96.dp),
            barColor = MaterialTheme.colorScheme.tertiary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            barGap = 2.dp,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "30 天前 ¥${moneyText(series.firstOrNull() ?: 0.0)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "现在 ¥${moneyText(current)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = when {
                change > 0.01 -> "较 30 天前 +¥${moneyText(change)}"
                change < -0.01 -> "较 30 天前 -¥${moneyText(-change)}"
                else -> "与 30 天前持平"
            },
            style = MaterialTheme.typography.labelSmall,
            color = if (change >= 0) {
                LocalAppExtraColors.current.income
            } else {
                LocalAppExtraColors.current.expense
            },
        )
        Text(
            text = "区间 ¥${moneyText(min)} ~ ¥${moneyText(max)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SixMonthCard(
    records: List<LedgerRecord>,
    proEnabled: Boolean,
    onNeedPro: () -> Unit,
) {
    val months = remember(records) { lastSixMonths(records) }
    val maxValue = months.maxOfOrNull { it.second } ?: 0f
    val total = months.sumOf { it.second.toDouble() }

    SectionCard(
        title = "近 6 个月支出",
        trailing = {
            Text(
                text = if (proEnabled) "合计 ¥${moneyText(total)}" else "Pro",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    ) {
        if (!proEnabled) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "图表增强是 Pro 权益：月度对比、全年走势、超支预警。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onNeedPro,
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(text = "开通 Pro 查看")
                }
            }
            return@SectionCard
        }

        BarTrend(
            values = months.map { it.second },
            modifier = Modifier.fillMaxWidth().height(96.dp),
            barColor = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            months.forEach { (label) ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (maxValue > 0) {
                "最高一个月 ¥${moneyText(maxValue.toDouble())}"
            } else {
                "这 6 个月还没有支出记录"
            },
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** 近 6 个月（含本月）的支出合计，退款/报销不计。 */
private fun lastSixMonths(records: List<LedgerRecord>): List<Pair<String, Float>> {
    val now = Calendar.getInstance()
    return (5 downTo 0).map { offset ->
        val start = (now.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.MONTH, -offset)
        }
        val end = (start.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
        val sum = records
            .filter {
                it.type == com.ve.bookkeeping.ui.model.EntryType.Expense &&
                    !it.flag.isFlagged &&
                    it.occurredAt >= start.timeInMillis &&
                    it.occurredAt < end.timeInMillis
            }
            .sumOf { it.statAmount }
        val label = "${start.get(Calendar.MONTH) + 1} 月"
        label to sum.toFloat()
    }
}

@Preview(showBackground = true, heightDp = 1100)
@Composable
private fun StatsPagePreview() {
    ZheErGenBookkeepingTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            StatsPage(records = DemoData.seedRecords())
        }
    }
}




