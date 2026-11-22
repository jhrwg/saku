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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.CurrencyExchange
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ve.bookkeeping.ui.DemoData
import com.ve.bookkeeping.ui.LayoutMode
import com.ve.bookkeeping.ui.rememberLayoutMode
import com.ve.bookkeeping.ui.components.AccountPickerSheet
import com.ve.bookkeeping.ui.components.AddCategoryDialog
import com.ve.bookkeeping.ui.components.AllCategoriesSheet
import com.ve.bookkeeping.ui.components.AmountKeypadSheet
import kotlinx.coroutines.launch
import com.ve.bookkeeping.ui.components.CurrencyPickerSheet
import com.ve.bookkeeping.ui.components.InlineAmountKeypad
import com.ve.bookkeeping.ui.components.editableAmount
import com.ve.bookkeeping.ui.components.keypadPress
import com.ve.bookkeeping.ui.components.PageHeader
import com.ve.bookkeeping.ui.components.SectionCard
import com.ve.bookkeeping.ui.components.formatMoney
import com.ve.bookkeeping.ui.model.AccountOption
import com.ve.bookkeeping.ui.model.CurrencyOption
import com.ve.bookkeeping.ui.model.EntryCategory
import com.ve.bookkeeping.ui.model.EntryType
import com.ve.bookkeeping.ui.model.LedgerRecord
import com.ve.bookkeeping.ui.theme.LocalAppExtraColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.draw.clip
import com.ve.bookkeeping.ui.model.Participant
import java.io.File
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.Percent
import androidx.compose.material3.Switch
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.foundation.Image
import androidx.compose.material3.FilterChip
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.TextButton
import androidx.compose.foundation.combinedClickable

/**
 * 记账页。
 *
 * 支出 / 收入 / 转账三种类型共用同一套骨架，差异只在：金额配色与符号、分类集合、
 * 转账多出「转入账户 / 手续费」。分类最多铺 8 个格子（7 个分类 + 「添加」），
 * 分类超过 7 个时出现「全部分类」入口。
 */
