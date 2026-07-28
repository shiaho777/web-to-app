# App Actions

Each app card on [My Apps](/guide/main-screen) has a ⋮ button that opens its action menu. (Tapping the card itself previews the app; swiping the card left is a quick delete.)

## The action menu

| Action | What it does |
| --- | --- |
| **Edit** *(Web type)* | Opens the full editor. Web apps have a single edit entry. |
| **Edit Core Config** *(other types)* | Opens the type-specific editor (e.g. the PHP or Node.js form) — the runtime/source settings. |
| **Edit Common Config** *(other types)* | Opens the shared editor — appearance, networking, privacy, export. See [App Configuration](/guide/config/). |
| **Create Shortcut** | Adds a launcher shortcut to the generated app on your home screen. |
| **Build APK** | Builds and signs the APK. See below. |
| **Share APK** | Builds the APK, then opens the system share sheet to send it. |
| **Export** | Exports the app as a reusable project template (`.wtapkg`-style) you can import elsewhere. |
| **Move to Category** | Moves the app to another [category](/guide/main-screen#category-tabs). |
| **Delete** | Deletes the app (with confirmation). Also available by swiping the card left. |

::: info Why two edit entries?
Non-web apps split settings into **core** (type-specific: the URL/runtime/source) and **common** (shared: appearance, network, privacy, export). Web apps put everything in one editor, so they show a single **Edit**.
:::

## Build APK

The build dialog patches the shell template, embeds your config and content, prunes permissions, and signs the result. Options:

- **Browser engine** — System WebView or GeckoView. Choosing GeckoView downloads it on first use.
- **Resource encryption** — PBKDF2 + AES-256-GCM for packaged config/HTML/media/BGM, with an optional custom password. Enabling it activates runtime hardening (anti-debug, anti-Frida, DEX-tamper) and always forces a full rebuild.
- **Isolation** — per-app isolation of storage, WebRTC, Canvas, Audio, WebGL, fonts, headers, and IP surfaces.
- **Background run** — keep the app's service alive in the background.
- **Notifications** — scheduled/persistent notifications, URL-polling foreground service, deep links.
- **Force full rebuild** — skip incremental caching.
- **Version code** — auto-suggests the next version code when a custom package name is already installed.

A **preflight check** runs before building and reports blocking errors. After a successful build you can jump straight to **AAB export** for Google Play.

### Incremental rebuilds

Rebuilds are content-addressed, with three modes:

| Mode | When |
| --- | --- |
| `FULL` | Template or identity changed; always used for encrypted builds |
| `CONTENT_OVERLAY` | Only app content changed |
| `REUSE_UNSIGNED` | Re-sign a previously built unsigned APK |

Cache keys are content hashes, never timestamps. Never feed a signed or renamed APK back as a template.

## Signing & AAB export

- **Keystore** — create, import, export, delete, and view certificate fingerprints. Supports PKCS12/PFX/JKS/BKS, including Android Studio upload-key cases where store and key passwords differ.
- **Schemes** — V1/V2/V3 independently controlled, with auto-fallback for legacy certificates and a custom V1 signer filename.
- **AAB export** — one tap in the [Google Play](/guide/more-features/system#google-play) screen: auto-builds the APK, converts to a signed AAB with `targetSdk` rewritten to the Play-required level (currently 36), and generates protobuf metadata locally. Cancellable mid-build.

::: info Two targetSdk values
The generated APK keeps `targetSdk` 28 (for fork+exec runtimes); the AAB exporter separately rewrites it for Play. These are independent paths.
:::

## Share & export

- **Share APK** builds then shares the APK via the system share sheet. If the build or share fails, a diagnostic report (with the build-log tail) is shown and can be copied.
- **Export** produces a reusable project template you can re-import on another device — distinct from building an installable APK.

Outputs land in the [File Manager](/guide/more-features/system#file-manager).
