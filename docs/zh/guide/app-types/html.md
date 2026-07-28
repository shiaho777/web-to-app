# HTML

把本地 HTML 打包进 APK,并从本地文件提供服务 —— 无需远程 URL。

## 适用场景

静态构建和离线 Web 应用,你已经有 HTML/CSS/JS 文件。

## 关键配置

- **输入** —— 一个 HTML/CSS/JS 文件夹,或导入时解压的 `.zip`。
- **入口文件** —— 默认 `index.html`。
- **加载模式** —— 内容如何被提供服务。
- **端口与端口冲突模式** —— 用于提供文件的本地服务器。
- **JavaScript 与本地存储** —— WebView 的开关。

## 说明

- 生成的应用获得 `allowFileAccess`,使纯文件加载可离线工作。
- **HTML vs 前端 vs 离线包:**
  - **HTML** —— 你已经有静态文件。
  - [前端](/zh/guide/app-types/frontend) —— 你有一个框架项目,打包其构建输出。
  - [离线包](/zh/guide/app-types/offline-pack) —— 你从一个远程 URL 开始,把它抓取下来。