@Composable
fun EntryPage(
    initialAmount: Double,
    bookName: String,
    expenseCategories: List<EntryCategory>,
    incomeCategories: List<EntryCategory>,
    transferCategories: List<EntryCategory>,
    accounts: List<AccountOption>,
    defaultAccount: AccountOption,
    onAddCategory: (EntryType, String) -> EntryCategory,
    onAddSubCategory: ((EntryType, String, String) -> EntryCategory)? = null,
    onSave: (LedgerRecord) -> Unit,
    onBack: () -> Unit,
    availableTags: List<String> = emptyList(),
    onAddTag: (String) -> Unit = {},
    proEnabled: Boolean = true,
    onNeedPro: () -> Unit = {},
    modifier: Modifier = Modifier,
    editRecord: LedgerRecord? = null,
) {
    BackHandler(onBack = onBack)

    val extra = LocalAppExtraColors.current

    var typeOrdinal by rememberSaveable { mutableIntStateOf(editRecord?.type?.ordinal ?: 0) }
    val type = EntryType.entries[typeOrdinal]

    var amount by rememberSaveable {
        mutableDoubleStateOf(editRecord?.amount ?: if (initialAmount > 0) initialAmount else 0.0)
    }
    var note by rememberSaveable { mutableStateOf(editRecord?.note ?: "") }
    var accountId by rememberSaveable {
        mutableStateOf(editRecord?.fromAccount?.id ?: defaultAccount.id)
    }
    var toAccountIdRaw by rememberSaveable {
        mutableStateOf(
            editRecord?.toAccount?.id
                ?: accounts.firstOrNull { it.id != defaultAccount.id }?.id
                ?: "",
        )
    }
    var fee by rememberSaveable { mutableDoubleStateOf(editRecord?.fee ?: 0.0) }
    var currencyCode by rememberSaveable {
        mutableStateOf(editRecord?.currency?.code ?: DemoData.currencies.first().code)
    }

    val initialCalendar = remember { Calendar.getInstance() }
    val initialDateMillis = remember { utcMidnightOf(initialCalendar) }
    // 编辑已有记录时，日期与时间从记录里还原
    val editCalendar = remember(editRecord) {
        editRecord?.let { Calendar.getInstance().apply { timeInMillis = it.occurredAt } }
    }
    var dateMillis by rememberSaveable {
        mutableLongStateOf(editCalendar?.let { utcMidnightOf(it) } ?: initialDateMillis)
    }
    var hour by rememberSaveable {
        mutableIntStateOf(editCalendar?.get(Calendar.HOUR_OF_DAY) ?: initialCalendar.get(Calendar.HOUR_OF_DAY))
    }
    var minute by rememberSaveable {
        mutableIntStateOf(editCalendar?.get(Calendar.MINUTE) ?: initialCalendar.get(Calendar.MINUTE))
    }

    var selectedCategoryId by rememberSaveable {
        mutableStateOf(editRecord?.category?.id ?: expenseCategories.firstOrNull()?.id ?: "")
    }
    var pinnedCategoryId by rememberSaveable { mutableStateOf("") }
    var selectionInitialized by rememberSaveable { mutableStateOf(false) }
    var childPickerFor by remember { mutableStateOf<EntryCategory?>(null) }
    var addChildFor by remember { mutableStateOf<EntryCategory?>(null) }

    var showFeeKeypad by remember { mutableStateOf(false) }
    var showAccountPicker by remember { mutableStateOf(false) }
    var showToAccountPicker by remember { mutableStateOf(false) }
    var showAllCategories by remember { mutableStateOf(false) }
    var showAddCategory by remember { mutableStateOf(false) }
    var selectedTags by remember { mutableStateOf<List<String>>(editRecord?.tags ?: emptyList()) }
    var showAddTag by remember { mutableStateOf(false) }
    var showCurrencyPicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val categories = when (type) {
        EntryType.Expense -> expenseCategories
        EntryType.Income -> incomeCategories
        EntryType.Transfer -> transferCategories
    }
    // 分类格子只铺一级分类；二级分类在点开一级分类后选择
    val roots = categories.filter { it.parentId == null }

    val currency = DemoData.currencies.firstOrNull { it.code == currencyCode }
        ?: DemoData.currencies.first()
    val toAccountId: String? = toAccountIdRaw.ifBlank { null }

    LaunchedEffect(typeOrdinal) {
        // 首次进入不要覆盖编辑态的分类，只有用户切换类型时才重置
        if (selectionInitialized) {
            pinnedCategoryId = ""
            selectedCategoryId = roots.firstOrNull()?.id ?: ""
        } else {
            selectionInitialized = true
        }
    }

    // 第一个格子可被「全部分类」里选中的分类替换，其余最多再排 6 个
    val pinnedCategory = roots.firstOrNull { it.id == pinnedCategoryId }
    val visibleCategories: List<EntryCategory> = if (pinnedCategory != null) {
        listOf(pinnedCategory) + roots.filter { it.id != pinnedCategory.id }.take(6)
    } else {
        roots.take(7)
    }

    val fromAccount = accounts.firstOrNull { it.id == accountId } ?: defaultAccount
    val toAccount = accounts.firstOrNull { it.id == toAccountId }
    val amountColor = when (type) {
        EntryType.Expense -> extra.expense
        EntryType.Income -> extra.income
        EntryType.Transfer -> MaterialTheme.colorScheme.primary
    }
    val signPrefix = if (type == EntryType.Expense) "-" else ""

    var lastSaveAt by remember { mutableStateOf(0L) }

    // —— 金额输入：内嵌键盘，点了立刻改数字；矮屏用 4 列小键，避免吃掉整屏 ——
    val configuration = LocalConfiguration.current
    val shortScreen = configuration.screenHeightDp < 520
    val keypadColumns = if (shortScreen) 4 else 3
    val keypadKeyHeight = if (shortScreen) 34.dp else 46.dp
    var keypadOpen by rememberSaveable { mutableStateOf(false) }
    var amountInput by rememberSaveable { mutableStateOf(editableAmount(amount)) }

    fun openKeypad() {
        amountInput = editableAmount(amount)
        keypadOpen = true
    }

    fun pressAmountKey(key: String) {
        val next = keypadPress(amountInput, key) ?: return
        amountInput = next
        amount = next.toDoubleOrNull() ?: 0.0
    }

    fun addQuickAmount(step: Double) {
        amount += step
        amountInput = editableAmount(amount)
    }

    // —— 优惠 / 分摊 / 附件 ——
    var originalAmount by rememberSaveable {
        mutableDoubleStateOf(editRecord?.originalAmount ?: 0.0)
    }
    var discount by rememberSaveable { mutableDoubleStateOf(editRecord?.discount ?: 0.0) }
    var splitEnabled by rememberSaveable {
        mutableStateOf(editRecord?.participants?.isNotEmpty() == true)
    }
    var splitCount by rememberSaveable {
        mutableIntStateOf(editRecord?.participants?.size?.coerceAtLeast(2) ?: 2)
    }
    var myShare by rememberSaveable { mutableDoubleStateOf(editRecord?.myShare ?: 0.0) }
    var installmentEnabled by rememberSaveable {
        mutableStateOf((editRecord?.installmentCount ?: 0) > 1)
    }
    var installmentPeriods by rememberSaveable {
        mutableIntStateOf(editRecord?.installmentCount?.takeIf { it > 1 } ?: 6)
    }
    val attachments = remember {
        mutableStateListOf<String>().apply { addAll(editRecord?.attachments.orEmpty()) }
    }
    var showOriginalKeypad by remember { mutableStateOf(false) }
    var showDiscountKeypad by remember { mutableStateOf(false) }
    val entryContext = LocalContext.current
    val attachPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            runCatching {
                val dir = File(entryContext.filesDir, "attachments").apply { mkdirs() }
                val name = "att_${System.currentTimeMillis()}.jpg"
                entryContext.contentResolver.openInputStream(uri)?.use { input ->
                    File(dir, name).outputStream().use { output -> input.copyTo(output) }
                }
                attachments.add(name)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surface) {
                Column(modifier = Modifier.navigationBarsPadding()) {
                    if (keypadOpen) {
                        InlineAmountKeypad(
                            amountText = amountInput,
                            onKey = { pressAmountKey(it) },
                            onDone = { keypadOpen = false },
                            keyHeight = keypadKeyHeight,
                            columns = keypadColumns,
                        )
                    }
                    Button(
                        onClick = {
                            // 防抖：600ms 内重复点击只记一次，避免连点生成重复账目
                            val now = System.currentTimeMillis()
                            if (now - lastSaveAt < 600) return@Button
                            lastSaveAt = now
                            if (amount > 9_999_999_999.0) return@Button
                            val category = categories.firstOrNull { it.id == selectedCategoryId }
                                ?: categories.firstOrNull()
                                ?: return@Button
                            onSave(
                                LedgerRecord(
                                    id = editRecord?.id ?: System.currentTimeMillis(),
                                    bookName = bookName,
                                    type = type,
                                    amount = amount,
                                    category = category,
                                    fromAccount = fromAccount,
                                    toAccount = if (type == EntryType.Transfer) toAccount else null,
                                    fee = if (type == EntryType.Transfer) fee else 0.0,
                                    note = note.trim(),
                                    currency = currency,
                                    occurredAt = combineDateAndTime(dateMillis, hour, minute),
                                    originalAmount = if (discount > 0) originalAmount else amount,
                                    discount = if (discount > 0) discount else 0.0,
                                    participants = if (splitEnabled && splitCount >= 2) {
                                        val mine = myShare.coerceIn(0.0, amount)
                                        val others = (amount - mine) / (splitCount - 1)
                                        (0 until splitCount).map { index ->
                                            if (index == 0) {
                                                Participant("我", mine)
                                            } else {
                                                Participant("成员 ${index + 1}", others)
                                            }
                                        }
                                    } else {
                                        emptyList()
                                    },
                                    myShare = if (splitEnabled && splitCount >= 2) {
                                        myShare.coerceIn(0.0, amount)
                                    } else {
                                        amount
                                    },
                                    attachments = attachments.toList(),
                                    installmentIndex = if (installmentEnabled && type == EntryType.Expense) 1 else 0,
                                    installmentCount = if (installmentEnabled && type == EntryType.Expense) installmentPeriods else 0,
                                    tags = selectedTags,
                                ),
                            )
                        },
                        enabled = amount > 0 && (type != EntryType.Transfer || toAccount != null),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Text(
                            text = if (amount > 0) "保存 ¥${formatMoney(amount)}" else "保存",
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        // 竖屏单列；横屏 / 平板两列：左侧金额与分类，右侧明细与附加项
        val wideLayout = rememberLayoutMode() != LayoutMode.Compact
        val scrollState = rememberScrollState()

        // 标题 + 当前账本
        val headerBlock: @Composable () -> Unit = {
            PageHeader(title = "记一笔", subtitle = "$bookName · ${type.label}")
            TypeTabs(selected = type, onSelect = { typeOrdinal = it.ordinal })
        }

        // 金额卡：大金额 + 一眼可见的上下文（币种 / 分类 / 账户）
        val amountBlock: @Composable () -> Unit = {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    AmountSection(
                        sign = signPrefix,
                        symbol = currency.symbol,
                        amountText = formatMoney(amount),
                        color = amountColor,
                        onClick = { openKeypad() },
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    androidx.compose.foundation.layout.FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        AmountChip(text = currency.code)
                        AmountChip(
                            text = categories.firstOrNull { it.id == selectedCategoryId }?.label
                                ?: "未选分类",
                            highlight = categories.any { it.id == selectedCategoryId },
                        )
                        AmountChip(text = fromAccount.name)
                        if (discount > 0) {
                            AmountChip(text = "优惠 -${formatMoney(discount)}", highlight = true)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    // 常用金额快捷累加
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(10.0, 50.0, 100.0).forEach { step ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                                    .clickable { addQuickAmount(step) },
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "+${step.toInt()}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        val categoryBlock: @Composable () -> Unit = {
            Spacer(modifier = Modifier.height(18.dp))
            CategorySection(
                categories = visibleCategories,
                selectedId = selectedCategoryId,
                childCounts = categories.filter { it.parentId != null }
                    .groupingBy { it.parentId!! }
                    .eachCount(),
                showAllEntry = roots.size > 7,
                onSelect = { picked ->
                    val children = categories.filter { it.parentId == picked.id }
                    if (children.isEmpty()) {
                        selectedCategoryId = picked.id
                    } else {
                        childPickerFor = picked
                    }
                },
                onLongPress = { category -> addChildFor = category },
                onAdd = { showAddCategory = true },
                onShowAll = { showAllCategories = true },
            )
        }

        // 明细卡：账户 / 日期时间币种 / 备注 / 标签 合并在一张卡里
        val detailsBlock: @Composable () -> Unit = {
            Spacer(modifier = Modifier.height(14.dp))
            SectionCard(title = "明细") {
                InfoRow(
                    icon = fromAccount.icon,
                    label = if (type == EntryType.Transfer) "转出账户" else "账户",
                    value = fromAccount.name,
                    onClick = { showAccountPicker = true },
                )
                if (type == EntryType.Transfer) {
                    DetailDivider()
                    InfoRow(
                        icon = toAccount?.icon ?: Icons.Rounded.Wallet,
                        label = "转入账户",
                        value = toAccount?.name ?: "请选择",
                        onClick = { showToAccountPicker = true },
                    )
                    DetailDivider()
                    InfoRow(
                        icon = Icons.Rounded.Payments,
                        label = "手续费",
                        value = "¥${formatMoney(fee)}",
                        onClick = { showFeeKeypad = true },
                    )
                }
                DetailDivider()
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MoreChip(
                        icon = Icons.Rounded.CalendarMonth,
                        label = "日期",
                        value = formatDate(dateMillis),
                        modifier = Modifier.weight(1f),
                        onClick = { showDatePicker = true },
                    )
                    MoreChip(
                        icon = Icons.Rounded.Schedule,
                        label = "时间",
                        value = String.format(Locale.CHINA, "%02d:%02d", hour, minute),
                        modifier = Modifier.weight(1f),
                        onClick = { showTimePicker = true },
                    )
                    MoreChip(
                        icon = Icons.Rounded.CurrencyExchange,
                        label = "币种",
                        value = currency.code,
                        modifier = Modifier.weight(1f),
                        onClick = { showCurrencyPicker = true },
                    )
                }
                // 非人民币时给出汇率，并允许就地更新（不用跑去汇率页）
                if (currency.code != "CNY") {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val scope = androidx.compose.runtime.rememberCoroutineScope()
                    // 读一下 rates 让它参与重组：刷新完成后自动更新数字
                    val liveRates = com.ve.bookkeeping.data.ExchangeRates.rates
                    val rate = liveRates[currency.code] ?: currency.rateToCny
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "1 ${currency.code} ≈ ¥${formatMoney(rate)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "更新汇率",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable {
                                    scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                        com.ve.bookkeeping.data.ExchangeRates.refresh(context)
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(text = "写点什么…") },
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(14.dp),
                )
                DetailDivider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "标签",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "＋ 新建标签",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { showAddTag = true }
                            .padding(horizontal = 4.dp, vertical = 6.dp),
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                val tagOptions = (availableTags + selectedTags).distinct()
                if (tagOptions.isEmpty()) {
                    Text(
                        text = "还没有标签，点「新建标签」添加",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    androidx.compose.foundation.layout.FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        tagOptions.forEach { tag ->
                            val on = selectedTags.contains(tag)
                            FilterChip(
                                selected = on,
                                onClick = {
                                    selectedTags = if (on) selectedTags - tag else selectedTags + tag
                                },
                                label = { Text(text = tag) },
                            )
                        }
                    }
                }
            }
        }

        val extrasBlock: @Composable () -> Unit = {
            Spacer(modifier = Modifier.height(14.dp))
            SectionCard(title = "优惠 / 分摊 / 附件") {
                if (type != EntryType.Transfer) {
                    InfoRow(
                        icon = Icons.Rounded.LocalOffer,
                        label = "原价",
                        value = if (discount > 0) "¥${formatMoney(originalAmount)}" else "未设置",
                        onClick = { showOriginalKeypad = true },
                    )
                    DetailDivider()
                    InfoRow(
                        icon = Icons.Rounded.Percent,
                        label = "优惠",
                        value = if (discount > 0) "-¥${formatMoney(discount)}" else "无",
                        onClick = { showDiscountKeypad = true },
                    )
                    DetailDivider()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "多人分摊",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = if (splitEnabled) {
                                    "$splitCount 人 · 我付 ¥${formatMoney(myShare)}"
                                } else {
                                    "全额计入我的支出"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        if (splitEnabled) {
                            TextButton(onClick = {
                                splitCount = (splitCount - 1).coerceAtLeast(2)
                                myShare = ((amount - discount) / splitCount).coerceAtLeast(0.0)
                            }) { Text(text = "−") }
                            Text(
                                text = "$splitCount",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            TextButton(onClick = {
                                splitCount = (splitCount + 1).coerceAtMost(20)
                                myShare = ((amount - discount) / splitCount).coerceAtLeast(0.0)
                            }) { Text(text = "+") }
                        }
                        Switch(
                            checked = splitEnabled,
                            onCheckedChange = { enabled ->
                                splitEnabled = enabled
                                if (enabled) {
                                    myShare = ((amount - discount) / splitCount)
                                        .coerceAtLeast(0.0)
                                }
                            },
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "分期付款",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = if (installmentEnabled) {
                                "分 $installmentPeriods 期 · 每期 ¥${formatMoney(
                                    (amount - discount) / installmentPeriods,
                                )}"
                            } else {
                                "一次性计入本月"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = installmentEnabled,
                        onCheckedChange = { installmentEnabled = it },
                    )
                }
                if (installmentEnabled) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(3, 6, 12, 24).forEach { periods ->
                            FilterChip(
                                selected = installmentPeriods == periods,
                                onClick = { installmentPeriods = periods },
                                label = { Text(text = "$periods 期") },
                            )
                        }
                    }
                }

                DetailDivider()
                Text(
                    text = if (attachments.isEmpty()) "还没有附件" else "附件 ${attachments.size} 张",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (attachments.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(attachments) { name ->
                            Box {
                                val thumb = remember(name) {
                                    runCatching {
                                        android.graphics.BitmapFactory.decodeFile(
                                            File(File(entryContext.filesDir, "attachments"), name)
                                                .absolutePath,
                                        )
                                    }.getOrNull()
                                }
                                if (thumb != null) {
                                    Image(
                                        bitmap = thumb.asImageBitmap(),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(RoundedCornerShape(12.dp)),
                                    )
                                }
                                Text(
                                    text = "×",
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .clickable { attachments.remove(name) },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }
                }
                TextButton(
                    onClick = {
                        attachPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    },
                ) {
                    Text(text = "添加图片")
                }
            }
        }

        if (wideLayout) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp),
            ) {
                headerBlock()
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        amountBlock()
                        categoryBlock()
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        detailsBlock()
                        extrasBlock()
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp),
            ) {
                headerBlock()
                amountBlock()
                categoryBlock()
                detailsBlock()
                extrasBlock()
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // —— 底部面板与弹窗 ——

    if (showOriginalKeypad) {
        AmountKeypadSheet(
            title = "原价",
            subtitle = "优惠前金额",
            initialAmount = if (originalAmount > 0) originalAmount else amount,
            onDismiss = { showOriginalKeypad = false },
            onDone = { value ->
                originalAmount = value
                if (discount > value) discount = 0.0
                amount = (value - discount).coerceAtLeast(0.01)
                showOriginalKeypad = false
            },
        )
    }

    if (showDiscountKeypad) {
        AmountKeypadSheet(
            title = "优惠",
            subtitle = "减免金额",
            initialAmount = discount,
            onDismiss = { showDiscountKeypad = false },
            onDone = { value ->
                val base = if (originalAmount > 0) originalAmount else amount + discount
                originalAmount = base
                discount = value.coerceAtMost(base - 0.01).coerceAtLeast(0.0)
                amount = (base - discount).coerceAtLeast(0.01)
                showDiscountKeypad = false
            },
        )
    }

    if (showFeeKeypad) {
        AmountKeypadSheet(
            title = "手续费",
            subtitle = if (toAccount != null) "${fromAccount.name} → ${toAccount.name}" else null,
            initialAmount = fee,
            onDismiss = { showFeeKeypad = false },
            onDone = { value ->
                fee = value
                showFeeKeypad = false
            },
        )
    }

    if (showAccountPicker) {
        AccountPickerSheet(
            title = if (type == EntryType.Transfer) "选择转出账户" else "选择账户",
            accounts = accounts,
            selectedId = accountId,
            excludeId = if (type == EntryType.Transfer) toAccountId else null,
            onSelect = { picked ->
                accountId = picked.id
                // 转账：转出与转入不能是同一个账户
                if (type == EntryType.Transfer && picked.id == toAccountId) {
                    toAccountIdRaw = accounts.firstOrNull { it.id != picked.id }?.id ?: ""
                }
                showAccountPicker = false
            },
            onDismiss = { showAccountPicker = false },
        )
    }

    if (showToAccountPicker) {
        AccountPickerSheet(
            title = "选择转入账户",
            accounts = accounts,
            selectedId = toAccountId,
            excludeId = accountId,
            onSelect = { picked ->
                toAccountIdRaw = picked.id
                showToAccountPicker = false
            },
            onDismiss = { showToAccountPicker = false },
        )
    }

    if (showAllCategories) {
        AllCategoriesSheet(
            categories = roots,
            selectedId = selectedCategoryId,
            onSelect = { picked ->
                pinnedCategoryId = picked.id
                selectedCategoryId = picked.id
                showAllCategories = false
            },
            onDismiss = { showAllCategories = false },
        )
    }

    childPickerFor?.let { parent ->
        val children = categories.filter { it.parentId == parent.id }
        AllCategoriesSheet(
            categories = listOf(parent) + children,
            selectedId = selectedCategoryId,
            onSelect = { picked ->
                selectedCategoryId = picked.id
                childPickerFor = null
            },
            onDismiss = { childPickerFor = null },
        )
    }

    if (showAddTag) {
        AddCategoryDialog(
            onDismiss = { showAddTag = false },
            onConfirm = { name ->
                onAddTag(name)
                selectedTags = selectedTags + name
                showAddTag = false
            },
            title = "新建标签",
            fieldLabel = "标签名称",
            placeholder = "例如：出差",
        )
    }

    if (showAddCategory) {
        AddCategoryDialog(
            onDismiss = { showAddCategory = false },
            onConfirm = { name ->
                val created = onAddCategory(type, name)
                pinnedCategoryId = created.id
                selectedCategoryId = created.id
                showAddCategory = false
            },
        )
    }

    // 长按一级分类 → 新建它的子分类（以前只记了状态，没有弹窗，所以点了没反应）
    addChildFor?.let { parent ->
        AddCategoryDialog(
            onDismiss = { addChildFor = null },
            onConfirm = { name ->
                val created = onAddSubCategory?.invoke(type, name, parent.id)
                if (created != null) {
                    pinnedCategoryId = created.id
                    selectedCategoryId = created.id
                }
                addChildFor = null
            },
            title = "新建「${parent.label}」的子分类",
            fieldLabel = "子分类名称",
            placeholder = "例如：早餐 / 打车",
        )
    }

    if (showCurrencyPicker) {
        CurrencyPickerSheet(
            currencies = DemoData.currencies,
            selectedCode = currency.code,
            proEnabled = proEnabled,
            onNeedPro = onNeedPro,
            onSelect = { picked: CurrencyOption ->
                currencyCode = picked.code
                showCurrencyPicker = false
            },
            onDismiss = { showCurrencyPicker = false },
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { dateMillis = it }
                    showDatePicker = false
                }) {
                    Text(text = "确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(text = "取消")
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = hour,
            initialMinute = minute,
            is24Hour = true,
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text(text = "选择时间") },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    hour = timePickerState.hour
                    minute = timePickerState.minute
                    showTimePicker = false
                }) {
                    Text(text = "确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(text = "取消")
                }
            },
        )
    }
}

@Composable
private fun TypeTabs(
    selected: EntryType,
    onSelect: (EntryType) -> Unit,
) {
    val types = EntryType.entries
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        types.forEachIndexed { index, entryType ->
            SegmentedButton(
                selected = selected == entryType,
                onClick = { onSelect(entryType) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = types.size),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = MaterialTheme.colorScheme.primary,
                    activeContentColor = MaterialTheme.colorScheme.onPrimary,
                    activeBorderColor = MaterialTheme.colorScheme.primary,
                ),
                label = { Text(text = entryType.label) },
            )
        }
    }
}

