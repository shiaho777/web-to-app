# Extension Authoring

WebToApp stays extensible after an app ships. You can add three kinds of extensions, all managed by a single `ExtensionManager` and injected by the WebView at page-lifecycle hooks:

| Type | What it is | Good for |
| --- | --- | --- |
| **[JS Module](/extensions/js-module)** | A `module.json` manifest + `main.js` (+ optional CSS) | Custom features with a config UI and floating panel |
| **[CSS Module](/extensions/css-module)** | A pure style override (still needs a `main.js` stub) | Theming, restyling, dark mode |
| **[Userscript](/extensions/userscript)** | Tampermonkey/Greasemonkey-style `.user.js` | Porting existing userscripts; `GM_*` APIs |
| **[Chrome MV3](/extensions/chrome-mv3)** | A Manifest V3 Chrome extension | Porting browser extensions; `chrome.*` APIs |

## How injection works

All four types are normalized into one internal `ExtensionModule` model. At runtime, `WebViewManager` injects each module at its configured **run time**:

| Run time | Fires at |
| --- | --- |
| `DOCUMENT_START` | `onPageStarted` |
| `DOCUMENT_END` | `onPageFinished` (DOMContentLoaded) |
| `DOCUMENT_IDLE` | after load (default) |
| `CONTEXT_MENU` | on context menu |
| `BEFORE_UNLOAD` | before unload |

Each module also carries **URL match rules** (Chrome-style globs or regex) that decide which pages it runs on.

::: warning Important accuracy notes
A few behaviors differ from what other extension platforms imply. These are documented on the relevant pages, but worth knowing up front:
- **Userscript `GM_*` functions are not gated by `@grant`** — all are exposed unconditionally.
- **Chrome `ISOLATED` vs `MAIN` world is not truly isolated** — Android WebView has a single JS context; isolation is simulated.
- **`GM_notification` only logs**; the MV3 "background service worker" is a hidden WebView, not a real service worker.
:::

## Where to go next

- [JS Modules](/extensions/js-module) — the most capable native format.
- [Userscripts](/extensions/userscript) — the `GM_*` / `GM.*` API reference.
- [Chrome MV3](/extensions/chrome-mv3) — the supported `chrome.*` surface.
- [Publish to the Market](/extensions/publish) — share JS/CSS modules with the community.
