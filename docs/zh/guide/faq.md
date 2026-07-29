# 常见问题

按主题组织的实用问答。要看完整细节,点链接。

## 基础

### WebToApp 免费吗?

是的。WebToApp 以 [The Unlicense](https://github.com/shiaho777/web-to-app/blob/main/LICENSE) 开源。

### 需要什么 Android 版本?

Android 6.0(API 23)或更高。

### 构建应用需要电脑吗?

不需要。整个构建 —— 二进制打补丁、签名、AAB 导出 —— 都在设备上完成。只有当你想从源码构建 WebToApp 本身时才需要电脑。

### 这和网址套壳应用有什么不同?

网址套壳只是在 WebView 里打开网站。WebToApp 额外在**设备上运行真实的服务运行时**(Node.js、PHP、Python、Go、WordPress,经 fork+exec)、搭载加固网络栈(DoH、TLS 指纹、ECH)、在二进制层面修改并签名 APK,并支持模块/油猴脚本/MV3 扩展 —— 全程无需电脑或远程构建服务器。

### 能创建哪些应用类型?

12 种:网页、多站点、HTML、离线包、前端、PHP、WordPress、Node.js、Python、Go、媒体和画廊。见[创建应用](/zh/guide/app-types/)。

## 创建与编辑

### 编辑核心配置和编辑通用配置有什么区别?

- **编辑核心配置** —— 类型专属的来源/运行时设置(每种应用类型都不同)。见[编辑核心配置](/zh/guide/app-actions/edit-core-config)。
- **编辑通用配置** —— 每个应用都有的共享选项(外观、网络、隐私、扩展、导出)。见[编辑通用配置](/zh/guide/app-actions/edit-common-config/)。

网页应用没有这两项,而是单一的合并 **编辑** 入口。

### 预览和导出有什么区别?

**预览** 走宿主路径(一切都在构建器的 classpath 上)。**导出** 构建一个独立 APK,运行 shell 运行时,从嵌入的 `app_config.json` 读取你的配置。见[快速开始](/zh/guide/getting-started)。

## 构建与导出

### 构建模式(FULL / CONTENT_OVERLAY / REUSE_UNSIGNED)是什么?

重建是增量且内容寻址的:

- `FULL` —— 从模板重建(加密构建恒用)。
- `CONTENT_OVERLAY` —— 仅应用内容变化。
- `REUSE_UNSIGNED` —— 对先前构建的未签名 APK 重新签名。

见[构建 APK](/zh/guide/app-actions/build-apk)。

### APK 导出和 AAB 导出有什么区别?

**构建 APK** 产生一个可安装的已签名 APK。**AAB 导出**(从 [Google Play](/zh/guide/more-features/google-play))产生 Play 级打包,并把 `targetSdk` 重写到 Play 要求的级别。见 [APK导出配置](/zh/guide/app-actions/edit-common-config/apk-export)。

### 为什么生成的应用 targetSdk 是 28?

较低的 `targetSdk` 是刻意为之:它让生成的应用能从应用存储 fork+exec 原生运行时(Node.js、PHP、Python、Go、WordPress)。AAB 导出器会单独为 Play 分发重写 `targetSdk`。

### 如何用自己的密钥签名?

创建或导入密钥库(PKCS12/PFX/JKS/BKS),并在 [APK导出配置](/zh/guide/app-actions/edit-common-config/apk-export)中选择签名方案(V1/V2/V3)。

### 构建产物存在哪里?

在[文件管理](/zh/guide/more-features/file-manager)中 —— APK 构建、AAB 导出、应用克隆和构建日志。

## 运行时

### 为什么我的 Node/PHP/Python/Go 应用首次使用要下载东西?

为了让基础应用保持小巧,运行时二进制不打包进去;它们在首次使用时下载一次并缓存。在[运行时管理](/zh/guide/more-features/runtime-management)和 [Linux 环境](/zh/guide/more-features/linux-environment)中管理。

### 我的 Node.js 应用报 `loadNode` / `loadJniBridge` 错误,为什么?

导出的 APK 必须嵌入 `libnode_bridge.so`、`libnode.so`(16KB 对齐)和 `libc++_shared.so`。缺少原生库会导致此失败。见 [Node.js](/zh/guide/app-types/nodejs) 的导出要求。

### 端口如何管理?冲突了怎么办?

运行时应用通过[端口管理](/zh/guide/more-features/port-manager)以冲突策略分配端口:`REASSIGN`(选另一个端口)、`AUTO_KILL`(停止冲突服务)或 `ALERT`(通知)。

### 为什么我的运行时应用需要 DNS 桥?

打包的原生二进制(基于 musl)不一定能触达系统 DNS 解析器,因此一个本地 DNS 桥代理提供 DNS 解析和出站 HTTP。这是自动接好的。

## 功能与配置

### 如何让应用全屏或隐藏状态栏?

用[全屏模式](/zh/guide/app-actions/edit-common-config/fullscreen)—— 它控制沉浸式模式以及状态栏/导航栏是否保持可见。

### 导出的应用里去广告如何工作?

宿主去广告器服务预览,编译后的规则集随导出的 APK 一起发布。在[广告拦截](/zh/guide/app-actions/edit-common-config/ad-blocking)中按应用配置规则/订阅;在 [Hosts 拦截](/zh/guide/more-features/hosts-adblock)中管理列表。

### 如何使用自定义 DNS 或 DNS-over-HTTPS?

见[自定义DNS](/zh/guide/app-actions/edit-common-config/custom-dns)—— 选择 DoH 提供商(Cloudflare、Google、AdGuard、NextDNS、CleanBrowsing、Quad9、Mullvad)或自定义端点。

## 扩展

### 模块、油猴脚本和 MV3 扩展有什么区别?

- **JS/CSS 模块** —— WebToApp 的原生格式,带配置 UI 和面板。见 [JS 模块](/zh/extensions/js-module)。
- **油猴脚本** —— Tampermonkey/Greasemonkey 风格的 `.user.js`,带 `GM_*` API。见[油猴脚本](/zh/extensions/userscript)。
- **MV3 扩展** —— Chrome Manifest V3 扩展,带 `chrome.*` API。见 [Chrome MV3](/zh/extensions/chrome-mv3)。

### 油猴脚本的 `GM_*` 函数按 `@grant` 门控吗?

不。`@grant` 声明会被解析并列入 `GM_info`,但所有 `GM_*` 函数都无条件暴露。为了与真正的 Tampermonkey/Greasemonkey 保持可移植性,仍应声明 grant。见 [API 参考](/zh/extensions/api-reference)。

### MV3 扩展运行在真正隔离的 world 中吗?

不。Android WebView 只有单一 JavaScript 上下文;`ISOLATED` 和 `MAIN` world 是模拟的(每个扩展覆盖 `globalThis.chrome`)。见 [Chrome MV3](/zh/extensions/chrome-mv3)。

### 如何把模块发布到市场?

在 `modules/` 下添加一个文件夹,更新 `registry.json`,然后开一个 pull request。见[发布到市场](/zh/extensions/publish)。

## 故障排除

### 某功能预览正常,导出后失效,为什么?

通常是某个配置字段没有贯通导出链路(模型 → `ApkConfig` JSON → shell 配置 → 运行时)。诊断清单见[配置字段漂移](/zh/developer/config-drift)。

### 构建失败了,去哪里看?

构建对话框会显示一份诊断报告(失败阶段、原因和构建日志尾部),可复制。构建日志也可在[文件管理](/zh/guide/more-features/file-manager)中查看。

### 我导出的应用连不上网,该检查什么?

检查应用的[自定义DNS](/zh/guide/app-actions/edit-common-config/custom-dns)和[高级设置](/zh/guide/app-actions/edit-common-config/advanced-settings)中的代理设置。对于运行时应用,本地 DNS 桥会自动处理解析。

## 数据与帮助

### 如何备份或迁移我的应用?

使用[关于](/zh/guide/more-features/about)中的 **数据备份 / 恢复**,或通过[导出](/zh/guide/app-actions/export-apk)把单个应用导出为可复用模板。

### 去哪里获取帮助?

- GitHub:[github.com/shiaho777/web-to-app](https://github.com/shiaho777/web-to-app)
- Telegram:[t.me/webtoapp777](https://t.me/webtoapp777)
- X(Twitter):[@shiaho777](https://x.com/shiaho777)
- QQ 群:1041130206
