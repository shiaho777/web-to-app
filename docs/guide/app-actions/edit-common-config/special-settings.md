# Special Settings

Compatibility polyfills, bridges, and other specialized toggles. This card collects the specialized `WebViewConfig` options.

**Where:** the **Special settings** card in the [Edit Common Config](/guide/app-actions/edit-common-config/) editor.

## Polyfills & bridges

- **Clipboard polyfill** — `enableClipboardPolyfill`.
- **Notification polyfill** — Web Notification support (`enableNotificationPolyfill`).
- **Orientation polyfill** — `enableOrientationPolyfill`.
- **Compat polyfills** — a bundle of compatibility shims (`enableCompatPolyfills`).
- **Native bridge** — expose a native bridge with capability gates (`enableNativeBridge`, `nativeBridgeCapabilities`).
- **Print bridge** — intercept `window.print()` and PDF output to the Android print framework (`enablePrintBridge`).
- **Media Session bridge** — bridge web media to the system media notification and lock-screen controls, including Bluetooth headsets and Android Auto (`enableMediaSession`).
- **Share bridge** — `enableShareBridge`.
- **Zoom polyfill** — `enableZoomPolyfill`.

## Media & content

- **Media autoplay** — with scope (`mediaAutoplayEnabled`, `mediaAutoplayScope`: video-only, …).
- **Image repair** — fix broken images (`enableImageRepair`).
- **Scroll memory** — remember scroll position (`enableScrollMemory`).
- **Back-state preservation** — `enableBackStatePreservation`.
- **Blob download interception** — with scope and size threshold (`enableBlobDownloadInterception`, `blobInterceptThresholdMb`).

## JavaScript & windows

- **JS can open windows** — with policy (`javaScriptCanOpenWindows`, `jsOpenWindowsPolicy`).
- **Prime user activation** — synthesize a user gesture, with mode and timing (`primeUserActivation`, `primeUserActivationMode`, `primeUserActivationTiming`).
- **Base64 deep links** — decode base64 deep links, gesture-only or always (`decodeBase64DeepLinks`, `decodeBase64Mode`).

## App-to-app returns

- **App return** — declare the channels other apps use to hand control **back**, so an OAuth / SSO login (QQ, Weibo, …) can return to this app once you have authorised inside the provider app (`enableAppReturn`, on by default). Without it the page can still *open* the provider app — any non-http scheme is handed to the system — but the callback has nowhere to return to, and the system reports that no app can handle the link.
  - Only **return** channels are declared, never launcher schemes such as `weixin` or `alipays`: claiming one of those would make this app a candidate for the real provider's own links.
  - **Custom return schemes** (`customAppReturnSchemes`) — some providers bind the callback scheme to the app id you registered with them (WeChat's `wx<appid>`, for instance), so it cannot ship as a default. Enter one per line or comma-separated.

### How each platform returns

Declaring a provider's *launcher* scheme (`weixin`, `alipays`, `taobao`, …) does not help and can hurt: those schemes are how the provider is **opened**, so claiming one makes this app a candidate for the provider's own links and the user gets an app chooser mid-payment. App return declares return channels only.

| Platform | How it hands control back | Covered by |
| --- | --- | --- |
| QQ (web OAuth) | `mqqopensdkapi://browser?url=<redirect_uri>` — app-agnostic | Built in, on by default |
| WeChat (SSO / H5 pay) | Returns to the scheme you registered on WeChat Open Platform (`wx<AppID>`); H5 pay additionally requires the merchant to whitelist it | **Custom return schemes** — register an app, then enter `wx<AppID>` |
| Alipay (H5 pay / auth) | Returns to a scheme **the caller supplies**; browsers and apps pass their own, and the page's value has to be rewritten to reach this app | Not automatic — see below |
| Weibo (SSO) | Returns to the scheme matching your AppKey (`wb<AppKey>`) | **Custom return schemes** |
| Taobao / Tmall / JD / Douyin / Bilibili | No app-agnostic return channel; the page resumes in place, or via an http(s) callback on its own host | The site's own https callback, which the export already declares |

For platforms that return to an **https** callback on the wrapped site's own host (Alipay H5, WeChat web authorisation), the export already declares that host, so the callback can come back to the app — Android may ask which app to use when a browser also matches.

## Security & misc

- **Cross-origin isolation** — `enableCrossOriginIsolation`.
- **Anti-capture** — block screen capture (`antiCapture`).
- **Hide status bar in video fullscreen** — force-hide the status bar while a web video plays in HTML5 fullscreen, restoring it on exit (`hideStatusBarInVideoFullscreen`, on by default). Overrides the [Fullscreen Mode](/guide/app-actions/edit-common-config/fullscreen) "show status bar in fullscreen" option while the video holds the screen.
- **File access from file URLs** — `allowFileAccessFromFileURLs`, `allowUniversalAccessFromFileURLs`.
- **Error page** — custom error page config (`errorPageConfig`).
- **Performance optimization** — `performanceOptimization`.
- **PWA offline** — offline cache strategy (`pwaOfflineEnabled`, `pwaOfflineStrategy`).
- **Floating back button** — `showFloatingBackButton`.
- **Keyboard adjust mode** — `keyboardAdjustMode` (resize, …). On Android 10 and below, the RESIZE mode uses the classic window-resize path (the window is not edge-to-edge), so the keyboard resizes content reliably.
- **Fullscreen video orientation** — how fullscreen video orients (`fullscreenVideoOrientation`), e.g. auto sensor landscape.
- **Hide URL preview** — `hideUrlPreview`.

## Notes

- These are power-user toggles; most apps leave them at defaults.
- **Defaults:** the safe experience features default to **ON** for new apps — clipboard / orientation / compat polyfills, media session, share, zoom and print bridges, image repair, scroll memory, back-state preservation, blob interception, JS windows, user-activation priming, base64 deep links, app return, private-network bridge, and the native bridge. Deliberately **OFF**: notification polyfill (it would prompt `POST_NOTIFICATIONS` on every export), geolocation, mixed content, third-party cookies, autoplay, and popup blocking.
