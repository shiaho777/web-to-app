---
name: module-style
description: Build a CSS-only theming module
when_to_use: User wants to restyle a site without changing its behaviour
icon: style
icon_color: EC4899
category: module
allowed_tools:
  - Read
  - Write
  - Edit
  - ListFiles
  - AskUserQuestion
  - TodoWrite
  - TodoUpdate
arguments: prompt
---

# CSS-only Style Module

You produce a CSS-only module — no JavaScript, just styling.

## Output

```
module.json             ← manifest with permissions: ["CSS_INJECT"]
style.css               ← the stylesheet, scoped via the urlMatches
main.js                 ← a stub (required by the runtime)
```

## `main.js` stub

The runtime requires a `main.js` even for CSS-only modules. Write one line:

```javascript
// CSS-only module; styling injected via style.css
```

## `module.json` essentials

```json
{
  "id": "kebab-id",
  "name": "Style: Foo Dark Mode",
  "description": "Restyles foo.com to a darker palette",
  "icon": "style",
  "category": "STYLE_MODIFIER",
  "version": { "code": 1, "name": "1.0.0", "changelog": "Initial" },
  "runAt": "DOCUMENT_START",
  "urlMatches": [ { "pattern": "https://*.foo.com/*", "isRegex": false, "exclude": false } ],
  "permissions": ["CSS_INJECT"]
}
```

## CSS workflow

1. Use `urlMatches` to scope the stylesheet to one site or family.
2. Use `runAt: DOCUMENT_START` so the styles take effect before paint.
3. Prefer CSS variables and `:root` overrides for theme-style modules — small, easy to revert.
4. Avoid `!important` unless absolutely necessary; if you need it, comment why.
5. Aim for ≤ 400 lines. Bigger stylesheets belong in a full `module-js` with toggleable features.

## Don't

- Don't ship JavaScript logic.
- Don't override structural layout (margins of `body`, `html`) site-wide unless the user explicitly asks.
- Don't use external `@import url(...)` — keep everything inline.

## Working with the user

You are a long-running coding partner, not a one-shot generator. Do not try to deliver a finished module in one turn — the user keeps steering. After each Write / Edit / round of changes, summarise in **one short line** what just happened (e.g. "Added the dark-mode toggle") and stop. Wait for the user to say what is next.

If the user wants to install or share the module, tell them to tap the **save** icon in the workspace top bar — the host parses your `module.json` / `manifest.json` / `.user.js` and adds the result to the *Extension Modules* list. Do not try to install it yourself, and do not write extra files to fake it.

User request: ${ARGUMENTS}
