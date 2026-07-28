# 常见问题

## WebToApp 免费吗?

是的。WebToApp 以 [The Unlicense](https://github.com/shiaho777/web-to-app/blob/main/LICENSE) 开源。

## 需要什么 Android 版本?

Android 6.0(API 23)或更高。

## 构建应用需要电脑吗?

不需要。整个构建 —— 二进制打补丁、签名、AAB 导出 —— 都在设备上完成。只有当你想从源码构建 WebToApp 本身时才需要电脑。

## 为什么生成的应用 targetSdk 是 28?

较低的 `targetSdk` 是刻意为之:它让生成的应用能从应用存储 fork+exec 原生运行时(Node.js、PHP、Python、Go、WordPress)。AAB 导出器会单独为 Play 商店分发重写 `targetSdk`。

## 某功能预览正常,导出后失效,为什么?

通常是某个配置字段没有贯通导出链路(模型 → `ApkConfig` JSON → shell 配置 → 运行时)。诊断清单见[配置字段漂移](/zh/developer/config-drift)。

## 能运行浏览器扩展吗?

能。WebToApp 支持内置 JS/CSS 模块、Tampermonkey 风格油猴脚本和 MV3 Chrome 扩展。**浏览器扩展** 标签页可实时搜索 Chrome 网上应用店。见[扩展开发](/zh/extensions/)。

## 如何把模块发布到市场?

在 `modules/` 下添加一个文件夹,更新 `registry.json`,然后开一个 pull request。见[发布到市场](/zh/extensions/publish)。

## 去哪里获取帮助?

- GitHub:[github.com/shiaho777/web-to-app](https://github.com/shiaho777/web-to-app)
- Telegram:[t.me/webtoapp777](https://t.me/webtoapp777)
- X(Twitter):[@shiaho777](https://x.com/shiaho777)
- QQ 群:1041130206
