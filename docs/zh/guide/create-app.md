# 创建应用

WebToApp 支持多种应用类型。每种类型决定了打包什么、生成的应用在运行时如何表现。选择与你的输入相匹配的类型。

## 应用类型

### Web
把远程 URL 套进 WebView(默认系统 WebView,可选 GeckoView)。适合落地页、仪表盘、文档和内部系统。可配置目标 URL、User-Agent、桌面模式、JS/CSS 注入和弹窗处理。

### HTML / 前端
把静态前端(React、Vue、Vite、纯 HTML)打包进 APK,并从本地文件提供服务。生成的应用获得 `allowFileAccess`,使纯文件加载可离线工作。适合离线 Web 应用和静态构建。

### Node.js
嵌入 Node.js 18.20 运行时,通过加载 `libnode.so` 的原生 `node_launcher` 包装器,在独立的 `:nodejs` 操作系统进程中运行。你的服务端代码跑在本地端口,WebView 指向 `localhost`。支持自定义原生 `.node` 扩展。

### PHP
运行 PHP 8.4(来自 `pmmp/PHP-Binaries`)+ Composer 2.10。首次使用时下载一次。支持自定义原生扩展(`zend_extension`、`.so`)。

### Python
运行 Python 3.14 —— Flask、Django、FastAPI(uvicorn)、Tornado 或内置 HTTP 服务器。pip 依赖解析进 `.pypackages`。支持自定义原生扩展。二进制名带版本号,未来升级不会写死路径。

### Go
使用官方 Go 1.26 Linux arm64 工具链(国内用 USTC 镜像)。支持设备端 `go build` / `go mod` / `go run`、`vendor/` 离线构建,以及通过原生 `go_exec_loader` 包装器提供静态服务。

### WordPress
在本地 PHP + SQLite(`sqlite-database-integration`)上运行 WordPress 7.x,支持主题和插件导入。一个装在 APK 里的便携 CMS。

### 图片 / 视频 / 图集
以媒体为核心的应用。图集应用支持分类媒体、网格/列表/时间线视图、随机/单循环、排序、缩略图栏、叠加层、自动下一个和播放记忆。

### 多网站
把多个站点合并到一个应用,提供标签、卡片、信息流或抽屉布局。每个站点可有自己的图标、主题色、提取选择器和刷新间隔,外加共享的 JS/CSS。

## 通用设置(所有类型)

无论哪种类型,每个应用都共享一大组开关,在编辑器中分组:

- **浏览器引擎与网络** —— 引擎选择、内核伪装、代理、DoH、ECH、TLS 指纹、CORS 绕过、故障转移镜像。
- **隐私与加固** —— 指纹伪装、去广告、资源加密、反调试、激活门控。
- **外观** —— 启动画面、背景音乐、工具栏、状态栏、导航、主题。
- **扩展** —— 内置模块、油猴脚本、MV3 扩展。
- **导出** —— 包名、版本、图标、架构目标、签名、AAB。

每一项都有专门的指南页。所有设置的唯一真实来源是 `WebApp` 模型及其嵌套的 `*Config` 类;你在编辑器中设置的一切都会通过完整的打包透传链流入生成的 APK。

::: tip
使用应用内置的 **示例项目**(React、Vue、Vite、Node/Express、PHP/Laravel、Python/Flask、Go/Gin、WordPress 等),在构建自己的应用之前,先看看每种技术栈的可工作配置。
:::
