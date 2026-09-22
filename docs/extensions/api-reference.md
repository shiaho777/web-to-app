# API Reference

A consolidated reference for the plugin APIs. Status legend:

- ✅ **Functional** — works as expected.
- 🟡 **Partial** — works with caveats (see notes).
- ⬜ **Stub** — present but returns defaults / no-op.

## HCJ `hcj.*` (page side)

| API | Status | Notes |
| --- | --- | --- |
| `hcj.config.get` / `set` / `remove` / `all` | ✅ | Persistent KV, gated by `STORAGE` permission |
| `hcj.fetch` | ✅ | Cross-origin via OkHttp, gated by `FETCH`; returns a Promise. `{method, headers, body}` supported; `responseType: "base64"` returns the body base64-encoded (`encoding` field tells which) for binary payloads |
| `hcj.notify` | ✅ | Android notification, gated by `NOTIFY` |
| `hcj.toast` | ✅ | In-page floating toast — use for action feedback; `hcj.notify` is for rare real notifications |
| `hcj.badge` | ✅ | Toolbar badge, gated by `BADGE` |
| `hcj.addStyle` | ✅ | Injects page CSS as `<style id="hcj-css-<id>">`, idempotent per plugin per document |
| `hcj.panel.open` / `close` / `send` / `onMessage` | ✅ | Hosted in the user-chosen panel surface |
| `hcj.on('action')` / `hcj.emit` | ✅ | Plugin entry tap / custom events |
| `hcj.on('navigate')` | ✅ | Fired on every URL change including SPA `pushState` — payload `{url}`; re-arm page hooks there |
| `hcj.id` / `hcj.manifest` / `hcj.lang` | ✅ | |

## `hcjPanel.*` (panel side)

| API | Status | Notes |
| --- | --- | --- |
| `hcjPanel.config.*` | ✅ | Same KV store as the page side |
| `hcjPanel.send` / `hcjPanel.onMessage` / `hcjPanel.close` | ✅ | Panel ↔ page channel and dismissal |
| `hcjPanel.toast` | ✅ | Floating toast inside the panel surface |

## Userscript `GM_*`

| API | Status | Notes |
| --- | --- | --- |
| `GM_getValue` / `GM_setValue` / `GM_deleteValue` / `GM_listValues` | ✅ | SharedPreferences-backed, per-script namespace |
| `GM_xmlhttpRequest` | ✅ | Via OkHttp; full callback set |
| `GM_addStyle` | ✅ | |
| `GM_setClipboard` | ✅ | |
| `GM_openInTab` | ✅ | |
| `GM_log` | ✅ | |
| `GM_getResourceText` / `GM_getResourceURL` | ✅ | |
| `GM_registerMenuCommand` / `GM_unregisterMenuCommand` | ✅ | Floating-window menu |
| `GM_openScriptWindow` / `GM_updateScriptWindow` / `GM_closeScriptWindow` | ✅ | WebToApp-specific |
| `GM_info` | ✅ | |
| `unsafeWindow` | ✅ | Equals `window` (no sandbox) |
| `GM_notification` | 🟡 | Logs only; no system notification |
| `@grant` gating | ⬜ | All APIs exposed regardless of declared grants |
| `GM.*` (Promise) | ✅ | Mirrors all of the above |

## Chrome `chrome.*`

| Namespace | Status | Notes |
| --- | --- | --- |
| `runtime` | ✅ | Messaging, lifecycle, `getURL`, `getManifest` |
| `storage` | ✅ | `local`/`sync`/`session`; `managed` no-op |
| `tabs` | ✅ | Core CRUD + events |
| `scripting` | ✅ | Dynamic content script registration |
| `cookies` | ✅ | |
| `alarms` | ✅ | |
| `declarativeNetRequest` | ✅ | block/allow/redirect/modifyHeaders/upgradeScheme |
| `downloads` | 🟡 | `download` functional |
| `webRequest` | 🟡 | `onBeforeRequest` filtering via native bridge |
| `i18n` / `notifications` / `permissions` / `contextMenus` / `commands` | ✅ | |
| `webNavigation` / `extension` / `idle` / `tabGroups` | 🟡 | |
| `action` / `sidePanel` | 🟡 | |
| `windows` / `management` / `topSites` / `fontSettings` | ⬜ | Stubs |
| `identity` / `history` / `proxy` / `privacy` | ⬜ | Stubs |
| `system.cpu/memory/display/storage` | ⬜ | Stubs |
| `bookmarks` / `offscreen` / `tts` | ⬜ | Stubs |
| `browserAction` / `pageAction` (MV2) | 🟡 | Compatibility aliases |

::: tip This table is a living document
The functional/stub status is being verified API-by-API. When in doubt, test against the current build.
:::
