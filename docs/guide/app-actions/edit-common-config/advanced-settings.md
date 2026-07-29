# Advanced Settings

A broad set of browser behavior toggles. This card collects the advanced `WebViewConfig` options.

**Where:** the **Advanced settings** card in the [Edit Common Config](/guide/app-actions/edit-common-config/) editor.

## User agent & rendering

- **User agent mode** — system default or a custom UA string (`userAgentMode`, `customUserAgent`).
- **Desktop mode** — request the desktop site (`desktopMode`).
- **Zoom** — enable zoom and initial scale (`zoomEnabled`, `initialScale`).
- **Viewport mode** — default or a custom viewport width (`viewportMode`, `customViewportWidth`).

## Navigation & refresh

- **Swipe refresh** — pull-to-refresh (`swipeRefreshEnabled`).
- **Auto refresh** — periodic reload with interval and countdown (`autoRefreshEnabled`, `autoRefreshIntervalSec`).
- **New-window behavior** — how popups/new windows open (`newWindowBehavior`: same window, external, popup, …).
- **Popup blocker** — block popups (`popupBlockerEnabled`).

## Downloads

- **Downloads** — enable downloads and choose location (`downloadEnabled`, `downloadLocationMode`: system / app-private / custom SAF dir).

## Networking & privacy

- **Proxy** — static HTTP/HTTPS/SOCKS5 or PAC, with auth and bypass rules (`proxyMode`, `proxyHost`, `pacUrl`, …).
- **TLS fingerprint** — impersonate a browser JA3 profile (`tlsFingerprintEnabled`, `tlsFingerprintTemplate` e.g. `CHROME_131`).
- **CORS bypass** — bypass CORS for cross-origin SPAs (`enableCorsBypass`).
- **Mixed content** — allow/compatibility mode (`allowMixedContent`, `mixedContentMode`).
- **Private network bridge** — bridge private-network requests (`enablePrivateNetworkBridge`, `privateNetworkScope`).
- **Hosts mappings** — host → IP overrides (`hostsMappingEnabled`, `hostsMappings`).
- **Cookies** — third-party cookies and persistence (`acceptThirdPartyCookies`, `thirdPartyCookieMode`).
- **Geolocation** — enable with accuracy and policy (`geolocationEnabled`, `geolocationAccuracy`, `geolocationPolicy`).

## Kernel & status bar

- **Kernel disguise** — present a different browser kernel flavor (`enableKernelDisguise`, `kernelFlavor`, `kernelDisguiseLevel`).
- **Cloudflare compatibility** — compatibility mode for Cloudflare challenges (`enableCloudflareCompat`, `cloudflareCompatMode`).
- **Status bar** — color mode (`THEME`/`PAGE_TOP`/`TRANSPARENT`/`CUSTOM`), custom color, dark icons, background (color/image), for light and dark separately.
- **Failover** — mirror URLs with triggers and timeout (`failoverEnabled`, `failoverUrls`, `failoverTimeoutSeconds`).

## Notes

- DNS is configured under [Custom DNS](/guide/app-actions/edit-common-config/custom-dns).
- The most exotic toggles (polyfills, native bridge, print bridge, etc.) live under [Special Settings](/guide/app-actions/edit-common-config/special-settings).
