# Build APK

Builds and signs an installable APK from the app. Tap ⋮ on an app card, then **Build APK**.

## The build dialog

- **Browser engine** — System WebView or GeckoView (GeckoView downloads on first use).
- **Resource encryption** — PBKDF2 + AES-256-GCM for packaged config/HTML/media/BGM, with an optional custom password. Enabling it activates runtime hardening (anti-debug, anti-Frida, DEX-tamper) and always forces a full rebuild.
- **Isolation** — per-app isolation of storage, WebRTC, Canvas, Audio, WebGL, fonts, headers, and IP. Generated fingerprints identify as a phone (Android Chrome, Edge, Firefox, Samsung Internet, or iPhone Safari), and the request User-Agent matches the client hints. A saved desktop fingerprint is replaced on the next launch. A custom User-Agent, desktop mode, and a desktop kernel flavor stay as you set them.
- **Background run** — keep the app's service alive in the background.
- **Notifications** — scheduled/persistent notifications, URL-polling foreground service, deep links.
- **Force full rebuild** — skip incremental caching.
- **Version code** — bumps to the next installable version code when the package is already installed with a higher one. Turn off **Auto-bump version** in the editor's APK export config to pin the configured version.

A **preflight check** runs first and reports blocking errors. After a successful build the summary offers [Export source](/guide/app-actions/export-apk), and you can jump to [AAB export](/guide/more-features/google-play).

When this package is already installed, **Launch** sits between AAB and the main build/install button, and the header card opens the same package. The action appears again when you return from the system installer. A package with no launcher icon shows a toast instead of doing nothing.

## What happens

WebToApp patches the shell template, embeds your config and content, prunes unused permissions, and signs the result (V1/V2/V3).

## Incremental rebuilds

| Mode | When |
| --- | --- |
| `FULL` | Template or identity changed; always for encrypted builds |
| `CONTENT_OVERLAY` | Only app content changed |
| `REUSE_UNSIGNED` | Re-sign a previously built unsigned APK |

Cache keys are content hashes, never timestamps. Never feed a signed/renamed APK back as a template.

## Signing

- **Keystore** — create/import/manage keys (PKCS12/PFX/JKS/BKS).
- **Schemes** — V1/V2/V3 independently controlled, with legacy auto-fallback and a custom V1 signer filename.

Outputs land in the [File Manager](/guide/more-features/file-manager).
