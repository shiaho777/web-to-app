# Advanced Settings

A broad set of browser behavior toggles. This card collects the advanced `WebViewConfig` options.

**Where:** the **Advanced settings** card in the [Edit Common Config](/guide/app-actions/edit-common-config/) editor.

## User agent & rendering

- **User agent mode** — system default or a custom UA string (`userAgentMode`, `customUserAgent`).
- **Desktop mode** — request the desktop site (`desktopMode`).
- **Zoom** — enable pinch zoom (`zoomEnabled`).
- **Page zoom** — build-time per-app whole-page zoom as a percentage, chosen from Chrome-style presets (50%–150%) or entered freely (`pageZoomPercent`, default 100). Applied via `setInitialScale` on every run, including cold starts — it scales text and layout/images/canvas together (unlike `textZoom`, which only scales glyphs), so no runtime toolbar needed. A stored legacy value of `0` is treated as 100.
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
- **Failover** — mirror URLs with triggers and timeout (`failoverEnabled`, `failoverUrls`, `failoverTimeoutSeconds`).

The status bar color/appearance configuration (color mode `THEME`/`PAGE_TOP`/`TRANSPARENT`/`CUSTOM`, custom color, dark icons, background color/image, light and dark separately) lives in the expandable section of the [Fullscreen Mode](/guide/app-actions/edit-common-config/fullscreen) card.

## Notes

- DNS is configured under [Custom DNS](/guide/app-actions/edit-common-config/custom-dns).
- The most exotic toggles (polyfills, native bridge, print bridge, etc.) live under [Special Settings](/guide/app-actions/edit-common-config/special-settings).
