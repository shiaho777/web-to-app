# WordPress

Runs a WordPress site on-device over local PHP + SQLite — a portable CMS in an APK.

## When to use

Portable sites, theme/plugin demos, and content packages you want to ship as an app.

## Runtime

- **Stack** — WordPress 7.x over the on-device [PHP](/guide/app-types/php) runtime, with SQLite via `sqlite-database-integration` (no MySQL server needed).
- **Import** — theme and plugin import supported.
- Managed in the [Linux Environment](/guide/more-features/linux-environment) and [Runtime Management](/guide/more-features/runtime-management) screens.

## Core config

Backed by `WordPressConfig`.

### Site

- **Site title** (`siteTitle`).
- **Site language** (`siteLanguage`) — e.g. `zh_CN`.
- **Permalink structure** (`permalinkStructure`) — e.g. `/%postname%/`.

### Admin account

- **Admin user / email / password** (`adminUser`, `adminEmail`, `adminPassword`).

### Theme & plugins

- **Theme** (`themeName`) — the active theme.
- **Plugins** (`plugins`, `activePlugins`) — installed and activated plugins.

### Source & install

- **Source type** (`sourceType`) — `BLANK` or an imported project (`sourceProjectId`).
- **Auto install** (`autoInstall`) — set up WordPress automatically on first run.

### Server

- **Port** (`phpPort`) — allocated through the [Port Manager](/guide/more-features/port-manager).
- **Custom PHP extensions** (`customPhpExtensions`) — add `.so` / zend extensions.

## Notes

- Because it uses SQLite, WordPress runs without a separate database server.
- The WebView loads the locally-served WordPress instance.
