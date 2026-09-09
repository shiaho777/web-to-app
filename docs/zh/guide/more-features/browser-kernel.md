# 浏览器内核

管理你的应用可用的浏览器引擎。从 [⋮ → 浏览器内核](/zh/guide/main-screen/more) 打开。

## 功能

- **当前 WebView 信息** —— 查看本设备的系统 WebView 版本。
- **内置引擎** —— 下载、管理和删除可选的 GeckoView(Firefox)运行时,用于 ECH / SNI 加密。显示下载大小和进度;沉重的原生产物在首次使用时获取。
- **更改 WebView 提供者** —— 切换系统 WebView 提供者(带开发者选项步骤指引)。
- **引擎说明** —— Chrome、Edge、Brave、Firefox 和 Via 的参考信息。

## 说明

- 各应用的引擎选择在[构建 APK](/zh/guide/app-actions/build-apk) 对话框中进行;本界面管理引擎本身。
- GeckoView 是 [ECH](/zh/guide/config/network) 所必需的。

## GeckoView(Firefox 内核)功能支持说明

GeckoView 是独立于系统 WebView 的完整第二引擎,而不是 WebView 的皮肤:网络层能力由内核原生实现,而一部分依赖 WebView 专有机制(请求拦截、`addJavascriptInterface`、JS 注入)的功能在 GeckoView 下不可用。构建 APK 选择内核前,请先读完本节。

### 完整支持(原生实现,与系统 WebView 无差异)

- **ECH(加密 SNI)** —— 仅 GeckoView 支持;开启 ECH 会自动切换到该内核
- DoH、静态 / PAC / SOCKS5 代理、防抓包(强制直连、忽略系统代理)
- UA 模式与自定义 User-Agent、桌面模式
- JavaScript 开关、自动播放策略、视口模式(适应屏幕 / 桌面)、启动时清除浏览数据、下载开关
- HTTP 下载、全屏视频、文件上传(含拍照)、渲染崩溃自动恢复
- 摄像头 / 麦克风与地理位置授权策略(全部拒绝 / 按主机记住)
- mTLS 客户端证书、HTTP Basic / 代理认证对话框
- JS 对话框(alert / confirm / prompt)
- 页面加载错误提示、前进 / 后退历史导航、自动刷新、页内查找、控制台脚本执行
- 媒体会话 / 锁屏播放控制(Gecko 原生实现)

### 暂不可用(需要这些功能请选择系统 WebView 内核)

- **[广告拦截](/zh/guide/app-actions/edit-common-config/ad-blocking)** —— 网络规则过滤与元素隐藏均不生效
- **[用户脚本](/zh/extensions/userscript)(GM_\* API)与 [Chrome MV3 扩展](/zh/extensions/chrome-mv3)**(含 declarativeNetRequest / webRequest 规则)
- **[NativeBridge](/zh/extensions/api-reference) 完整 JS API** —— 剪贴板、通知、震动、打印、分享、屏幕常亮、屏幕方向、Google 登录等;GeckoView 下仅 CORS 绕过 / 私有网络访问的 `httpRequest` 可用
- **JS 注入类功能** —— 浏览器指纹伪装、[设备伪装](/zh/guide/app-actions/edit-common-config/device-disguise)、内核伪装与风味、[页面翻译](/zh/guide/app-actions/edit-common-config/translate)、音频解锁、剪贴板 / 通知 / 屏幕方向 polyfill、Cloudflare 兼容辅助、自定义注入脚本、弹窗拦截、滚动位置记忆、图片修复、隐藏链接预览
- **PWA 离线(Service Worker 注入)、静态资产包、资源加密加载** —— 因此**开启资源加密的 HTML / [前端离线包](/zh/guide/app-types/frontend)类应用必须使用系统 WebView 内核**
- blob / data: 下载拦截
- [长按菜单](/zh/guide/app-actions/edit-common-config/long-press-menu)(保存图片 / 视频、复制链接等)
- 状态栏自动取色(跟随页面顶部颜色;纯色 / 主题模式不受影响)
- 页面缩放设置(缩放比例 / 初始缩放 / 文字缩放;GeckoView 仅保留自带的双指缩放)
- 跟随系统深色模式
- Cookie 策略(GeckoView 固定接受全部 Cookie)
- Hosts 映射(域名 → IP)
- 控制台消息回传与 eval 结果返回(脚本会执行,但面板收不到消息与返回值)
- 新窗口策略(GeckoView 弹窗一律在当前窗口加载)
- 代理的用户名 / 密码认证
- WebView 状态保存 / 恢复(进程被杀后的会话还原)

### 设计上不支持(属于取舍,不是缺陷)

- **TLS 指纹伪装(MITM 桥)** —— GeckoView 原生携带真实的 Firefox TLS / JA3 指纹,本身就是最强"伪装";MITM 桥会终止 TLS 重签,与 ECH 直接冲突。因此该功能仅在系统 WebView 内核下提供。使用 GeckoView 时,目标网站看到的就是货真价实的 Firefox。
- **SSL 证书错误"仍要继续"** —— GeckoView 不允许忽略证书错误;编辑器导入的自定义 CA 也不会被 GeckoView 信任(仅可通过"信任用户证书"开关信任安装进 Android 系统的用户 CA)。
- DOM 存储 / 数据库无法关闭(GeckoView 始终开启)。
- 悬浮窗模式固定使用系统 WebView,不跟随各应用的内核选择。

### 选型建议

- 依赖广告拦截、用户脚本、Chrome 扩展、NativeBridge API 或资源加密打包 → 选择**系统 WebView**。
- 需要 ECH / SNI 加密、真实 Firefox 指纹或 Gecko 内核行为 → 选择 **GeckoView**,并接受上列功能缺失。
- 构建前需先在本页下载 GeckoView 运行时,否则构建对话框不允许选择该内核;宿主预览中运行时缺失时会自动回退到系统 WebView。
