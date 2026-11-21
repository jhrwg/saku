package com.ve.bookkeeping.data

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.Wallet
import com.ve.bookkeeping.ui.DemoData
import com.ve.bookkeeping.ui.model.AccountOption
import com.ve.bookkeeping.ui.model.AccountType
import com.ve.bookkeeping.ui.model.BudgetConfig
import com.ve.bookkeeping.ui.model.CurrencyOption
import com.ve.bookkeeping.ui.model.EntryCategory
import com.ve.bookkeeping.ui.model.EntryType
import com.ve.bookkeeping.ui.model.LedgerRecord
import com.ve.bookkeeping.ui.model.Participant
import com.ve.bookkeeping.ui.model.RecurringFrequency
import com.ve.bookkeeping.ui.model.RecurringRule
import com.ve.bookkeeping.ui.model.RecordFlag
import com.ve.bookkeeping.ui.model.accountIconById
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import com.ve.bookkeeping.ui.model.LoanConfig

/** 当前数据结构版本。 */
const val SCHEMA_VERSION = 2

/** 一次完整快照：账目 + 账户 + 账本 + 自定义分类 + 周期记账规则 + 预算 + 当前账本。 */
data class LedgerSnapshot(
    val records: List<LedgerRecord>,
    val accounts: List<AccountOption>,
    val books: List<String>,
    val selectedBook: String,
    val customCategories: List<Pair<EntryType, EntryCategory>>,
    val recurringRules: List<RecurringRule> = emptyList(),
    val budget: BudgetConfig = BudgetConfig(),
    /** 数据格式版本：用于后续字段迁移（批次2 起生效）。 */
    val schemaVersion: Int = SCHEMA_VERSION,
    /** 回收站：删除的账目先放这里，可恢复。 */
    val trash: List<LedgerRecord> = emptyList(),
    /** 车贷 / 房贷。 */
    val loans: List<LoanConfig> = emptyList(),
    /** 借入 / 借出。 */
    val debts: List<com.ve.bookkeeping.ui.model.Debt> = emptyList(),
)

/**
 * 极简 JSON 持久化：把账目与账户写到应用私有目录的 `ledger.json`。
 *
 * 图标是 `ImageVector` 不能序列化，所以只存分类/账户的 id 或图标 id，读回时还原；
 * 自定义分类统一还原成「标签」图标。等接 Room 时替换这个类即可。
 */
class LedgerStore(context: Context) {

    private val file: File = File(context.filesDir, "ledger.json")
    private val prefs = context.getSharedPreferences("app", Context.MODE_PRIVATE)

    /**
     * 读取快照；如果本地还是旧版（含演示账本/演示账户/演示账目）的数据，
     * 直接丢弃并返回 null，让应用以「全新空账本」启动。
     *
     * 注意：只在「数据版本升级」这一个前提下清理，且不再因为偏好丢失就删文件——
     * 之前若 shared_prefs 被系统清掉，用户的账目会被误删。
     */
    fun loadOrReset(): LedgerSnapshot? {
        if (prefs.getInt(KEY_DATA_VERSION, 1) < DATA_VERSION) {
            prefs.edit().putInt(KEY_DATA_VERSION, DATA_VERSION).apply()
            // 老版本文件里全是演示数据；真实数据从 1.2.x 才有，这里不再删除，
            // 由下面的 load() 正常读取（读不出来还有 .bak 兜底）。
        }
        return load()
    }

    /** 当前数据版本：2 = 全新空账本（无演示账本/账户/账目）。 */
    private companion object {
        const val KEY_DATA_VERSION = "dataVersion"
        const val DATA_VERSION = 2
    }

    /** 读取快照；文件不存在（首次启动）返回 null，由调用方回落到种子数据。 */
    fun load(): LedgerSnapshot? {
        if (!file.exists()) return null
        val text = file.readText()
        val decoded = decode(text)
        if (decoded != null) return decoded

        // 主文件坏了：先留档，再尝试上一份好文件（save 时写入的 .bak）
        if (text.length > 2) {
            runCatching {
                file.parentFile?.let { dir -> File(dir, "ledger.broken.json").writeText(text) }
            }
        }
        val backup = File(file.parentFile, "${file.name}.bak")
        if (backup.exists()) {
            val fromBackup = decode(backup.readText())
            if (fromBackup != null) return fromBackup
        }
        return null
    }

