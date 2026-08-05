# 特殊设置

兼容性 polyfill、桥接和其他专门开关。这张卡片汇集了专门的 `WebViewConfig` 选项。

**位置:**[编辑通用配置](/zh/guide/app-actions/edit-common-config/)编辑器中的 **特殊设置** 卡片。

## Polyfill 与桥接

- **剪贴板 polyfill** —— `enableClipboardPolyfill`。
- **通知 polyfill** —— Web Notification 支持(`enableNotificationPolyfill`)。
- **方向 polyfill** —— `enableOrientationPolyfill`。
- **兼容 polyfill** —— 一组兼容 shim(`enableCompatPolyfills`)。
- **原生桥** —— 暴露带能力门控的原生桥(`enableNativeBridge`、`nativeBridgeCapabilities`)。
- **打印桥** —— 拦截 `window.print()` 和 PDF 输出到 Android 打印框架(`enablePrintBridge`)。
- **媒体会话桥** —— 把网页媒体接入系统媒体通知和锁屏控制,支持蓝牙耳机和 Android Auto(`enableMediaSession`)。
- **分享桥** —— `enableShareBridge`。
- **缩放 polyfill** —— `enableZoomPolyfill`。

## 媒体与内容

- **媒体自动播放** —— 带范围(`mediaAutoplayEnabled`、`mediaAutoplayScope`:仅视频……)。
- **图片修复** —— 修复损坏的图片(`enableImageRepair`)。
- **滚动记忆** —— 记住滚动位置(`enableScrollMemory`)。
- **返回状态保留** —— `enableBackStatePreservation`。
- **Blob 下载拦截** —— 带范围和大小阈值(`enableBlobDownloadInterception`、`blobInterceptThresholdMb`)。

## JavaScript 与窗口

- **JS 可打开窗口** —— 带策略(`javaScriptCanOpenWindows`、`jsOpenWindowsPolicy`)。
- **预置用户激活** —— 合成用户手势,带模式和时机(`primeUserActivation`、`primeUserActivationMode`、`primeUserActivationTiming`)。
- **Base64 深度链接** —— 解码 base64 深度链接,仅手势或总是(`decodeBase64DeepLinks`、`decodeBase64Mode`)。

## 安全与其他

- **跨源隔离** —— `enableCrossOriginIsolation`。
- **防截屏** —— 阻止屏幕截取(`antiCapture`)。
- **文件 URL 的文件访问** —— `allowFileAccessFromFileURLs`、`allowUniversalAccessFromFileURLs`。
- **错误页** —— 自定义错误页配置(`errorPageConfig`)。
- **性能优化** —— `performanceOptimization`。
- **PWA 离线** —— 离线缓存策略(`pwaOfflineEnabled`、`pwaOfflineStrategy`)。
- **浮动返回按钮** —— `showFloatingBackButton`。
- **键盘调整模式** —— `keyboardAdjustMode`(resize……)。
- **隐藏 URL 预览** —— `hideUrlPreview`。

## 说明

- 这些是高级用户开关;大多数应用保持默认即可。
