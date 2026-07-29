# HTML

把本地 HTML 打包进 APK,并从本地文件提供服务 —— 无需远程 URL。

## 适用场景

静态构建和离线 Web 应用,你已经有 HTML/CSS/JS 文件。

## 核心配置

由 `HtmlConfig` 支撑。

### 来源

- **项目目录**(`projectDir`)—— HTML/CSS/JS 文件夹,或导入时解压的 `.zip`。
- **文件**(`files`)—— 打包的文件,类型为 HTML/CSS/JS/图片/字体/其他。

### 入口

- **入口文件**(`entryFile`)—— 默认 `index.html`。

### 加载

- **加载模式**(`loadMode`)—— `AUTO`、`FILE`(文件协议)或 `LOCAL_HTTP`(本地服务器)。
- **端口**(`port`)—— 本地服务器端口(用于 `LOCAL_HTTP`)。
- **端口冲突模式**(`portConflictMode`)—— `AUTO_KILL` 或 `ALERT`。

### 能力

- **启用 JavaScript**(`enableJavaScript`)。
- **启用本地存储**(`enableLocalStorage`)。
- **允许文件访问**(`allowFileAccess`)—— 纯文件加载所必需。

### 外观

- **背景色**(`backgroundColor`)。

## 说明

- 生成的应用获得 `allowFileAccess`,使纯文件加载可离线工作。
- **HTML vs 前端 vs 离线包:**
  - **HTML** —— 你已经有静态文件。
  - [前端](/zh/guide/app-types/frontend) —— 你有一个框架项目,打包其构建输出。
  - [离线包](/zh/guide/app-types/offline-pack) —— 你从一个远程 URL 开始,把它抓取下来。
