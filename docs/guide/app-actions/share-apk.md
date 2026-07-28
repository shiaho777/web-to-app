# Share APK

Builds the APK, then opens the system share sheet to send it. Tap ⋮ on an app card, then **Share APK**.

## How it works

1. WebToApp builds the APK (showing a "building" notice).
2. On success, it reports the incremental build mode used.
3. The system share sheet opens with the APK attached, ready to send to any app.

## If it fails

A diagnostic report is shown with:

- the failure stage and cause,
- the project details,
- the build-log tail,
- recent logs.

You can copy the report for troubleshooting.

## Notes

- To build without sharing, use [Build APK](/guide/app-actions/build-apk).
- To export a reusable project instead of an APK, use [Export](/guide/app-actions/export-apk).
