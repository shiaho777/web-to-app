# More · System

The remaining tools in the **⋮** menu.

## Usage Stats

Per-app analytics and health monitoring.

- **Usage stats** — per-app launch/usage charts (Vico), plus overall totals.
- **URL health** — checks each app's target URL and reports online / slow / offline (the same status shown as a dot on app cards).
- **Actions** — check one app, check all, refresh overall, or clear all stats.

## Google Play

Export a generated app as a Play-ready signed AAB.

- **AAB export** — auto-builds the APK on demand, converts it to a signed AAB with `targetSdk` rewritten to the Play-required level (currently 36), and generates protobuf metadata locally.
- **Keystore** — create/import/manage signing keys.
- Cancellable mid-build; can be launched directly for a specific app from its [Build APK](/guide/app-actions#build-apk) dialog.

See [Signing & AAB export](/guide/app-actions#signing-aab-export).

## File Manager

A single place for build outputs and user files.

- **Browse** — APK builds, AAB exports, app clones, build logs, and a user-files directory.
- **Actions** — view, share, install, open, and clear files; multi-select mode for batch operations.
- **Build log viewer** — read-only viewer for build logs.

## Batch Import

Import many apps at once. Paste or load a list of entries (e.g. name + URL pairs) and WebToApp creates the apps in bulk — handy for migrating a collection.

## About

App information and data tools.

- **About** — version and project links.
- **Data backup / restore** — back up and restore your projects and app data.
- **Update check** — check for a newer version of the builder.
