package com.ve.bookkeeping.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import android.app.Activity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.ve.bookkeeping.data.BiometricUnlock
import com.ve.bookkeeping.data.LockConfig
import com.ve.bookkeeping.data.LockSettings
import com.ve.bookkeeping.data.LogFilter
import com.ve.bookkeeping.data.LockType
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.ve.bookkeeping.data.AppLog
import com.ve.bookkeeping.data.AppSettings
import com.ve.bookkeeping.data.AppearanceSettings
import com.ve.bookkeeping.data.ChangelogGroups
import com.ve.bookkeeping.data.CsvCodec
import com.ve.bookkeeping.data.NotificationHelper
import com.ve.bookkeeping.data.LedgerSnapshot
import com.ve.bookkeeping.data.LedgerStore
import com.ve.bookkeeping.data.UiState
import com.ve.bookkeeping.data.UiPrefs
import com.ve.bookkeeping.data.UpdateChecker
import com.ve.bookkeeping.data.UpdateInfo
import com.ve.bookkeeping.data.UpdateResult
import com.ve.bookkeeping.data.WebDavClient
import com.ve.bookkeeping.data.WebDavConfig
import com.ve.bookkeeping.data.WebDavResult
import com.ve.bookkeeping.data.WebDavSettings
import com.ve.bookkeeping.ui.components.AccountActionSheet
import com.ve.bookkeeping.ui.components.AdjustBalanceSheet
import com.ve.bookkeeping.ui.components.LogCenterSheet
import com.ve.bookkeeping.ui.components.BalanceAdjustMode
import com.ve.bookkeeping.ui.components.AppBackgroundLayer
import com.ve.bookkeeping.ui.components.builtInBackgroundThemes
import com.ve.bookkeeping.ui.components.AppearanceSheet
import com.ve.bookkeeping.ui.components.CUSTOM_BACKGROUND_INDEX
import com.ve.bookkeeping.ui.components.ChangelogSheet
import com.ve.bookkeeping.ui.components.CrashLogSheet
import com.ve.bookkeeping.ui.components.DataTransferSheet
import com.ve.bookkeeping.ui.components.LockScreen
import com.ve.bookkeeping.ui.components.NotificationSheet
import com.ve.bookkeeping.ui.components.ProRequiredDialog
import com.ve.bookkeeping.ui.components.RecordActionSheet
import com.ve.bookkeeping.ui.components.UpdateSheet
import com.ve.bookkeeping.ui.components.WebDavSheet
import com.ve.bookkeeping.ui.components.todayStamp
import com.ve.bookkeeping.ui.components.formatMoney
import com.ve.bookkeeping.ui.model.AccountOption
import com.ve.bookkeeping.ui.model.accountBalances
import com.ve.bookkeeping.ui.model.AccountType
import com.ve.bookkeeping.ui.model.BudgetConfig
import com.ve.bookkeeping.ui.model.EntryCategory
import com.ve.bookkeeping.ui.model.EntryType
import com.ve.bookkeeping.ui.model.LedgerRecord
import com.ve.bookkeeping.ui.model.RecordFlag
import com.ve.bookkeeping.ui.model.collectDueDates
import com.ve.bookkeeping.ui.pages.BudgetPage
import com.ve.bookkeeping.ui.pages.CategoryManagePage
import com.ve.bookkeeping.ui.pages.CreateAccountPage
import com.ve.bookkeeping.ui.pages.EntryPage
import com.ve.bookkeeping.ui.pages.ProPage
import com.ve.bookkeeping.ui.pages.RatesPage
import com.ve.bookkeeping.ui.pages.RecordsPage
import com.ve.bookkeeping.ui.pages.RecurringPage
import com.ve.bookkeeping.ui.pages.SecurityPage
import com.ve.bookkeeping.ui.theme.ThemePalette
import com.ve.bookkeeping.ui.theme.ZheErGenBookkeepingTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.snapshotFlow
import java.io.File
import java.util.Calendar
import com.ve.bookkeeping.ui.model.budgetProgress
import com.ve.bookkeeping.ui.pages.TrashPage
import com.ve.bookkeeping.ui.pages.AboutPage
import com.ve.bookkeeping.ui.pages.PrivacyPage
import com.ve.bookkeeping.ui.pages.LicensesPage
import com.ve.bookkeeping.ui.pages.LoansPage
import com.ve.bookkeeping.ui.model.installmentDate
import androidx.compose.animation.animateColorAsState
import com.ve.bookkeeping.ui.pages.DownloaderPage
import com.ve.bookkeeping.ui.pages.FeedbackPage
import com.ve.bookkeeping.ui.components.AccountIconPickerDialog
import androidx.compose.ui.draw.clipToBounds
import com.ve.bookkeeping.ui.pages.TagsPage
import com.ve.bookkeeping.ui.pages.DebtsPage

/** 应用内的页面路由。 */
sealed interface AppRoute {
    data object Main : AppRoute
    data class Entry(val amount: Double, val editingId: Long? = null) : AppRoute
    data class CreateAccount(
        val type: AccountType,
        val editingAccountId: String? = null,
    ) : AppRoute

    data object CategoryManage : AppRoute

    data object Pro : AppRoute

    data object Recurring : AppRoute

    data object Records : AppRoute

    data object Budget : AppRoute

    data object Security : AppRoute

    data object Trash : AppRoute

    data object About : AppRoute

    data object Privacy : AppRoute

    data object Licenses : AppRoute

    data object Loans : AppRoute

    data object Downloader : AppRoute

    data object WebDav : AppRoute

    data object Tags : AppRoute

    data object Rates : AppRoute

    data object Debts : AppRoute

    data object Feedback : AppRoute
}

/**
 * 应用根：外观（主题色 / 背景图）在最外层生效，里面是页面路由与全局数据。
 */
@Composable
fun ZheErGenApp(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val appSettings = remember { AppSettings(context) }
    var appearance by remember { mutableStateOf(appSettings.load()) }
    val uiPrefs = remember { UiPrefs(context) }
    LaunchedEffect(Unit) {
        UiState.themeMode = uiPrefs.themeMode
        UiState.fontScale = uiPrefs.fontScale
        UiState.materialYou = uiPrefs.materialYou
        UiState.privacyMode = uiPrefs.privacyMode
    }
    val dark = when (UiState.themeMode) {
        UiPrefs.MODE_DARK -> true
        UiPrefs.MODE_LIGHT -> false
        else -> isSystemInDarkTheme()
    }

    // 关掉 PRO 时立刻收回它的权益：自选主题色 / 自选背景 / 关闭自检
    fun sanitizeForFree(settings: AppearanceSettings): AppearanceSettings {
        if (settings.proEnabled) return settings
        val customBackground = settings.backgroundIndex == CUSTOM_BACKGROUND_INDEX
        return settings.copy(
            themeId = if (settings.themeId == ThemePalette.ID_CUSTOM) {
                ThemePalette.ID_DEFAULT
            } else {
                settings.themeId
            },
            backgroundIndex = if (customBackground) 0 else settings.backgroundIndex,
            backgroundEnabled = if (customBackground) false else settings.backgroundEnabled,
            customBackgroundPath = if (customBackground) null else settings.customBackgroundPath,
            autoUpdateCheck = true,
        ).also {
            // 关掉 PRO 时一并收回测试版通道
            com.ve.bookkeeping.data.UiState.betaUpdates = false
            com.ve.bookkeeping.data.UiPrefs(context).betaUpdates = false
        }
    }

    // 渲染层也兜一道：即使本地还残留 PRO 配置，非 PRO 也不会真的用上
    val effectiveTheme = when {
        !appearance.proEnabled && appearance.themeId == ThemePalette.ID_CUSTOM -> ThemePalette.Default
        else -> ThemePalette.resolve(appearance.themeId, appearance.customColorArgb)
    }
    val useCustomBackground = appearance.proEnabled &&
        appearance.backgroundIndex == CUSTOM_BACKGROUND_INDEX

    // 主题色动画：切换主题时颜色平滑过渡，而不是瞬间跳变
    val animatedPrimary by animateColorAsState(
        targetValue = effectiveTheme.primary,
        animationSpec = tween(320),
        label = "themePrimary",
    )
    val animatedPalette = remember(effectiveTheme, animatedPrimary) {
        effectiveTheme.copy(primary = animatedPrimary)
    }

    ZheErGenBookkeepingTheme(
        darkTheme = dark,
        dynamicColor = false,
        // 主题色渐变过渡：换色时 320ms 内平滑过渡
        themeColor = animatedPalette,
        backgroundImage = appearance.backgroundEnabled,
    ) {
        Box(modifier = modifier.fillMaxSize()) {
            AppBackgroundLayer(
                enabled = appearance.backgroundEnabled && (appearance.proEnabled || !useCustomBackground),
                backgroundIndex = appearance.backgroundIndex,
                customPath = if (useCustomBackground) appearance.customBackgroundPath else null,
                dark = dark,
            )
            AppContent(
                appearance = appearance,
                onSettingsChange = { next ->
                    val sanitized = sanitizeForFree(next)
                    appearance = sanitized
                    appSettings.saveTheme(sanitized.themeId, sanitized.customColorArgb)
                    appSettings.saveBackground(
                        sanitized.backgroundEnabled,
                        sanitized.backgroundIndex,
                        sanitized.customBackgroundPath,
                    )
                    appSettings.savePro(sanitized.proEnabled)
                    appSettings.saveAutoUpdate(sanitized.autoUpdateCheck)
                    appSettings.saveNotifications(sanitized.notificationsEnabled)
                    appSettings.saveReminder(
                        sanitized.reminderEnabled,
                        sanitized.reminderHour,
                        sanitized.reminderMinute,
                    )
                    appSettings.saveLockOnBackground(sanitized.lockOnBackground)
                },
            )
        }
    }
}

