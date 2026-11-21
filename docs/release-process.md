# 发版流程（必须按这个顺序）

> 目的：用户看到「有新版本」时，安装包一定已经能下载。
> 顺序错了会出现「提示更新 → 点下载 404 / 空文件」。

## 铁律

**先传包、后发版、最后才更新版本清单。**

`Changelog-beta.json` / `Changelog.json` 是 App 判断「有没有新版本」的唯一依据，
所以它必须**最后**改：改完用户就会收到更新提示，此时直链必须已经可用。

## 步骤

| # | 步骤 | 校验 |
| --- | --- | --- |
| 1 | 改代码、升 `versionCode`（单调 +1）与 `versionName`（测试版带 `-beta`） | `:app:assembleDebug :app:testDebugUnitTest` 通过 |
| 2 | 生成安装包 `vebk-<ver>-beta.apk` | 文件大小 > 20 MB |
| 3 | GitHub：创建 release（**draft = true**），上传 asset | `assets[].state == "uploaded"` 且 size 与本地一致 |
| 4 | Gitee：创建 release，上传 attach_files | `HEAD` 请求返回 **200**，`Content-Length` 与本地一致 |
| 5 | 拿到两个直链后再改 `Changelog-beta.json`（`latest` / `version` / `gitee` / `direct` / `mirror`） | 直链能 200 下载 |
| 6 | 提交并推送源码（Gitee 双仓 + GitHub 镜像） | 推送成功 |
| 7 | 最后才把 GitHub release 从 draft 发布（`draft = false`） | API 返回 `draft: false` |

## 校验脚本

每次发版都要跑一遍：

```powershell
# Gitee 直链
Invoke-WebRequest -Uri $giteeUrl -Method Head -MaximumRedirection 5 -UseBasicParsing

# GitHub 资源
Invoke-RestMethod -Uri "https://api.github.com/repos/jhrwg/saku/releases/tags/$tag" `
  -Headers @{ Authorization = "token $env:GH_TOKEN"; 'User-Agent' = 'dsh' } |
  Select-Object -ExpandProperty assets |
  Select-Object name, size, state
```

期望：Gitee `200` + 正确大小；GitHub `state = uploaded` 且大小一致。

## 常见事故

- **先改 Changelog 再传包** → 用户检查更新后下载 404（本次要修的就是这个）。
- **用 GitHub API 直接下 APK** → 返回的是 JSON 元数据，必须带 `Accept: application/octet-stream`。
- **asset 名重复** → GitHub 会另外命名（加后缀），直链就对不上；发布前确认旧 asset 已删。
- **忘记 `versionCode` +1** → 用户装了新版又被「已是最新」挡住。
