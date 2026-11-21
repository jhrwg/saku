package com.ve.bookkeeping

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Category
import com.ve.bookkeeping.ui.ensureUniqueIds
import com.ve.bookkeeping.ui.expandInstallments
import com.ve.bookkeeping.ui.model.AccountOption
import com.ve.bookkeeping.ui.model.CurrencyOption
import com.ve.bookkeeping.ui.model.EntryCategory
import com.ve.bookkeeping.ui.model.EntryType
import com.ve.bookkeeping.ui.model.LedgerRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** 记录 id 唯一性：撞 id 会让 LazyColumn 直接崩（Key "trash-xxx" was already used）。 */
class RecordIdsTest {

    private val account = AccountOption("acc", "现金", 0.0, Icons.Rounded.Category)
    private val cny = CurrencyOption("CNY", "人民币", "¥")

    private fun record(id: Long, installment: Int = 0) = LedgerRecord(
        id = id,
        bookName = "日常账本",
        type = EntryType.Expense,
        amount = 120.0,
        category = EntryCategory("food", "饮食", Icons.Rounded.Category),
        fromAccount = account,
        note = "测试",
        currency = cny,
        occurredAt = 1_700_000_000_000L,
        installmentCount = installment,
        installmentIndex = if (installment > 1) 1 else 0,
    )

    @Test
    fun duplicateIdsAreReassigned() {
        val list = listOf(record(100), record(100), record(101), record(100))
        val fixed = ensureUniqueIds(list)
        assertEquals(list.size, fixed.size)
        assertEquals(fixed.size, fixed.map { it.id }.distinct().size)
        // 原有唯一 id 保持不动
        assertEquals(100L, fixed[0].id)
        assertEquals(101L, fixed[2].id)
    }

    @Test
    fun uniqueListIsUntouched() {
        val list = listOf(record(1), record(2), record(3))
        assertEquals(list, ensureUniqueIds(list))
    }

    @Test
    fun installmentsGetDistinctIdsAboveBase() {
        val parts = expandInstallments(record(5_000, installment = 6), baseId = 9_000)
        assertEquals(6, parts.size)
        assertEquals(9_000L, parts.first().id)
        assertEquals(9_005L, parts.last().id)
        assertEquals(6, parts.map { it.id }.distinct().size)
        // 合计金额不被拆丢
        assertTrue(kotlin.math.abs(parts.sumOf { it.amount } - 120.0) < 0.001)
    }

    @Test
    fun newRecordIdStaysAboveExistingMax() {
        // 模拟 AppRoot 的分配规则：max + 1 起，分期依次 +i
        val existing = listOf(record(10), record(12))
        val baseId = existing.maxOf { it.id } + 1
        val parts = expandInstallments(record(0, installment = 3), baseId = baseId)
        assertTrue(parts.all { it.id > 12L })
        assertNotEquals(parts[0].id, existing[0].id)
    }
}
