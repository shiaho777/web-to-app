# 油猴脚本

WebToApp 运行 Tampermonkey/Greasemonkey 风格的 `.user.js` 脚本,带 `GM_*` 桥。导入一个脚本,它会像其他扩展一样被解析、存储和注入。

## 元数据块

油猴脚本必须包含一个 `// ==UserScript== ... // ==/UserScript==` 块。没有它,文件会作为普通 JS 导入(作为油猴脚本则 `isValid = false`)。

```js
// ==UserScript==
// @name         Example Script
// @namespace    https://example.com/
// @version      1.0.0
// @description  Does a thing
// @author       You
// @match        *://example.com/*
// @grant        GM_getValue
// @grant        GM_setValue
// @grant        GM_xmlhttpRequest
// @run-at       document-idle
// @require      https://cdn.example.com/lib.js
// @resource     ICON https://example.com/icon.png
// @noframes
// ==/UserScript==

console.log('Hello from a userscript')
```

### 支持的 `@` 标签

`@name`、`@description`/`@desc`、`@version`、`@author`、`@namespace`、`@match`、`@include`、`@exclude`/`@exclude-match`、`@run-at`、`@grant`、`@require`、`@resource`(`name url`)、`@noframes`、`@icon`/`@iconURL`/`@icon64`/`@icon64URL`、`@homepage`/`@homepageURL`/`@website`。

- **`@run-at`**:`document-start` → `DOCUMENT_START`;`document-end`/`document-body` → `DOCUMENT_END`;`document-idle`(默认)→ `DOCUMENT_IDLE`。
- **`@match`**:Chrome glob(`<all_urls>` → `*`)。
- **`@include`**:若写成 `/regex/` 形式,按正则处理。
- **`@grant none`**:被过滤掉。

## `GM_*` API

同时提供经典的 `GM_*` 函数和基于 Promise 的 `GM.*`(Tampermonkey 4.x)。

| API | 说明 |
| --- | --- |
| `GM_getValue` / `GM_setValue` / `GM_deleteValue` / `GM_listValues` | 由 SharedPreferences 支撑,按脚本命名空间隔离。 |
| `GM_xmlhttpRequest` | 经 OkHttp。支持 `method`、`url`、`headers`、`data`、`responseType`;回调 `onload`/`onerror`/`ontimeout`/`onprogress`;返回 `{status, statusText, responseText, responseHeaders, finalUrl}`。 |
| `GM_addStyle` | 注入样式表。 |
| `GM_setClipboard` | 写入剪贴板。 |
| `GM_openInTab` | 在新标签页打开 URL。 |
| `GM_log` | 日志输出。 |
| `GM_notification` | **简化实现 —— 仅记录日志;不弹系统通知。** |
| `GM_getResourceText` / `GM_getResourceURL` | 读取 `@resource` 内容。 |
| `GM_registerMenuCommand` / `GM_unregisterMenuCommand` | 集成到浮窗菜单。 |
| `GM_openScriptWindow` / `GM_updateScriptWindow` / `GM_closeScriptWindow` | WebToApp 专属脚本窗口。 |
| `GM_info` | `{script:{name,version,description,author,namespace,grants,resources}, scriptHandler:'WebToApp', version:'1.0'}` |
| `unsafeWindow` | 等于 `window`(无沙箱)。 |

基于 Promise 的 `GM.*` 镜像上述全部(`GM.getValue`、`GM.xmlHttpRequest`……)。

::: warning grant 不被强制执行
尽管 `@grant` 声明会被解析并列入 `GM_info.script.grants`,但**所有 `GM_*` 函数都无条件暴露** —— 宿主目前不按 grant 门控 API。为了与真正的 Tampermonkey/Greasemonkey 保持可移植性,仍应声明 grant。
:::

## `@require` 与 `@resource`

`@require` 脚本和 `@resource` 文件会被下载并缓存,然后在你的脚本主体之前注入。

- 单个 `@require` 最大:**5 MB**。
- 整个扩展包最大:**50 MB**。

注入顺序:bootstrap(窗口管理器)→ GM polyfill → 各 `@require` → 你的脚本。

## 示例

```js
// ==UserScript==
// @name         Highlighter
// @match        *://*/*
// @grant        GM_addStyle
// @run-at       document-start
// ==/UserScript==

GM_addStyle('mark { background: #fde047; }')
document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('code').forEach(el => {
    const m = document.createElement('mark')
    m.textContent = el.textContent
    el.replaceWith(m)
  })
})
```
