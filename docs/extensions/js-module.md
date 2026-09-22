# HCJ Plugins

An HCJ plugin is the native format: ordinary **HTML + CSS + JavaScript** packaged as a folder. There is no custom DSL — if you can write a userscript, you can write an HCJ plugin, and when you need a UI you write real HTML instead of learning a form schema.

## File layout

```
my-plugin/
├── plugin.json    # required — the manifest
├── plugin.html    # required — page script + panel document in one file
├── icon.png       # optional — ≤256KB; png/svg/webp/jpg/jpeg
└── files/         # optional extra package files
```

::: info One HTML file is the whole plugin
`plugin.html` is a normal HTML document. The part that runs inside matching **pages** lives in an inert block `<script type="hcj/page">…</script>` (unknown script types never execute — the standard data-block idiom). Everything else in the document is the **panel UI**. A page-only plugin is just a `plugin.html` holding nothing but the `hcj/page` block.
:::

::: details Legacy multi-file layout
Older packages may still carry `main.js` + `panel.html` + `style.css`. They keep loading unchanged — page scripts run, `panel.html` is hosted, `style.css` is injected. Saving such a package in the editor rewrites it as `plugin.html`.
:::

## `plugin.json` schema

```json
{
  "id": "my-plugin",
  "name": "My Plugin",
  "version": "1.0.0",
  "description": "What it does",
  "author": "You",
  "homepage": "https://example.com",
  "icon": "star",
  "matches": ["*://example.com/*"],
  "excludeMatches": [],
  "runAt": "document_end",
  "permissions": ["STORAGE"],
  "toolbar": true
}
```

### Field reference

| Field | Notes |
| --- | --- |
| `id` | Globally unique. |
| `name` | Display name (required). |
| `version` | Semver string. |
| `author` / `homepage` | Credits. |
| `icon` | A Material Icons name or a package icon filename. |
| `matches` | Chrome-style globs; `/pattern/` denotes a regex. Default `["*"]`. |
| `excludeMatches` | Same syntax; wins over `matches`. |
| `runAt` | `document_start`, `document_end` (default), `document_idle`. |
| `permissions` | Capability gates for `hcj.*`: `STORAGE` (config KV), `FETCH` (cross-origin), `NOTIFY`, `BADGE`, `CLIPBOARD`, `DOWNLOAD`. Page DOM access needs no permission. |
| `toolbar` | Show an entry for this plugin. Default `true`. |
| `preferredEntry` | Suggested entry style (`toolbar` / `floating_handle` / `menu`); the user's app-level choice wins. |

## URL matching

- **Glob** (default) — Chrome-style. `*` matches anything; `*://` expands to `(https?|ftp|file)://`; `*` alone matches everything.
- **Regex** — wrap the pattern in slashes: `"/example\\.com\\/article\\/\\d+/"`. A 200ms timeout applies; a timeout counts as no match.
- **`excludeMatches`** — removes matching URLs from the result set.

## The page-script contract

The code inside `<script type="hcj/page">` is wrapped in an IIFE with a scoped `hcj` API object (errors go to `console.error` and never break the page):

```html
<script type="hcj/page">
const greeting = hcj.config.get('greeting', 'Hello')
const banner = document.createElement('div')
banner.textContent = greeting
banner.style.cssText = 'position:fixed;top:0;left:0;z-index:99999;padding:8px;background:#2563eb;color:#fff'
document.body.appendChild(banner)
</script>
```

| API | Notes |
| --- | --- |
| `hcj.id` / `hcj.manifest` / `hcj.lang` | Plugin id, manifest summary, app language |
| `hcj.config.get/set/remove/all` | Persistent KV — requires `STORAGE` |
| `hcj.fetch(url, opts)` | Cross-origin fetch via the host — requires `FETCH`, returns a Promise |
| `hcj.notify(title, body)` | Android notification — requires `NOTIFY` |
| `hcj.toast(msg)` | In-page floating toast — prefer over `notify` for action feedback |
| `hcj.badge(text, color)` | Toolbar badge — requires `BADGE` |
| `hcj.addStyle(css)` | Inject page CSS (idempotent per document) |
| `hcj.panel.open()` / `close()` | Open/close this plugin's panel document |
| `hcj.panel.send(msg)` / `hcj.panel.onMessage(fn)` | Page ↔ panel messages |
| `hcj.on('action', fn)` | Fired when the user taps the plugin entry (page-only plugins) |
| `hcj.emit(evt, data)` | Fire a custom event into your own handlers |
| `hcj.log(msg)` | Host-side log |

::: warning No top-level `return`
Because your code is wrapped in an IIFE, a top-level `return` is invalid and is rejected by the market validator.
:::

## The panel (the rest of `plugin.html`)

Everything outside the `hcj/page` block is the plugin's own page, hosted in the user's chosen container (bottom sheet / floating window / fullscreen). Inside it, a mirrored `hcjPanel` object is available before your scripts run:

```html
<script>
  hcjPanel.onMessage((msg) => { /* page → panel */ })
  hcjPanel.send({ type: 'refresh' })        // panel → page
  const theme = hcjPanel.config.get('theme', 'auto')
  hcjPanel.config.set('theme', 'dark')
  hcjPanel.close()
</script>
```

**Settings UI is yours.** Build it directly in `plugin.html` with ordinary HTML/CSS/JS and persist via `hcjPanel.config`. The page side can re-read values or listen for a `hcj.panel.onMessage` ping to apply changes live.

## Userscript interop

`.user.js` files import directly: the `==UserScript==` block is converted into `plugin.json` (`@match`/`@include` → `matches`, `@grant` → `permissions`, `@run-at` → `runAt`), and `GM_*` calls keep working through the userscript runtime. An `.hcj` file is just a zipped package for sharing.

See the built-in plugins under [`app/src/main/assets/plugins/`](https://github.com/shiaho777/web-to-app/tree/main/app/src/main/assets/plugins) and the market packages under [`modules/`](https://github.com/shiaho777/web-to-app/tree/main/modules) for complete working examples.
