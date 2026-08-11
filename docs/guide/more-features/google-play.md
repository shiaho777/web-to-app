# Google Play

Export a generated app as a Play-ready signed AAB. Open it from [⋮ → Google Play](/guide/main-screen/more).

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
- The generated APK keeps `targetSdk` 28 by default (required for fork+exec runtimes — Node.js, PHP, Python, Go, WordPress); only the AAB is rewritten for Play. WebView-only app types can optionally raise the standalone APK's `targetSdk` from the APK export section; see [Build APK](/guide/app-actions/build-apk).
- A pre-upload advisory and warning are shown before exporting.
