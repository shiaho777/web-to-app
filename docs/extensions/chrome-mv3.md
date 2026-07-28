# Chrome MV3 Extensions

WebToApp runs Manifest V3 (and V2) Chrome extensions: content scripts, popups, options pages, a background context, and `declarativeNetRequest` rules. You can import a `.crx`/`.zip`, or install live from the Chrome Web Store via the **Browser Extensions** tab.

## Supported `manifest.json` fields

| Field | Notes |
| --- | --- |
| `manifest_version` | `2` or `3`. |
| `name`, `version`, `description` | Support `__MSG_key__` i18n via `_locales/<locale>/messages.json`. |
| `default_locale` | Base locale for i18n. |
| `content_scripts[]` | `matches`, `exclude_matches`, `js[]`, `css[]`, `run_at` (`document_start/end/idle`, default idle), `all_frames` (default false), `world` (`"MAIN"` or default `"ISOLATED"`). Each entry becomes one internal module. |
| `action.default_popup` / `browser_action` / `page_action` | Popup page. |
| `options_page` / `options_ui.page` | Options page. |
| `background.service_worker` / `background.scripts[0]` | Background context; `background.type: "module"` loads as an ES module. |
| `declarative_net_request.rule_resources[]` | `{id, enabled, path}`. |
| `permissions`, `host_permissions`, `optional_permissions` | Mapped to internal permission categories; unsupported ones produce a warning. |

## `chrome.*` API support

A polyfill provides a broad `chrome.*` (and `browser.*`) surface. **Core functional** namespaces:

| Namespace | Functional highlights |
| --- | --- |
| `runtime` | `getURL`, `getManifest`, `sendMessage`, `onMessage`, `onInstalled`, `connect`/`onConnect`, `getPlatformInfo`, `id`, `lastError` |
| `storage` | `local` / `sync` / `session` + `onChanged` (`sync` maps to local; `managed` is a no-op) |
| `tabs` | `create`, `query`, `sendMessage`, `get`, `getCurrent`, `update` + events |
| `scripting` | `executeScript`, `insertCSS`, `removeCSS`, `registerContentScripts`, `unregisterContentScripts`, `getRegisteredContentScripts` |
| `cookies` | Cookie access |
| `alarms` | Scheduling |
| `declarativeNetRequest` | Rule-based blocking/redirect (see below) |
| `downloads` | `download` |
| `webRequest` | `onBeforeRequest` can register real filtering via the native bridge |
| `i18n`, `notifications`, `permissions`, `contextMenus`, `commands` | Supported |

Many other namespaces (`windows`, `management`, `bookmarks`, `history`, `identity`, `system.*`, `offscreen`, `tts`, …) are present as **stubs** returning defaults. See the [API Reference](/extensions/api-reference) for the per-API status.

::: warning No true world isolation
Android WebView has a **single JavaScript context**. `world: "ISOLATED"` and `world: "MAIN"` are both injected into the same window via `evaluateJavascript`; isolation is *simulated* by each extension overwriting `globalThis.chrome` and capturing its own extension id in a closure. Don't rely on real isolation between extensions.
:::

::: info Background is a hidden WebView
The MV3 "service worker" runs in a hidden WebView with a service-worker environment shim (`self`, `clients`, `caches`, `install`/`activate`/`fetch` events). In the background, `fetch()` is rewritten to go through a native HTTP path that bypasses CORS. It is not a real service worker.
:::

## `declarativeNetRequest`

Supported actions: `block`, `allow`, `redirect`, `modifyHeaders`, `upgradeScheme`, `allowAllRequests`. Conditions support `urlFilter` / `regexFilter`, `resourceTypes`, `initiatorDomains`, `requestMethods`, and more. Prefer DNR over `webRequestBlocking` (which is unsupported).

## Resource URLs

`chrome.runtime.getURL(path)` returns `https://localhost/__ext__/<EXT_ID>/<path>`, intercepted by the resource interceptor and served from the extension's bundled files.

## Unsupported permissions (warning only)

`nativeMessaging`, `debugger`, `proxy`, `webRequestBlocking`, `management`, `devtools`, `bookmarks`, `identity`, `tabCapture`, and similar produce a warning and are not functional.

## Installing

- **Import** a `.crx` (CRX2/CRX3 parsed directly) or `.zip`.
- **Chrome Web Store** — the Browser Extensions tab searches the store live by keyword (or paste a store URL / extension ID) and installs on demand through the CRX pipeline. Live search requires a network that can reach Google.
