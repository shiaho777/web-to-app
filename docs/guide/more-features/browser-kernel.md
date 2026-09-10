# Browser Kernel

Manage the browser engines available to your apps. Open it from [⋮ → Browser Kernel](/guide/main-screen/more).

## Features

- **Current WebView info** — inspect the system WebView version on this device.
- **Embedded engine** — download, manage, and delete the optional GeckoView (Firefox) runtime. Shows download size and progress; the heavy native artifacts are fetched on first use.
- **Change WebView provider** — switch the system WebView provider (with developer-options steps guidance).
- **Engine descriptions** — reference info for Chrome, Edge, Brave, Firefox, and Via.

## Notes

- Per-app engine selection happens in the [Build APK](/guide/app-actions/build-apk) dialog; this screen manages the engines themselves.
- [ECH](/guide/config/network) works on both engines; GeckoView is one option, not a requirement.

## GeckoView (Firefox engine) feature support

GeckoView is a complete second engine independent of the system WebView, not a WebView skin: its network-layer capabilities are implemented natively by the engine, while a set of features that rely on WebView-only mechanisms (request interception, `addJavascriptInterface`, JS injection) are unavailable on GeckoView. Read this section before picking a kernel in the Build APK dialog.

### Fully supported (native implementations, on par with the system WebView)

- **ECH (encrypted SNI)** — available on both engines; GeckoView implements it natively via TRR + its ECH prefs, the system WebView via the MITM bridge's Chromium upstream (auto-downloaded on first use, embedded in exported APKs)
- DoH, static / PAC / SOCKS5 proxy, anti-capture (force direct connection, ignore system proxies)
- UA modes and custom User-Agent, desktop mode
- JavaScript toggle, autoplay policy, viewport mode (fit screen / desktop), clear browsing data on launch, download toggle
- HTTP downloads, fullscreen video, file upload (incl. camera capture), automatic crash recovery
- Camera / microphone and geolocation permission policies (deny all / remember per host)
- mTLS client certificates, HTTP Basic / proxy auth dialogs
- JS dialogs (alert / confirm / prompt)
- Page-load error UI, back / forward history navigation, auto-refresh, find-in-page, console script execution
- Media session / lock-screen playback controls (native Gecko implementation)

### Not available yet (choose the system WebView kernel if you need these)

- **[Ad blocking](/guide/app-actions/edit-common-config/ad-blocking)** — neither network-rule filtering nor cosmetic hiding takes effect
- **[Userscripts](/extensions/userscript) (GM_\* APIs) and [Chrome MV3 extensions](/extensions/chrome-mv3)** (incl. declarativeNetRequest / webRequest rules)
- **The full [NativeBridge](/extensions/api-reference) JS API** — clipboard, notifications, vibration, printing, sharing, screen wake, orientation, Google sign-in, etc.; on GeckoView only the CORS-bypass / private-network `httpRequest` entry point is available
- **JS-injection features** — browser fingerprint disguise, [device disguise](/guide/app-actions/edit-common-config/device-disguise), kernel disguise & flavors, [page translation](/guide/app-actions/edit-common-config/translate), audio unlock, clipboard / notification / orientation polyfills, Cloudflare compatibility helpers, custom injected scripts, popup blocking, scroll-position memory, image repair, hide-link-preview
- **PWA offline (service-worker injection), static asset packs, encrypted asset loading** — therefore **HTML / [front-end offline-package](/guide/app-types/frontend) apps with resource encryption enabled must use the system WebView kernel**
- blob / data: download interception
- The [long-press menu](/guide/app-actions/edit-common-config/long-press-menu) (save image/video, copy link, etc.)
- Status-bar auto color sampling (follow page-top color; solid-color / theme modes are unaffected)
- Page zoom settings (zoom percent / initial scale / text zoom; GeckoView keeps only its built-in pinch zoom)
- Follow-system dark mode
- Cookie policy (GeckoView always accepts all cookies)
- Hosts mapping (domain → IP)
- Console message capture and eval result return (scripts do run, but the panel receives no messages or return values)
- New-window policy (GeckoView popups always load in the current window)
- Proxy username / password authentication
- WebView state save / restore (session restore after process death)

### Unsupported by design (trade-offs, not defects)

- **TLS fingerprint spoofing (MITM bridge) and forced HTTP/3 (QUIC)** — both ride the MITM bridge, which is a system-WebView-kernel feature; GeckoView natively presents a genuine Firefox TLS / JA3 fingerprint, which is the strongest "disguise" in itself, and the MITM bridge would terminate TLS and directly conflict with GeckoView's native ECH. On GeckoView, the target site sees a real Firefox.
- **"Proceed anyway" on SSL certificate errors** — GeckoView cannot ignore certificate errors, and editor-imported custom CAs are not trusted by GeckoView (only user CAs installed into the Android system, via the trust-user-certificates toggle).
- DOM storage / database cannot be disabled (always on in GeckoView).
- Floating-window mode always uses the system WebView regardless of the per-app engine choice.

### Choosing an engine

- Apps that rely on ad blocking, userscripts, Chrome extensions, the NativeBridge API, or encrypted resource packaging → choose the **system WebView**.
- Apps that want a genuine Firefox fingerprint or Gecko engine behavior → choose **GeckoView** and accept the gaps listed above.
- ECH / SNI encryption works on both engines; if you also need TLS fingerprint spoofing or forced HTTP/3, choose the **system WebView**.
- Download the GeckoView runtime on this page first: the Build APK dialog will not offer the engine until it is installed, and host previews silently fall back to the system WebView when the runtime is missing.
