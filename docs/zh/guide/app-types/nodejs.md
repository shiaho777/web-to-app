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

桥接/启动器/C++ 库由 shell 模板为全部四种 ABI 内置;`libnode.so` 属于按需下载内容,会按所选 APK 架构逐 ABI 嵌入 —— 上游 nodejs-mobile 包提供 `arm64-v8a`、`armeabi-v7a` 和 `x86_64`(不含 32 位 `x86`)。若所选 ABI 在本地运行时缓存中缺失,导出时会自动重新下载一次 Node.js 运行时并解压全部 ABI;若仍缺失,构建直接失败,避免产出在该架构上启动即报"libnode.so 未安装"的 APK(例如没有 ARM 转译的 x86_64 模拟器)。

## 核心配置

由 `NodeJsConfig` 支撑。

### 项目

- **项目**(`projectId`/`projectName`、`sourceProjectPath`)—— Node 源码。
- **框架**(`framework`)—— 识别出的框架(若有)。
- **Node 版本**(`nodeVersion`)。
- **含 node_modules**(`hasNodeModules`)—— 依赖是否被打包。

### 构建模式

- **构建模式**(`buildMode`):
  - `STATIC` —— 静态站点
  - `SSR` —— 服务端渲染
  - `API_BACKEND` —— API 后端(默认)
  - `FULLSTACK` —— 全栈

### 服务器

- **入口文件**(`entryFile`)—— 默认 `index.js`。
- **端口**(`serverPort`)—— 通过[端口管理](/zh/guide/more-features/port-manager)分配。
- **环境变量**(`envVars`)—— 传给进程的键值对。

### 原生插件

- **自定义 Node 扩展**(`customNodeExtensions`)—— 添加 `.node` 插件,各带加载顺序。
