# Getting Started

This walkthrough takes you from a fresh install to your first signed APK, following the actual flow in the app.

## 1. Install WebToApp

Install the WebToApp builder on an Android device running **Android 6.0 (API 23) or newer**. On first launch you land on **My Apps** — the home screen that lists every app you create. See [Main Screen](/guide/main-screen/my-apps) for a tour of it.

## 2. Open the create menu

At the bottom of **My Apps**, tap the **Create** button. A panel expands with a 3-column grid of app types:

**Web · Multi-Web · HTML · Offline Pack · Frontend · PHP · WordPress · Node.js · Python · Go · Media · Gallery**

For your first app, tap **Web**.

## 3. Fill in the basics

The Web editor opens. Fill in the top **Basic info** card:

- **App name** — anything you like
- **Target URL** — e.g. `https://example.com`
- **Icon** — pick an image (optional; a type-specific default is used otherwise)

The rest of the editor is a long list of optional capability cards (fullscreen, splash screen, ad blocking, DNS, fingerprint disguise, and more). You can ignore all of them for now — sensible defaults are used. Each is explained under [App Configuration](/guide/config/).

Tap **Save**. Your app now appears in the list on **My Apps**.

## 4. Preview

On **My Apps**, tap your app's card. WebToApp launches it in preview, exactly as it will behave once exported. (Tap the ⋮ button on the card instead to open the action menu — see [App Actions](/guide/app-actions/edit-core-config).)

::: warning Preview ≠ export
Preview and export share the same runtime code, but export additionally serializes your configuration into the generated APK. If a feature works in preview but not after export, a config field likely did not flow through the export chain. See [Config Field Drift](/developer/config-drift).
:::

## 5. Build the APK

Back on **My Apps**, tap the ⋮ button on your app's card, then **Build APK**. In the dialog you can:

- pick the **browser engine** (System WebView or GeckoView),
- optionally enable **resource encryption**, **isolation**, **background run**, and **notifications**,
- force a **full rebuild** (otherwise an incremental mode is chosen automatically).

Tap build. WebToApp patches the shell template, embeds your config and content, and signs the result. When it finishes, the APK is ready.

## 6. Find and install it

Open **⋮ → File Manager** from the top-right of **My Apps**. Your build output is there — install it on your device or share it to another device. Launch it: it runs the shell runtime reading *your* embedded configuration, independent of the builder.

## Next steps

- Tour the [Main Screen](/guide/main-screen/my-apps).
- Learn what each [app type](/guide/app-types/) can do.
- Explore the per-app [App Actions](/guide/app-actions/edit-core-config) (shortcut, share, export, AAB).
- Open the top-right **⋮** menu — see [More Features](/guide/more-features/ai-coding).

## Build from source

Requirements: Android Studio Hedgehog or newer, JDK 17. The Gradle wrapper pins Gradle 9.4.1.

```bash
git clone https://github.com/shiaho777/web-to-app.git
cd web-to-app
./gradlew assembleDebug
```

For release builds, configure signing through `local.properties` and `app/build.gradle.kts`.
