# Data Backup

Export or import all app data — configs, icons, splash screens, BGM, and other resource files. Open it from [⋮ → Data Backup](/guide/main-screen/more).

## How it works

- **Export Data** — writes every app's configuration and resource files into a single `.zip` backup.
- **Import Data** — restores a previously exported `.zip`. Imported apps are added as new entries; existing data is never overwritten.
- If local settings files were restored, the app asks before restarting so they take effect.

## When to use

Moving projects between devices, or taking a snapshot before a major update.

## Notes

- Importing adds new apps rather than merging — export a backup before updating the app.
- The backup is a plain `.zip` you can store anywhere.

## Source export archives

**Export source** on a single app produces an Android Studio project, and that zip is also a valid WebToApp backup for that one app. It carries `apps.json` (the full project configuration), `resources/` (icons and media), and `local/files/` (HTML, frontend, and Node.js / PHP / Python / Go / WordPress project sources). After reinstalling WebToApp, pick that zip from **Import Data** to restore the app.

A source bundle holds one app only. It does not include global settings, extension modules, ad-block rules, or signing keystores — use **Export Data** for a full snapshot. Source bundles produced by older versions predate `apps.json` and cannot be imported; re-export them with a current build.

## Before uninstalling

WebToApp disables Android's system backup (`allowBackup="false"`), so uninstalling the builder permanently deletes every project, its source files, and its configuration. Export a backup before uninstalling, moving to a new device, or letting a device security scanner act on the app. An "uninstall" triggered by a virus scanner is not reversible.
