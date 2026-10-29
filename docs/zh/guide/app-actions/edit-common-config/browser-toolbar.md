# 浏览器工具栏

控制应用内浏览器工具栏(带后退/前进/刷新/标题/URL 的栏)。

**位置:**[编辑通用配置](/zh/guide/app-actions/edit-common-config/)编辑器中的 **浏览器工具栏** 卡片。

## 选项

- **浏览器工具栏** —— 主开关(`browserToolbarEnabled`)。**默认关闭**:页面完全不带工具栏渲染,效果类似全屏。打开后工具栏**所有子项全开**;再从中裁剪单个按钮。再次关闭会把所有子项一并关闭——重新打开总是从全开状态开始。
- **工具栏项** —— 工具栏启用时,每个按钮有独立开关:
  - 显示标题(`toolbarShowTitle`)
  - 显示 URL(`toolbarShowUrl`)
  - 显示后退(`toolbarShowBack`)
  - 显示前进(`toolbarShowForward`)
  - 显示刷新(`toolbarShowRefresh`)
  - 显示控制台按钮(`toolbarShowConsole`)
  - 显示页内查找按钮(`toolbarShowFind`)

只保留部分子项的工具栏仍会渲染(例如只剩控制台);宿主预览与导出 APK 共用同一可见性规则,预览裁剪的结果就是导出的结果。

## 运行时工具栏面板

以下面板**从运行时工具栏打开**(在生成的 APK 或宿主预览内)。它们的工具栏按钮受上方构建期项控制——隐藏对应项,按钮即消失:

- **控制台** —— 工具栏按钮打开控制台面板,显示 `console.log` / 错误输出。便于在运行时调试已加载的页面。
- **页内查找** —— 工具栏按钮打开原生底部搜索栏,实时匹配计数、上/下跳转,由 WebView 引擎(`findAllAsync`)驱动;搜索栏打开时键盘自动弹起。

页面缩放不再是工具栏动作——它已成为[高级设置](/zh/guide/app-actions/edit-common-config/advanced-settings)中的构建期设置。

## 说明

- 隐藏系统状态栏/导航栏见[全屏模式](/zh/guide/app-actions/edit-common-config/fullscreen)。
- 浮动返回按钮可在[特殊设置](/zh/guide/app-actions/edit-common-config/special-settings)中启用——工具栏(及其返回按钮)隐藏时特别有用。
- 全屏不会关掉这条工具栏。栏是一行，标题和地址并排。多网页抽屉模式的站点菜单也放在这一行，不再另占一条栏。
