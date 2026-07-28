# App Modifier

Repackage an *installed* app. Open it from [⋮ → App Modifier](/guide/main-screen/more).

## Two modes

- **Shortcut disguise** — create a disguised shortcut entry pointing at an existing app.
- **Clone** — a real binary clone with manifest/resource patching and re-signing, producing a new installable APK with a new identity.

## Features

- **App library** — browse installed apps (system / user), sorted by name or recent, with search and result counts.
- **Identity** — set a new identity: custom name and **custom icon**.
- **Output mode** — choose how the result is produced.
- **Preview** — preview the modified result before building.

## Notes

- Clone mode may require the original icon and applies no enhancements by default.
- The modified APK is patched at the binary level (AXML/ARSC) and re-signed.
- Intended for icon/name/package experiments and repackaging research — use only with appropriate rights.
