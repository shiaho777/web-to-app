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

## 应用回跳

- **应用回跳** —— 声明其他应用把控制权**交回**本应用的通道,这样在对方应用内完成授权后,QQ、微博等第三方登录才能回到本应用(`enableAppReturn`,默认开启)。关闭后页面仍能*打开*对方应用(任何非 http scheme 都会交给系统),但回调无处可返,系统会提示没有可用于打开的应用。
  - 只声明**回跳**通道,绝不声明 `weixin`、`alipays` 这类唤起通道:声明它们会让本应用成为对方自家链接的候选,点"微信支付"可能弹出本应用而不是微信。
  - **自定义回跳 scheme**(`customAppReturnSchemes`)—— 部分平台把回调 scheme 绑定到你在它那里注册的应用 ID(例如微信的 `wx<appid>`),无法作为默认值提供。每行或用逗号分隔一个。

### 各平台的回跳方式

声明平台的**唤起** scheme(`weixin`、`alipays`、`taobao` 等)没有帮助,反而有害:这些 scheme 是用来**打开**该平台的,一旦你也声明,本应用就成了对方自家链接的候选,用户在支付中途会看到应用选择框。应用回跳只声明**回跳**通道。

| 平台 | 如何交回控制权 | 由谁覆盖 |
| --- | --- | --- |
| QQ(网页登录) | `mqqopensdkapi://browser?url=<回调地址>` —— 与调用方无关 | 内置,默认开启 |
| 微信(SSO / H5 支付) | 回到你在微信开放平台注册的 scheme(`wx<AppID>`);H5 支付还需要商户后台把它加入白名单 | **自定义回跳 scheme** —— 先注册应用,再填 `wx<AppID>` |
| 支付宝(H5 支付 / 授权) | 回到**调用方指定**的 scheme;浏览器和应用各传各的,页面里的那个值必须被改写才能回到本应用 | 不自动 —— 见下 |
| 微博(SSO) | 回到与你的 AppKey 匹配的 scheme(`wb<AppKey>`) | **自定义回跳 scheme** |
| 淘宝 / 天猫 / 京东 / 抖音 / 哔哩哔哩 | 没有与调用方无关的回跳通道;页面原地继续,或走自己域名下的 http(s) 回调 | 站点自身的 https 回调,导出时已声明 |

对于回跳到**被封装站点自己域名下的 https 回调**的平台(支付宝 H5、微信网页授权),导出时已声明该 host,所以回调能够回到本应用 —— 当浏览器也匹配时,Android 可能询问用哪个应用打开。

## 安全与其他

- **跨源隔离** —— `enableCrossOriginIsolation`。
- **防截屏** —— 阻止屏幕截取(`antiCapture`)。
- **视频全屏时隐藏状态栏** —— 网页视频进入 HTML5 全屏播放时强制隐藏状态栏,退出全屏后恢复(`hideStatusBarInVideoFullscreen`,默认开启)。视频全屏期间优先于[全屏模式](/zh/guide/app-actions/edit-common-config/fullscreen)中的"全屏显示状态栏"选项。
- **文件 URL 的文件访问** —— `allowFileAccessFromFileURLs`、`allowUniversalAccessFromFileURLs`。
- **错误页** —— 自定义错误页配置(`errorPageConfig`)。
- **性能优化** —— `performanceOptimization`。
- **PWA 离线** —— 离线缓存策略(`pwaOfflineEnabled`、`pwaOfflineStrategy`)。
- **浮动返回按钮** —— `showFloatingBackButton`。
- **键盘调整模式** —— `keyboardAdjustMode`(resize……)。在 Android 10 及以下,RESIZE 模式走经典窗口缩放路径(窗口非 edge-to-edge),键盘可可靠地压缩内容。
- **全屏视频方向** —— 全屏视频如何转向(`fullscreenVideoOrientation`),如自动传感器横屏。
- **隐藏 URL 预览** —— `hideUrlPreview`。

## 说明

- 这些是高级用户开关;大多数应用保持默认即可。
- **默认值:**以下安全体验特性对新应用默认**开启**——剪贴板/方向/兼容 polyfill、媒体会话、分享、缩放与打印桥、图片修复、滚动记忆、返回状态保留、Blob 拦截、JS 窗口、用户激活预置、Base64 深链、应用回跳、私网桥和原生桥。刻意保持**关闭**:通知 polyfill(否则每个导出包都会弹通知权限)、定位、混合内容、第三方 Cookie、自动播放和弹窗拦截。
