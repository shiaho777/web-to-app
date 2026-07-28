# 简介

**WebToApp** 把网页项目变成独立、已签名的 Android APK —— 完全在手机上完成。它不是网址套壳,而是一个掌上 APK 工坊:能运行真实的服务端运行时、搭载加固的反审查网络栈、为 Google Play 签名打包、运行 MV3 浏览器扩展,全程无需电脑或远程构建服务器。

## 它有何不同

大多数"网址转 App"工具止步于把 URL 套进 WebView。WebToApp 恰恰在最难的地方另辟蹊径:

- **在设备上运行真实的服务运行时。** Node.js、PHP、Python、Go、WordPress 作为原生二进制直接从应用存储 fork+exec —— 如同 Termux,但打包成可安装的 APK。网址套壳工具根本做不到。
- **搭载加固网络栈。** DNS-over-HTTPS、带本地 MITM 桥的 TLS 指纹伪造(Chrome / Firefox / Safari JA3 模板)、GeckoView 引擎上的加密客户端 Hello(ECH)、按应用代理,以及针对受限 SPA 的 CORS 绕过。
- **构建全程自包含。** 二进制 AXML/ARSC 打补丁、权限裁剪、V1/V2/V3 签名、Google Play 级 AAB 导出,全部通过 `apksig` 在应用内完成。
- **发布后仍可扩展。** 添加 JS/CSS 模块、Tampermonkey 风格油猴脚本或 MV3 Chrome 扩展,无需重建宿主。

## 你能构建什么

| 输入 | 输出 | 适用场景 |
| --- | --- | --- |
| 网站 URL | 基于 WebView 的 APK | 落地页、工具、仪表盘、文档、内部系统 |
| HTML / 静态前端 | 本地托管的 APK | React、Vue、Vite、静态构建、离线 Web 应用 |
| Node.js / PHP / Python / Go | 带设备端本地服务器的 APK | 小型服务端应用、管理工具、演示、原型 |
| WordPress | 在本地 PHP + SQLite 上运行 WordPress 的 APK | 便携站点、主题/插件演示、内容打包 |
| 图片 / 视频 / 图集 | 以媒体为核心的 APK | 相册、课程资料、作品集、离线查看器 |
| 多个站点 | 标签/卡片/信息流/抽屉式多网站 APK | 链接枢纽、门户、应用合集 |
| 已安装的 APK | 换壳克隆或快捷方式伪装 | 图标/名称/包名实验、重打包研究 |

## 如何阅读这些文档

- **[使用手册](/zh/guide/getting-started)** —— 安装应用并构建第一个 APK,然后配置网络、运行时、隐私与外观。
- **[开发者文档](/zh/developer/)** —— 代码如何组织、导出管线与 shell 同步如何工作,以及常见改动的配方。
- **[扩展开发](/zh/extensions/)** —— 编写 JS/CSS 模块、油猴脚本和 MV3 Chrome 扩展,并发布到应用内市场。

::: tip
宿主应用界面提供 10 种语言,可在 **设置 → 语言** 中随时切换。你*生成*的应用的语言则按应用单独配置。
:::
