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
  <a href="#ai-coding-system">AI Coding</a> ·
  <a href="#architecture">Architecture</a> ·
  <a href="#build-from-source">Build</a>
</p>

---

<div align="center">
<img src=".github/assets/social-preview.jpg" width="90%" alt="WebToApp: My Apps home, Create App picker, main toolbox, and per-app APK actions running on an Android phone" />
</div>

---

## What makes WebToApp different

Most "website to app" tools stop at wrapping a URL in a WebView. WebToApp is closer to a pocket-sized APK workshop, and the hard parts are exactly where it diverges:

- **It runs real server runtimes on-device.** Node.js, PHP, Python, Go, and WordPress are fork+exec'd as native binaries straight from app storage — like Termux, packaged into an installable APK. URL-wrapper tools cannot do this at all.
- **It ships a hardened, anti-censorship network stack.** DNS-over-HTTPS, TLS fingerprint spoofing (Chrome / Firefox / Safari JA3 templates) with a local MITM bridge, Encrypted Client Hello (ECH) on the GeckoView engine to encrypt the SNI, per-app proxies, and CORS bypass for locked-down SPAs.
- **The whole build is self-contained.** Binary AXML/ARSC patching, permission pruning, V1/V2/V3 signing, and Google Play-ready AAB export all happen inside the app via `apksig` — no remote build queue, no PC.
- **It stays extensible after shipping.** Add JS/CSS modules, Tampermonkey-style userscripts, or MV3 Chrome extensions (live-searched and installed from the Chrome Web Store) without rebuilding the host.
- **AI-powered app building.** Generate apps, modules, userscripts, and MV3 extensions through natural language prompts using multiple LLM providers (OpenAI, Anthropic, Gemini, Ollama, and more).
- **20+ built-in AI coding skills** for generating HTML apps, React/Vue SPAs, Node.js/PHP/Python/Go server apps, WordPress themes/plugins, gallery apps, multi-web apps, and more.

---

## Capability overview

A quick scan of what's in the box. Each links to the detailed feature map below.

| Area | Highlights |
| --- | --- |
| **Build targets** | Web · HTML · Frontend · WordPress · Node.js · PHP · Python · Go · Image · Video · Gallery · Multi-Web |
| **Browser engines** | System WebView by default; optional GeckoView (Firefox) runtime for ECH / SNI encryption |
| **Network & anti-censorship** | DoH (7 providers), TLS fingerprint spoofing + MITM bridge, ECH, static/PAC/SOCKS5 proxies, CORS bypass |
| **Privacy & hardening** | 50+ vector browser fingerprint disguise, resource encryption (AES-256-GCM), anti-debug, activation gating |
| **Local runtimes** | Native Node.js, PHP 8.4 + Composer, Python (Flask/Django/FastAPI), Go, WordPress over SQLite |
| **Extensions** | Built-in modules, userscripts with `GM_*`, MV3 Chrome extensions, live Chrome Web Store search |
| **APK/AAB output** | On-device V1/V2/V3 signing, Google Play AAB export with targetSdk rewrite, keystore management |
| **AI coding** | Prompt-driven generation of web apps, modules, userscripts, and runtime projects with 20+ skills |
| **App types** | 12 distinct app types — from simple URL wrappers to full server runtime APKs |

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
- **CORS bypass** — let static SPAs call external APIs that would otherwise be blocked by CORS.
- **Failover** — automatic fallback to mirror URLs when the primary target is unreachable.
- **PWA** offline cache strategies, custom error pages, per-app host overrides, and payment-scheme handlers.
- **Compatibility toggles** — blob download interception, scroll memory, image repair, clipboard / orientation / notification polyfills, private-network bridging, and Native Bridge capability gates.
- **Cloudflare compatibility** — auto-detect or always-on mode for sites behind Cloudflare.
- **Mixed content** handling — control whether HTTPS pages can load HTTP resources.
- **Third-party cookie** control — block or allow third-party cookies.
- **Private network bridge** — access local network/private IP resources from web content.

</details>

<details>
<summary><b>🛡️ Privacy, fingerprint defense & hardening</b></summary>

