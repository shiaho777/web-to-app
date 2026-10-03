# Google Play

Export a generated app as a Play-ready signed AAB. Open it from [⋮ → Google Play](/guide/main-screen/more).

## Which apps can be published

WebView app types export a Play-ready AAB. Node.js, PHP, Python, Go, and WordPress do not: their APKs are pinned to `targetSdk` 28 so they can fork+exec, and Play's required level turns on W^X. The APK `targetSdk` never reaches Play — you upload the AAB, and the exporter rewrites it to 36.

| App type | Play AAB |
| --- | --- |
| Web · Multi-Web · HTML · Offline Pack · Frontend · Media · Gallery | Supported |
| Node.js · PHP · Python · Go · WordPress | Not supported |
| Any type with resource encryption enabled | Not supported |

**Why server-runtime apps are excluded.** They need `targetSdk` 28 to fork+exec their bundled runtimes from app storage, and Play requires a target level that turns on W^X — which blocks exactly that. Converting one would produce a bundle whose app can never start its own server, so AAB export is refused outright rather than silently degraded. Distribute those as APKs instead: sideload, enterprise, or any store that accepts `targetSdk` 28. Nothing else about them is limited.

A Multi-Web app is always publishable. Server-runtime apps cannot be embedded as multi-web sites in the first place — their runtimes are not packaged into a multi-web APK — so such a site falls back to its URL at export and the app never runs one.

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
- WebView APKs inherit the shell template's `targetSdk` 35. Server-runtime APKs are pinned to 28 and ignore the override. Only the AAB is rewritten for Play. See [Build APK](/guide/app-actions/build-apk).
- A pre-upload advisory and warning are shown before exporting.
