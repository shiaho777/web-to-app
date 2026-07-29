# 自定义DNS

覆盖应用的 DNS 解析,包括 DNS-over-HTTPS。

**位置:**[编辑通用配置](/zh/guide/app-actions/edit-common-config/)编辑器中的 **自定义DNS** 卡片。

## 选项

- **DNS 模式**(`dnsMode`)—— `SYSTEM`(默认)或 DoH 提供商 / 自定义端点。
- **DoH 提供商** —— Cloudflare、Google、AdGuard、NextDNS、CleanBrowsing、Quad9、Mullvad,或自定义端点。
- **DNS 配置** —— 提供商专属设置(`dnsConfig`)。
- **引擎类型** —— 这里也可选择浏览器引擎(`engineType`;系统 WebView 或 GeckoView)。

## 说明

- 严格模式的 DoH 把所有 DNS 走 HTTPS;自动模式按需回退。
- Hosts 映射(host → IP 覆盖)在[高级设置](/zh/guide/app-actions/edit-common-config/advanced-settings)中配置。