- **Browser fingerprint disguise across 50+ vectors** — User-Agent, WebGL, Canvas, AudioContext, ClientRects, timezone, language, memory, media devices, WebRTC, fonts, battery, permissions, performance, storage, notifications, CSS media, iframe propagation, and error-stack cleanup.
- **Hosts-rule ad blocker** with cosmetic MutationObserver filtering, **20 built-in community filter lists** (EasyList, uBlock Origin, AdGuard, AdAway, plus 8 language-specific lists), per-source enable/disable/delete, and custom subscription rules bundled into the APK.
- **Resource encryption** (PBKDF2 + AES-256-GCM) for packaged config, HTML, media, and BGM; optional custom encryption password stronger than package/certificate-derived keys.
- **Runtime hardening** when encryption is on — anti-debug, anti-Frida, DEX-tamper checks; threat response of log-only, silent exit, or randomized crash. Also supports data wipe and fake data responses.
- **WebView/content isolation** for storage, WebRTC, Canvas, Audio, WebGL, fonts, headers, and IP surfaces.
- **Activation-code gating** — local verification, or your own HTTPS endpoint signed with EC P-256. See the [remote activation docs](.github/docs/remote-activation.md).
- **Privacy mode** for sensitive browsing — blocks tracking, clears data on exit.
- **Anti-capture proxy** — prevents screenshot/screen recording of sensitive content.
- **Content obfuscation** — protects page content from unauthorized extraction.

</details>

<details>
<summary><b>📦 Local server runtimes (fork + exec on-device)</b></summary>

- **Node.js** runs in a dedicated `:nodejs` OS process via a native `node_launcher` wrapper loading `libnode.so`; supports custom native `.node` extensions. Build modes: static, SSR, API, fullstack.
- **PHP 8.4** from `pmmp/PHP-Binaries`, downloaded once on first use, with Composer and custom native extensions (`zend_extension`, `.so`). Default document root with PDO/SQLite support.
- **Python** — Flask, Django, FastAPI via uvicorn, Tornado, the built-in HTTP server; pip dependencies resolved into `.pypackages`, custom native extensions supported.
- **Go** — on-device `go build`, `vendor/` offline builds, static serving, and the native `go_exec_loader` wrapper. Pure-Go modules only.
- **WordPress** over local PHP + SQLite (`sqlite-database-integration`), with theme and plugin import.
- **Linux Environment** screen manages toolchains and dependencies for Node, PHP, and Python.
- **Port Manager** coordinates runtime ports across generated apps via broadcast receivers.
- A **local DNS bridge proxy** (HTTP CONNECT in the Android JVM) gives runtimes working DNS resolution and outbound HTTP where the musl/packed binary can't reach the system resolver.
- **Dependency download engine** with progress notifications for runtime dependencies.

</details>

<details>
<summary><b>🧩 Extensions & automation</b></summary>

- **Built-in modules** — video download (YouTube / Bilibili / Douyin / Xiaohongshu extractors), video enhancer with YouTube cleanup (ad skip, max quality, background play, SponsorBlock), web analyzer, find-in-page, dark mode, privacy tools, content enhancer, element blocker, and YouTube launcher.
- **Userscripts** — Greasemonkey/Tampermonkey-style `.user.js` with a `GM_*` bridge (storage, requests, styles, menu commands) and promise-based `GM.*` APIs gated by script grants.
- **MV3 Chrome extension runtime** for manifest content scripts in isolated or main worlds, with `chrome.*` polyfills for runtime, storage, tabs, scripting, and declarative network-request parsing.
- **In-app Chrome Web Store search** — browse and install browser extensions by keyword (or paste a store URL / extension ID), with offline fallback to manual import.
- **Extension panel system** — FAB button integration for module UI, actions registered via `__WTA_MODULE_UI__.register()`.
- **DeclarativeNetRequest engine** for MV3-style request blocking, redirecting, and header modification.
- **Export codes** (`WTA1:` gzip + Base64) and QR sharing via ZXing.
- **AI Coding** skills to generate modules, userscripts, MV3 extensions, front-end apps, and local runtime projects.

</details>

<details>
<summary><b>🤖 AI Coding System</b></summary>

- **Agent-based AI coding engine** with full tool-calling support — the AI can read, write, edit, delete files, search code, and execute plans autonomously.
- **Multi-LLM support** — OpenAI (GPT-4, GPT-4o), Anthropic (Claude 3.5 Sonnet, Claude 3 Opus), Google Gemini (Pro, Flash), Ollama (local models), and any OpenAIChat-compatible endpoint.
- **LiteLLM model registry** with 200+ model configurations and live pricing data.
- **20 built-in AI coding skills:**
  - **App builders (7):** HTML App, React App, Vue 3 App, Node.js App, PHP App, Python App, Go App
  - **Configuration (3):** Gallery App, Multi-Web App, WordPress Theme/Plugin
  - **Module builders (4):** JS Module, CSS Style Module, Userscript, Chrome MV3 Extension
  - **Tool skills (6):** Debug, Explain, Refactor, Optimize, i18n (Internationalize), Imagery (Generate Images)
