# Google Play

把生成的应用导出为 Play 级已签名 AAB。从 [⋮ → Google Play](/zh/guide/main-screen/more) 打开。

## 哪些应用可以上架

绝大多数都可以。生成 APK 的低 `targetSdk` 只是 APK 打包层面的细节,不会带到 Play —— 你上传的是 AAB,导出器会给它写入符合 Play 要求的 `targetSdk`(当前 36)。

| 应用类型 | Play AAB |
| --- | --- |
| Web · 多网站 · HTML · 离线包 · Frontend · 媒体 · 图库 | 支持 |
| Node.js · PHP · Python · Go · WordPress | 不支持 |
| 任何开启资源加密的构建 | 不支持 |

**为什么排除服务端运行时类型。** 它们需要 `targetSdk` 28 才能从应用存储 fork+exec 内置运行时,而 Play 要求的目标级别会启用 W^X —— 恰好拦住这件事。强行转换只会得到一个自身服务器永远起不来的包,所以 AAB 导出是直接拒绝,而不是静默降级。这类应用请改用 APK 分发:侧载、企业分发,或任何接受 `targetSdk` 28 的商店。除此之外它们没有任何功能受限。

多网站应用始终可以上架。服务端运行时应用本来就无法作为多网站站点被嵌入 —— 它们的运行时不会打进多网站 APK —— 因此这类站点在导出时会回退为它的 URL,应用本身也不会运行任何运行时。

## AAB 导出

一键运行完整管线,带可见阶段:

1. **构建 APK** —— 按需组装 APK。
2. **组装** —— 转换为 AAB。
3. **签名** —— 签名打包。
4. **已签名** —— 完成;可分享或上传。

- **targetSdk 重写** —— AAB 的 `targetSdk` 被重写到 Play 要求的级别(当前 36)。
- **元数据** —— protobuf 元数据在本地生成。
- **可取消** —— 中途停止。
- **应用数** —— 查看有多少应用符合条件。

## 密钥库

创建、导入和管理用于 AAB 的签名密钥。

## 说明

- 你也可以从某个应用的[构建 APK](/zh/guide/app-actions/build-apk) 对话框直接针对该应用启动 AAB 导出。
- 生成的 APK 默认保持 `targetSdk` 28(fork+exec 运行时所需);只有 AAB 会为 Play 重写。纯 WebView 应用类型可在 APK 导出设置中可选地提升独立 APK 的 `targetSdk`;见[构建 APK](/zh/guide/app-actions/build-apk)。
- 导出前会显示上传前建议和警告。
