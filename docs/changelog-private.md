# 私有更新日志（内部记录，不对外）

> 用途：每次发版由我（开发）在这里写**详细技术改动**；`Changelog.json`（正式）与 `Changelog-beta.json`（测试）里面向用户的文案由作者据此编写。
> 位置：私有仓库 `yijiang772/vebk`（Gitee）与 `jhrwg/vebk`（GitHub）的 `docs/` 下。

## 版本号规则（自 1.2.0 起）

- **正式版**：`1.2.0` → `1.3.0` → `1.4.0`，每次发正式版 **minor +1、patch 归 0**。
- **测试版**：基于「当前最新正式版」，**patch 递增**：正式版 `1.3.0` 之后是 `1.3.1`、`1.3.2`…
  只要没发新的正式版，这个 patch 就一直往上加；发出 `1.4.0` 后测试版再从 `1.4.1` 开始。
- **versionCode** 始终单调递增（不随版本名重置）。
- **日志区分**：正式版只写 `Changelog.json`；测试版只写 `Changelog-beta.json`（内容是我自己写的测试改动，不混入正式历史）。

## 记录格式

```
## vX.Y.Z（YYYY-MM-DD）· versionCode N · 正式/测试
- [类型] 改动点：涉及文件 / 关键实现
- 已知问题 / 待办
```

---

## v1.3.0（2026-11-21）· versionCode 90 · 正式

- [发布] 正式版 1.3.0：汇总 1.2.x 全部改动（我的页改版、日历、标签、借入借出、记账页重做、横屏两列、星期分布、全量日志）
- [修复] 数据不落盘（重启回到初始状态）、记完立刻清后台丢账、回收站重复 id 闪退、编辑资料保存无效、左滑删除不刷新、返回跳错页
- [优化] 切换 Tab 不再逐页滚动、减弱整页模糊；云备份入口移到我的页右上角、恢复补全并留本机快照
- [日志] 新增 AppLog：运行日志按天 + 崩溃独立文件，弹窗可选「全部 / 崩溃 / 运行」并导出 txt
- [公告] 公告文案改为「1.3.0版本已发布更新，全新我的页等你体验！」

## v1.2.0（2026-11-21）· versionCode 64 · 正式

- [UI] 我的页按手稿重做：顶部「外观」胶囊 + 设置齿轮；资料卡（头像/名称/签名 + 天数·账本·记录数）；通栏 PRO；功能区两行四列；更多精简为 4 项
- [功能] 资料编辑：名称右侧铅笔 → ModalBottomSheet，可改头像（相册）、名称（≤12）、个性签名（≤30），持久化于 `UiPrefs.userName/userBio`
- [适配] `ui/Adaptive.kt`：Compact/Medium/Expanded 三档，宽 ≥600dp 用左侧 NavigationRail，内容宽度 720/1040dp；`AppRoot` 路由容器加 `clipToBounds()` 修抽屉渗透
- [UI] 设置页：移除「外观」行，新增「通知」
- [修复] 云备份改为路由页（`AppRoute.WebDav`）；「更新前先备份」开关改本地状态；测试版版本号支持 `-beta` 后缀解析
- [发布] 正式通道 1.2.0；测试通道重置为 1.2.0 基线（等待下一个 1.2.x 测试版）
## v1.1.26（2026-11-21）· versionCode 48

- [UI] `MainScreen`：Scaffold 去掉 bottomBar，改为 Box 内叠加透明胶囊 Tab；内容 `padding(bottom = 92.dp)` 避免被挡
- [UI] 公告 `widthIn(max = 132.dp)`，不再挤压左侧标题
- 用户文案建议：「底部 Tab 悬浮透明、公告不再挡字」

## v1.1.25（2026-11-21）· versionCode 47

- [UI] 底部 Tab 首次改为悬浮胶囊（该版仍是 Scaffold bottomBar，背景不透明）
- [UI] 公告宽度 150 → 260dp（该版反而过长，1.1.26 已收窄到 132dp）
- [文档] 新增 `docs/changelog-private.md` 私有更新日志
- 用户文案建议：「底部 Tab 悬浮化」
## v1.1.24（2026-11-21）· versionCode 46

- [发布] 验证版：更新弹窗三通道（Gitee / GitHub / 镜像）上线验证
- [配置] `Changelog.json` 增加 `gitee` 字段，与 `direct` / `mirror` 独立
- 用户文案建议：「更新弹窗新增 Gitee 下载（国内更快）」

## v1.1.23（2026-11-21）· versionCode 45

