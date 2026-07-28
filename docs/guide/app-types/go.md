# Go

Builds and runs a Go project on-device; the WebView points at the local port.

## When to use

Gin/Echo/Fiber services, static file serving, and compiled tools.

## Runtime

- **Toolchain** — official Go 1.26 Linux arm64 (`.tar.gz` from `dl.google.com`, USTC mirror for CN).
- **On-device** — `go build` / `go mod` / `go run`, `vendor/` offline builds, and static serving via the native `go_exec_loader` wrapper.
- Managed in the [Linux Environment](/guide/more-features/linux-environment) and [Runtime Management](/guide/more-features/runtime-management) screens.

## Export requirements

The exported APK embeds `libgo_exec_loader.so`.

## Key configuration

- **Project** — the Go module.
- **Build / run** — build on-device or run directly; `vendor/` enables offline builds.
- **Port** — allocated through the [Port Manager](/guide/more-features/port-manager).

## Notes

DNS and CA trust for the Go toolchain go through the same local JVM bridge used by PHP.
