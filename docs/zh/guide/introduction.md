# 简介

WebToApp 是一个 Android 应用,它**在设备上**把网页项目变成可安装的 APK。本页从代码的角度说明它到底是什么,让你在上手前心里有数。

## 具体而言,这个应用是什么

WebToApp 的核心是管理一份**应用定义**列表。每个定义是一条 `WebApp` 记录,存储在本地 Room 数据库(`web_apps` 表)中。一个 `WebApp` 包含:

- **身份** —— `name`、`url`、`iconPath`、`packageName`,以及一个 `appType`。
- **类型专属配置** —— `mediaConfig`、`galleryConfig`、`htmlConfig`、`wordpressConfig`、`nodejsConfig`、`phpAppConfig`、`pythonAppConfig`、`goAppConfig` 或 `multiWebConfig` 之一,取决于类型。
- **功能开关 + 配置** —— 激活、广告、公告、去广告、WebView 设置、启动画面、背景音乐、翻译、扩展、自启动、伪装,以及用于打包的 `apkExportConfig`。

当你"构建"一个应用时,构建器取得 shell 模板 APK,修改其身份与资源,把你的 `WebApp` 配置作为 assets JSON 嵌入,并签名。产物是一个可安装或可分享的独立 APK。

## 12 种应用类型

`AppType` 枚举定义了一个应用可以是什么:

`WEB` · `IMAGE` · `VIDEO` · `HTML` · `GALLERY` · `FRONTEND` · `WORDPRESS` · `NODEJS_APP` · `PHP_APP` · `PYTHON_APP` · `GO_APP` · `MULTI_WEB`

网页类类型在 WebView 中加载 URL 或本地文件;运行时类型(`NODEJS_APP`、`PHP_APP`、`PYTHON_APP`、`GO_APP`、`WORDPRESS`)在设备上 fork 一个原生服务二进制,并把 WebView 指向本地端口;媒体类型(`IMAGE`、`VIDEO`、`GALLERY`)直接播放内容。每种见[创建应用](/zh/guide/app-types/)。

## 一套代码,两种运行方式

同一个 `WebToAppApplication` 以两种模式运行,由一个构建标志选择:

- **构建器(宿主)** —— `SHELL_RUNTIME_ONLY = false`。这是你从商店安装的应用:编辑器、应用列表和导出管线,所有东西都在主 classpath 上。
- **生成的应用(shell 运行时)** —— `SHELL_RUNTIME_ONLY = true`。导出的 APK 运行同步来的 shell 运行时,并通过 `ShellModeManager` 从 `app_config.json` 读取*你*嵌入的配置。Node.js 甚至运行在独立的 `:nodejs` 操作系统进程中。

这正是"预览正常、导出失效"会成为真实故障模式的原因:预览走宿主路径,导出走 shell 路径,而配置字段必须在这两者之间存活下来。见[配置字段漂移](/zh/developer/config-drift)。

## 各界面在哪里

- [我的应用](/zh/guide/main-screen/my-apps) —— 主页:你的应用列表、分类和创建按钮。
- [创建应用](/zh/guide/app-types/) —— 12 种应用类型及其创建流程。
- [应用功能](/zh/guide/app-actions/edit-core-config) —— 每个应用能做什么(编辑、构建、分享、导出……)。
- [更多功能](/zh/guide/more-features/agent) —— 右上角 ⋮ 菜单后的全局工具。
- [应用配置](/zh/guide/config/) —— 共享的按应用选项(网络、隐私、外观、运行时)。

## 如何阅读这些文档

- **[开始](/zh/guide/getting-started)** —— 构建第一个 APK 并导览主界面。
- **[开发者文档](/zh/developer/)** —— 代码库布局、导出管线、shell 同步和改动配方。
- **[扩展开发](/zh/extensions/)** —— 编写 JS/CSS 模块、油猴脚本和 MV3 Chrome 扩展。

::: tip
构建器界面提供 10 种语言 —— 从顶栏的[语言按钮](/zh/guide/main-screen/language)切换。你*生成*的应用的语言按应用配置。
:::
