# Offline Pack

Scrapes a live website into a self-contained offline package, then wraps it like an [HTML](/guide/app-types/html) app.

## When to use

Archiving a site for offline use — reading material, documentation, or any site you want available without a network.

## Core config (scraper)

### Crawl scope

- **Max depth** (`maxDepth`) — how many link hops to follow.
- **Follow links** (`followLinks`) — toggle crawling within the site.
- **Max files** (`maxFiles`) and **max total size** (`maxTotalSizeMb`) — caps on the scrape.

### Filtering

- **Skip patterns** (`skipPatterns`) — URL patterns to exclude.

### Network

- **Timeout** (`timeoutSeconds`) — per-request timeout.
- **Download CDN resources** (`downloadCdnResources`) — whether to pull CDN-hosted assets locally.

## What it rewrites

HTML, CSS, JS, images, and fonts — including `url()`, `srcset`, and `@import` references — with path rewriting, subject to same-domain, depth, and size limits.

## Notes

- The result is a local, offline copy; dynamic server-side behavior won't be captured.
- For a site that stays online, use [Web](/guide/app-types/web).
