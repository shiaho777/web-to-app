# Userscripts

WebToApp runs Tampermonkey/Greasemonkey-style `.user.js` scripts with a `GM_*` bridge. Import a script and it is parsed, stored, and injected like any other extension.

## Metadata block

A userscript must contain a `// ==UserScript== ... // ==/UserScript==` block. Without it, the file is imported as plain JS (`isValid = false` as a userscript).

```js
// ==UserScript==
// @name         Example Script
// @namespace    https://example.com/
// @version      1.0.0
// @description  Does a thing
// @author       You
// @match        *://example.com/*
// @grant        GM_getValue
// @grant        GM_setValue
// @grant        GM_xmlhttpRequest
// @run-at       document-idle
// @require      https://cdn.example.com/lib.js
// @resource     ICON https://example.com/icon.png
// @noframes
// ==/UserScript==

console.log('Hello from a userscript')
```

### Supported `@` tags

`@name`, `@description`/`@desc`, `@version`, `@author`, `@namespace`, `@match`, `@include`, `@exclude`/`@exclude-match`, `@run-at`, `@grant`, `@require`, `@resource` (`name url`), `@noframes`, `@icon`/`@iconURL`/`@icon64`/`@icon64URL`, `@homepage`/`@homepageURL`/`@website`.

- **`@run-at`**: `document-start` → `DOCUMENT_START`; `document-end`/`document-body` → `DOCUMENT_END`; `document-idle` (default) → `DOCUMENT_IDLE`.
- **`@match`**: Chrome glob (`<all_urls>` → `*`).
- **`@include`**: if written as `/regex/`, treated as a regex.
- **`@grant none`**: filtered out.

## `GM_*` API

Both the classic `GM_*` functions and the Promise-based `GM.*` (Tampermonkey 4.x) are provided.

| API | Notes |
| --- | --- |
| `GM_getValue` / `GM_setValue` / `GM_deleteValue` / `GM_listValues` | Backed by SharedPreferences, namespaced per script. |
| `GM_xmlhttpRequest` | Via OkHttp. Supports `method`, `url`, `headers`, `data`, `responseType`; callbacks `onload`/`onerror`/`ontimeout`/`onprogress`; returns `{status, statusText, responseText, responseHeaders, finalUrl}`. |
| `GM_addStyle` | Inject a stylesheet. |
| `GM_setClipboard` | Write to the clipboard. |
| `GM_openInTab` | Open a URL in a new tab. |
| `GM_log` | Log output. |
| `GM_notification` | **Simplified — logs only; does not show a system notification.** |
| `GM_getResourceText` / `GM_getResourceURL` | Read `@resource` contents. |
| `GM_registerMenuCommand` / `GM_unregisterMenuCommand` | Integrated into the floating window menu. |
| `GM_openScriptWindow` / `GM_updateScriptWindow` / `GM_closeScriptWindow` | WebToApp-specific script windows. |
| `GM_info` | `{script:{name,version,description,author,namespace,grants,resources}, scriptHandler:'WebToApp', version:'1.0'}` |
| `unsafeWindow` | Equals `window` (no sandbox). |

The Promise-based `GM.*` mirrors all of the above (`GM.getValue`, `GM.xmlHttpRequest`, …).

::: warning Grants are not enforced
Although `@grant` declarations are parsed and listed in `GM_info.script.grants`, **all `GM_*` functions are exposed unconditionally** — the host does not currently gate APIs by grant. Declare grants anyway for portability with real Tampermonkey/Greasemonkey.
:::

## `@require` and `@resource`

`@require` scripts and `@resource` files are downloaded and cached, then injected before your script body.

- Max single `@require` size: **5 MB**.
- Max total extension package size: **50 MB**.

Injection order: bootstrap (window manager) → GM polyfill → each `@require` → your script.

## Example

```js
// ==UserScript==
// @name         Highlighter
// @match        *://*/*
// @grant        GM_addStyle
// @run-at       document-start
// ==/UserScript==

GM_addStyle('mark { background: #fde047; }')
document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('code').forEach(el => {
    const m = document.createElement('mark')
    m.textContent = el.textContent
    el.replaceWith(m)
  })
})
```
