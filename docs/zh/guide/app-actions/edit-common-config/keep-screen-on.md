# 保持屏幕常亮

在应用运行时防止屏幕熄灭,可选超时和亮度控制。

**位置:**[编辑通用配置](/zh/guide/app-actions/edit-common-config/)编辑器中的 **保持屏幕常亮** 卡片。

## 选项

- **屏幕常亮模式**(`screenAwakeMode`):
  - `OFF` —— 正常系统行为
  - `ALWAYS` —— 无限期保持屏幕常亮
  - `TIMED` —— 保持常亮设定时长
- **超时(分钟)** —— `TIMED` 模式下保持常亮的时长(`screenAwakeTimeoutMinutes`)。
- **屏幕亮度** —— 运行时覆盖亮度(`screenBrightness`;`-1` 表示不改变)。

## 说明

- 启用任何非 `OFF` 模式都会设置 `keepScreenOn`。
- 媒体应用有自己的常亮开关(见[媒体](/zh/guide/app-types/media)类型)。
