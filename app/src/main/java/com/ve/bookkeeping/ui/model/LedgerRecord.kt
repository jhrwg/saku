package com.ve.bookkeeping.ui.model

import java.util.Calendar

/**
 * 一条已保存的账目记录。由 `LedgerStore` 以 JSON 形式持久化到应用私有目录。
 *
 * [occurredAt] 是把「日期 + 时间」合并后的本地时间戳，首页按它排序和分月统计。
 */
data class LedgerRecord(
    val id: Long,
    val bookName: String,
    val type: EntryType,
    val amount: Double,
    val category: EntryCategory,
    val fromAccount: AccountOption,
    val toAccount: AccountOption? = null,
    val fee: Double = 0.0,
    val note: String = "",
    val currency: CurrencyOption,
    val occurredAt: Long,
    /** 退款 / 报销标记：带标记的记录不计入收支统计。 */
    val flag: RecordFlag = RecordFlag.None,
    /** 原价（未优惠前），等于 amount 表示没有优惠。 */
    val originalAmount: Double = amount,
    /** 优惠 / 折扣金额。 */
    val discount: Double = 0.0,
    /** 多人分摊时的参与人。 */
    val participants: List<Participant> = emptyList(),
    /** 我的份额（分摊时用于统计），默认等于实付金额。 */
    val myShare: Double = amount,
    /** 附件文件名（存在 filesDir/attachments 下）。 */
    val attachments: List<String> = emptyList(),
    /** 退款关联：指向被退款的原消费记录 id。 */
    val refundOfId: Long? = null,
    /** 分期：第几期（1 起，0 表示不是分期）。 */
    val installmentIndex: Int = 0,
    /** 标签（自由文本），用于多维统计与筛选。 */
    val tags: List<String> = emptyList(),
    /** 分期：总期数（0 表示不是分期）。 */
    val installmentCount: Int = 0,
) {
    /** 统计口径：分摊时按我的份额，否则按实付。 */
    val statAmount: Double get() = if (participants.isEmpty()) amount else myShare

    /** 是否有优惠。 */
    val hasDiscount: Boolean get() = discount > 0.01
}

/** 分摊参与人。 */
data class Participant(
    val name: String,
    val share: Double,
)

/** 统计页的期间口径。 */
enum class StatsRange(val label: String) {
    Last7Days("日常"),
    Month("月统计"),
    Year("年统计"),
}

/** 分类聚合结果（只统计支出）。 */
data class CategoryStat(
    val category: EntryCategory,
    val amount: Double,
    val share: Float,
)

/** 标签聚合结果（只统计支出，一条记录多个标签时每个标签各记一次）。 */
data class TagStat(
    val tag: String,
    val amount: Double,
    val count: Int,
    val share: Float,
)

/** 某个期间的收支汇总。 */
data class PeriodStats(
    val income: Double,
    val expense: Double,
    val balance: Double,
    val incomeCount: Int,
    val expenseCount: Int,
    val categories: List<CategoryStat>,
    val bars: List<Float>,
    val barLabels: List<String>,
    /** 收入构成（按分类）。 */
    val incomeCategories: List<CategoryStat> = emptyList(),
    /** 标签聚合（按金额降序）。 */
    val tags: List<TagStat> = emptyList(),
    /** 按星期几的支出（周一 → 周日）。 */
    val weekdayBars: List<Float> = emptyList(),
)

/** 退款关联：统计时按「原消费 id → 已退金额」抵扣。 */
fun refundNets(records: List<LedgerRecord>): Map<Long, Double> =
    records
        .filter { it.refundOfId != null }
        .groupBy { it.refundOfId!! }
        .mapValues { (_, list) -> list.sumOf { it.amount } }

/** 记录的净统计金额（已扣掉关联的退款）。 */
fun LedgerRecord.netStatAmount(refunds: Map<Long, Double>): Double =
    (statAmount - (refunds[id] ?: 0.0)).coerceAtLeast(0.0)

/**
 * 计算某个期间的收支、分类聚合与柱状序列。
 * 转账同样不计入收支合计，只出现在「账户余额」里。
 */
