@file:OptIn(ExperimentalMaterial3Api::class)

package com.ve.bookkeeping.ui.pages

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ve.bookkeeping.ui.components.SectionCard
import com.ve.bookkeeping.ui.components.formatMoney
import com.ve.bookkeeping.ui.model.EntryType
import com.ve.bookkeeping.ui.model.LedgerRecord
import com.ve.bookkeeping.ui.theme.LocalAppExtraColors
import java.util.Calendar
import java.util.Locale

/**
 * 日历：按月展示每天的收入/支出，点某天看当天明细。
 */
@Composable
fun CalendarPage(
    records: List<LedgerRecord>,
    onRecordClick: (LedgerRecord) -> Unit,
    modifier: Modifier = Modifier,
) {
    val extra = LocalAppExtraColors.current
    val today = remember { Calendar.getInstance() }
    var year by remember { mutableIntStateOf(today.get(Calendar.YEAR)) }
    var month by remember { mutableIntStateOf(today.get(Calendar.MONTH)) }
    var selectedDay by remember { mutableIntStateOf(today.get(Calendar.DAY_OF_MONTH)) }

    // 当月每天的支出/收入合计
    val dayTotals = remember(records, year, month) {
        val expense = mutableMapOf<Int, Double>()
        val income = mutableMapOf<Int, Double>()
        records.forEach { record ->
            val cal = Calendar.getInstance().apply { timeInMillis = record.occurredAt }
            if (cal.get(Calendar.YEAR) == year && cal.get(Calendar.MONTH) == month) {
                val day = cal.get(Calendar.DAY_OF_MONTH)
                if (record.type == EntryType.Expense) {
                    expense[day] = (expense[day] ?: 0.0) + record.amount
                } else if (record.type == EntryType.Income) {
                    income[day] = (income[day] ?: 0.0) + record.amount
                }
            }
        }
        expense to income
    }

    val firstDayOfWeek = remember(year, month) {
        (Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
        }.get(Calendar.DAY_OF_WEEK) + 5) % 7 // 周一为 0
    }
    val daysInMonth = remember(year, month) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
        }.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    val selectedRecords = remember(records, year, month, selectedDay) {
        records.filter { record ->
            val cal = Calendar.getInstance().apply { timeInMillis = record.occurredAt }
            cal.get(Calendar.YEAR) == year &&
                cal.get(Calendar.MONTH) == month &&
                cal.get(Calendar.DAY_OF_MONTH) == selectedDay
        }.sortedByDescending { it.occurredAt }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "日历",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "按月查看每天收支",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = {
                if (month == 0) {
                    month = 11
                    year -= 1
                } else {
                    month -= 1
                }
            }) { Icon(Icons.Rounded.ChevronLeft, contentDescription = "上个月") }
            Text(
                text = String.format(Locale.CHINA, "%d年%d月", year, month + 1),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            IconButton(onClick = {
                if (month == 11) {
                    month = 0
                    year += 1
                } else {
                    month += 1
                }
            }) { Icon(Icons.Rounded.ChevronRight, contentDescription = "下个月") }
        }

        SectionCard {
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("一", "二", "三", "四", "五", "六", "日").forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))

            val cells = List(firstDayOfWeek) { null } + (1..daysInMonth).toList()
            cells.chunked(7).forEach { week ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    week.forEach { day ->
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            if (day == null) {
                                Spacer(modifier = Modifier.height(44.dp))
                            } else {
                                val isSelected = day == selectedDay
                                val expense = dayTotals.first[day] ?: 0.0
                                val income = dayTotals.second[day] ?: 0.0
                                Surface(
                                    onClick = { selectedDay = day },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    },
                                    border = if (isSelected) {
                                        null
                                    } else {
                                        BorderStroke(
                                            0.5.dp,
                                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                        )
                                    },
                                    modifier = Modifier.padding(2.dp).size(48.dp),
                                ) {
                                    // 日期固定在左上、金额固定占两行：
                                    // 这样有没有收支、只有一边还是两边，数字都不会上下跳
                                    Column(
                                        modifier = Modifier.fillMaxSize().padding(horizontal = 2.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Top,
                                    ) {
                                        Text(
                                            text = "$day",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(top = 3.dp),
                                        )
                                        Spacer(modifier = Modifier.height(1.dp))
                                        // 最多两行：第一行永远从同一位置开始写，
                                        // 只有一边有金额时也只占第一行，视觉上不会错位
                                        val lines = buildList {
                                            if (expense > 0) add("-${shortMoney(expense)}" to extra.expense)
                                            if (income > 0) add("+${shortMoney(income)}" to extra.income)
                                        }
                                        repeat(2) { index ->
                                            val line = lines.getOrNull(index)
                                            Text(
                                                text = line?.first ?: "",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                color = line?.second ?: extra.expense,
                                                maxLines = 1,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    repeat(7 - week.size) { Spacer(modifier = Modifier.weight(1f)) }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${month + 1}月${selectedDay}日",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            val dayExpense = dayTotals.first[selectedDay] ?: 0.0
            val dayIncome = dayTotals.second[selectedDay] ?: 0.0
            Text(
                text = "支出 ¥${formatMoney(dayExpense)} · 收入 ¥${formatMoney(dayIncome)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        SectionCard {
            if (selectedRecords.isEmpty()) {
                Text(
                    text = "这一天还没有记录",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                selectedRecords.forEach { record ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (record.type == EntryType.Income) {
                                extra.income.copy(alpha = 0.12f)
                            } else {
                                extra.expense.copy(alpha = 0.10f)
                            },
                            modifier = Modifier.size(32.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = record.category.icon,
                                    contentDescription = null,
                                    tint = if (record.type == EntryType.Income) extra.income else extra.expense,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                        Spacer(modifier = Modifier.size(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = record.category.label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            val note = record.note.ifBlank { record.fromAccount.name }
                            Text(
                                text = note,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                            )
                        }
                        Text(
                            text = (if (record.type == EntryType.Income) "+" else "-") +
                                "¥${formatMoney(record.amount)}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (record.type == EntryType.Income) extra.income else extra.expense,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

/** 日历格子里的短金额：超过 1 万显示为 「1.2万」。 */
private fun shortMoney(value: Double): String = when {
    value >= 10000 -> String.format(Locale.CHINA, "%.1f万", value / 10000)
    value >= 100 -> String.format(Locale.CHINA, "%.0f", value)
    else -> String.format(Locale.CHINA, "%.0f", value)
}
