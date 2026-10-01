# Google Play

把生成的应用导出为 Play 级已签名 AAB。从 [⋮ → Google Play](/zh/guide/main-screen/more) 打开。

## 哪些应用可以上架

当前所有应用类型都基于 WebView,所以 AAB 路径覆盖构建器能创建的全部类型。独立 APK 的 `targetSdk`(默认 35,或被覆盖固定的值)只是 APK 打包层面的细节,不会带到 Play —— 你上传的是 AAB,导出器会给它写入符合 Play 要求的 `targetSdk`(当前 36)。

| 应用类型 | Play AAB |
| --- | --- |
| Web · 多网站 · HTML · 离线包 · Frontend · 图库 | 支持 |
| 绑定签名的资源加密(非内嵌密钥) | 不支持 |
| 从旧备份恢复的已移除类型应用 | 不支持 |

**为什么排除绑定签名的加密。** 绑定签名的资源加密从签名证书派生密钥;Play App Signing 会重签下发的 APK,派生密钥就无法再解开配置。内嵌密钥模式能扛过重签,对 Play 安全。

**已移除类型。** 旧版本用已下线类型(服务端运行时或独立媒体应用)创建的应用无法通过合规检查,不能导出 AAB。

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
- 生成的 APK 携带 shell 模板的 `targetSdk` 35;只有 AAB 会为 Play 重写。所有应用类型都可在 APK 导出设置中为独立 APK 固定其他 `targetSdk`;见[构建 APK](/zh/guide/app-actions/build-apk)。
- 导出前会显示上传前建议和警告。