- **Project file manager** for AI — manage files in the AI workspace.
- **Diff engine** for reviewing code changes before applying them.
- **Plan mode** for multi-step autonomous execution with task breakdown.
- **Session persistence** — save, restore, and manage AI coding sessions.
- **Export AI session to app or module** — convert AI-generated projects into installable apps/modules.
- **Skill editor** — create custom AI coding skills with YAML frontmatter.
- **Built-in agent tools:** Read, Write, Edit, Delete, Glob, Grep, ListFiles, AskUser, TodoWrite/TodoUpdate, Plan, Skill, GenerateImage, ViewImage, ListImages.
- **AI image generation** — generate and refine images through supported providers.

</details>

<details>
<summary><b>📱 App experience</b></summary>

- **Splash screens** — image or video, with skip behavior, trim ranges, and fixed orientation.
- **Background music** — playlists with synced LRC lyrics, lyric animations, custom font/color/stroke/shadow, and online music search and download.
- **Toolbar** — customize button visibility, title display, URL bar, and toolbar style.
- **Status bar** — light & dark mode, color customization, image background.
- **Navigation** — navigation bar style (default, hidden, gesture, minimal).
- **Floating-window mode** — pip-style overlay with configurable size, position, and opacity.
- **Long-press menu styles** — disabled, simple, full, iOS-style, floating, context-menu.
- **Announcement templates** — 10 templates: Minimal, Xiaohongshu, Gradient, Glassmorphism, Neon, Cute, Elegant, Festive, Dark, Nature. Trigger on launch, interval, or no-network.
- **Translation overlay** — 20 target languages via Google, MyMemory, LibreTranslate, Lingva, or Auto engines.
- **Print bridge** — intercept `window.print()` and blob/data-URL PDFs to the Android print framework / PDF export.
- **Notifications** — Web Notification polyfill, scheduled and persistent notifications with progress, URL-polling foreground service, deep links, boot auto-start, scheduled launch, and background-run service.
- **Per-app usage stats** with Vico charts and URL health monitoring.
- **Orientation lock** — portrait, landscape, sensor, reverse portrait/landscape, full sensor, or locked.
- **Zoom** — enable/disable pinch-to-zoom.
- **Swipe refresh** — pull-to-refresh with configurable colors.
- **Screen capture prevention** — anti-capture proxy for sensitive apps.
- **Fullscreen controls** — immersive mode, sticky immersive mode, edge-to-edge.
- **Auto-refresh** — with countdown overlay timer.
- **Scroll memory** — automatically restore scroll position on page revisit.
- **Back state preservation** — maintain page state when navigating back.
- **Image repair** — fix broken image rendering for certain CDN images.

</details>

<details>
<summary><b>🔧 APK / AAB export & signing</b></summary>

- **Custom package name**, `versionName`, `versionCode`, icon, label, architecture target, and export format.
- **Build-time permission injection** with unused permissions pruned from the template manifest.
- **One-tap AAB export** — auto-builds the APK on demand, converts it to a Play-ready signed AAB with `targetSdk` rewritten to the Play-required level (currently 35) and protobuf metadata generated locally; cancellable mid-build.
- **Keystore management** — create, import, export, delete, and certificate-fingerprint viewing; PKCS12/PFX/JKS/BKS import including Android Studio upload-key cases where store and key passwords differ.
- **Signature schemes** — V1, V2, V3 independently controlled, with auto-fallback for legacy certificates; custom V1 signer filename for `META-INF/<name>.SF` / `.RSA`.
- **Binary AXML/ARSC patching** — direct binary editing of Android manifest and resources (no decompile/recompile needed).
- **Performance options** — image compression, WebP conversion, code minification, lazy loading, DNS prefetch, and preload hints.
- **16K page alignment** — ELF binary realignment for Android 16+ compatibility.
- **Full project and app-data backup/restore.**

</details>

<details>
<summary><b>🗂 File manager & project tooling</b></summary>

