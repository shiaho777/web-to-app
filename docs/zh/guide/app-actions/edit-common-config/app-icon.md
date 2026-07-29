# 应用图标

应用在应用列表中显示的图标,导出后也是设备启动器上的图标。

**位置:**[编辑通用配置](/zh/guide/app-actions/edit-common-config/)编辑器顶部的 **基本信息** 卡片。

## 选项

- **选择图片** —— 从设备选一张图片(`image/*`)。所选图片成为应用图标。
- **从图标库选择** —— 选一个 previously 保存的图标(`savedIconPath`)。
- **默认** —— 不设图标时,使用类型专属的默认图标。

## 说明

- 导出时,图标被写入 APK 的资源(二进制资源表被打补丁)。见 [APK导出配置](/zh/guide/app-actions/edit-common-config/apk-export)。
- [图标与应用](/zh/guide/app-actions/edit-common-config/icon-disguise)卡片可为生成的应用进一步伪装图标(多启动器图标、图标风暴)。
