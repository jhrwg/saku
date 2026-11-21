@file:OptIn(ExperimentalMaterial3Api::class)

package com.ve.bookkeeping.ui.pages

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ve.bookkeeping.ui.components.CurrencyPickerSheet
import com.ve.bookkeeping.ui.components.PageHeader
import com.ve.bookkeeping.ui.components.SectionCard
import com.ve.bookkeeping.ui.model.AccountIconCatalog
import com.ve.bookkeeping.ui.model.AccountOption
import com.ve.bookkeeping.ui.model.AccountType
import com.ve.bookkeeping.ui.model.CurrencyOption
import com.ve.bookkeeping.ui.model.accountIconById

/**
 * 创建账户页：类型固定，字段按类型变化。
 *
 * - 网络 / 借记卡 / 现金 / 储值卡：名称、余额、图标、币种、卡号备注
 * - 信用卡：再加信用额度、账单日、还款日
 * - 基金账户：用「股票名称」代替余额
 *
 * 余额允许为负，负数即负债（信用卡欠款、花呗等）。
 */
@Composable
fun CreateAccountPage(
    accountType: AccountType,
    currencies: List<CurrencyOption>,
    onSave: (AccountOption) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    editAccount: AccountOption? = null,
    currentBalance: Double? = null,
) {
    BackHandler(onBack = onBack)

    var name by rememberSaveable { mutableStateOf(editAccount?.name ?: "") }
    var balanceText by rememberSaveable {
        mutableStateOf(editAccount?.balance?.let { editableNumber(it) } ?: "")
    }
    var limitText by rememberSaveable {
        mutableStateOf(editAccount?.creditLimit?.let { editableNumber(it) } ?: "")
    }
    var billDayText by rememberSaveable {
        mutableStateOf(editAccount?.billDay?.toString() ?: "")
    }
    var repayDayText by rememberSaveable {
        mutableStateOf(editAccount?.repayDay?.toString() ?: "")
    }
    var stockName by rememberSaveable { mutableStateOf(editAccount?.stockName ?: "") }
    var cardNote by rememberSaveable { mutableStateOf(editAccount?.cardNote ?: "") }
    var iconId by rememberSaveable {
        mutableStateOf(editAccount?.iconId ?: AccountIconCatalog.first().id)
    }
    var currencyCode by rememberSaveable {
        mutableStateOf(editAccount?.currencyCode ?: currencies.first().code)
    }
    var showCurrencyPicker by remember { mutableStateOf(false) }

    val currency = currencies.firstOrNull { it.code == currencyCode } ?: currencies.first()
    val isCredit = accountType == AccountType.Credit
    val isFund = accountType == AccountType.Fund
    val canSave = name.isNotBlank()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surface) {
                Column(modifier = Modifier.navigationBarsPadding()) {
                    Button(
                        onClick = {
                            onSave(
                                AccountOption(
                                    id = editAccount?.id ?: "acc_${System.currentTimeMillis()}",
                                    name = name.trim(),
                                    balance = balanceText.toDoubleOrNull() ?: 0.0,
                                    icon = accountIconById(iconId),
                                    type = accountType,
                                    iconId = iconId,
                                    creditLimit = limitText.toDoubleOrNull(),
                                    billDay = billDayText.toIntOrNull()?.coerceIn(1, 28),
                                    repayDay = repayDayText.toIntOrNull()?.coerceIn(1, 28),
                                    stockName = stockName.trim().ifBlank { null },
                                    cardNote = cardNote.trim(),
                                    currencyCode = currencyCode,
                                    // 编辑账户时保留「余额起始时间」，否则保存后会把历史账目重新算进来
                                    balanceStartAt = editAccount?.balanceStartAt ?: 0L,
                                ),
                            )
                        },
                        enabled = canSave,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Text(text = "保存", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            PageHeader(
                title = if (editAccount != null) "编辑账户" else "创建账户",
                subtitle = accountType.label,
            )

            SectionCard(title = "账户类型") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = accountType.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = accountType.label,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            SectionCard(title = "基本信息") {
                OutlinedTextField(
                    value = name,
                    onValueChange = { if (it.length <= 16) name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = "账户名称") },
                    placeholder = { Text(text = "例如：招行储蓄卡") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                )

                if (isFund) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = stockName,
                        onValueChange = { if (it.length <= 20) stockName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(text = "股票名称") },
                        placeholder = { Text(text = "例如：沪深300ETF") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                    )
                } else {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = balanceText,
                        onValueChange = { balanceText = sanitizeAmount(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(text = "初始余额（${currency.symbol}）") },
                        placeholder = { Text(text = "负数表示负债") },
                        supportingText = {
                            Text(
                                text = if (currentBalance != null) {
                                    "这里改的是初始余额，当前余额 ¥${"%.2f".format(currentBalance)} 不会跟着变；" +
                                        "想让余额对齐，用资产页的「修改余额」"
                                } else {
                                    "开账户时的起点金额，之后记的账会累加到当前余额；填负数（如 -1500）记为负债"
                                },
                                style = MaterialTheme.typography.labelSmall,
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(14.dp),
                    )
                }

                if (isCredit) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = limitText,
                        onValueChange = { limitText = sanitizeAmount(it).removePrefix("-") },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(text = "信用额度（${currency.symbol}）") },
                        placeholder = { Text(text = "例如：10000") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(14.dp),
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = billDayText,
                            onValueChange = { billDayText = sanitizeDay(it) },
                            modifier = Modifier.weight(1f),
                            label = { Text(text = "账单日") },
                            placeholder = { Text(text = "如 5") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(14.dp),
                        )
                        OutlinedTextField(
                            value = repayDayText,
                            onValueChange = { repayDayText = sanitizeDay(it) },
                            modifier = Modifier.weight(1f),
                            label = { Text(text = "还款日") },
                            placeholder = { Text(text = "如 20") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(14.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            SectionCard(title = "图标") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AccountIconCatalog.take(5).forEach { option ->
                        IconChoice(
                            selected = option.id == iconId,
                            onClick = { iconId = option.id },
                            iconId = option.id,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AccountIconCatalog.drop(5).forEach { option ->
                        IconChoice(
                            selected = option.id == iconId,
                            onClick = { iconId = option.id },
                            iconId = option.id,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            SectionCard(title = "其他") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "币种",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Surface(
                        onClick = { showCurrencyPicker = true },
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "${currency.code}  ${currency.symbol}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Icon(
                                imageVector = Icons.Rounded.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = cardNote,
                    onValueChange = { if (it.length <= 30) cardNote = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = "卡号备注") },
                    placeholder = { Text(text = "如：尾号 8842") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showCurrencyPicker) {
        CurrencyPickerSheet(
            currencies = currencies,
            selectedCode = currencyCode,
            onSelect = { picked ->
                currencyCode = picked.code
                showCurrencyPicker = false
            },
            onDismiss = { showCurrencyPicker = false },
        )
    }
}

@Composable
private fun IconChoice(
    selected: Boolean,
    onClick: () -> Unit,
    iconId: String,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        },
        modifier = Modifier.size(48.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = accountIconById(iconId),
                contentDescription = null,
                tint = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

/** 金额输入过滤：允许开头一个负号、一个小数点、最多两位小数。 */
private fun sanitizeAmount(input: String): String {
    val negative = input.startsWith("-")
    val body = input.removePrefix("-").filter { it.isDigit() || it == '.' }
    val firstDot = body.indexOf('.')
    val normalized = if (firstDot >= 0) {
        body.substring(0, firstDot + 1) +
            body.substring(firstDot + 1).replace(".", "").take(2)
    } else {
        body.take(9)
    }
    return if (negative) "-$normalized" else normalized
}

/** 账单日 / 还款日：最多两位数字。 */
private fun sanitizeDay(input: String): String = input.filter { it.isDigit() }.take(2)

/** 把已有金额转成可继续编辑的文本（去掉多余的 .0）。 */
private fun editableNumber(value: Double): String {
    val rounded = Math.round(value * 100.0) / 100.0
    return if (rounded == Math.floor(rounded)) {
        rounded.toLong().toString()
    } else {
        val text = rounded.toString()
        if (text.endsWith("0")) text.dropLast(1) else text
    }
}
