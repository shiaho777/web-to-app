# Browser Toolbar

Controls the in-app browser toolbar (the bar with back/forward/refresh/title/URL).

**Where:** the **Browser toolbar** card in the [Edit Common Config](/guide/app-actions/edit-common-config/) editor.

## Options

- **Browser toolbar** — the master switch (`browserToolbarEnabled`). **Off by default**: the page renders with no toolbar at all, fullscreen-like. Turning it on shows the toolbar with **every item on**; trim individual buttons from there. Turning it off again flips every item off — a re-enable always starts from the all-on slate.
- **Toolbar items** — while the toolbar is enabled, each button has its own switch:
  - show title (`toolbarShowTitle`)
  - show URL (`toolbarShowUrl`)
  - show back (`toolbarShowBack`)
  - show forward (`toolbarShowForward`)
  - show refresh (`toolbarShowRefresh`)
  - show console button (`toolbarShowConsole`)
  - show find-in-page button (`toolbarShowFind`)

A toolbar with only some items still renders (e.g. console alone); the preview and the exported APK share the same visibility rule, so what you trim is what ships.

## Runtime toolbar panels

These panels open **from the toolbar at runtime** (inside the generated APK or the host preview). Their toolbar buttons are gated by the build-time items above — hide the item and the button disappears:

- **Console** — a toolbar button opens a console panel showing `console.log` / error output. Useful for debugging the loaded page at runtime.
- **Find in page** — a toolbar button opens a native bottom search bar with live match counting and previous/next navigation, powered by the WebView engine (`findAllAsync`). The keyboard is raised automatically when the bar opens.

Page zoom is no longer a toolbar action — it is a build-time setting under [Advanced Settings](/guide/app-actions/edit-common-config/advanced-settings).

## Notes

- For hiding the system status/navigation bars, see [Fullscreen Mode](/guide/app-actions/edit-common-config/fullscreen). Fullscreen does not hide this toolbar.
- The bar is one row. The title and the address share it. Multi-web drawer mode puts its site menu on this row instead of adding another bar.
- A floating back button can be enabled under [Special Settings](/guide/app-actions/edit-common-config/special-settings) — handy when the toolbar (and its back button) is hidden.
