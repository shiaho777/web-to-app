# Shell 同步与模板

生成的 APK 运行 **shell** 运行时,其 Kotlin 源码从 `app/` 同步而来。理解这套同步机制至关重要:改错副本,你的改动要么被覆盖,要么悄悄分叉。

## 规则

> **共享运行时代码在 `app/` 中编写。** 只编辑 `shell/src` 下的文件通常是错的;它会在同步时被覆盖,或与宿主分叉。

## 什么进入 shell

`shell/build.gradle.kts` 定义了一个带 include/exclude 列表的 `syncShellRuntimeSources` 任务,从 `app/` 选出完整的运行时集合(如 `core/shell`、`core/webview`、`core/engine`、`core/extension`、`ui/shell`)。shell 专属的覆盖位于 `shell/src/main/java-overrides/`。

## 构建模板

```bash
./gradlew :shell:assembleRelease :app:syncShellTemplateApk --no-configuration-cache
```

这会在 `app/src/main/assets/template/webview_shell.apk` 产出唯一的标准模板。

| 关注点 | 路径 |
| --- | --- |
| 什么进入 shell | `shell/build.gradle.kts` → `syncShellRuntimeSources` 的 include/exclude |
| shell 模板构建 | `:shell:assembleRelease` + `:app:syncShellTemplateApk` |
| 模板输出 | `app/src/main/assets/template/webview_shell.apk` |
| 配置 → shell JSON | `app/.../apkbuilder/ApkConfigJsonFactory.kt` |
| shell 配置类型 | `app/.../core/shell/ShellModeManager.kt` |
| 运行时 WebView 配置 | `app/.../ui/shell/ShellWebViewConfig.kt` |

## shell 运行时入口

运行时,生成的 APK:

1. `WebToAppApplication` 启动。
2. `ShellModeManager.isShellMode()` 检查 assets 中是否有 `app_config.json`。
3. 若有,`getConfig()` 将其(经 Gson)反序列化为 `ShellConfig` —— 可能先解密。
4. `ShellServerLauncher` 解析并启动服务端运行时;`ShellRuntimeServices` 初始化运行时栈。

## 约束

- **唯一的 shell 模板。** 不要引入第二个模板 APK。
- **低 `targetSdk`(28)。** fork+exec 运行时所需。不要随意抬高。
- **精简依赖集。** 不要把宿主专属依赖拉进 `shell/build.gradle.kts`。
- **通知失败要软处理。** FGS / 通知渠道创建必须使用 `SafeNotificationChannels`;渠道创建失败不得导致 FGS 启动崩溃。
- **配置缓存安全。** 自定义 Gradle 任务(`syncCloneHostDex` 等)必须在配置期捕获 `File`/`Provider` 值 —— 不要在任务闭包内引用 `Project`/`android.sdkDirectory`。

## 改动 shell 成员之后

如果你改变了什么进入 shell、或模板如何打包,**重建你所改动的模板。** 陈旧模板是"预览正常、导出失效"最常见的原因之一。

```bash
./gradlew :shell:assembleRelease :app:syncShellTemplateApk --no-configuration-cache
```