    /** 从 JSON 文本解析快照；本地读盘与云端恢复共用。 */
    fun decode(text: String): LedgerSnapshot? {
        return runCatching {
            val root = JSONObject(text)

            val customCategories = buildList {
                val array = root.optJSONArray("customCategories") ?: JSONArray()
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    val type = EntryType.entries.firstOrNull { it.name == item.optString("type") }
                        ?: EntryType.Expense
                    add(
                        type to EntryCategory(
                            id = item.getString("id"),
                            label = item.getString("label"),
                            icon = Icons.Rounded.LocalOffer,
                            isCustom = true,
                            parentId = item.optString("parentId").ifBlank { null },
                        ),
                    )
                }
            }

            val accounts = buildList {
                val array = root.optJSONArray("accounts") ?: JSONArray()
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    add(
                        AccountOption(
                            id = item.getString("id"),
                            name = item.optString("name"),
                            balance = item.optDouble("balance", 0.0),
                            icon = accountIconById(item.optString("iconId")),
                            type = AccountType.entries.firstOrNull { it.name == item.optString("type") }
                                ?: AccountType.Network,
                            iconId = item.optString("iconId").ifBlank { "wallet" },
                            creditLimit = if (item.has("creditLimit")) item.optDouble("creditLimit") else null,
                            billDay = if (item.has("billDay")) item.optInt("billDay") else null,
                            repayDay = if (item.has("repayDay")) item.optInt("repayDay") else null,
                            stockName = item.optString("stockName").ifBlank { null },
                            cardNote = item.optString("cardNote"),
                            currencyCode = item.optString("currency").ifBlank { "CNY" },
                        ),
                    )
                }
            }.ifEmpty { emptyList() }

            val categoryById = (DemoData.expenseCategories +
                DemoData.incomeCategories +
                DemoData.transferCategories +
                customCategories.map { it.second })
                .associateBy { it.id }

            val accountById = accounts.associateBy { it.id }
            val currencyByCode = DemoData.currencies.associateBy { it.code }

