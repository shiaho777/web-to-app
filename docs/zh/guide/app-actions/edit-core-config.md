# 编辑核心配置

打开应用的 **类型专属** 设置 —— 其来源与运行时配置。点击应用卡片上的 ⋮,再点 **编辑核心配置**。每种应用类型的核心配置都不同;每个类型页都在一个详细的 **核心配置** 分区中记录自己的配置。

## 编辑什么

类型专属的创建表单,复用为编辑器。点各链接看完整的、按小类组织的字段参考:

| 应用类型 | 核心配置涵盖 |
| --- | --- |
| [网页](/zh/guide/app-types/web) | 目标与引擎、User-Agent 与显示、注入、弹窗 *(网页有单一合并编辑器 —— 见说明)* |
| [多站点](/zh/guide/app-types/multi-web) | 站点(名称/URL/类型/图标/主题色/选择器)、布局与显示、刷新、共享注入 |
| [HTML](/zh/guide/app-types/html) | 来源、入口文件、加载模式与端口、能力、外观 |
| [离线包](/zh/guide/app-types/offline-pack) | 爬取范围、过滤、网络 |
| [前端](/zh/guide/app-types/frontend) | 构建输出、框架、工具链 |
| [PHP](/zh/guide/app-types/php) | 项目、服务器(端口/环境变量)、依赖与扩展 |
| [WordPress](/zh/guide/app-types/wordpress) | 站点、管理员账户、主题与插件、来源与安装、服务器 |
| [Node.js](/zh/guide/app-types/nodejs) | 项目、构建模式、服务器(入口/端口/环境变量)、原生插件 |
| [Python](/zh/guide/app-types/python) | 项目、入口与服务器、依赖与扩展 |
| [Go](/zh/guide/app-types/go) | 项目、构建(二进制/架构)、服务器(端口/静态/环境变量) |
| [媒体](/zh/guide/app-types/media) | 来源、播放、显示 |
| [画廊](/zh/guide/app-types/gallery) | 内容、播放、视图、显示 |

## 说明

- **网页应用不显示此入口** —— 它们有单一的 **编辑**,涵盖一切,因为网页配置不拆分。
- 共享选项(外观、网络、隐私、导出)用[编辑通用配置](/zh/guide/app-actions/edit-common-config/)。
