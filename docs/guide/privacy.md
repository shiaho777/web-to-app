# Privacy & Hardening

WebToApp includes a broad set of privacy, fingerprint-defense, and hardening features. All are per-app and opt-in.

## Browser fingerprint disguise

Disguise across **50+ vectors**: User-Agent, WebGL, Canvas, AudioContext, ClientRects, timezone, language, memory, media devices, WebRTC, fonts, battery, permissions, performance, storage, notifications, CSS media, iframe propagation, and error-stack cleanup.

## Ad blocking

A hosts-rule ad blocker with cosmetic MutationObserver filtering, **20 built-in community filter lists** (EasyList, uBlock Origin, AdGuard, AdAway, plus 8 language-specific lists), per-source enable/disable/delete, and custom subscription rules bundled into the APK.

Ad blocking is wired for both preview and export: the host ad blocker serves preview, and the compiled rule set ships in the generated APK.

## Resource encryption

PBKDF2 + AES-256-GCM for packaged config, HTML, media, and BGM. An optional custom encryption password is stronger than package/certificate-derived keys. See [Build & Export](/guide/build-export#resource-encryption).

## Runtime hardening

When encryption is on: anti-debug, anti-Frida, and DEX-tamper checks. Threat response is configurable — log-only, silent exit, or randomized crash.

## Content isolation

Isolate storage, WebRTC, Canvas, Audio, WebGL, fonts, headers, and IP surfaces per app.

## Activation gating

Gate the app behind activation codes — local verification, or your own HTTPS endpoint signed with EC P-256. See the [remote activation reference](https://github.com/shiaho777/web-to-app/blob/main/.github/docs/remote-activation.md).

---

::: tip Configuration walkthroughs coming soon
Step-by-step guides for each privacy feature are being written.
:::
