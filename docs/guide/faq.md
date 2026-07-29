# FAQ

Practical questions, organized by topic. For full detail, follow the links.

## Basics

### Is WebToApp free?

Yes. WebToApp is open source under [The Unlicense](https://github.com/shiaho777/web-to-app/blob/main/LICENSE).

### What Android version do I need?

Android 6.0 (API 23) or newer.

### Do I need a PC to build apps?

No. The entire build — binary patching, signing, and AAB export — happens on-device. A PC is only needed if you want to build WebToApp itself from source.

### How is this different from a URL-wrapper app?

A URL wrapper just opens a website in a WebView. WebToApp additionally runs **real server runtimes on-device** (Node.js, PHP, Python, Go, WordPress via fork+exec), ships a hardened network stack (DoH, TLS fingerprint, ECH), patches and signs APKs at the binary level, and supports modules/userscripts/MV3 extensions — all without a PC or remote build server.

### What app types can I create?

Twelve: Web, Multi-Web, HTML, Offline Pack, Frontend, PHP, WordPress, Node.js, Python, Go, Media, and Gallery. See [Create App](/guide/app-types/).

## Creating & editing

### What's the difference between Edit Core Config and Edit Common Config?

- **Edit Core Config** — the type-specific source/runtime settings (different for each app type). See [Edit Core Config](/guide/app-actions/edit-core-config).
- **Edit Common Config** — the shared options every app has (appearance, networking, privacy, extensions, export). See [Edit Common Config](/guide/app-actions/edit-common-config/).

Web apps have a single combined **Edit** entry instead of the two.

### What's the difference between preview and export?

**Preview** runs the host path (everything on the builder's classpath). **Export** builds a standalone APK that runs the shell runtime, reading your config from an embedded `app_config.json`. See [Getting Started](/guide/getting-started).

## Building & export

### What are the build modes (FULL / CONTENT_OVERLAY / REUSE_UNSIGNED)?

Rebuilds are incremental and content-addressed:

- `FULL` — rebuild from the template (always used for encrypted builds).
- `CONTENT_OVERLAY` — only app content changed.
- `REUSE_UNSIGNED` — re-sign a previously built unsigned APK.

See [Build APK](/guide/app-actions/build-apk).

### What's the difference between APK and AAB export?

**Build APK** produces an installable, signed APK. **AAB export** (from [Google Play](/guide/more-features/google-play)) produces a Play-ready bundle and rewrites `targetSdk` to the Play-required level. See [APK Export Config](/guide/app-actions/edit-common-config/apk-export).

### Why do generated apps target SDK 28?

The low `targetSdk` is deliberate: it lets generated apps fork+exec native runtimes (Node.js, PHP, Python, Go, WordPress) from app storage. The AAB exporter separately rewrites `targetSdk` for Play distribution.

### How do I sign with my own key?

Create or import a keystore (PKCS12/PFX/JKS/BKS) and choose the signature schemes (V1/V2/V3) in [APK Export Config](/guide/app-actions/edit-common-config/apk-export).

### Where are build outputs stored?

In the [File Manager](/guide/more-features/file-manager) — APK builds, AAB exports, app clones, and build logs.

## Runtimes

### Why does my Node/PHP/Python/Go app download something on first use?

The runtime binaries aren't bundled into the base app to keep it small; they're downloaded once on first use and cached. Manage them in [Runtime Management](/guide/more-features/runtime-management) and the [Linux Environment](/guide/more-features/linux-environment).

### My Node.js app fails with a `loadNode` / `loadJniBridge` error. Why?

The exported APK must embed `libnode_bridge.so`, `libnode.so` (16KB-aligned), and `libc++_shared.so`. A missing native library causes this failure. See the [Node.js](/guide/app-types/nodejs) export requirements.

### How are ports managed? What if there's a conflict?

Runtime apps allocate ports through the [Port Manager](/guide/more-features/port-manager) with a conflict policy: `REASSIGN` (pick another port), `AUTO_KILL` (stop the conflicting service), or `ALERT` (notify).

### Why does my runtime app need a DNS bridge?

The packed native binaries (musl-based) can't always reach the system DNS resolver, so a local DNS bridge proxy provides DNS resolution and outbound HTTP. This is wired automatically.

## Features & config

### How do I make my app fullscreen or hide the status bar?

Use [Fullscreen Mode](/guide/app-actions/edit-common-config/fullscreen) — it controls immersive mode and whether the status/navigation bars stay visible.

### How does ad blocking work in exported apps?

The host ad blocker serves preview, and the compiled rule set ships inside the exported APK. Configure rules/subscriptions per app under [Ad Blocking](/guide/app-actions/edit-common-config/ad-blocking); manage lists in [Hosts Ad Blocking](/guide/more-features/hosts-adblock).

### How do I use a custom DNS or DNS-over-HTTPS?

See [Custom DNS](/guide/app-actions/edit-common-config/custom-dns) — choose a DoH provider (Cloudflare, Google, AdGuard, NextDNS, CleanBrowsing, Quad9, Mullvad) or a custom endpoint.

## Extensions

### What's the difference between modules, userscripts, and MV3 extensions?

- **JS/CSS modules** — WebToApp's native format with a config UI and panel. See [JS Modules](/extensions/js-module).
- **Userscripts** — Tampermonkey/Greasemonkey-style `.user.js` with `GM_*` APIs. See [Userscripts](/extensions/userscript).
- **MV3 extensions** — Chrome Manifest V3 extensions with `chrome.*` APIs. See [Chrome MV3](/extensions/chrome-mv3).

### Are userscript `GM_*` functions gated by `@grant`?

No. `@grant` declarations are parsed and listed in `GM_info`, but all `GM_*` functions are exposed unconditionally. Declare grants anyway for portability with real Tampermonkey/Greasemonkey. See the [API Reference](/extensions/api-reference).

### Do MV3 extensions run in a truly isolated world?

No. Android WebView has a single JavaScript context; `ISOLATED` and `MAIN` worlds are simulated (each extension overwrites `globalThis.chrome`). See [Chrome MV3](/extensions/chrome-mv3).

### How do I publish a module to the market?

Add a folder under `modules/`, update `registry.json`, and open a pull request. See [Publish to the Market](/extensions/publish).

## Troubleshooting

### A feature works in preview but not after export. Why?

Usually a config field did not flow through the export chain (model → `ApkConfig` JSON → shell config → runtime). See [Config Field Drift](/developer/config-drift) for the diagnosis checklist.

### My build failed. Where do I look?

The build dialog shows a diagnostic report (failure stage, cause, and the build-log tail) that you can copy. Build logs are also viewable in the [File Manager](/guide/more-features/file-manager).

### My exported app can't reach the network. What should I check?

Check the app's [Custom DNS](/guide/app-actions/edit-common-config/custom-dns) and proxy settings under [Advanced Settings](/guide/app-actions/edit-common-config/advanced-settings). For runtime apps, the local DNS bridge handles resolution automatically.

## Data & help

### How do I back up or migrate my apps?

Use **Data backup / restore** in [About](/guide/more-features/about), or export individual apps as reusable templates via [Export](/guide/app-actions/export-apk).

### Where do I get help?

- GitHub: [github.com/shiaho777/web-to-app](https://github.com/shiaho777/web-to-app)
- Telegram: [t.me/webtoapp777](https://t.me/webtoapp777)
- X (Twitter): [@shiaho777](https://x.com/shiaho777)
