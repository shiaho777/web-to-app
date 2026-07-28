# 网络与反审查

WebToApp 搭载了一套远超普通 WebView 的加固网络栈。所有设置均按应用配置。

## 浏览器引擎

- **系统 WebView** —— 默认引擎。
- **GeckoView(Firefox)** —— 可选运行时,首次使用时下载。ECH 必需。GeckoView API 类来自 Gradle 依赖;沉重的原生产物(`.so` + `omni.ja`)按需获取。
- **内核风味伪装** —— 在保留真实引擎的同时,伪装成 Chrome、Edge、Samsung Internet、Firefox 或 Safari 风格。

## DNS-over-HTTPS(DoH) {#dns-over-https-doh}

可选 Cloudflare、Google、AdGuard、NextDNS、CleanBrowsing、Quad9、Mullvad,或自定义端点。模式:严格或自动。

## 代理

静态 HTTP/HTTPS/SOCKS5、PAC、认证、绕过规则,以及本地 HTTP-to-SOCKS 桥。代理按应用配置。

## TLS 指纹伪造

模拟 Chrome 131 / Firefox 133 / Safari 18 的 JA3 配置(或自定义密码套件),通过本地 TLS-MITM 桥提供服务,使发出的 ClientHello 与真实浏览器一致。

## 加密客户端 Hello(ECH)

加密 TLS 握手中的 SNI。**仅 GeckoView。** 开启 ECH 会自动联动 DoH + GeckoView。

## CORS 绕过

默认开启,针对那些调用被 CORS 拦截的外部 API 的静态 SPA。同源流量不受影响。仅涉及 CORS 的应用可使用轻量级 `PrivateNetworkNativeBridgeAdapter`,无需完整的 Native Bridge 接口。

## 故障转移

当主目标不可达时,自动回退到镜像 URL。

---

::: tip 详细教程即将推出
上述各项功能的逐步配置配方正在编写中。这里列出的能力如今都已在编辑器的网络分区中可用。
:::
