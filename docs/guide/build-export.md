# Build & Export

WebToApp builds and signs APKs entirely on-device, and can convert them to Google Play-ready AABs.

## The build pipeline

When you tap **Build APK**, the builder:

1. **Loads the shell template** — a single canonical `webview_shell.apk` (the runtime, synced from the host `app/` sources).
2. **Patches binary resources** — rewrites the package name, icon, label, and version in the binary AXML manifest and ARSC resource table.
3. **Prunes permissions** — removes unused permissions from the template manifest.
4. **Embeds config & content** — serializes your `WebApp` configuration to an assets JSON (`app_config.json`) and packages app content (HTML, media, runtime assets).
5. **Signs** — applies V1/V2/V3 signatures via `apksig`.

### Incremental rebuilds

Rebuilds are incremental via a content-addressed cache with three modes:

| Mode | When |
| --- | --- |
| `FULL` | Template or identity changed; always used for encrypted builds |
| `CONTENT_OVERLAY` | Only app content changed |
| `REUSE_UNSIGNED` | Re-sign a previously built unsigned APK |

Cache keys are **content hashes**, never timestamps. Encrypted builds always force a full rebuild.

::: warning
Never feed a signed or renamed APK back into the full build path as a template — it corrupts cache keys and identity patching.
:::

## Signing

Configure under the export settings:

- **Keystore management** — create, import, export, delete, and view certificate fingerprints. Supports PKCS12/PFX/JKS/BKS import, including Android Studio upload-key cases where store and key passwords differ.
- **Signature schemes** — V1, V2, V3 independently controlled, with auto-fallback for legacy certificates. Custom V1 signer filename for `META-INF/<name>.SF` / `.RSA`.

## Resource encryption

Enable encryption to protect packaged config, HTML, media, and BGM with **PBKDF2 + AES-256-GCM**. You can supply a custom encryption password stronger than the package/certificate-derived key. When encryption is on, runtime hardening (anti-debug, anti-Frida, DEX-tamper checks) activates, with a configurable threat response: log-only, silent exit, or randomized crash.

Encrypted builds always rebuild from scratch.

## Google Play AAB export

One-tap AAB export:

1. Auto-builds the APK on demand.
2. Converts it to a signed AAB with `targetSdk` rewritten to the Play-required level (currently 36).
3. Generates protobuf metadata locally.

The build is cancellable mid-way.

::: info Why two targetSdk values?
The generated APK keeps a low `targetSdk` (28) so it can fork+exec native runtimes from app storage. The AAB exporter separately rewrites `targetSdk` to satisfy Play Store policy. These are independent paths.
:::

## Performance options

Image compression, WebP conversion, code minification, lazy loading, DNS prefetch, and preload hints — all toggled per app.

## File Manager

A single screen to view, share, install, open, and clear build outputs (APK builds, AAB exports, app clones, build logs) plus a user-files directory, with a read-only build-log viewer.
