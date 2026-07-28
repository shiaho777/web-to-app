# Offline Pack

Scrapes a live website into a self-contained offline package, then wraps it like an [HTML](/guide/app-types/html) app.

## When to use

Archiving a site for offline use — reading material, documentation, or any site you want available without a network.

## Key configuration (scraper)

- **Max depth** — how many link hops to follow.
- **Follow links** — toggle crawling within the site.
- **Max files** and **max total size** — caps on the scrape.
- **Skip patterns** — URL patterns to exclude.
- **Timeout** — per-request timeout.
- **Download CDN resources** — whether to pull CDN-hosted assets locally.

## What it rewrites

HTML, CSS, JS, images, and fonts — including `url()`, `srcset`, and `@import` references — with path rewriting, subject to same-domain, depth, and size limits.

## Notes

- The result is a local, offline copy; dynamic server-side behavior won't be captured.
- For a site that stays online, use [Web](/guide/app-types/web).
