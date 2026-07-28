# 本地服务运行时

WebToApp 能把真实的服务运行时作为原生二进制,直接从应用存储 fork+exec —— 如同 Termux,但打包成可安装的 APK。生成应用中的 WebView 指向由运行时提供的本地端口。

## 支持的运行时

| 运行时 | 版本 | 说明 |
| --- | --- | --- |
| **Node.js** | 18.20.x | 通过加载 `libnode.so` 的原生 `node_launcher` 包装器,在独立的 `:nodejs` 操作系统进程中运行。支持自定义原生 `.node` 扩展。 |
| **PHP** | 8.4 | 来自 `pmmp/PHP-Binaries`,首次使用下载一次。Composer 2.10.x。自定义原生扩展(`zend_extension`、`.so`)。 |
| **Python** | 3.14 | Flask、Django、FastAPI(uvicorn)、Tornado、内置 HTTP 服务器。pip 依赖解析进 `.pypackages`。二进制名带版本号。 |
| **Go** | 1.26 | 官方 Linux arm64 工具链(国内用 USTC 镜像)。设备端 `go build` / `go mod` / `go run`、`vendor/` 离线构建、通过 `go_exec_loader` 提供静态服务。 |
| **WordPress** | 7.x | 基于本地 PHP + SQLite(`sqlite-database-integration`)。主题与插件导入。 |

**Linux 环境** 界面管理 Node、PHP、Python 的工具链与依赖。

## 运行时如何协调

- **端口管理器** —— 通过广播接收器在多个生成应用间协调运行时端口,冲突策略:`REASSIGN`、`AUTO_KILL` 或 `ALERT`。运行时通过端口管理器分配端口,并在停止时清理。
- **本地 DNS 桥代理** —— Android JVM 中的 HTTP CONNECT 代理,为那些 musl/打包二进制无法触达系统解析器的运行时,提供可用的 DNS 解析和出站 HTTP。
- **下载** —— 大型运行时下载使用延长超时的下载客户端,而非默认的短超时客户端。

## 导出要求

导出运行时应用时,所需的原生库会被嵌入 APK:

- **Node.js** → `libnode_bridge.so` + `libnode.so`(16KB 对齐)+ `libc++_shared.so`
- **Go** → `libgo_exec_loader.so`

::: info 16KB 页对齐
`libnode.so` 和其他大型 ELF 原生库为 Android 15+ 设备做了 16KB 对齐。原生启动器在 `dlopen` 之前启用 16KB 应用兼容。
:::

---

::: tip 各运行时的安装指南即将推出
每个运行时的详细首次安装、依赖安装与故障排除正在编写中。
:::
