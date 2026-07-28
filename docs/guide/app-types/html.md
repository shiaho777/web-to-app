# HTML

Packages local HTML into the APK and serves it from local files — no remote URL required.

## When to use

Static builds and offline web apps where you already have the HTML/CSS/JS files.

## Key configuration

- **Input** — a folder of HTML/CSS/JS, or a `.zip` that is extracted on import.
- **Entry file** — defaults to `index.html`.
- **Load mode** — how the content is served.
- **Port & port-conflict mode** — for the local server that serves the files.
- **JavaScript & local storage** — toggles for the WebView.

## Notes

- The generated app gets `allowFileAccess` so pure file-based loads work offline.
- **HTML vs Frontend vs Offline Pack:**
  - **HTML** — you already have static files.
  - [Frontend](/guide/app-types/frontend) — you have a framework project whose build output you package.
  - [Offline Pack](/guide/app-types/offline-pack) — you start from a remote URL and scrape it down.
