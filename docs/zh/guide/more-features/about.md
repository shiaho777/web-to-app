# 关于

应用信息与数据工具。从 [⋮ → 关于](/zh/guide/main-screen/more) 打开。

## 功能

- **关于** —— 版本、当前语言和项目链接。
- **检查更新** —— 检查是否有更新版本的构建器(显示当前版本及是否有可用更新)。
- **法律免责声明** —— 涵盖允许用途和责任的重要声明。

## 宿主开关

这些开关只在构建器里。生成的 APK 不会读取它们。

- **显示说明文字** —— 在功能标题下方显示辅助说明。默认开启。关闭后标题、当前值、错误和控件上唯一的状态仍然显示。
- **WebApp 独立任务** —— 默认关闭。开启后,首页预览和桌面快捷方式会让每个 WebApp 单独占用一条最近任务。这是另一条最近任务,不是另一个进程,也更占内存。再次打开同一个应用会把那张卡片调到前面。旧版本创建的快捷方式仍会新开一张卡片,需要重新创建。关闭后这些额外任务会结束。
- **高级功能** —— 默认关闭。开启后可以创建、预览并导出 Node.js、PHP、Python、Go、WordPress、图片和视频。代码始终在应用里。开关关闭时,已有项目仍留在列表中,也可以继续编辑。[Linux 环境](/zh/guide/more-features/linux-environment)、[运行时管理](/zh/guide/more-features/runtime-management)和[端口管理](/zh/guide/more-features/port-manager)只在开关开启时出现在 ⋮ 菜单。服务端运行时导出固定 targetSdk 28。WebView 导出保持 shell 模板的 targetSdk 35。Play 和 AAB 仍然拒绝会 exec 进程的类型。
- **本地 MCP** —— 默认关闭。开启后,构建器在 `127.0.0.1` 提供与应用内 [Agent](/zh/guide/more-features/agent) 相同的工具,外加一个只读预览工具,并用 bearer 令牌保护。长按地址或令牌即可复制。**复制配置**复制一份 `mcp.json`。**更换令牌**签发新令牌。写操作仍会在手机上确认。在电脑上先执行 `adb reverse tcp:<port> tcp:<port>`,再用卡片上的地址。生成的 APK 不包含这个服务。

## 说明

- 在重大更改前或更换设备时,使用 [数据备份](/zh/guide/more-features/data-backup)。
- WebToApp 以 [The Unlicense](https://github.com/shiaho777/web-to-app/blob/main/LICENSE) 开源。
