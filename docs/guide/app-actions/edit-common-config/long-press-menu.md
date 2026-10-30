# Long-press Menu

Configures the context menu shown when long-pressing page content.

**Where:** the **Long-press menu** card in the [Edit Common Config](/guide/app-actions/edit-common-config/) editor.

## Options

- **Style** (`longPressMenuStyle`):
  - `DISABLED` — no long-press menu (also disables it)
  - `SIMPLE`
  - `FULL`
  - `IOS`
  - `FLOATING`
  - `CONTEXT`
- **No haptic on an empty long-press** (`longPressHapticOnActionOnly`): off by default. This row stays visible when the menu itself is off. When it is on, a long-press that opens this menu vibrates when the menu appears. A long-press the page handles, such as selecting text or opening the page's own menu, still vibrates. A long-press that does neither stays quiet. The option applies to the system WebView in preview and in the built APK.

## Notes

- Choosing any style other than `DISABLED` enables the long-press menu (`longPressMenuEnabled`).
