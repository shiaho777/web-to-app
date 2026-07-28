# JS Modules

A JS module is the most capable native extension format: a manifest, a script, optional CSS, a config UI, and an optional floating panel — all packaged together.

## File layout

```
my-module/
├── module.json    # required — the manifest
├── main.js        # required — runs in the WebView
├── style.css      # optional — auto-injected when hasCss / cssCode present
└── icon.png       # optional — ≤256KB; png/svg/webp/jpg/jpeg
```

## `module.json` schema

```json
{
  "id": "my-module",
  "name": "My Module",
  "description": "What it does",
  "icon": "star",
  "category": "CONTENT_ENHANCE",
  "tags": ["demo"],
  "version": { "code": 1, "name": "1.0.0", "changelog": "Initial release" },
  "author": { "name": "You", "url": "https://example.com" },
  "runAt": "DOCUMENT_END",
  "urlMatches": [
    { "pattern": "*://example.com/*", "isRegex": false, "exclude": false }
  ],
  "permissions": ["DOM_ACCESS", "STORAGE"],
  "configItems": [
    {
      "key": "greeting",
      "name": "Greeting text",
      "type": "TEXT",
      "defaultValue": "Hello",
      "required": true
    }
  ]
}
```

::: warning `version` is an object
`version` has `code` (int), `name` (semver string), and `changelog`. Don't make it a plain string in `module.json`.
:::

### Field reference

| Field | Notes |
| --- | --- |
| `id` | Globally unique. |
| `icon` | A Material Icons name (e.g. `star`, `package`). |
| `category` | One of: `CONTENT_FILTER`, `CONTENT_ENHANCE`, `STYLE_MODIFIER`, `THEME`, `FUNCTION_ENHANCE`, `AUTOMATION`, `NAVIGATION`, `DATA_EXTRACT`, `DATA_SAVE`, `INTERACTION`, `ACCESSIBILITY`, `MEDIA`, `VIDEO`, `IMAGE`, `AUDIO`, `SECURITY`, `ANTI_TRACKING`, `SOCIAL`, `SHOPPING`, `READING`, `TRANSLATE`, `DEVELOPER`, `OTHER`. |
| `runAt` | `DOCUMENT_START`, `DOCUMENT_END` (default), `DOCUMENT_IDLE`, `CONTEXT_MENU`, `BEFORE_UNLOAD`. |
| `urlMatches[]` | `{pattern, isRegex=false, exclude=false}`. See [URL matching](#url-matching). |
| `permissions[]` | Display-only; the runtime does **not** sandbox based on these. Dangerous ones (e.g. `CAMERA`, `LOCATION`, `EVAL`, `FILE_ACCESS`) get extra review. |
| `configItems[]` | User-configurable fields; see [Config items](#config-items). |

## URL matching

- **`isRegex: false`** (default) — Chrome-style glob. `*` matches anything; `*://` expands to `(https?|ftp|file)://`; `*` or `<all_urls>` matches everything. Falls back to substring `contains` if the glob fails to match.
- **`isRegex: true`** — Java regex with a **200ms timeout**; a timeout counts as no match.
- **`exclude: true`** — removes matching URLs from the result set.

## The `main.js` contract

Your code is wrapped in an IIFE with a `try/catch` (errors go to `console.error` and never break the page). These globals are available:

| Global | Value |
| --- | --- |
| `__MODULE_INFO__` | `{id, name, icon, version, uiConfig, runMode}` |
| `__MODULE_CONFIG__` | The resolved config object |
| `__MODULE_UI_CONFIG__` | UI config |
| `__MODULE_RUN_MODE__` | `'INTERACTIVE'` or `'AUTO'` |
| `__MODULE_PANEL_HTML__` | Your `panelHtml`, if any |
| `getConfig(key, defaultValue)` | Convenience accessor for config values |

```js
// main.js
const greeting = getConfig('greeting', 'Hello')
const banner = document.createElement('div')
banner.textContent = greeting
banner.style.cssText = 'position:fixed;top:0;left:0;z-index:99999;padding:8px;background:#2563eb;color:#fff'
document.body.appendChild(banner)
```

::: warning No top-level `return`
Because your code is wrapped in an IIFE, a top-level `return` statement is invalid and is rejected by the market validator.
:::

## Config items

`configItems[]` build a settings UI for the user. Each item:

```json
{
  "key": "speedLevel",
  "name": "Speed",
  "description": "Scroll speed multiplier",
  "type": "NUMBER",
  "defaultValue": "3",
  "options": [],
  "required": false,
  "placeholder": "",
  "validation": ""
}
```

Supported `type` values: `TEXT`, `TEXTAREA`, `NUMBER`, `BOOLEAN`, `SELECT`, `MULTI_SELECT`, `RADIO`, `CHECKBOX`, `COLOR`, `URL`, `EMAIL`, `PASSWORD`, `REGEX`, `CSS_SELECTOR`, `JAVASCRIPT`, `JSON`, `RANGE`, `DATE`, `TIME`, `DATETIME`, `FILE`, `IMAGE`.

Read values with `getConfig(key, defaultValue)`.

## Interactive panel

For a floating UI, provide `panelHtml` and register a panel button:

```js
window.__WTA_MODULE_UI__.register({
  id: __MODULE_INFO__.id,
  name: __MODULE_INFO__.name,
  icon: __MODULE_INFO__.icon
})
```

Inside `panelHtml`, use `data-wta-action` attributes wired to `window.__wta_module_action_<name>` handlers, and style with the `var(--wta-*)` theme variables so your panel matches the app theme.

## Multi-file modules

`codeFiles` is a `Map<filename, source>`. The entry point is auto-detected from `main.js`, `index.js`, `app.js`, `script.js`, or `content.js`.

## Packaging & sharing

- Module export extension: `.wtamod`; a bundle of modules: `.wtapkg`.
- Share code prefix: `WTA1:` (gzip + Base64), shareable via QR.

See the built-in `hello-world` and `auto-scroll` modules under [`modules/`](https://github.com/shiaho777/web-to-app/tree/main/modules) for complete working examples.