- **File manager** — a single screen to view, share, install, open, and clear build outputs (APK builds, AAB exports, app clones, build logs) and a user-files directory, with a read-only build-log viewer.
- **Website scraper** for offline packs — HTML, CSS, JS, images, fonts, `url()`, `srcset`, `@import`, path rewriting, same-domain limits, depth limits, and size limits.
- **Multi-Web layouts** — tabs, cards, feeds, drawers, per-site icons/theme colors/extraction selectors/refresh intervals, and shared JS/CSS.
- **Gallery apps** — categorized media, grid/list/timeline views, shuffle/single-loop, sorting, thumbnail bar, overlays, auto-next, and playback memory.
- **App Modifier** — shortcut disguise or real binary clone with manifest/resource patching and re-signing. Can rebrand any installed APK on the device.
- **Frontend project detector** — auto-detect React, Vue, Vite, and other frontend projects.
- **GitHub repo fetcher** — clone repositories for building into apps.
- **Sample projects** — 25+ pre-built sample projects across all runtimes (Node.js Express/Fastify/Koa, Go Echo/Fiber/Gin, Python Flask, WordPress WooCommerce/Portfolio, VitePress/MkDocs/Hexo docs sites).
- **Config presets** — save and load full app configurations for reuse.
- **Permission presets** — save and load permission configurations.
- **Batch import** — import multiple sites or projects at once.

</details>

<details>
<summary><b>🔬 Specialized tools & research features</b></summary>

- **Forced-run** — kiosk/lockdown mode with accessibility service, hardware button blocking, and admin lock.
- **BlackTech** — device action automation and remote control capabilities.
- **Device disguise** — mask the device's hardware identity and specifications.
- **Icon Storm** — icon manipulation and customization features.
- **App Modifier** — rebrand, clone, or disguise any installed APK on the device.

These features are included for technical demonstration and must only be used with informed user consent.

</details>

<details>
<summary><b>🌐 Multi-Language & Internationalization</b></summary>

- **Trilingual support** — Chinese (zh), English (en), Arabic (ar) — full UI strings in all three languages.
- **RTL support** — proper right-to-left layout for Arabic language.
- **Language persistence** — user language preference saved across sessions.
- **Random app name generator** — generates culturally-appropriate random names in all supported languages.
- **AI prompt multi-language** — AI Coding prompts adapt to the user's language.
- **Translation overlay** — in-app page translation via 5 engines with 20+ target languages.

</details>

<details>
<summary><b>🔐 Authentication & Security</b></summary>

- **Activation-code gating** — local (baked into APK) or remote (your own HTTPS endpoint with EC P-256 signature verification).
- **Offline policies** — ALLOW_CACHED (default), DENY, or ALLOW for when the device is offline.
- **Anti-debug protection** — blocks debuggers and dynamic analysis tools.
- **Anti-Frida detection** — detects and responds to Frida instrumentation framework.
- **DEX tamper detection** — verifies APK integrity at runtime.
- **Threat response options** — log only, silent exit, random crash, data wipe, or fake data.
- **Resource encryption** — PBKDF2 + AES-256-GCM with optional custom password.
- **WebView content isolation** — isolates storage, WebRTC, Canvas, Audio, WebGL across surfaces.
- **Configurable keystore** — create, import, export, and manage signing keys.

</details>

<details>
<summary><b>🎨 UI / Design System</b></summary>

- **Jetpack Compose + Material 3** — modern Android UI framework.
- **Wta Design System** — complete design token system with spacing, radius, size, elevation, and alpha primitives.
- **Custom design tokens** — `WtaSpacing`, `WtaRadius`, `WtaSize`, `WtaElevation`, `WtaAlpha` in `WtaTokens.kt`.
- **Semantic colors** — success, warning, error, info, neutral with container variants.
- **Custom typography** — `WtaTypography` with Material 3 type scale.
- **Glassmorphism** — Haze library integration for frosted glass effects.
- **Themed backgrounds** — `WtaBackground`, `WtaScreen` primitives for consistent layout.
- **Settings UI kit** — `WtaSettingCard`, `WtaSettingRow`, `WtaToggleRow`, `WtaChoiceRow`, `WtaTextFieldRow`, `WtaSliderRow`, `WtaDangerRow`.
- **Status banners & states** — `WtaStatusBanner`, `WtaEmptyState`, `WtaLoadingState`, `WtaErrorState`.
- **Button variants** — Primary, Tonal, Outlined, Text, Destructive in Small/Medium/Large.
- **Custom animations** — haptic click feedback, press scale, motion tokens.
- **Swipe back gesture** — `WtaSwipeBack` for intuitive navigation.
- **Design system audit** — automated CI check ensuring UI consistency across the codebase.

