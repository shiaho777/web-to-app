# APK导出配置

生成 APK 的打包与身份设置。这是编辑器中的导出抽屉。

**位置:**[编辑通用配置](/zh/guide/app-actions/edit-common-config/)编辑器中的 **APK导出配置** 抽屉(由 `ApkExportConfig` 支撑)。

## 身份

- **自定义包名** —— APK 的 application id(按包名模式校验)。
- **版本号 / 版本名** —— APK 版本。
- **自动提升版本号** —— 当设备已安装同包名且 versionCode 更高的应用时,构建器自动提升版本以便覆盖安装。默认开启;关闭后始终使用上方填写的版本(降级安装可能因此失败)。
- **引擎类型** —— 导出应用使用系统 WebView 或 GeckoView。

## 签名

- **密钥库** —— 创建/导入/管理签名密钥(PKCS12/PFX/JKS/BKS)。
- **签名方案** —— V1/V2/V3 独立,旧证书自动回退,可自定义 V1 签名者文件名。

## 运行时权限

- 权限由启用的功能派生(功能驱动),构建时未使用的权限会从模板 manifest 裁剪。

## 静态 SAEP 策略

导出抽屉和构建 APK 页面中的可选 **静态 SAEP 策略** 开关按应用保存 (`ApkExportConfig.saepEnabled`)。默认 **关闭**，旧配置也保持关闭：关闭只省略 SAEP 元数据，并非声明拒绝策略。开启通过静态策略元数据声明 **无额外限制**，不会授予 Android 权限、绕过授权，也不保证智能体支持 SAEP。

此功能只作用于生成的 APK，不修改构建器宿主的策略。导出时以真实 Android 资源引用写入 `com.obric.agentrobots.POLICY_JSON`，策略绑定最终包名，在开启资源加密后仍可被系统读取。切换开关会触发完整重建。需要重新构建壳模板；旧模板缺少策略资源时，启用 SAEP 将明确报错，而非输出无效声明。系统及智能体是否支持、自动化操作能否成功，仍须在兼容设备上验证。参见[官方 SAEP 示例](https://github.com/bytedance/SAEP-demo)。

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
