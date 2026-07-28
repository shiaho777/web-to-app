# 创建应用

在[我的应用](/zh/guide/main-screen)点击 **创建**,打开一个 3 列网格的应用类型。类型按顺序为:

| | | |
| --- | --- | --- |
| **网页** | **多站点** | **HTML** |
| **离线包** | **前端** | **PHP** |
| **WordPress** | **Node.js** | **Python** |
| **Go** | **媒体** | **画廊** |

选择与你的输入相匹配的类型。每种都打开自己的创建流程,含类型专属字段,然后共享同一套[应用配置](/zh/guide/config/)选项。

## 选择类型

| 类型 | 输入 | 输出 | 适用场景 |
| --- | --- | --- | --- |
| [网页](/zh/guide/app-types/web-content#web) | 一个 URL | 基于 WebView 的 APK | 落地页、工具、仪表盘、文档、内部系统 |
| [多站点](/zh/guide/app-types/web-content#multi-web) | 多个 URL | 标签/卡片/信息流/抽屉 APK | 链接枢纽、门户、应用合集 |
| [HTML](/zh/guide/app-types/web-content#html) | 本地 HTML 文件 / zip | 本地托管 APK | 静态构建、离线 Web 应用 |
| [离线包](/zh/guide/app-types/web-content#offline-pack) | 一个 URL(抓取) | 自包含离线 APK | 把网站归档为离线使用 |
| [前端](/zh/guide/app-types/web-content#frontend) | 已构建的前端项目 | 本地托管 APK | React、Vue、Vite 生产构建 |
| [PHP](/zh/guide/app-types/server-runtimes#php) | 一个 PHP 项目 | APK + 设备端 PHP 服务器 | 小型 PHP 应用、管理工具 |
| [WordPress](/zh/guide/app-types/server-runtimes#wordpress) | 一个 WordPress 站点 | APK + PHP + SQLite | 便携站点、主题/插件演示 |
| [Node.js](/zh/guide/app-types/server-runtimes#node-js) | 一个 Node 项目 | APK + 设备端 Node 服务器 | Express/Fastify/Koa 应用、API |
| [Python](/zh/guide/app-types/server-runtimes#python) | 一个 Python 项目 | APK + 设备端 Python 服务器 | Flask、Django、FastAPI、Tornado |
| [Go](/zh/guide/app-types/server-runtimes#go) | 一个 Go 项目 | APK + 设备端 Go | Gin/Echo/Fiber、静态服务 |
| [媒体](/zh/guide/app-types/media#media) | 一张图片或一个视频 | 媒体播放器 APK | 单图/单视频查看器、课程媒体 |
| [画廊](/zh/guide/app-types/media#gallery) | 一个媒体集合 | 画廊 APK | 相册、作品集、离线查看器 |

## 创建流程

每种类型都遵循相同的形态:

1. **类型专属表单** —— 例如网页要求填 URL;Node.js 要求填项目和启动命令;画廊要求填媒体和布局。运行时类型(PHP/WordPress/Node.js/Python/前端)会链接到 [Linux 环境](/zh/guide/more-features/dev-tools#linux-environment)做工具链安装。
2. **基本信息** —— 名称、图标。
3. **保存** —— 应用被创建并出现在[我的应用](/zh/guide/main-screen)上。

创建后,打开应用的 ⋮ 菜单,选 **编辑核心配置** 回到类型专属表单,或选 **编辑通用配置** 配置共享选项(外观、网络、隐私、导出)。见[应用功能](/zh/guide/app-actions)与[应用配置](/zh/guide/config/)。

::: tip 先试一个示例
应用内置了示例项目(React、Vue、Vite、Node/Express、PHP/Laravel、Python/Flask、Go/Gin、WordPress 等)。在构建自己的应用之前,先用一个看看你的技术栈的可工作配置。
:::