fun statsFor(
    records: List<LedgerRecord>,
    range: StatsRange,
    now: Calendar = Calendar.getInstance(),
): PeriodStats {
    val rangeStart: Long
    val rangeEnd: Long
    when (range) {
        StatsRange.Last7Days -> {
            val todayStart = startOfDay(now.clone() as Calendar)
            rangeStart = todayStart - 6 * DAY_MILLIS
            rangeEnd = todayStart + DAY_MILLIS
        }

        StatsRange.Month -> {
            rangeStart = startOfDay((now.clone() as Calendar).apply {
                set(Calendar.DAY_OF_MONTH, 1)
            })
            rangeEnd = startOfDay((now.clone() as Calendar).apply {
                set(Calendar.DAY_OF_MONTH, 1)
                add(Calendar.MONTH, 1)
            })
        }

        StatsRange.Year -> {
            rangeStart = startOfDay((now.clone() as Calendar).apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.MONTH, Calendar.JANUARY)
            })
            rangeEnd = startOfDay((now.clone() as Calendar).apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.MONTH, Calendar.JANUARY)
                add(Calendar.YEAR, 1)
            })
        }
    }

    val inRange = records.filter { it.occurredAt >= rangeStart && it.occurredAt < rangeEnd }
    // 退款 / 报销不计入收支统计（钱确实动了，但不算「花掉」）
    val statRecords = inRange.filter { !it.flag.isFlagged }
    val expenseRecords = statRecords.filter { it.type == EntryType.Expense }
    val incomeRecords = statRecords.filter { it.type == EntryType.Income }

    val refundMap = refundNets(inRange)
    val expense = expenseRecords.sumOf { it.netStatAmount(refundMap) }
    val income = incomeRecords.sumOf { it.netStatAmount(refundMap) }

    // 分类聚合：按金额降序，占比基于总支出
    val grouped = expenseRecords
        .groupBy { it.category.id }
        .map { (_, list) ->
            val category = list.first().category
            val amount = list.sumOf { it.netStatAmount(refundMap) }
            CategoryStat(
                category = category,
                amount = amount,
                share = if (expense > 0) (amount / expense).toFloat() else 0f,
            )
        }
        .sortedByDescending { it.amount }
        .take(6)

    val (bars, labels) = when (range) {
        StatsRange.Last7Days -> {
            val values = (6 downTo 0).map { offset ->
                val dayStart = startOfDay(now.clone() as Calendar) - offset * DAY_MILLIS
                expenseRecords
                    .filter { it.occurredAt >= dayStart && it.occurredAt < dayStart + DAY_MILLIS }
                    .sumOf { it.netStatAmount(refundMap) }
                    .toFloat()
            }
            values to (6 downTo 0).map { offset -> dayLabel(now, offset) }
        }

        StatsRange.Month -> {
            val dayCount = now.getActualMaximum(Calendar.DAY_OF_MONTH)
            val values = (1..dayCount).map { day ->
                expenseRecords
                    .filter {
                        val c = Calendar.getInstance().apply { timeInMillis = it.occurredAt }
                        c.get(Calendar.DAY_OF_MONTH) == day
                    }
                    .sumOf { it.netStatAmount(refundMap) }
                    .toFloat()
            }
            values to listOf("1", "8", "15", "22", "$dayCount")
        }

        StatsRange.Year -> {
            val values = (0..11).map { month ->
                expenseRecords
                    .filter {
                        val c = Calendar.getInstance().apply { timeInMillis = it.occurredAt }
                        c.get(Calendar.MONTH) == month
                    }
                    .sumOf { it.netStatAmount(refundMap) }
                    .toFloat()
            }
            values to listOf("1月", "4月", "7月", "10月", "12月")
        }
    }

    val incomeGrouped = incomeRecords
        .groupBy { it.category.id }
        .map { (_, list) ->
            val category = list.first().category
            val amount = list.sumOf { it.amount }
            CategoryStat(
                category = category,
                amount = amount,
                share = if (income > 0) (amount / income).toFloat() else 0f,
            )
        }
        .sortedByDescending { it.amount }
        .take(6)

    return PeriodStats(
        income = income,
        expense = expense,
        balance = income - expense,
        incomeCount = incomeRecords.size,
        expenseCount = expenseRecords.size,
        categories = grouped,
        bars = bars,
        barLabels = labels,
        incomeCategories = incomeGrouped,
        tags = expenseRecords
            .filter { it.tags.isNotEmpty() }
            .flatMap { record ->
                val amount = record.netStatAmount(refundMap)
                record.tags.map { tag -> tag to amount }
            }
            .groupBy({ it.first }, { it.second })
            .map { (tag, amounts) ->
                TagStat(
                    tag = tag,
                    amount = amounts.sum(),
                    count = amounts.size,
                    share = if (expense > 0) (amounts.sum() / expense).toFloat() else 0f,
                )
            }
            .sortedByDescending { it.amount }
            .take(8),
        weekdayBars = (0..6).map { index ->
            expenseRecords
                .filter { record ->
                    val c = Calendar.getInstance().apply { timeInMillis = record.occurredAt }
                    ((c.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7) == index
                }
                .sumOf { it.netStatAmount(refundMap) }
                .toFloat()
        },
    )
}

