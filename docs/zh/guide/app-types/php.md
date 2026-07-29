# PHP

在设备端 PHP 服务器上运行 PHP 项目;WebView 指向本地端口。

## 适用场景

小型 PHP 应用、管理工具和演示 —— 包括自定义 PHP 框架。

## 运行时

- **版本** —— PHP 8.4(来自 `pmmp/PHP-Binaries`),首次使用下载一次。
- **Composer** —— 提供 2.10.x 用于依赖管理。
- **扩展** —— 支持自定义原生扩展(`zend_extension`、`.so`)。
- 在 [Linux 环境](/zh/guide/more-features/linux-environment)和[运行时管理](/zh/guide/more-features/runtime-management)界面管理。

## 核心配置

由 `PhpAppConfig` 支撑。

### 项目

- **项目**(`projectId`/`projectName`)—— 要提供服务的 PHP 源码。
- **框架**(`framework`)—— 识别出的框架(若有)。
- **文档根**(`documentRoot`)—— Web 根目录。
- **入口文件**(`entryFile`)—— 默认 `index.php`。

### 服务器

- **端口**(`phpPort`)—— 通过[端口管理](/zh/guide/more-features/port-manager)分配。
- **环境变量**(`envVars`)—— 传给进程的键值对。

### 依赖与扩展

- **Composer**(`hasComposerJson`)—— 是否存在 `composer.json`(经 Composer 2.10.x 安装)。
- **PHP 扩展**(`phpExtensions`)—— 开关内置扩展。
- **自定义原生扩展**(`customPhpExtensions`)—— 添加 `.so` 扩展,每个为 `EXTENSION` 或 `ZEND_EXTENSION`,带加载顺序。

## 说明

- WordPress 运行在同一个 PHP 运行时上 —— 见 [WordPress](/zh/guide/app-types/wordpress)。
- 打包 PHP 二进制的 DNS 和出站 HTTP 经由本地 DNS 桥。
