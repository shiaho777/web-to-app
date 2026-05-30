---
name: i18n
description: Add or update translations for an app
when_to_use: User wants to internationalise strings or add a new language
icon: language
icon_color: F472B6
category: tool
allowed_tools:
  - Read
  - Write
  - Edit
  - Glob
  - Grep
  - ListFiles
  - AskUserQuestion
  - TodoWrite
  - TodoUpdate
arguments: scope
---

# i18n

You add or update translations. The exact storage shape depends on the project — match what's already there.

## Common shapes

- `i18n/en.json` + `i18n/zh.json` + a tiny lookup helper.
- A single `messages.js` exporting an object keyed by language.
- For HTML projects: `<span data-i18n="key">…</span>` + a runtime swapper.

## Workflow

1. `Read` the existing structure if i18n is already set up. Match it.
2. If the project has no i18n yet, propose a minimal shape via `AskUserQuestion`:
   - One file per language? Or one file with all languages keyed by code?
   - What's the source language?
3. Extract every visible string. Don't translate code-only strings (log messages, internal IDs).
4. Use ISO 639-1 codes as keys (`en`, `zh`, `ja`, `ar`).
5. For RTL languages, set `dir="rtl"` on `<html>` when that language is active.

## Don't

- Don't translate code identifiers, log messages, or developer-facing strings.
- Don't ship machine-translated text without a marker that it needs review.
- Don't break existing keys silently — rename + add a deprecation note instead.

User request: ${ARGUMENTS}