</details>

<details>
<summary><b>📱 Progressive Web App (PWA) Features</b></summary>

- **PWA analyzer** — detect and parse service workers and manifest files from websites.
- **PWA offline cache strategies** — network-first, cache-first, and custom strategies.
- **Custom error pages** — 10+ style templates with offline game integration.
- **Network error diagnostics** — helpful error pages with diagnostic information.
- **Manifest parsing** — parse web app manifests for offline configuration.

</details>

<details>
<summary><b>🖨️ Print & Export</b></summary>

- **Print bridge** — intercept `window.print()` calls and route to Android print framework.
- **PDF export** — convert blob/data-URL PDFs to Android PDF export.
- **APK file sharing** — share built APKs directly from the app.
- **QR code sharing** — share app configurations via QR codes (ZXing integration).
- **Export codes** — WTA1 format (gzip + Base64) for sharing app configs.
- **Backup/restore** — full project and app data backup and restore.

</details>

<details>
<summary><b>📊 Analytics & Monitoring</b></summary>

- **Per-app usage statistics** — track how often each generated app is used.
- **Vico charts** — interactive usage charts in Compose Material 3.
- **URL health monitoring** — check if target URLs are reachable.
- **App health monitor** — system health tracking for the host app.
- **Batch import service** — bulk import analytics data.

</details>

<details>
<summary><b>⚙️ System & Performance</b></summary>

- **Native performance engine** — on-device system optimization.
- **System performance optimizer** — tune Android system for better app performance.
- **Native sys optimizer** — low-level system tweaks for speed.
- **APK build optimizer** — image compression, WebP, code minification, lazy loading.
- **DNS prefetch** — pre-resolve DNS for faster page loads.
- **Preload hints** — resource preloading for the WebView.
- **Cache manager** — intelligent caching for faster repeat loads.
- **Efficient pagination** — all lists use pagination for performance.
- **Lazy loading** — images and resources loaded on demand.

</details>

<details>
<summary><b>🔄 Update & Self-Update</b></summary>

- **Self-update engine** — check for and install new versions of WebToApp itself.
- **APK update installer** — download and install updates in-app.
- **Update checker** — periodic checks for new releases.
- **Background services startup** — manage which services start on boot.
- **Auto-start** — boot completed receiver and scheduled launch support.

</details>

---

## AI Coding Skills

WebToApp ships **20 built-in AI skills** that guide the AI agent in generating specific types of projects. Each skill has YAML frontmatter defining its behavior, allowed tools, and capabilities.

### App Builder Skills (7)

| Skill | What it builds | Key Features |
|-------|---------------|--------------|
| **HTML App** | Single-page HTML/CSS/JS app for WebView | Viewport-optimized, 44px+ tap targets, no build step |
| **React App** | React SPA using UMD + HTM | No build step, offline vendor bundles, plain CSS |
| **Vue 3 App** | Vue 3 SPA using global build | No SFC/Vite, reactive/ref state, inline templates |
| **Node.js App** | Node.js HTTP server app | Express or stdlib, PORT-based, 4 build modes |
| **PHP App** | PHP web app with SQLite | PDO/prepared statements, Composer available, no framework needed |
| **Python App** | Python web app (Flask/Django/stdlib) | pip deps in .pypackages, SQLite, no async routes |
| **Go App** | Go HTTP service | net/http stdlib, no CGO, go build on-device |

### Configuration Skills (3)

| Skill | What it creates | Purpose |
|-------|----------------|---------|
| **Gallery App** | `gallery.json` configuration | Media gallery with grid/list/timeline views, categorized items |
| **Multi-Web App** | `multi-web.json` configuration | Multi-site tabs/cards/feed/drawer layout configuration |
| **WordPress App** | WordPress theme/plugin files | Theme style.css + functions.php, plugin with hooks, block editor |

### Module Builder Skills (4)

