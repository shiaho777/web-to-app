# 应用配置

当你编辑一个应用的 **通用配置**(或创建一个网页应用)时,编辑器是一长卷能力卡片。本页为其索引;分组的参考页更深入。

::: info 核心 vs 通用
非网页应用把编辑拆分为 **编辑核心配置**(类型专属的来源/运行时)和 **编辑通用配置**(下面的卡片)。网页应用在单一编辑器中显示这些。见[应用功能](/zh/guide/app-actions/edit-core-config)。
:::

## 配置卡片

| 卡片 | 配置什么 |
| --- | --- |
| **基本信息** | 名称、目标 URL、图标。 |
| **PWA 分析** | 检测目标的 PWA/离线缓存提示。 |
| **激活码** | 用激活码为应用设门(本地或远程)。见[隐私](/zh/guide/config/privacy#activation-gating)。 |
| **隐藏浏览器工具栏** | 显示/隐藏应用内工具栏。 |
| **全屏模式** | 沉浸式全屏。 |
| **横屏模式** | 锁定方向。 |
| **保持屏幕常亮** | 运行时防止熄屏。 |
| **浮窗** | 浮窗模式配置。 |
| **长按菜单** | 长按菜单样式。 |
| **启动画面** | 图片/视频启动、跳过行为、裁剪。见[外观](/zh/guide/config/appearance#splash-screen)。 |
| **背景音乐** | 播放列表、LRC 歌词、样式。见[外观](/zh/guide/config/appearance#background-music)。 |
| **公告** | 启动/间隔/无网络公告。 |
| **翻译** | 页内翻译叠加层(20 种语言)。 |
| **扩展模块** | 附加 JS/CSS 模块、油猴脚本、MV3 扩展。见[扩展](/zh/extensions/)。 |
| **去广告** | hosts 规则去广告器。见[隐私](/zh/guide/config/privacy#ad-blocking)。 |
| **DNS** | DNS-over-HTTPS 提供商与模式。见[网络](/zh/guide/config/network#dns-over-https-doh)。 |
| **伪装** | 浏览器指纹伪装(50+ 维)。见[隐私](/zh/guide/config/privacy#browser-fingerprint-disguise)。 |
| **设备伪装** | 设备级伪装。 |
| **自启动** | 开机自启 / 定时启动。 |
| **强制运行** | 强制运行行为(演示用;请在知情同意下使用)。 |
| **设备操作** | 设备操作钩子。 |
| **浏览器高级** | 高级 WebView/浏览器开关。见[网络](/zh/guide/config/network)。 |
| **特殊设置** | 其他高级开关。 |
| **导出与权限** *(抽屉)* | 包名、版本、签名、运行时权限。见[构建 APK](/zh/guide/app-actions/build-apk)。 |

## 分组参考

- [网络与反审查](/zh/guide/config/network) —— 引擎、DoH、代理、TLS 指纹、ECH、CORS。
- [隐私与加固](/zh/guide/config/privacy) —— 指纹伪装、去广告、加密、激活。
- [外观](/zh/guide/config/appearance) —— 启动画面、BGM、工具栏、状态栏、主题。
- [本地服务运行时](/zh/guide/config/runtimes) —— Node/PHP/Python/Go/WordPress 细节。
