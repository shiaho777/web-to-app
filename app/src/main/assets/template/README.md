# APK 模板说明

此目录存放**唯一的** shell 模板 `webview_shell.apk`，它是 `ApkBuilder` 生成所有 APK 的基础。

## 模板来源（自动构建，勿手工放置）

- 模板由 `:shell` 模块的 release 构建产出：

  ```bash
  ./gradlew :shell:assembleRelease :app:syncShellTemplateApk
  ```

  `syncShellTemplateApk` 会把 `shell/build/outputs/apk/release/shell-release.apk`
  复制到本目录并重命名为 `webview_shell.apk`。
- 该任务已挂入 `:app` 的 `preBuild` 依赖链，本地构建会自动重建模板；
  CI 可用 `-PskipShellTemplateSync=true` 跳过。
- 文件被 `.gitignore`（`*.apk`）排除、不入库，新克隆后首次构建即重新生成。
- 禁止引入第二个模板 APK（见 AGENTS.md「One shell template」）。

## 模板如何被使用

1. `CompositeTemplateProvider` 优先从 assets 提取本模板（`AssetTemplateProvider`）；
   仅当模板缺失时才回退到宿主自身 APK（`SelfAsTemplateProvider`，降级路径，
   产物不具备 shell 运行时，正常构建流程不应触达）。
2. `ApkBuilder` 对模板做二进制级补丁：
   - 注入 `assets/app_config.json` 配置（运行时由 `ShellModeManager` 经 Gson 读取，
     字段名必须与 `@SerializedName` 一致，`checkConfigFieldDrift` 负责门禁）；
   - 改写包名、应用名、图标（AXML/ARSC 二进制补丁，包名修改已完整支持）；
   - 按应用类型嵌入运行时资源（HTML/前端包、媒体、`libnode.so` 等原生库），
     并按启用的功能裁剪未用权限（`RuntimePermissionSync`）。
3. `apksig` 完成 V1/V2/V3 签名后即为最终 APK；AAB 导出会另行把 `targetSdk`
   改写为 Play 要求的级别。

## 注意事项

- 模板必须保持 `:shell` release 的原始产物；不要把已签名/已改名的 APK 再喂回构建链当模板。
- 修改 `shell/proguard-rules.pro` 或 shell 打包逻辑后，务必在本地跑一次完整
  `:shell:assembleRelease` + 模板同步验证——CI 的 check 任务只编译 shell 的 debug 变体，
  不覆盖 R8 与模板管线。
- 生成的 APK 固定 `targetSdk = 28`（fork+exec 运行时的前提），不要随意提升。
- shell 清单中的组件声明（service/receiver）必须与 shell 同步代码保持一致，
  `ShellManifestComponentParityTest` 负责门禁；宿主清单新增 shell 同步包的组件时，
  shell 清单要同步补充。
