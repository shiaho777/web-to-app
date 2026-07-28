# CSS 模块

CSS 模块是纯样式覆盖 —— 为某个站点做主题化、重设样式或夜间模式。它使用与 [JS 模块](/zh/extensions/js-module)相同的 `module.json` 清单,但实质是样式表。

## 文件布局

```
my-theme/
├── module.json    # 必需
├── main.js        # 必需 —— 可以是近乎为空的桩
├── style.css      # 实际样式
└── icon.png       # 可选
```

::: info 仍需 `main.js`
即便是纯 CSS 模块也需要一个 `main.js`(可以是最小的桩)。把 `runAt` 设为 `DOCUMENT_START`,使你的样式尽早生效,避免未设样式内容的闪现。
:::

## `module.json`

```json
{
  "id": "dark-reader-lite",
  "name": "Dark Reader Lite",
  "description": "一个简单的夜间主题",
  "category": "THEME",
  "runAt": "DOCUMENT_START",
  "urlMatches": [
    { "pattern": "*://news.ycombinator.com/*" }
  ],
  "permissions": ["CSS_INJECT"]
}
```

使用 `category: "STYLE_MODIFIER"` 或 `"THEME"`。市场的 `hasCss` 标志必须为 `true` 且必须存在 `style.css` —— 校验器会检查两者一致。

## CSS 如何注入

当模块有 CSS(`cssCode` / `style.css`)时,它会在 JS 运行之前作为 `<style id="ext-module-<id>">` 元素注入。你的 `main.js` 仍可在需要时操作 DOM。

## `style.css` 示例

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

可工作的 `DOCUMENT_START` 样式模块示例见 [`modules/`](https://github.com/shiaho777/web-to-app/tree/main/modules) 下内置的 `web-tint` 模块。
