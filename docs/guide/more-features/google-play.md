# Google Play

Export a generated app as a Play-ready signed AAB. Open it from [⋮ → Google Play](/guide/main-screen/more).

## Which apps can be published

All current app types are WebView-based, so the AAB path covers everything the builder can create. The standalone APK's `targetSdk` (35 by default, or the pinned override) is an APK-packaging detail and never reaches Play — you upload the AAB, and the exporter gives it a Play-compliant `targetSdk` (currently 36).

| App type | Play AAB |
| --- | --- |
| Web · Multi-Web · HTML · Offline Pack · Frontend · Gallery | Supported |
| Signature-bound resource encryption (non-embedded key) | Not supported |
| Apps of discontinued types restored from old backups | Not supported |

**Why signature-bound encryption is excluded.** Signature-bound resource encryption derives its key from the signing certificate; Play App Signing re-signs the delivered APKs, so the derived key no longer decrypts the config. Embedded-key mode survives re-signing and is Play-safe.

**Discontinued types.** Apps created by older versions with since-removed types (server-runtime or standalone media apps) fail the policy check and can't be exported as AAB.

## AAB export

One tap runs the full pipeline, with visible stages:

1. **Building APK** — assemble the APK on demand.
2. **Assembling** — convert to AAB.
3. **Signing** — sign the bundle.
4. **Signed** — done; ready to share or upload.

- **targetSdk rewrite** — the AAB's `targetSdk` is rewritten to the Play-required level (currently 36).
- **Metadata** — protobuf metadata is generated locally.
- **Cancellable** — stop mid-build.
- **App count** — see how many apps are eligible.

## Keystore

Create, import, and manage the signing keys used for the AAB.

## Notes

- You can also launch AAB export for a specific app from its [Build APK](/guide/app-actions/build-apk) dialog.
- The generated APK ships `targetSdk` 35 from the shell template; only the AAB is rewritten for Play. All app types can optionally pin a different `targetSdk` for the standalone APK from the APK export section; see [Build APK](/guide/app-actions/build-apk).
- A pre-upload advisory and warning are shown before exporting.
