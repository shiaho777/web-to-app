# 快速开始

本教程带你在几分钟内从全新安装走到第一个已签名的 APK。

## 1. 安装 WebToApp

在 **Android 6.0(API 23)或更高版本** 的设备上安装 WebToApp 构建器应用。你可以从源码构建(见[从源码构建](#从源码构建)),或安装提供的 APK。

首次启动会进入 **我的应用**,即列出你所创建的全部应用的主页。

## 2. 创建第一个应用

点击 **创建** 并选择应用类型。第一个应用选 **Web** 并输入一个 URL:

1. **应用类型** → `Web`
2. **目标 URL** → 例如 `https://example.com`
3. **应用名称** → 随意
4. **图标** → 选一张图片(可选,否则用默认图标)

编辑器会以合理的默认值打开,这里的一切之后都能改。

## 3. 预览

点击 **预览**,在构建器内运行应用,行为与导出后完全一致。预览在主 classpath 上运行完整运行时,因此能真实检验 WebView 行为、注入脚本和运行时服务器。

::: warning 预览 ≠ 导出
预览和导出共享同一套运行时代码,但导出还会把你的配置序列化进生成的 APK。如果某功能预览正常、导出后失效,通常是某个配置字段没有贯通导出链路。见[配置字段漂移](/zh/developer/config-drift)。
:::

## 4. 构建 APK

点击 **构建 APK**。WebToApp 会:

1. 取得 shell 模板(`webview_shell.apk`)。
2. 修改其二进制 manifest 与资源(包名、图标、标签、权限)。
3. 把你的配置和应用内容嵌入 assets。
4. 签名(V1/V2/V3,可配置)。

完成后,APK 出现在 **文件管理器** 中,可安装或分享。

## 5. 安装并测试

把生成的 APK 安装到设备(或分享到另一台设备)。启动它 —— 它运行 shell 运行时,读取*你*嵌入的配置,独立于构建器。

## 下一步

- 了解每种[应用类型](/zh/guide/create-app)能做什么。
- 配置[签名与 Play 商店导出](/zh/guide/build-export)。
- 用 [DoH、代理与 TLS 指纹](/zh/guide/network)加固网络。
- 在设备上运行 [Node.js / PHP / Python / Go / WordPress](/zh/guide/runtimes)。

## 从源码构建

要求:Android Studio Hedgehog 或更新版本,JDK 17。Gradle wrapper 固定为 Gradle 9.4.1。

```bash
git clone https://github.com/shiaho777/web-to-app.git
cd web-to-app
./gradlew assembleDebug
```

发布构建需通过 `local.properties` 和 `app/build.gradle.kts` 配置签名。
