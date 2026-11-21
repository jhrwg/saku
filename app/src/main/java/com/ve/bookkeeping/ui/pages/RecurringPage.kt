@file:OptIn(ExperimentalMaterial3Api::class)

package com.ve.bookkeeping.ui.pages

import com.ve.bookkeeping.ui.components.AppSheet

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ve.bookkeeping.ui.components.SectionCard
import com.ve.bookkeeping.ui.components.formatMoney
import com.ve.bookkeeping.ui.model.AccountOption
import com.ve.bookkeeping.ui.model.EntryCategory
import com.ve.bookkeeping.ui.model.EntryType
import com.ve.bookkeeping.ui.model.RecurringFrequency
import com.ve.bookkeeping.ui.model.RecurringRule
import com.ve.bookkeeping.ui.model.firstDueDate

/**
 * 周期记账：列出规则，可开关/删除，也可以新建。
 * 到期后由 AppRoot 在启动时补生成账目（关掉几天再打开也不会漏）。
 */
@Composable
fun RecurringPage(
    rules: List<RecurringRule>,
    accounts: List<AccountOption>,
    expenseCategories: List<EntryCategory>,
    incomeCategories: List<EntryCategory>,
    onAdd: (RecurringRule) -> Unit,
    onToggle: (RecurringRule) -> Unit,
    onDelete: (RecurringRule) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onBack)
    var showAdd by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "周期记账",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "到期后自动生成账目；启动 App 时会把错过的日期补上。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(14.dp))
        if (rules.isEmpty()) {
            SectionCard {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 26.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Repeat,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(30.dp),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "还没有周期记账",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "比如房租、工资、订阅费，可以在这里自动记",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        } else {
            rules.forEach { rule ->
                RuleCard(
                    rule = rule,
                    onToggle = { onToggle(rule) },
                    onDelete = { onDelete(rule) },
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { showAdd = true },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(14.dp),
        ) {
            Text(text = "新建周期记账")
        }
        Spacer(modifier = Modifier.height(40.dp))
    }

    if (showAdd) {
        AddRecurringSheet(
            accounts = accounts,
            expenseCategories = expenseCategories,
            incomeCategories = incomeCategories,
            onDismiss = { showAdd = false },
            onConfirm = { rule ->
                onAdd(rule)
                showAdd = false
            },
        )
    }
}

@Composable
private fun RuleCard(
    rule: RecurringRule,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
) {
    val extra = com.ve.bookkeeping.ui.theme.LocalAppExtraColors.current
    SectionCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rule.categoryLabel,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "${rule.scheduleText} · ${rule.accountName}" +
                        if (rule.note.isNotBlank()) " · ${rule.note}" else "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = (if (rule.type == EntryType.Income) "+" else "-") + "¥${formatMoney(rule.amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (rule.type == EntryType.Income) extra.income else extra.expense,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (rule.enabled) "已开启" else "已暂停",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Switch(checked = rule.enabled, onCheckedChange = { onToggle() })
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun AddRecurringSheet(
    accounts: List<AccountOption>,
    expenseCategories: List<EntryCategory>,
    incomeCategories: List<EntryCategory>,
    onDismiss: () -> Unit,
    onConfirm: (RecurringRule) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var typeOrdinal by rememberSaveable { mutableIntStateOf(0) }
    val type = if (typeOrdinal == 0) EntryType.Expense else EntryType.Income
    val categories = if (type == EntryType.Expense) expenseCategories else incomeCategories

    var amountText by rememberSaveable { mutableStateOf("") }
    var categoryId by rememberSaveable { mutableStateOf("") }
    var accountId by rememberSaveable { mutableStateOf(accounts.firstOrNull()?.id ?: "") }
    var frequencyOrdinal by rememberSaveable { mutableIntStateOf(2) }
    var dayText by rememberSaveable { mutableStateOf("1") }
    var note by rememberSaveable { mutableStateOf("") }

    val frequency = RecurringFrequency.entries[frequencyOrdinal]
    val amount = amountText.toDoubleOrNull() ?: 0.0
    val category = categories.firstOrNull { it.id == categoryId } ?: categories.firstOrNull()
    val account = accounts.firstOrNull { it.id == accountId } ?: accounts.firstOrNull()
    val canSave = amount > 0 && category != null && account != null

    AppSheet(onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                                                .padding(bottom = 4.dp),
        ) {
            Text(
                text = "新建周期记账",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = typeOrdinal == 0,
                    onClick = { typeOrdinal = 0; categoryId = "" },
                    label = { Text("支出") },
                )
                FilterChip(
                    selected = typeOrdinal == 1,
                    onClick = { typeOrdinal = 1; categoryId = "" },
                    label = { Text("收入") },
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = amountText,
                onValueChange = { input ->
                    amountText = input.filter { it.isDigit() || it == '.' }.take(10)
                },
                label = { Text("金额") },
                prefix = { Text("¥ ") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "分类",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(categories) { item ->
                    FilterChip(
                        selected = item.id == (category?.id ?: ""),
                        onClick = { categoryId = item.id },
                        label = { Text(item.label) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "账户",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (accounts.isEmpty()) {
                Text(
                    text = "还没有账户，先去资产页添加",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(accounts) { item ->
                        FilterChip(
                            selected = item.id == account?.id,
                            onClick = { accountId = item.id },
                            label = { Text(item.name) },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "频率",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                RecurringFrequency.entries.forEachIndexed { index, item ->
                    FilterChip(
                        selected = frequencyOrdinal == index,
                        onClick = { frequencyOrdinal = index },
                        label = { Text(item.label) },
                    )
                }
            }

            if (frequency == RecurringFrequency.Monthly) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = dayText,
                        onValueChange = { dayText = it.filter { ch -> ch.isDigit() }.take(2) },
                        label = { Text("每月几号") },
                        singleLine = true,
                        modifier = Modifier.width(140.dp),
                        shape = RoundedCornerShape(14.dp),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "1 ~ 28 比较稳",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it.take(30) },
                label = { Text("备注（可选）") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val day = dayText.toIntOrNull()?.coerceIn(1, 28) ?: 1
                    onConfirm(
                        RecurringRule(
                            id = System.currentTimeMillis(),
                            type = type,
                            amount = amount,
                            categoryId = category!!.id,
                            categoryLabel = category.label,
                            accountId = account!!.id,
                            accountName = account.name,
                            note = note.trim(),
                            frequency = frequency,
                            dayOfMonth = day,
                            nextDueAt = firstDueDate(frequency, day, System.currentTimeMillis()),
                        ),
                    )
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text(text = "保存")
            }
        }
    }
}

@Composable
private fun Unused() {
    // 保留一个占位，避免预览/未来扩展时找不到入口
    Box(modifier = Modifier.fillMaxWidth())
}

