@file:OptIn(ExperimentalMaterial3Api::class)

package com.ve.bookkeeping.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.ve.bookkeeping.ui.components.AccountTypeSheet
import com.ve.bookkeeping.ui.components.AddBookDialog
import com.ve.bookkeeping.ui.components.AmountKeypadSheet
import com.ve.bookkeeping.ui.components.LedgerDrawerSheet
import com.ve.bookkeeping.ui.components.ProGateSheet
import com.ve.bookkeeping.ui.model.AccountOption
import com.ve.bookkeeping.ui.model.AccountType
import com.ve.bookkeeping.ui.model.LedgerRecord
import com.ve.bookkeeping.ui.pages.AssetsPage
import com.ve.bookkeeping.ui.pages.HomePage
import com.ve.bookkeeping.ui.pages.ProfilePage
import com.ve.bookkeeping.ui.pages.StatsPage
import kotlin.math.absoluteValue
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Alignment
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.outlined.CalendarMonth
import com.ve.bookkeeping.ui.pages.CalendarPage

/** 底部导航的四个目的地。 */
private enum class MainTab(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    Home("首页", Icons.Rounded.Home, Icons.Outlined.Home),
    Assets("资产", Icons.Rounded.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
    Calendar("日历", Icons.Rounded.CalendarMonth, Icons.Outlined.CalendarMonth),
    Stats("统计", Icons.Rounded.BarChart, Icons.Outlined.BarChart),
    Profile("我的", Icons.Rounded.Person, Icons.Outlined.Person),
}

/**
 * 主页容器：底部 Tab 栏 + 可左右滑动的四个页面 + 账本侧边栏。
 *
 * 切换方式：
 * 1. 点击底部 Tab —— `animateScrollToPage` + 弹簧，平滑滚动到目标页；
 * 2. 左右滑动 —— `HorizontalPager` 跟手拖动、松手吸附；
 * 3. 每页按偏移量做缩放 / 淡出 / 圆角 / 视差的层叠动画。
 *
 * 另外：
 * - 首页左上角图标打开账本侧边栏（抽屉手势关闭，避免和左右翻页冲突）；
 * - 首页右下角「+」按钮弹出金额键盘，点「完成」跳转记账页。
 */
@Composable
fun MainScreen(
    books: List<String>,
    selectedBook: String,
    records: List<LedgerRecord>,
    accounts: List<AccountOption>,
    debts: List<com.ve.bookkeeping.ui.model.Debt> = emptyList(),
    defaultAccountId: String?,
    categoryCount: Int,
    recurringCount: Int,
    onSelectBook: (String) -> Unit,
    onAddBook: (String) -> Unit,
    onStartEntry: (Double) -> Unit,
    onCreateAccount: (AccountType) -> Unit,
    onRecordClick: (LedgerRecord) -> Unit,
    onAccountClick: (AccountOption) -> Unit,
    onOpenCategories: () -> Unit,
    onOpenWebDav: () -> Unit,
    onOpenAppearance: () -> Unit,
    themeModeLabel: String,
    onCycleThemeMode: () -> Unit,
    fontSizeLabel: String,
    onCycleFontScale: () -> Unit,
    materialYouLabel: String,
    onToggleMaterialYou: () -> Unit,
    privacyLabel: String,
    onTogglePrivacy: () -> Unit,
    onOpenPro: () -> Unit,
    onOpenRecurring: () -> Unit,
    onOpenRecords: () -> Unit,
    onOpenRecordsWithTag: (String) -> Unit = {},
    onOpenBudget: () -> Unit,
    onOpenTransfer: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenSecurity: () -> Unit,
    onOpenFeedback: () -> Unit,
    onOpenTags: () -> Unit,
    onOpenRates: () -> Unit,
    onOpenDebts: () -> Unit,
    avatarPath: String,
    userName: String,
    userBio: String,
    onSaveProfile: (String, String, String) -> Unit,
    noticeText: String,
    onNoticeClick: () -> Unit,
    onAvatarPicked: (String) -> Unit,
    onOpenTrash: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenLoans: () -> Unit,
    onOpenCrashLog: () -> Unit,
    crashCount: Int,
    budgetSummary: String,
    proEnabled: Boolean,
    appVersion: String,
    updateStatus: String?,
    onCheckUpdate: () -> Unit,
    onOpenChangelog: () -> Unit,
    onPageChanged: (Int) -> Unit = {},
    modifier: Modifier = Modifier,
    initialPage: Int = 0,
) {
    val tabs = MainTab.entries
    val pagerState = rememberPagerState(initialPage = initialPage.coerceIn(0, tabs.lastIndex)) {
        tabs.size
    }
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    // 记住当前 Tab，进出子页面后能回到原来那一页
    LaunchedEffect(pagerState.currentPage) {
        onPageChanged(pagerState.currentPage)
    }

    // 外部改了要显示的 tab（例如从二级页面返回原来的那一页）时，把 pager 也切过去
    LaunchedEffect(initialPage) {
        val target = initialPage.coerceIn(0, tabs.lastIndex)
        if (pagerState.currentPage != target) {
            pagerState.scrollToPage(target)
        }
    }

    var showAddBookDialog by remember { mutableStateOf(false) }
    var showBookProGate by remember { mutableStateOf(false) }
    var showKeypad by remember { mutableStateOf(false) }
    var showAccountTypes by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        // 只在抽屉已打开时允许滑动关闭：主页的左右滑动属于翻页，不能被抽屉抢走
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            LedgerDrawerSheet(
                books = books,
                selectedBook = selectedBook,
                proEnabled = proEnabled,
                onSelectBook = { book ->
                    onSelectBook(book)
                    scope.launch { drawerState.close() }
                },
                onAddBook = {
                    scope.launch { drawerState.close() }
                    // 免费版只允许一个账本，PRO 才能继续新增
                    if (!proEnabled && books.size >= 1) {
                        showBookProGate = true
                    } else {
                        showAddBookDialog = true
                    }
                },
            )
        },
    ) {
        val layoutMode = rememberLayoutMode()
        Row(modifier = modifier.fillMaxSize()) {
        if (layoutMode != LayoutMode.Compact) {
            // 大屏 / 横屏：左侧图标导轨，替代底部悬浮 Tab
            androidx.compose.material3.NavigationRail(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 1f),
                modifier = Modifier.fillMaxHeight(),
            ) {
                tabs.forEachIndexed { index, tab ->
                    val selected = pagerState.currentPage == index
                    androidx.compose.material3.NavigationRailItem(
                        selected = selected,
                        onClick = {
                            // 跨多页直接跳过去：逐页滚过去要滚三四个屏幕，体感很卡
                            switchToPage(pagerState, index, scope)
                        },
                        icon = {
                            Icon(
                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.label,
                            )
                        },
                        label = { Text(text = tab.label) },
                    )
                }
            }
        }
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
        ) { innerPadding ->
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                pageSpacing = 0.dp,
                // 预合成左右相邻页：否则页面进入视野的瞬间才第一次绘制，看起来像"闪出来"
                beyondViewportPageCount = 1,
            ) { page ->
                // 页面相对当前选中页的偏移量：0 = 完全居中，±1 = 相邻整页
                val pageOffset =
                    (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                // 滑动进度归一化（0 = 居中，1 = 完全离开视野）
                val rawDistance = pageOffset.absoluteValue.coerceIn(0f, 1f)
                // 模糊/淡出的曲线用 easeIn：靠近边缘时就开始变模糊，进入时再逐渐清晰
                val distance = rawDistance * rawDistance

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        // 离中心越远越模糊，进出都像从雾里升起/消散
                        .blur(
                            radius = 6.dp * distance,
                            edgeTreatment = BlurredEdgeTreatment.Unbounded,
                        )
                        .graphicsLayer {
                            // 透明度与缩放的曲线用 easeOut，保证页面「浮起来」的速度更柔和
                            val eased = 1f - (1f - rawDistance) * (1f - rawDistance)
                            alpha = 1f - 0.72f * eased
                            val scale = 1f - 0.12f * eased
                            scaleX = scale
                            scaleY = scale
                            // 视差：内容比页面本身移动得更慢，产生"内容在卡片里滑动"的错觉
                            translationX = 56.dp.toPx() * pageOffset
                            // 轻微上浮
                            translationY = -12.dp.toPx() * eased
                            // 离开中心时收成圆角卡片
                            shape = RoundedCornerShape((26.dp * eased).toPx())
                            clip = true
                        },
                ) {
                    when (tabs[page]) {
                        MainTab.Home -> HomePage(
                            bookName = selectedBook,
                            records = records,
                            onOpenDrawer = { scope.launch { drawerState.open() } },
                            onRecordClick = onRecordClick,
                            noticeText = noticeText,
                            onNoticeClick = onNoticeClick,
                            onOpenRecords = onOpenRecords,
                        )

                        MainTab.Assets -> AssetsPage(
                            records = records,
                            accounts = accounts,
                            debts = debts,
                            defaultAccountId = defaultAccountId,
                            onAccountClick = onAccountClick,
                            proEnabled = proEnabled,
                            onNeedPro = onOpenPro,
                            onOpenDebts = onOpenDebts,
                        )

                        MainTab.Calendar -> CalendarPage(
                            records = records,
                            onRecordClick = onRecordClick,
                        )

                        MainTab.Stats -> StatsPage(
                            records = records,
                            accounts = accounts,
                            proEnabled = proEnabled,
                            onNeedPro = onOpenPro,
                            onTagClick = onOpenRecordsWithTag,
                        )
                        MainTab.Profile -> ProfilePage(
                            records = records,
                            books = books,
                            categoryCount = categoryCount,
                            recurringCount = recurringCount,
                            proEnabled = proEnabled,
                            appVersion = appVersion,
                            updateStatus = updateStatus,
                            onCheckUpdate = onCheckUpdate,
                            onOpenChangelog = onOpenChangelog,
                            onOpenRecords = onOpenRecords,
                            onOpenBudget = onOpenBudget,
                            onOpenTransfer = onOpenTransfer,
                            onOpenNotifications = onOpenNotifications,
                            onOpenSecurity = onOpenSecurity,
                            onOpenFeedback = onOpenFeedback,
                            onOpenTags = onOpenTags,
                    onOpenRates = onOpenRates,
                            onOpenDebts = onOpenDebts,
                            avatarPath = avatarPath,
                            userName = userName,
                            userBio = userBio,
                            onSaveProfile = onSaveProfile,
                            onAvatarPicked = onAvatarPicked,
                            onOpenTrash = onOpenTrash,
                            onOpenAbout = onOpenAbout,
                            onOpenLoans = onOpenLoans,
                            onOpenCrashLog = onOpenCrashLog,
                            crashCount = crashCount,
                            budgetSummary = budgetSummary,
                            onOpenCategories = onOpenCategories,
                            onOpenWebDav = onOpenWebDav,
                            onOpenAppearance = onOpenAppearance,
                            themeModeLabel = themeModeLabel,
                            onCycleThemeMode = onCycleThemeMode,
                            fontSizeLabel = fontSizeLabel,
                            onCycleFontScale = onCycleFontScale,
                            materialYouLabel = materialYouLabel,
                            onToggleMaterialYou = onToggleMaterialYou,
                            privacyLabel = privacyLabel,
                            onTogglePrivacy = onTogglePrivacy,
                            onOpenPro = onOpenPro,
                            onOpenRecurring = onOpenRecurring,
                        )
                    }
                }
            }
        }
            // 悬浮胶囊 Tab：仅手机竖屏显示（大屏用左侧导轨）
            if (layoutMode == LayoutMode.Compact) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 28.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    shape = RoundedCornerShape(32.dp),
                    // 不透明：背景图模式下避免后面账目透出来
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 1f),
                    shadowElevation = 10.dp,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        tabs.forEachIndexed { index, tab ->
                            val selected = pagerState.currentPage == index
                            Surface(
                                onClick = {
                                    switchToPage(pagerState, index, scope)
                                },
                                shape = RoundedCornerShape(50),
                                color = if (selected) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    Color.Transparent
                                },
                                modifier = Modifier.weight(1f),
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label,
                                        tint = if (selected) {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                        modifier = Modifier.size(22.dp),
                                    )
                                    Text(
                                        text = tab.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (selected) {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
            }

            // 记一笔：右下角，同样贴在屏幕底部（与胶囊同级）
            val onHome = pagerState.currentPage == MainTab.Home.ordinal
            val onAssets = pagerState.currentPage == MainTab.Assets.ordinal
            val fabScaleAnim = remember { androidx.compose.animation.core.Animatable(1f) }
            val fabScope = rememberCoroutineScope()
            if (onHome || onAssets) {
                androidx.compose.material3.FloatingActionButton(
                    onClick = {
                        fabScope.launch {
                            fabScaleAnim.animateTo(
                                0.88f,
                                androidx.compose.animation.core.tween(90),
                            )
                            fabScaleAnim.animateTo(
                                1f,
                                androidx.compose.animation.core.spring(
                                    dampingRatio = androidx.compose.animation.core.Spring
                                        .DampingRatioMediumBouncy,
                                ),
                            )
                        }
                        if (onAssets) showAccountTypes = true else showKeypad = true
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .navigationBarsPadding()
                        .padding(end = 20.dp, bottom = if (layoutMode == LayoutMode.Compact) 96.dp else 28.dp)
                        .size(width = 64.dp, height = 56.dp)
                        .graphicsLayer {
                            scaleX = fabScaleAnim.value
                            scaleY = fabScaleAnim.value
                        },
                    shape = RoundedCornerShape(18.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = if (onAssets) "添加账户" else "记一笔",
                        modifier = Modifier.size(26.dp),
                    )
                }
            }
        }
        }
    }

    if (showKeypad) {
        AmountKeypadSheet(
            title = "记一笔",
            subtitle = selectedBook,
            onDismiss = { showKeypad = false },
            onDone = { amount ->
                showKeypad = false
                onStartEntry(amount)
            },
        )
    }

    if (showAccountTypes) {
        AccountTypeSheet(
            onSelect = { type ->
                showAccountTypes = false
                onCreateAccount(type)
            },
            onDismiss = { showAccountTypes = false },
        )
    }

    if (showAddBookDialog) {
        AddBookDialog(
            onDismiss = { showAddBookDialog = false },
            onConfirm = { name ->
                if (name.isNotBlank() && books.none { it == name }) {
                    onAddBook(name)
                }
                showAddBookDialog = false
            },
        )
    }

    ProGateSheet(
        visible = showBookProGate,
        onUpgrade = {
            showBookProGate = false
            onOpenPro()
        },
        onDismiss = { showBookProGate = false },
    )
}

/**
 * 切换底部 Tab。
 *
 * 相邻页用弹簧动效（有前后关系的连贯感）；跨两页以上直接落位，
 * 否则要连续滚过好几个整屏（首页 → 我的），又慢又卡。
 */
private fun switchToPage(
    pagerState: androidx.compose.foundation.pager.PagerState,
    index: Int,
    scope: kotlinx.coroutines.CoroutineScope,
) {
    if (pagerState.currentPage == index) return
    val distance = kotlin.math.abs(pagerState.currentPage - index)
    scope.launch {
        if (distance <= 1) {
            pagerState.animateScrollToPage(
                page = index,
                animationSpec = androidx.compose.animation.core.spring(
                    dampingRatio = 0.85f,
                    stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow,
                ),
            )
        } else {
            pagerState.scrollToPage(index)
        }
    }
}
