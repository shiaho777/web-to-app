# HTML

Packages local HTML into the APK and serves it from local files — no remote URL required.

## When to use

Static builds and offline web apps where you already have the HTML/CSS/JS files.

## Core config

Backed by `HtmlConfig`.

### Source

- **Project directory** (`projectDir`) — the folder of HTML/CSS/JS, or a `.zip` extracted on import.
- **Files** (`files`) — the packaged files, typed as HTML/CSS/JS/image/font/other.

### Entry

- **Entry file** (`entryFile`) — defaults to `index.html`.

### Loading

- **Load mode** (`loadMode`) — `AUTO`, `FILE` (file scheme), or `LOCAL_HTTP` (local server).
- **Port** (`port`) — the local server port (for `LOCAL_HTTP`).
- **Port-conflict mode** (`portConflictMode`) — `AUTO_KILL` or `ALERT`.

### Capabilities

- **Enable JavaScript** (`enableJavaScript`).
- **Enable local storage** (`enableLocalStorage`).
- **Allow file access** (`allowFileAccess`) — required for pure file-based loads.

### Appearance

- **Background color** (`backgroundColor`).

## Notes

- The generated app gets `allowFileAccess` so pure file-based loads work offline.
- **HTML vs Frontend vs Offline Pack:**
  - **HTML** — you already have static files.
  - [Frontend](/guide/app-types/frontend) — you have a framework project whose build output you package.
  - [Offline Pack](/guide/app-types/offline-pack) — you start from a remote URL and scrape it down.
