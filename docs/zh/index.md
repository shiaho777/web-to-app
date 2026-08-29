---
layout: home

hero:
  name: WebToApp
  text: 在手机上构建 Android APK
  tagline: 一个远超"网址转 App"的设备端 APK 工坊 —— fork+exec 真实服务运行时、搭载加固网络栈、导出 Play 级安装包,全程无需电脑。
  image:
    src: /logo.png
    alt: WebToApp
  actions:
    - theme: brand
      text: 快速开始
      link: /zh/guide/introduction
    - theme: alt
      text: 开发者文档
      link: /zh/developer/
    - theme: alt
      text: 在 GitHub 上查看
      link: https://github.com/shiaho777/web-to-app

features:
  - icon: <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="4" y="4" width="16" height="16" rx="2"/><path d="M9 4v16"/><path d="M4 9h5"/></svg>
    title: 真实的设备端运行时
    details: Node.js、PHP、Python、Go、WordPress 作为原生二进制直接从应用存储 fork+exec —— 如同 Termux,但打包成可安装的 APK。
  - icon: <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M12 3l8 3v6c0 4.5-3.2 7.6-8 9-4.8-1.4-8-4.5-8-9V6z"/></svg>
    title: 加固网络栈
    details: DNS-over-HTTPS、带本地 MITM 桥的 TLS 指纹伪造、加密客户端 Hello(ECH)、按应用代理,以及针对受限 SPA 的 CORS 绕过。
  - icon: <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M21 8l-9-5-9 5 9 5z"/><path d="M3 8v8l9 5 9-5V8"/><path d="M12 13v8"/></svg>
    title: 自包含构建
    details: 二进制 AXML/ARSC 打补丁、权限裁剪、V1/V2/V3 签名、Google Play 级 AAB 导出 —— 全部通过 apksig 在应用内完成,无需远程构建队列。
  - icon: <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M10 2v4M14 2v4M10 18v4M14 18v4M2 10h4M2 14h4M18 10h4M18 14h4"/><rect x="8" y="8" width="8" height="8" rx="1.5"/></svg>
    title: 发布后仍可扩展
    details: 添加 JS/CSS 模块、Tampermonkey 风格油猴脚本,或 MV3 Chrome 扩展(从 Chrome 网上应用店实时搜索),无需重建宿主。
  - icon: <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="4" y="10" width="16" height="10" rx="2"/><path d="M8 10V7a4 4 0 0 1 8 0v3"/><circle cx="12" cy="15" r="1.4"/></svg>
    title: 隐私与指纹防护
    details: 50+ 维浏览器指纹伪装、内置 20 个过滤列表的 hosts 去广告、AES-256-GCM 资源加密,以及激活码门控。
  - icon: <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="9"/><path d="M3 12h18"/><path d="M12 3a14 14 0 0 1 0 18a14 14 0 0 1 0-18"/></svg>
    title: 10 种界面语言
    details: 中文、英文、阿拉伯文(RTL)、葡萄牙文、西班牙文、法文、德文、俄文、日文、韩文 —— 在设置中随时切换。
---

<div class="wta-home">

## 从 URL 到签名 APK,只需三步

<div class="wta-steps">

1. **选择类型**

   从 [12 种应用类型](/zh/guide/app-types/)里选 —— 普通的 [Web](/zh/guide/app-types/web) 封装、[HTML](/zh/guide/app-types/html) 或 [Frontend](/zh/guide/app-types/frontend) 构建,或是设备端运行的 [Node.js](/zh/guide/app-types/nodejs)、[PHP](/zh/guide/app-types/php)、[Python](/zh/guide/app-types/python)、[Go](/zh/guide/app-types/go)、[WordPress](/zh/guide/app-types/wordpress) 服务器。

2. **填写基本信息**

   名称、URL 或项目、图标 —— 保存即可。所有类型共享同一套[配置卡片](/zh/guide/config/):网络、隐私、外观、运行时。

3. **构建并分享**

   [构建 APK](/zh/guide/app-actions/build-apk) 用 V1/V2/V3 在设备上完成签名,再[分享](/zh/guide/app-actions/share-apk)出去或[导出 Play 级 AAB](/zh/guide/app-actions/export-apk)。无需电脑,没有构建队列。

</div>

## 十二种类型,一个构建器

<div class="wta-types">

<div class="wta-tile">

[**Web 与 Multi-Web**](/zh/guide/app-types/multi-web)

URL 封装、多标签页枢纽、门户与链接流。

</div>

<div class="wta-tile">

[**HTML 与离线包**](/zh/guide/app-types/html)

打包本地 HTML 或 zip 构建,或抓取整站生成自包含的离线 APK。

</div>

<div class="wta-tile">

[**Frontend**](/zh/guide/app-types/frontend)

把 React、Vue、Vite 构建产物发布成 localhost 服务的 APK。

</div>

<div class="wta-tile">

[**服务端运行时**](/zh/guide/app-types/nodejs)

fork+exec Node.js、PHP、Python、Go 原生二进制,在本地端口提供服务。

</div>

<div class="wta-tile">

[**WordPress**](/zh/guide/app-types/wordpress)

完整的便携 WordPress 站点,PHP 与 SQLite 都跑在设备上。

</div>

<div class="wta-tile">

[**媒体与相册**](/zh/guide/app-types/media)

图片和视频播放器、相册集、作品集,打包成独立应用。

</div>

</div>

## 编辑器背后的工具箱

<div class="wta-types">

<div class="wta-tile">

[**Agent**](/zh/guide/more-features/agent)

内置 57 个工具的调用式助手,可以构建、编辑、操作整个应用。

</div>

<div class="wta-tile">

[**扩展模块**](/zh/guide/more-features/extension-modules)

向任何生成的应用注入 JS/CSS、油猴脚本或 MV3 Chrome 扩展。

</div>

<div class="wta-tile">

[**Hosts 去广告**](/zh/guide/more-features/hosts-adblock)

内置 20 个过滤列表和按应用的订阅规则,编译进导出的 APK。

</div>

<div class="wta-tile">

[**Linux 环境**](/zh/guide/more-features/linux-environment)

Termux 风格的设备端环境,带有构建和运行项目所需的真实工具链。

</div>

<div class="wta-tile">

[**端口管理**](/zh/guide/more-features/port-manager)

冲突策略、真实停止处理器,以及所有本地服务运行时的 DNS 桥接。

</div>

<div class="wta-tile">

[**应用修改器**](/zh/guide/more-features/app-modifier)

克隆和重打包已安装的 APK、批量导入定义、导出模板。

</div>

</div>

## 深入底层

<div class="wta-stats">

<div class="wta-stat"><b>12</b><span>种应用类型</span></div>

<div class="wta-stat"><b>57</b><span>个 Agent 工具</span></div>

<div class="wta-stat"><b>10</b><span>种界面语言</span></div>

<div class="wta-stat"><b>20</b><span>个去广告列表</span></div>

</div>

构建器自己做二进制手术 —— AXML/ARSC 重写、权限裁剪、AES-256-GCM 资源加密、16 KB 页对齐的原生库 —— 并让 shell 保持低 targetSdk,使 fork+exec 运行时持续可用。[开发者文档](/zh/developer/architecture)覆盖了完整的导出管线。

<div class="wta-cta">

准备好构建你的第一个 APK 了吗?

[快速开始](/zh/guide/getting-started)

</div>

</div>
