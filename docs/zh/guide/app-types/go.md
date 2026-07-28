# Go

在设备上构建并运行 Go 项目;WebView 指向本地端口。

## 适用场景

Gin/Echo/Fiber 服务、静态文件服务和编译工具。

## 运行时

- **工具链** —— 官方 Go 1.26 Linux arm64(从 `dl.google.com` 下载 `.tar.gz`,国内用 USTC 镜像)。
- **设备端** —— `go build` / `go mod` / `go run`、`vendor/` 离线构建,以及通过原生 `go_exec_loader` 包装器提供静态服务。
- 在 [Linux 环境](/zh/guide/more-features/linux-environment)和[运行时管理](/zh/guide/more-features/runtime-management)界面管理。

## 导出要求

导出的 APK 嵌入 `libgo_exec_loader.so`。

## 关键配置

- **项目** —— Go 模块。
- **构建 / 运行** —— 设备端构建或直接运行;`vendor/` 启用离线构建。
- **端口** —— 通过[端口管理](/zh/guide/more-features/port-manager)分配。

## 说明

Go 工具链的 DNS 和 CA 信任经由 PHP 所用的同一个本地 JVM 桥。
