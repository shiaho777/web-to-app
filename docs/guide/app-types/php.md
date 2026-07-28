# PHP

Runs a PHP project on an on-device PHP server; the WebView points at the local port.

## When to use

Small PHP apps, admin tools, and demos — including custom PHP frameworks.

## Runtime

- **Version** — PHP 8.4 (from `pmmp/PHP-Binaries`), downloaded once on first use.
- **Composer** — 2.10.x available for dependency management.
- **Extensions** — custom native extensions (`zend_extension`, `.so`) supported.
- Managed in the [Linux Environment](/guide/more-features/linux-environment) and [Runtime Management](/guide/more-features/runtime-management) screens.

## Key configuration

- **Project** — the PHP source to serve.
- **Start command / entry** — how the server is launched.
- **Port** — allocated through the [Port Manager](/guide/more-features/port-manager).

## Notes

- WordPress runs on this same PHP runtime — see [WordPress](/guide/app-types/wordpress).
- DNS and outbound HTTP for the packed PHP binary go through the local DNS bridge.
