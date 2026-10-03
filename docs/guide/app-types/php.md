# PHP

Runs a PHP project on an on-device PHP server; the WebView points at the local port.

## When to use

Small PHP apps, admin tools, and demos — including custom PHP frameworks.

## Runtime

- **Version** — PHP 8.4 (from `pmmp/PHP-Binaries`), downloaded once on first use.
- **Composer** — 2.10.x available for dependency management.
- **Extensions** — custom native extensions (`zend_extension`, `.so`) supported.
- Managed in the [Linux Environment](/guide/more-features/linux-environment) and [Runtime Management](/guide/more-features/runtime-management) screens.

## Core config

Backed by `PhpAppConfig`.

### Project

- **Project** (`projectId`/`projectName`) — the PHP source to serve.
- **Framework** (`framework`) — detected framework, if any.
- **Document root** (`documentRoot`) — the web root directory.
- **Entry file** (`entryFile`) — defaults to `index.php`.

### Server

- **Port** (`phpPort`) — allocated through the [Port Manager](/guide/more-features/port-manager).
- **Environment variables** (`envVars`) — key/value pairs passed to the process.

### Dependencies & extensions

- **Composer** (`hasComposerJson`) — whether a `composer.json` is present (install via Composer 2.10.x).
- **PHP extensions** (`phpExtensions`) — toggle built-in extensions.
- **Custom native extensions** (`customPhpExtensions`) — add `.so` extensions, each `EXTENSION` or `ZEND_EXTENSION`, with load order.

## Notes

- WordPress runs on this same PHP runtime — see [WordPress](/guide/app-types/wordpress).
- DNS and outbound HTTP for the packed PHP binary go through the local DNS bridge.
