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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ve.bookkeeping.ui.DemoData
import com.ve.bookkeeping.ui.components.AccountRow
import com.ve.bookkeeping.ui.components.PageHeader
import com.ve.bookkeeping.ui.components.SectionCard
import com.ve.bookkeeping.ui.components.UsageBar
import com.ve.bookkeeping.ui.components.formatMoney
import com.ve.bookkeeping.ui.model.AccountOption
import com.ve.bookkeeping.ui.model.AccountType
import com.ve.bookkeeping.ui.model.LedgerRecord
import com.ve.bookkeeping.ui.model.accountBalances
import com.ve.bookkeeping.ui.theme.LocalAppExtraColors
import com.ve.bookkeeping.ui.theme.ZheErGenBookkeepingTheme
import androidx.compose.material3.Button
import com.ve.bookkeeping.ui.model.BillSummary
import com.ve.bookkeeping.ui.model.creditBills
import androidx.compose.foundation.shape.RoundedCornerShape
import com.ve.bookkeeping.ui.moneyText

@Composable
fun AssetsPage(
    records: List<LedgerRecord>,
    accounts: List<AccountOption>,
    defaultAccountId: String?,
    onAccountClick: (AccountOption) -> Unit,
    proEnabled: Boolean = true,
    onNeedPro: () -> Unit = {},
    debts: List<com.ve.bookkeeping.ui.model.Debt> = emptyList(),
    onOpenDebts: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    // 初始余额 + 所有记账的影响 = 当前余额；负数即负债
    val computed = accountBalances(records, accounts)
    val groups = AccountType.entries.mapNotNull { type ->
        val list = computed.filter { it.type == type }
        if (list.isEmpty()) null else type to list
    }
    // 多币种：按内置参考汇率折算成人民币后再汇总
    val rates = DemoData.currencies.associate {
        it.code to com.ve.bookkeeping.data.ExchangeRates.rateToCny(it.code, it.rateToCny)
    }
    fun inCny(account: AccountOption): Double =
        account.balance * (rates[account.currencyCode] ?: 1.0)

    val allBalances = computed.map { inCny(it) }
    // 借入借出：算进净资产（借出=应收资产，借入=应付负债），只算未结清的
    val receivable = debts
        .filter { !it.settled && it.type == com.ve.bookkeeping.ui.model.DebtType.Lend }
        .sumOf { it.amount }
    val payable = debts
        .filter { !it.settled && it.type == com.ve.bookkeeping.ui.model.DebtType.Borrow }
        .sumOf { it.amount }
    val totalAssets = allBalances.filter { it > 0 }.sum() + receivable
    val totalDebt = (-allBalances.filter { it < 0 }.sum()).let { if (it == 0.0) 0.0 else it } + payable
    val netAssets = totalAssets - totalDebt
    var amountsVisible by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        PageHeader(title = "资产", subtitle = "总览与账户")
        NetWorthCard(
            netAssets = netAssets,
            totalAssets = totalAssets,
            totalDebt = totalDebt,
            visible = amountsVisible,
            onToggle = { amountsVisible = !amountsVisible },
        )
        Spacer(modifier = Modifier.height(14.dp))
        if (receivable > 0 || payable > 0) {
            DebtSummaryCard(
                receivable = receivable,
                payable = payable,
                visible = amountsVisible,
                onOpenDebts = onOpenDebts,
            )
            Spacer(modifier = Modifier.height(14.dp))
        }
        val bills = creditBills(accounts, records)
        if (bills.isNotEmpty()) {
            BillCard(
                bills = bills,
                proEnabled = proEnabled,
                onNeedPro = onNeedPro,
            )
            Spacer(modifier = Modifier.height(14.dp))
        }
        AccountsCard(
            groups = groups,
            visible = amountsVisible,
            defaultAccountId = defaultAccountId,
            onAccountClick = onAccountClick,
        )
        Spacer(modifier = Modifier.height(96.dp))
    }
}

@Composable
private fun DebtSummaryCard(
    receivable: Double,
    payable: Double,
    visible: Boolean,
    onOpenDebts: () -> Unit,
) {
    fun money(value: Double): String = if (visible) "¥${moneyText(value)}" else "¥ ••••"

    SectionCard(
        title = "借入借出",
        trailing = {
            Text(
                text = "管理",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable { onOpenDebts() }
                    .padding(horizontal = 4.dp, vertical = 2.dp),
            )
        },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "应收（借出未结清）",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "计入总资产",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = money(receivable),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = LocalAppExtraColors.current.income,
            )
        }
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 4.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.14f),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "应付（借入未结清）",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "计入总负债",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = money(payable),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = LocalAppExtraColors.current.expense,
            )
        }
    }
}