            fun parseRecords(raw: JSONArray?): List<LedgerRecord> = buildList {
                val array = raw ?: JSONArray()
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    val categoryId = item.optString("categoryId")
                    val category = categoryById[categoryId] ?: EntryCategory(
                        id = categoryId.ifBlank { "unknown" },
                        label = item.optString("categoryLabel").ifBlank { "未分类" },
                        icon = Icons.Rounded.LocalOffer,
                    )
                    val fromAccount = accountById[item.optString("fromAccountId")]
                        ?: AccountOption(
                            id = item.optString("fromAccountId"),
                            name = item.optString("fromAccountName").ifBlank { "账户" },
                            balance = 0.0,
                            icon = Icons.Rounded.Wallet,
                        )
                    val toAccountId = item.optString("toAccountId").ifBlank { null }
                    val toAccount = toAccountId?.let { id ->
                        accountById[id] ?: AccountOption(
                            id = id,
                            name = item.optString("toAccountName").ifBlank { "账户" },
                            balance = 0.0,
                            icon = Icons.Rounded.Wallet,
                        )
                    }
                    val currency: CurrencyOption =
                        currencyByCode[item.optString("currency")] ?: DemoData.currencies.first()

                    add(
                        LedgerRecord(
                            id = item.getLong("id"),
                            bookName = item.optString("book"),
                            type = EntryType.entries.firstOrNull { it.name == item.optString("type") }
                                ?: EntryType.Expense,
                            amount = item.getDouble("amount"),
                            category = category,
                            fromAccount = fromAccount,
                            toAccount = toAccount,
                            fee = item.optDouble("fee", 0.0),
                            note = item.optString("note"),
                            currency = currency,
                            occurredAt = item.getLong("occurredAt"),
                            flag = RecordFlag.entries
                                .firstOrNull { it.name == item.optString("flag") }
                                ?: RecordFlag.None,
                            originalAmount = item.optDouble("originalAmount", item.optDouble("amount")),
                            discount = item.optDouble("discount", 0.0),
                            participants = item.optJSONArray("participants")?.let { array ->
                                (0 until array.length()).mapNotNull { i ->
                                    val p = array.optJSONObject(i) ?: return@mapNotNull null
                                    Participant(
                                        name = p.optString("name"),
                                        share = p.optDouble("share", 0.0),
                                    )
                                }
                            }.orEmpty(),
                            myShare = item.optDouble("myShare", item.optDouble("amount")),
                            attachments = item.optJSONArray("attachments")?.let { array ->
                                (0 until array.length()).mapNotNull { array.optString(it).ifBlank { null } }
                            }.orEmpty(),
                            refundOfId = item.optLong("refundOfId", 0L).takeIf { it > 0 },
                            installmentIndex = item.optInt("installmentIndex", 0),
                            installmentCount = item.optInt("installmentCount", 0),
            tags = item.optJSONArray("tags")?.let { array ->
                (0 until array.length()).mapNotNull { i -> array.optString(i).takeIf { it.isNotBlank() } }
            }.orEmpty(),
                        ),
                    )
                }
            }

            val records = parseRecords(root.optJSONArray("records"))

            val books = buildList {
                val array = root.optJSONArray("books") ?: JSONArray()
                for (index in 0 until array.length()) add(array.getString(index))
            }.ifEmpty { DemoData.ledgerBooks }

            val recurringRules = buildList {
                val array = root.optJSONArray("recurringRules") ?: JSONArray()
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    add(
                        RecurringRule(
                            id = item.optLong("id"),
                            type = EntryType.entries.firstOrNull { it.name == item.optString("type") }
                                ?: EntryType.Expense,
                            amount = item.optDouble("amount", 0.0),
                            categoryId = item.optString("categoryId"),
                            categoryLabel = item.optString("categoryLabel"),
                            accountId = item.optString("accountId"),
                            accountName = item.optString("accountName"),
                            note = item.optString("note"),
                            frequency = RecurringFrequency.entries
                                .firstOrNull { it.name == item.optString("frequency") }
                                ?: RecurringFrequency.Monthly,
                            dayOfMonth = item.optInt("dayOfMonth", 1),
                            nextDueAt = item.optLong("nextDueAt", 0L),
                            enabled = item.optBoolean("enabled", true),
                        ),
                    )
                }
            }

            val budget = root.optJSONObject("budget")?.let { obj ->
                val limits = mutableMapOf<String, Double>()
                obj.optJSONObject("categoryLimits")?.let { map ->
                    map.keys().forEach { key -> limits[key] = map.optDouble(key, 0.0) }
                }
                BudgetConfig(
                    monthlyLimit = obj.optDouble("monthlyLimit", 0.0),
                    categoryLimits = limits,
                    period = obj.optString("period", "Month"),
                    rollover = obj.optBoolean("rollover", false),
                    savingsGoal = obj.optDouble("savingsGoal", 0.0),
                )
            } ?: BudgetConfig()

            LedgerSnapshot(
                records = records,
                accounts = accounts,
                books = books,
                selectedBook = root.optString("selectedBook").ifBlank { books.first() },
                customCategories = customCategories,
                recurringRules = recurringRules,
                budget = budget,
                schemaVersion = root.optInt("schemaVersion", SCHEMA_VERSION),
                trash = parseRecords(root.optJSONArray("trash")),
                loans = root.optJSONArray("loans")?.let { array ->
                    (0 until array.length()).mapNotNull { i ->
                        val item = array.optJSONObject(i) ?: return@mapNotNull null
                        LoanConfig(
                            id = item.optLong("id"),
                            name = item.optString("name"),
                            principal = item.optDouble("principal", 0.0),
                            annualRate = item.optDouble("annualRate", 0.0),
                            months = item.optInt("months", 0),
                            startAt = item.optLong("startAt"),
                            paidCount = item.optInt("paidCount", 0),
                        )
                    }
                }.orEmpty(),
                debts = root.optJSONArray("debts")?.let { array ->
                    (0 until array.length()).mapNotNull { i ->
                        val item = array.optJSONObject(i) ?: return@mapNotNull null
                        com.ve.bookkeeping.ui.model.Debt(
                            id = item.optLong("id"),
                            type = runCatching {
                                com.ve.bookkeeping.ui.model.DebtType.valueOf(
                                    item.optString("type", "Borrow"),
                                )
                            }.getOrDefault(com.ve.bookkeeping.ui.model.DebtType.Borrow),
                            counterparty = item.optString("counterparty"),
                            amount = item.optDouble("amount", 0.0),
                            note = item.optString("note"),
                            createdAt = item.optLong("createdAt"),
                            settled = item.optBoolean("settled", false),
                        )
                    }
                }.orEmpty(),
            )
        }.onFailure {
            android.util.Log.e("LedgerStore", "decode failed: ${it.message}", it)
        }.getOrNull()
    }

    /** 写入快照；先留一份 .bak，再写临时文件并改名，避免写一半留下坏文件。 */
    fun save(snapshot: LedgerSnapshot) {
        runCatching {
            val text = encode(snapshot)
            val backup = File(file.parentFile, "${file.name}.bak")
            if (file.exists()) {
                runCatching { file.copyTo(backup, overwrite = true) }
            }
            val temp = File(file.parentFile, "${file.name}.tmp")
            temp.writeText(text)
            if (file.exists()) file.delete()
            temp.renameTo(file)
        }
    }

    /** 生成与 ledger.json 同构的 JSON 文本（备份上传用）。 */
    fun encode(snapshot: LedgerSnapshot): String = buildJson(snapshot).toString()

    private fun buildJson(snapshot: LedgerSnapshot): JSONObject {
        val root = JSONObject()
        root.put("schemaVersion", SCHEMA_VERSION)

            fun recordJson(record: LedgerRecord): JSONObject = JSONObject().apply {
                        put("id", record.id)
                        put("book", record.bookName)
                        put("type", record.type.name)
                        put("amount", record.amount)
                        put("categoryId", record.category.id)
                        put("categoryLabel", record.category.label)
                        put("fromAccountId", record.fromAccount.id)
                        put("fromAccountName", record.fromAccount.name)
                        record.toAccount?.let {
                            put("toAccountId", it.id)
                            put("toAccountName", it.name)
                        }
                        put("fee", record.fee)
                        put("note", record.note)
                        put("currency", record.currency.code)
                        put("occurredAt", record.occurredAt)
                        put("flag", record.flag.name)
                        put("originalAmount", record.originalAmount)
                        put("discount", record.discount)
                        put("myShare", record.myShare)
                        put("participants", JSONArray().apply {
                            record.participants.forEach { p ->
                                put(JSONObject().apply {
                                    put("name", p.name)
                                    put("share", p.share)
                                })
                            }
                        })
                        put("attachments", JSONArray().apply {
                            record.attachments.forEach { put(it) }
                        })
                        record.refundOfId?.let { put("refundOfId", it) }
                        put("installmentIndex", record.installmentIndex)
                        put("installmentCount", record.installmentCount)
            put("tags", JSONArray().apply { record.tags.forEach { put(it) } })
            }

            root.put("records", JSONArray().apply { snapshot.records.forEach { put(recordJson(it)) } })
            root.put("trash", JSONArray().apply { snapshot.trash.forEach { put(recordJson(it)) } })
            root.put("debts", JSONArray().apply {
                snapshot.debts.forEach { debt ->
                    put(JSONObject().apply {
                        put("id", debt.id)
                        put("type", debt.type.name)
                        put("counterparty", debt.counterparty)
                        put("amount", debt.amount)
                        put("note", debt.note)
                        put("createdAt", debt.createdAt)
                        put("settled", debt.settled)
                    })
                }
            })
            root.put("loans", JSONArray().apply {
                snapshot.loans.forEach { loan ->
                    put(JSONObject().apply {
                        put("id", loan.id)
                        put("name", loan.name)
                        put("principal", loan.principal)
                        put("annualRate", loan.annualRate)
                        put("months", loan.months)
                        put("startAt", loan.startAt)
                        put("paidCount", loan.paidCount)
                    })
                }
            })

            val accountsArray = JSONArray()
            snapshot.accounts.forEach { account ->
                accountsArray.put(
                    JSONObject().apply {
                        put("id", account.id)
                        put("name", account.name)
                        put("balance", account.balance)
                        put("type", account.type.name)
                        put("iconId", account.iconId)
                        account.creditLimit?.let { put("creditLimit", it) }
                        account.billDay?.let { put("billDay", it) }
                        account.repayDay?.let { put("repayDay", it) }
                        account.stockName?.let { put("stockName", it) }
                        put("cardNote", account.cardNote)
                        put("currency", account.currencyCode)
                    },
                )
            }
            root.put("accounts", accountsArray)

            root.put("books", JSONArray(snapshot.books))
            root.put("selectedBook", snapshot.selectedBook)

            val categoriesArray = JSONArray()
            snapshot.customCategories.forEach { (type, category) ->
                categoriesArray.put(
                    JSONObject().apply {
                        put("type", type.name)
                        put("id", category.id)
                        put("label", category.label)
                        category.parentId?.let { put("parentId", it) }
                    },
                )
            }
            root.put("customCategories", categoriesArray)

            val rulesArray = JSONArray()
            snapshot.recurringRules.forEach { rule ->
                rulesArray.put(
                    JSONObject().apply {
                        put("id", rule.id)
                        put("type", rule.type.name)
                        put("amount", rule.amount)
                        put("categoryId", rule.categoryId)
                        put("categoryLabel", rule.categoryLabel)
                        put("accountId", rule.accountId)
                        put("accountName", rule.accountName)
                        put("note", rule.note)
                        put("frequency", rule.frequency.name)
                        put("dayOfMonth", rule.dayOfMonth)
                        put("nextDueAt", rule.nextDueAt)
                        put("enabled", rule.enabled)
                    },
                )
            }
            root.put("recurringRules", rulesArray)

            root.put(
                "budget",
                JSONObject().apply {
                    put("monthlyLimit", snapshot.budget.monthlyLimit)
                    put("period", snapshot.budget.period)
                    put("rollover", snapshot.budget.rollover)
                    put("savingsGoal", snapshot.budget.savingsGoal)
                    put(
                        "categoryLimits",
                        JSONObject().apply {
                            snapshot.budget.categoryLimits.forEach { (id, limit) ->
                                put(id, limit)
                            }
                        },
                    )
                },
            )
            return root
    }
}

