# Hosts Ad Blocking

A hosts-rule ad blocker with cosmetic filtering. Open it from [⋮ → Hosts Ad Blocking](/guide/main-screen/more).

## Features

- **Active rules count** — see how many blocking rules are loaded.
- **Sources** — manage filter subscriptions: download, import from a file or URL, or delete a source.
- **Built-in lists** — 20 community filter lists (EasyList, uBlock Origin, AdGuard, AdAway, plus 8 language-specific lists).
- **Search** — find specific rules.
- **Clear** — remove all loaded rules (with confirmation).

## Cosmetic filtering

Beyond host blocking, a MutationObserver-based cosmetic filter hides elements matched by cosmetic rules.

## Notes

- Ad blocking is wired for both preview and export: the host blocker serves preview, and the compiled rule set ships inside the generated APK.
- Configure ad blocking per app under [Privacy](/guide/config/privacy).
