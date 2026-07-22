# AGENTS.md

Instructions for coding agents working in this repository.

## Code style

- Do what you believe is right. Make the change complete and correct, not the smallest possible diff. If a fix calls for refactoring, renaming, or touching multiple files, do it.
- Match the patterns and conventions already in the surrounding code.
- Do not add copyright or license headers unless asked.

## Project layout

| Path | Role |
|------|------|
| `app/` | Full builder host: editor UI, export pipeline, runtimes, preview. |
| `shell/` | Runtime template. Built to `app/src/main/assets/template/webview_shell.apk` via `:shell:assembleRelease` + `:app:syncShellTemplateApk`. |
| `clone-host/` | Host-side APK clone / identity reshape support library (compiled to a DEX asset). |
| `modules/` | Module Market catalog (`registry.json` + per-module folders). |
| `scripts/` | Build helpers and gates (`check_config_field_drift.py`). |

Runtime Kotlin is authored under `app/` and synced into `shell` by `syncShellRuntimeSources`. Edit the `app/` source once; do not permanently fork copies under shell.

User-facing product docs: `README.md`, `.github/docs/README_CN.md`, `.github/CONTRIBUTING.md`, `modules/README.md`.

## How the main pieces connect

```text
Editor (Compose screens in app/)
  ↔ data models (WebApp, configs)
  ↔ export factory (ApkConfig / ApkConfigJsonFactory)
  ↔ ApkBuilder / ApkBuildCache  →  signed generated APK

app/ sources
  → syncShellRuntimeSources  →  shell DEX  →  webview_shell.apk (template)

Generated APK runtime
  WebToAppApplication → ShellModeManager → load assets JSON config
  → WebViewManager / runtime servers (Node/PHP/Python/Go/WordPress)
```

Mental model:

- **Host preview** runs `:app` with all classes on the main classpath.
- **Generated APK** runs the shell template classes (full runtime synced from `app/`), reading config from assets JSON via `ShellModeManager`.
- A flag in the editor is useless at export unless it flows through **model → ApkConfig JSON → shell config → runtime code**.

## i18n

- Host UI strings live in `app/src/main/java/com/webtoapp/core/i18n/Strings.kt` (split across `Strings` / `StringsA` … `StringsE`).
- Any new user-facing host string needs all 10 languages: Chinese, English, Arabic, Portuguese, Spanish, French, German, Russian, Japanese, Korean.
- Prefer adding properties on the existing split objects; match surrounding style.

## Android and packaging constraints

- Generated apps keep a low `targetSdk` (28) on the shell path because they rely on on-device fork+exec runtimes. Do not raise shell targetSdk casually.
- Avoid new third-party dependencies unless strongly justified (`app/build.gradle.kts` / `shell/build.gradle.kts`). Prefer platform APIs and existing modules.
- Notification push channels: Web Notification polyfill, polling, WebSocket, FCM (developer-owned Firebase config). Do not add OEM vendor push SDKs by default.
- Foreground services and notification helpers must use `SafeNotificationChannels` (or equivalent fail-soft create). Channel creation failures must not crash FGS startup.
- **One shell template:** `webview_shell.apk` from `:shell` release. Do not introduce a second template APK.
- Export incremental rebuild lives in `app/.../apkbuilder` (`ApkBuildCache` + `ApkBuilder`):
  - Modes: `FULL` / `CONTENT_OVERLAY` / `REUSE_UNSIGNED`.
  - Template / entry identities must be **content-stable** (no mtime-based keys).
  - Encrypted builds always force a full rebuild.
  - Do not feed signed or renamed APKs back into full `modifyApk` as templates.
