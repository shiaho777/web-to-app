# GitHub Actions APK Build Guide (Cloud Build)

This project can build APKs online via GitHub Actions, so your laptop does not do heavy build work.

Workflow file:
- `.github/workflows/android-apk-build.yml`

## Verified Result (Your Latest Run)

- Workflow run: `Build APK #1`
- Status: `succeeded`
- Total time: `12m 44s`
- Output quality check: APK installed and all expected NativeBridge methods worked in APK Inspector Pro.

## 1) Start a Build

1. Open `Actions` tab in your repo.
2. Click workflow: **Build APK**.
3. Click **Run workflow**.
4. Choose build type:
   - `debug`
   - `release`
   - `both`
5. Click **Run workflow**.

## 2) Download Built APK

After the run is green/success:

1. Open that run.
2. Scroll to **Artifacts**.
3. Download:
   - `app-debug-apk-<run_number>` for debug build
   - `app-release-apk-<run_number>` for release build

## 3) What Your Current Logs Mean

If you see many lines starting with `w:`:
- Those are compiler warnings.
- Warnings are not build failures.
- Build only fails on `FAILURE: Build failed with an exception` or a red failed step.

If you see tasks like:
- `:shell:minifyReleaseWithR8`
- `:app:assembleDebug`

That is normal for this repo. Even debug builds may trigger shell/template release tasks because of project build wiring.

## 4) Typical Cloud Build Time

On GitHub-hosted runners for this project:

1. First run on a fresh branch:
   - Usually `12-45 min`
2. Later runs (same workflow, similar code):
   - Usually `10-30 min`
3. If `release` or `both` selected:
   - Can be longer (`20-60 min`)

Time depends on:
1. GitHub runner queue
2. Dependency download time
3. Native/NDK/CMake steps

## 5) Build Status Quick Meanings

1. `queued` = waiting for runner
2. `in_progress` = building
3. `completed + success` = APK artifact ready
4. `completed + failure` = open failed step logs

## 6) If Build Fails

1. Open failed step logs.
2. Check first actual `error:` line (ignore warnings first).
3. Re-run failed jobs once (temporary network issues are common).
4. If still failing, fix code/workflow and push again.

## 7) Cost Notes

For public repos, standard GitHub-hosted runner usage is free (per GitHub Actions billing docs at the time of writing).

## 8) Methods Confirmed Working In Built APK

Security:
- `isDeveloperOptionsEnabled`
- `isAdbEnabled`
- `isDebuggable`
- `getSecurityInfo`

Notification/runtime:
- `requestNotificationPermission`
- `areNotificationsEnabled`
- `openNotificationSettings`
- `createNotificationChannel`
- `showNotification`
- `scheduleNotification`
- `cancelNotification`
- `cancelAllNotifications`
- `startForegroundService`
- `stopForegroundService`
- `scheduleWorker`
- `scheduleExactAlarm`
- `canScheduleExactAlarms`
- `isDozeMode`
- `isIgnoringBatteryOptimizations`
- `openBatteryOptimizationSettings`
- `getAppState`
- `isAppInForeground`
