# Export Pipeline

The export pipeline turns a `WebApp` model into a signed APK. It lives in `app/src/main/java/com/webtoapp/core/apkbuilder/` (~24 files).

## Key classes

| File | Role |
| --- | --- |
| `ApkBuilder.kt` | Orchestrates APK assembly + signing. Contains `WebApp.toApkConfig(...)`. |
| `ApkConfig.kt` | The master config schema: `data class ApkConfig(meta, activation, adBlock, webView, proxy, dns, nodejs, phpApp, pythonApp, goApp, multiWeb, ...)`. Everything an exported APK can encode. |
| `ApkConfigJsonFactory.kt` | Serializes `ApkConfig` to the assets JSON the shell reads; includes `ApkConfigValidator`. |
| `ApkTemplate.kt` / `ShellTemplateProvider.kt` | Locate and load the shell template APK. |
| `ApkBuildCache.kt` | Incremental rebuild; defines `enum class ModifyApkMode`. |
| `AxmlEditor` / `AxmlRebuilder` | Edit/rebuild the binary AndroidManifest (AXML). |
| `ArscEditor` / `ArscRebuilder` | Edit/rebuild the binary resource table (resources.arsc). |
| `JarSigner.kt` | Signs the APK (jarsigner path); `apksig` handles V1/V2/V3. |
| `ZipAligner` / `ZipUtils` | Zip alignment and low-level zip manipulation. |
| `ElfAligner16k.kt` | 16KB-page ELF alignment for native `.so` files. |
| `RuntimeAssetEmbedder.kt` | Injects runtime assets (Node/PHP/Python/Go) into the APK. |
| `NetworkSecurityConfigBuilder.kt` | Generates the network security config XML. |

Related: `core/playstore/aab/` handles AAB/Play packaging; `core/crypto/` (`AssetEncryptor`, `EncryptedApkBuilder`, `KeyManager`) handles asset encryption.

## The flow

```text
WebApp (editor model)
  → WebApp.toApkConfig()          [ApkBuilder.kt]
  → ApkConfig                     [typed schema, *Block sub-objects]
  → ApkConfigJsonFactory          [serialize to app_config.json]
  → embed into template assets
  → patch AXML / ARSC (identity, permissions, icon)
  → embed runtime assets (if a server runtime)
  → sign (V1/V2/V3)
  → output APK
```

## `ApkConfig` structure

`ApkConfig` is composed of a `MetaBlock` plus dozens of feature blocks (`WebViewBlock`, `ProxyBlock`, `DnsBlock`, `NodejsBlock`, `PhpAppBlock`, `PythonAppBlock`, `GoAppBlock`, `MultiWebBlock`, `AdBlockBlock`, …). Convenience getters on `ApkConfig` flatten these (`appName`, `targetUrl`, `adBlockEnabled`, …).

The JSON field names produced by `ApkConfigJsonFactory` **must match** the `@SerializedName` annotations in the shell config class — see [Config Field Drift](/developer/config-drift).

## Incremental rebuild (`ApkBuildCache`)

Three modes:

| Mode | Meaning |
| --- | --- |
| `FULL` | Rebuild from template. Always used for encrypted builds. |
| `CONTENT_OVERLAY` | Only app content changed; overlay onto a prior build. |
| `REUSE_UNSIGNED` | Re-sign a previously built unsigned APK. |

Rules:

- Cache keys are **content-stable hashes** — never mtime-based.
- Template / entry identities must be content-stable.
- Encrypted builds always force a full rebuild.
- **Do not** feed signed or renamed APKs back into full `modifyApk` as templates.

## Native library embedding

- **Node.js** export must embed `libnode_bridge.so` + `libnode.so` (16KB-aligned via `ElfAligner16k`) + `libc++_shared.so`.
- **Go** export must embed `libgo_exec_loader.so`.

Missing any native lib causes `loadNode` / `loadJniBridge` failure at runtime.
