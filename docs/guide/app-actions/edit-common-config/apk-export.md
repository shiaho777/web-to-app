# APK Export Config

Packaging and identity settings for the generated APK. This is the export drawer in the editor.

**Where:** the **APK export config** drawer in the [Edit Common Config](/guide/app-actions/edit-common-config/) editor (backed by `ApkExportConfig`).

## Identity

- **Custom package name** — the APK's application id (validated against a package-name pattern).
- **Version code / version name** — the APK version.
- **Auto-bump version** — when this package is already installed with a higher versionCode, the builder raises the version so the update can install. On by default; turn it off to always keep the version set above (a downgrade install may then fail).
- **Engine type** — System WebView or GeckoView for the exported app.

## Signing

- **Keystore** — create/import/manage the signing key (PKCS12/PFX/JKS/BKS).
- **Signature schemes** — V1/V2/V3 independently, with legacy auto-fallback and a custom V1 signer filename.

## Runtime permissions

- Permissions are derived from the enabled features (feature-driven), and unused permissions are pruned from the template manifest at build time.

## Static SAEP policy

The optional **Static SAEP policy** switch is saved per app (`ApkExportConfig.saepEnabled`) in the export drawer and Build APK screen. It defaults to **OFF**, including for older saved configurations: OFF omits SAEP metadata, rather than declaring a deny policy. ON declares **no additional restrictions** through static policy metadata. It does not grant Android permissions, bypass authorization, or guarantee that an agent supports SAEP.

This applies only to generated APKs, not the builder host. Export writes `com.obric.agentrobots.POLICY_JSON` as an Android resource reference, binds the policy to the final package name, and keeps it readable even with resource encryption enabled. Changing the switch forces a full rebuild. A newly built shell template is required; enabling SAEP with an older template fails explicitly instead of emitting a broken declaration. System/agent support and successful automation must still be verified on a compatible device. See the [official SAEP demo](https://github.com/bytedance/SAEP-demo).

## Network trust

- **Client Certificate Authentication (mTLS)** — when a server requests a client certificate, the generated app opens Android's system certificate picker and uses the selected device-installed identity. This is separate from trusting a server CA. The selected identity is reused for later connections to the same server.

## Build-time options (in the Build dialog)

These are chosen when you [Build APK](/guide/app-actions/build-apk):

- **Resource encryption** — PBKDF2 + AES-256-GCM, with an optional custom password.
- **Isolation** — per-app isolation of storage/WebRTC/Canvas/Audio/WebGL/fonts/headers/IP.
- **Background run** — keep a service alive (`backgroundRunConfig`).
- **Notifications** — scheduled/persistent notifications and polling (`notificationConfig`).
- **Force full rebuild** — skip incremental caching.

## Notes

- For Play Store distribution, export an AAB from [Google Play](/guide/more-features/google-play), which rewrites `targetSdk` to the Play-required level.
