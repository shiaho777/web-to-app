# Fullscreen Mode

Runs the app immersive, hiding the system bars and (optionally) working together with the browser-toolbar setting.

**Where:** the **Fullscreen mode** card in the [Edit Common Config](/guide/app-actions/edit-common-config/) editor.

## Options

- **Fullscreen** — enable immersive fullscreen (`hideToolbar`).
- **Always show status bar in fullscreen** — keep the top status bar persistently visible (`showStatusBarInFullscreen`). Persistent mode never auto-hides.
- **Show navigation bar in fullscreen** — keep the bottom navigation bar visible (`showNavigationBarInFullscreen`).
- **Display cutout** — choose whether fullscreen content extends into the notch / punch-hole area (`displayCutoutMode`): *Extend into cutout* (`SHORT_EDGES`, the default and previous behavior), *Use system default* (`DEFAULT`), or *Avoid cutout area* (`NEVER`). Applies to WebView-based app types on Android 9 (API 28) and newer; earlier devices have no cutout to avoid.
- **Fullscreen content padding** — inset content by a number of dp (`fullscreenContentPaddingDp`).
- **Customize status bar** — an expandable sub-panel (`statusBarCustomizeLabel`) with light/dark tabs: color mode (`THEME`/`PAGE_TOP`/`TRANSPARENT`/`CUSTOM`), custom color, dark icons, and background (color/image) for each mode. `TRANSPARENT`/image bars overlay content instead of reserving space.

## Interaction with the browser toolbar

Fullscreen and the [browser toolbar](/guide/app-actions/edit-common-config/browser-toolbar) are independent. Fullscreen hides the system status and navigation bars. The toolbar stays visible whenever its own switch is on, including in fullscreen. It is off by default, so a freshly exported app has no toolbar in or out of fullscreen. The bar is a single row: the title and the address share that line. In multi-web drawer mode the site menu joins this bar instead of adding a second one.

## Safe area insets in web content

When fullscreen content draws into the notch or punch-hole area, WebToApp publishes the measured display-cutout insets to the page as CSS custom properties:

- `--wta-safe-area-inset-top`
- `--wta-safe-area-inset-right`
- `--wta-safe-area-inset-bottom`
- `--wta-safe-area-inset-left`

They are available before the page's first paint on System WebView builds and refresh when the window insets change (rotation, system-bar visibility, fullscreen transitions). Values are CSS pixels converted from platform pixels using the display density.

```css
.app-header {
  padding-top: var(--wta-safe-area-inset-top, 0px);
}
```

These are **not** `env(safe-area-inset-*)`. Android WebView does not reliably populate those variables, and where newer Chromium does, they describe the browser's gesture bar rather than the display cutout. WebToApp neither injects `viewport-fit=cover` nor rewrites the page's viewport meta, so existing CSS keeps working unchanged.

- With **Avoid cutout area** (`NEVER`) all four values are `0px`: the window is already outside the cutout region, and applying a second inset would double the offset.
- The values describe the display cutout only, not the status or navigation bars. Use the status-bar and fullscreen padding options for those.
- Non-zero values require Android 9 (API 28) or newer. Older devices and GeckoView-based pages keep the `0px` fallback.

## Notes

- If the [floating window](/guide/app-actions/edit-common-config/floating-window) is also on, the app opens as that window. Fullscreen does not replace it.
- When the status bar is visible, the splash countdown/skip chip sits below it so it is never covered.
- The bottom content padding stays on the physical screen edge. Opening the keyboard keeps only the part of that band the keyboard does not cover. Once the keyboard is taller than the band, the page sits flush on the keyboard.
- Transparent and follow-page modes sample the system WebView and paint the navigation bar and the fullscreen margin bands from the page. The first paint is sampled again, so the first screen is not left on the light fallback. Theme mode paints the status bar in the interface color, including dark mode. Custom mode keeps the color you set. GeckoView cannot return that sample, so it keeps the previous bar colors.
- Top and bottom tab pages use the fullscreen content padding once. Cards, the drawer, and the feed already did.
- Fullscreen **video** orientation (landscape/sensor for fullscreen video playback, `fullscreenVideoOrientation`) is configured under [Special Settings](/guide/app-actions/edit-common-config/special-settings). The same card also has **hide status bar in video fullscreen** (`hideStatusBarInVideoFullscreen`, on by default), which force-hides the status bar while a web video is in HTML5 fullscreen even when "show status bar in fullscreen" is on.
- Keyboard-avoidance behavior below Android 11 uses the classic window-resize path; see [Special Settings](/guide/app-actions/edit-common-config/special-settings) for the keyboard adjust mode.
- **Avoid cutout area** keeps the whole window out of the notch region. This is the reliable fix for pages whose top buttons are obscured: CSS `env(safe-area-inset-*)` is not populated reliably by Android WebView, and the fullscreen content padding above is a fixed dp value rather than a measured cutout inset. Media and gallery player screens keep their own full-bleed behavior.
- Android 15 (API 35) and later enforce edge-to-edge, and the opt-out attribute is disabled for apps targeting Android 16. WebToApp already calls `enableEdgeToEdge()` and disables the status-bar contrast scrim, so the window layer is configured correctly. The WebView container itself still has no measured system-bar padding — the cutout mode and the CSS variables above are the two supported ways to keep web content clear of those regions.
