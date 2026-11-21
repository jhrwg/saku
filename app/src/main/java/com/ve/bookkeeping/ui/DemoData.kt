package com.ve.bookkeeping.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.CreditScore
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.NorthEast
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Percent
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.ShowChart
import androidx.compose.material.icons.rounded.SouthWest
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material.icons.rounded.Work
import com.ve.bookkeeping.ui.model.AccountOption
import com.ve.bookkeeping.ui.model.AccountType
import com.ve.bookkeeping.ui.model.EntryCategory
import com.ve.bookkeeping.ui.model.currencyCatalog
import com.ve.bookkeeping.ui.model.EntryType
import com.ve.bookkeeping.ui.model.LedgerRecord
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Redeem
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Replay

/**
 * 应用的默认数据：只保留「日常账本」和一套默认分类，**没有任何演示账目与账户**。
 * 账户由用户在资产页自行创建，账目由记账页产生。
 */
object DemoData {

    /** 默认只有一个账本。 */
    val ledgerBooks = listOf("日常账本")

    val expenseCategories = listOf(
        EntryCategory("exp_food", "饮食", Icons.Rounded.Restaurant),
        EntryCategory("exp_transport", "交通", Icons.Rounded.DirectionsBus),
        EntryCategory("exp_shopping", "购物", Icons.Rounded.ShoppingBag),
        EntryCategory("exp_housing", "居住", Icons.Rounded.Home),
        EntryCategory("exp_health", "健康", Icons.Rounded.Favorite),
        EntryCategory("exp_fun", "娱乐", Icons.Rounded.SportsEsports),
        EntryCategory("exp_other", "其他", Icons.Rounded.MoreHoriz),
    EntryCategory("exp_phone", "通讯", Icons.Rounded.Phone),
    EntryCategory("exp_edu", "教育", Icons.Rounded.School),
    EntryCategory("exp_gift", "人情", Icons.Rounded.CardGiftcard),
    EntryCategory("exp_pet", "宠物", Icons.Rounded.Pets),
    EntryCategory("exp_sport", "运动", Icons.Rounded.FitnessCenter),
    EntryCategory("exp_digital", "数码", Icons.Rounded.Devices),
    EntryCategory("exp_beauty", "美容", Icons.Rounded.Spa),
    EntryCategory("exp_travel", "旅行", Icons.Rounded.Flight),
    EntryCategory("exp_car", "汽车", Icons.Rounded.DirectionsCar),
    )

    val incomeCategories = listOf(
        EntryCategory("inc_salary", "工资", Icons.Rounded.Payments),
        EntryCategory("inc_living", "生活费", Icons.Rounded.Savings),
        EntryCategory("inc_interest", "利息", Icons.Rounded.Percent),
        EntryCategory("inc_fund", "基金", Icons.Rounded.ShowChart),
    EntryCategory("inc_bonus", "奖金", Icons.Rounded.Redeem),
    EntryCategory("inc_parttime", "兼职", Icons.Rounded.Work),
    EntryCategory("inc_redpacket", "红包", Icons.Rounded.CardGiftcard),
    EntryCategory("inc_reimburse", "报销", Icons.Rounded.ReceiptLong),
    EntryCategory("inc_refund", "退款", Icons.Rounded.Replay),
        EntryCategory("inc_bonus", "奖金", Icons.Rounded.EmojiEvents),
        EntryCategory("inc_lottery", "彩票", Icons.Rounded.Casino),
        EntryCategory("inc_parttime", "兼职", Icons.Rounded.Work),
    )

    val transferCategories = listOf(
        EntryCategory("trf_out", "转出", Icons.Rounded.NorthEast),
        EntryCategory("trf_in", "转入", Icons.Rounded.SouthWest),
        EntryCategory("trf_between", "账户互转", Icons.Rounded.SwapHoriz),
        EntryCategory("trf_repay", "还款", Icons.Rounded.CreditScore),
    )

    /** 全部在流通的货币（153 种），见 CurrencyCatalog。 */
    val currencies = currencyCatalog

    const val userName = "折耳根"

    /** 全新账本：没有任何账户。 */
    val accounts: List<AccountOption> = emptyList()

    /** 全新账本：没有任何账目。 */
    fun seedRecords(): List<LedgerRecord> = emptyList()

    /** 没有账户时的占位账户（记账时可以不选账户）。 */
    val NoAccount = AccountOption(
        id = "none",
        name = "无账户",
        balance = 0.0,
        icon = Icons.Rounded.Wallet,
        type = AccountType.Cash,
        iconId = "cash",
    )

    /** 用户新增的分类：统一的默认图标，`ordinal` 只用来保证 id 唯一。 */
    fun createCustomCategory(type: EntryType, label: String, ordinal: Int): EntryCategory =
        EntryCategory(
            id = "${type.name.lowercase()}_custom_$ordinal",
            label = label,
            icon = Icons.Rounded.LocalOffer,
            isCustom = true,
        )
}


