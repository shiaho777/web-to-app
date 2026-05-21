# How To Convert To APK

Project path:
`C:\phpserver\www\web-to-app`

## Verified Status (2026-05-18)

Cloud build succeeded on GitHub Actions in `12m 44s`, and generated APK was validated in APK Inspector Pro with:

1. Security methods working
2. Notification methods working
3. Scheduling/worker/alarm methods working
4. Runtime state methods working

## 1. Install Required Tools

1. Install Android Studio (latest stable).
2. Install Git.
3. Use JDK 17 (Android Studio usually includes it).

## 2. Open The Project

1. Open Android Studio.
2. Click `Open`.
3. Select: `C:\phpserver\www\web-to-app`.
4. Wait for Gradle sync and indexing to complete.

## 3. Configure SDK (If Needed)

If build fails with "SDK location not found", create/edit:
`C:\phpserver\www\web-to-app\local.properties`

Add:

```properties
sdk.dir=C\:\\Users\\Yasin\\AppData\\Local\\Android\\Sdk
```

## 4. Build APK In Android Studio

1. Go to `Build`.
2. Click `Build Bundle(s) / APK(s)`.
3. Click `Build APK(s)`.
4. Wait until build finishes.

## 5. Build APK In Terminal (Optional)

Open terminal in project folder and run:

```powershell
cd C:\phpserver\www\web-to-app
.\gradlew.bat :app:assembleDebug
```

For release build:

```powershell
.\gradlew.bat :app:assembleRelease
```

## 6. Output APK Location

Debug APK:
`C:\phpserver\www\web-to-app\app\build\outputs\apk\debug\app-debug.apk`

Release APK:
`C:\phpserver\www\web-to-app\app\build\outputs\apk\release\app-release.apk`

## 7. Install APK On Phone

Using ADB:

```powershell
adb install -r C:\phpserver\www\web-to-app\app\build\outputs\apk\debug\app-debug.apk
```

Or copy APK to your phone and install manually.

## 8. If Build Fails

1. In Android Studio, run `File` -> `Invalidate Caches / Restart`.
2. Re-sync Gradle.
3. Install missing SDK/NDK/CMake from `SDK Manager`.
4. Make sure enough disk space is available (25 GB+ recommended).

## 9. Recommended Build Path For Low-End PCs

Use GitHub Actions build instead of local build:

1. Push your branch to GitHub.
2. Open `Actions` -> `Build APK`.
3. Run workflow (`debug`, `release`, or `both`).
4. Download artifact APK from the successful run.
