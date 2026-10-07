#插件开发

WebToApp 生成的应用依然可扩展。三种插件共用同一个管理界面和注入管线：

| 类型 |是什么|合适|
| --- | --- | --- |
| **[HCJ插件](/zh/extensions/js-module)** | `plugin.json` + 单文件 `plugin.html`的包--纯HTML+CSS+JS，无DSL|自定义功能、面板、设置界面|
| **[油猴脚本](/zh/extensions/userscript)** |TamperMonkey/Greasemonkey风格的`.user.js` | 移植现成脚本；`总经理_*`API|
| **[铬MV3](/zh/extensions/chrome-MV3)** |清单V3Chrome扩展| 移植浏览器扩展；`铬。*`API|

HCJ(HTML+CSS+JS)是原生格式--可以理解为带真实UI能力的油猴脚本升级版.

## 注入机制

脚本类插件按配置的**运行时机**注入：

| 运行时机 |接触点|
| --- | --- |
| `document_start` | `onPageStarted`，早于页面脚本|
| `文档结束(_E)` |DOMContentLoaded（默认）|
| `document_idle` | 页面加载完成后 |

`hcj.addStyle()`（或旧版 `style.css`)在document-start注入页岩面css，视觉类插入件在颈肛门纹染前生效。**URL匹配规则**(Chrome风格glob或`/正则/`）决定插件在哪些页面运行。铬扩展走MV3引擎--隐藏WebView跑后台服务人员+动态注册内容脚本。

## 插件入口在哪

每个插件都有**插件宿主面**上的入口，宿主形态由用户按应用（或全局）选择：

- **工具栏** —— 原生工具栏里的插件按钮，点开插件抽屉
- **悬浮句柄** —— 可拖动的原生悬浮柄，自动收起（工具栏隐藏时也可用）
- **菜单** —— 收纳进右上角溢出菜单

`plugin.html` 里有面板内容的插件在用户选定的面板宿主中打开——**底部抽屉**、**悬浮窗**或**全屏**；纯页面插件则触发它的 `hcj.on('action')` 回调。Chrome 扩展的 `action.popup` 页面也托管在同一个面板面。

::: warning 准确性说明
- **油猴 `GM_*` 不受 `@grant` 门控** —— 全部无条件暴露。
- **Chrome `ISOLATED`/`MAIN` world 并非真隔离** —— Android WebView 只有一个 JS 上下文，隔离是模拟的。
- **MV3 "后台 service worker" 是一个隐藏 WebView**，不是真的 service worker。
:::

## 下一步

- [HCJ 插件](/zh/extensions/js-module) —— 原生包格式
- [CSS 插件](/zh/extensions/css-module) —— 纯样式覆盖
- [油猴脚本](/zh/extensions/userscript) —— `GM_*` / `GM.*` API 参考
- [Chrome MV3](/zh/extensions/chrome-mv3) —— 支持的 `chrome.*` 能力面
- [发布到市场](/zh/extensions/publish) —— 与社区分享插件
