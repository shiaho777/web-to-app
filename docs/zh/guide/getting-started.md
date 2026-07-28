# 快速开始

本教程按应用内的真实流程,带你从全新安装走到第一个已签名的 APK。

## 1. 安装 WebToApp

在 **Android 6.0(API 23)或更高版本** 的设备上安装 WebToApp 构建器。首次启动会进入 **我的应用** —— 即列出你所创建的全部应用的主页。主界面导览见[主界面](/zh/guide/main-screen/my-apps)。

## 2. 打开创建菜单

在 **我的应用** 底部,点击 **创建** 按钮。会展开一个 3 列网格的应用类型面板:

**网页 · 多站点 · HTML · 离线包 · 前端 · PHP · WordPress · Node.js · Python · Go · 媒体 · 画廊**

第一个应用,点击 **网页**。

## 3. 填写基本信息

网页编辑器打开。填写顶部的 **基本信息** 卡片:

- **应用名称** —— 随意
- **目标 URL** —— 例如 `https://example.com`
- **图标** —— 选一张图片(可选,否则用类型专属默认图标)

编辑器其余部分是一长串可选能力卡片(全屏、启动画面、去广告、DNS、指纹伪装等)。现在可以全部忽略 —— 会使用合理默认值。每一项都在[应用配置](/zh/guide/config/)中说明。

点击 **保存**。你的应用现在出现在 **我的应用** 的列表中。

## 4. 预览

在 **我的应用** 中,点击你的应用卡片。WebToApp 会以预览方式启动它,行为与导出后完全一致。(改为点击卡片上的 ⋮ 按钮可打开操作菜单 —— 见[应用功能](/zh/guide/app-actions/edit-core-config)。)

::: warning 预览 ≠ 导出
预览和导出共享同一套运行时代码,但导出还会把你的配置序列化进生成的 APK。如果某功能预览正常、导出后失效,通常是某个配置字段没有贯通导出链路。见[配置字段漂移](/zh/developer/config-drift)。
:::

## 5. 构建 APK

回到 **我的应用**,点击应用卡片上的 ⋮ 按钮,再点 **构建 APK**。在对话框中你可以:

- 选择 **浏览器引擎**(系统 WebView 或 GeckoView),
- 可选启用 **资源加密**、**隔离**、**后台运行** 和 **通知**,
- 强制 **全量重建**(否则自动选择增量模式)。

点击构建。WebToApp 修改 shell 模板、嵌入你的配置与内容并签名。完成后,APK 就绪。

## 6. 找到并安装

从 **我的应用** 右上角打开 **⋮ → 文件管理**。你的构建产物就在那里 —— 安装到设备,或分享到另一台设备。启动它:它运行 shell 运行时,读取*你*嵌入的配置,独立于构建器。

## 下一步

- 导览[主界面](/zh/guide/main-screen/my-apps)。
- 了解每种[应用类型](/zh/guide/app-types/)能做什么。
- 探索各应用[应用功能](/zh/guide/app-actions/edit-core-config)(快捷方式、分享、导出、AAB)。
- 打开右上角 **⋮** 菜单 —— 见[更多功能](/zh/guide/more-features/ai-coding)。

## 从源码构建

要求:Android Studio Hedgehog 或更新版本,JDK 17。Gradle wrapper 固定为 Gradle 9.4.1。

```bash
git clone https://github.com/shiaho777/web-to-app.git
cd web-to-app
./gradlew assembleDebug
```

发布构建需通过 `local.properties` 和 `app/build.gradle.kts` 配置签名。
