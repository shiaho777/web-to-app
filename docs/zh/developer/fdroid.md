# F-Droid 变体

GitHub、Play 和 F-Droid 共用同一份源码。F-Droid 构建是 Gradle 属性，不是长期存在的 git 分支。

```bash
./gradlew :app:assembleStandardRelease -Pfdroid=true
```

产物：`app/build/outputs/apk/standard/release/app-standard-release-unsigned.apk`。由 F-Droid 签名。不要在同一次 Gradle 调用里把它和普通 release 混编——一次调用只有一份 shell 模板。

## 这个开关做什么

| 部分 | `-Pfdroid=true` |
| --- | --- |
| Firebase Cloud Messaging | 不编入。推送仍走 Web Notification polyfill、HTTP 轮询和 WebSocket。 |
| Google 登录（Play 服务 / Credential Manager） | 不编入。`NativeBridge.googleSignIn` 返回 `UNAVAILABLE`。 |
| AdMob 功能栈 | 不 include `:feature-stacks:admob`。编辑器广告项隐藏。导出写入关闭的 ads 块。 |
| Play Services FIDO（GeckoView WebAuthn） | 排除。GeckoView 其余路径不变。 |
| Release 签名 | `signingConfig` 为空，APK 保持未签名。 |
| 宿主编辑器 | 隐藏 FCM / Google 登录 / AdMob 入口。配置字段留在 schema 里，导出时空操作，不拆 JSON。 |

`:app` 和 `:shell` 的 `BuildConfig.FDROID` 都为 `true`。带 Google 类型的源码在 `app/src/google/java`；同 FQCN 的 F-Droid 空实现在 `app/src/fdroid/java`。Shell 同步只拷其中一棵树。

## CI

`check` 任务用**第二次** Gradle 调用编译这个变体，避免污染普通构建：

```text
./gradlew -PskipShellTemplateSync=true -Pfdroid=true \
  :app:compileStandardReleaseKotlin \
  :shell:compileReleaseKotlin \
  :app:processStandardReleaseManifest \
  :shell:processReleaseManifest
```

## 商店文案

Fastlane 文件在 `fastlane/metadata/android/<locale>/`。F-Droid 从本仓库读取。

提交到 [fdroiddata](https://gitlab.com/fdroid/fdroiddata) 时，复制本仓库的 `metadata/com.webtoapp.yml`。MR 标题：`New app: WebToApp`。

```yaml
Categories:
  - Development
  - Internet
License: Unlicense
SourceCode: https://github.com/shiaho777/web-to-app
IssueTracker: https://github.com/shiaho777/web-to-app/issues
Changelog: https://github.com/shiaho777/web-to-app/releases
WebSite: https://shiaho777.github.io/web-to-app/

RepoType: git
Repo: https://github.com/shiaho777/web-to-app.git

Builds:
  - versionName: "2.7.9"
    versionCode: 77
    commit: v2.7.9
    subdir: app
    gradle:
      - standard
    gradleprops:
      - fdroid=true
    ndk: r28c
    rm:
      - feature-stacks/admob
      - app/src/google

AutoUpdateMode: Version
UpdateCheckMode: Tags
UpdateCheckData: app/build.gradle.kts|versionCode = (\d+)|.|versionName = "([^"]+)"
```

使用与 GitHub Releases 相同的 tag（`v2.7.9`）。两个渠道同一版本；F-Droid 用自己的签名，两边的 APK 不能互相覆盖更新。

## 不要混用模板

带 `-Pfdroid=true` 的 assemble 会重建不含 Firebase 的 `webview_shell.apk`，并拷进 `app/src/main/assets/template/`。做普通 GitHub/Play 模板构建前，从 git 恢复该文件，或**不要**加 `-Pfdroid` 再跑 `:shell:assembleRelease :app:syncShellTemplateApk`。
