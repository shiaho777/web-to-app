# APK Export Config

Packaging and identity settings for the generated APK. This is the export drawer in the editor.

**Where:** the **APK export config** drawer in the [Edit Common Config](/guide/app-actions/edit-common-config/) editor (backed by `ApkExportConfig`).

## Identity

- **Custom package name** — the APK's application id (validated against a package-name pattern).
- **Version code / version name** — the APK version.
- **Auto-bump version** — when this package is already installed with a higher versionCode, the builder raises the version so the update can install. On by default; turn it off to always keep the version set above (a downgrade install may then fail).
- **Engine type** — System WebView or GeckoView for the exported app.
- **Override minSdk** — raise `minSdkVersion` above the shell template's 23 (choices 23/24/26/28/31/33/34/35/36, never above the current targetSdk). Off keeps 23. The packed runtime is compiled for API 23, so the floor cannot go lower.
- **Override targetSdk** — pin a different `targetSdkVersion` for the generated APK (choices 28/31/33/34/35/36); off keeps the shell template's 35. Server-runtime types stay pinned at 28. For Play distribution the AAB export still forces the Play-required level.

## Signing

- **Keystore** — create/import/manage the signing key (PKCS12/PFX/JKS/BKS).
- **Signature schemes** — V1/V2/V3 independently, with legacy auto-fallback and a custom V1 signer filename.

## Runtime permissions

This list is the official set a **third-party app can declare and request**, from API 23 through Android 17. Each row is one manifest permission, marked with the API that added it. Deprecated rows stay marked, and are not requested past that API. Signature, internal, role-only, and OEM-private permissions are not listed.

- Older saved switches still export as the same bundle. Camera still declares `CAMERA` and `FOREGROUND_SERVICE_CAMERA`. Turning one row off in this list affects only that permission.
- Features still auto-check the permissions they need (`RuntimePermissionSync`). Export prunes template permissions the app does not use.
- **Foreground service types** follow the services that remain. `DATA_SYNC` is host-only. A generated app declares `specialUse` only for the services it keeps, and `mediaPlayback` only when media session is on.
- **Local network** (`ACCESS_LOCAL_NETWORK`) is declared when the app needs the network, so Android 16/17 LAN loads do not fail. You can turn that row off. File HTML with no network omits `INTERNET`, `ACCESS_NETWORK_STATE`, and this permission.
- **Installed app list** (`QUERY_ALL_PACKAGES`) is off by default. It is written into the generated APK only when checked, and Play may reject it. The GitHub host flavor already declares it for the builder; that does not apply to generated APKs.
- SMS, call log, outgoing-call processing, all-files access, usage access, unknown-app install, and exact alarms are each their own row and marked Play-restricted. Turning one on writes it into the manifest. Dangerous permissions prompt on first launch. Page JS has no matching API.
- Bluetooth no longer pulls in nearby Wi-Fi devices. Reading images no longer pulls in user-selected photos and videos. Those are separate rows. Background location is its own row; turning it on also turns on approximate and precise location.

## Static SAEP policy

The optional **Static SAEP policy** switch is saved per app (`ApkExportConfig.saepEnabled`) in the export drawer and Build APK screen. It defaults to **OFF**, including for older saved configurations: OFF omits SAEP metadata, rather than declaring a deny policy. ON declares **no additional restrictions** through static policy metadata. It does not grant Android permissions, bypass authorization, or guarantee that an agent supports SAEP.

This applies only to generated APKs, not the builder host. Export writes `com.obric.agentrobots.POLICY_JSON` as an Android resource reference, binds the policy to the final package name, and keeps it readable even with resource encryption enabled. Changing the switch forces a full rebuild. A newly built shell template is required; enabling SAEP with an older template fails explicitly instead of emitting a broken declaration. System/agent support and successful automation must still be verified on a compatible device. See the [official SAEP demo](https://github.com/bytedance/SAEP-demo).

## Network trust

- **Client Certificate Authentication (mTLS)** — when a server requests a client certificate, the generated app opens Android's system certificate picker and uses the selected device-installed identity. This is separate from trusting a server CA. The selected identity is reused for later connections to the same server.

## Build-time options (in the Build dialog)

These are chosen when you [Build APK](/guide/app-actions/build-apk):

- **Resource encryption** — PBKDF2 + AES-256-GCM, with an optional custom password. An encrypted HTML site stays ciphertext in the APK and is decrypted in memory for each request. An invalid AdMob app id is refused before export, including an empty id or an ad-unit id in the app-id field.
- **Isolation** — per-app isolation of storage/WebRTC/Canvas/Audio/WebGL/fonts/headers/IP. Fingerprints identify as a phone, and the request User-Agent matches the client hints. A saved desktop fingerprint is replaced on the next launch. A custom User-Agent, desktop mode, and a desktop kernel flavor stay as you set them.
- **Background run** — keep a service alive (`backgroundRunConfig`).
- **Notifications** — scheduled/persistent notifications and polling (`notificationConfig`).
- **Force full rebuild** — skip incremental caching.

## Notes

- For Play Store distribution, export an AAB from [Google Play](/guide/more-features/google-play), which rewrites `targetSdk` to the Play-required level.
