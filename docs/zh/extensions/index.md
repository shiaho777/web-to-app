# 扩展开发

WebToApp 在应用发布后仍可扩展。你可以添加三类扩展,它们都由同一个 `ExtensionManager` 管理,并由 WebView 在页面生命周期钩子处注入:

| 类型 | 是什么 | 适用场景 |
| --- | --- | --- |
| **[JS 模块](/zh/extensions/js-module)** | `module.json` 清单 + `main.js`(+ 可选 CSS) | 带配置 UI 和浮动面板的自定义功能 |
| **[CSS 模块](/zh/extensions/css-module)** | 纯样式覆盖(仍需 `main.js` 桩) | 主题化、重设样式、夜间模式 |
| **[油猴脚本](/zh/extensions/userscript)** | Tampermonkey/Greasemonkey 风格 `.user.js` | 移植现有油猴脚本;`GM_*` API |
| **[Chrome MV3](/zh/extensions/chrome-mv3)** | Manifest V3 Chrome 扩展 | 移植浏览器扩展;`chrome.*` API |

## 注入如何工作

四种类型都被归一化为一个内部 `ExtensionModule` 模型。运行时,`WebViewManager` 在每个模块配置的**运行时机**注入它:

| 运行时机 | 触发于 |
| --- | --- |
| `DOCUMENT_START` | `onPageStarted` |
| `DOCUMENT_END` | `onPageFinished`(DOMContentLoaded) |
| `DOCUMENT_IDLE` | 加载后(默认) |
| `CONTEXT_MENU` | 上下文菜单时 |
| `BEFORE_UNLOAD` | 卸载前 |

每个模块还带有 **URL 匹配规则**(Chrome 风格 glob 或正则),决定它在哪些页面运行。

::: warning 重要的准确性说明
有些行为与其他扩展平台的暗示不同。这些在相关页面有说明,但值得预先了解:
- **油猴脚本的 `GM_*` 函数不按 `@grant` 门控** —— 全部无条件暴露。
- **Chrome 的 `ISOLATED` 与 `MAIN` world 并非真正隔离** —— Android WebView 只有单一 JS 上下文;隔离是模拟的。
- **`GM_notification` 仅记录日志**;MV3 的"后台 service worker"是一个隐藏的 WebView,而非真正的 service worker。
:::

## 接下来去哪

- [JS 模块](/zh/extensions/js-module) —— 能力最强的原生格式。
- [油猴脚本](/zh/extensions/userscript) —— `GM_*` / `GM.*` API 参考。
- [Chrome MV3](/zh/extensions/chrome-mv3) —— 支持的 `chrome.*` 接口。
- [发布到市场](/zh/extensions/publish) —— 与社区分享 JS/CSS 模块。