/** 账户余额 = 初始余额 + 该账户所有收支/转账的净影响。 */
fun accountBalances(
    records: List<LedgerRecord>,
    base: List<AccountOption>,
): List<AccountOption> {
    val deltas = mutableMapOf<String, Double>()
    records.forEach { record ->
        when (record.type) {
            EntryType.Expense ->
                deltas.merge(record.fromAccount.id, -record.amount, Double::plus)

            EntryType.Income ->
                deltas.merge(record.fromAccount.id, record.amount, Double::plus)

            EntryType.Transfer -> {
                deltas.merge(record.fromAccount.id, -(record.amount + record.fee), Double::plus)
                record.toAccount?.let { deltas.merge(it.id, record.amount, Double::plus) }
            }
        }
    }
    return base.map { account ->
        account.copy(balance = account.balance + (deltas[account.id] ?: 0.0))
    }
}

/** 有记账的自然日数量，用于「我的」页展示坚持天数。 */
fun activeDayCount(records: List<LedgerRecord>): Int =
    records.map { startOfDay(Calendar.getInstance().apply { timeInMillis = it.occurredAt }) }
        .distinct()
        .size


/** 首页概览卡的统计结果。 */
data class HomeSummary(
    val monthExpense: Double,
    val monthIncome: Double,
    val monthBalance: Double,
    val dailyAverage: Double,
    val todayExpense: Double,
    val recordCount: Int,
    val trend: List<Float>,
)

/**
 * 汇总首页数据：本月收支、日均、今日支出，以及最近 10 天的支出趋势。
 * 转账不计入收支统计（只影响账户余额），与常见记账口径一致。
 */
fun summarizeRecords(
    records: List<LedgerRecord>,
    now: Calendar = Calendar.getInstance(),
): HomeSummary {
    val monthStart = startOfDay(
        (now.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) },
    )
    val monthEnd = (now.clone() as Calendar).apply {
        set(Calendar.DAY_OF_MONTH, 1)
        add(Calendar.MONTH, 1)
    }.let { startOfDay(it) }

    val monthRecords = records.filter { it.occurredAt >= monthStart && it.occurredAt < monthEnd }
    val statRecords = monthRecords.filter { !it.flag.isFlagged }
    val refundMap = refundNets(records)

    val expense = statRecords.filter { it.type == EntryType.Expense }.sumOf { it.netStatAmount(refundMap) }
    val income = statRecords.filter { it.type == EntryType.Income }.sumOf { it.netStatAmount(refundMap) }

    val todayStart = startOfDay(now.clone() as Calendar)
    val todayEnd = todayStart + DAY_MILLIS
    val todayExpense = statRecords
        .filter { it.type == EntryType.Expense && it.occurredAt in todayStart until todayEnd }
        .sumOf { it.netStatAmount(refundMap) }

    val elapsedDays = now.get(Calendar.DAY_OF_MONTH).coerceAtLeast(1)

    // 最近 10 天每日支出，用于首页折线
    val trend = (9 downTo 0).map { offset ->
        val dayStart = todayStart - offset * DAY_MILLIS
        val dayEnd = dayStart + DAY_MILLIS
        records
            .filter {
                it.type == EntryType.Expense &&
                    !it.flag.isFlagged &&
                    it.occurredAt in dayStart until dayEnd
            }
            .sumOf { it.netStatAmount(refundMap) }
            .toFloat()
    }

    return HomeSummary(
        monthExpense = expense,
        monthIncome = income,
        monthBalance = income - expense,
        dailyAverage = expense / elapsedDays,
        todayExpense = todayExpense,
        recordCount = monthRecords.size,
        trend = trend,
    )
}

private const val DAY_MILLIS = 24 * 60 * 60 * 1000L

private fun startOfDay(calendar: Calendar): Long = (calendar.clone() as Calendar).apply {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis

private fun dayLabel(now: Calendar, daysAgo: Int): String {
    val calendar = (now.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, -daysAgo) }
    return "${calendar.get(Calendar.MONTH) + 1}.${calendar.get(Calendar.DAY_OF_MONTH)}"
}
