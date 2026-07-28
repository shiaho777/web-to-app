# WordPress

Runs a WordPress site on-device over local PHP + SQLite — a portable CMS in an APK.

## When to use

Portable sites, theme/plugin demos, and content packages you want to ship as an app.

## Runtime

- **Stack** — WordPress 7.x over the on-device [PHP](/guide/app-types/php) runtime, with SQLite via `sqlite-database-integration` (no MySQL server needed).
- **Import** — theme and plugin import supported.
- Managed in the [Linux Environment](/guide/more-features/linux-environment) and [Runtime Management](/guide/more-features/runtime-management) screens.

## Key configuration

- **WordPress source** — the site/theme to package.
- **Themes & plugins** — import the ones you need.
- **Port** — allocated through the [Port Manager](/guide/more-features/port-manager).

## Notes

- Because it uses SQLite, WordPress runs without a separate database server.
- The WebView loads the locally-served WordPress instance.
