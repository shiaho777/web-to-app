# 隐私与加固

WebToApp 包含一整套隐私、指纹防护与加固功能。全部按应用配置、按需启用。

## 浏览器指纹伪装 {#browser-fingerprint-disguise}

跨 **50+ 维度** 伪装:User-Agent、WebGL、Canvas、AudioContext、ClientRects、时区、语言、内存、媒体设备、WebRTC、字体、电池、权限、性能、存储、通知、CSS 媒体、iframe 传播,以及错误栈清理。

## 去广告 {#ad-blocking}

基于 hosts 规则的去广告,带 cosmetic MutationObserver 过滤,**内置 20 个社区过滤列表**(EasyList、uBlock Origin、AdGuard、AdAway,外加 8 个语言专属列表),按来源启用/禁用/删除,以及打包进 APK 的自定义订阅规则。

去广告同时贯通预览与导出:宿主去广告器服务预览,编译后的规则集随生成的 APK 一起发布。

## 资源加密

对打包的配置、HTML、媒体和 BGM 使用 PBKDF2 + AES-256-GCM。可选的自定义加密密码比包名/证书派生密钥更强。见[构建 APK](/zh/guide/app-actions#build-apk)。

## 运行时加固

开启加密后:反调试、反 Frida、DEX 篡改检查。威胁响应可配置 —— 仅记录日志、静默退出或随机崩溃。

## 内容隔离

按应用隔离存储、WebRTC、Canvas、Audio、WebGL、字体、头部和 IP 表面。

## 激活门控 {#activation-gating}

用激活码为应用设门 —— 本地校验,或用你自己经 EC P-256 签名的 HTTPS 端点。见[远程激活参考](https://github.com/shiaho777/web-to-app/blob/main/.github/docs/remote-activation.md)。

---

::: tip 配置教程即将推出
各项隐私功能的逐步指南正在编写中。
:::
