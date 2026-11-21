# 折耳根记账 · 开发规范（统一化）

> 目的：让页面、弹窗、权益提示、发版节奏保持一致，后续改动都按这份文档来。
> 新页面 / 新弹窗写完后，对照最后一节的自查清单过一遍。

---

## 一、独立页（二级页面）

**样式基准**：`周期记账`、`车贷房贷` 这类页面。

| 规则 | 说明 |
| --- | --- |
| 左上角**不放返回按钮** | 靠系统返回手势 / 返回键返回；页面标题直接顶格 |
| 统一用 `PageScaffold` | 标题 + 副标题 + 内容，不要再手拼 `Column + statusBarsPadding` |
| 内容可滚动 | 内容多时整页滚动，底部操作栏固定在底部不随滚动 |
| 返回行为 | 必须 `BackHandler` 回到上一页；不要直接把 Activity 关掉（会「退回桌面」） |

```kotlin
PageScaffold(
    title = "汇率",
    subtitle = "全部 153 种货币",
    onBack = onBack,        // 只用于 BackHandler，不显示箭头
    scrollable = true,
)
```

---

## 二、底部弹窗（Bottom Sheet）

| 规则 | 说明 |
| --- | --- |
| 高度上限 | **不超过屏幕高度的一半**（`maxHeight = 屏高 × 0.5`），横屏按实际高度自适配 |
| 内容过多 | 弹窗内部**可滚动**，不要顶出屏幕 |
| 按钮位置 | 有按钮时**固定在弹窗底部**，不随内容滚动（滚动区只包内容） |
| 圆角与配色 | 统一 `RoundedCornerShape` + `surfaceContainerLow`，与现有弹窗一致 |

结构示意：

```
ModalBottomSheet
 └ Column(heightIn(max = 屏高 * 0.5))
     ├ 标题 / 说明（固定）
     ├ 滚动区（weight(1f) + verticalScroll）
     └ 按钮区（固定，放在滚动区外面）
```

实现统一走 `AppSheet` / `AppSheetBody`（见 `ui/components/SheetHost.kt`），不要自己写高度。

---

## 三、无 Pro 权限的提示

**统一使用底部弹窗**（不是 AlertDialog、不是 Toast），文案固定：

- 标题/内容：**该功能需开启 Pro 权益**
- 按钮 1：**前往 Pro** → 跳到 Pro 页
- 按钮 2：**以后再说** → 关闭弹窗

```kotlin
ProGateSheet(
    visible = true,
    onUpgrade = { 打开 Pro 页 },
    onDismiss = { 关闭 },
)
```

弹窗里不要再写「诚信付款」「付款后开通」之类的说明，说明只放在 Pro 页。

---

## 四、发版规范

### 测试版

- 版本号 = **当前最新正式版的补丁号依次 +1**，并加 `-beta`：
  - 正式版 `1.2.0` → 测试版 `1.2.1-beta`、`1.2.2-beta`、`1.2.3-beta` …
  - 正式版 `1.3.2` → 测试版 `1.3.3-beta`、`1.3.4-beta` …
- 每次发版 `versionCode` **单调 +1**（不能重复、不能回退）
- `versionName` 必须带 `-beta` 后缀；更新日志里的版本号同样带后缀
- Tag：`v<版本>`，例如 `v1.3.13-beta`；安装包名：`vebk-<版本>-beta.apk`

### 正式版

暂无额外规范；发布时去掉 `-beta` 后缀、`versionCode` 继续 +1、更新日志按四段式。

### 发布顺序（务必遵守）

1. 编译 + 单测通过
2. 生成安装包
3. **先上传安装包**（GitHub 建 draft、Gitee 建 release），校验直链 200 且大小一致
4. 再更新 `Changelog-beta.json` / `Changelog.json`（App 靠它发现更新）
5. 推送源码
6. 最后把 GitHub release 从 draft 发布

详见 [release-process.md](release-process.md)。

---

## 五、其他既有约定（沿用）

- 文案全中文；金额、日期格式跟随系统本地化
- Pro 写法统一为 `Pro`（不写 `PRO`）
- 功能入口只在「我的 → 功能」区，宫格自动补位（不满 4 个时补在末尾，不留在前面）
- 所有功能必须有入口：数据模型里加字段，就要在界面上给到能看到 / 能改的地方
- 日志统一走 `LogCenterSheet`（我的页与意见反馈共用）

---

## 六、自查清单

- [ ] 独立页左上角没有返回按钮，系统返回能回上一页
- [ ] 底部弹窗高度 ≤ 屏高一半，内容多时内部可滚，按钮固定
- [ ] Pro 拦截统一是底部弹窗，文案「该功能需开启 Pro 权益」+ 前往 Pro / 以后再说
- [ ] 测试版号 = 正式版补丁号 +1 且带 `-beta`，`versionCode` +1
- [ ] 先上传包、后改 Changelog
- [ ] 新功能有入口，宫格自动补位
