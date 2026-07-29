# Custom DNS

Overrides DNS resolution for the app, including DNS-over-HTTPS.

**Where:** the **Custom DNS** card in the [Edit Common Config](/guide/app-actions/edit-common-config/) editor.

## Options

- **DNS mode** (`dnsMode`) — `SYSTEM` (default) or a DoH provider / custom endpoint.
- **DoH providers** — Cloudflare, Google, AdGuard, NextDNS, CleanBrowsing, Quad9, Mullvad, or a custom endpoint.
- **DNS config** — provider-specific settings (`dnsConfig`).
- **Engine type** — the browser engine can also be chosen here (`engineType`; System WebView or GeckoView).

## Notes

- DoH in strict mode routes all DNS over HTTPS; automatic mode falls back as needed.
- Hosts mappings (host → IP overrides) are configured under [Advanced Settings](/guide/app-actions/edit-common-config/advanced-settings).
