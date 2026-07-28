# Node.js

在独立的设备端 Node 服务器中运行 Node.js 项目;WebView 指向本地端口。

## 适用场景

Express/Fastify/Koa 应用、API 和服务端演示。

## 运行时

- **版本** —— Node.js 18.20.x。
- **进程模型** —— 通过加载 `libnode.so` 的原生 `node_launcher` 包装器,在独立的 `:nodejs` 操作系统进程中运行,使 V8 生命周期与宿主隔离。
- **插件** —— 支持自定义原生 `.node` 扩展。
- 在 [Linux 环境](/zh/guide/more-features/linux-environment)和[运行时管理](/zh/guide/more-features/runtime-management)界面管理。

## 导出要求

导出的 APK 嵌入:

- `libnode_bridge.so`
- `libnode.so`(为 Android 15+ 做 16KB 对齐)
- `libc++_shared.so`

缺少任何一个都会在运行时导致 `loadNode` / `loadJniBridge` 失败。

## 关键配置

- **项目** —— Node 源码。
- **启动命令** —— 例如 `node server.js`。
- **端口** —— 通过[端口管理](/zh/guide/more-features/port-manager)分配。
