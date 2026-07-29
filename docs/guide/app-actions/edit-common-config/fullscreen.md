# Fullscreen Mode

Runs the app immersive, hiding the system bars and optionally the browser toolbar.

**Where:** the **Fullscreen mode** card in the [Edit Common Config](/guide/app-actions/edit-common-config/) editor.

## Options

- **Fullscreen** — enable immersive fullscreen (`hideToolbar`).
- **Show status bar in fullscreen** — keep the top status bar visible (`showStatusBarInFullscreen`).
- **Show navigation bar in fullscreen** — keep the bottom navigation bar visible (`showNavigationBarInFullscreen`).
- **Hide browser toolbar in fullscreen** — hide the in-app toolbar while fullscreen (derived from `showToolbarInFullscreen`).
- **Fullscreen content padding** — inset content by a number of dp (`fullscreenContentPaddingDp`).
- **Fullscreen video orientation** — how fullscreen video orients (`fullscreenVideoOrientation`, e.g. auto sensor landscape).

## Notes

- When the status bar is visible, the splash countdown/skip chip sits below it so it is never covered.
- Status bar color/appearance is configured under [Advanced Settings](/guide/app-actions/edit-common-config/advanced-settings).
