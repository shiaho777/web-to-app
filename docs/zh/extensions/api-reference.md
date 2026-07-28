# API 参考

扩展 API 的汇总参考。状态图例:

- ✅ **可用** —— 按预期工作。
- 🟡 **部分** —— 可用但有注意事项(见说明)。
- ⬜ **桩** —— 存在但返回默认值 / no-op。

## 油猴脚本 `GM_*`

| API | 状态 | 说明 |
| --- | --- | --- |
| `GM_getValue` / `GM_setValue` / `GM_deleteValue` / `GM_listValues` | ✅ | SharedPreferences 支撑,按脚本命名空间 |
| `GM_xmlhttpRequest` | ✅ | 经 OkHttp;完整回调集 |
| `GM_addStyle` | ✅ | |
| `GM_setClipboard` | ✅ | |
| `GM_openInTab` | ✅ | |
| `GM_log` | ✅ | |
| `GM_getResourceText` / `GM_getResourceURL` | ✅ | |
| `GM_registerMenuCommand` / `GM_unregisterMenuCommand` | ✅ | 浮窗菜单 |
| `GM_openScriptWindow` / `GM_updateScriptWindow` / `GM_closeScriptWindow` | ✅ | WebToApp 专属 |
| `GM_info` | ✅ | |
| `unsafeWindow` | ✅ | 等于 `window`(无沙箱) |
| `GM_notification` | 🟡 | 仅记录日志;无系统通知 |
| `@grant` 门控 | ⬜ | 无论声明的 grant,所有 API 都暴露 |
| `GM.*`(Promise) | ✅ | 镜像上述全部 |

## Chrome `chrome.*`

| 命名空间 | 状态 | 说明 |
| --- | --- | --- |
| `runtime` | ✅ | 消息、生命周期、`getURL`、`getManifest` |
| `storage` | ✅ | `local`/`sync`/`session`;`managed` no-op |
| `tabs` | ✅ | 核心增删改查 + 事件 |
| `scripting` | ✅ | 动态 content script 注册 |
| `cookies` | ✅ | |
| `alarms` | ✅ | |
| `declarativeNetRequest` | ✅ | block/allow/redirect/modifyHeaders/upgradeScheme |
| `downloads` | 🟡 | `download` 可用 |
| `webRequest` | 🟡 | `onBeforeRequest` 经原生桥过滤 |
| `i18n` / `notifications` / `permissions` / `contextMenus` / `commands` | ✅ | |
| `webNavigation` / `extension` / `idle` / `tabGroups` | 🟡 | |
| `action` / `sidePanel` | 🟡 | |
| `windows` / `management` / `topSites` / `fontSettings` | ⬜ | 桩 |
| `identity` / `history` / `proxy` / `privacy` | ⬜ | 桩 |
| `system.cpu/memory/display/storage` | ⬜ | 桩 |
| `bookmarks` / `offscreen` / `tts` | ⬜ | 桩 |
| `browserAction` / `pageAction`(MV2) | 🟡 | 兼容别名 |

::: tip 本表是活文档
可用/桩状态正在逐项核实。有疑问时,请以当前构建实测为准。
:::
