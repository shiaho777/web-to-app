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

- **加载模式**(`loadMode`)—— `AUTO`(新建应用的默认值)、`FILE`(文件协议)或 `LOCAL_HTTP`(本地服务器)。
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
- **本地文件**在页面能留在磁盘上时不申请 `INTERNET` 权限。自动模式对普通页面同样如此。必须使用 CDN、ES 模块或 WASM 的页面仍走本地服务并保留该权限,**本地服务**也始终保留。
- `AUTO` 对普通页面使用文件协议。页面需要真实源时改由本地 HTTP 服务器提供:CDN 地址、ES 模块(`type=module` 或动态 `import(`)、`.mjs`、`.wasm`、Service Worker、Web Manifest,或跨源隔离。已保存的 `FILE` 页面仍走文件协议,除非它需要这个源。只用 `fetch` 或 `localStorage` 不会强制走服务器。`LOCAL_HTTP` 始终走服务器。内置代码编辑器默认软换行,可以关掉,并会高亮 HTML。
- **HTML vs 前端 vs 离线包:**
  - **HTML** —— 你已经有静态文件。
  - [前端](/zh/guide/app-types/frontend) —— 你有一个框架项目,打包其构建输出。
  - [离线包](/zh/guide/app-types/offline-pack) —— 你从一个远程 URL 开始,把它抓取下来。
