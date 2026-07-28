# Architecture

The single most important mental model in WebToApp: **preview and export are two different execution paths that share the same runtime code.** Most bugs of the form *"works in preview, broken after export"* come from breaking this contract.

## The two paths

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

| | Host `:app` (preview) | Generated APK (export) |
| -- | --------------------- | ---------------------- |
| DEX | All `app/src` classes | Shell-synced subset (full runtime set) |
| Config | Editor / in-memory models | Assets JSON via `ShellModeManager` |
| Template | Not used | `app/src/main/assets/template/webview_shell.apk` |

## The contract

A flag set in the editor is useless at export unless it flows through **all** of:

1. **Model** — `WebApp` / nested config, bound to the editor UI.
2. **Export mapping** — `ApkBuilder` / `ApkConfig` / `ApkConfigJsonFactory`.
3. **Shell config types** — `ShellModeManager` / shell config data classes (if the runtime reads it).
4. **Runtime use site** — in shell-synced code.

Miss any step and you get one of three symptoms: the editor shows a switch that export ignores, export embeds config the runtime never reads, or the runtime reads a field that was never written.

## One shell template

There is exactly **one** shell template: `webview_shell.apk` from `:shell` release. Do not introduce a second template APK. Generated apps keep a low `targetSdk` (28) on the shell path because they rely on on-device fork+exec runtimes — do not raise shell `targetSdk` casually.

## Configuration center

The single source of truth for all feature settings is `WebApp` (`data/model/WebApp.kt`) and its `*Config` classes, carried through a full packaging passthrough chain into the generated APK. At runtime, the generated app reads its config from `app_config.json` in assets via `ShellModeManager`.

## Dependency policy

Avoid new third-party dependencies unless strongly justified (`app/build.gradle.kts` / `shell/build.gradle.kts`). Prefer platform APIs and existing modules. The shell has a thin dependency set and a low `targetSdk` — keep it that way.
