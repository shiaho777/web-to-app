# Change Recipes

Default approaches for everyday work. Follow each chain end-to-end; stopping at UI or host-only code is how preview and export diverge.

## 1. Add or change a host UI string

1. Add the property in the correct `Strings*` split with all 10 languages.
2. Reference it from Compose/UI the same way neighbors do.

See [Internationalization](/developer/i18n).

## 2. Add an editor setting that must affect the generated APK

Trace and update **all** of:

1. Model (`WebApp` / nested config) and editor UI binding.
2. Export mapping (`ApkBuilder` / `ApkConfig` / `ApkConfigJsonFactory`).
3. Shell config types (`ShellModeManager` / shell config data classes) if the runtime reads them.
4. Runtime use site in shell-synced code.
5. Unit tests for export wiring when flags change.

See [Config Field Drift](/developer/config-drift).

## 3. Change shell runtime behavior used by every generated app

1. Edit the source under `app/` (shared runtime).
2. Confirm the file is included by `syncShellRuntimeSources`.
3. Rebuild the shell template if you need to validate packaging.
4. Keep changes surgical; shell has a low `targetSdk` and a thin dependency set.
5. If you touch FGS / notification channel creation, fail soft via `SafeNotificationChannels`.

## 4. Add a host-only feature (editor, market, tooling)

1. Keep implementation under host-only packages (`core/apkbuilder`, host screens, sample/market, `core/host`, …).
2. Do not pull host-only deps into `shell/build.gradle.kts`.

## 5. Touch APK export or incremental rebuild

1. Prefer `ApkBuildCache` / content hashes over timestamps.
2. Encrypted builds stay full rebuild.
3. Do not use signed outputs as templates.
4. If template bytes change, ensure cache keys invalidate correctly.

## 6. Change notifications / engines / network hardening

1. Prefer existing channel abstractions (polyfill / polling / WebSocket / FCM).
2. Do not add OEM push SDKs by default.
3. FGS channel-create paths must tolerate OEM/channel failures.

## 7. Module Market / `modules/`

1. Follow `modules/README.md` catalog layout (`registry.json` + module folders).
2. Runtime consumption still goes through the extension/shell paths if it ships inside generated APKs.

## 8. Local server runtime / download path

1. Allocate ports through `PortManager` with the configured conflict policy; implement real stop handlers.
2. Wire fork+exec processes into `LocalDnsBridgeProxy` when they need host DNS/proxy env.
3. Use `NetworkModule.downloadClient` for large dependency / engine / runtime downloads.

## 9. Node.js / Go export

1. **Node.js:** ensure `injectNodeJsNativeLibs` embeds `libnode_bridge.so` + `libnode.so` (16KB-aligned via `ElfAligner16k`) + `libc++_shared.so`. Node binary resolution prefers `nativeLibraryDir`, falls back to download cache.
2. **Go:** ensure `injectGoExecLoaderNativeLib` embeds `libgo_exec_loader.so`.
3. `NodeService` runs in a dedicated `:nodejs` OS process so V8 lifecycle is isolated from the host.

## Verify commands

```bash
./gradlew :shell:assembleRelease :app:syncShellTemplateApk --no-configuration-cache
./gradlew :app:compileDebugKotlin -x syncCloneHostDex --no-configuration-cache
./gradlew :app:checkConfigFieldDrift --no-configuration-cache
python3 scripts/check_config_field_drift.py
```

Use these when you change shell membership, export packaging, or config fields. For host-only UI/string work, targeted compile on `:app` is usually enough.

Focused tests often worth running after nearby edits: `ApkBuildCacheTest`, `AdBlockerHostRuntimeTest`, `AdBlockExportWiringTest`, `PortManagerTest`, `BuildInputPreflightTest`, `GoBuildEnvironmentTest`, `RuntimePermissionSyncTest`.
