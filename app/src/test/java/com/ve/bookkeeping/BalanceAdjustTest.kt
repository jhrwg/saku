package com.ve.bookkeeping

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalOffer
import com.ve.bookkeeping.ui.DemoData
import com.ve.bookkeeping.ui.model.AccountOption
import com.ve.bookkeeping.ui.model.EntryCategory
import com.ve.bookkeeping.ui.model.EntryType
import com.ve.bookkeeping.ui.model.LedgerRecord
import com.ve.bookkeeping.ui.model.accountBalances
import org.junit.Assert.assertEquals
import org.junit.Test

/** 资产页余额口径：初始余额 + 账目，且尊重「余额起始时间」。 */
class BalanceAdjustTest {

    private val account = AccountOption(
        id = "acc1",
        name = "支付宝",
        balance = 1000.0,
        icon = Icons.Rounded.LocalOffer,
    )

    private val category = EntryCategory("exp_food", "饮食", Icons.Rounded.LocalOffer)

    private fun record(id: Long, type: EntryType, amount: Double, at: Long) = LedgerRecord(
        id = id,
        bookName = "日常账本",
        type = type,
        amount = amount,
        category = category,
        fromAccount = account,
        currency = DemoData.currencies.first(),
        occurredAt = at,
    )

    @Test
    fun `初始余额加账目等于当前余额`() {
        val records = listOf(
            record(1, EntryType.Expense, 120.0, 1_000L),
            record(2, EntryType.Income, 500.0, 2_000L),
        )
        val balance = accountBalances(records, listOf(account)).first().balance
        assertEquals(1380.0, balance, 0.001)
    }

    @Test
    fun `更改当前余额后更早的账目不再影响余额`() {
        val records = listOf(
            record(1, EntryType.Expense, 120.0, 1_000L),
            record(2, EntryType.Income, 500.0, 2_000L),
        )
        // 把起始时间设为 1500：只统计 2000 那条
        val reset = account.copy(balance = 900.0, balanceStartAt = 1_500L)
        val balance = accountBalances(records, listOf(reset)).first().balance
        assertEquals(1400.0, balance, 0.001)
    }

    @Test
    fun `设置初始余额不影响流水`() {
        val records = listOf(record(1, EntryType.Expense, 120.0, 1_000L))
        val changed = account.copy(balance = 5000.0)
        val balance = accountBalances(records, listOf(changed)).first().balance
        assertEquals(4880.0, balance, 0.001)
    }

    @Test
    fun `转账按金额加手续费扣减`() {
        val target = AccountOption(id = "acc2", name = "工资卡", balance = 0.0, icon = Icons.Rounded.LocalOffer)
        val transfer = LedgerRecord(
            id = 9,
            bookName = "日常账本",
            type = EntryType.Transfer,
            amount = 200.0,
            category = category,
            fromAccount = account,
            toAccount = target,
            fee = 2.0,
            currency = DemoData.currencies.first(),
            occurredAt = 5_000L,
        )
        val balances = accountBalances(listOf(transfer), listOf(account, target))
        assertEquals(798.0, balances.first { it.id == "acc1" }.balance, 0.001)
        assertEquals(200.0, balances.first { it.id == "acc2" }.balance, 0.001)
    }
}
