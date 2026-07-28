# CSS Modules

A CSS module is a pure style override — theming, restyling, or a dark mode for a site. It uses the same `module.json` manifest as a [JS module](/extensions/js-module), but the substance is the stylesheet.

## File layout

```
my-theme/
├── module.json    # required
├── main.js        # required — can be a near-empty stub
├── style.css      # the actual styles
└── icon.png       # optional
```

::: info A `main.js` is still required
Even a pure CSS module needs a `main.js` (it can be a minimal stub). Set `runAt` to `DOCUMENT_START` so your styles apply as early as possible and avoid a flash of unstyled content.
:::

## `module.json`

```json
{
  "id": "dark-reader-lite",
  "name": "Dark Reader Lite",
  "description": "A simple dark theme",
  "category": "THEME",
  "runAt": "DOCUMENT_START",
  "urlMatches": [
    { "pattern": "*://news.ycombinator.com/*" }
  ],
  "permissions": ["CSS_INJECT"]
}
```

Use `category: "STYLE_MODIFIER"` or `"THEME"`. The market's `hasCss` flag must be `true` and a `style.css` must be present — the validator checks that these agree.

## How CSS is injected

When a module has CSS (`cssCode` / `style.css`), it is injected as a `<style id="ext-module-<id>">` element before the JS runs. Your `main.js` can still manipulate the DOM if needed.

## Example `style.css`

```css
:root {
  color-scheme: dark;
}
body {
  background: #111 !important;
  color: #ddd !important;
}
a {
  color: #60a5fa !important;
}
```

See the built-in `web-tint` module under [`modules/`](https://github.com/shiaho777/web-to-app/tree/main/modules) for a working `DOCUMENT_START` style module.
