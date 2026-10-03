# WordPress

在设备上通过本地 PHP + SQLite 运行 WordPress 站点 —— 一个装在 APK 里的便携 CMS。

## 适用场景

便携站点、主题/插件演示,以及想作为应用发布的内容打包。

## 运行时

- **技术栈** —— WordPress 7.x,基于设备端 [PHP](/zh/guide/app-types/php) 运行时,用 `sqlite-database-integration` 的 SQLite(无需 MySQL 服务器)。
- **导入** —— 支持主题和插件导入。
- 在 [Linux 环境](/zh/guide/more-features/linux-environment)和[运行时管理](/zh/guide/more-features/runtime-management)界面管理。

## 核心配置

由 `WordPressConfig` 支撑。

### 站点

- **站点标题**(`siteTitle`)。
- **站点语言**(`siteLanguage`)—— 例如 `zh_CN`。
- **固定链接结构**(`permalinkStructure`)—— 例如 `/%postname%/`。

### 管理员账户

- **管理员用户 / 邮箱 / 密码**(`adminUser`、`adminEmail`、`adminPassword`)。

### 主题与插件

- **主题**(`themeName`)—— 当前主题。
- **插件**(`plugins`、`activePlugins`)—— 已安装和已启用的插件。

### 来源与安装

- **来源类型**(`sourceType`)—— `BLANK` 或导入的项目(`sourceProjectId`)。
- **自动安装**(`autoInstall`)—— 首次运行时自动安装 WordPress。

### 服务器

- **端口**(`phpPort`)—— 通过[端口管理](/zh/guide/more-features/port-manager)分配。
- **自定义 PHP 扩展**(`customPhpExtensions`)—— 添加 `.so` / zend 扩展。

## 说明

- 因为使用 SQLite,WordPress 无需单独的数据库服务器即可运行。
- WebView 加载本地提供服务的 WordPress 实例。
