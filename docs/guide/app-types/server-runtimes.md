# Server Runtime Types

These types fork+exec a real server runtime as a native binary on-device, then point the WebView at a local port. Think Termux, packaged into an installable APK. Runtimes are downloaded on first use and managed in the [Linux Environment](/guide/more-features/dev-tools#linux-environment) and [Runtime Deps](/guide/more-features/dev-tools#runtime-management) screens.

## PHP

- **Version** — PHP 8.4 (from `pmmp/PHP-Binaries`), downloaded once on first use.
- **Composer** — 2.10.x available.
- **Extensions** — custom native extensions (`zend_extension`, `.so`) supported.
- **Good for** — small PHP apps, admin tools, demos.

## WordPress

- **Stack** — WordPress 7.x over local PHP + SQLite (`sqlite-database-integration`).
- **Import** — theme and plugin import supported.
- **Good for** — portable sites, theme/plugin demos, content packages.

## Node.js

- **Version** — Node.js 18.20.x.
- **Process model** — runs in a dedicated `:nodejs` OS process via a native `node_launcher` wrapper loading `libnode.so`, so the V8 lifecycle is isolated from the host.
- **Extensions** — custom native `.node` addons supported.
- **Export embeds** — `libnode_bridge.so` + `libnode.so` (16KB-aligned) + `libc++_shared.so`.
- **Good for** — Express/Fastify/Koa apps, APIs, server-side demos.

## Python

- **Version** — Python 3.14.
- **Frameworks** — Flask, Django, FastAPI (uvicorn), Tornado, or the built-in HTTP server.
- **Dependencies** — pip resolves into `.pypackages`; custom native extensions supported.
- **Versioning** — binary names are versioned so future bumps don't hard-code paths.
- **Good for** — Flask/Django/FastAPI apps, data demos.

## Go

- **Toolchain** — official Go 1.26 Linux arm64 (`.tar.gz` from `dl.google.com`, USTC mirror for CN).
- **On-device** — `go build` / `go mod` / `go run`, `vendor/` offline builds, static serving via the native `go_exec_loader` wrapper.
- **Export embeds** — `libgo_exec_loader.so`.
- **Good for** — Gin/Echo/Fiber services, static serving, compiled tools.

## Shared runtime infrastructure

- **Port Manager** — allocates runtime ports with a conflict policy (`REASSIGN` / `AUTO_KILL` / `ALERT`) and cleans up on stop. See [Port Manager](/guide/more-features/dev-tools#port-manager).
- **Local DNS bridge** — an HTTP CONNECT proxy in the Android JVM gives runtimes working DNS and outbound HTTP where the packed binary can't reach the system resolver.
- **Downloads** — large runtime downloads use an extended-timeout download client.

::: info Why targetSdk 28?
Generated apps keep a low `targetSdk` (28) precisely so they can fork+exec these native runtimes from app storage. The AAB exporter separately rewrites `targetSdk` for Play distribution. See [Build & Export](/guide/app-actions#build-apk).
:::
