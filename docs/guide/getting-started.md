# Getting Started

This walkthrough takes you from a fresh install to your first signed APK in a few minutes.

## 1. Install WebToApp

Install the WebToApp builder app on an Android device running **Android 6.0 (API 23) or newer**. You can build it from source (see [Build from source](#build-from-source)) or install a provided APK.

On first launch you land on **My Apps**, the home screen that lists every app you create.

## 2. Create your first app

Tap the **create** action and pick an app type. For your first app, choose **Web** and enter a URL:

1. **App type** → `Web`
2. **Target URL** → e.g. `https://example.com`
3. **App name** → anything you like
4. **Icon** → pick an image (optional; a default is used otherwise)

The editor opens with sensible defaults. Everything here can be changed later.

## 3. Preview

Tap **Preview** to run the app inside the builder, exactly as it will behave once exported. Preview runs the full runtime on the host classpath, so it is a faithful check of WebView behavior, injected scripts, and runtime servers.

::: warning Preview ≠ export
Preview and export share the same runtime code, but export additionally serializes your configuration into the generated APK. If a feature works in preview but not after export, a config field likely did not flow through the export chain. See [Config Field Drift](/developer/config-drift).
:::

## 4. Build the APK

Tap **Build APK**. WebToApp will:

1. Take the shell template (`webview_shell.apk`).
2. Patch its binary manifest and resources (package name, icon, label, permissions).
3. Embed your configuration and app content into assets.
4. Sign the result (V1/V2/V3 — configurable).

When it finishes, the APK appears in the **File Manager**, ready to install or share.

## 5. Install and test

Install the generated APK on your device (or share it to another device). Launch it — it runs the shell runtime reading *your* embedded configuration, independent of the builder.

## Next steps

- Learn what each [app type](/guide/create-app) can do.
- Configure [signing and Play Store export](/guide/build-export).
- Harden networking with [DoH, proxies, and TLS fingerprinting](/guide/network).
- Run [Node.js / PHP / Python / Go / WordPress](/guide/runtimes) on-device.

## Build from source

Requirements: Android Studio Hedgehog or newer, JDK 17. The Gradle wrapper pins Gradle 9.4.1.

```bash
git clone https://github.com/shiaho777/web-to-app.git
cd web-to-app
./gradlew assembleDebug
```

For release builds, configure signing through `local.properties` and `app/build.gradle.kts`.
