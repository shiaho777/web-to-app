# WordPress

在设备上通过本地 PHP + SQLite 运行 WordPress 站点 —— 一个装在 APK 里的便携 CMS。

## 适用场景

便携站点、主题/插件演示,以及想作为应用发布的内容打包。

## 运行时

- **技术栈** —— WordPress 7.x,基于设备端 [PHP](/zh/guide/app-types/php) 运行时,用 `sqlite-database-integration` 的 SQLite(无需 MySQL 服务器)。
- **导入** —— 支持主题和插件导入。
- 在 [Linux 环境](/zh/guide/more-features/linux-environment)和[运行时管理](/zh/guide/more-features/runtime-management)界面管理。

## 关键配置

- **WordPress 来源** —— 要打包的站点/主题。
- **主题与插件** —— 导入你需要的。
- **端口** —— 通过[端口管理](/zh/guide/more-features/port-manager)分配。

## 说明

- 因为使用 SQLite,WordPress 无需单独的数据库服务器即可运行。
- WebView 加载本地提供服务的 WordPress 实例。
