# 高级设置

一大组浏览器行为开关。这张卡片汇集了高级 `WebViewConfig` 选项。

**位置:**[编辑通用配置](/zh/guide/app-actions/edit-common-config/)编辑器中的 **高级设置** 卡片。

## User-Agent 与渲染

- **User-Agent 模式** —— 系统默认或自定义 UA 字符串(`userAgentMode`、`customUserAgent`)。
- **桌面模式** —— 请求桌面版站点(`desktopMode`)。
- **缩放** —— 启用双指缩放(`zoomEnabled`)。
- **页面缩放** —— 构建期的按应用整页缩放百分比,可选 Chrome 式档位(50%–150%)或自由输入(`pageZoomPercent`,默认 100)。每次运行(含冷启动)通过 `setInitialScale` 应用——文字与布局/图片/画布一起缩放(不同于只缩放字形的 `textZoom`),无需运行时工具栏。旧数据中的 `0` 视作 100。
- **视口模式** —— 默认或自定义视口宽度(`viewportMode`、`customViewportWidth`)。

## 导航与刷新

- **下拉刷新** —— 拉动刷新(`swipeRefreshEnabled`)。
- **自动刷新** —— 定期重载,带间隔和倒计时(`autoRefreshEnabled`、`autoRefreshIntervalSec`)。
- **新窗口行为** —— 弹窗/新窗口如何打开(`newWindowBehavior`:同窗口、外部、弹窗……)。
- **弹窗拦截** —— 拦截弹窗(`popupBlockerEnabled`)。

## 下载

- **下载** —— 启用下载并选择位置(`downloadEnabled`、`downloadLocationMode`:系统 / 应用私有 / 自定义 SAF 目录)。

## 网络与隐私

- **代理** —— 静态 HTTP/HTTPS/SOCKS5 或 PAC,带认证和绕过规则(`proxyMode`、`proxyHost`、`pacUrl`……)。
- **TLS 指纹** —— 模拟浏览器 JA3 配置(`tlsFingerprintEnabled`、`tlsFingerprintTemplate` 如 `CHROME_131`)。
- **CORS 绕过** —— 为跨源 SPA 绕过 CORS(`enableCorsBypass`)。
- **混合内容** —— 允许/兼容模式(`allowMixedContent`、`mixedContentMode`)。
- **私有网络桥** —— 桥接私有网络请求(`enablePrivateNetworkBridge`、`privateNetworkScope`)。
- **Hosts 映射** —— host → IP 覆盖(`hostsMappingEnabled`、`hostsMappings`)。
- **Cookie** —— 第三方 Cookie 和持久化(`acceptThirdPartyCookies`、`thirdPartyCookieMode`)。
- **地理位置** —— 启用,带精度和策略(`geolocationEnabled`、`geolocationAccuracy`、`geolocationPolicy`)。

## 内核与状态栏

- **内核伪装** —— 呈现不同的浏览器内核风味(`enableKernelDisguise`、`kernelFlavor`、`kernelDisguiseLevel`)。
- **Cloudflare 兼容** —— Cloudflare 挑战的兼容模式(`enableCloudflareCompat`、`cloudflareCompatMode`)。
- **故障转移** —— 镜像 URL,带触发条件和超时(`failoverEnabled`、`failoverUrls`、`failoverTimeoutSeconds`)。

状态栏颜色/外观配置(颜色模式 `THEME`/`PAGE_TOP`/`TRANSPARENT`/`CUSTOM`、自定义颜色、深色图标、背景颜色/图片,明暗分别配置)位于[全屏模式](/zh/guide/app-actions/edit-common-config/fullscreen)卡片内的可展开区。

## 说明

- DNS 在[自定义DNS](/zh/guide/app-actions/edit-common-config/custom-dns)中配置。
- 最特殊的开关(polyfill、原生桥、打印桥等)在[特殊设置](/zh/guide/app-actions/edit-common-config/special-settings)中。
