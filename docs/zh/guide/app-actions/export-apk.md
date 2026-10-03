# 导出源码

把应用的源码打成压缩包，并打开系统分享面板，方便保存或发送。

## 入口

- 首页应用卡片点 ⋮，再点 **导出源码**。
- 构建页在本次构建成功后，构建摘要上有 **导出源码**。

## 压缩包里有什么

- `README.md` 说明这份压缩包。
- `app_config.json` 是写进 APK 的运行时配置。资源加密的安装包里这份文件是密文，这里是明文。
- `network_security_config.xml` 和 `certs/` 是网络信任设置，完整证书链（根 CA 与中间 CA）用这两处。
- `content/` 是属于该应用的本地 HTML、前端、画廊、启动画面和音频。不含 `node_modules` 和 `.git`。

## 它不是什么

可安装的文件仍然是[构建出的 APK](/zh/guide/app-actions/build-apk)或 [Play AAB](/zh/guide/more-features/google-play)。这份压缩包是那些构建所运行的定义：WebToApp 的壳加上这些文件。它不是 Android Studio 工程，也不含签名密钥库。

压缩包里可能有你配置的激活密钥和代理凭据。

## 在设备之间迁移

整份工作区用[数据备份](/zh/guide/more-features/data-backup)。源码压缩包是单个应用可读的定义，不是恢复包。
