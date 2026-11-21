package com.ve.bookkeeping.ui.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.ShowChart
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.rounded.CorporateFare
import androidx.compose.material.icons.rounded.Contactless
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.LocalMall
import androidx.compose.material.icons.rounded.LocalAtm
import androidx.compose.material.icons.rounded.RequestQuote
import androidx.compose.material.icons.rounded.TrendingUp

/** 记账类型：支出 / 收入 / 转账。 */
enum class EntryType(val label: String) {
    Expense("支出"),
    Income("收入"),
    Transfer("转账"),
}

/** 账户类型；创建账户时先选类型，再按类型显示不同字段。 */
enum class AccountType(val label: String, val icon: ImageVector) {
    Network("网络账户", Icons.Rounded.AccountBalanceWallet),
    Credit("信用卡", Icons.Rounded.CreditCard),
    Debit("借记卡", Icons.Rounded.Savings),
    Fund("基金账户", Icons.Rounded.ShowChart),
    Cash("现金", Icons.Rounded.Payments),
    StoredValue("储值卡", Icons.Rounded.CardGiftcard),
}

/** 账户可选图标（持久化时存 id）。 */
data class AccountIconOption(val id: String, val icon: ImageVector)

val AccountIconCatalog = listOf(
    AccountIconOption("wallet", Icons.Rounded.AccountBalanceWallet),
    AccountIconOption("card", Icons.Rounded.CreditCard),
    AccountIconOption("savings", Icons.Rounded.Savings),
    AccountIconOption("bank", Icons.Rounded.AccountBalance),
    AccountIconOption("cash", Icons.Rounded.Payments),
    AccountIconOption("gift", Icons.Rounded.CardGiftcard),
    AccountIconOption("chart", Icons.Rounded.ShowChart),
    AccountIconOption("store", Icons.Rounded.Storefront),
    AccountIconOption("phone", Icons.Rounded.Smartphone),
    AccountIconOption("qr", Icons.Rounded.QrCode),
    // 更多银行 / 常见信用产品
    AccountIconOption("bank_city", Icons.Rounded.CorporateFare),
    AccountIconOption("bank_online", Icons.Rounded.AccountBalance),
    AccountIconOption("unionpay", Icons.Rounded.Contactless),
    AccountIconOption("huabei", Icons.Rounded.ShoppingBag),
    AccountIconOption("baitiao", Icons.Rounded.LocalMall),
    AccountIconOption("jiebei", Icons.Rounded.LocalAtm),
    AccountIconOption("loan", Icons.Rounded.RequestQuote),
    AccountIconOption("stock", Icons.Rounded.TrendingUp),
)

fun accountIconById(id: String?): ImageVector =
    AccountIconCatalog.firstOrNull { it.id == id }?.icon ?: Icons.Rounded.AccountBalanceWallet

/** 记账页的分类；[parentId] 非空表示这是一个二级分类。 */
data class EntryCategory(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val isCustom: Boolean = false,
    val parentId: String? = null,
)

/**
 * 一个资金账户。余额为负表示负债（信用卡欠款、花呗等）。
 * [iconId] 指向 [AccountIconCatalog]，用于持久化后还原图标。
 */
data class AccountOption(
    val id: String,
    val name: String,
    val balance: Double,
    val icon: ImageVector,
    val type: AccountType = AccountType.Network,
    val iconId: String = "wallet",
    val creditLimit: Double? = null,
    val billDay: Int? = null,
    val repayDay: Int? = null,
    val stockName: String? = null,
    val cardNote: String = "",
    val currencyCode: String = "CNY",
    /**
     * 余额起始时间（毫秒，0 = 从最开始算）。
     *
     * 只统计 occurredAt >= 该时间的账目来推算当前余额；「更改当前余额」会把它设成现在，
     * 之前的账目仍然保留在流水里，但不再影响这个账户的余额。
     */
    val balanceStartAt: Long = 0L,
)

/** 记账页可选的币种。 */
data class CurrencyOption(
    val code: String,
    val name: String,
    val symbol: String,
    /** 非 CNY / USD 的币种需要 PRO。 */
    val proOnly: Boolean = false,
    /** 折算人民币的参考汇率（1 单位外币 = rateToCny 元）。 */
    val rateToCny: Double = 1.0,
)

