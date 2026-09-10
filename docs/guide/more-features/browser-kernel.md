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
