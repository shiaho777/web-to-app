# Network & Anti-Censorship

A thematic index of the networking capabilities. Each is configured by a card in [Edit Common Config](/guide/app-actions/edit-common-config/) — follow the links for full detail.

## DNS

- [Custom DNS](/guide/app-actions/edit-common-config/custom-dns) — DNS mode, DoH providers, custom endpoints.

## Proxies, TLS & CORS

These live under [Advanced Settings](/guide/app-actions/edit-common-config/advanced-settings):

- **Proxy** — static HTTP/HTTPS/SOCKS5 or PAC, with auth and bypass rules.
- **TLS fingerprint** — impersonate a browser JA3 profile (e.g. `CHROME_131`).
- **CORS bypass** — for cross-origin SPAs.
- **Mixed content** and **private network bridge**.
- **Hosts mappings** — host → IP overrides.

## Failover

- **Failover mirrors** — automatic fallback URLs, under [Advanced Settings](/guide/app-actions/edit-common-config/advanced-settings).

## Browser engine

- The engine (System WebView / GeckoView) is chosen in [Custom DNS](/guide/app-actions/edit-common-config/custom-dns) or [APK Export Config](/guide/app-actions/edit-common-config/apk-export); manage engines in [Browser Kernel](/guide/more-features/browser-kernel).

## Runtimes

- Server-runtime DNS/proxy bridging is covered in [Local Server Runtimes](/guide/config/runtimes).
