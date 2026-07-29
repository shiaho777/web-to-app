# Keep Screen On

Prevents the screen from turning off while the app runs, with optional timeout and brightness control.

**Where:** the **Keep screen on** card in the [Edit Common Config](/guide/app-actions/edit-common-config/) editor.

## Options

- **Screen-awake mode** (`screenAwakeMode`):
  - `OFF` — normal system behavior
  - `ALWAYS` — keep the screen on indefinitely
  - `TIMED` — keep on for a set duration
- **Timeout (minutes)** — for `TIMED` mode, how long to keep the screen on (`screenAwakeTimeoutMinutes`).
- **Screen brightness** — override brightness while running (`screenBrightness`; `-1` leaves it unchanged).

## Notes

- Enabling any non-`OFF` mode sets `keepScreenOn`.
- Media apps have their own keep-screen-on toggle (see the [Media](/guide/app-types/media) type).
