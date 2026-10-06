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

- **Display mode** (`displayMode`) — chosen in the editor. **Bottom Tabs** (`TABS`, the default for new apps), **Top Tabs** (`TOP_TABS`), **Card Home** (`CARDS`), **Side Drawer** (`DRAWER`), or **Feed** (`FEED`).
- **Top Tabs** — labels sit under the status bar. The selected site has a dot. A sideways swipe follows the finger and coasts onto the neighboring site. One gesture stays on the current page or the next one; past the ends the page rubber-bands. Bottom tabs, cards, the drawer, and the feed stay a discrete switch. The bar background follows a live sample of the top of the current page, then the site's theme color. The first paint is sampled again, so the bar is not left on the light fallback. **Show site icons** applies here too. On GeckoView the sample is unavailable, so the bar uses the theme color. A covered Gecko tab is collapsed, so switching back shows that tab's page instead of the newest frame.
- **Card home and drawer** — card home uses 1–4 columns (`cardColumns`, default 2). The side drawer uses 1–3 (`drawerColumns`, default 1). More columns shrink the whole card. A height slider shortens it further (`cardAspectRatio`, default 1.2). An old config that omits these keys keeps that layout.
- **Site order** — long-press the grip in the site list. The order is saved with the app and written into the export, so the generated app shows the same sequence.
- **Tab opened on launch** (`startTab`) — **Last opened** (`LAST`, the default) or a chosen site (`SITE` plus `startSiteId`). A pinned site that was removed or disabled falls back to the first site. Card home skips the grid when a site is pinned. Feed mode has no current tab, so the control is hidden there.
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
