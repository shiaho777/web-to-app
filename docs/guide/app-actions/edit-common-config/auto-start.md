# Auto-start

Lets the generated app start automatically (for example on device boot) or on a schedule.

**Where:** the **Auto-start** card in the [Edit Common Config](/guide/app-actions/edit-common-config/) editor (backed by `AutoStartConfig`).

## Options

- **Boot auto-start** — launch the app when the device boots.
- **Scheduled launch** — start the app on a schedule.
- **Background-run service** — keep a service running (related to the background-run option in [APK Export Config](/guide/app-actions/edit-common-config/apk-export)).

## Notes

- Auto-start relies on the system allowing the app to start; some OEMs restrict background starts.
- Notification channels used by background services are created fail-soft so channel failures don't crash startup.