| Skill | Output | Standard | Runtime |
|-------|--------|----------|---------|
| **JS Module** | `module.json` + `main.js` + optional `style.css` | WebToApp Module Market | IIFE wrapper, GM\_* bridge, panel UI |
| **CSS Style Module** | `module.json` + `style.css` + stub `main.js` | WebToApp Module Market | CSS-only, DOCUMENT_START injection |
| **Userscript** | `.user.js` file | Greasemonkey/Tampermonkey | GM\_* bridge, @match patterns |
| **Chrome MV3** | `manifest.json` + scripts + icons | Chrome MV3 standard | content scripts, declarativeNetRequest, popup |

### Tool Skills (6)

| Skill | Purpose | Tools Available |
|-------|---------|-----------------|
| **Debug** | Diagnose and fix bugs systematically | Read, Write, Edit, Glob, Grep, ListFiles, AskUser, Todo |
| **Explain** | Read and explain code/flow without changes | Read, Glob, Grep, ListFiles |
| **Refactor** | Restructure code without changing behavior | Read, Write, Edit, Delete, Glob, Grep, ListFiles, AskUser, Todo |
| **Optimize** | Make code faster/smaller/more efficient | Read, Write, Edit, Glob, Grep, ListFiles, AskUser, Todo |
| **i18n** | Add translations and internationalization | Read, Write, Edit, Glob, Grep, ListFiles, AskUser, Todo |
| **Imagery** | Generate, view, and refine images via AI | GenerateImage, ViewImage, ListImages, Read, Write |

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

### Community Modules (8 pre-installed)

| Module | ID | Author | Category | Purpose |
|--------|----|--------|----------|---------|
| Hello World | `wta-hello-world` | shiaho777 | OTHER | Floating greeting banner, demo template |
| Web Tint | `wta-web-tint` | shiaho777 | STYLE_MODIFIER | Night shift, grayscale, invert, custom color tint |
| Reading Mode | `wta-reading-mode` | shiaho777 | READING | Article extraction, light/sepia/dark themes |
| Selection Actions | `wta-selection-actions` | shiaho777 | INTERACTION | Floating action bar on text selection |
| Auto Scroll | `wta-auto-scroll` | shiaho777 | NAVIGATION | Smooth auto-scroll with speed control (1x-10x) |
| TV D-Pad Cursor | `wta-tv-dpad-cursor` | shiaho777 | ACCESSIBILITY | Virtual mouse for Android TV D-pad |
| Auto Open Video Player | `wta-auto-open-video-player` | Stremiouser666 | INTERACTION | Video auto-detection with countdown override |
| YouTube Fullscreen Player | `wta-youtube-player` | Stremiouser666 | VIDEO | Clean YouTube playback via privacy-friendly embed |

### Built-in Modules (9 pre-installed)

| Module | Purpose |
|--------|---------|
| Video Download | YouTube / Bilibili / Douyin / Xiaohongshu video extractors |
| Video Enhancer | YouTube cleanup (ad skip, max quality, background play, SponsorBlock) |
| Web Analyzer | Page structure and metadata analysis |
| Find in Page | Text search within the loaded page |
| Dark Mode | Dark theme CSS injector |
| Privacy Tools | Privacy enhancement and tracker blocking |
| Content Enhancer | Content readability and display improvements |
| Element Blocker | Hide or block specific page elements |
| YouTube Launcher | Quick-access YouTube launcher |

---

## Architecture

- The repository has **three Gradle modules**: `app` (the full builder and host), `shell` (the runtime host embedded into generated APKs), and `clone-host` (host code for app cloning — compiled to a `classes.jar`, converted to DEX via d8, and bundled as an asset for `AppCloner`).
- Runtime code is authored in `app` and synchronized into `shell`, so shared WebView/runtime behavior has one source of truth (`core/shell`, `core/webview`, `core/engine`, `core/extension`, `ui/shell`, etc.).
- The APK builder patches template APKs at the binary AXML/ARSC level, injects config/resources, prunes permissions, and signs with `apksig`. A separate encrypted build path (`EncryptedApkBuilder`) offers resource encryption, shelling, and integrity checks.
- The host pins `targetSdk = 28` deliberately — it is what lets generated apps fork+exec native runtimes (Node.js, PHP, Python, Go, WordPress) from app storage, a capability URL-wrapper tools lack. The AAB exporter separately rewrites `targetSdk` for Play Store distribution.
- Server runtimes and the optional GeckoView native runtime are downloaded on first use rather than bundled into the base APK.
- The configuration center is `WebApp` (`data/model/WebApp.kt`) and its `*Config` classes — the single source of truth for all feature settings, carried through a full packaging passthrough chain into the generated APK.
- **AI Coding engine** operates through an agent system with tool-calling, session persistence, skill management, and multi-LLM provider support.

