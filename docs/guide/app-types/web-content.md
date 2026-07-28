# Web Content Types

These types wrap or package web content. They don't run a server runtime — the WebView loads a remote URL or local files.

## Web

Wraps a remote URL in a WebView.

- **Engine** — System WebView by default; optional GeckoView (downloaded on first use, required for ECH).
- **Key config** — target URL, user agent, desktop mode, JS/CSS injection (document-start/end/idle), popup handling (same window / external / popup / block).
- **Good for** — landing pages, tools, dashboards, docs, internal systems.

The Web type is also the richest to configure: its editor exposes the full set of capability cards (fullscreen, splash, ad blocking, DNS, disguise, and more). See [App Configuration](/guide/config/).

## Multi-Web

Combines multiple sites into one app.

- **Layouts** — tabs, cards, feed, or drawer.
- **Per-site** — each site can have its own icon, theme color, extraction selector, and refresh interval.
- **Shared** — common JS/CSS injected across sites.
- **Good for** — link hubs, portals, app collections.

## HTML

Packages local HTML into the APK and serves it from local files.

- **Input** — a folder of HTML/CSS/JS, or a `.zip` that is extracted on import.
- **Entry file** — defaults to `index.html`.
- **File access** — the generated app gets `allowFileAccess` so pure file-based loads work offline.
- **Load mode / port** — configurable, with a port-conflict mode for the local server.
- **Good for** — static builds, offline web apps.

## Offline Pack

Scrapes a live site into a self-contained offline package, then wraps it like an HTML app.

- **Scraper controls** — max depth, follow-links toggle, max files, max total size, skip patterns, timeout, and a CDN-resource download toggle.
- **Rewrites** — HTML/CSS/JS/images/fonts, `url()`, `srcset`, `@import`, with path rewriting and same-domain/depth/size limits.
- **Good for** — archiving a site for offline use.

## Frontend

Packages a *built* front-end project (the production output of React, Vue, Vite, etc.).

- **Input** — the build output directory; the framework is detected/selected.
- **Toolchain** — links to the [Linux Environment](/guide/more-features/dev-tools#linux-environment) when a build step is needed on-device.
- **Good for** — React, Vue, Vite production builds, SPAs.

::: info HTML vs Frontend vs Offline Pack
- **HTML** — you already have static files.
- **Frontend** — you have a framework project whose build output you package.
- **Offline Pack** — you start from a remote URL and scrape it down.
:::
