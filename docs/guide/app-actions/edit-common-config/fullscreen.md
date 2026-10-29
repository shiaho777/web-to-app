# Fullscreen Mode

Runs the app immersive, hiding the system bars and (optionally) working together with the browser-toolbar setting.

**Where:** the **Fullscreen mode** card in the [Edit Common Config](/guide/app-actions/edit-common-config/) editor.

## Options

- **Fullscreen** — enable immersive fullscreen (`hideToolbar`).
- **Always show status bar in fullscreen** — keep the top status bar persistently visible (`showStatusBarInFullscreen`). Persistent mode never auto-hides.
- **Show navigation bar in fullscreen** — keep the bottom navigation bar visible (`showNavigationBarInFullscreen`).
- **Fullscreen content padding** — inset content by a number of dp (`fullscreenContentPaddingDp`).
- **Customize status bar** — an expandable sub-panel (`statusBarCustomizeLabel`) with light/dark tabs: color mode (`THEME`/`PAGE_TOP`/`TRANSPARENT`/`CUSTOM`), custom color, dark icons, and background (color/image) for each mode. `TRANSPARENT`/image bars overlay content instead of reserving space.

## Interaction with the browser toolbar

Fullscreen and the [browser toolbar](/guide/app-actions/edit-common-config/browser-toolbar) are independent. Fullscreen hides the system status and navigation bars. The toolbar stays visible whenever its own switch is on, including in fullscreen. It is off by default, so a freshly exported app has no toolbar in or out of fullscreen. The bar is a single row: the title and the address share that line. In multi-web drawer mode the site menu joins this bar instead of adding a second one.

## Notes

- If the [floating window](/guide/app-actions/edit-common-config/floating-window) is also on, the app opens as that window. Fullscreen does not replace it.
- When the status bar is visible, the splash countdown/skip chip sits below it so it is never covered.
- The bottom content padding stays on the physical screen edge. Opening the keyboard keeps only the part of that band the keyboard does not cover. Once the keyboard is taller than the band, the page sits flush on the keyboard.
- Transparent and follow-page modes sample the system WebView and paint the navigation bar and the fullscreen margin bands from the page. The first paint is sampled again, so the first screen is not left on the light fallback. Theme mode paints the status bar in the interface color, including dark mode. Custom mode keeps the color you set. GeckoView cannot return that sample, so it keeps the previous bar colors.
- Top and bottom tab pages use the fullscreen content padding once. Cards, the drawer, and the feed already did.
- Fullscreen **video** orientation (landscape/sensor for fullscreen video playback, `fullscreenVideoOrientation`) is configured under [Special Settings](/guide/app-actions/edit-common-config/special-settings). The same card also has **hide status bar in video fullscreen** (`hideStatusBarInVideoFullscreen`, on by default), which force-hides the status bar while a web video is in HTML5 fullscreen even when "show status bar in fullscreen" is on.
- Keyboard-avoidance behavior below Android 11 uses the classic window-resize path; see [Special Settings](/guide/app-actions/edit-common-config/special-settings) for the keyboard adjust mode.
