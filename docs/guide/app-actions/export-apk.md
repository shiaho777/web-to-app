# Export source

Packages a compilable Android Studio project for the app and opens the system share sheet so you can save or send the zip.

## Where

- On the home screen, tap ⋮ on an app card, then **Export source**.
- On the build page, after a successful build, tap **Export source** on the build summary.

## What is in the zip

Open the folder in Android Studio to sync and build it.

- `settings.gradle.kts`, `build.gradle.kts`, and `app/build.gradle.kts` are the Gradle project.
- `app/src/main/java/.../MainActivity.kt` opens the app's page or local files in a WebView.
- `app/src/main/AndroidManifest.xml` holds the package name, permissions, and icon.
- `app/src/main/assets/app_config.json` is the runtime configuration embedded in a WebToApp APK. An encrypted build stores that file as ciphertext inside the APK; this copy is plaintext.
- `app/src/main/res/xml/network_security_config.xml` and `res/raw/` are the network-trust settings.
- `app/src/main/assets/www/` and `assets/files/` hold local HTML, frontend, gallery, splash, and audio. `node_modules` and `.git` are left out.

## What it is not

The installable file from WebToApp is still the [built APK](/guide/app-actions/build-apk) or a [Play AAB](/guide/more-features/google-play). This project builds a WebView app from the same name, package, permissions, and local files. It does not include the signing keystore or the full WebToApp shell runtime.

The archive can include activation secrets and proxy credentials you configured.

## Moving apps between devices

Use [Data Backup](/guide/more-features/data-backup) to move the whole workspace. The source zip is one app's project, not a restore package.
