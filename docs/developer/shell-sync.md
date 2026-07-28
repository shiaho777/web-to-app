# Shell Sync & Template

Generated APKs run the **shell** runtime, whose Kotlin sources are synced from `app/`. Understanding this sync is essential: edit the wrong copy and your change either gets overwritten or silently diverges.

## The rule

> **Shared runtime sources are authored in `app/`.** Editing only a file under `shell/src` is usually wrong; it will be overwritten on sync or diverge from host.

## What enters the shell

`shell/build.gradle.kts` defines a `syncShellRuntimeSources` task with an include/exclude list that selects the full runtime set from `app/` (e.g. `core/shell`, `core/webview`, `core/engine`, `core/extension`, `ui/shell`). Shell-specific overrides live in `shell/src/main/java-overrides/`.

## Building the template

```bash
./gradlew :shell:assembleRelease :app:syncShellTemplateApk --no-configuration-cache
```

This produces the single canonical template at `app/src/main/assets/template/webview_shell.apk`.

| Concern | Path |
| --- | --- |
| What enters shell | `shell/build.gradle.kts` → `syncShellRuntimeSources` include/exclude |
| Shell template build | `:shell:assembleRelease` + `:app:syncShellTemplateApk` |
| Template output | `app/src/main/assets/template/webview_shell.apk` |
| Config → shell JSON | `app/.../apkbuilder/ApkConfigJsonFactory.kt` |
| Shell config types | `app/.../core/shell/ShellModeManager.kt` |
| Runtime WebView config | `app/.../ui/shell/ShellWebViewConfig.kt` |

## The shell runtime entry

At runtime, a generated APK:

1. `WebToAppApplication` starts.
2. `ShellModeManager.isShellMode()` checks for `app_config.json` in assets.
3. If present, `getConfig()` deserializes it (via Gson) into `ShellConfig` — possibly decrypting it first.
4. `ShellServerLauncher` resolves and launches server-backed runtimes; `ShellRuntimeServices` initializes the runtime stack.

## Constraints

- **One shell template.** Do not introduce a second template APK.
- **Low `targetSdk` (28).** Required for fork+exec runtimes. Do not raise it casually.
- **Thin dependency set.** Do not pull host-only deps into `shell/build.gradle.kts`.
- **Fail-soft notifications.** FGS / notification channel creation must use `SafeNotificationChannels`; channel creation failures must not crash FGS startup.
- **Configuration-cache safety.** Custom Gradle tasks (`syncCloneHostDex`, etc.) must capture `File`/`Provider` values at configuration time — do not reference `Project`/`android.sdkDirectory` inside task closures.

## After changing shell membership

If you change what enters the shell or how the template is packaged, **rebuild the template you touched.** A stale template is one of the most frequent causes of "works in preview, broken after export."

```bash
./gradlew :shell:assembleRelease :app:syncShellTemplateApk --no-configuration-cache
```