- [UI] `UpdateSheet`：Gitee 下载为主按钮（全宽 50dp），GitHub / 镜像并排次要按钮（44dp），增加一行说明
- [数据] `UpdateInfo` 新增 `giteeUrl`，`UpdateChecker` 两个解析器都读取 `gitee` 字段
- 用户文案建议：「更新弹窗改版，下载更快」

## v1.1.22（2026-11-21）· versionCode 44

- [发布] 维护版本，双通道重发
- 用户文案建议：「稳定性维护」

## v1.1.21（2026-11-21）· versionCode 43

- [基建] Gitee 公开仓 `yijiang772/saku` 建立：公告 / 更新 JSON / APK Release
- [代码] `NoticeClient`、`UpdateChecker` 首选 Gitee raw；`direct` 指向 Gitee APK
- [基建] Gitee 私有源码仓 `yijiang772/vebk` 建立，全量源码推送
- 用户文案建议：「接入国内下载源，更新与公告更稳」

## v1.1.20（2026-11-21）· versionCode 42

- [修复] `MainScreen` 未把 `noticeText` 传给 `HomePage`，导致公告不显示
- [增强] 公告新增 `Changelog.json` 的 `notice` 字段兜底
- 用户文案建议：「修复首页公告不显示」

## v1.1.19（2026-11-21）· versionCode 41

- [增强] 公告拉取改为镜像优先（gh-proxy → ghproxy → raw）
- 用户文案建议：「提升公告拉取成功率」

## v1.1.18（2026-11-21）· versionCode 40

- [UI] `PageScaffold` 增加 `showBack`（默认 false）；预算/分类/记录/周期/安全/云备份页去掉返回箭头
- [功能] 我的页头像自定义：相册选图 → 复制到 `filesDir/avatar_*.jpg`，持久化 `UiPrefs.avatarPath`
- [修复] 启动闪主题：`MainActivity.onCreate` 在 `setContent` 前同步 `applyUiPrefs()`
- [功能] 反馈页改为 WebView 内嵌腾讯问卷 + 导出日志（分享文本）/刷新
- [功能] 首页公告跑马灯：`NoticeClient` 读公开仓库 `notice.json`，`PageHeader` 新增 `trailing` 插槽
- 用户文案建议：「头像可换、启动不再闪、反馈内置问卷、首页公告」

## v1.1.17（2026-11-21）· versionCode 39

- [功能] 反馈改腾讯问卷（`FEEDBACK_FORM_URL`），移除 Worker 依赖
- 用户文案建议：「意见反馈走问卷，国内直接可用」

## v1.1.16（2026-11-21）· versionCode 38

- [功能] `FeedbackClient` 通道地址可在 App 内修改（`prefs.endpoint`），失败可复制内容，GitHub 提交兜底
- 用户文案建议：「反馈通道地址可改」

## v1.1.15（2026-11-21）· versionCode 37

- [调试] 反馈页显示当前通道地址与配置状态
- 用户文案建议：「反馈页可自查通道」

## v1.1.14（2026-11-21）· versionCode 36

- [配置] `FeedbackClient.ENDPOINT` 指向 Cloudflare Worker（workers.dev 被 DNS 污染，实测不通）
- 用户文案建议：「反馈通道接入」

## v1.1.13（2026-11-21）· versionCode 35

- [功能] 新增意见反馈（Cloudflare Worker → GitHub Issue），含设备信息/崩溃日志附件、离线队列
- [基建] `worker/feedback-worker.js` + 部署文档
- 用户文案建议：「新增意见反馈，无需登录」

## 更早版本（摘要）

- v1.1.12 · 页面骨架统一为记账页风格（Scaffold + PageHeader + 底栏）
- v1.1.11 · 回收站重写、`PageScaffold` 初版、更新弹窗按钮调整
- v1.1.10 · 修复源码编码导致的中文方块、车贷房贷移入快捷入口、我的页标题对齐
- v1.1.9 · 恢复 PRO 横幅、新增应用内下载器（`DownloaderPage`）
- v1.1.8 · 恢复 PRO 权益入口、修复深色模式白底（路由层补 `Surface`）
- v1.1.7 · 我的页重做、云备份全屏页 + 更新前备份、记账按钮动画、国内镜像、PRO 延迟
- v1.1.6 · 图标更换、回收站布局、备份周期、提醒持久化、PRO 校验动画、分类扩充
- v1.1.5 · PRO 宫格/收款码/按钮统一、设置页拆分、二级分类快捷新建、银行图标
- v1.1.4 · 修复检查更新被镜像旧缓存误判
- v1.1.3 · 外观弹窗加深浅模式、主题色渐变、隐私眼睛、移除 Material You
- v1.1.0 · 首个正式版（外观/记账/资产/预算/报表/数据/安全 全功能）
