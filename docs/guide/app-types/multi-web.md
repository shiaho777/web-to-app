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
- **Per-site config** — a site can carry its own `webViewConfig`, `htmlConfig`, or runtime config.

### Layout & display

- **Display mode** (`displayMode`) — `TABS`, cards, feed, or drawer.
- **Show site icons** (`showSiteIcons`).

### Refresh

- **Refresh interval** (`refreshInterval`) — seconds between auto-refreshes.

### Shared injection

- Common JS/CSS applied across all sites.

## Notes

- Each site is still a web target, so per-app networking and privacy options apply to the whole multi-web app.
- For a single site, use [Web](/guide/app-types/web) instead.
