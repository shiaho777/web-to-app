# 快速开始

本教程带你从全新安装走到第一个已签名的 APK。每一步都会说明应用在背后实际做了什么,让流程能从代码的角度理解。

## 1. 安装并启动

在运行 **Android 6.0(API 23)或更高版本** 的设备上安装 WebToApp 构建器。

<div class="wta-install">

**下载构建器**

<LatestRelease variant="install" />

从 [GitHub Releases](https://github.com/shiaho777/web-to-app/releases) 获取 APK,像普通应用一样安装即可。按钮会自动检测最新版本。

</div>

启动时,`WebToAppApplication` 以 **构建器模式**(`SHELL_RUNTIME_ONLY = false`)启动:初始化 i18n 字符串、Room 数据库和依赖图,然后显示 **我的应用** —— 即列出你所创建的全部应用的主页。见[主界面](/zh/guide/main-screen/my-apps)。

## 2. 创建一个应用定义

在 **我的应用** 底部,点击 **创建**。会展开一个面板,含 12 种[应用类型](/zh/guide/app-types/)的 3 列网格:

**网页 · 多站点 · HTML · 离线包 · 前端 · PHP · WordPress · Node.js · Python · Go · 媒体 · 画廊**

第一个应用点击 **网页**。网页编辑器打开。

## 3. 填写基本信息

填写顶部的 **基本信息** 卡片:

- **应用名称**
- **目标 URL** —— 例如 `https://example.com`
- **图标** —— 可选;否则用类型专属默认图标

编辑器其余部分是一长串可选能力卡片(全屏、启动画面、去广告、DNS、伪装……)。现在忽略它们 —— 会使用默认值。每一项都在[应用配置](/zh/guide/config/)中说明。

点击 **保存**。在背后,编辑器组装一个 `WebApp` 对象(`appType = WEB`,带一个 `webViewConfig`),并写入 `web_apps` Room 表。你的应用现在出现在列表中。

## 4. 预览

在 **我的应用** 中,点击你的应用卡片。预览路由检查应用的 `appType` 并启动对应的运行时:

- `IMAGE` / `VIDEO` → 媒体播放器 activity
- `GALLERY` → 画廊播放器 activity
- 其他一切(包括 `WEB`)→ WebView activity

对于网页应用,WebView activity 用你配置的设置加载你的 URL —— 与导出的应用将运行的代码相同。(改为点击卡片上的 ⋮ 按钮可打开[操作菜单](/zh/guide/app-actions/edit-core-config)。)

::: warning 预览 ≠ 导出
预览走 **宿主** 路径(一切都在构建器的 classpath 上)。导出走 **shell** 路径,从嵌入的 JSON 读取你的配置。如果某个配置字段没有走完这段旅程,功能可能预览正常、导出后却消失。见[配置字段漂移](/zh/developer/config-drift)。
:::

## 5. 构建 APK

点击应用卡片上的 ⋮,再点 **构建 APK**。在对话框中你可以:

- 选择 **浏览器引擎**(系统 WebView 或 GeckoView),
- 可选启用 **资源加密**、**隔离**、**后台运行** 和 **通知**,
- 强制 **全量重建**(否则自动选择增量模式)。

点击构建。`ApkBuilder` 取得 shell 模板 APK,修改其包名 / 图标 / 权限,把你的 `WebApp` 配置作为 `app_config.json` 嵌入,并签名(V1/V2/V3)。见[构建 APK](/zh/guide/app-actions/build-apk)。

## 6. 安装

从我的应用右上角打开 **⋮ → [文件管理](/zh/guide/more-features/file-manager)**。你的 APK 就在那里 —— 安装或分享。启动它时,那个 APK 以 **shell 模式**(`SHELL_RUNTIME_ONLY = true`)运行:`ShellModeManager` 读取*你*嵌入的 `app_config.json` 并驱动运行时,完全独立于构建器。

## 下一步

- 导览[主界面](/zh/guide/main-screen/my-apps)。
- 了解每种[应用类型](/zh/guide/app-types/)能做什么。
- 探索各应用[应用功能](/zh/guide/app-actions/edit-core-config)。
- 打开右上角 **⋮** 菜单 —— 见[更多功能](/zh/guide/more-features/agent)。

## 从源码构建

要求:Android Studio Hedgehog 或更新版本,JDK 17。Gradle wrapper 固定为 Gradle 9.4.1。

```bash
git clone https://github.com/shiaho777/web-to-app.git
cd web-to-app
./gradlew assembleDebug
```

发布构建需通过 `local.properties` 和 `app/build.gradle.kts` 配置签名。
