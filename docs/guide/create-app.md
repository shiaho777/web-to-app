# Creating an App

WebToApp supports many app types. Each type changes what gets packaged and how the generated app behaves at runtime. Pick the one that matches your input.

## App types

### Web
Wraps a remote URL in a WebView (system WebView by default, optional GeckoView). Best for landing pages, dashboards, docs, and internal systems. Configure the target URL, user agent, desktop mode, JS/CSS injection, and popup handling.

### HTML / Frontend
Packages a static front-end (React, Vue, Vite, plain HTML) into the APK and serves it from local files. The generated app gets `allowFileAccess` so pure file-based loads work offline. Ideal for offline web apps and static builds.

### Node.js
Embeds a Node.js 18.20 runtime that runs in a dedicated `:nodejs` OS process via a native `node_launcher` wrapper loading `libnode.so`. Your server code runs on a local port; the WebView points at `localhost`. Supports custom native `.node` extensions.

### PHP
Runs PHP 8.4 (from `pmmp/PHP-Binaries`) with Composer 2.10. Downloaded once on first use. Supports custom native extensions (`zend_extension`, `.so`).

### Python
Runs Python 3.14 — Flask, Django, FastAPI (uvicorn), Tornado, or the built-in HTTP server. pip dependencies resolve into `.pypackages`. Custom native extensions supported. Binary names are versioned so future bumps don't hard-code paths.

### Go
Uses the official Go 1.26 Linux arm64 toolchain (USTC mirror for CN). Supports on-device `go build` / `go mod` / `go run`, `vendor/` offline builds, and static serving via the native `go_exec_loader` wrapper.

### WordPress
Runs WordPress 7.x over local PHP + SQLite (`sqlite-database-integration`), with theme and plugin import. A portable CMS in an APK.

### Image / Video / Gallery
Media-focused apps. Gallery apps support categorized media, grid/list/timeline views, shuffle/loop, sorting, thumbnail bars, overlays, auto-next, and playback memory.

### Multi-Web
Combines multiple sites into one app with tab, card, feed, or drawer layouts. Each site can have its own icon, theme color, extraction selector, and refresh interval, plus shared JS/CSS.

## Common settings (all types)

Regardless of type, every app shares a large set of switches, grouped in the editor:

- **Browser engine & networking** — engine choice, kernel disguise, proxies, DoH, ECH, TLS fingerprint, CORS bypass, failover mirrors.
- **Privacy & hardening** — fingerprint disguise, ad blocking, resource encryption, anti-debug, activation gating.
- **Appearance** — splash screen, background music, toolbar, status bar, navigation, themes.
- **Extensions** — built-in modules, userscripts, MV3 extensions.
- **Export** — package name, version, icon, architecture target, signing, AAB.

Each of these is covered in its own guide page. The single source of truth for all settings is the `WebApp` model and its nested `*Config` classes; everything you set in the editor flows through a packaging passthrough chain into the generated APK.

::: tip
Use the **sample projects** bundled with the app (React, Vue, Vite, Node/Express, PHP/Laravel, Python/Flask, Go/Gin, WordPress, and more) to see a working configuration for each stack before building your own.
:::
