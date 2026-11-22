# App Modifier

Repackage an *installed* app. Open it from [⋮ → App Modifier](/guide/main-screen/more).

## Clone

App Modifier creates a separate installable copy from an installed APK. It does not modify or replace the original app.

## Features

- **App library** — browse installed apps (system / user), sorted by name or recent, with search and result counts.
- **Identity** — set a new app name and package name for the clone.
- **Preview** — review the clone's identity before building.

## Notes

- A clone must use a package name that is not installed; the editor pre-fills a unique one and rejects a package that is already in use.
- The clone's launcher label is written into the binary manifest, so the new name is used even when the original label came from an Activity, a literal, or a localized resource.
- The modified APK is patched at the binary level (AXML/ARSC) and re-signed.
- The clone changes only the app name and package identity. It does not inject runtime features or customize the icon.
- Re-signing changes the app's signing identity, so the clone cannot be installed as an update to the original. Use only with appropriate rights.
