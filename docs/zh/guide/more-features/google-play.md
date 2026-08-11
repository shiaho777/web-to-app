# Google Play

把生成的应用导出为 Play 级已签名 AAB。从 [⋮ → Google Play](/zh/guide/main-screen/more) 打开。

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
- 生成的 APK 默认保持 `targetSdk` 28（fork+exec 运行时 —— Node.js、PHP、Python、Go、WordPress —— 所需）；只有 AAB 会为 Play 重写。纯 WebView 应用类型可在 APK 导出设置中可选地提升独立 APK 的 `targetSdk`；见[构建 APK](/zh/guide/app-actions/build-apk)。
- 导出前会显示上传前建议和警告。
