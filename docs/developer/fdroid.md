# F-Droid flavor

WebToApp ships one source tree for GitHub, Play, and F-Droid. The F-Droid build is a Gradle property, not a long-lived git branch.

```bash
./gradlew :app:assembleStandardRelease -Pfdroid=true
```

Output: `app/build/outputs/apk/standard/release/app-standard-release-unsigned.apk`. F-Droid signs it. Do not pass `-Pfdroid` in the same Gradle invocation as a normal release — there is one shell template per invocation.

## What the flag does

| Piece | `-Pfdroid=true` |
| --- | --- |
| Firebase Cloud Messaging | Omitted. Push still works through the Web Notification polyfill, HTTP polling, and WebSocket. |
| Google Sign-In (Play Services / Credential Manager) | Omitted. `NativeBridge.googleSignIn` returns `UNAVAILABLE`. |
| AdMob feature stack | The `:feature-stacks:admob` module is not included. Editor ads UI is hidden. Export writes a disabled ads block. |
| Play Services FIDO (GeckoView WebAuthn) | Excluded. Other GeckoView paths are unchanged. |
| Release signing | `signingConfig` is null so the APK stays unsigned. |
| Host editor | FCM / Google Sign-In / AdMob entries are hidden. Saved config fields stay in the schema so export no-ops instead of forking JSON. |

`BuildConfig.FDROID` is `true` in both `:app` and `:shell`. Google-typed sources live in `app/src/google/java`; the F-Droid twins with the same FQCN live in `app/src/fdroid/java`. Shell sync copies one of those two trees.

## CI

The `check` job compiles this flavor in a **second** Gradle invocation so the flag cannot contaminate the normal build:

```text
./gradlew -PskipShellTemplateSync=true -Pfdroid=true \
  :app:compileStandardReleaseKotlin \
  :shell:compileReleaseKotlin \
  :app:processStandardReleaseManifest \
  :shell:processReleaseManifest
```

## Store listing copy

Fastlane files live at `fastlane/metadata/android/<locale>/`. F-Droid reads those from this repository.

The fdroiddata recipe to copy into a [fdroiddata](https://gitlab.com/fdroid/fdroiddata) merge request is `metadata/com.webtoapp.yml` at the repo root of **this** project (path `metadata/com.webtoapp.yml`). MR title: `New app: WebToApp`.

```yaml
Categories:
  - Development
  - Internet
License: Unlicense
SourceCode: https://github.com/shiaho777/web-to-app
IssueTracker: https://github.com/shiaho777/web-to-app/issues
Changelog: https://github.com/shiaho777/web-to-app/releases
WebSite: https://shiaho777.github.io/web-to-app/

RepoType: git
Repo: https://github.com/shiaho777/web-to-app.git

Builds:
  - versionName: "2.7.9"
    versionCode: 77
    commit: v2.7.9
    subdir: app
    gradle:
      - standard
    gradleprops:
      - fdroid=true
    ndk: r28c
    rm:
      - feature-stacks/admob
      - app/src/google

AutoUpdateMode: Version
UpdateCheckMode: Tags
UpdateCheckData: app/build.gradle.kts|versionCode = (\d+)|.|versionName = "([^"]+)"
```

Tag the same commit that GitHub Releases use (`v2.7.9`). F-Droid and GitHub then ship the same version from one tag; F-Droid's signing key is different, so the two APKs cannot update each other.

## Do not mix templates

A `-Pfdroid=true` assemble rebuilds `webview_shell.apk` without Firebase and copies it into `app/src/main/assets/template/`. Restore that file from git before a normal GitHub/Play template build, or run `:shell:assembleRelease :app:syncShellTemplateApk` **without** `-Pfdroid`.