@Composable
private fun AppContent(
    appearance: AppearanceSettings,
    onSettingsChange: (AppearanceSettings) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val store = remember { LedgerStore(context) }
    val webDavSettings = remember { WebDavSettings(context) }
    val snapshot = remember { store.loadOrReset() }
    val noticeClient = remember { com.ve.bookkeeping.data.NoticeClient(context) }
    var notice by remember { mutableStateOf(noticeClient.cached()) }

    val appVersion = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "1.0.0"
    }
    val betaChannel = appearance.proEnabled && com.ve.bookkeeping.data.UiState.betaUpdates
    val updateChecker = remember(betaChannel) {
        UpdateChecker(if (betaChannel) UpdateChecker.CHANGELOG_BETA_URLS else UpdateChecker.CHANGELOG_URLS)
    }
    val updateScope = rememberCoroutineScope()
    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
    var updateStatus by remember { mutableStateOf<String?>(null) }
    var showUpdateSheet by remember { mutableStateOf(false) }
    var downloadUrl by remember { mutableStateOf("") }
    var downloadVersion by remember { mutableStateOf("") }
    var checkingUpdate by remember { mutableStateOf(false) }
    var pendingUpdateBackup by remember { mutableStateOf<String?>(null) }

    // 更新日志（只读）
    var showChangelog by remember { mutableStateOf(false) }
    var changelogGroups by remember {
        mutableStateOf(ChangelogGroups(emptyList(), null, emptyList()))
    }
    var changelogLoading by remember { mutableStateOf(false) }
    var changelogError by remember { mutableStateOf<String?>(null) }

    fun openChangelog() {
        showChangelog = true
        changelogLoading = true
        changelogError = null
        updateScope.launch {
            val releases = withContext(Dispatchers.IO) { updateChecker.fetchReleases() }
            changelogLoading = false
            if (releases.isEmpty()) {
                changelogError = "暂时读不到更新日志，请检查网络后重试"
            } else {
                changelogGroups = updateChecker.classify(releases, appVersion)
            }
        }
    }

    fun checkUpdate(manual: Boolean) {
        if (checkingUpdate) return
        checkingUpdate = true
        if (manual) updateStatus = "检查中…"
        updateScope.launch {
            val result = withContext(Dispatchers.IO) { updateChecker.check(appVersion, force = manual) }
            checkingUpdate = false
            when (result) {
                is UpdateResult.Newer -> {
                    updateInfo = result.info
                    showUpdateSheet = true
                    updateStatus = "发现新版本 v${result.info.version}"
                    // 交给 AppContent 去做「更新前备份」
                    pendingUpdateBackup = result.info.version
                }

                UpdateResult.UpToDate -> if (manual) updateStatus = "已是最新版"
                is UpdateResult.Failure -> {
                    updateStatus = if (manual) "检查失败：${result.message}" else null
                }
            }
        }
    }

    // 启动自检放在锁屏状态之后（未解锁前不跑）
    var webDavConfig by remember { mutableStateOf(webDavSettings.load()) }
    var lastBackupDay by remember { mutableStateOf(webDavSettings.lastBackupDay()) }

    var routeKey by rememberSaveable { mutableStateOf("main") }
    var entryAmount by rememberSaveable { mutableDoubleStateOf(0.0) }
    var editingId by rememberSaveable { mutableLongStateOf(NO_EDITING) }
    var createTypeOrdinal by rememberSaveable { mutableIntStateOf(-1) }
    var editingAccountId by rememberSaveable { mutableStateOf("") }
    var mainTab by rememberSaveable { mutableIntStateOf(0) }
    // 进入二级页面前记下所在的 tab，返回时回到这一页，而不是固定跳首页
    var routeOriginTab by rememberSaveable { mutableIntStateOf(0) }
    var prevRouteKey by remember { mutableStateOf("main") }
    LaunchedEffect(routeKey) {
        val from = prevRouteKey
        prevRouteKey = routeKey
        if (from == "main" && routeKey != "main") {
            routeOriginTab = mainTab
        }
    }

    /** 返回上一级：回到进入子页面时所在的 tab（而不是固定跳首页）。 */
    fun backToMain() {
        mainTab = routeOriginTab
        routeKey = "main"
    }

    // 记录页面跳转，排查「返回跳错页」时能看出从哪来、去哪
    LaunchedEffect(routeKey) {
        AppLog.info("页面", "跳转 route=$routeKey fromTab=$routeOriginTab 当前tab=$mainTab")
    }

    val route: AppRoute = when {
        routeKey == "entry" ->
            AppRoute.Entry(entryAmount, editingId.takeIf { it != NO_EDITING })

        routeKey == "create" && createTypeOrdinal in AccountType.entries.indices ->
            AppRoute.CreateAccount(
                type = AccountType.entries[createTypeOrdinal],
                editingAccountId = editingAccountId.ifBlank { null },
            )

        routeKey == "categories" -> AppRoute.CategoryManage
        routeKey == "pro" -> AppRoute.Pro
        routeKey == "recurring" -> AppRoute.Recurring

        routeKey == "records" -> AppRoute.Records

        routeKey == "budget" -> AppRoute.Budget

        routeKey == "security" -> AppRoute.Security

        routeKey == "trash" -> AppRoute.Trash

        routeKey == "about" -> AppRoute.About

        routeKey == "privacy" -> AppRoute.Privacy

        routeKey == "licenses" -> AppRoute.Licenses

        routeKey == "loans" -> AppRoute.Loans

        routeKey == "downloader" -> AppRoute.Downloader

        routeKey == "webdav" -> AppRoute.WebDav

        routeKey == "tags" -> AppRoute.Tags

        routeKey == "rates" -> AppRoute.Rates

        routeKey == "debts" -> AppRoute.Debts

        routeKey == "feedback" -> AppRoute.Feedback
        else -> AppRoute.Main
    }

    val books = remember {
        mutableStateListOf(*(snapshot?.books ?: DemoData.ledgerBooks).toTypedArray())
    }
    var selectedBook by rememberSaveable {
        mutableStateOf(snapshot?.selectedBook ?: DemoData.ledgerBooks.first())
    }

    val records = remember {
        mutableStateListOf(*(ensureUniqueIds(snapshot?.records ?: emptyList())).toTypedArray())
    }
    val accounts = remember {
        mutableStateListOf(*(snapshot?.accounts ?: emptyList()).toTypedArray())
    }

    val settingsStore = remember { AppSettings(context) }
    var defaultAccountId by remember { mutableStateOf(settingsStore.load().defaultAccountId) }
    var lastAccountId by remember { mutableStateOf(settingsStore.load().lastAccountId) }

    val recurringRules = remember {
        mutableStateListOf(*(snapshot?.recurringRules ?: emptyList()).toTypedArray())
    }

    val debts = remember {
        mutableStateListOf(*(snapshot?.debts ?: emptyList()).toTypedArray())
    }

    val loans = remember {
        mutableStateListOf(*(snapshot?.loans ?: emptyList()).toTypedArray())
    }

    val trash = remember {
        mutableStateListOf(*(ensureUniqueIds(snapshot?.trash ?: emptyList())).toTypedArray())
    }

    // 标签清单放进 SnapshotStateList：新增/删除立刻反映到记账页与标签页
    val tagList = remember {
        mutableStateListOf<String>().apply {
            addAll(com.ve.bookkeeping.data.TagStore(context).all())
        }
    }
    val allTags: List<String> = (tagList + records.flatMap { it.tags }).distinct().sorted()

    var budget by remember { mutableStateOf(snapshot?.budget ?: BudgetConfig()) }
    var lastBudgetNoticeDay by remember { mutableStateOf<String?>(null) }
    // 统计页点标签 → 记录页带上这个标签筛选
    var pendingTagFilter by remember { mutableStateOf<String?>(null) }

    var backupSchedule by remember { mutableStateOf(webDavSettings.schedule()) }
    var backupIntervalHours by remember { mutableIntStateOf(webDavSettings.intervalHours()) }
    var showTransfer by remember { mutableStateOf(false) }
    var transferBusy by remember { mutableStateOf(false) }
    var transferStatus by remember { mutableStateOf<String?>(null) }
    var showNotifications by remember { mutableStateOf(false) }

    /* 崩溃日志 */
    var showCrashLog by remember { mutableStateOf(false) }
    var logFilter by remember { mutableStateOf(LogFilter.All) }
    // 清空/刷新日志时 +1，触发重新读取
    var logRevision by remember { mutableIntStateOf(0) }

    /* 锁屏：数字密码 / 手势 / 指纹 */
    val lockSettings = remember { LockSettings(context) }
    var lockConfig by remember { mutableStateOf(lockSettings.load()) }
    var unlocked by remember { mutableStateOf(!lockConfig.enabled) }
    val activity = context as? Activity
    val biometricAvailable = remember(activity) {
        activity?.let { BiometricUnlock.isAvailable(it) } ?: false
    }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            // 直接读落盘配置，避免闭包拿到旧值
            if (event == Lifecycle.Event.ON_STOP) {
                val cfg = lockSettings.load()
                if (cfg.enabled && AppSettings(context).load().lockOnBackground) {
                    unlocked = false
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // 启动自检：PRO 用户可以在 PRO 页关掉；未解锁前不跑
    LaunchedEffect(unlocked) {
        if (!unlocked) return@LaunchedEffect
        if (appearance.autoUpdateCheck) checkUpdate(manual = false)
    }

    fun requestBiometricSetup() {
        val act = activity ?: return
        BiometricUnlock.show(
            activity = act,
            onSuccess = {
                lockSettings.setBiometric(true)
                lockConfig = lockSettings.load()
            },
            onError = { },
        )
    }

    fun requestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) return
        val act = activity ?: return
        runCatching {
            androidx.core.app.ActivityCompat.requestPermissions(
                act,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                1002,
            )
        }
    }

    /* 导出 / 导入 CSV：用系统文件选择器，不申请存储权限 */
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv"),
    ) { uri ->
        if (uri == null) {
            transferBusy = false
            transferStatus = "已取消导出"
            return@rememberLauncherForActivityResult
        }
        transferBusy = true
        runCatching {
            val text = CsvCodec.encode(records.toList())
            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(text.toByteArray(Charsets.UTF_8))
            }
            transferStatus = "✓ 已导出 ${records.size} 条账目"
        }.onFailure {
            transferStatus = "✕ 导出失败：${it.message ?: "无法写入文件"}"
        }
        transferBusy = false
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) {
            transferBusy = false
            transferStatus = "已取消导入"
            return@rememberLauncherForActivityResult
        }
        transferBusy = true
        runCatching {
            val text = context.contentResolver.openInputStream(uri)?.use {
                it.readBytes().toString(Charsets.UTF_8)
            }.orEmpty()
            val imported = CsvCodec.decode(text, DemoData.currencies)
            val startId = (records.maxOfOrNull { it.id } ?: 0L) + 1
            imported.forEachIndexed { index, record ->
                records.add(record.copy(id = startId + index))
            }
            transferStatus = if (imported.isEmpty()) {
                "✕ 没读到有效记录，检查 CSV 列顺序"
            } else {
                "✓ 已导入 ${imported.size} 条账目"
            }
        }.onFailure {
            transferStatus = "✕ 导入失败：${it.message ?: "文件读取失败"}"
        }
        transferBusy = false
    }

    fun customOf(type: EntryType) = snapshot?.customCategories
        ?.filter { it.first == type }
        ?.map { it.second }
        .orEmpty()

    val expenseCategories = remember {
        mutableStateListOf(*(customOf(EntryType.Expense) + DemoData.expenseCategories).toTypedArray())
    }
    val incomeCategories = remember {
        mutableStateListOf(*(customOf(EntryType.Income) + DemoData.incomeCategories).toTypedArray())
    }
    val transferCategories = remember {
        mutableStateListOf(*(customOf(EntryType.Transfer) + DemoData.transferCategories).toTypedArray())
    }
    var customCategorySeq by remember {
        mutableIntStateOf(
            expenseCategories.count { it.isCustom } +
                incomeCategories.count { it.isCustom } +
                transferCategories.count { it.isCustom },
        )
    }

    fun categoriesOf(type: EntryType) = when (type) {
        EntryType.Expense -> expenseCategories
        EntryType.Income -> incomeCategories
        EntryType.Transfer -> transferCategories
    }

    fun addCategory(type: EntryType, label: String, parentId: String?): EntryCategory {
        customCategorySeq += 1
        val created = DemoData.createCustomCategory(type, label, customCategorySeq)
            .copy(parentId = parentId)
        val list = categoriesOf(type)
        if (parentId == null) list.add(0, created) else list.add(created)
        return created
    }

    fun deleteCategory(type: EntryType, id: String) {
        val list = categoriesOf(type)
        list.removeAll { it.id == id || it.parentId == id }
    }

    var actionRecord by remember { mutableStateOf<LedgerRecord?>(null) }
    var actionAccount by remember { mutableStateOf<AccountOption?>(null) }
    var adjustAccount by remember { mutableStateOf<AccountOption?>(null) }
    var iconEditAccount by remember { mutableStateOf<AccountOption?>(null) }
    var showAppearance by remember { mutableStateOf(false) }
    var showWebDavProGate by remember { mutableStateOf(false) }
    val webDavClient = remember { WebDavClient() }

    /** 备份成功后把附件图片也传到云端 attachments/ 目录。 */
    fun uploadAttachments(config: WebDavConfig) {
        runCatching {
            val dir = java.io.File(context.filesDir, "attachments")
            if (!dir.exists()) return@runCatching
            records.flatMap { it.attachments }.distinct().forEach { name ->
                val file = java.io.File(dir, name)
                if (file.exists()) {
                    runCatching { webDavClient.uploadAttachment(config, name, file.readBytes()) }
                }
            }
        }
    }


    fun customCategoryPairs(): List<Pair<EntryType, EntryCategory>> = buildList {
        expenseCategories.filter { it.isCustom }.forEach { add(EntryType.Expense to it) }
        incomeCategories.filter { it.isCustom }.forEach { add(EntryType.Income to it) }
        transferCategories.filter { it.isCustom }.forEach { add(EntryType.Transfer to it) }
    }

    fun currentSnapshot() = LedgerSnapshot(
        records = records.toList(),
        accounts = accounts.toList(),
        books = books.toList(),
        selectedBook = selectedBook,
        customCategories = customCategoryPairs(),
        recurringRules = recurringRules.toList(),
        budget = budget,
        trash = trash.toList(),
        loans = loans.toList(),
        debts = debts.toList(),
    )

    // 本地持久化：任何变化立刻落盘；关键操作（记账/删除/恢复）再用 persistNow 同步写一次，
    // 否则「记完马上清后台」会丢掉最后一笔（之前 250ms 防抖留下的漏洞）。
    LaunchedEffect(Unit) {
        snapshotFlow { currentSnapshot() }
            .collect { snap ->
                withContext(Dispatchers.IO) { runCatching { store.save(snap) } }
            }
    }

    /** 同步写盘：用于用户刚做完、不能丢的操作。 */
    fun persistNow() {
        runCatching { store.save(currentSnapshot()) }
    }

    // 切到后台 / 锁屏前再兜一次盘，MIUI 这类系统可能随时冻结进程
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) persistNow()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    /**
     * 记账时默认用哪个账户：
     * 1. 资产页设了默认付款账户 → 用它；
     * 2. 否则用上次记账的账户；
     * 3. 都没有就用第一个账户。
     */
    // 当前余额 = 初始余额 + 账目影响；对外一律用这一份，避免和资产页对不上
    fun liveAccounts(): List<AccountOption> = accountBalances(records, accounts)

    fun preferredAccount(): AccountOption {
        val live = liveAccounts()
        val byDefault = live.firstOrNull { it.id == defaultAccountId }
        val byLast = live.firstOrNull { it.id == lastAccountId }
        return byDefault ?: byLast ?: live.firstOrNull() ?: DemoData.NoAccount
    }

    // 周期记账：启动时把错过的日期补记上
    LaunchedEffect(Unit) {
        val now = System.currentTimeMillis()
        val generated = mutableListOf<LedgerRecord>()
        var nextId = records.maxOfOrNull { it.id } ?: 0L
        var changed = false

        val updated = recurringRules.map { rule ->
            if (!rule.enabled) return@map rule
            val (dates, next) = collectDueDates(rule, now)
            if (dates.isEmpty()) return@map rule

            val category = categoriesOf(rule.type).firstOrNull { it.id == rule.categoryId }
                ?: EntryCategory(rule.categoryId, rule.categoryLabel, Icons.Rounded.LocalOffer)
            val account = accounts.firstOrNull { it.id == rule.accountId }
                ?: AccountOption(rule.accountId, rule.accountName, 0.0, Icons.Rounded.Wallet)

            dates.forEach { date ->
                nextId += 1
                generated.add(
                    LedgerRecord(
                        id = nextId,
                        bookName = selectedBook,
                        type = rule.type,
                        amount = rule.amount,
                        category = category,
                        fromAccount = account,
                        toAccount = null,
                        fee = 0.0,
                        note = rule.note,
                        currency = DemoData.currencies.first(),
                        occurredAt = date + 9 * 60 * 60 * 1000L,
                    ),
                )
            }
            changed = true
            rule.copy(nextDueAt = next)
        }

        if (generated.isNotEmpty()) records.addAll(0, generated)
        if (changed) {
            recurringRules.clear()
            recurringRules.addAll(updated)
        }
        if (generated.isNotEmpty() && appearance.notificationsEnabled) {
            NotificationHelper.notify(
                context = context,
                id = NOTIFY_RECURRING,
                channel = NotificationHelper.CHANNEL_EVENT,
                title = "已补记 ${generated.size} 笔周期账目",
                text = generated.joinToString("、") {
                    "${it.category.label} ¥${formatMoney(it.amount)}"
                }.take(120),
            )
        }
    }

    // 还款提醒（PRO）：按信用卡账户的还款日排程
    LaunchedEffect(accounts.toList(), appearance.proEnabled, appearance.notificationsEnabled) {
        NotificationHelper.cancelRepayReminders(context)
        if (appearance.proEnabled && appearance.notificationsEnabled) {
            accounts
                .filter { it.type == AccountType.Credit && it.repayDay != null }
                .forEach { account ->
                    NotificationHelper.scheduleRepayReminder(
                        context = context,
                        accountName = account.name,
                        repayDay = account.repayDay ?: 1,
                    )
                }
        }
    }

    // 通知：建通道 + 按设置排程每日提醒
    LaunchedEffect(appearance.reminderEnabled, appearance.reminderHour, appearance.reminderMinute) {
        NotificationHelper.ensureChannels(context)
        if (appearance.reminderEnabled && appearance.notificationsEnabled) {
            NotificationHelper.scheduleDailyReminder(
                context,
                appearance.reminderHour,
                appearance.reminderMinute,
            )
        } else {
            NotificationHelper.cancelDailyReminder(context)
        }
    }

    // 汇率：先读缓存，过期则后台刷新（24h）
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            com.ve.bookkeeping.data.ExchangeRates.loadCached(context)
            if (com.ve.bookkeeping.data.ExchangeRates.shouldRefresh(context)) {
                com.ve.bookkeeping.data.ExchangeRates.refresh(context)
            }
        }
    }

    // 拉取首页公告（公开仓库 notice.json，改文件即可远程开关）
    LaunchedEffect(Unit) {
        val fetched = withContext(Dispatchers.IO) { noticeClient.fetch() }
        if (fetched != null) notice = fetched
    }
    // 每天首次启动时自动备份一次（已配置且今天还没备份过；未解锁前不跑）
    LaunchedEffect(unlocked) {
        if (!unlocked) return@LaunchedEffect
        val cfg = webDavConfig
        val today = todayStamp()
        // 自动备份周期：每次启动 / 每 N 小时 / 每天一次
        val now = System.currentTimeMillis()
        val due = when (webDavSettings.schedule()) {
            "startup" -> true
            "hours" -> now - webDavSettings.lastBackupAt() >=
                webDavSettings.intervalHours() * 60L * 60L * 1000L
            else -> lastBackupDay != today
        }
        if (cfg.isConfigured && due) {
            val json = store.encode(currentSnapshot())
            // 增量：内容指纹没变就不重复上传
            val hash = java.security.MessageDigest.getInstance("SHA-256")
                .digest(json.toByteArray())
                .joinToString("") { "%02x".format(it) }
            if (hash == webDavSettings.lastBackupHash()) {
                webDavSettings.markBackupDone(today)
                lastBackupDay = today
                return@LaunchedEffect
            }
            val result = withContext(Dispatchers.IO) {
                webDavClient.backup(cfg, json)
            }
            if (result is WebDavResult.Success) {
                webDavSettings.markBackupDone(today)
                webDavSettings.markBackupHash(hash)
                lastBackupDay = today
                uploadAttachments(cfg)
            }
        }
    }

    // 检测到新版本：更新前先备份一次（云备份页可关闭）
    LaunchedEffect(pendingUpdateBackup) {
        val version = pendingUpdateBackup ?: return@LaunchedEffect
        val cfg = webDavConfig
        if (!webDavSettings.backupBeforeUpdate() || !cfg.isConfigured) {
            return@LaunchedEffect
        }
        runCatching {
            val json = store.encode(currentSnapshot())
            val result = withContext(Dispatchers.IO) { webDavClient.backup(cfg, json) }
            if (result is WebDavResult.Success) {
                webDavSettings.markBackupDone(todayStamp())
                lastBackupDay = todayStamp()
                withContext(Dispatchers.IO) { uploadAttachments(cfg) }
            }
        }
    }
    val locked = lockConfig.enabled && !unlocked

    // 平板 / 横屏：内容居中并限制在 600dp 阅读宽度，避免拉伸
    // 统一尺寸分级：手机 / 横屏 / 平板，内容宽度按档位限制，不再写死 600dp
    val layoutMode = rememberLayoutMode()
    val screenWidthDp = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp
    val contentMaxWidthDp = layoutMode.contentMaxWidthDp()
    val sideInset = if (contentMaxWidthDp > 0 && screenWidthDp > contentMaxWidthDp) {
        ((screenWidthDp - contentMaxWidthDp) / 2).dp
    } else {
        0.dp
    }

    // 所有路由页统一铺一层主题底色：否则深色模式下会露出窗口的白色背景
    androidx.compose.material3.Surface(
        modifier = modifier.fillMaxSize(),
        color = androidx.compose.material3.MaterialTheme.colorScheme.background,
    ) {
    AnimatedContent(
        targetState = route,
        // 未解锁时把整个界面模糊掉，避免看到账目
        modifier = if (locked) {
            Modifier.blur(22.dp).padding(horizontal = sideInset).clipToBounds()
        } else {
            Modifier.padding(horizontal = sideInset).clipToBounds()
        },
        transitionSpec = {
            val enteringMain = targetState is AppRoute.Main
            if (enteringMain) {
                (slideInHorizontally(animationSpec = tween(320)) { -it / 5 } + fadeIn(tween(320)))
                    .togetherWith(
                        slideOutHorizontally(animationSpec = tween(320)) { it } + fadeOut(tween(200)),
                    )
            } else {
                (slideInHorizontally(animationSpec = tween(320)) { it } + fadeIn(tween(320)))
                    .togetherWith(
                        slideOutHorizontally(animationSpec = tween(320)) { -it / 5 } + fadeOut(tween(200)),
                    )
            }
        },
        label = "appRoute",
    ) { current ->
        when (current) {
            AppRoute.Main -> MainScreen(
                books = books,
                selectedBook = selectedBook,
                records = records,
                accounts = accounts,
                debts = debts,
                defaultAccountId = defaultAccountId,
                categoryCount = expenseCategories.size + incomeCategories.size + transferCategories.size,
                recurringCount = recurringRules.size,
                initialPage = mainTab,
                onSelectBook = { selectedBook = it },
                onAddBook = { name ->
                    books.add(name)
                    selectedBook = name
                },
                onStartEntry = { amount ->
                    entryAmount = amount
                    editingId = NO_EDITING
                    mainTab = 0
                    routeKey = "entry"
                },
                onCreateAccount = { type ->
                    createTypeOrdinal = type.ordinal
                    editingAccountId = ""
                    routeKey = "create"
                },
                onRecordClick = { actionRecord = it },
                onAccountClick = { actionAccount = it },
                onOpenCategories = { routeKey = "categories" },
                onOpenWebDav = {
                    if (appearance.proEnabled) {
                        routeKey = "webdav"
                    } else {
                        showWebDavProGate = true
                    }
                },
                onOpenAppearance = { showAppearance = true },
                themeModeLabel = when (UiState.themeMode) {
                    UiPrefs.MODE_LIGHT -> "浅色"
                    UiPrefs.MODE_DARK -> "深色"
                    else -> "跟随系统"
                },
                onCycleThemeMode = {
                    UiState.themeMode = UiPrefs(context).cycleThemeMode()
                },
                fontSizeLabel = when (UiState.fontScale) {
                    UiPrefs.FONT_SMALL -> "小"
                    UiPrefs.FONT_LARGE -> "大"
                    UiPrefs.FONT_XLARGE -> "特大"
                    else -> "标准"
                },
                materialYouLabel = if (UiState.materialYou) "已开启 · Pro" else "跟随壁纸（Pro）",
                onToggleMaterialYou = {
                    if (!appearance.proEnabled) {
                        mainTab = 3
                        routeKey = "pro"
                    } else {
                        val prefs = UiPrefs(context)
                        prefs.materialYou = !prefs.materialYou
                        UiState.materialYou = prefs.materialYou
                    }
                },
                privacyLabel = if (UiState.privacyMode) "已开启（金额打码）" else "关闭",
                onTogglePrivacy = {
                    val prefs = UiPrefs(context)
                    prefs.privacyMode = !prefs.privacyMode
                    UiState.privacyMode = prefs.privacyMode
                },
                onCycleFontScale = {
                    val prefs = UiPrefs(context)
                    val next = when (prefs.fontScale) {
                        UiPrefs.FONT_SMALL -> UiPrefs.FONT_NORMAL
                        UiPrefs.FONT_NORMAL -> UiPrefs.FONT_LARGE
                        UiPrefs.FONT_LARGE -> UiPrefs.FONT_XLARGE
                        else -> UiPrefs.FONT_SMALL
                    }
                    prefs.fontScale = next
                    UiState.fontScale = next
                },
                onOpenPro = { routeKey = "pro" },
                onOpenRecurring = { routeKey = "recurring" },
                onOpenRecords = { routeKey = "records" },
                onOpenRecordsWithTag = { tag ->
                    pendingTagFilter = tag
                    routeKey = "records"
                },
                onOpenBudget = {
                    if (appearance.proEnabled) {
                        routeKey = "budget"
                    } else {
                        mainTab = 3
                        routeKey = "pro"
                    }
                },
                onOpenTransfer = {
                    transferStatus = null
                    showTransfer = true
                },
                onOpenNotifications = { showNotifications = true },
                onOpenSecurity = { routeKey = "security" },
                onOpenTrash = { routeKey = "trash" },
                onOpenAbout = { routeKey = "about" },
                onOpenFeedback = { routeKey = "feedback" },
                onOpenTags = { routeKey = "tags" },
                onOpenRates = { routeKey = "rates" },
                onOpenDebts = { routeKey = "debts" },
                noticeText = if (notice.enabled) notice.text else "",
                onNoticeClick = {
                    if (notice.link.isNotBlank()) {
                        runCatching {
                            context.startActivity(
                                android.content.Intent(
                                    android.content.Intent.ACTION_VIEW,
                                    android.net.Uri.parse(notice.link),
                                ),
                            )
                        }
                    }
                },
                avatarPath = UiState.avatarPath,
                userName = UiState.userName,
                userBio = UiState.userBio,
                onSaveProfile = { name, bio, path ->
                    AppLog.info("资料", "写入 userName=$name userBio=$bio avatar=$path")
                    val prefs = UiPrefs(context)
                    prefs.userName = name
                    prefs.userBio = bio
                    if (path.isNotBlank()) prefs.avatarPath = path
                    UiState.userName = name
                    UiState.userBio = bio
                    UiState.avatarPath = path
                },
                onAvatarPicked = { path ->
                    UiPrefs(context).avatarPath = path
                    UiState.avatarPath = path
                },
                onOpenLoans = {
                    if (appearance.proEnabled) {
                        routeKey = "loans"
                    } else {
                        mainTab = 3
                        routeKey = "pro"
                    }
                },
                onOpenCrashLog = {
                    logFilter = LogFilter.All
                    logRevision += 1
                    showCrashLog = true
                },
                crashCount = AppLog.count(context, LogFilter.All),
                budgetSummary = if (budget.isSet) {
                    "本月预算 ¥${formatMoney(budget.monthlyLimit)}"
                } else {
                    "还没设置"
                },
                proEnabled = appearance.proEnabled,
                appVersion = appVersion,
                updateStatus = updateStatus,
                onCheckUpdate = { checkUpdate(manual = true) },
                onOpenChangelog = { openChangelog() },
                onPageChanged = { mainTab = it },
            )

            is AppRoute.Entry -> EntryPage(
                initialAmount = current.amount,
                bookName = selectedBook,
                editRecord = current.editingId?.let { id -> records.firstOrNull { it.id == id } },
                expenseCategories = expenseCategories,
                incomeCategories = incomeCategories,
                transferCategories = transferCategories,
                accounts = liveAccounts(),
                defaultAccount = preferredAccount(),
                onAddCategory = { type, name -> addCategory(type, name, null) },
                onAddSubCategory = { type, name, parentId -> addCategory(type, name, parentId) },
                availableTags = allTags,
                onAddTag = { name ->
                    val trimmed = name.trim()
                    if (trimmed.isNotEmpty()) {
                        com.ve.bookkeeping.data.TagStore(context).add(trimmed)
                        if (!tagList.contains(trimmed)) tagList.add(trimmed)
                    }
                },
                proEnabled = appearance.proEnabled,
                onNeedPro = {
                    editingId = NO_EDITING
                    routeKey = "pro"
                },
                onSave = { record ->
                    val index = records.indexOfFirst { it.id == record.id }
                    if (index >= 0) {
                        records[index] = record
                    } else {
                        // 新记录用「已有最大 id + 1」分配，避免时间戳相同/分期派生撞 id
                        val baseId = (records.maxOfOrNull { it.id } ?: 0L) + 1
                        records.addAll(0, expandInstallments(record, baseId))
                    }
                    persistNow()
                    AppLog.info(
                        "记账",
                        "保存 ${record.type.name} ¥${record.amount} ${record.category.label} 账户=${record.fromAccount.name}",
                    )
                    // 超支提醒：一旦跨过预算就发一条（同一天不重复刷）
                    runCatching {
                        if (appearance.notificationsEnabled && budget.monthlyLimit > 0) {
                            val progress = budgetProgress(budget, records.toList())
                            val today = todayStamp()
                            if (progress.overspent && lastBudgetNoticeDay != today) {
                                lastBudgetNoticeDay = today
                                NotificationHelper.notify(
                                    context = context,
                                    id = NOTIFY_BUDGET,
                                    channel = NotificationHelper.CHANNEL_EVENT,
                                    title = "预算超支",
                                    text = "本${progress.daysLeft}天剩余 — 已花 ¥${formatMoney(
                                        progress.spent,
                                    )}，超出 ¥${formatMoney(-progress.remaining)}",
                                )
                            }
                        }
                    }
                    // 记住这次用的账户，下次记账默认用它（没设默认账户时）
                    if (record.fromAccount.id != "none") {
                        lastAccountId = record.fromAccount.id
                        settingsStore.saveLastAccount(record.fromAccount.id)
                    }
                    editingId = NO_EDITING
                    backToMain()
                },
                onBack = {
                    editingId = NO_EDITING
                    backToMain()
                },
            )

            is AppRoute.CreateAccount -> CreateAccountPage(
                accountType = current.type,
                currencies = DemoData.currencies,
                editAccount = current.editingAccountId?.let { id ->
                    accounts.firstOrNull { it.id == id }
                },
                currentBalance = current.editingAccountId?.let { id ->
                    accountBalances(records, accounts).firstOrNull { it.id == id }?.balance
                },
                onSave = { account ->
                    val index = accounts.indexOfFirst { it.id == account.id }
                    if (index >= 0) accounts[index] = account else accounts.add(account)
                    backToMain()
                },
                onBack = {
                    backToMain()
                },
            )

            AppRoute.CategoryManage -> CategoryManagePage(
                expenseCategories = expenseCategories,
                incomeCategories = incomeCategories,
                transferCategories = transferCategories,
                onAddCategory = { type, label, parentId ->
                    addCategory(type, label, parentId)
                },
                onDeleteCategory = { type, id -> deleteCategory(type, id) },
                onBack = { routeKey = "main" },
            )

            AppRoute.Pro -> ProPage(
                proEnabled = appearance.proEnabled,
                autoUpdateCheck = appearance.autoUpdateCheck,
                betaUpdates = com.ve.bookkeeping.data.UiState.betaUpdates,
            onToggleBetaUpdates = { enabled ->
                com.ve.bookkeeping.data.UiPrefs(context).betaUpdates = enabled
                com.ve.bookkeeping.data.UiState.betaUpdates = enabled
            },
            onToggleAutoUpdate = { enabled ->
                    onSettingsChange(appearance.copy(autoUpdateCheck = enabled))
                },
                onActivate = {
                    onSettingsChange(appearance.copy(proEnabled = true))
                },
                onDeactivate = {
                    onSettingsChange(appearance.copy(proEnabled = false))
                },
                onBack = {
                    backToMain()
                },
            )

            AppRoute.Feedback -> FeedbackPage(
                appVersion = appVersion,
                proEnabled = appearance.proEnabled,
                onBack = {
                    backToMain()
                },
            )

            AppRoute.Debts -> DebtsPage(
                debts = debts,
                onAdd = { debts.add(0, it) },
                onToggleSettled = { debt ->
                    val index = debts.indexOfFirst { it.id == debt.id }
                    if (index >= 0) debts[index] = debts[index].copy(settled = !debts[index].settled)
                },
                onDelete = { debt -> debts.removeAll { it.id == debt.id } },
                onBack = {
                    backToMain()
                },
            )

            AppRoute.Rates -> RatesPage(
                onBack = { backToMain() },
            )

            AppRoute.Tags -> TagsPage(
                tags = allTags,
                usage = records.flatMap { it.tags }.groupingBy { it }.eachCount(),
                onAdd = { name ->
                    val trimmed = name.trim()
                    if (trimmed.isNotEmpty()) {
                        com.ve.bookkeeping.data.TagStore(context).add(trimmed)
                        if (!tagList.contains(trimmed)) tagList.add(trimmed)
                    }
                },
                onRemove = { tag ->
                    com.ve.bookkeeping.data.TagStore(context).remove(tag)
                    tagList.remove(tag)
                    records.forEachIndexed { index, record ->
                        if (record.tags.contains(tag)) {
                            records[index] = record.copy(tags = record.tags - tag)
                        }
                    }
                },
                onBack = {
                    backToMain()
                },
            )

            AppRoute.WebDav -> WebDavSheet(
                config = webDavConfig,
                lastBackupDay = lastBackupDay,
                schedule = backupSchedule,
                intervalHours = backupIntervalHours,
                backupBeforeUpdate = webDavSettings.backupBeforeUpdate(),
                onToggleBackupBeforeUpdate = { enabled ->
                    webDavSettings.saveBackupBeforeUpdate(enabled)
                },
                            onScheduleChange = { sched, hours ->
                    webDavSettings.saveSchedule(sched, hours)
                    backupSchedule = sched
                    backupIntervalHours = hours
                },
                onSave = { config: WebDavConfig ->
                    webDavConfig = config
                    webDavSettings.save(config)
                },
                onTest = { config ->
                    withContext(Dispatchers.IO) { webDavClient.test(config) }
                },
                onBackup = { config ->
                    withContext(Dispatchers.IO) {
                        val result = webDavClient.backup(config, store.encode(currentSnapshot()))
                        if (result is WebDavResult.Success) {
                            uploadAttachments(config)
                        }
                        result
                    }
                },
                onBackupDone = { day ->
                    webDavSettings.markBackupDone(day)
                    lastBackupDay = day
                },
                onListBackups = {
                    withContext(Dispatchers.IO) { webDavClient.listBackups(webDavConfig) }
                },
                onLoadBackup = { name ->
                    withContext(Dispatchers.IO) { webDavClient.download(webDavConfig, name) }
                },
                onPreviewBackup = { json ->
                    store.decode(json)?.let { it.records.size to it.accounts.size }
                },
                onApplyBackup = { json ->
                    val restored = store.decode(json)
                    if (restored == null) {
                        false
                    } else {
                        // 覆盖前先把本机数据另存一份，后悔了还能找回来
                        runCatching {
                            File(context.filesDir, "ledger.before-restore.json")
                                .writeText(store.encode(currentSnapshot()))
                        }
                        records.clear()
                        records.addAll(restored.records)
                        accounts.clear()
                        accounts.addAll(restored.accounts)
                        books.clear()
                        books.addAll(restored.books)
                        selectedBook = restored.selectedBook
                        recurringRules.clear()
                        recurringRules.addAll(restored.recurringRules)
                        // 之前漏了预算 / 回收站 / 车贷房贷 / 借入借出，恢复后对不上
                        budget = restored.budget
                        trash.clear()
                        trash.addAll(restored.trash)
                        loans.clear()
                        loans.addAll(restored.loans)
                        debts.clear()
                        debts.addAll(restored.debts)
                        // 账本列表坏了会导致首页只剩空：兜一下底
                        if (books.isEmpty()) books.add(DemoData.ledgerBooks.first())
                        if (books.none { it == selectedBook }) selectedBook = books.first()

                        // 附件：本地缺的从云端拉回来
                        runCatching {
                            val dir = File(context.filesDir, "attachments").apply { mkdirs() }
                            restored.records.flatMap { it.attachments }.distinct().forEach { name ->
                                val file = File(dir, name)
                                if (!file.exists()) {
                                    webDavClient.downloadAttachment(webDavConfig, name)?.let { bytes ->
                                        runCatching { file.writeBytes(bytes) }
                                    }
                                }
                            }
                        }

                        fun rebuild(
                            list: MutableList<EntryCategory>,
                            type: EntryType,
                            seeds: List<EntryCategory>,
                        ) {
                            list.clear()
                            list.addAll(restored.customCategories.filter { it.first == type }.map { it.second })
                            list.addAll(seeds)
                        }
                        rebuild(expenseCategories, EntryType.Expense, DemoData.expenseCategories)
                        rebuild(incomeCategories, EntryType.Income, DemoData.incomeCategories)
                        rebuild(transferCategories, EntryType.Transfer, DemoData.transferCategories)

                        defaultAccountId = null
                        settingsStore.saveDefaultAccount(null)
                        persistNow()
                        true
                    }
                } , onDismiss = { backToMain() },
            )

            AppRoute.Downloader -> DownloaderPage(
                url = downloadUrl,
                versionLabel = downloadVersion,
                onBack = {
                    backToMain()
                },
            )

            AppRoute.Loans -> LoansPage(
                loans = loans,
                onAdd = { loans.add(it) },
                onPay = { loan ->
                    val index = loans.indexOfFirst { it.id == loan.id }
                    if (index >= 0 && loan.remaining > 0) {
                        val period = loan.paidCount + 1
                        val category = expenseCategories.firstOrNull { it.id == "exp_housing" }
                            ?: expenseCategories.firstOrNull()
                        val account = preferredAccount()
                        if (category != null) {
                            records.add(
                                0,
                                LedgerRecord(
                                    id = System.currentTimeMillis(),
                                    bookName = selectedBook,
                                    type = EntryType.Expense,
                                    amount = loan.monthlyPayment,
                                    category = category,
                                    fromAccount = account,
                                    note = "${loan.name} 第 $period 期",
                                    currency = DemoData.currencies.first(),
                                    occurredAt = loan.installmentDate(period),
                                ),
                            )
                        }
                        loans[index] = loan.copy(paidCount = period)
                    }
                },
                onDelete = { loan -> loans.removeAll { it.id == loan.id } },
                onBack = {
                    backToMain()
                },
            )

            AppRoute.About -> AboutPage(
                appVersion = appVersion,
                onOpenPrivacy = { routeKey = "privacy" },
                onOpenLicenses = { routeKey = "licenses" },
                onBack = {
                    backToMain()
                },
            )

            AppRoute.Privacy -> PrivacyPage(
                onBack = { routeKey = "about" },
            )

            AppRoute.Licenses -> LicensesPage(
                onBack = { routeKey = "about" },
            )

            AppRoute.Trash -> TrashPage(
                records = trash,
                onRestore = { record ->
                    trash.removeAll { it.id == record.id }
                    // 恢复时如果 id 已被现有账目占用（历史脏数据），重新编号，避免两条 id 相同
                    val restored = if (records.any { it.id == record.id }) {
                        record.copy(id = (records.maxOfOrNull { it.id } ?: 0L) + 1)
                    } else {
                        record
                    }
                    records.add(0, restored)
                    persistNow()
                    AppLog.info("恢复", "从回收站恢复 ${restored.category.label} ¥${restored.amount}")
                },
                onDeleteForever = { record ->
                    trash.removeAll { it.id == record.id }
                    persistNow()
                },
                onClear = {
                    trash.clear()
                    persistNow()
                },
                onBack = {
                    backToMain()
                },
            )

            AppRoute.Security -> SecurityPage(
                config = lockConfig,
                hasRecovery = lockConfig.hasRecovery,
                onGenerateRecovery = { lockSettings.generateRecoveryCode().also { lockConfig = lockSettings.load() } },
                biometricAvailable = biometricAvailable,
                proEnabled = appearance.proEnabled,
                lockOnBackground = appearance.lockOnBackground,
                onToggleLockOnBackground = { enabled ->
                    onSettingsChange(appearance.copy(lockOnBackground = enabled))
                },
                onRequestBiometric = { requestBiometricSetup() },
                onSavePin = { pin ->
                    lockSettings.save(LockType.Pin, pin, lockConfig.biometric)
                    lockConfig = lockSettings.load()
                    unlocked = true
                },
                onSavePattern = { secret ->
                    lockSettings.save(LockType.Pattern, secret, lockConfig.biometric)
                    lockConfig = lockSettings.load()
                    unlocked = true
                },
                onToggleBiometric = { enabled ->
                    lockSettings.setBiometric(enabled)
                    lockConfig = lockSettings.load()
                },
                onDisable = {
                    lockSettings.clear()
                    lockConfig = lockSettings.load()
                    unlocked = true
                },
                onNeedPro = { routeKey = "pro" },
                onBack = {
                    backToMain()
                },
            )

            AppRoute.Records -> RecordsPage(
                onDeleteRecord = { record ->
                    records.removeAll { it.id == record.id }
                    trash.removeAll { it.id == record.id }
                    trash.add(0, record)
                    persistNow()
                    AppLog.info("删除", "记录 ${record.category.label} ¥${record.amount} 移入回收站")
                },
                records = records,
                accounts = accounts,
                onRecordClick = { actionRecord = it },
                initialTag = pendingTagFilter,
                onBack = {
                    pendingTagFilter = null
                    backToMain()
                },
            )

            AppRoute.Budget -> BudgetPage(
                budget = budget,
                records = records,
                expenseCategories = expenseCategories.filter { it.parentId == null },
                onSave = { next ->
                    budget = next
                    backToMain()
                },
                onBack = {
                    backToMain()
                },
            )

            AppRoute.Recurring -> RecurringPage(
                rules = recurringRules,
                accounts = accounts,
                expenseCategories = expenseCategories.filter { it.parentId == null },
                incomeCategories = incomeCategories.filter { it.parentId == null },
                onAdd = { rule -> recurringRules.add(rule) },
                onToggle = { rule ->
                    val index = recurringRules.indexOfFirst { it.id == rule.id }
                    if (index >= 0) {
                        recurringRules[index] = recurringRules[index].copy(enabled = !rule.enabled)
                    }
                },
                onDelete = { rule -> recurringRules.removeAll { it.id == rule.id } },
                onBack = {
                    backToMain()
                },
            )
        }
    }
    }

    if (showCrashLog) {
        LogCenterSheet(onDismiss = { showCrashLog = false })
    }

    if (showTransfer) {
        DataTransferSheet(
            recordCount = records.size,
            busy = transferBusy,
            statusText = transferStatus,
            onExport = {
                transferStatus = null
                runCatching {
                    exportLauncher.launch("折耳根账目-${CsvCodec.stamp()}.csv")
                }.onFailure { transferStatus = "✕ 无法打开文件选择器" }
            },
            onImport = {
                transferStatus = null
                runCatching {
                    importLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "text/plain", "*/*"))
                }.onFailure { transferStatus = "✕ 无法打开文件选择器" }
            },
            onDismiss = { showTransfer = false },
        )
    }

    if (showNotifications) {
        NotificationSheet(
            notificationsEnabled = appearance.notificationsEnabled,
            permissionGranted = NotificationHelper.canPost(context),
            reminderEnabled = appearance.reminderEnabled,
            hour = appearance.reminderHour,
            minute = appearance.reminderMinute,
            onToggleNotifications = { enabled ->
                onSettingsChange(appearance.copy(notificationsEnabled = enabled))
                if (enabled) {
                    NotificationHelper.ensureChannels(context)
                    if (!NotificationHelper.canPost(context)) requestNotificationPermission()
                }
            },
            onToggleReminder = { enabled ->
                onSettingsChange(appearance.copy(reminderEnabled = enabled))
                if (enabled) {
                    NotificationHelper.scheduleDailyReminder(
                        context,
                        appearance.reminderHour,
                        appearance.reminderMinute,
                    )
                } else {
                    NotificationHelper.cancelDailyReminder(context)
                }
            },
            onTimeChange = { hour, minute ->
                onSettingsChange(
                    appearance.copy(reminderHour = hour, reminderMinute = minute),
                )
                if (appearance.reminderEnabled) {
                    NotificationHelper.scheduleDailyReminder(context, hour, minute)
                }
            },
            onRequestPermission = {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    requestNotificationPermission()
                }
            },
            onDismiss = { showNotifications = false },
        )
    }

    actionRecord?.let { record ->
        RecordActionSheet(
            record = record,
            proEnabled = appearance.proEnabled,
            onEdit = {
                entryAmount = record.amount
                editingId = record.id
                routeKey = "entry"
                actionRecord = null
            },
            onDelete = {
                // 删除进回收站，可恢复
                val index = records.indexOfFirst { it.id == record.id }
                if (index >= 0) {
                    trash.removeAll { it.id == record.id }
                    trash.add(0, records[index])
                    records.removeAt(index)
                    persistNow()
                    AppLog.info("删除", "记录 ${record.category.label} ¥${record.amount} 移入回收站")
                }
                actionRecord = null
            },
            refundTargets = records
                .filter { it.type == EntryType.Expense && !it.flag.isFlagged }
                .sortedByDescending { it.occurredAt }
                .take(4),
            onLinkRefund = { targetId ->
                val index = records.indexOfFirst { it.id == record.id }
                if (index >= 0) {
                    records[index] = records[index].copy(refundOfId = targetId)
                }
                actionRecord = records.firstOrNull { it.id == record.id }
            },
            onSetFlag = { flag ->
                val index = records.indexOfFirst { it.id == record.id }
                if (index >= 0) {
                    records[index] = records[index].copy(
                        flag = flag,
                        refundOfId = if (flag == RecordFlag.Refund) records[index].refundOfId else null,
                    )
                }
                actionRecord = records.firstOrNull { it.id == record.id }
            },
            onNeedPro = {
                actionRecord = null
                routeKey = "pro"
            },
            onDismiss = { actionRecord = null },
        )
    }

    iconEditAccount?.let { account ->
        AccountIconPickerDialog(
            selectedId = account.iconId,
            onSelect = { option ->
                val index = accounts.indexOfFirst { it.id == account.id }
                if (index >= 0) {
                    accounts[index] = accounts[index].copy(iconId = option.id, icon = option.icon)
                }
                iconEditAccount = null
            },
            onDismiss = { iconEditAccount = null },
        )
    }

    adjustAccount?.let { account ->
        val live = accountBalances(records, accounts).firstOrNull { it.id == account.id }
        AdjustBalanceSheet(
            accountName = account.name,
            currentBalance = live?.balance ?: account.balance,
            initialBalance = accounts.firstOrNull { it.id == account.id }?.balance ?: account.balance,
            onConfirm = { mode, target ->
                val index = accounts.indexOfFirst { it.id == account.id }
                if (index >= 0) {
                    val current = live?.balance ?: accounts[index].balance
                    val delta = target - current
                    when (mode) {
                        BalanceAdjustMode.DifferenceRecord -> {
                            if (kotlin.math.abs(delta) >= 0.005) {
                                val isIncome = delta > 0
                                val categories = if (isIncome) incomeCategories else expenseCategories
                                val category = categories.lastOrNull() ?: categories.first()
                                val record = LedgerRecord(
                                    id = (records.maxOfOrNull { it.id } ?: 0L) + 1,
                                    bookName = selectedBook,
                                    type = if (isIncome) EntryType.Income else EntryType.Expense,
                                    amount = kotlin.math.abs(delta),
                                    category = category,
                                    fromAccount = accounts[index],
                                    note = "修改余额补记",
                                    currency = DemoData.currencies.first(),
                                    occurredAt = System.currentTimeMillis(),
                                )
                                records.add(0, record)
                            }
                        }
                        BalanceAdjustMode.DifferenceTransfer -> {
                            if (kotlin.math.abs(delta) >= 0.005) {
                                val transferCategory = transferCategories.firstOrNull()
                                    ?: expenseCategories.first()
                                val record = if (delta > 0) {
                                    // 从「无账户」转进来：只加余额，不动别的账户
                                    LedgerRecord(
                                        id = (records.maxOfOrNull { it.id } ?: 0L) + 1,
                                        bookName = selectedBook,
                                        type = EntryType.Transfer,
                                        amount = delta,
                                        category = transferCategory,
                                        fromAccount = DemoData.NoAccount,
                                        toAccount = accounts[index],
                                        note = "修改余额补记",
                                        currency = DemoData.currencies.first(),
                                        occurredAt = System.currentTimeMillis(),
                                    )
                                } else {
                                    LedgerRecord(
                                        id = (records.maxOfOrNull { it.id } ?: 0L) + 1,
                                        bookName = selectedBook,
                                        type = EntryType.Transfer,
                                        amount = -delta,
                                        category = transferCategory,
                                        fromAccount = accounts[index],
                                        toAccount = null,
                                        note = "修改余额补记",
                                        currency = DemoData.currencies.first(),
                                        occurredAt = System.currentTimeMillis(),
                                    )
                                }
                                records.add(0, record)
                            }
                        }
                        BalanceAdjustMode.ResetCurrent ->
                            accounts[index] = accounts[index].copy(
                                balance = target,
                                balanceStartAt = System.currentTimeMillis(),
                            )
                        BalanceAdjustMode.SetInitial ->
                            accounts[index] = accounts[index].copy(balance = target)
                    }
                }
                adjustAccount = null
            },
            onDismiss = { adjustAccount = null },
        )
    }

    actionAccount?.let { account ->
        AccountActionSheet(
            account = account,
            isDefault = account.id == defaultAccountId,
            initialBalance = accounts.firstOrNull { it.id == account.id }?.balance ?: account.balance,
            onSetDefault = {
                val next = if (defaultAccountId == account.id) null else account.id
                defaultAccountId = next
                settingsStore.saveDefaultAccount(next)
                actionAccount = null
            },
            onChangeIcon = {
                iconEditAccount = account
                actionAccount = null
            },
            onAdjustBalance = {
                adjustAccount = account
                actionAccount = null
            },
            onMoveUp = {
                val index = accounts.indexOfFirst { it.id == account.id }
                if (index > 0) {
                    val item = accounts.removeAt(index)
                    accounts.add(index - 1, item)
                }
                actionAccount = null
            },
            onMoveDown = {
                val index = accounts.indexOfFirst { it.id == account.id }
                if (index >= 0 && index < accounts.lastIndex) {
                    val item = accounts.removeAt(index)
                    accounts.add(index + 1, item)
                }
                actionAccount = null
            },
            onEdit = {
                createTypeOrdinal = account.type.ordinal
                editingAccountId = account.id
                routeKey = "create"
                actionAccount = null
            },
            onDelete = {
                accounts.removeAll { it.id == account.id }
                if (defaultAccountId == account.id) {
                    defaultAccountId = null
                    settingsStore.saveDefaultAccount(null)
                }
                actionAccount = null
            },
            onDismiss = { actionAccount = null },
        )
    }

    if (showUpdateSheet) {
        updateInfo?.let { info ->
            UpdateSheet(
                info = info,
                currentVersion = appVersion,
                onOpenChangelog = {
                    showUpdateSheet = false
                    openChangelog()
                },
                onDismiss = { showUpdateSheet = false },
            )
        }
    }

    if (showChangelog) {
        ChangelogSheet(
            groups = changelogGroups,
            localVersion = appVersion,
            loading = changelogLoading,
            error = changelogError,
            onRetry = { openChangelog() },
            onDismiss = { showChangelog = false },
        )
    }

    // 锁屏遮罩：有锁且未解锁时盖住整个界面
    if (lockConfig.enabled && !unlocked) {
        LockScreen(
            type = lockConfig.type,
            hasRecovery = lockConfig.hasRecovery,
            onRecover = { code ->
                if (lockSettings.verifyRecovery(lockConfig, code)) {
                    lockSettings.clear()
                    lockConfig = lockSettings.load()
                    true
                } else {
                    false
                }
            },
            biometricEnabled = lockConfig.biometric && biometricAvailable && appearance.proEnabled,
            biometricAvailable = biometricAvailable,
            verify = { secret -> lockSettings.matches(lockConfig, secret) },
            onUnlocked = { unlocked = true },
            onFingerprint = {
                activity?.let { act ->
                    BiometricUnlock.show(
                        activity = act,
                        onSuccess = { unlocked = true },
                        onError = { },
                    )
                }
            },
        )
    }

    if (showAppearance) {
        AppearanceSheet(
            palette = ThemePalette.resolve(appearance.themeId, appearance.customColorArgb),
            customColorArgb = appearance.customColorArgb,
            backgroundEnabled = appearance.backgroundEnabled,
            backgroundIndex = appearance.backgroundIndex,
            customBackgroundPath = appearance.customBackgroundPath,
            proEnabled = appearance.proEnabled,
            onSelectPreset = { preset ->
                onSettingsChange(appearance.copy(themeId = preset.id))
            },
            onSelectCustomColor = { argb ->
                onSettingsChange(appearance.copy(themeId = ThemePalette.ID_CUSTOM, customColorArgb = argb))
            },
            onToggleBackground = { enabled ->
                onSettingsChange(appearance.copy(backgroundEnabled = enabled))
            },
            onSelectBuiltIn = { index ->
                // 背景和主题色配套切换：每张背景都有一个推荐配色
                val matchedTheme = builtInBackgroundThemes.getOrNull(index)
                onSettingsChange(
                    appearance.copy(
                        backgroundIndex = index,
                        backgroundEnabled = true,
                        themeId = matchedTheme ?: appearance.themeId,
                    ),
                )
            },
            onPickCustomImage = { uri ->
                val saved = runCatching {
                    // 每次用新文件名：路径变了，界面上的 remember 才会重新解码
                    val target = File(
                        context.filesDir,
                        "background_${System.currentTimeMillis()}.jpg",
                    )
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        target.outputStream().use { output -> input.copyTo(output) }
                    }
                    // 删掉旧的自选背景，避免越攒越多
                    appearance.customBackgroundPath?.let { old ->
                        runCatching { File(old).takeIf { it.exists() && it != target }?.delete() }
                    }
                    target.absolutePath
                }.getOrNull()
                if (saved != null) {
                    onSettingsChange(
                        appearance.copy(
                            backgroundIndex = CUSTOM_BACKGROUND_INDEX,
                            customBackgroundPath = saved,
                            backgroundEnabled = true,
                        ),
                    )
                }
            },
            onNeedPro = { showAppearance = false; routeKey = "pro" },
            themeMode = UiState.themeMode,
            onThemeMode = { mode ->
                UiState.themeMode = mode
                UiPrefs(context).themeMode = mode
            },
            onDismiss = { showAppearance = false },
        )
    }

    if (showWebDavProGate) {
        ProRequiredDialog(
            title = "WebDAV 备份需要 Pro",
            message = "开通 Pro 后可使用 WebDAV 云备份：保存配置时自动测一次连接，之后每天首次启动自动备份一次。",
            onUpgrade = {
                showWebDavProGate = false
                mainTab = 3
                routeKey = "pro"
            },
            onDismiss = { showWebDavProGate = false },
        )
    }
}

