# More · Developer Tools

The developer-tools group in the **⋮** menu.

## Extension Modules

Manage the extensions that run inside your generated apps: built-in modules, custom JS/CSS modules, userscripts, and MV3 Chrome extensions.

- **List & toggle** — enable/disable installed extensions per app.
- **Editor** — create or edit a module (manifest, JS, CSS, config items, panel). See [JS Modules](/extensions/js-module).
- **Market** — browse the community module market. See [Publish to the Market](/extensions/publish).
- **Browser extensions** — search the Chrome Web Store live and install MV3 extensions. See [Chrome MV3](/extensions/chrome-mv3).
- **AI developer** — jump to [AI Coding](/guide/more-features/ai-tools#ai-coding) to generate an extension.

## App Modifier

Repackage an *installed* app: a shortcut disguise, or a real binary clone with manifest/resource patching and re-signing.

- Pick an installed app, then modify its icon, name, and package identity.
- The modified APK is patched at the binary level and re-signed.
- Intended for icon/name/package experiments and repackaging research — use only with appropriate rights.

## Linux Environment

Manages the on-device toolchains and dependencies used by the server-runtime app types (Node.js, PHP, Python) and Frontend builds.

- Install and update runtime toolchains.
- Manage shared dependencies.
- **Reset environment** to recover from a broken state.

Runtime types link here from their creation screens when setup is needed.

## Runtime Management

Downloads and manages the runtime binaries themselves:

- **PHP runtime**, **WordPress core**, **Node.js runtime**, **Python runtime**.
- Each is downloaded on first use (large downloads use an extended-timeout client) and cached for reuse across apps.

## Port Manager

Coordinates the local-server ports used by runtime apps across all your generated apps.

- See which ports are in use and by which runtime.
- Conflict policy — `REASSIGN`, `AUTO_KILL`, or `ALERT`.
- Scan for conflicts; runtimes allocate through the Port Manager and release on stop.
