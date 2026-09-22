# HCJ 插件

HCJ 插件是原生格式：把普通的 **HTML + CSS + JavaScript** 打成一个文件夹。没有自定义 DSL——会写油猴脚本就会写 HCJ 插件；需要界面时写真正的 HTML，而不是学一套表单 Schema。

## 文件结构

```
my-plugin/
├── plugin.json    # 必需 —— 清单
├── plugin.html    # 必需 —— 页面脚本 + 面板文档合一
├── icon.png       # 可选 —— ≤256KB；png/svg/webp/jpg/jpeg
└── files/         # 可选的额外包文件
```

::: info 一个 HTML 文件就是整个插件
`plugin.html` 是普通 HTML 文档。在匹配**网页**里运行的部分放在惰性块 `<script type="hcj/page">…</script>` 中（未知 script type 不会执行——标准的数据块用法）；文档的其余部分就是**面板 UI**。纯页面插件的 `plugin.html` 里只有这一块。
:::

::: details 旧的多文件布局
旧包可能还带着 `main.js` + `panel.html` + `style.css`。它们照常加载——页面脚本照跑、`panel.html` 照托管、`style.css` 照注入。在编辑器里保存这类包时会自动改写成 `plugin.html`。
:::

## `plugin.json` 清单

```json
{
  "id": "my-plugin",
  "name": "My Plugin",
  "version": "1.0.0",
  "description": "做什么的",
  "author": "You",
  "homepage": "https://example.com",
  "icon": "star",
  "matches": ["*://example.com/*"],
  "excludeMatches": [],
  "runAt": "document_end",
  "permissions": ["STORAGE"],
  "toolbar": true
}
```

### 字段说明

| 字段 | 说明 |
| --- | --- |
| `id` | 全局唯一。 |
| `name` | 显示名（必填）。 |
| `version` | 语义化版本字符串。 |
| `author` / `homepage` | 署名。 |
| `icon` | Material 图标名或包内图标文件名。 |
| `matches` | Chrome 风格 glob；`/pattern/` 表示正则。默认 `["*"]`。 |
| `excludeMatches` | 同语法；优先级高于 `matches`。 |
| `runAt` | `document_start`、`document_end`（默认）、`document_idle`。 |
| `permissions` | `hcj.*` 能力门禁：`STORAGE`（配置 KV）、`FETCH`（跨域）、`NOTIFY`、`BADGE`、`CLIPBOARD`、`DOWNLOAD`。页面 DOM 访问无需权限。 |
| `toolbar` | 是否在插件宿主面显示入口，默认 `true`。 |
| `preferredEntry` | 建议的入口形态（`toolbar`/`floating_handle`/`menu`）；用户的选择优先。 |

## URL 匹配

- **Glob**（默认）—— Chrome 风格。`*` 匹配任意；`*://` 展开为 `(https?|ftp|file)://`；单独的 `*` 匹配所有。
- **正则** —— 用斜杠包裹：`"/example\\.com\\/article\\/\\d+/"`，200ms 超时，超时按不匹配处理。
- **`excludeMatches`** —— 把命中的 URL 从结果中排除。

## 页面脚本合约

`<script type="hcj/page">` 里的代码被包进 IIFE，并注入一个作用域化的 `hcj` API 对象（异常进 `console.error`，不会影响页面）：

```html
<script type="hcj/page">
const greeting = hcj.config.get('greeting', 'Hello')
const banner = document.createElement('div')
banner.textContent = greeting
banner.style.cssText = 'position:fixed;top:0;left:0;z-index:99999;padding:8px;background:#2563eb;color:#fff'
document.body.appendChild(banner)
</script>
```

| API | 说明 |
| --- | --- |
| `hcj.id` / `hcj.manifest` / `hcj.lang` | 插件 id、清单摘要、应用语言 |
| `hcj.config.get/set/remove/all` | 持久化 KV——需要 `STORAGE` |
| `hcj.fetch(url, opts)` | 经宿主的跨域请求——需要 `FETCH`，返回 Promise |
| `hcj.notify(title, body)` | 系统通知——需要 `NOTIFY` |
| `hcj.toast(msg)` | 页面内浮层提示——操作反馈优先用它而非 `notify` |
| `hcj.badge(text, color)` | 工具栏角标——需要 `BADGE` |
| `hcj.addStyle(css)` | 注入页面 CSS（按文档幂等） |
| `hcj.panel.open()` / `close()` | 打开/关闭本插件的面板文档 |
| `hcj.panel.send(msg)` / `hcj.panel.onMessage(fn)` | 页面 ↔ 面板消息 |
| `hcj.on('action', fn)` | 用户点击插件入口时触发（纯页面插件） |
| `hcj.emit(evt, data)` | 给自己的 handler 发自定义事件 |
| `hcj.log(msg)` | 写宿主日志 |

::: warning 不要有顶层 `return`
代码被包在 IIFE 里，顶层 `return` 是语法错误，市场校验器会直接拒绝。
:::

## 面板（`plugin.html` 的其余部分）

`hcj/page` 块之外的内容就是插件自己的页面，由用户选定的宿主承载（底部抽屉/悬浮窗/全屏）。面板脚本运行前，会先注入镜像版 `hcjPanel` 对象：

```html
<script>
  hcjPanel.onMessage((msg) => { /* 页面 → 面板 */ })
  hcjPanel.send({ type: 'refresh' })        // 面板 → 页面
  const theme = hcjPanel.config.get('theme', 'auto')
  hcjPanel.config.set('theme', 'dark')
  hcjPanel.close()
</script>
```

**设置界面完全由你写。** 直接在 `plugin.html` 里用普通 HTML/CSS/JS 做表单，用 `hcjPanel.config` 持久化；页面侧可以重读配置，或监听 `hcj.panel.onMessage` 实时应用变更。

## 油猴互通

`.user.js` 可直接导入：`==UserScript==` 块会被转换成 `plugin.json`（`@match`/`@include` → `matches`，`@grant` → `permissions`，`@run-at` → `runAt`），`GM_*` 调用继续走油猴运行时。`.hcj` 文件就是 zip 压缩的插件包，用于分享。

完整示例见内置插件包 [`app/src/main/assets/plugins/`](https://github.com/shiaho777/web-to-app/tree/main/app/src/main/assets/plugins) 和市场包 [`modules/`](https://github.com/shiaho777/web-to-app/tree/main/modules)。
