# APK导出配置

生成 APK 的打包与身份设置。这是编辑器中的导出抽屉。

**位置:**[编辑通用配置](/zh/guide/app-actions/edit-common-config/)编辑器中的 **APK导出配置** 抽屉(由 `ApkExportConfig` 支撑)。

## 身份

- **自定义包名** —— APK 的 application id(按包名模式校验)。
- **版本号 / 版本名** —— APK 版本;若包名已安装,构建器可建议下一个 version code。
- **引擎类型** —— 导出应用使用系统 WebView 或 GeckoView。

## 签名

- **密钥库** —— 创建/导入/管理签名密钥(PKCS12/PFX/JKS/BKS)。
- **签名方案** —— V1/V2/V3 独立,旧证书自动回退,可自定义 V1 签名者文件名。

## 运行时权限

- 权限由启用的功能派生(功能驱动),构建时未使用的权限会从模板 manifest 裁剪。

## 网络信任

- **客户端证书认证 (mTLS)** —— 当服务器请求客户端证书时，生成的应用会打开 Android 系统证书选择器，并使用设备上已安装的身份凭据。这与信任服务器 CA 是两项不同的设置。选定的身份会在后续连接同一服务器时自动复用。

## 构建时选项(在构建对话框中)

这些在你[构建 APK](/zh/guide/app-actions/build-apk) 时选择:

- **资源加密** —— PBKDF2 + AES-256-GCM,可选自定义密码。
- **隔离** —— 按应用隔离存储/WebRTC/Canvas/Audio/WebGL/字体/头部/IP。
- **后台运行** —— 保持服务存活(`backgroundRunConfig`)。
- **通知** —— 定时/持久通知和轮询(`notificationConfig`)。
- **强制全量重建** —— 跳过增量缓存。

## 说明

- 要发布到 Play 商店,从 [Google Play](/zh/guide/more-features/google-play) 导出 AAB,它会把 `targetSdk` 重写到 Play 要求的级别。