- Port coordination: `PortManager` + `PortConflictMode` (`REASSIGN` / `AUTO_KILL` / `ALERT`) with real stop handlers. Local server runtimes must allocate through PortManager and clean up on stop.
- Local server / Linux env DNS: fork+exec runtimes (Node / PHP / Python / Go / WordPress / Linux env) should wire through `LocalDnsBridgeProxy` when they need host DNS/proxy bridging.
- Large runtime downloads use `NetworkModule.downloadClient` (extended timeouts), not the default short-lived client.
- HTML / FRONTEND packaged shells need file-scheme access via `ShellWebViewConfig` (`allowFileAccess` / local-file detection). Do not regress pure file-based HTML loads.
- Node.js export must embed `libnode_bridge.so`, `libnode.so` (16KB-aligned), and `libc++_shared.so` as native libs. Go export must embed `libgo_exec_loader.so`.
- Gradle custom tasks (`syncCloneHostDex`, etc.) must be configuration-cache safe: capture `File`/`Provider` values at configuration time, do not reference `Project`/`android.sdkDirectory` inside task closures.

## Workflow

- Do not commit secrets, `local.properties`, keystores, or IDE/cache junk.
- Do not create commits, push, open PRs, or file Issues unless the user asks to deliver / ship / push / open a PR (or equivalent).
- When changing export or shell packaging, rebuild the template you touched.
- When changing config fields, run `checkConfigFieldDrift` to catch model ↔ shell config name drift.

### Delivery (Issue + PR + CI)