### Source Code Structure

```
com.webtoapp/
├── core/                    # Core business logic
│   ├── activation/          # Activation code system
│   ├── adblock/             # Ad blocking engine
│   ├── ai/                  # AI API integration
│   ├── aicoding/            # AI Coding engine (agent, tools, skills, sessions)
│   ├── apkbuilder/          # APK building engine (22 files)
│   ├── appearance/          # Browser disguise & fingerprinting
│   ├── appmodifier/         # APK cloning/rebranding
│   ├── bgm/                 # Background music player
│   ├── crypto/              # Encryption engine (15 files)
│   ├── engine/              # Browser engines (WebView, GeckoView)
│   ├── extension/           # Extension system (29 files)
│   ├── golang/              # Go runtime
│   ├── i18n/                # Internationalization
│   ├── linux/               # Linux environment
│   ├── market/              # Module market
│   ├── nodejs/              # Node.js runtime
│   ├── php/                 # PHP runtime
│   ├── python/              # Python runtime
│   ├── webview/             # WebView engine (25 files)
│   └── wordpress/           # WordPress runtime
├── data/                    # Data layer
│   ├── model/               # Data models (WebApp.kt = 1479 lines)
│   └── repository/          # Repositories
├── ui/                      # UI layer
│   ├── design/              # Wta design system (22 files)
│   ├── screens/             # All screens (42 files)
│   ├── aicoding/            # AI coding UI
│   └── components/          # Shared UI components (61 files)
└── util/                    # Utilities (28 files)
```

---

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

---

## Build from source

Requirements: Android Studio Hedgehog or newer, JDK 17. The Gradle wrapper pins Gradle 9.4.1.

```bash
git clone https://github.com/shiaho777/web-to-app.git
cd web-to-app
./gradlew assembleDebug
```

For release builds, configure signing through `local.properties` and `app/build.gradle.kts`.

---

## Contributing

