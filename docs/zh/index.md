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