/** 用 -1 表示「不是编辑模式」，方便放进 rememberSaveable。 */
private const val NO_EDITING = -1L

private const val NOTIFY_RECURRING = 2001

private const val NOTIFY_BUDGET = 2002

/**
 * 分期付款：把一条「总额 + 期数」的账目拆成 N 条，按月生成。
 * 最后一期吸收除不尽的零头，保证合计等于总额。
 */
internal fun expandInstallments(record: LedgerRecord, baseId: Long = record.id): List<LedgerRecord> {
    val count = record.installmentCount
    if (count <= 1) return listOf(record.copy(id = baseId))

    val total = record.amount
    val per = Math.round(total / count * 100.0) / 100.0
    return (0 until count).map { i ->
        val calendar = Calendar.getInstance().apply {
            timeInMillis = record.occurredAt
            add(Calendar.MONTH, i)
        }
        val part = if (i == count - 1) {
            Math.round((total - per * (count - 1)) * 100.0) / 100.0
        } else {
            per
        }
        record.copy(
            id = baseId + i,
            amount = part,
            originalAmount = part,
            discount = 0.0,
            myShare = part,
            participants = emptyList(),
            note = listOf(record.note, "${i + 1}/$count")
                .filter { it.isNotBlank() }
                .joinToString(" · "),
            occurredAt = calendar.timeInMillis,
            installmentIndex = i + 1,
            installmentCount = count,
        )
    }
}

/**
 * 修复历史数据里重复的记录 id。
 *
 * 旧版本按时间戳生成 id，同一毫秒保存多条、或分期/周期补记派生的 id 会撞上，
 * 撞 id 后 LazyColumn 会直接崩（Key "trash-xxx" was already used）。这里给重复项重新编号。
 */
internal fun ensureUniqueIds(list: List<LedgerRecord>): List<LedgerRecord> {
    if (list.isEmpty()) return list
    val seen = HashSet<Long>(list.size * 2)
    var maxId = list.maxOf { it.id }
    var changed = false
    val result = list.map { record ->
        if (seen.add(record.id)) {
            record
        } else {
            maxId += 1
            changed = true
            seen.add(maxId)
            record.copy(id = maxId)
        }
    }
    return if (changed) result else list
}























