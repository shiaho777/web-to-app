# APK Export Config

Packaging and identity settings for the generated APK. This is the export drawer in the editor.

**Where:** the **APK export config** drawer in the [Edit Common Config](/guide/app-actions/edit-common-config/) editor (backed by `ApkExportConfig`).

## Identity

- **Custom package name** — the APK's application id (validated against a package-name pattern).
- **Version code / version name** — the APK version; the builder can suggest the next version code if the package is already installed.
- **Engine type** — System WebView or GeckoView for the exported app.

## Signing

- **Keystore** — create/import/manage the signing key (PKCS12/PFX/JKS/BKS).
- **Signature schemes** — V1/V2/V3 independently, with legacy auto-fallback and a custom V1 signer filename.

## Runtime permissions

- Permissions are derived from the enabled features (feature-driven), and unused permissions are pruned from the template manifest at build time.

## Build-time options (in the Build dialog)

These are chosen when you [Build APK](/guide/app-actions/build-apk):

- **Resource encryption** — PBKDF2 + AES-256-GCM, with an optional custom password.
- **Isolation** — per-app isolation of storage/WebRTC/Canvas/Audio/WebGL/fonts/headers/IP.
- **Background run** — keep a service alive (`backgroundRunConfig`).
- **Notifications** — scheduled/persistent notifications and polling (`notificationConfig`).
- **Force full rebuild** — skip incremental caching.

## Notes

- For Play Store distribution, export an AAB from [Google Play](/guide/more-features/google-play), which rewrites `targetSdk` to the Play-required level.
