# Export source

Packages the app's source and opens the system share sheet so you can save or send the zip.

## Where

- On the home screen, tap ⋮ on an app card, then **Export source**.
- On the build page, after a successful build, tap **Export source** on the build summary.

## What is in the zip

- `README.md` explains the archive.
- `app_config.json` is the runtime configuration embedded in the APK. An encrypted build stores that file as ciphertext inside the APK; this copy is plaintext.
- `network_security_config.xml` and `certs/` are the network-trust settings, including what you need for full certificate-chain validation (root and intermediate).
- `content/` holds local HTML, frontend, gallery, splash, and audio files that belong to the app. `node_modules` and `.git` are left out.

## What it is not

The installable file is still the [built APK](/guide/app-actions/build-apk) or a [Play AAB](/guide/more-features/google-play). This zip is the definition those builds run: the WebToApp shell plus these files. It is not an Android Studio project, and it does not include the signing keystore.

The archive can include activation secrets and proxy credentials you configured.

## Moving apps between devices

Use [Data Backup](/guide/more-features/data-backup) to move the whole workspace. The source zip is the readable definition of one app, not a restore package.
