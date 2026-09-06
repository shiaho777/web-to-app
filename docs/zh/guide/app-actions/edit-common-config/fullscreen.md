# 全屏模式

以沉浸式运行应用,隐藏系统栏,并与浏览器工具栏设置(可选)配合工作。

**位置:**[编辑通用配置](/zh/guide/app-actions/edit-common-config/)编辑器中的 **全屏模式** 卡片。

## 选项

- **全屏** —— 启用沉浸式全屏(`hideToolbar`)。
- **全屏常驻状态栏** —— 保持顶部状态栏持续可见(`showStatusBarInFullscreen`)。常驻模式永不自动隐藏。
- **全屏显示导航栏** —— 保持底部导航栏可见(`showNavigationBarInFullscreen`)。
- **全屏内容内边距** —— 内容内缩若干 dp(`fullscreenContentPaddingDp`)。
- **自定义状态栏** —— 可展开子面板(`statusBarCustomizeLabel`),含亮色/暗色两个标签页:颜色模式(`THEME`/`PAGE_TOP`/`TRANSPARENT`/`CUSTOM`)、自定义颜色、深色图标,以及各模式的背景(颜色/图片)。`TRANSPARENT`/图片栏压盖内容,不预留占位。

## 与浏览器工具栏的关系

全屏时是否显示应用内工具栏,由[浏览器工具栏](/zh/guide/app-actions/edit-common-config/browser-toolbar)主开关和 `showToolbarInFullscreen` 共同决定(运行时为 `hideToolbar = !browserToolbarEnabled || !showToolbarInFullscreen`)。注意工具栏默认关闭,因此新导出的应用无论是否全屏都没有工具栏。

## 说明

- 当状态栏可见时,启动倒计时/跳过胶囊会落在其下方,绝不被遮挡。
- 全屏**视频**方向(全屏视频播放的横屏/传感器方向,`fullscreenVideoOrientation`)在[特殊设置](/zh/guide/app-actions/edit-common-config/special-settings)中配置。同一卡片还有"视频全屏时隐藏状态栏"(`hideStatusBarInVideoFullscreen`,默认开启):网页视频进入 HTML5 全屏时,即使"全屏显示状态栏"开启也会强制隐藏状态栏。
- Android 10 及以下的键盘避让走经典窗口缩放路径;键盘调整模式见[特殊设置](/zh/guide/app-actions/edit-common-config/special-settings)。
