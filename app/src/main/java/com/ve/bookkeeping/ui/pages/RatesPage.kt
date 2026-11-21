@file:OptIn(ExperimentalMaterial3Api::class)

package com.ve.bookkeeping.ui.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ve.bookkeeping.data.ExchangeRates
import com.ve.bookkeeping.ui.DemoData
import com.ve.bookkeeping.ui.components.PageScaffold
import com.ve.bookkeeping.ui.components.SectionCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 汇率页：列出全部货币的「1 单位 = 多少人民币」。
 *
 * 数据来源与应用内换算一致（[ExchangeRates]），联网时每 24 小时自动更新一次，
 * 拉不到就用内置兜底汇率，页面底部会写清楚当前用的是哪一种。
 */
@Composable
fun RatesPage(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var refreshing by remember { mutableStateOf(false) }
    var lastAt by remember { mutableStateOf(ExchangeRates.lastUpdated(context)) }
    var message by remember { mutableStateOf<String?>(null) }
    var query by remember { mutableStateOf("") }

    val online = ExchangeRates.rates
    val catalog = DemoData.currencies
    val rows = remember(query, online) {
        val q = query.trim().lowercase()
        val list = catalog.map { currency ->
            val fallback = currency.rateToCny
            val rate = if (currency.code == "CNY") 1.0 else online[currency.code] ?: fallback
            currency to rate
        }
        if (q.isEmpty()) list else list.filter {
            it.first.code.lowercase().contains(q) ||
                it.first.name.contains(query.trim()) ||
                it.first.symbol.contains(query.trim())
        }
    }

    PageScaffold(
        title = "汇率",
        subtitle = "全部 ${catalog.size} 种货币 · 1 单位 = 多少人民币",
        onBack = onBack,
        modifier = modifier,
        actions = {
            IconButton(onClick = onBack) { }
        },
    ) {
        SectionCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (online.isEmpty()) "内置汇率（未联网更新）" else "已更新为最新汇率",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = when {
                            refreshing -> "正在拉取最新汇率…"
                            lastAt > 0L -> "上次更新：" + SimpleDateFormat(
                                "yyyy-MM-dd HH:mm", Locale.CHINA,
                            ).format(Date(lastAt))
                            else -> "还没联网更新过，先用内置汇率"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Button(
                    onClick = {
                        if (refreshing) return@Button
                        refreshing = true
                        message = null
                        scope.launch {
                            val done = withContext(Dispatchers.IO) { ExchangeRates.refresh(context) }
                            refreshing = false
                            lastAt = ExchangeRates.lastUpdated(context)
                            message = if (done) "汇率已更新" else "更新失败，请检查网络后重试"
                        }
                    },
                    shape = RoundedCornerShape(50),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(text = if (refreshing) "更新中" else "更新汇率")
                }
            }
            message?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            placeholder = { Text(text = "搜索币种代码或名称", style = MaterialTheme.typography.bodySmall) },
        )

        Spacer(modifier = Modifier.height(12.dp))
        SectionCard {
            LazyColumn(modifier = Modifier.heightIn(max = 1200.dp)) {
                items(rows, key = { it.first.code }) { (currency, rate) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        ) {
                            Text(
                                text = currency.code,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currency.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "1 " + currency.code + " = ¥" + if (rate >= 0.01) {
                                    String.format(Locale.CHINA, "%.4f", rate)
                                } else {
                                    String.format(Locale.CHINA, "%.6f", rate)
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            text = "¥1 = " + String.format(Locale.CHINA, "%.4f", 1.0 / rate) + " " + currency.code,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHigh)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "汇率来自公开汇率接口（open.er-api.com），每 24 小时自动更新一次；" +
                "离线时使用应用内置的参考汇率，仅用于记账换算，不构成任何交易依据。",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}
