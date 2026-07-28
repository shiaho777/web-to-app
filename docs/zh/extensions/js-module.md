# JS 模块

JS 模块是能力最强的原生扩展格式:一个清单、一个脚本、可选 CSS、一个配置 UI,以及一个可选的浮动面板 —— 全部打包在一起。

## 文件布局

```
my-module/
├── module.json    # 必需 —— 清单
├── main.js        # 必需 —— 在 WebView 中运行
├── style.css      # 可选 —— 当 hasCss / cssCode 存在时自动注入
└── icon.png       # 可选 —— ≤256KB;png/svg/webp/jpg/jpeg
```

## `module.json` schema

```json
{
  "id": "my-module",
  "name": "My Module",
  "description": "它做什么",
  "icon": "star",
  "category": "CONTENT_ENHANCE",
  "tags": ["demo"],
  "version": { "code": 1, "name": "1.0.0", "changelog": "首次发布" },
  "author": { "name": "You", "url": "https://example.com" },
  "runAt": "DOCUMENT_END",
  "urlMatches": [
    { "pattern": "*://example.com/*", "isRegex": false, "exclude": false }
  ],
  "permissions": ["DOM_ACCESS", "STORAGE"],
  "configItems": [
    {
      "key": "greeting",
      "name": "问候语",
      "type": "TEXT",
      "defaultValue": "Hello",
      "required": true
    }
  ]
}
```

::: warning `version` 是一个对象
`version` 有 `code`(整数)、`name`(semver 字符串)和 `changelog`。不要在 `module.json` 里把它写成纯字符串。
:::

### 字段参考

| 字段 | 说明 |
| --- | --- |
| `id` | 全局唯一。 |
| `icon` | Material Icons 名称(如 `star`、`package`)。 |
| `category` | 取值之一:`CONTENT_FILTER`、`CONTENT_ENHANCE`、`STYLE_MODIFIER`、`THEME`、`FUNCTION_ENHANCE`、`AUTOMATION`、`NAVIGATION`、`DATA_EXTRACT`、`DATA_SAVE`、`INTERACTION`、`ACCESSIBILITY`、`MEDIA`、`VIDEO`、`IMAGE`、`AUDIO`、`SECURITY`、`ANTI_TRACKING`、`SOCIAL`、`SHOPPING`、`READING`、`TRANSLATE`、`DEVELOPER`、`OTHER`。 |
| `runAt` | `DOCUMENT_START`、`DOCUMENT_END`(默认)、`DOCUMENT_IDLE`、`CONTEXT_MENU`、`BEFORE_UNLOAD`。 |
| `urlMatches[]` | `{pattern, isRegex=false, exclude=false}`。见 [URL 匹配](#url-匹配)。 |
| `permissions[]` | 仅展示用;运行时**不**据此沙箱化。危险项(如 `CAMERA`、`LOCATION`、`EVAL`、`FILE_ACCESS`)会受到额外审核。 |
| `configItems[]` | 用户可配置字段;见[配置项](#配置项)。 |

## URL 匹配

- **`isRegex: false`**(默认)—— Chrome 风格 glob。`*` 匹配任意字符;`*://` 展开为 `(https?|ftp|file)://`;`*` 或 `<all_urls>` 匹配一切。若 glob 无法匹配则回退为子串 `contains`。
- **`isRegex: true`** —— Java 正则,带 **200ms 超时**;超时算作不匹配。
- **`exclude: true`** —— 从结果集中移除匹配的 URL。

## `main.js` 契约

你的代码被包在一个带 `try/catch` 的 IIFE 中(错误写入 `console.error`,绝不破坏页面)。以下全局可用:

| 全局 | 值 |
| --- | --- |
| `__MODULE_INFO__` | `{id, name, icon, version, uiConfig, runMode}` |
| `__MODULE_CONFIG__` | 解析后的配置对象 |
| `__MODULE_UI_CONFIG__` | UI 配置 |
| `__MODULE_RUN_MODE__` | `'INTERACTIVE'` 或 `'AUTO'` |
| `__MODULE_PANEL_HTML__` | 你的 `panelHtml`(若有) |
| `getConfig(key, defaultValue)` | 读取配置值的便捷访问器 |

```js
// main.js
const greeting = getConfig('greeting', 'Hello')
const banner = document.createElement('div')
banner.textContent = greeting
banner.style.cssText = 'position:fixed;top:0;left:0;z-index:99999;padding:8px;background:#2563eb;color:#fff'
document.body.appendChild(banner)
```

::: warning 禁止顶层 `return`
因为你的代码被包在 IIFE 里,顶层 `return` 语句是非法的,会被市场校验器拒绝。
:::

## 配置项

`configItems[]` 为用户构建设置 UI。每一项:

```json
{
  "key": "speedLevel",
  "name": "速度",
  "description": "滚动速度倍数",
  "type": "NUMBER",
  "defaultValue": "3",
  "options": [],
  "required": false,
  "placeholder": "",
  "validation": ""
}
```

支持的 `type` 取值:`TEXT`、`TEXTAREA`、`NUMBER`、`BOOLEAN`、`SELECT`、`MULTI_SELECT`、`RADIO`、`CHECKBOX`、`COLOR`、`URL`、`EMAIL`、`PASSWORD`、`REGEX`、`CSS_SELECTOR`、`JAVASCRIPT`、`JSON`、`RANGE`、`DATE`、`TIME`、`DATETIME`、`FILE`、`IMAGE`。

用 `getConfig(key, defaultValue)` 读取值。

## 交互面板

要一个浮动 UI,提供 `panelHtml` 并注册一个面板按钮:

```js
window.__WTA_MODULE_UI__.register({
  id: __MODULE_INFO__.id,
  name: __MODULE_INFO__.name,
  icon: __MODULE_INFO__.icon
})
```

在 `panelHtml` 内部,使用绑定到 `window.__wta_module_action_<name>` 处理器的 `data-wta-action` 属性,并用 `var(--wta-*)` 主题变量做样式,使你的面板与应用主题一致。

## 多文件模块

`codeFiles` 是一个 `Map<文件名, 源码>`。入口点从 `main.js`、`index.js`、`app.js`、`script.js` 或 `content.js` 自动识别。

## 打包与分享

- 模块导出扩展名:`.wtamod`;模块打包:`.wtapkg`。
- 分享码前缀:`WTA1:`(gzip + Base64),可通过二维码分享。

完整可工作的示例见 [`modules/`](https://github.com/shiaho777/web-to-app/tree/main/modules) 下内置的 `hello-world` 和 `auto-scroll` 模块。
