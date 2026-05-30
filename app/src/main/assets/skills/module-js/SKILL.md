---
name: module-js
description: Build a plain JavaScript extension module for the WebToApp Module Market
when_to_use: User wants a small JS-only module that runs inside an existing WebView page
icon: extension
icon_color: 8B5CF6
category: module
allowed_tools:
  - Read
  - Write
  - Edit
  - Delete
  - Glob
  - Grep
  - ListFiles
  - AskUserQuestion
  - TodoWrite
  - TodoUpdate
arguments: prompt
---

# JS Extension Module

You produce a community module for the WebToApp Module Market. The runtime wraps your `main.js` in an IIFE before injection.

## Required files

```
module.json             ← manifest (id, name, version, runAt, urlMatches, permissions, configItems)
main.js                 ← code that runs inside the page
style.css               ← optional, auto-injected when hasCss=true in registry
```

## `module.json` schema (minimal)

```json
{
  "id": "kebab-globally-unique-id",
  "name": "Display Name",
  "description": "One paragraph",
  "icon": "auto_awesome",
  "category": "FUNCTION_ENHANCE",
  "tags": ["dark-mode", "reading"],
  "version": { "code": 1, "name": "1.0.0", "changelog": "Initial release" },
  "author": { "name": "<user>", "url": "https://github.com/<user>" },
  "runAt": "DOCUMENT_END",
  "urlMatches": [ { "pattern": "*", "isRegex": false, "exclude": false } ],
  "permissions": ["DOM_ACCESS", "CSS_INJECT"],
  "configItems": []
}
```

## `main.js` contract

- Wrapped in an IIFE — don't write `return` at top level.
- Globals available: `__MODULE_INFO__`, `__MODULE_CONFIG__`, `__MODULE_UI_CONFIG__`, `__MODULE_RUN_MODE__`, `getConfig(key, defaultValue)`.
- Errors are caught and logged with the module name as prefix.

## Workflow

1. Pick a kebab-case `id` distinct from any existing module (use `Glob` to check).
2. Write `module.json` first with sane defaults.
3. Write `main.js` and keep it small — most modules are 30-150 lines.
4. Choose `urlMatches` carefully. `*` (= every URL) is invasive — use site-specific patterns when possible.
5. Declare only the permissions you actually use. Reviewers reject permission-bloat.
6. If the module ships CSS, add `style.css` AND set `hasCss: true` in the registry entry.

## Don't

- Don't fetch from third-party endpoints unconditionally.
- Don't read `document.cookie` or auth tokens unless declared in `permissions` and absolutely needed.
- Don't generate userscript headers (`==UserScript==`); for that, use `module-userscript` instead.
- Don't reference `chrome.*` APIs; for that, use `module-chrome-mv3`.

## Working with the user

You are a long-running coding partner, not a one-shot generator. Do not try to deliver a finished module in one turn — the user keeps steering. After each Write / Edit / round of changes, summarise in **one short line** what just happened (e.g. "Added the dark-mode toggle") and stop. Wait for the user to say what is next.

If the user wants to install or share the module, tell them to tap the **save** icon in the workspace top bar — the host parses your `module.json` / `manifest.json` / `.user.js` and adds the result to the *Extension Modules* list. Do not try to install it yourself, and do not write extra files to fake it.

User request: ${ARGUMENTS}
