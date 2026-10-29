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

全屏和[浏览器工具栏](/zh/guide/app-actions/edit-common-config/browser-toolbar)互不取消。全屏隐藏的是系统状态栏和导航栏。工具栏自己的开关开着，全屏时它也留在屏幕上。工具栏默认关闭，所以新导出的应用无论是否全屏都没有工具栏。工具栏是一行：标题和地址排在同一行。多网页的抽屉模式把站点菜单并进这一行，不再另占一条栏。

## 说明

- 如果同时开启了[悬浮小窗](/zh/guide/app-actions/edit-common-config/floating-window),应用以小窗打开。全屏不会把它换成全屏页面。
- 当状态栏可见时,启动倒计时/跳过胶囊会落在其下方,绝不被遮挡。
- 底部内容内边距留在物理屏幕边缘。键盘打开时,只保留键盘没有盖住的那一段。键盘比这段更高时,页面贴着键盘。
- 透明和跟随页面模式会采样系统 WebView,并用页面颜色绘制导航栏和全屏边距。首屏绘制后会再采一次,所以第一屏不会停在浅色回退上。主题模式用界面颜色绘制状态栏,深色模式也一样。自定义模式保持你设置的颜色。GeckoView 拿不到这个采样,所以沿用之前的栏颜色。
- 顶部和底部标签页只应用一次全屏内容内边距。卡片、抽屉和信息流本来就是这样。
- 全屏**视频**方向(全屏视频播放的横屏/传感器方向,`fullscreenVideoOrientation`)在[特殊设置](/zh/guide/app-actions/edit-common-config/special-settings)中配置。同一卡片还有"视频全屏时隐藏状态栏"(`hideStatusBarInVideoFullscreen`,默认开启):网页视频进入 HTML5 全屏时,即使"全屏显示状态栏"开启也会强制隐藏状态栏。
- Android 10 及以下的键盘避让走经典窗口缩放路径;键盘调整模式见[特殊设置](/zh/guide/app-actions/edit-common-config/special-settings)。
