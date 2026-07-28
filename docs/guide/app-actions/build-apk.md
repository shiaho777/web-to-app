# Build APK

Builds and signs an installable APK from the app. Tap ⋮ on an app card, then **Build APK**.

## The build dialog

- **Browser engine** — System WebView or GeckoView (GeckoView downloads on first use).
- **Resource encryption** — PBKDF2 + AES-256-GCM for packaged config/HTML/media/BGM, with an optional custom password. Enabling it activates runtime hardening (anti-debug, anti-Frida, DEX-tamper) and always forces a full rebuild.
- **Isolation** — per-app isolation of storage, WebRTC, Canvas, Audio, WebGL, fonts, headers, and IP surfaces.
- **Background run** — keep the app's service alive in the background.
- **Notifications** — scheduled/persistent notifications, URL-polling foreground service, deep links.
- **Force full rebuild** — skip incremental caching.
- **Version code** — auto-suggests the next version code when a custom package name is already installed.

A **preflight check** runs first and reports blocking errors. After a successful build you can jump to [AAB export](/guide/more-features/google-play).

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
