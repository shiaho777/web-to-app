# 导出管线

导出管线把 `WebApp` 模型变成已签名的 APK。它位于 `app/src/main/java/com/webtoapp/core/apkbuilder/`(约 24 个文件)。

## 关键类

| 文件 | 角色 |
| --- | --- |
| `ApkBuilder.kt` | 编排 APK 组装 + 签名。包含 `WebApp.toApkConfig(...)`。 |
| `ApkConfig.kt` | 主配置 schema:`data class ApkConfig(meta, activation, adBlock, webView, proxy, dns, nodejs, phpApp, pythonApp, goApp, multiWeb, ...)`。导出 APK 能编码的一切。 |
| `ApkConfigJsonFactory.kt` | 把 `ApkConfig` 序列化为 shell 读取的 assets JSON;含 `ApkConfigValidator`。 |
| `ApkTemplate.kt` / `ShellTemplateProvider.kt` | 定位并加载 shell 模板 APK。 |
| `ApkBuildCache.kt` | 增量重建;定义 `enum class ModifyApkMode`。 |
| `AxmlEditor` / `AxmlRebuilder` | 编辑/重建二进制 AndroidManifest(AXML)。 |
| `ArscEditor` / `ArscRebuilder` | 编辑/重建二进制资源表(resources.arsc)。 |
| `JarSigner.kt` | 为 APK 签名(jarsigner 路径);`apksig` 负责 V1/V2/V3。 |
| `ZipAligner` / `ZipUtils` | zip 对齐与底层 zip 操作。 |
| `ElfAligner16k.kt` | 为原生 `.so` 文件做 16KB 页 ELF 对齐。 |
| `RuntimeAssetEmbedder.kt` | 把运行时资源(Node/PHP/Python/Go)注入 APK。 |
| `NetworkSecurityConfigBuilder.kt` | 生成网络安全配置 XML。 |

相关:`core/playstore/aab/` 负责 AAB/Play 打包;`core/crypto/`(`AssetEncryptor`、`EncryptedApkBuilder`、`KeyManager`)负责资源加密。

## 流程

```text
WebApp(编辑器模型)
  → WebApp.toApkConfig()          [ApkBuilder.kt]
  → ApkConfig                     [类型化 schema,*Block 子对象]
  → ApkConfigJsonFactory          [序列化为 app_config.json]
  → 嵌入模板 assets
  → 修改 AXML / ARSC(身份、权限、图标)
  → 嵌入运行时资源(如果是服务端运行时)
  → 签名(V1/V2/V3)
  → 输出 APK
```

## `ApkConfig` 结构

`ApkConfig` 由一个 `MetaBlock` 加数十个功能块组成(`WebViewBlock`、`ProxyBlock`、`DnsBlock`、`NodejsBlock`、`PhpAppBlock`、`PythonAppBlock`、`GoAppBlock`、`MultiWebBlock`、`AdBlockBlock`……)。`ApkConfig` 上的便捷 getter 把它们扁平化(`appName`、`targetUrl`、`adBlockEnabled`……)。

`ApkConfigJsonFactory` 产生的 JSON 字段名**必须匹配** shell 配置类中的 `@SerializedName` 注解 —— 见[配置字段漂移](/zh/developer/config-drift)。

## 增量重建(`ApkBuildCache`)

三种模式:

| 模式 | 含义 |
| --- | --- |
| `FULL` | 从模板重建。加密构建恒用此模式。 |
| `CONTENT_OVERLAY` | 仅应用内容变化;叠加到先前构建之上。 |
| `REUSE_UNSIGNED` | 对先前构建的未签名 APK 重新签名。 |

规则:

- 缓存键是**内容稳定哈希** —— 绝不用 mtime。
- 模板 / 条目身份必须内容稳定。
- 加密构建总是强制全量重建。
- **不要**把已签名或已改名的 APK 当作模板喂回全量 `modifyApk`。

## 原生库嵌入

- **Node.js** 导出必须嵌入 `libnode_bridge.so` + `libnode.so`(经 `ElfAligner16k` 16KB 对齐)+ `libc++_shared.so`。
- **Go** 导出必须嵌入 `libgo_exec_loader.so`。

缺少任何原生库都会在运行时导致 `loadNode` / `loadJniBridge` 失败。
