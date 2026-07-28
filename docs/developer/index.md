# Developer Docs

This section is for people working on WebToApp itself — contributors, deep customizers, and AI coding agents. It explains how the codebase is organized and how the two things that trip everyone up actually work: the **export pipeline** and **shell sync**.

::: tip Authoritative reference
[`AGENTS.md`](https://github.com/shiaho777/web-to-app/blob/main/AGENTS.md) at the repo root is the authoritative guide for AI agents and deep contributors. These pages expand on it with more context.
:::

## Repository layout

| Path | Role |
| --- | --- |
| `app/` | Full builder host: editor UI, export pipeline, runtimes, preview. The main application module. |
| `shell/` | Runtime template. Built to `app/src/main/assets/template/webview_shell.apk` via `:shell:assembleRelease` + `:app:syncShellTemplateApk`. |
| `clone-host/` | Host-side APK clone / identity reshape support library (compiled to a DEX asset). |
| `modules/` | Module Market catalog (`registry.json` + per-module folders). |
| `scripts/` | Build helpers and gates (`check_config_field_drift.py`). |
| `docs/` | This documentation site. |

Runtime Kotlin is authored under `app/` and synced into `shell/` by `syncShellRuntimeSources`. **Edit the `app/` source once; do not permanently fork copies under `shell/`.**

## The three Gradle modules

- **`:app`** — the builder. `applicationId = com.webtoapp`, `compileSdk = 36`, `minSdk = 23`, `targetSdk = 28`, `buildConfigField SHELL_RUNTIME_ONLY = false`.
- **`:shell`** — the runtime template embedded into generated APKs. Same SDK/version as `:app`, but `SHELL_RUNTIME_ONLY = true`. Its sources are synced from `app/`.
- **`:clone-host`** — a minimal `com.android.library` (namespace `com.webtoapp.clone`) with no dependencies, compiled to a DEX asset for `AppCloner`.

## Package structure (`app/src/main/java/com/webtoapp`)

- **`core/*`** — ~53 sub-packages of business/runtime logic: `apkbuilder`, `shell`, `webview`, `engine`, `extension`, `crypto`, `nodejs`, `php`, `python`, `golang`, `wordpress`, `linux`, `port`, `dns`, `network`, `adblock`, `aicoding`, and more.
- **`data/*`** — persistence: Room DAOs, database, type converters, and the `WebApp` model + nested `*Config` classes.
- **`ui/*`** — Jetpack Compose screens, components, design system, and the shell UI.
- **`di/`** — Koin dependency injection.
- **`util/`** — helpers and constants.

Native C++ lives under `app/src/main/cpp/` (crypto, integrity, anti-debug, `node_bridge`, `node_launcher`, `go_exec_loader`).

## Where to read next

- [Architecture](/developer/architecture) — the preview-vs-export mental model.
- [Export Pipeline](/developer/export-pipeline) — how a `WebApp` becomes a signed APK.
- [Shell Sync & Template](/developer/shell-sync) — how runtime code reaches generated apps.
- [Config Field Drift](/developer/config-drift) — the most common silent failure.
