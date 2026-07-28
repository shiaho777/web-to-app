# Introduction

**WebToApp** turns web projects into standalone, signed Android APKs — entirely on your phone. It is not a URL wrapper. It is a pocket-sized APK workshop that can run real server runtimes, ship a hardened anti-censorship network stack, sign bundles for Google Play, and run MV3 browser extensions, all without a PC or a remote build server.

## What makes it different

Most "website to app" tools stop at wrapping a URL in a WebView. WebToApp diverges exactly where the hard parts are:

- **It runs real server runtimes on-device.** Node.js, PHP, Python, Go, and WordPress are fork+exec'd as native binaries straight from app storage — like Termux, packaged into an installable APK. URL-wrapper tools cannot do this at all.
- **It ships a hardened network stack.** DNS-over-HTTPS, TLS fingerprint spoofing (Chrome / Firefox / Safari JA3 templates) with a local MITM bridge, Encrypted Client Hello (ECH) on the GeckoView engine, per-app proxies, and CORS bypass for locked-down SPAs.
- **The whole build is self-contained.** Binary AXML/ARSC patching, permission pruning, V1/V2/V3 signing, and Google Play-ready AAB export all happen inside the app via `apksig`.
- **It stays extensible after shipping.** Add JS/CSS modules, Tampermonkey-style userscripts, or MV3 Chrome extensions without rebuilding the host.

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

## How to read these docs

- **[User Guide](/guide/getting-started)** — install the app and build your first APK, then configure networking, runtimes, privacy, and appearance.
- **[Developer Docs](/developer/)** — how the codebase is organized, how the export pipeline and shell sync work, and the recipes for common changes.
- **[Extension Authoring](/extensions/)** — write JS/CSS modules, userscripts, and MV3 Chrome extensions, and publish them to the in-app market.

::: tip
The host app UI is available in 10 languages. Switch it anytime under **Settings → Language**. The language of the apps you *generate* is configured separately per app.
:::
