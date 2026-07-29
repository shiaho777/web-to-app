# Splash Animation

Shows an image or video splash screen when the app launches.

**Where:** the **Splash screen** card in the [Edit Common Config](/guide/app-actions/edit-common-config/) editor.

## Options

- **Enable** — turn the splash on (`splashEnabled`).
- **Media** — pick an image (`image/*`) or a video (`video/*`).
- **Duration** — how long the splash shows (`splashConfig.duration`).
- **Click to skip** — allow tapping to skip, with a countdown (`clickToSkip`).
- **Orientation** — splash orientation (`orientation`).
- **Fill screen** — crop to fill vs fit (`fillScreen`).
- **Enable audio** — for video splashes (`enableAudio`).
- **Video trim** — set start/end trim range for a video splash (`videoStartMs`/`videoEndMs`).
- **Clear media** — remove the selected splash media.

## Notes

- When the status bar is visible, the countdown/skip chip automatically sits below it so it is never covered; in pure fullscreen it stays at the top corner.
- Splash media is packaged into the exported APK (and can be encrypted).
