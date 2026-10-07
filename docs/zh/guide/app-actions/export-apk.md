# 导出源码

把应用打成可以在 Android Studio 里打开并编译的工程，并打开系统分享面板，方便保存或发送。

## 入口

- 首页应用卡片点 ⋮，再点 **导出源码**。
- 构建页在本次构建成功后，构建摘要上有 **导出源码**。

## 压缩包里有什么

用 Android Studio 打开这个文件夹即可同步并构建。

- `settings.gradle.kts`、`build.gradle.kts` 和 `app/build.gradle.kts` 是 Gradle 工程。
- `app/src/main/java/.../MainActivity.kt` 用 WebView 打开这个应用的页面或本地文件。
- `app/src/main/AndroidManifest.xml` 是包名、权限和图标。
- `app/src/main/assets/app_config.json` 是写进 WebToApp APK 的运行时配置。资源加密的安装包里这份文件是密文，这里是明文。
- `app/src/main/res/xml/network_security_config.xml` 和 `res/raw/` 是网络信任设置。
- `app/src/main/assets/www/` 和 `assets/files/` 是本地 HTML、前端、画廊、启动画面和音频。不含 `node_modules` 和 `.git`。

## 它不是什么

在 WebToApp 里直接安装的仍然是[构建出的 APK](/zh/guide/app-actions/build-apk)或 [Play AAB](/zh/guide/more-features/google-play)。这份工程用同样的名称、包名、权限和本地文件编出一个 WebView 应用。它不含签名密钥库，也不包含 WebToApp 壳的全部运行时。

压缩包里可能有你配置的激活密钥和代理凭据。

## 在设备之间迁移

整份工作区用[数据备份](/zh/guide/more-features/data-backup)。源码压缩包是单个应用的工程，不是恢复包。
