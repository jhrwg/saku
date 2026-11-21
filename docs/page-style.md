# 页面样式规范（新页面必须遵守）

> **基准样式 = 「记账」页**：`Scaffold` + `PageHeader` + 内容卡片 + 底部操作栏（可选）。
> 已封装为 `ui/components/PageScaffold.kt`，新页面直接调用它，不要再手拼标题。

## 标准结构（与记账页一致）

```kotlin
@Composable
fun XxxPage(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onBack)

    PageScaffold(
        title = "标题",              // PageHeader：headlineMedium + Bold
        subtitle = "一句话说明",      // bodySmall + onSurfaceVariant
        onBack = onBack,            // 传了才显示左侧返回箭头
        scrollable = true,           // 列表页传 false，自己在内容里用 LazyColumn(weight(1f))
        actions = {                  // 可选：标题下方右对齐的操作按钮
            TextButton(onClick = {}) { Text("清空") }
        },
        bottomBar = {                // 可选：底部固定主按钮（同记账页「保存」）
            Button(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
            ) { Text("保存") }
        },
        modifier = modifier,
    ) {
        SectionCard { /* 内容 */ }
    }
}
```

`PageScaffold` 内部就是记账页那套：

```
Scaffold(containerColor = colorScheme.background)
  ├ bottomBar（Surface + navigationBarsPadding + 主按钮）
  └ Column(padding(innerPadding) + 16dp 横边距)
       ├ PageHeader(title, subtitle, leading = 返回箭头)
       └ 内容（SectionCard / LazyColumn）
```

## 约定细节

| 项 | 规格 |
| --- | --- |
| 底色 | `Scaffold(containerColor = colorScheme.background)` |
| 系统栏 | 由 `Scaffold` 的 `innerPadding` 处理，页面里**不要**再 `statusBarsPadding()` |
| 左右边距 | 统一 `16.dp` |
| 标题 | `PageHeader`：`headlineMedium` + Bold；副标题 `bodySmall` + `onSurfaceVariant` |
| 卡片 | `SectionCard`（圆角 20dp、`surfaceContainerLow`），列表项圆角 18dp |
| 底部按钮 | `fillMaxWidth` + `height(52.dp)` + `RoundedCornerShape(16.dp)`，外层 `Surface(surface)` + `navigationBarsPadding()` |
| 滚动 | 普通页面 `scrollable = true`；长列表 `scrollable = false` + `LazyColumn(Modifier.weight(1f))` |

## 反例（不要再出现）

- ❌ 自己写 `Column + statusBarsPadding + 自定义标题 Text` → 高度和别页不一致（回收站/设置页早期就是这么错的）；
- ❌ 路由页不铺底色 → 深色模式露出窗口白底；
- ❌ 页面底部按钮不加 `navigationBarsPadding` → 被手势条压住。

## 已迁移页面

- 回收站 `TrashPage`（`scrollable = false` + LazyColumn + 顶部「清空」）
- 车贷 / 房贷 `LoansPage`
- 下载更新 `DownloaderPage`

后续新页面（报表、导入向导等）一律照此实现。
