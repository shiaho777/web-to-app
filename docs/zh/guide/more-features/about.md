# 关于

应用信息与数据工具。从 [⋮ → 关于](/zh/guide/main-screen/more) 打开。

## 功能

- **关于** —— 版本、当前语言和项目链接。
- **检查更新** —— 查看已安装版本，并从 GitHub Releases 检查新版。默认在启动时自动检查，可以在「关于」关闭。选择「跳过此版本」后，该版本不会继续提醒。F-Droid 通过自己的渠道更新。
- **更新内容** —— 构建器记录当前版本后，后续升级的首次启动会在可取得日志时显示更新内容。
- **功能导览** —— 可以在「关于」重新播放新手导览，也可以跳过。
- **法律免责声明** —— 涵盖允许用途和责任的重要声明。

## 宿主开关

这些开关只在构建器里。生成的 APK 不会读取它们。

- **显示说明文字** —— 在功能标题下方显示辅助说明。默认开启。关闭后标题、当前值、错误和控件上唯一的状态仍然显示。
- **WebApp 独立任务** —— 默认关闭。开启后,首页预览会让每个 WebApp 单独占用一条最近任务,可同时打开多个。这是另一条最近任务,不是另一个进程,也更占内存。再次打开同一个预览会把那张卡片调到前面。关闭后,这些预览任务会结束。桌面快捷方式始终单独占用一个任务,返回时回到桌面。关闭这个开关不会结束快捷方式的任务。很早以前创建的快捷方式仍可能每次新开一张卡片,需要重新创建。
- **高级功能** —— 默认关闭。开启后可以创建、预览并导出 Node.js、PHP、Python、Go、WordPress、图片和视频。代码始终在应用里。开关关闭时,已有项目仍留在列表中,也可以继续编辑。[Linux 环境](/zh/guide/more-features/linux-environment)、[运行时管理](/zh/guide/more-features/runtime-management)和[端口管理](/zh/guide/more-features/port-manager)只在开关开启时出现在 ⋮ 菜单。服务端运行时导出固定 targetSdk 28。WebView 导出保持 shell 模板的 targetSdk 35。Play 和 AAB 仍然拒绝会 exec 进程的类型。
- **本地 MCP** —— 默认关闭。开启后,构建器在 `127.0.0.1` 提供与应用内 [Agent](/zh/guide/more-features/agent) 相同的工具,外加一个只读预览工具,并用 bearer 令牌保护。长按地址或令牌即可复制。**复制配置**复制一份 `mcp.json`。**更换令牌**签发新令牌。写操作仍会在手机上确认。在电脑上先执行 `adb reverse tcp:<port> tcp:<port>`,再用卡片上的地址。生成的 APK 不包含这个服务。

## 说明

- 在重大更改前或更换设备时,使用 [数据备份](/zh/guide/more-features/data-backup)。
- WebToApp 以 [The Unlicense](https://github.com/shiaho777/web-to-app/blob/main/LICENSE) 开源。
