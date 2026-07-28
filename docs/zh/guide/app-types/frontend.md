# 前端

打包一个*已构建*的前端项目 —— React、Vue、Vite 等框架的生产输出。

## 适用场景

你有一个框架项目,想把它的构建输出作为应用发布。

## 关键配置

- **输入** —— 构建输出目录。
- **框架** —— 被识别或选择(React、Vue、Vite 等)。
- **工具链** —— 当需要设备端构建步骤时,界面会链接到 [Linux 环境](/zh/guide/more-features/linux-environment)安装 Node 和构建工具。

## 说明

- **前端 vs HTML:** 前端用于框架项目的构建输出;[HTML](/zh/guide/app-types/html) 用于你已有的纯静态文件。
- 打包的前端在本地提供服务,因此离线可用的 SPA 表现良好。
