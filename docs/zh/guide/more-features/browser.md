# 更多 · 浏览器

**⋮** 菜单中的浏览器组。

## 浏览器内核 {#browser-kernel}

管理你的应用可用的浏览器引擎。

- **内置引擎** —— 下载并管理可选的 GeckoView(Firefox)运行时,用于 ECH / SNI 加密。沉重的原生产物在首次使用时获取。
- **当前 WebView 信息** —— 查看本设备的系统 WebView 版本。

各应用的引擎选择在[构建 APK](/zh/guide/app-actions#build-apk) 对话框中进行;本界面管理引擎本身。

## Hosts 拦截 {#hosts-adblock}

带 cosmetic 过滤的 hosts 规则去广告器。

- **内置列表** —— 20 个社区过滤列表(EasyList、uBlock Origin、AdGuard、AdAway,外加 8 个语言专属列表)。
- **按来源控制** —— 启用、禁用或删除每个订阅。
- **自定义规则** —— 添加你自己的订阅 URL,或从文件导入规则。
- **Cosmetic 过滤** —— 基于 MutationObserver 的元素隐藏。

去广告同时贯通预览与导出:宿主去广告器服务预览,编译后的规则集随生成的 APK 一起发布(在[应用配置](/zh/guide/config/privacy)中按应用配置)。
