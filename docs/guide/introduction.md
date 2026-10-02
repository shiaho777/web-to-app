# Introduction

WebToApp is an Android application that turns web projects into installable APKs **on the device**. This page describes what it actually is, in terms of the code, so you know what to expect before you start.

## What the app is, concretely

At its core, WebToApp manages a list of **app definitions**. Each definition is a `WebApp` record stored in a local Room database (the `web_apps` table). A `WebApp` holds:

- **Identity** — `name`, `url`, `iconPath`, `packageName`, and an `appType`.
- **A type-specific config** — one of `htmlConfig`, `galleryConfig`, or `multiWebConfig`, depending on the type. (Deprecated columns for removed types stay in the record so shipped databases still decode.)
- **Feature flags + configs** — activation, ads, announcement, ad blocking, WebView settings, splash, background music, translation, extensions, auto-start, disguise, and an `apkExportConfig` for packaging.

When you "build" an app, the builder takes the shell template APK, patches its identity and resources, embeds your `WebApp` configuration as an assets JSON, and signs the result. The output is a standalone APK you can install or share.

## The app types

The `AppType` enum defines what an app can be. The creatable types are:

`WEB` · `HTML` · `GALLERY` · `FRONTEND` · `MULTI_WEB`

(The enum still carries the removed `IMAGE`/`VIDEO`/`WORDPRESS`/`NODEJS_APP`/`PHP_APP`/`PYTHON_APP`/`GO_APP` constants so persisted projects and backups decode; they can't be created, previewed, or exported.)

All types render in a WebView: `WEB` loads a URL, `HTML`/`FRONTEND` load packaged site files, `GALLERY` plays image/video collections, and `MULTI_WEB` embeds several sites or packaged apps under one launcher. The create flow also offers an **Offline Pack** flow that scrapes a site into a packaged HTML app. See [Create App](/guide/app-types/) for each.

## One codebase, two ways to run

The same `WebToAppApplication` runs in two modes, selected by a build flag:

- **Builder (host)** — `SHELL_RUNTIME_ONLY = false`. This is the app you install from the store: the editor, the app list, and the export pipeline, with everything on the main classpath.
- **Generated app (shell runtime)** — `SHELL_RUNTIME_ONLY = true`. The exported APK runs the synced shell runtime and reads *your* embedded config from `app_config.json` via `ShellModeManager`.

This is why "works in preview but not after export" is a real failure mode: preview runs the host path, export runs the shell path, and a config field has to survive the trip between them. See [Config Field Drift](/developer/config-drift).

## Where things live in the UI

- [My Apps](/guide/main-screen/my-apps) — the home screen: your app list, categories, and the create button.
- [Create App](/guide/app-types/) — the app types and their creation flows.
- [App Actions](/guide/app-actions/edit-core-config) — what you can do per app (edit, build, share, export, …).
- [More Features](/guide/more-features/agent) — the global tools behind the top-right ⋮ menu.
- [App Configuration](/guide/config/) — the shared per-app options (network, privacy, appearance).

## How to read these docs

- **[Start](/guide/getting-started)** — build your first APK and tour the main screen.
- **[Developer Docs](/developer/)** — the codebase layout, the export pipeline, shell sync, and change recipes.
- **[Extension Authoring](/extensions/)** — write JS/CSS modules, userscripts, and MV3 Chrome extensions.

::: tip
The builder UI is available in 10 languages — switch from the [language button](/guide/main-screen/language) in the top bar. The language of the apps you *generate* is configured per app.
:::
