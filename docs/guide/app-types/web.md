# Web

Wraps a remote URL in a WebView. This is the most common — and most configurable — app type.

## When to use

Landing pages, tools, dashboards, documentation, and internal systems that already live at a URL.

## Key configuration

- **Target URL** — the site to load.
- **Browser engine** — System WebView by default; optional GeckoView (downloaded on first use, required for ECH). See [Browser Kernel](/guide/more-features/browser-kernel).
- **User agent & desktop mode** — present a custom UA and/or request the desktop site.
- **JS/CSS injection** — inject scripts/styles at document-start, document-end, or idle.
- **Popup handling** — same window, external browser, popup window, or block.

## Notes

- The Web editor exposes the **full** set of capability cards (fullscreen, splash, ad blocking, DNS, disguise, and more) in a single screen — Web apps have one combined editor rather than separate core/common config.
- For multiple sites in one app, use [Multi-Web](/guide/app-types/multi-web).
- To archive a site for offline use, use [Offline Pack](/guide/app-types/offline-pack).