Default target: [shiaho777/web-to-app](https://github.com/shiaho777/web-to-app). Prefer a pull request over direct pushes to `main` when delivering code. Human-facing wording of the same loop lives in [CONTRIBUTING.md](.github/CONTRIBUTING.md); keep those docs in sync when this process changes.

**Language (required):** GitHub **Issues and PRs must be written in English** — titles, bodies, labels text you author, and delivery comments on the Issue/PR. Local chat with the user may be Chinese or any language; do not copy that language into Issue/PR text.

When the user asks to deliver a change, run the Issue → branch → PR → CI → merge loop end-to-end. Do not close the Issue until the PR is merged and CI is green.

---

## Shell template and runtime sync

### Dual runtime (preview vs export)

| | Host `:app` | Generated APK |
|--|-------------|---------------|
| DEX | All `app/src` classes | Shell sync include−exclude (full runtime set) |
| Config | Editor / in-memory models | Assets JSON via `ShellModeManager` |
| Template | Not used | `app/src/main/assets/template/webview_shell.apk` |

**Preview ≠ export** unless both paths stay valid:

1. Host still has the real implementation.
2. Shell-synced code reads the config field from assets JSON at runtime.
3. The field name in `ApkConfig` JSON matches the `@SerializedName` in `ShellModeManager` (Gson silently drops mismatches).

Most common failure: preview works; exported APK silently skips the feature because a config field name drifted between the export factory and the shell config class.

### Where each concern is edited

| Concern | Path |
|---------|------|
| What enters shell | `shell/build.gradle.kts` → `syncShellRuntimeSources` include/exclude |
| Shell template build | `:shell:assembleRelease` + `:app:syncShellTemplateApk` |
| Template output | `app/src/main/assets/template/webview_shell.apk` |
| Config → shell JSON | `app/.../apkbuilder/ApkConfigJsonFactory.kt` |
| Shell config types | `app/.../core/shell/ShellModeManager.kt` |
| Runtime WebView config | `app/.../ui/shell/ShellWebViewConfig.kt` |
| Config drift gate | `scripts/check_config_field_drift.py` → `:app:checkConfigFieldDrift` |
| Shell minify policy | `shell/proguard-rules.pro` |

---

## Common change recipes

These are the default approaches for everyday work. Follow the chain end-to-end; stopping at UI or host-only code is how preview and export diverge.

### 1. Add or change a host UI string

1. Add the property in the correct `Strings*` split with all 10 languages.
2. Reference it from Compose/UI the same way neighbors do.

### 2. Add an editor setting that must affect the generated APK

Trace and update **all** of:

1. Model (`WebApp` / nested config) and editor UI binding
2. Export mapping (`ApkBuilder` / `ApkConfig` / `ApkConfigJsonFactory`)
3. Shell config types (`ShellModeManager` / shell config data classes) if runtime reads them
4. Runtime use site in shell-synced code
5. Unit tests for export wiring when flags change

Missing any step usually yields: editor shows the switch, export ignores it, or export embeds config the runtime never reads.

### 3. Change shell runtime behavior used by every generated app

1. Edit the source under `app/` (shared runtime).
2. Confirm the file is included by `syncShellRuntimeSources`.
3. Rebuild shell template if you need to validate packaging.
4. Keep changes surgical; shell has a low targetSdk and a thin dependency set.
5. If you touch FGS / notification channel creation, fail soft via `SafeNotificationChannels`.

### 4. Add a host-only feature (editor, market, tooling)

1. Keep implementation under host-only packages (`core/apkbuilder`, host screens, sample/market, `core/host`, …).
2. Do not pull host-only deps into `shell/build.gradle.kts`.

### 5. Touch APK export or incremental rebuild

1. Prefer `ApkBuildCache` / content hashes over timestamps.
2. Encrypted builds stay full rebuild.
3. Do not use signed outputs as templates.
4. If template bytes change, ensure cache keys invalidate correctly.

### 6. Change notifications / engines / network hardening

1. Prefer existing channel abstractions (polyfill / polling / WebSocket / FCM).
2. Do not add OEM push SDKs by default.
3. FGS channel create paths must tolerate OEM/channel failures.

### 7. Module Market / `modules/`

1. Follow `modules/README.md` catalog layout (`registry.json` + module folders).
2. Runtime consumption still goes through the extension/shell paths if it ships inside generated APKs.

### 8. Fix "works in preview, broken after export"

Checklist in order:

1. Did shell config JSON actually contain the field at runtime?
2. Does the field name in `ApkConfig` JSON match the `@SerializedName` in shell config? Run `checkConfigFieldDrift`.
3. Is the runtime use site shell-synced (not host-only)?
4. For adblock: confirm `adBlockEnabled` mapping, host filter rebuild from cached subscriptions, and export rule compile without wiping host state.
5. Rebuild template after sync changes (stale template is a frequent miss).

### 9. Local server runtime / download path

1. Allocate ports through `PortManager` with the configured conflict policy; implement real stop handlers.
2. Wire fork+exec processes into `LocalDnsBridgeProxy` when they need host DNS/proxy env.
3. Use `NetworkModule.downloadClient` for large dependency / engine / runtime downloads.

### 10. Node.js / Go export

1. Node.js: ensure `injectNodeJsNativeLibs` embeds `libnode_bridge.so` + `libnode.so` (16KB-aligned via `ElfAligner16k`) + `libc++_shared.so`. Node binary resolution prefers `nativeLibraryDir`, falls back to download cache.
2. Go: ensure `injectGoExecLoaderNativeLib` embeds `libgo_exec_loader.so`.
3. `NodeService` runs in a dedicated `:nodejs` OS process so V8 lifecycle is isolated from the host.

---

## Easy-to-miss points

- **Shared sources are authored in `app/`.** Editing only a file under `shell/src` is usually wrong; it will be overwritten on sync or diverge from host.
- **Config field names drift.** Editor model, `ApkConfig`, JSON factory, and shell config must stay aligned; Gson silently drops unknown/missing fields. Run `checkConfigFieldDrift`.
- **Low targetSdk (28) and fork/exec runtimes** constrain "modernize the shell SDK" changes.
- **Incremental export cache** keys must be content-based; mtime and resigned APKs create false hits/misses.
- **HTML/FRONTEND file access.** Packaged local-file shells must have `allowFileAccess = true` (forced in `buildWebViewBlock` and `ShellWebViewConfig`); do not regress pure file-based HTML loads.
- **Node native libs.** Exported NODEJS_APP needs `libnode_bridge.so` + `libnode.so` + `libc++_shared.so`; missing any causes `loadNode` / `loadJniBridge` failure at runtime.
- **16KB page alignment.** `libnode.so` and other large ELF natives must be 16KB-aligned (`ElfAligner16k`) for Android 15+ devices; `node_bridge.cpp` / `node_launcher.c` enable 16KB app-compat before `dlopen`.
- **Node JNI output bridge.** `NodeJniOutputBridge` is a stable class referenced by native code; keep its `-keep` proguard rule so R8 does not rename `onOutput`.
- **Crashing FGS when notification channel creation fails.** Always use `SafeNotificationChannels` for channel creation.
- **Adblock is wired for preview + export.** Do not wipe host filter state during export; the host AdBlocker serves preview and the compiled rule set ships in the APK.
- **Runtime permissions are feature-driven.** `RuntimePermissionSync` derives the permission list from enabled features; do not revert to a static template.
- **Splash preview media path.** Preview reads splash media from the host filesystem (`splashMediaPath`); export packages it into assets. Do not hardcode `assets/splash_media.*` as the only source.
- **Port conflict policy.** Local server runtimes must allocate through `PortManager` and clean up on stop; do not bind ports directly.

---

## Forbidden / high-risk mistakes

- Second shell template APK
- Excluding a shell-synced class but leaving imports/constructors in shell-synced sources
- Re-enabling shell R8 obfuscation / aggressive shrink without testing exported apps
- Putting host-only tools back into shell sync for convenience
- OEM push SDKs or unjustified heavy dependencies in shell
- Feeding signed/renamed APKs into template/modify paths
- Crashing FGS when notification channel creation fails
- Committing secrets, keystores, or local machine config
- Regressing HTML/FRONTEND file access in packaged shells
- Shipping NODEJS_APP without `libnode_bridge.so` / `libnode.so` / `libc++_shared.so`
- Skipping 16KB alignment for large ELF natives

---

## Verify commands

```bash
./gradlew :shell:assembleRelease :app:syncShellTemplateApk --no-configuration-cache
./gradlew :app:compileDebugKotlin -x syncCloneHostDex --no-configuration-cache
./gradlew :app:checkConfigFieldDrift --no-configuration-cache
python3 scripts/check_config_field_drift.py
```

Use these when you change shell membership, export packaging, or config fields. For host-only UI/string work, targeted compile on `:app` is usually enough.

Related focused tests often worth running after nearby edits: `ApkBuildCacheTest`, `AdBlockerHostRuntimeTest`, `AdBlockExportWiringTest`, `PortManagerTest`, `BuildInputPreflightTest`, `GoBuildEnvironmentTest`, `RuntimePermissionSyncTest`.

---

## Implementation snapshot

Landed:

- Single shell template (`webview_shell.apk`) from `:shell` release, full runtime synced from `app/`
- Incremental `ApkBuildCache` (`FULL` / `CONTENT_OVERLAY` / `REUSE_UNSIGNED`); encrypted builds always full
- Notification channels: polyfill, polling, WebSocket, FCM (BYO Firebase) via existing abstractions
- `SafeNotificationChannels` fail-soft path for FGS
- `PortManager` conflict policies + real stop handlers across Node/PHP/Python/Go/WordPress
- `LocalDnsBridgeProxy` wiring for local server runtimes (including Node.js)
- Runtime downloads via `NetworkModule.downloadClient`
- Adblock preview + export wiring restored
- HTML/FRONTEND file-access for packaged local shells
- Node.js export: `libnode_bridge.so` + 16KB-aligned `libnode.so` + `libc++_shared.so`; 16KB app-compat before dlopen; stable `NodeJniOutputBridge` JNI callback
- Go export: `libgo_exec_loader.so` embedded; in-app build ENOSPC handling + GOTMPDIR relocation
- Runtime permission sync (feature-driven)
- Splash preview media path fallback
- Config field drift detection (`checkConfigFieldDrift`)
- Module Market: Chrome Web Store live search + GreasyFork browse
- Code editor find-and-replace
