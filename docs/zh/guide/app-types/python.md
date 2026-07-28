# Python

在设备端 Python 服务器上运行 Python 项目;WebView 指向本地端口。

## 适用场景

Flask、Django、FastAPI、Tornado 应用,或内置 HTTP 服务器。

## 运行时

- **版本** —— Python 3.14。
- **框架** —— Flask、Django、FastAPI(uvicorn)、Tornado、内置 HTTP 服务器。
- **依赖** —— pip 解析进 `.pypackages`;支持自定义原生扩展。
- **版本化** —— 二进制名带版本号,未来升级不会写死路径。
- 在 [Linux 环境](/zh/guide/more-features/linux-environment)和[运行时管理](/zh/guide/more-features/runtime-management)界面管理。

## 关键配置

- **项目** —— Python 源码。
- **启动命令** —— 例如 `python app.py` 或 `uvicorn main:app`。
- **端口** —— 通过[端口管理](/zh/guide/more-features/port-manager)分配。
