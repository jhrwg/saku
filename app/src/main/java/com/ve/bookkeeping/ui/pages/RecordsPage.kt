@file:OptIn(ExperimentalMaterial3Api::class)

package com.ve.bookkeeping.ui.pages

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ve.bookkeeping.ui.components.SectionCard
import com.ve.bookkeeping.ui.components.formatMoney
import com.ve.bookkeeping.ui.model.AccountOption
import com.ve.bookkeeping.ui.model.EntryType
import com.ve.bookkeeping.ui.model.LedgerRecord
import com.ve.bookkeeping.ui.theme.LocalAppExtraColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.ve.bookkeeping.ui.moneyText
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.foundation.layout.width
import androidx.compose.material3.TextButton

private enum class TimeFilter(val label: String) {
    All("全部"),
    Month("本月"),
    Last3Months("近 3 月"),
    Year("今年"),
}

/**
 * 记录页：关键字搜索 + 类型 / 时间 / 账户筛选，按天分组。
 */
@Composable
fun RecordsPage(
    records: List<LedgerRecord>,
    accounts: List<AccountOption>,
    onRecordClick: (LedgerRecord) -> Unit,
    onDeleteRecord: (LedgerRecord) -> Unit = {},
    onBack: () -> Unit,
    initialTag: String? = null,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onBack)
    val context = androidx.compose.ui.platform.LocalContext.current

    // 筛选条件持久化：退出 App 再进来仍保留上次选择
    val filterPrefs = remember { context.getSharedPreferences("records_filter", 0) }
    var query by rememberSaveable { mutableStateOf(filterPrefs.getString("query", "") ?: "") }
    var typeOrdinal by rememberSaveable { mutableStateOf(filterPrefs.getInt("type", 0)) }
    var timeOrdinal by rememberSaveable { mutableStateOf(filterPrefs.getInt("time", 0)) }
    var accountId by rememberSaveable {
        mutableStateOf(filterPrefs.getString("account", null))
    }
    var tagFilter by rememberSaveable {
        mutableStateOf(initialTag ?: filterPrefs.getString("tag", null))
    }
    var selectedIds by rememberSaveable { mutableStateOf(emptySet<Long>()) }
    var batchMode by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(query, typeOrdinal, timeOrdinal, accountId, tagFilter) {
        filterPrefs.edit()
            .putString("query", query)
            .putInt("type", typeOrdinal)
            .putInt("time", timeOrdinal)
            .putString("account", accountId)
            .putString("tag", tagFilter)
            .apply()
    }

    val type = when (typeOrdinal) {
        1 -> EntryType.Expense
        2 -> EntryType.Income
        3 -> EntryType.Transfer
        else -> null
    }
    val timeFilter = TimeFilter.entries[timeOrdinal]
    val extra = LocalAppExtraColors.current
    // 所有记录里出现过的标签，用于筛选
    val source = records.toList()
    val availableTags = remember(source) {
        source.flatMap { it.tags }.distinct().sorted()
    }

    // 注意：records 是同一个 SnapshotStateList 实例，用它当 remember 的 key 不会在
    // 增删记录时失效（左滑删除后列表不刷新就是这个原因），所以上面先取了内容快照 source。
    val filtered = remember(source, query, type, timeFilter, accountId, tagFilter) {
        val now = Calendar.getInstance()
        val (start, end) = when (timeFilter) {
            TimeFilter.All -> Long.MIN_VALUE to Long.MAX_VALUE
            TimeFilter.Month -> {
                val startCal = (now.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
                val endCal = (now.clone() as Calendar).apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    add(Calendar.MONTH, 1)
                }
                startOfDay(startCal) to startOfDay(endCal)
            }

            TimeFilter.Last3Months -> {
                val startCal = (now.clone() as Calendar).apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    add(Calendar.MONTH, -2)
                }
                val endCal = (now.clone() as Calendar).apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    add(Calendar.MONTH, 1)
                }
                startOfDay(startCal) to startOfDay(endCal)
            }

            TimeFilter.Year -> {
                val startCal = (now.clone() as Calendar).apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.MONTH, Calendar.JANUARY)
                }
                val endCal = (startCal.clone() as Calendar).apply { add(Calendar.YEAR, 1) }
                startOfDay(startCal) to startOfDay(endCal)
            }
        }

        val keyword = query.trim()
        source
            .filter { record ->
                record.occurredAt in start until end &&
                    (type == null || record.type == type) &&
                    (accountId == null ||
                        record.fromAccount.id == accountId ||
                        record.toAccount?.id == accountId) &&
                    (tagFilter == null || record.tags.contains(tagFilter)) &&
                    (keyword.isEmpty() ||
                        record.note.contains(keyword, true) ||
                        record.category.label.contains(keyword, true) ||
                        record.fromAccount.name.contains(keyword, true) ||
                        record.tags.any { it.contains(keyword, true) } ||
                        record.amount.toString().contains(keyword))
            }
            .sortedByDescending { it.occurredAt }
    }

    val expenseSum = filtered.filter { it.type == EntryType.Expense }.sumOf { it.statAmount }
    val incomeSum = filtered.filter { it.type == EntryType.Income }.sumOf { it.statAmount }

    val grouped = remember(filtered) {
        filtered.groupBy { dayKey(it.occurredAt) }
    }

    Column(modifier = modifier.fillMaxSize().statusBarsPadding()) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "记录",
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("搜索备注 / 分类 / 账户 / 金额") },
            leadingIcon = { Icon(imageVector = Icons.Rounded.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(14.dp),
        )

        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            items(listOf("全部", "支出", "收入", "转账")) { label ->
                val index = listOf("全部", "支出", "收入", "转账").indexOf(label)
                FilterChip(
                    selected = typeOrdinal == index,
                    onClick = { typeOrdinal = index },
                    label = { Text(label) },
                )
            }
        }

        LazyRow(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            items(TimeFilter.entries) { filter ->
                FilterChip(
                    selected = timeFilter == filter,
                    onClick = { timeOrdinal = filter.ordinal },
                    label = { Text(filter.label) },
                )
            }
            items(accounts) { account ->
                FilterChip(
                    selected = accountId == account.id,
                    onClick = {
                        accountId = if (accountId == account.id) null else account.id
                    },
                    label = { Text(account.name) },
                )
            }
        }

        if (availableTags.isNotEmpty()) {
            LazyRow(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                item {
                    Text(
                        text = "标签",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                items(availableTags) { tag ->
                    FilterChip(
                        selected = tagFilter == tag,
                        onClick = { tagFilter = if (tagFilter == tag) null else tag },
                        label = { Text("#$tag") },
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${filtered.size} 笔",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "支出 ¥${moneyText(expenseSum)} · 收入 ¥${moneyText(incomeSum)}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (filtered.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            ) {
                SectionCard {
                    Text(
                        text = "没有匹配的记录\n换个关键字或筛选条件试试",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            if (batchMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "已选 ${selectedIds.size} 条",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = {
                        val all = filtered.map { it.id }.toSet()
                        selectedIds = if (selectedIds.size == all.size) emptySet() else all
                    }) { Text(text = "全选") }
                    TextButton(
                        onClick = {
                            filtered.filter { selectedIds.contains(it.id) }
                                .forEach { onDeleteRecord(it) }
                            selectedIds = emptySet()
                            batchMode = false
                        },
                        enabled = selectedIds.isNotEmpty(),
                    ) {
                        Text(
                            text = "删除",
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    TextButton(onClick = {
                        selectedIds = emptySet()
                        batchMode = false
                    }) { Text(text = "取消") }
                }
            }
            LazyColumn(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 32.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                grouped.forEach { (day, dayRecords) ->
                    item(key = "day-$day") {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = dayLabel(day),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            val dayExpense = dayRecords
                                .filter { it.type == EntryType.Expense }
                                .sumOf { it.statAmount }
                            Text(
                                text = "支出 ¥${moneyText(dayExpense)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    itemsIndexed(dayRecords, key = { index, record -> "rec-${record.id}-$index" }) { _, record ->
                        // 左滑删除：滑出后进入回收站
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { value ->
                                if (value == SwipeToDismissBoxValue.EndToStart) {
                                    onDeleteRecord(record)
                                    true
                                } else {
                                    false
                                }
                            },
                        )
                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromStartToEnd = false,
                            backgroundContent = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 4.dp),
                                    contentAlignment = Alignment.CenterEnd,
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Delete,
                                        contentDescription = "删除",
                                        tint = MaterialTheme.colorScheme.error,
                                    )
                                }
                            },
                        ) {
                            RecordRow(
                                record = record,
                                onClick = { onRecordClick(record) },
                                expenseColor = extra.expense,
                                incomeColor = extra.income,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordRow(
    record: LedgerRecord,
    onClick: () -> Unit,
    expenseColor: androidx.compose.ui.graphics.Color,
    incomeColor: androidx.compose.ui.graphics.Color,
    selected: Boolean = false,
    onLongClick: () -> Unit = {},
) {
    SectionCard(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = record.category.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (record.flag.isFlagged) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = record.flag.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Text(
                    text = buildString {
                        append(timeText(record.occurredAt))
                        append(" · ")
                        append(record.fromAccount.name)
                        if (record.note.isNotBlank()) {
                            append(" · ")
                            append(record.note)
                        }
                        if (record.hasDiscount) {
                            append(" · 省¥")
                            append(formatMoney(record.discount))
                        }
                        if (record.participants.isNotEmpty()) {
                            append(" · ")
                            append(record.participants.size)
                            append(" 人分摊")
                        }
                        if (record.attachments.isNotEmpty()) {
                            append(" · 附件")
                            append(record.attachments.size)
                        }
                        if (record.tags.isNotEmpty()) {
                            append(" · ")
                            append(record.tags.joinToString(" ") { "#$it" })
                        }
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = when (record.type) {
                    EntryType.Expense -> "-¥${moneyText(record.amount)}"
                    EntryType.Income -> "+¥${moneyText(record.amount)}"
                    EntryType.Transfer -> "¥${moneyText(record.amount)}"
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = when (record.type) {
                    EntryType.Expense -> expenseColor
                    EntryType.Income -> incomeColor
                    EntryType.Transfer -> MaterialTheme.colorScheme.onSurface
                },
            )
        }
    }
}

private val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
private val timeFormat = SimpleDateFormat("HH:mm", Locale.CHINA)

private fun dayKey(timestamp: Long): String = dayFormat.format(Date(timestamp))

private fun dayLabel(key: String): String = key

private fun timeText(timestamp: Long): String = timeFormat.format(Date(timestamp))

private fun startOfDay(calendar: Calendar): Long = (calendar.clone() as Calendar).apply {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis


