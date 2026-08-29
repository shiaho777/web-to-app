# Getting Started

This walkthrough takes you from a fresh install to your first signed APK. At each step it notes what the app is actually doing under the hood, so the flow makes sense in terms of the code.

## 1. Install and launch

Install the WebToApp builder on a device running **Android 6.0 (API 23) or newer**.

<div class="wta-install">

**Download the builder**

<LatestRelease variant="install" />

Get the APK from [GitHub Releases](https://github.com/shiaho777/web-to-app/releases) and install it like any other APK. The button auto-detects the newest version.

</div>

On launch, `WebToAppApplication` starts in **builder mode** (`SHELL_RUNTIME_ONLY = false`): it initializes the i18n strings, the Room database, and the dependency graph, then shows **My Apps** — the home screen that lists every app you create. See [Main Screen](/guide/main-screen/my-apps).

## 2. Create an app definition

At the bottom of **My Apps**, tap **Create**. A panel expands with a 3-column grid of the 12 [app types](/guide/app-types/):

**Web · Multi-Web · HTML · Offline Pack · Frontend · PHP · WordPress · Node.js · Python · Go · Media · Gallery**

Tap **Web** for your first app. The Web editor opens.

## 3. Fill in the basics

Fill in the top **Basic info** card:

- **App name**
- **Target URL** — e.g. `https://example.com`
- **Icon** — optional; a type-specific default is used otherwise

The rest of the editor is a long list of optional capability cards (fullscreen, splash, ad blocking, DNS, disguise, …). Ignore them for now — defaults are used. Each is covered under [App Configuration](/guide/config/).

Tap **Save**. Under the hood, the editor assembles a `WebApp` object (with `appType = WEB` and a `webViewConfig`) and writes it to the `web_apps` Room table. Your app now appears in the list.

## 4. Preview

On **My Apps**, tap your app's card. The preview router checks the app's `appType` and launches the matching runtime:

- `IMAGE` / `VIDEO` → the media player activity
- `GALLERY` → the gallery player activity
- everything else (including `WEB`) → the WebView activity

For a Web app, the WebView activity loads your URL with your configured settings — the same code the exported app will run. (Tap the card's ⋮ button instead to open the [action menu](/guide/app-actions/edit-core-config).)

::: warning Preview ≠ export
Preview runs the **host** path (everything on the builder's classpath). Export runs the **shell** path, reading your config from an embedded JSON. A feature can work in preview yet vanish after export if a config field doesn't survive that trip. See [Config Field Drift](/developer/config-drift).
:::

## 5. Build the APK

Tap ⋮ on your app's card, then **Build APK**. In the dialog you can:

- pick the **browser engine** (System WebView or GeckoView),
- optionally enable **resource encryption**, **isolation**, **background run**, and **notifications**,
- force a **full rebuild** (otherwise an incremental mode is chosen automatically).

Tap build. The `ApkBuilder` takes the shell template APK, patches its package name / icon / permissions, embeds your `WebApp` config as `app_config.json`, and signs the result (V1/V2/V3). See [Build APK](/guide/app-actions/build-apk).

## 6. Install it

Open **⋮ → [File Manager](/guide/more-features/file-manager)** from the top-right of My Apps. Your APK is there — install it or share it. When you launch it, that APK runs in **shell mode** (`SHELL_RUNTIME_ONLY = true`): `ShellModeManager` reads *your* embedded `app_config.json` and drives the runtime, fully independent of the builder.

## Next steps

- Tour the [Main Screen](/guide/main-screen/my-apps).
- Learn what each [app type](/guide/app-types/) does.
- Explore the per-app [App Actions](/guide/app-actions/edit-core-config).
- Open the top-right **⋮** menu — see [More Features](/guide/more-features/agent).

## Build from source

Requirements: Android Studio Hedgehog or newer, JDK 17. The Gradle wrapper pins Gradle 9.4.1.

```bash
git clone https://github.com/shiaho777/web-to-app.git
cd web-to-app
./gradlew assembleDebug
```

For release builds, configure signing through `local.properties` and `app/build.gradle.kts`.