@Composable
private fun NetWorthCard(
    netAssets: Double,
    totalAssets: Double,
    totalDebt: Double,
    visible: Boolean,
    onToggle: () -> Unit,
) {
    val extra = LocalAppExtraColors.current
    val debtRatio = if (totalAssets > 0) (totalDebt / totalAssets).toFloat().coerceIn(0f, 1f) else 0f

    fun money(value: Double): String = if (visible) "¥${moneyText(value)}" else "¥ ••••"

    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "净资产",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = Icons.Rounded.Visibility,
                    contentDescription = if (visible) "隐藏金额" else "显示金额",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Text(
            text = money(netAssets),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "总资产",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = money(totalAssets),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "总负债",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = money(totalDebt),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = extra.expense,
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        UsageBar(
            fraction = debtRatio,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            progressColor = extra.expense,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "负债率 ${(debtRatio * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun BillCard(
    bills: List<BillSummary>,
    proEnabled: Boolean,
    onNeedPro: () -> Unit,
) {
    val extra = LocalAppExtraColors.current
    SectionCard(title = "信用卡账单") {
        if (!proEnabled) {
            Text(
                text = "账单管理是 Pro 权益：本期应还、最低还款、还款日提醒。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onNeedPro, shape = RoundedCornerShape(14.dp)) {
                Text(text = "开通 Pro 查看")
            }
            return@SectionCard
        }

        bills.forEach { bill ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = bill.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "最低还款 ¥${moneyText(bill.minPayment)} · 每月 ${bill.repayDay} 日",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "¥${moneyText(bill.due)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = extra.expense,
                    )
                    Text(
                        text = when {
                            bill.daysUntilRepay == 0 -> "今天还款"
                            bill.daysUntilRepay > 0 -> "${bill.daysUntilRepay} 天后还款"
                            else -> "已过还款日"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (bill.daysUntilRepay in 0..3) {
                            extra.expense
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountsCard(
    groups: List<Pair<AccountType, List<AccountOption>>>,
    visible: Boolean,
    defaultAccountId: String?,
    onAccountClick: (AccountOption) -> Unit,
) {
    val extra = LocalAppExtraColors.current

    SectionCard(
        title = "账户",
        trailing = {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Rounded.FilterList,
                    contentDescription = "筛选账户",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        },
    ) {
        if (groups.isEmpty()) {
            Text(
                text = "还没有账户，点右下角「+」添加第一个账户",
                modifier = Modifier.padding(vertical = 20.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        groups.forEachIndexed { groupIndex, (type, accounts) ->
            if (groupIndex > 0) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 6.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.16f),
                )
            }
            GroupHeader(type = type, accounts = accounts, visible = visible)
            accounts.forEach { account ->
                AccountRow(
                    accountId = account.id,
                    icon = account.icon,
                    tint = if (account.balance < 0) {
                        extra.expense
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    name = account.name,
                    subtitle = accountSubtitle(account),
                    amount = if (visible) "¥${moneyText(account.balance)}" else "¥ ••••",
                    amountColor = if (account.balance < 0) {
                        extra.expense
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    limit = account.creditLimit,
                    balance = account.balance,
                    isDefault = account.id == defaultAccountId,
                    onClick = { onAccountClick(account) },
                )
            }
        }
    }
}

private fun accountSubtitle(account: AccountOption): String = when (account.type) {
    AccountType.Credit -> buildString {
        account.billDay?.let { append("账单日 $it 日") }
        account.repayDay?.let {
            if (isNotEmpty()) append(" · ")
            append("还款日 $it 日")
        }
        if (isEmpty()) append(account.cardNote.ifBlank { "信用卡" })
    }

    AccountType.Fund -> account.stockName?.takeIf { it.isNotBlank() } ?: "基金账户"
    else -> account.cardNote.ifBlank { account.type.label }
}

@Composable
private fun GroupHeader(
    type: AccountType,
    accounts: List<AccountOption>,
    visible: Boolean,
) {
    val extra = LocalAppExtraColors.current
    val total = accounts.sumOf { it.balance }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = type.label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = if (visible) "¥${moneyText(total)}" else "¥ ••••",
            style = MaterialTheme.typography.labelMedium,
            color = if (total < 0) extra.expense else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun AssetsPagePreview() {
    ZheErGenBookkeepingTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            AssetsPage(
                records = DemoData.seedRecords(),
                accounts = DemoData.accounts,
                defaultAccountId = null,
                onAccountClick = {},
            )
        }
    }
}



