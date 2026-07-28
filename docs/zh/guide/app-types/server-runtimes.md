# 服务运行时类型

这些类型在设备上把真实的服务运行时作为原生二进制 fork+exec,然后把 WebView 指向一个本地端口。可以理解为 Termux,但打包成可安装的 APK。运行时在首次使用时下载,并在 [Linux 环境](/zh/guide/more-features/dev-tools#linux-environment)和[运行时管理](/zh/guide/more-features/dev-tools#runtime-management)界面中管理。

## PHP {#php}

- **版本** —— PHP 8.4(来自 `pmmp/PHP-Binaries`),首次使用下载一次。
- **Composer** —— 提供 2.10.x。
- **扩展** —— 支持自定义原生扩展(`zend_extension`、`.so`)。
- **适用** —— 小型 PHP 应用、管理工具、演示。

## WordPress {#wordpress}

- **技术栈** —— WordPress 7.x,基于本地 PHP + SQLite(`sqlite-database-integration`)。
- **导入** —— 支持主题和插件导入。
- **适用** —— 便携站点、主题/插件演示、内容打包。

## Node.js {#node-js}

- **版本** —— Node.js 18.20.x。
- **进程模型** —— 通过加载 `libnode.so` 的原生 `node_launcher` 包装器,在独立的 `:nodejs` 操作系统进程中运行,使 V8 生命周期与宿主隔离。
- **扩展** —— 支持自定义原生 `.node` 插件。
- **导出嵌入** —— `libnode_bridge.so` + `libnode.so`(16KB 对齐)+ `libc++_shared.so`。
- **适用** —— Express/Fastify/Koa 应用、API、服务端演示。

## Python {#python}

- **版本** —— Python 3.14。
- **框架** —— Flask、Django、FastAPI(uvicorn)、Tornado 或内置 HTTP 服务器。
- **依赖** —— pip 解析进 `.pypackages`;支持自定义原生扩展。
- **版本化** —— 二进制名带版本号,未来升级不会写死路径。
- **适用** —— Flask/Django/FastAPI 应用、数据演示。

## Go {#go}

- **工具链** —— 官方 Go 1.26 Linux arm64(从 `dl.google.com` 下载 `.tar.gz`,国内用 USTC 镜像)。
- **设备端** —— `go build` / `go mod` / `go run`、`vendor/` 离线构建、通过原生 `go_exec_loader` 包装器提供静态服务。
- **导出嵌入** —— `libgo_exec_loader.so`。
- **适用** —— Gin/Echo/Fiber 服务、静态服务、编译工具。

## 共享运行时基础设施

- **端口管理器** —— 以冲突策略(`REASSIGN` / `AUTO_KILL` / `ALERT`)分配运行时端口,并在停止时清理。见[端口管理器](/zh/guide/more-features/dev-tools#port-manager)。
- **本地 DNS 桥** —— Android JVM 中的 HTTP CONNECT 代理,为那些打包二进制无法触达系统解析器的运行时提供可用的 DNS 和出站 HTTP。
- **下载** —— 大型运行时下载使用延长超时的下载客户端。

::: info 为什么 targetSdk 是 28?
生成的应用保持较低的 `targetSdk`(28),正是为了能从应用存储 fork+exec 这些原生运行时。AAB 导出器会单独为 Play 分发重写 `targetSdk`。见[构建与导出](/zh/guide/app-actions#build-apk)。
:::
