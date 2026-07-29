# URL / Webpage

The target the WebView loads. This is the primary field for web-oriented app types.

**Where:** the **Basic info** card at the top of the [Edit Common Config](/guide/app-actions/edit-common-config/) editor.

## Options

- **Target URL** — the website to load (e.g. `https://example.com`).

## PWA analysis (Web type)

For `WEB` apps, a **PWA analysis** section can inspect the target and detect Progressive Web App / offline-cache hints, helping you decide on offline strategies.

## Notes

- For non-web types, the "target" is the type-specific source instead (a local folder, a project, media, etc.) — configured in [Edit Core Config](/guide/app-actions/edit-core-config).
- Failover mirror URLs (automatic fallback when the target is unreachable) are configured under [Advanced Settings](/guide/app-actions/edit-common-config/advanced-settings).
