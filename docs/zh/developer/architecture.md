# 架构

WebToApp 中最重要的心智模型:**预览和导出是两条共享同一套运行时代码的不同执行路径。** 大多数"预览正常、导出失效"形式的 bug,都源于破坏了这一契约。

## 两条路径

```text
编辑器(app/ 中的 Compose 界面)
  ↔ 数据模型(WebApp、各配置)
  ↔ 导出工厂(ApkConfig / ApkConfigJsonFactory)
  ↔ ApkBuilder / ApkBuildCache  →  已签名的生成 APK

app/ 源码
  → syncShellRuntimeSources  →  shell DEX  →  webview_shell.apk(模板)

生成 APK 运行时
  WebToAppApplication → ShellModeManager → 加载 assets JSON 配置
  → WebViewManager / 运行时服务器(Node/PHP/Python/Go/WordPress)
```

| | 宿主 `:app`(预览) | 生成 APK(导出) |
| -- | --------------------- | ---------------------- |
| DEX | 全部 `app/src` 类 | shell 同步的子集(完整运行时集合) |
| 配置 | 编辑器 / 内存模型 | 通过 `ShellModeManager` 读 assets JSON |
| 模板 | 不使用 | `app/src/main/assets/template/webview_shell.apk` |

## 契约

编辑器中设置的开关,除非贯通以下**全部**环节,否则在导出时毫无用处:

1. **模型** —— `WebApp` / 嵌套配置,绑定到编辑器 UI。
2. **导出映射** —— `ApkBuilder` / `ApkConfig` / `ApkConfigJsonFactory`。
3. **Shell 配置类型** —— `ShellModeManager` / shell 配置数据类(如果运行时读取它)。
4. **运行时使用点** —— 在 shell 同步的代码中。

漏掉任何一步,你会得到三种症状之一:编辑器显示一个导出会忽略的开关、导出嵌入了运行时从不读取的配置,或运行时读取一个从未被写入的字段。

## 唯一的 shell 模板

只有**一个** shell 模板:来自 `:shell` release 的 `webview_shell.apk`。不要引入第二个模板 APK。生成的应用在 shell 路径上保持较低的 `targetSdk`(28),因为它们依赖设备端 fork+exec 运行时 —— 不要随意抬高 shell 的 `targetSdk`。

## 配置中心

所有功能设置的唯一真实来源是 `WebApp`(`data/model/WebApp.kt`)及其 `*Config` 类,通过完整的打包透传链传入生成的 APK。运行时,生成的应用通过 `ShellModeManager` 从 assets 中的 `app_config.json` 读取配置。

## 依赖政策

除非有强有力的理由,否则避免新增第三方依赖(`app/build.gradle.kts` / `shell/build.gradle.kts`)。优先使用平台 API 和现有模块。shell 有一套精简的依赖和较低的 `targetSdk` —— 保持如此。
