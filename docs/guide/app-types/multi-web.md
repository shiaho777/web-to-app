# Multi-Web

Combines multiple sites into a single app with a choice of layouts.

## When to use

Link hubs, portals, and app collections where you want several sites under one roof.

## Core config

Backed by `MultiWebConfig` and a list of `MultiWebSite` entries.

### Sites

Each site entry has:

- **Name & URL** — the site's label and address.
- **Type** — `URL`, `LOCAL`, `INLINE_HTML`, or `EXISTING` (reuse another app/project).
- **Icon** — an emoji icon or a favicon URL.
- **Theme color** — a per-site accent color.
- **Category** — group sites within the app.
- **Selectors** — a CSS selector (`cssSelector`) and link selector (`linkSelector`) for content extraction.
- **Enabled & order** — toggle a site and set its sort index.
- **Per-site config** — a site can carry its own `webViewConfig` or `htmlConfig`. The model also has server-runtime config slots, but a site typed as a server-runtime app is not embeddable — see Notes.

### Layout & display

- **Display mode** (`displayMode`) — `TABS`, cards, feed, or drawer. New apps are created with `TABS`; there is currently no UI switch for the other layouts.
- **Show site icons** (`showSiteIcons`).

### Refresh

- **Refresh interval** (`refreshInterval`) — seconds between auto-refreshes.

### Shared injection

- Common JS/CSS applied across all sites.

## Notes

- Each site is still a web target, so per-app networking and privacy options apply to the whole multi-web app.
- Gallery / single-image / single-video sites have their media embedded at export and resolved at runtime, so they render instead of going black.
- Server-runtime apps (Node.js / PHP / Python / Go / WordPress) cannot be embedded as sites — their runtimes are not packaged into a multi-web APK. The site picker hides them, and a legacy config that still references one falls back to the site's URL at export.
- For a single site, use [Web](/guide/app-types/web) instead.