@Composable
private fun AmountSection(
    sign: String,
    symbol: String,
    amountText: String,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = androidx.compose.ui.graphics.Color.Transparent,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = "$sign$symbol",
                style = MaterialTheme.typography.headlineSmall,
                color = color.copy(alpha = 0.75f),
                modifier = Modifier.padding(bottom = 6.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = amountText,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1,
                modifier = Modifier.weight(1f, fill = false),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "点击修改",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 10.dp),
            )
        }
    }
}

@Composable
private fun CategorySection(
    categories: List<EntryCategory>,
    selectedId: String?,
    childCounts: Map<String, Int> = emptyMap(),
    showAllEntry: Boolean,
    onSelect: (EntryCategory) -> Unit,
    onLongPress: (EntryCategory) -> Unit = {},
    onAdd: () -> Unit,
    onShowAll: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "分类",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.weight(1f))
            if (showAllEntry) {
                TextButton(onClick = onShowAll) {
                    Text(text = "全部分类", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        val tiles = categories + null // null 代表「添加」格子
        tiles.chunked(4).forEach { rowItems ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowItems.forEach { category ->
                    if (category == null) {
                        AddCategoryTile(
                            modifier = Modifier.weight(1f),
                            onClick = onAdd,
                        )
                    } else {
                        CategoryTile(
                            category = category,
                            selected = category.id == selectedId,
                            childCount = childCounts[category.id] ?: 0,
                            modifier = Modifier.weight(1f),
                            onClick = { onSelect(category) },
                            onLongClick = { onLongPress(category) },
                        )
                    }
                }
                repeat(4 - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CategoryTile(
    category: EntryCategory,
    selected: Boolean,
    childCount: Int = 0,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
) {
    Surface(
        modifier = modifier
            .height(72.dp)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = RoundedCornerShape(18.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
        },
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box {
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    tint = if (selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(22.dp),
                )
                // 有二级分类的角标：提示可以下钻
                if (childCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (selected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 8.dp, y = (-4).dp),
                    ) {
                        Text(
                            text = "$childCount",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.onPrimary
                            },
                            modifier = Modifier.padding(horizontal = 3.dp),
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = category.label,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun AddCategoryTile(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(72.dp),
        shape = RoundedCornerShape(18.dp),
        color = androidx.compose.ui.graphics.Color.Transparent,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(18.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "添加分类",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp),
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "添加",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(17.dp),
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.weight(1f))
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(8.dp),
            color = androidx.compose.ui.graphics.Color.Transparent,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = value,
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
}

@Composable
private fun MoreChip(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(62.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(13.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
        }
    }
}

/** 明细卡内部的分隔线。 */
@Composable
private fun DetailDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 6.dp),
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.14f),
    )
}

private fun utcMidnightOf(calendar: Calendar): Long {
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    utc.clear()
    utc.set(
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH),
    )
    return utc.timeInMillis
}

/** 把「UTC 午夜日期」+「本地时分」拼成本地时间戳。 */
private fun combineDateAndTime(dateMillis: Long, hour: Int, minute: Int): Long {
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = dateMillis }
    return Calendar.getInstance().apply {
        set(
            utc.get(Calendar.YEAR),
            utc.get(Calendar.MONTH),
            utc.get(Calendar.DAY_OF_MONTH),
            hour,
            minute,
            0,
        )
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

private fun formatDate(millis: Long): String {
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
    format.timeZone = TimeZone.getTimeZone("UTC")
    return format.format(Date(millis))
}

/** 金额卡里的小标签。 */
@Composable
private fun AmountChip(text: String, highlight: Boolean = false) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (highlight) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = if (highlight) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            maxLines = 1,
        )
    }
}