# Update And Rebuild Flow (After New Changes)

Use this every time you add/fix features and want a fresh APK from cloud CI.

## Current Proven Baseline (2026-05-18)

Validated in APK Inspector Pro:

1. Native security detection works (`isDeveloperOptionsEnabled`, `isAdbEnabled`, `isDebuggable`, `getSecurityInfo`).
2. Notification flow works (foreground, sound, action/deep link, scheduled notification).
3. Worker/alarm scheduling works (`scheduleWorker`, `scheduleExactAlarm`).
4. Runtime checks work (`getAppState`, `isAppInForeground`, doze/battery methods).
5. Bridge method count seen: `56`.

## A) Normal Update Flow

1. Make code changes locally.
2. Commit changes.
3. Push to your branch.
4. Open GitHub `Actions` tab.
5. Run **Build APK** workflow manually (`workflow_dispatch`).
6. Download artifact APK after success.
7. Install and test on phone.

## B) Recommended Branching Pattern

1. Keep one stable branch for building:
   - `codex/nativebridge-runtime-notification-bridge`
2. For risky edits, create temporary feature branch:
   - `feature/<name>`
3. After testing, merge back to stable build branch.
4. Run workflow on stable branch for cleaner artifacts.

## C) Versioning Suggestion

Before each cloud build you share externally:

1. Update version fields in app config if needed.
2. Use commit message like:
   - `build: add X + fix Y`
3. Tag important builds:
   - `v1-test-<date>`

This helps track which APK includes which features.

## D) Fast Rebuild Checklist

Before clicking Run workflow:

1. Confirm correct branch selected in Actions UI.
2. Pick right build type:
   - `debug` for testing
   - `release` for distribution checks
3. Confirm required files are pushed.

## E) How To Handle Failures On Future Updates

1. Re-run the job once.
2. If still failing, inspect:
   - SDK install step
   - Gradle compile step
   - R8/minify step
3. Fix and push.
4. Run workflow again.

## F) Practical Time Expectations Per Update

1. Small JS/UI/native patch + debug:
   - `15-30 min`
2. Larger Kotlin/native changes:
   - `20-45 min`
3. Release with minify:
   - `30-75 min`

## G) Keep Your Laptop Light

You can do this with minimal local load:

1. Edit code
2. Commit + push
3. Build in GitHub Actions
4. Download APK artifact

No local SDK/NDK setup required for this cloud build flow.

## H) What To Do Next

1. Run a `release` cloud build and verify the same tests again.
2. Comment on upstream PR with your successful APK Inspector results and method count.
3. Ask maintainer to merge and publish a release/test APK.
4. After merge, rebase your branch and keep only forward changes.
