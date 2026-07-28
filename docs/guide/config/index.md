# App Configuration

When you edit an app's **common config** (or create a Web app), the editor is a scroll of capability cards. This page indexes them; the grouped reference pages go deeper.

::: info Core vs common
Non-web apps split editing into **Edit Core Config** (type-specific source/runtime) and **Edit Common Config** (the cards below). Web apps show these in a single editor. See [App Actions](/guide/app-actions).
:::

## The config cards

| Card | What it configures |
| --- | --- |
| **Basic info** | Name, target URL, icon. |
| **PWA analysis** | Detects PWA/offline-cache hints for the target. |
| **Activation code** | Gate the app behind activation codes (local or remote). See [Privacy](/guide/config/privacy#activation-gating). |
| **Hide browser toolbar** | Show/hide the in-app toolbar. |
| **Fullscreen mode** | Immersive fullscreen. |
| **Landscape mode** | Lock orientation. |
| **Keep screen on** | Prevent screen-off while running. |
| **Floating window** | Floating-window mode config. |
| **Long-press menu** | Long-press menu style. |
| **Splash screen** | Image/video splash, skip behavior, trim. See [Appearance](/guide/config/appearance#splash-screen). |
| **Background music** | Playlists, LRC lyrics, styling. See [Appearance](/guide/config/appearance#background-music). |
| **Announcement** | Launch/interval/no-network announcements. |
| **Translate** | In-page translation overlay (20 languages). |
| **Extension modules** | Attach JS/CSS modules, userscripts, MV3 extensions. See [Extensions](/extensions/). |
| **Ad blocking** | Hosts-rule ad blocker. See [Privacy](/guide/config/privacy#ad-blocking). |
| **DNS** | DNS-over-HTTPS providers and mode. See [Network](/guide/config/network#dns-over-https-doh). |
| **Disguise** | Browser fingerprint disguise (50+ vectors). See [Privacy](/guide/config/privacy#browser-fingerprint-disguise). |
| **Device disguise** | Device-level disguise. |
| **Auto start** | Boot auto-start / scheduled launch. |
| **Forced run** | Forced-run behavior (demonstration; use with consent). |
| **Device actions** | Device action hooks. |
| **Browser advanced** | Advanced WebView/browser toggles. See [Network](/guide/config/network). |
| **Special settings** | Miscellaneous advanced toggles. |
| **Export & permissions** *(drawer)* | Package name, version, signing, runtime permissions. See [App Actions](/guide/app-actions#build-apk). |

## Grouped reference

- [Network & Anti-Censorship](/guide/config/network) — engines, DoH, proxies, TLS fingerprint, ECH, CORS.
- [Privacy & Hardening](/guide/config/privacy) — fingerprint disguise, ad blocking, encryption, activation.
- [Appearance](/guide/config/appearance) — splash, BGM, toolbar, status bar, themes.
- [Local Server Runtimes](/guide/config/runtimes) — Node/PHP/Python/Go/WordPress details.
