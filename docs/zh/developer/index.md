# 开发者文档

本节面向参与 WebToApp 本身开发的人 —— 贡献者、深度定制者和 AI 编码代理。它解释代码库如何组织,以及两个最容易让人栽跟头的机制实际如何工作:**导出管线**和 **shell 同步**。

::: tip 权威参考
仓库根目录的 [`AGENTS.md`](https://github.com/shiaho777/web-to-app/blob/main/AGENTS.md) 是面向 AI 代理和深度贡献者的权威指南。这些页面在其基础上提供更多背景。
:::

## 仓库布局

| 路径 | 角色 |
| --- | --- |
| `app/` | 完整构建器宿主:编辑器 UI、导出管线、运行时、预览。主应用模块。 |
| `shell/` | 运行时模板。通过 `:shell:assembleRelease` + `:app:syncShellTemplateApk` 构建为 `app/src/main/assets/template/webview_shell.apk`。 |
| `clone-host/` | 宿主侧 APK 克隆 / 身份重塑支持库(编译为 DEX 资源)。 |
| `modules/` | 模块市场目录(`registry.json` + 各模块文件夹)。 |
| `scripts/` | 构建辅助与门禁(`check_config_field_drift.py`)。 |
| `docs/` | 本文档站点。 |

运行时 Kotlin 在 `app/` 下编写,由 `syncShellRuntimeSources` 同步进 `shell/`。**只在 `app/` 源码处编辑一次;不要在 `shell/` 下永久分叉副本。**

## 三个 Gradle 模块

- **`:app`** —— 构建器。`applicationId = com.webtoapp`,`compileSdk = 36`,`minSdk = 23`,`targetSdk = 28`,`buildConfigField SHELL_RUNTIME_ONLY = false`。
- **`:shell`** —— 嵌入生成 APK 的运行时模板。SDK/版本与 `:app` 相同,但 `SHELL_RUNTIME_ONLY = true`。其源码从 `app/` 同步。
- **`:clone-host`** —— 一个极简的 `com.android.library`(命名空间 `com.webtoapp.clone`),无依赖,编译为供 `AppCloner` 使用的 DEX 资源。

## 包结构(`app/src/main/java/com/webtoapp`)

- **`core/*`** —— 约 53 个业务/运行时逻辑子包:`apkbuilder`、`shell`、`webview`、`engine`、`extension`、`crypto`、`nodejs`、`php`、`python`、`golang`、`wordpress`、`linux`、`port`、`dns`、`network`、`adblock`、`agent` 等。
- **`data/*`** —— 持久化:Room DAO、数据库、类型转换器,以及 `WebApp` 模型 + 嵌套的 `*Config` 类。
- **`ui/*`** —— Jetpack Compose 界面、组件、设计系统和 shell UI。
- **`di/`** —— Koin 依赖注入。
- **`util/`** —— 辅助工具与常量。

原生 C++ 位于 `app/src/main/cpp/`(加密、完整性、反调试、`node_bridge`、`node_launcher`、`go_exec_loader`)。

## 接下来读什么

- [架构](/zh/developer/architecture) —— 预览 vs 导出的心智模型。
- [导出管线](/zh/developer/export-pipeline) —— `WebApp` 如何变成已签名的 APK。
- [Shell 同步与模板](/zh/developer/shell-sync) —— 运行时代码如何抵达生成的应用。
- [配置字段漂移](/zh/developer/config-drift) —— 最常见的静默失败。
