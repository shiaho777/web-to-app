# 隐藏浏览器工具栏

控制应用内浏览器工具栏(带后退/前进/刷新/标题/URL 的栏)。

**位置:**[编辑通用配置](/zh/guide/app-actions/edit-common-config/)编辑器中的 **隐藏浏览器工具栏** 卡片。

## 选项

- **隐藏浏览器工具栏** —— 完全隐藏工具栏(`hideBrowserToolbar`)。
- 首次启用隐藏时,各工具栏项会被关闭并标记为已自定义。
- **工具栏项** —— 独立开关工具栏显示的内容:
  - 显示标题(`toolbarShowTitle`)
  - 显示 URL(`toolbarShowUrl`)
  - 显示后退(`toolbarShowBack`)
  - 显示前进(`toolbarShowForward`)
  - 显示刷新(`toolbarShowRefresh`)

## 运行时工具栏控件

以下控件位于运行时工具栏(在生成的 APK 或宿主预览内),而非上方的构建期配置:

- **页面缩放** —— 工具栏动作打开预设选择器(50% / 67% / 75% / 80% / 90% / 100% / 110% / 125% / 150%,对齐 Chrome 的档位)。所选缩放**按应用**保存(以包名为键),冷启动后无需重载页面即可重新应用。与构建期的[缩放](/zh/guide/app-actions/edit-common-config/advanced-settings)开关是两回事。
- **控制台** —— 工具栏按钮打开控制台面板,显示 `console.log` / 错误输出。便于在运行时调试已加载的页面。

## 说明

- 隐藏系统状态栏/导航栏见[全屏模式](/zh/guide/app-actions/edit-common-config/fullscreen)。
- 浮动返回按钮可在[特殊设置](/zh/guide/app-actions/edit-common-config/special-settings)中启用。
