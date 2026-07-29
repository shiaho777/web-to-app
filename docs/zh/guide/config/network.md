# 网络与反审查

网络能力的主题索引。每一项都在[编辑通用配置](/zh/guide/app-actions/edit-common-config/)的某张卡片中配置 —— 点链接看详解。

## DNS

- [自定义DNS](/zh/guide/app-actions/edit-common-config/custom-dns) —— DNS 模式、DoH 提供商、自定义端点。

## 代理、TLS 与 CORS

这些在[高级设置](/zh/guide/app-actions/edit-common-config/advanced-settings)中:

- **代理** —— 静态 HTTP/HTTPS/SOCKS5 或 PAC,带认证和绕过规则。
- **TLS 指纹** —— 模拟浏览器 JA3 配置(如 `CHROME_131`)。
- **CORS 绕过** —— 用于跨源 SPA。
- **混合内容** 与 **私有网络桥**。
- **Hosts 映射** —— host → IP 覆盖。

## 故障转移

- **故障转移镜像** —— 自动回退 URL,在[高级设置](/zh/guide/app-actions/edit-common-config/advanced-settings)中。

## 浏览器引擎

- 引擎(系统 WebView / GeckoView)在[自定义DNS](/zh/guide/app-actions/edit-common-config/custom-dns)或 [APK导出配置](/zh/guide/app-actions/edit-common-config/apk-export)中选择;在[浏览器内核](/zh/guide/more-features/browser-kernel)中管理引擎。

## 运行时

- 服务端运行时的 DNS/代理桥接见[本地服务运行时](/zh/guide/config/runtimes)。
