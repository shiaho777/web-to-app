# Plugin Authoring

WebToApp stays extensible after an app ships. Three kinds of plugins share one management surface and one injection pipeline:

| Type | What it is | Good for |
| --- | --- | --- |
| **[HCJ Plugin](/extensions/js-module)** | A `plugin.json` + single `plugin.html` package — plain HTML + CSS + JS, no DSL | Custom features, panels, settings UIs |
| **[Userscript](/extensions/userscript)** | Tampermonkey/Greasemonkey-style `.user.js` | Porting existing userscripts; `GM_*` APIs |
| **[Chrome MV3](/extensions/chrome-mv3)** | A Manifest V3 Chrome extension | Porting browser extensions; `chrome.*` APIs |

HCJ (HTML+CSS+JS) is the native format — think of it as a userscript upgraded with a real UI surface.

## How injection works

Script plugins are injected at their configured **run time**:

| Run time | Fires at |
| --- | --- |
| `document_start` | `onPageStarted`, before page scripts |
| `document_end` | DOMContentLoaded (default) |
| `document_idle` | after load |

`hcj.addStyle()` (or a legacy `style.css`) injects page CSS at document-start so visual plugins apply before first paint. **URL match rules** (Chrome-style globs or `/regex/`) decide which pages a plugin runs on. Chrome extensions run through the MV3 engine — a hidden WebView for the background service worker plus dynamically registered content scripts.

## Where plugins live

Every plugin gets an entry on the **plugin surface**. The user picks the host style per app (or globally):

- **Toolbar** — a plugins button in the native toolbar; tap to open the plugin drawer
- **Floating handle** — a draggable native handle that auto-collapses (works with the toolbar hidden)
- **Menu** — an item inside the overflow menu

A plugin whose `plugin.html` carries panel markup opens it in the user-chosen panel host — **bottom sheet**, **floating window**, or **fullscreen**. A page-only plugin fires its `hcj.on('action')` handler instead. Chrome extension `action.popup` pages are hosted in the same panel surface.

::: warning Accuracy notes
- **Userscript `GM_*` functions are not gated by `@grant`** — all are exposed unconditionally.
- **Chrome `ISOLATED` vs `MAIN` world is not truly isolated** — Android WebView has a single JS context; isolation is simulated.
- **The MV3 "background service worker" is a hidden WebView**, not a real service worker.
:::

## Where to go next

- [HCJ Plugins](/extensions/js-module) — the native package format.
- [CSS Plugins](/extensions/css-module) — pure style overrides.
- [Userscripts](/extensions/userscript) — the `GM_*` / `GM.*` API reference.
- [Chrome MV3](/extensions/chrome-mv3) — the supported `chrome.*` surface.
- [Publish to the Market](/extensions/publish) — share plugins with the community.
