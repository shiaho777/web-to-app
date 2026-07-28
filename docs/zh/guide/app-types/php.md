# PHP

在设备端 PHP 服务器上运行 PHP 项目;WebView 指向本地端口。

## 适用场景

小型 PHP 应用、管理工具和演示 —— 包括自定义 PHP 框架。

## 运行时

- **版本** —— PHP 8.4(来自 `pmmp/PHP-Binaries`),首次使用下载一次。
- **Composer** —— 提供 2.10.x 用于依赖管理。
- **扩展** —— 支持自定义原生扩展(`zend_extension`、`.so`)。
- 在 [Linux 环境](/zh/guide/more-features/linux-environment)和[运行时管理](/zh/guide/more-features/runtime-management)界面管理。

## 关键配置

- **项目** —— 要提供服务的 PHP 源码。
- **启动命令 / 入口** —— 服务器如何启动。
- **端口** —— 通过[端口管理](/zh/guide/more-features/port-manager)分配。

## 说明

- WordPress 运行在同一个 PHP 运行时上 —— 见 [WordPress](/zh/guide/app-types/wordpress)。
- 打包 PHP 二进制的 DNS 和出站 HTTP 经由本地 DNS 桥。
