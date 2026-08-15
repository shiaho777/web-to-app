# Local Server Runtimes

WebToApp can fork+exec real server runtimes as native binaries straight from app storage — like Termux, packaged into an installable APK. The WebView in the generated app points at a local port served by the runtime.

## Supported runtimes

| Runtime | Version | Notes |
| --- | --- | --- |
| **Node.js** | 18.20.x | Runs in a dedicated `:nodejs` OS process via a native `node_launcher` wrapper loading `libnode.so`. Custom native `.node` extensions supported. |
| **PHP** | 8.4 | From `pmmp/PHP-Binaries`, downloaded once on first use. Composer 2.10.x. Custom native extensions (`zend_extension`, `.so`). |
| **Python** | 3.14 | Flask, Django, FastAPI (uvicorn), Tornado, built-in HTTP server. pip deps resolve into `.pypackages`. Versioned binary names. |
| **Go** | 1.26 | Official Linux arm64 toolchain (USTC mirror for CN). On-device `go build` / `go mod` / `go run`, `vendor/` offline builds, static serving via `go_exec_loader`. |
| **WordPress** | 7.x | Over local PHP + SQLite (`sqlite-database-integration`). Theme and plugin import. |

A **Linux Environment** screen manages toolchains and dependencies for Node, PHP, and Python.

## How runtimes are coordinated

- **Port Manager** — coordinates runtime ports across generated apps via broadcast receivers, with conflict policies: `REASSIGN`, `AUTO_KILL`, or `ALERT`. Runtimes allocate through the Port Manager and clean up on stop.
- **Local DNS bridge proxy** — an HTTP CONNECT proxy in the Android JVM gives runtimes working DNS resolution and outbound HTTP where the musl/packed binary can't reach the system resolver.
- **Downloads** — large runtime downloads use an extended-timeout download client, not the default short-lived one.

## Export requirements

When you export a runtime app, the required native libraries are embedded into the APK:

- **Node.js** → `libnode_bridge.so` + `libnode.so` (16KB-aligned) + `libc++_shared.so`
- **Go** → `libgo_exec_loader.so`

::: info 16KB page alignment
`libnode.so` and other large ELF natives are 16KB-aligned for Android 15+ devices. The native launcher enables 16KB app-compat before `dlopen`.
:::

::: info Host-side preview vs generated apps
Host preview of Node.js apps goes through the same JNI launcher and works at any `targetSdk`. The exec-based runtimes (PHP / Python / Go / WordPress) can only be previewed when the host build permits exec from app storage — on the `targetSdk` 35 host, SELinux W^X blocks it and those previews degrade with an explicit message. Generated APKs always ship `targetSdk` 28 and are never affected.

---

::: tip Per-runtime setup guides coming soon
Detailed first-run setup, dependency installation, and troubleshooting for each runtime are being written.
:::
