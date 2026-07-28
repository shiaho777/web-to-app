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

## Key configuration

- **Project** — the Node source.
- **Start command** — e.g. `node server.js`.
- **Port** — allocated through the [Port Manager](/guide/more-features/port-manager).
