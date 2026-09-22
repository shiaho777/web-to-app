# Publish to the Market

The Plugin Market is a GitHub-backed catalog of community plugins. There is no backend — the app fetches catalog files straight from this repository, so contributing is a normal pull-request flow.

::: info Canonical rules
The authoritative submission rules, field schemas, reviewer checklist, and CI validation live in [`modules/README.md`](https://github.com/shiaho777/web-to-app/blob/main/modules/README.md). This page is a quick start.
:::

## Catalog layout

```
modules/
├── registry.json        # app-facing catalog
├── submissions.json     # CI-generated PR / contributor metadata
├── README.md            # contributor guide
└── <plugin-folder>/     # each plugin package
```

The app fetches both `registry.json` and `submissions.json` and **only shows plugins present in both**, keeping the in-app catalog aligned with actually-merged PRs.

## Add a plugin

1. Create a **kebab-case** folder under `modules/`:

   ```
   modules/my-plugin/
   ├── plugin.json    # required
   ├── plugin.html    # required — page script (`<script type="hcj/page">`) + panel document
   └── icon.png       # optional, ≤256KB
   ```

   Legacy `main.js` / `style.css` / `panel.html` packages still validate and install; new submissions should use the single `plugin.html`.

2. Add an entry to `registry.json`:

   ```json
   {
     "id": "my-plugin",
     "path": "my-plugin",
     "name": "My Plugin",
     "description": "What it does",
     "icon": "star",
     "category": "CONTENT_ENHANCE",
     "tags": ["demo"],
     "version": "1.0.0",
     "author": { "name": "You" },
     "runAt": "DOCUMENT_END",
     "permissions": ["STORAGE"],
     "urlMatches": [{ "pattern": "*://example.com/*" }],
     "hasCss": false
   }
   ```

   The registry keeps its legacy field names (`runAt` in upper-snake, `urlMatches` objects, `hasCss`) so older clients keep working — `plugin.json` uses the new field names (`document_end`, `matches`).

3. Keep `plugin.json` and `registry.json` consistent — `id`, `name`, and `version` must agree. The retired `module.json` format is rejected by CI.

4. Open a pull request. CI runs the validator automatically.

## What CI validates

`python3 .github/scripts/ci/validate_modules.py` checks:

- JSON validity and required fields (`plugin.json`, `registry.json`)
- Allowed values (`runAt`, `permissions`, registry enums)
- `plugin.json` ↔ `registry.json` consistency (`id` / `name` / `version`)
- kebab-case folder names; no orphan or ghost entries; no duplicate `id`/`path`
- Required files present (`plugin.html`, or legacy `main.js`)
- `iconUrl` size/extension limits
- No top-level `return` in the page script (`hcj/page` block / `main.js`)

## Browser extensions are different

The community market carries **only HCJ plugin packages**. MV3 browser extensions are not a community catalog — the **Browser Extensions** tab searches the Chrome Web Store live instead. See [Chrome MV3 Extensions](/extensions/chrome-mv3).

## Mirrors

Catalog files and plugin icons route through a global mirror first, with `raw.githubusercontent.com` and jsDelivr as automatic fallbacks, so the store loads fast everywhere (including mainland China). The default client cache is one hour, so merged plugins propagate without an app update.
