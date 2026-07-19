<div align="center">

<img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" width="96" alt="WebToApp icon" />

# WebToApp

### Build Android APKs from web projects, directly on your phone.

**An on-device APK workshop that goes far beyond URL wrapping — it can fork and exec full server runtimes, ship a hardened anti-censorship network stack, sign bundles for Google Play, and run MV3 browser extensions, all without a PC or a remote build server.**

**English** · [简体中文](.github/docs/README_CN.md)

[![Stars](https://img.shields.io/github/stars/shiaho777/web-to-app?style=for-the-badge)](https://github.com/shiaho777/web-to-app/stargazers)
[![Forks](https://img.shields.io/github/forks/shiaho777/web-to-app?style=for-the-badge)](https://github.com/shiaho777/web-to-app/network/members)
[![License](https://img.shields.io/badge/License-Unlicense-blue?style=for-the-badge)](LICENSE)
[![Android](https://img.shields.io/badge/Android-23%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white)](#)

</div>

<p align="center">
  <a href="#what-makes-webtoapp-different">What's different</a> ·
  <a href="#what-you-can-build">What you can build</a> ·
  <a href="#capability-overview">Capability overview</a> ·
  <a href="#full-feature-map">Feature map</a> ·
  <a href="#module-market">Module market</a> ·
  <a href="#architecture">Architecture</a> ·
  <a href="#build-from-source">Build</a>
</p>

---

<div align="center">
<img src=".github/assets/social-preview.jpg" width="90%" alt="WebToApp screenshots: My Apps, Create App, toolbox, and APK actions on Android" />
</div>

---

## What makes WebToApp different

Most "website to app" tools stop at wrapping a URL in a WebView. WebToApp is closer to a pocket-sized APK workshop, and the hard parts are exactly where it diverges:

- **It runs real server runtimes on-device.** Node.js, PHP, Python, Go, and WordPress are fork+exec'd as native binaries straight from app storage — like Termux, packaged into an installable APK. URL-wrapper tools cannot do this at all.
- **It ships a hardened, anti-censorship network stack.** DNS-over-HTTPS, TLS fingerprint spoofing (Chrome / Firefox / Safari JA3 templates) with a local MITM bridge, Encrypted Client Hello (ECH) on the GeckoView engine to encrypt the SNI, per-app proxies, and CORS bypass for locked-down SPAs.
- **The whole build is self-contained.** Binary AXML/ARSC patching, permission pruning, V1/V2/V3 signing, and Google Play-ready AAB export all happen inside the app via `apksig` — no remote build queue, no PC.
- **It stays extensible after shipping.** Add JS/CSS modules, Tampermonkey-style userscripts, or MV3 Chrome extensions (live-searched and installed from the Chrome Web Store) without rebuilding the host.
- **The host UI speaks 10 languages out of the box.** Chinese, English, Arabic (RTL), Portuguese, Spanish, French, German, Russian, Japanese, and Korean — switch anytime in Settings; new in-app copy is maintained for all ten.

---

## Capability overview

A quick scan of what's in the box. Each links to the detailed feature map below.

| Area | Highlights |
| --- | --- |
| **Build targets** | Web · HTML · Frontend · WordPress · Node.js · PHP · Python · Go · Image · Video · Gallery · Multi-Web |
| **Browser engines** | System WebView by default; optional GeckoView (Firefox) runtime for ECH / SNI encryption |
| **Network & anti-censorship** | DoH (7 providers), TLS fingerprint spoofing + MITM bridge, ECH, static/PAC/SOCKS5 proxies, CORS bypass |
| **Privacy & hardening** | 50+ vector browser fingerprint disguise, resource encryption (AES-256-GCM), anti-debug, activation gating |
| **Local runtimes** | Native Node.js 18.20, PHP 8.4 + Composer 2.10, Python 3.14, official Go 1.26, WordPress 7.x over SQLite |
| **Extensions** | Built-in modules, userscripts with `GM_*`, MV3 Chrome extensions, live Chrome Web Store search |
| **APK/AAB output** | On-device V1/V2/V3 signing, **incremental rebuild** (reuse unsigned / content overlay), Google Play AAB export with targetSdk rewrite, keystore management |
| **AI coding** | Prompt-driven generation of web apps, modules, userscripts, and runtime projects; auto-retry on 429/5xx |
| **Host languages** | **10 UI languages** — 中文 · English · العربية · Português · Español · Français · Deutsch · Русский · 日本語 · 한국어 (Arabic RTL) |
| **Notifications** | Web Notification polyfill · URL polling · WebSocket push · FCM (BYO Firebase) · deep links · boot restore |

---

## What you can build

| Input | Output | Good for |
| --- | --- | --- |
| Website URL | WebView-based APK | Landing pages, tools, dashboards, docs, internal systems |
| HTML / static front-end | Localhost-backed APK | React, Vue, Vite, static builds, offline web apps |
| Node.js / PHP / Python / Go | APK with an on-device local server | Small server apps, admin tools, demos, prototypes |
| WordPress | APK running WordPress over local PHP + SQLite | Portable sites, theme/plugin demos, content packages |
| Images / video / galleries | Media-focused APK | Albums, course materials, portfolios, offline viewers |
| Multiple sites | Tab/card/feed/drawer multi-web APK | Link hubs, portals, app collections |
| Installed APK | Rebranded clone or shortcut disguise | Icon/name/package experiments, repackaging research |

---

## Full feature map

WebToApp has a large number of switches. The sections below group them by use case, kept collapsible so the top of the page stays scannable.

<details>
<summary><b>🌐 Browser engine & networking</b></summary>

- **Dual engine** — System WebView by default, or an optional GeckoView (Firefox) runtime downloaded on first use.
- **Kernel flavor disguise** — present as Chrome, Edge, Samsung Internet, Firefox, or Safari-style while keeping the real engine.
- **Desktop mode**, custom User-Agent, and JS/CSS injection at document-start / end / idle.
- **Popup handling** — same window, external browser, popup window, or block.
- **Proxies** — static HTTP/HTTPS/SOCKS5, PAC, authentication, bypass rules, and a local HTTP-to-SOCKS bridge.
- **DNS-over-HTTPS** — Cloudflare, Google, AdGuard, NextDNS, CleanBrowsing, Quad9, Mullvad, plus custom endpoints; strict or automatic modes.
- **Encrypted Client Hello (ECH)** — encrypt the SNI in the TLS handshake (GeckoView only; auto-wires DoH + GeckoView when toggled).
- **TLS fingerprint spoofing** — impersonate Chrome 131 / Firefox 133 / Safari 18 JA3 profiles (or custom ciphers), served through a local TLS-MITM bridge so the outgoing ClientHello matches a real browser.
- **CORS bypass** — on by default for static SPAs that call external APIs blocked by CORS; same-origin traffic is left alone, and CORS-only apps can use a lightweight `PrivateNetworkNativeBridgeAdapter` without the full Native Bridge surface.
- **Failover** — automatic fallback to mirror URLs when the primary target is unreachable.
- **PWA** offline cache strategies, custom error pages, per-app host overrides, and payment-scheme handlers.
- **Compatibility toggles** — blob download interception, scroll memory, image repair, clipboard / orientation / notification polyfills, private-network bridging, and Native Bridge capability gates.
- **Download location** — system Downloads, app-private storage, or a user-picked SAF folder, wired through the full packaging passthrough chain.

</details>

<details>
<summary><b>🛡️ Privacy, fingerprint defense & hardening</b></summary>

- **Browser fingerprint disguise across 50+ vectors** — User-Agent, WebGL, Canvas, AudioContext, ClientRects, timezone, language, memory, media devices, WebRTC, fonts, battery, permissions, performance, storage, notifications, CSS media, iframe propagation, and error-stack cleanup.
- **Hosts-rule ad blocker** with cosmetic MutationObserver filtering, **20 built-in community filter lists** (EasyList, uBlock Origin, AdGuard, AdAway, plus 8 language-specific lists), per-source enable/disable/delete, and custom subscription rules bundled into the APK.
- **Resource encryption** (PBKDF2 + AES-256-GCM) for packaged config, HTML, media, and BGM; optional custom encryption password stronger than package/certificate-derived keys.
- **Runtime hardening** when encryption is on — anti-debug, anti-Frida, DEX-tamper checks; threat response of log-only, silent exit, or randomized crash.
- **WebView/content isolation** for storage, WebRTC, Canvas, Audio, WebGL, fonts, headers, and IP surfaces.
- **Activation-code gating** — local verification, or your own HTTPS endpoint signed with EC P-256. See the [remote activation docs](.github/docs/remote-activation.md).

</details>

<details>
<summary><b>📦 Local server runtimes (fork + exec on-device)</b></summary>

- **Node.js** (18.20.x) runs in a dedicated `:nodejs` OS process via a native `node_launcher` wrapper loading `libnode.so`; supports custom native `.node` extensions.
- **PHP 8.4** from `pmmp/PHP-Binaries`, downloaded once on first use, with Composer 2.10.x and custom native extensions (`zend_extension`, `.so`).
- **Python 3.14** — Flask, Django, FastAPI via uvicorn, Tornado, the built-in HTTP server; pip dependencies resolved into `.pypackages`, custom native extensions supported; binary names are versioned so future bumps do not hard-code paths.
- **Go 1.26** — official Linux arm64 toolchain (`.tar.gz` from `dl.google.com`, USTC mirror for CN), on-device `go build` / `go mod` / `go run`, `vendor/` offline builds, static serving, and the native `go_exec_loader` wrapper; DNS and CA trust go through the same local JVM bridge used by PHP.
- **WordPress 7.x** over local PHP + SQLite (`sqlite-database-integration`), with theme and plugin import.
- **Linux Environment** screen manages toolchains and dependencies for Node, PHP, and Python.
- **Port Manager** coordinates runtime ports across host previews and generated apps — conflict policies (reassign / auto-kill / alert), real stop handlers, and process tracking so local servers are not left dangling.
- A **local DNS bridge proxy** (HTTP CONNECT in the Android JVM) gives runtimes working DNS resolution and outbound HTTP where the musl/packed binary can't reach the system resolver.

</details>

<details>
<summary><b>🧩 Extensions & automation</b></summary>

- **Built-in modules** — video download (YouTube / Bilibili / Douyin / Xiaohongshu extractors), video enhancer with YouTube cleanup (ad skip, max quality, background play, SponsorBlock), web analyzer, find-in-page, dark mode, privacy tools, content enhancer, element blocker, and YouTube launcher.
- **Userscripts** — Greasemonkey/Tampermonkey-style `.user.js` with a `GM_*` bridge (storage, requests, styles, menu commands) and promise-based `GM.*` APIs gated by script grants.
- **MV3 Chrome extension runtime** for manifest content scripts in isolated or main worlds, with `chrome.*` polyfills for runtime, storage, tabs, scripting, and declarative network-request parsing.
- **In-app Chrome Web Store search** — browse and install browser extensions by keyword (or paste a store URL / extension ID), with offline fallback to manual import.
- **Export codes** (`WTA1:` gzip + Base64) and QR sharing via ZXing.
- **AI Coding** skills to generate modules, userscripts, MV3 extensions, front-end apps, and local runtime projects, with automatic retry/backoff on 429/5xx and plan-mode exit that waits for user approval.

</details>

<details>
<summary><b>📱 App experience</b></summary>

- **Splash screens** — image or video, with skip behavior, trim ranges, and fixed orientation.
- **Background music** — playlists with synced LRC lyrics, lyric animations, custom font/color/stroke/shadow, and online music search.
- **Toolbar, status bar (light & dark), navigation, floating-window mode, and long-press menu styles.** Status bar color can follow theme, a custom color, full transparency, or **PAGE_TOP** (sample the page’s top pixels so the chrome matches the content).
- **Download location mode** — system Downloads, app-private directory, or a custom SAF folder picked by the user.
- **Announcement templates** for launch, interval, and no-network moments.
- **Host app language** — switch the entire builder UI among 10 languages (中文 / English / العربية / Português / Español / Français / Deutsch / Русский / 日本語 / 한국어); Arabic is full RTL.
- **Translation overlay** — 20 target languages via Google, MyMemory, LibreTranslate, Lingva, or Auto engines (in-page translate for the *content* of generated apps, separate from host UI language).
- **Print bridge** — intercept `window.print()` and blob/data-URL PDFs to the Android print framework / PDF export (with an onPageStarted re-inject fallback so late navigations stay hooked).
- **Notifications** — Web Notification polyfill, URL-polling and **WebSocket** push channels, **FCM** (bring-your-own Firebase project / optional `google-services.json`), scheduled and persistent notifications with progress, optional token-registration callbacks, deep links, boot / update restore, scheduled launch, and background-run service. OEM vendor SDKs are not bundled; non-GMS devices can use the self-hosted WebSocket channel.
- **Per-app usage stats** with Vico charts and URL health monitoring.

</details>

<details>
<summary><b>🔧 APK / AAB export & signing</b></summary>

- **Custom package name**, `versionName`, `versionCode`, icon, label, architecture target, and export format.
- **Build-time permission injection** with unused permissions pruned from the template manifest.
- **Incremental APK rebuild** — caches the last **unsigned** base per app under `filesDir/apk_build_cache/`. Unchanged identity + content → sign-only reuse; content-only changes → content overlay without re-expanding the shell template; package/icon/engine/encryption/shell changes (or **Force full rebuild**) → full pack. Encryption always forces a full rebuild. This is **not** detection of an already-installed package on the device.
- **One-tap AAB export** — auto-builds the APK on demand, converts it to a Play-ready signed AAB with `targetSdk` rewritten to the Play-required level (currently 35) and protobuf metadata generated locally; cancellable mid-build. AAB currently goes through a full APK build path.
- **Keystore management** — create, import, export, delete, and certificate-fingerprint viewing; PKCS12/PFX/JKS/BKS import including Android Studio upload-key cases where store and key passwords differ.
- **Signature schemes** — V1, V2, V3 independently controlled, with auto-fallback for legacy certificates; custom V1 signer filename for `META-INF/<name>.SF` / `.RSA`.
- **Performance options** — image compression, WebP conversion, code minification, lazy loading, DNS prefetch, and preload hints.
- **Full project and app-data backup/restore.**

</details>

<details>
<summary><b>🗂 File manager & project tooling</b></summary>

- **File manager** — a single screen to view, share, install, open, and clear build outputs (APK builds, AAB exports, app clones, build logs) and a user-files directory, with a read-only build-log viewer.
- **Website scraper** for offline packs — HTML, CSS, JS, images, fonts, `url()`, `srcset`, `@import`, path rewriting, same-domain limits, depth limits, and size limits; parallel streaming worker pool with main-thread progress callbacks.
- **Multi-Web layouts** — tabs, cards, feeds, drawers, per-site icons/theme colors/extraction selectors/refresh intervals, and shared JS/CSS.
- **Gallery apps** — categorized media, grid/list/timeline views, shuffle/single-loop, sorting, thumbnail bar, overlays, auto-next, and playback memory.
- **App Modifier** — shortcut disguise or real binary clone with manifest/resource patching and re-signing.

</details>

<details>
<summary><b>🔬 Specialized tools & research features</b></summary>

- **Forced-run**, **BlackTech**, **device disguise**, and **Icon Storm** are included for technical demonstration and must only be used with informed user consent.

</details>

---

## Module market

WebToApp has a GitHub-backed module market for community JS/CSS extension modules. The catalog is just files in this repository, so contributions use a normal pull-request flow.

```
modules/
├── registry.json        # app-facing catalog
├── submissions.json     # CI-generated PR / contributor metadata
├── README.md            # contributor guide
└── <module-folder>/     # each module
```

The app fetches both `registry.json` and `submissions.json` and only shows modules present in both, keeping the in-app catalog aligned with actually-merged PRs. The submissions file also records every contributor per module, so the catalog shows stacked avatars and a contributors leaderboard. Catalog files and module icons route through a global mirror first, with `raw.githubusercontent.com` and jsDelivr as automatic fallbacks, so the store loads fast everywhere (including mainland China).

- Users open **Extension Modules** and tap the storefront icon.
- Contributors add a folder under `modules/`, update `registry.json`, and open a PR.
- The default client cache is one hour, so merged modules propagate without an app update.

The high-level architecture lives here; the canonical submission rules, field schemas, reviewer checklist, and CI validation details live in [`modules/README.md`](modules/README.md).

The community market carries only JS/CSS extension modules. **Browser extensions (MV3)** are no longer a community catalog — instead the **Browser Extensions** tab searches the Chrome Web Store live: type a keyword, browse results, and install on demand through the existing CRX pipeline. Live search requires a network that can reach Google.

## Architecture

- The repository has **three Gradle modules**: `app` (the full builder and host), `shell` (the runtime host embedded into generated APKs), and `clone-host` (host code for app cloning — compiled to a `classes.jar`, converted to DEX via d8, and bundled as an asset for `AppCloner`).
- Runtime code is authored in `app` and synchronized into `shell`, so shared WebView/runtime behavior has one source of truth (`core/shell`, `core/webview`, `core/engine`, `core/extension`, `ui/shell`, etc.).
- The APK builder patches template APKs at the binary AXML/ARSC level, injects config/resources, prunes permissions, and signs with `apksig`. `ApkBuildCache` keeps unsigned bases for incremental rebuilds (`FULL` / `CONTENT_OVERLAY` / `REUSE_UNSIGNED`). A separate encrypted build path (`EncryptedApkBuilder`) offers resource encryption, shelling, and integrity checks (encrypted builds always skip the incremental cache).
- The host pins `targetSdk = 28` deliberately — it is what lets generated apps fork+exec native runtimes (Node.js, PHP, Python, Go, WordPress) from app storage, a capability URL-wrapper tools lack. The AAB exporter separately rewrites `targetSdk` for Play Store distribution.
- Server runtimes and the optional GeckoView native runtime are downloaded on first use rather than bundled into the base APK.
- The configuration center is `WebApp` (`data/model/WebApp.kt`) and its `*Config` classes — the single source of truth for all feature settings, carried through a full packaging passthrough chain into the generated APK.

## Tech stack

- Kotlin, Jetpack Compose, Material 3
- Koin for dependency injection
- Room 2.7.2 + KSP for persistence
- OkHttp 4.12.0 + `okhttp-dnsoverhttps`
- `com.android.tools.build:apksig` 8.3.0 for APK signing
- `protobuf-javalite` 3.25.5 for AAB metadata
- GeckoView as an optional browser engine
- Coil for image/video/GIF loading
- AndroidX Security Crypto + DataStore for stored secrets
- Vico Compose-M3 for charts
- ZXing for QR sharing
- Apache Commons Compress + xz for project import and website scraping
- Native C++ via JNI for `node_launcher` and `go_exec_loader`
- Robolectric for unit tests

See [app/build.gradle.kts](app/build.gradle.kts) for the complete dependency list.

## Build from source

Requirements: Android Studio Hedgehog or newer, JDK 17. The Gradle wrapper pins Gradle 9.4.1.

```bash
git clone https://github.com/shiaho777/web-to-app.git
cd web-to-app
./gradlew assembleDebug
```

For release builds, configure signing through `local.properties` and `app/build.gradle.kts`.

## Contributing

Human PR workflow is in [CONTRIBUTING.md](.github/CONTRIBUTING.md); agent-oriented repo guidance is in [AGENTS.md](AGENTS.md).

Default delivery for code changes: **Issue → branch → PR into `main` → CI (`check` green) → merge** (Issue closes via `Fixes #N` on merge, not on PR open). Details: [CONTRIBUTING.md § Pull requests](.github/CONTRIBUTING.md#pull-requests) and [AGENTS.md § Delivery](AGENTS.md#delivery-issue--pr--ci).

| Lane | What you do | Guide |
| --- | --- | --- |
| `modules/` | Publish a community module to the in-app market | [modules/README.md](modules/README.md) |
| Issues | Report a bug or request a feature | [GitHub Issues](https://github.com/shiaho777/web-to-app/issues) |
| Code | Fix a bug or build a feature in the Android client | [CONTRIBUTING.md](.github/CONTRIBUTING.md) |
| Agents / AI coding | Work on this repo with a coding agent (layout, LITE shell, export packs, common workflows) | [AGENTS.md](AGENTS.md) |

## Contact

Developed by **shiaho**.

| Platform | Link |
| --- | --- |
| GitHub | [github.com/shiaho777/web-to-app](https://github.com/shiaho777/web-to-app) |
| Telegram | [t.me/webtoapp777](https://t.me/webtoapp777) |
| X (Twitter) | [@shiaho777](https://x.com/shiaho777) |
| Bilibili | [b23.tv/8mGDo2N](https://b23.tv/8mGDo2N) |
| QQ Group | 1041130206 |

## License

[The Unlicense](LICENSE).

Advanced features such as forced run, BlackTech, device disguise, and Icon Storm are intended for technical demonstration and must only be used with informed user consent.

<div align="center">

**Open source · Built for Android power users · Star to support the project**

</div>
