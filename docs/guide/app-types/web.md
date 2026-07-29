# Web

Wraps a remote URL in a WebView. This is the most common — and most configurable — app type.

## When to use

Landing pages, tools, dashboards, documentation, and internal systems that already live at a URL.

## Core config

The Web type's core config is the WebView behavior (backed by `WebViewConfig`).

### Target & engine

- **Target URL** — the site to load.
- **Browser engine** — System WebView by default; optional GeckoView (downloaded on first use, required for ECH). See [Browser Kernel](/guide/more-features/browser-kernel).

### User agent & display

- **User agent mode** — system default or a custom UA string (`userAgentMode`, `customUserAgent`).
- **Desktop mode** — request the desktop version of the site (`desktopMode`).
- **Zoom & viewport** — enable zoom, initial scale, and viewport mode.

### Injection

- **JS/CSS injection** — inject scripts/styles at document-start, document-end, or idle (`injectScripts`).

### Popups & windows

- **New-window behavior** — how popups/new windows open (`newWindowBehavior`: same window, external browser, popup window).
- **Popup blocker** — block popups (`popupBlockerEnabled`).
- **JS can open windows** — with policy (`javaScriptCanOpenWindows`, `jsOpenWindowsPolicy`).

## Notes

- The Web editor exposes the **full** set of capability cards (fullscreen, splash, ad blocking, DNS, disguise, and more) in a single screen — Web apps have one combined editor rather than separate core/common config.
- For multiple sites in one app, use [Multi-Web](/guide/app-types/multi-web).
- To archive a site for offline use, use [Offline Pack](/guide/app-types/offline-pack).
