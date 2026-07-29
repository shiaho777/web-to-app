# 网页

把远程 URL 套进 WebView。这是最常见、也是配置最丰富的应用类型。

## 适用场景

已经有一个 URL 的落地页、工具、仪表盘、文档和内部系统。

## 核心配置

网页类型的核心配置是 WebView 行为(由 `WebViewConfig` 支撑)。

### 目标与引擎

- **目标 URL** —— 要加载的站点。
- **浏览器引擎** —— 默认系统 WebView;可选 GeckoView(首次使用下载,ECH 必需)。见[浏览器内核](/zh/guide/more-features/browser-kernel)。

### User-Agent 与显示

- **User-Agent 模式** —— 系统默认或自定义 UA 字符串(`userAgentMode`、`customUserAgent`)。
- **桌面模式** —— 请求桌面版站点(`desktopMode`)。
- **缩放与视口** —— 启用缩放、初始缩放和视口模式。

### 注入

- **JS/CSS 注入** —— 在 document-start、document-end 或 idle 注入脚本/样式(`injectScripts`)。

### 弹窗与窗口

- **新窗口行为** —— 弹窗/新窗口如何打开(`newWindowBehavior`:同窗口、外部浏览器、弹窗)。
- **弹窗拦截** —— 拦截弹窗(`popupBlockerEnabled`)。
- **JS 可打开窗口** —— 带策略(`javaScriptCanOpenWindows`、`jsOpenWindowsPolicy`)。

## 说明

- 网页编辑器在单一界面中暴露 **完整** 的能力卡片(全屏、启动画面、去广告、DNS、伪装等)—— 网页应用有一个合并的编辑器,而非拆分的核心/通用配置。
- 多个站点合并到一个应用,用[多站点](/zh/guide/app-types/multi-web)。
- 把网站归档为离线使用,用[离线包](/zh/guide/app-types/offline-pack)。
