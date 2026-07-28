# More · Browser

The browser group in the **⋮** menu.

## Browser Kernel

Manage the browser engines available to your apps.

- **Embedded engine** — download and manage the optional GeckoView (Firefox) runtime, used for ECH / SNI encryption. The heavy native artifacts are fetched on first use.
- **Current WebView info** — inspect the system WebView version on this device.

Per-app engine selection happens in the [Build APK](/guide/app-actions#build-apk) dialog; this screen manages the engines themselves.

## Hosts Ad Blocking

A hosts-rule ad blocker with cosmetic filtering.

- **Built-in lists** — 20 community filter lists (EasyList, uBlock Origin, AdGuard, AdAway, plus 8 language-specific lists).
- **Per-source control** — enable, disable, or delete each subscription.
- **Custom rules** — add your own subscription URLs or import rules from a file.
- **Cosmetic filtering** — MutationObserver-based element hiding.

Ad blocking is wired for both preview and export: the host blocker serves preview, and the compiled rule set ships inside the generated APK (configure it per app under [App Configuration](/guide/config/privacy)).
