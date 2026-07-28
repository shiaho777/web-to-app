# Network & Anti-Censorship

WebToApp ships a hardened network stack that goes well beyond a plain WebView. All settings are per-app.

## Browser engines

- **System WebView** — the default engine.
- **GeckoView (Firefox)** — an optional runtime downloaded on first use. Required for ECH. The GeckoView API classes come from a Gradle dependency; the heavy native artifacts (`.so` + `omni.ja`) are fetched on demand.
- **Kernel flavor disguise** — present as Chrome, Edge, Samsung Internet, Firefox, or Safari-style while keeping the real engine.

## DNS-over-HTTPS (DoH)

Choose from Cloudflare, Google, AdGuard, NextDNS, CleanBrowsing, Quad9, Mullvad, or a custom endpoint. Modes: strict or automatic.

## Proxies

Static HTTP/HTTPS/SOCKS5, PAC, authentication, bypass rules, and a local HTTP-to-SOCKS bridge. Proxies are per-app.

## TLS fingerprint spoofing

Impersonate Chrome 131 / Firefox 133 / Safari 18 JA3 profiles (or custom ciphers), served through a local TLS-MITM bridge so the outgoing ClientHello matches a real browser.

## Encrypted Client Hello (ECH)

Encrypt the SNI in the TLS handshake. **GeckoView only.** Toggling ECH auto-wires DoH + GeckoView.

## CORS bypass

On by default for static SPAs that call external APIs blocked by CORS. Same-origin traffic is left alone. CORS-only apps can use a lightweight `PrivateNetworkNativeBridgeAdapter` without the full Native Bridge surface.

## Failover

Automatic fallback to mirror URLs when the primary target is unreachable.

---

::: tip Detailed walkthroughs coming soon
Step-by-step configuration recipes for each feature above are being written. The capabilities listed here are all available in the editor today under the networking section.
:::
