# 长按菜单

配置长按页面内容时显示的上下文菜单。

**位置:**[编辑通用配置](/zh/guide/app-actions/edit-common-config/)编辑器中的 **长按菜单** 卡片。

## 选项

- **样式**(`longPressMenuStyle`):
  - `DISABLED` —— 无长按菜单(同时禁用)
  - `SIMPLE`
  - `FULL`
  - `IOS`
  - `FLOATING`
  - `CONTEXT`
- **空长按时不震动**(`longPressHapticOnActionOnly`):默认关闭。菜单本身关掉时，这一行仍然在。打开后，长按弹出本菜单时，在菜单出现的那一下震动。网页自己处理的长按(选中文字、网页自己的菜单)仍然震动。两边都没有的长按保持安静。预览和导出的 APK 都作用于系统 WebView。

## 说明

- 选择 `DISABLED` 以外的任意样式都会启用长按菜单(`longPressMenuEnabled`)。
