# Node.js

Runs a Node.js project in a dedicated on-device Node server; the WebView points at the local port.

## When to use

Express/Fastify/Koa apps, APIs, and server-side demos.

## Runtime

- **Version** — Node.js 18.20.x.
- **Process model** — runs in a dedicated `:nodejs` OS process via a native `node_launcher` wrapper loading `libnode.so`, so the V8 lifecycle is isolated from the host.
- **Addons** — custom native `.node` extensions supported.
- Managed in the [Linux Environment](/guide/more-features/linux-environment) and [Runtime Management](/guide/more-features/runtime-management) screens.

## Export requirements

The exported APK embeds:

- `libnode_bridge.so`
- `libnode.so` (16KB-aligned for Android 15+)
- `libc++_shared.so`

Missing any of these causes `loadNode` / `loadJniBridge` failure at runtime.

The bridge/launcher/C++ libs ship inside the shell template for all four ABIs. `libnode.so`
is downloaded content and is embedded once per selected APK architecture — the upstream
nodejs-mobile zip provides `arm64-v8a`, `armeabi-v7a` and `x86_64` (no 32-bit `x86`). If a
selected ABI is missing from the local runtime cache, export re-downloads the Node.js
runtime once and extracts every ABI; a still-missing ABI fails the build rather than
shipping an APK that would report "libnode.so not installed" on that architecture (e.g.
x86_64 emulators without ARM translation).

## Core config

Backed by `NodeJsConfig`.

### Project

- **Project** (`projectId`/`projectName`, `sourceProjectPath`) — the Node source.
- **Framework** (`framework`) — detected framework, if any.
- **Node version** (`nodeVersion`).
- **Has node_modules** (`hasNodeModules`) — whether dependencies are bundled.

### Build mode

- **Build mode** (`buildMode`):
  - `STATIC` — static site
  - `SSR` — server-side rendering
  - `API_BACKEND` — API backend (default)
  - `FULLSTACK` — full-stack

### Server

- **Entry file** (`entryFile`) — defaults to `index.js`.
- **Port** (`serverPort`) — allocated through the [Port Manager](/guide/more-features/port-manager).
- **Environment variables** (`envVars`) — key/value pairs passed to the process.

### Native addons

- **Custom Node extensions** (`customNodeExtensions`) — add `.node` addons, each with load order.
