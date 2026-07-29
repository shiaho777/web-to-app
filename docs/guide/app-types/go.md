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

## Core config

Backed by `GoAppConfig`.

### Project

- **Project** (`projectId`/`projectName`) — the Go module.
- **Framework** (`framework`) — detected framework, if any.

### Build

- **Binary name** (`binaryName`) — the output binary.
- **Target architecture** (`targetArch`) — e.g. `arm64`.
- `vendor/` enables offline builds.

### Server

- **Port** (`serverPort`) — allocated through the [Port Manager](/guide/more-features/port-manager).
- **Static directory** (`staticDir`) — directory to serve statically.
- **Environment variables** (`envVars`) — key/value pairs passed to the process.

## Notes

DNS and CA trust for the Go toolchain go through the same local JVM bridge used by PHP.
