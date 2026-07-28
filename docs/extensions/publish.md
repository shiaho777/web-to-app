# Publish to the Market

The Module Market is a GitHub-backed catalog of community JS/CSS extension modules. There is no backend — the app fetches catalog files straight from this repository, so contributing is a normal pull-request flow.

::: info Canonical rules
The authoritative submission rules, field schemas, reviewer checklist, and CI validation live in [`modules/README.md`](https://github.com/shiaho777/web-to-app/blob/main/modules/README.md). This page is a quick start.
:::

## Catalog layout

```
modules/
├── registry.json        # app-facing catalog
├── submissions.json     # CI-generated PR / contributor metadata
├── README.md            # contributor guide
└── <module-folder>/     # each module
```

The app fetches both `registry.json` and `submissions.json` and **only shows modules present in both**, keeping the in-app catalog aligned with actually-merged PRs.

## Add a module

1. Create a **kebab-case** folder under `modules/`:

   ```
   modules/my-module/
   ├── module.json    # required
   ├── main.js        # required
   ├── style.css      # optional
   └── icon.png       # optional, ≤256KB
   ```

2. Add an entry to `registry.json`:

   ```json
   {
     "id": "my-module",
     "path": "my-module",
     "name": "My Module",
     "description": "What it does",
     "icon": "star",
     "category": "CONTENT_ENHANCE",
     "tags": ["demo"],
     "version": "1.0.0",
     "author": { "name": "You" },
     "runAt": "DOCUMENT_END",
     "permissions": ["DOM_ACCESS"],
     "urlMatches": [{ "pattern": "*://example.com/*" }],
     "hasCss": false
   }
   ```

   Note: in `registry.json`, `version` is a **semver string** (unlike `module.json`, where it's an object), and there are two extra fields — `path` and `hasCss`.

3. Keep `registry.json` and `module.json` consistent — `id`, `name`, `version`, `runAt`, and `permissions` must agree. CI enforces this.

4. Open a pull request. CI runs the validator automatically.

## What CI validates

`python3 .github/scripts/ci/validate_modules.py` checks:

- JSON validity and required fields
- Allowed enum values (`category`, `runAt`, `permissions`, `configItems.type`)
- `registry.json` ↔ `module.json` consistency
- kebab-case folder names; no orphan or ghost entries; no duplicate `id`/`path`
- Required files present; `hasCss` agrees with the presence of `style.css`
- `iconUrl` size/extension limits
- No top-level `return` in `main.js`
- `getConfig` keys correspond to declared `configItems`

## Browser extensions are different

The community market carries **only JS/CSS modules**. MV3 browser extensions are not a community catalog — the **Browser Extensions** tab searches the Chrome Web Store live instead. See [Chrome MV3 Extensions](/extensions/chrome-mv3).

## Mirrors

Catalog files and module icons route through a global mirror first, with `raw.githubusercontent.com` and jsDelivr as automatic fallbacks, so the store loads fast everywhere (including mainland China). The default client cache is one hour, so merged modules propagate without an app update.
