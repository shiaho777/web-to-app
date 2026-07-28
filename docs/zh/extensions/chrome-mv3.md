# Chrome MV3 扩展

WebToApp 运行 Manifest V3(及 V2)Chrome 扩展:content script、popup、options 页、后台上下文,以及 `declarativeNetRequest` 规则。你可以导入 `.crx`/`.zip`,或通过 **浏览器扩展** 标签页从 Chrome 网上应用店实时安装。

## 支持的 `manifest.json` 字段

| 字段 | 说明 |
| --- | --- |
| `manifest_version` | `2` 或 `3`。 |
| `name`、`version`、`description` | 支持通过 `_locales/<locale>/messages.json` 的 `__MSG_key__` i18n。 |
| `default_locale` | i18n 的基础语言。 |
| `content_scripts[]` | `matches`、`exclude_matches`、`js[]`、`css[]`、`run_at`(`document_start/end/idle`,默认 idle)、`all_frames`(默认 false)、`world`(`"MAIN"` 或默认 `"ISOLATED"`)。每个条目成为一个内部模块。 |
| `action.default_popup` / `browser_action` / `page_action` | Popup 页。 |
| `options_page` / `options_ui.page` | Options 页。 |
| `background.service_worker` / `background.scripts[0]` | 后台上下文;`background.type: "module"` 以 ES module 加载。 |
| `declarative_net_request.rule_resources[]` | `{id, enabled, path}`。 |
| `permissions`、`host_permissions`、`optional_permissions` | 映射到内部权限类别;不支持的会产生警告。 |

## `chrome.*` API 支持

polyfill 提供了广泛的 `chrome.*`(及 `browser.*`)接口。**核心可用**的命名空间:

| 命名空间 | 可用亮点 |
| --- | --- |
| `runtime` | `getURL`、`getManifest`、`sendMessage`、`onMessage`、`onInstalled`、`connect`/`onConnect`、`getPlatformInfo`、`id`、`lastError` |
| `storage` | `local` / `sync` / `session` + `onChanged`(`sync` 映射到 local;`managed` 为 no-op) |
| `tabs` | `create`、`query`、`sendMessage`、`get`、`getCurrent`、`update` + 事件 |
| `scripting` | `executeScript`、`insertCSS`、`removeCSS`、`registerContentScripts`、`unregisterContentScripts`、`getRegisteredContentScripts` |
| `cookies` | Cookie 访问 |
| `alarms` | 调度 |
| `declarativeNetRequest` | 基于规则的拦截/重定向(见下) |
| `downloads` | `download` |
| `webRequest` | `onBeforeRequest` 可经原生桥注册真实过滤 |
| `i18n`、`notifications`、`permissions`、`contextMenus`、`commands` | 支持 |

许多其他命名空间(`windows`、`management`、`bookmarks`、`history`、`identity`、`system.*`、`offscreen`、`tts`……)以**桩**形式存在,返回默认值。逐项状态见 [API 参考](/zh/extensions/api-reference)。

::: warning 没有真正的 world 隔离
Android WebView 只有**单一 JavaScript 上下文**。`world: "ISOLATED"` 和 `world: "MAIN"` 都通过 `evaluateJavascript` 注入同一个 window;隔离是*模拟*的 —— 每个扩展覆盖 `globalThis.chrome`,并在闭包中捕获自己的扩展 id。不要依赖扩展之间的真实隔离。
:::

::: info 后台是一个隐藏的 WebView
MV3 的"service worker"运行在一个带 service-worker 环境 shim 的隐藏 WebView 中(`self`、`clients`、`caches`、`install`/`activate`/`fetch` 事件)。在后台,`fetch()` 被改写为走绕过 CORS 的原生 HTTP 路径。它不是真正的 service worker。
:::

## `declarativeNetRequest`

支持的 action:`block`、`allow`、`redirect`、`modifyHeaders`、`upgradeScheme`、`allowAllRequests`。condition 支持 `urlFilter` / `regexFilter`、`resourceTypes`、`initiatorDomains`、`requestMethods` 等。优先用 DNR 而非 `webRequestBlocking`(后者不支持)。

## 资源 URL

`chrome.runtime.getURL(path)` 返回 `https://localhost/__ext__/<EXT_ID>/<path>`,由资源拦截器拦截并从扩展打包的文件中提供。

## 不支持的权限(仅警告)

`nativeMessaging`、`debugger`、`proxy`、`webRequestBlocking`、`management`、`devtools`、`bookmarks`、`identity`、`tabCapture` 等会产生警告且不可用。

## 安装

- **导入** `.crx`(直接解析 CRX2/CRX3)或 `.zip`。
- **Chrome 网上应用店** —— 浏览器扩展标签页按关键词实时搜索商店(或粘贴商店 URL / 扩展 ID),并通过 CRX 管线按需安装。实时搜索需要能访问 Google 的网络。