| Lane | What you do | Guide |
| --- | --- | --- |
| `modules/` | Publish a community module to the in-app market | [modules/README.md](modules/README.md) |
| Issues | Report a bug or request a feature | [GitHub Issues](https://github.com/shiaho777/web-to-app/issues) |
| Code | Fix a bug or build a feature in the Android client | [CONTRIBUTING.md](.github/CONTRIBUTING.md) |

---

## Contact

Developed by **shiaho**.

| Platform | Link |
| --- | --- |
| GitHub | [github.com/shiaho777/web-to-app](https://github.com/shiaho777/web-to-app) |
| Telegram | [t.me/webtoapp777](https://t.me/webtoapp777) |
| X (Twitter) | [@shiaho777](https://x.com/shiaho777) |
| Bilibili | [b23.tv/8mGDo2N](https://b23.tv/8mGDo2N) |
| QQ Group | 1041130206 |

---

## License

[The Unlicense](LICENSE).

Advanced features such as forced run, BlackTech, device disguise, and Icon Storm are intended for technical demonstration and must only be used with informed user consent.

---

## اردو تعارف

<img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" width="64" align="left" alt="WebToApp آئیکن" />

**WebToApp** ایک اینڈرائیڈ ایپ ہے جو آپ کو ویب سائٹس اور ویب پروجیکٹس کو براہ راست اپنے فون پر انسٹال ایبل اینڈرائیڈ ایپلیکیشنز (APK) میں تبدیل کرنے کی سہولت دیتی ہے۔ یہ صرف ویب سائٹ کو WebView میں لپیٹنے سے کہیں آگے ہے — یہ آپ کے فون پر ہی Node.js، PHP، Python، Go، اور WordPress جیسے سرور رن ٹائم چلا سکتا ہے، ایک مضبوط اینٹی سنسرشپ نیٹ ورک اسٹیک فراہم کرتا ہے، APKs پر دستخط کر سکتا ہے، اور Google Play کے لیے AAB ایکسپورٹ کر سکتا ہے — یہ سب کچھ بغیر کسی PC یا ریموٹ بلڈ سرور کے۔

---

### WebToApp کی خاص باتیں

- **12 قسم کی ایپس بنائیں** — سادہ URL ویپر سے لے کر مکمل سرور رن ٹائم والی APKs تک
- **AI کوڈنگ** — قدرتی زبان میں پرامپٹ دے کر ایپس، ماڈیولز، اور اسکرپٹس بنائیں
- **5 سرور رن ٹائم** — Node.js، PHP 8.4، Python، Go، WordPress براہ راست فون پر
- **دو براؤزر انجن** — سسٹم WebView اور اختیاری GeckoView (Firefox)
- **پرائیویسی اور حفاظت** — 50+ فیچر فنگر پرنٹ چھپائیں، ڈیٹا انکرپٹ کریں، ایڈ بلاک کریں
- **توسیع پذیری** — ماڈیولز، یوزر اسکرپٹس، اور Chrome MV3 ایکسٹنشنز انسٹال کریں
- **APK/AAB ایکسپورٹ** — V1/V2/V3 دستخط، Google Play AAB، کی اسٹور مینجمنٹ
- **20 AI اسکلز** — HTML، React، Vue، Node.js، PHP، Python، Go، WordPress، اور مزید
- **8 کمیونٹی ماڈیولز** — ماڈیول مارکیٹ سے اضافی فیچرز انسٹال کریں
- **Wta ڈیزائن سسٹم** — جدید Material 3 ڈیزائن کے ساتھ خوبصورت UI

---

### WebToApp کس طرح کام کرتا ہے

1. **ایپ بنائیں** — Create App بٹن پر تپ کریں اور اپنا پروجیکٹ کی قسم چنیں
2. **کنفیگر کریں** — URL، نیٹ ورک سیٹنگز، پرائیویسی، اور بہت کچھ سیٹ کریں
3. **بلڈ کریں** — APK یا AAB بطور فون پر ہی بلڈ کریں
4. **شیئر کریں** — اپنی بنائی ہوئی ایپ کو شیئر کریں یا Google Play پر اپ لوڈ کریں

---

### AI Coding کے ساتھ ایپس بنائیں

AI Coding فیچر آپ کو قدرتی زبان میں بتانے کی سہولت دیتا ہے کہ آپ کیا بنانا چاہتے ہیں، اور AI آپ کے لیے کوڈ لکھ دیتا ہے۔ 20 مختلف اسکلز کے ساتھ، آپ HTML ایپس، React/Vue SPAs، Node.js/PHP/Python/Go سرور ایپس، WordPress تھیمز، اور بہت کچھ بنا سکتے ہیں۔

**AI Coding کی خصوصیات:**
- **ملٹی LLM سپورٹ** — OpenAI، Anthropic، Gemini، Ollama، اور OpenAIChat-مطابق
- **پلان موڈ** — AI خود بخود ملٹی سٹیپ ٹاسکس کو پلان اور ایکزیکیوٹ کرتا ہے
- **ٹول استعمال** — AI فائلیں پڑھ، لکھ، ایڈیٹ، اور ڈیلیٹ کر سکتا ہے
- **سیشن سیو** — AI کوڈنگ سیشنز کو محفوظ کریں بعد میں جاری رکھنے کے لیے
- **ایکسپورٹ** — AI جنریٹڈ پروجیکٹس کو ایپ یا ماڈیول کے طور پر ایکسپورٹ کریں

---

### آپ کیا بنا سکتے ہیں

| ان پٹ | آؤٹ پٹ | کس کے لیے مفید |
|--------|--------|----------------|
| ویب سائٹ URL | WebView-based APK | لینڈنگ پیجز، ٹولز، ڈیش بورڈز، دستاویزات |
| HTML / سٹیٹک فرنٹ اینڈ | Localhost-backed APK | React، Vue، Vite، آف لائن ویب ایپس |
| Node.js / PHP / Python / Go | سرور رن ٹائم APK | چھوٹے سرور ایپس، ایڈمن ٹولز، ڈیمو |
| WordPress | WordPress + SQLite APK | پورٹیبل سائٹس، تھیم/پلگ ان ڈیمو |
| تصاویر / ویڈیوز / گیلری | میڈیا فوکسڈ APK | البمز، کورس میٹریل، پورٹ فولیو |
| متعدد سائٹس | ملٹی ویب APK | پورٹلز، ایپ کلیکشنز |
| انسٹالڈ APK | ریبرانڈڈ کلون | آئیکن/نام/پیکیج تجربات |

---

<div align="center">

**اوپن سورس · Android پاور یوزرز کے لیے بنایا گیا · پروجیکٹ کو سپورٹ کرنے کے لیے Star کریں**

</div>
