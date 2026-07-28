# 常见改动配方

日常工作的默认做法。把每条链路走到底;停在 UI 或宿主专属代码上,正是预览与导出分叉的原因。

## 1. 添加或修改宿主 UI 字符串

1. 在正确的 `Strings*` 拆分中添加属性,覆盖全部 10 种语言。
2. 像相邻代码那样从 Compose/UI 引用它。

见[国际化](/zh/developer/i18n)。

## 2. 添加一个必须影响生成 APK 的编辑器设置

追踪并更新**全部**:

1. 模型(`WebApp` / 嵌套配置)与编辑器 UI 绑定。
2. 导出映射(`ApkBuilder` / `ApkConfig` / `ApkConfigJsonFactory`)。
3. shell 配置类型(`ShellModeManager` / shell 配置数据类),如果运行时读取它们。
4. shell 同步代码中的运行时使用点。
5. 当开关变化时,为导出接线编写单元测试。

见[配置字段漂移](/zh/developer/config-drift)。

## 3. 修改每个生成应用都用的 shell 运行时行为

1. 编辑 `app/` 下的源码(共享运行时)。
2. 确认该文件被 `syncShellRuntimeSources` 包含。
3. 若需验证打包,重建 shell 模板。
4. 保持改动外科手术式精准;shell 有低 `targetSdk` 和精简依赖集。
5. 若触及 FGS / 通知渠道创建,通过 `SafeNotificationChannels` 软失败。

## 4. 添加宿主专属功能(编辑器、市场、工具)

1. 把实现放在宿主专属包(`core/apkbuilder`、宿主界面、sample/market、`core/host`……)。
2. 不要把宿主专属依赖拉进 `shell/build.gradle.kts`。

## 5. 触及 APK 导出或增量重建

1. 优先用 `ApkBuildCache` / 内容哈希,而非时间戳。
2. 加密构建保持全量重建。
3. 不要把已签名输出当作模板。
4. 若模板字节变化,确保缓存键正确失效。

## 6. 修改通知 / 引擎 / 网络加固

1. 优先用现有渠道抽象(polyfill / 轮询 / WebSocket / FCM)。
2. 默认不要添加 OEM 推送 SDK。
3. FGS 渠道创建路径必须容忍 OEM/渠道失败。

## 7. 模块市场 / `modules/`

1. 遵循 `modules/README.md` 的目录布局(`registry.json` + 模块文件夹)。
2. 若随生成 APK 一起发布,运行时消费仍走扩展/shell 路径。

## 8. 本地服务运行时 / 下载路径

1. 通过 `PortManager` 以配置的冲突策略分配端口;实现真正的停止处理器。
2. 当 fork+exec 进程需要宿主 DNS/proxy 环境时,接入 `LocalDnsBridgeProxy`。
3. 大型依赖 / 引擎 / 运行时下载使用 `NetworkModule.downloadClient`。

## 9. Node.js / Go 导出

1. **Node.js:** 确保 `injectNodeJsNativeLibs` 嵌入 `libnode_bridge.so` + `libnode.so`(经 `ElfAligner16k` 16KB 对齐)+ `libc++_shared.so`。Node 二进制解析优先 `nativeLibraryDir`,回退到下载缓存。
2. **Go:** 确保 `injectGoExecLoaderNativeLib` 嵌入 `libgo_exec_loader.so`。
3. `NodeService` 运行在独立的 `:nodejs` 操作系统进程中,使 V8 生命周期与宿主隔离。

## 验证命令

```bash
./gradlew :shell:assembleRelease :app:syncShellTemplateApk --no-configuration-cache
./gradlew :app:compileDebugKotlin -x syncCloneHostDex --no-configuration-cache
./gradlew :app:checkConfigFieldDrift --no-configuration-cache
python3 scripts/check_config_field_drift.py
```

当你改动 shell 成员、导出打包或配置字段时使用这些。对于宿主专属的 UI/字符串工作,通常对 `:app` 做定向编译即可。

附近编辑后常值得运行的聚焦测试:`ApkBuildCacheTest`、`AdBlockerHostRuntimeTest`、`AdBlockExportWiringTest`、`PortManagerTest`、`BuildInputPreflightTest`、`GoBuildEnvironmentTest`、`RuntimePermissionSyncTest`。
