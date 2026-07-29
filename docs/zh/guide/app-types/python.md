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

## 核心配置

由 `PythonAppConfig` 支撑。

### 项目

- **项目**(`projectId`/`projectName`、`sourceProjectPath`)—— Python 源码。
- **框架**(`framework`)—— 识别出的框架(若有)。
- **Python 版本**(`pythonVersion`)。

### 入口与服务器

- **入口文件**(`entryFile`)—— 默认 `app.py`。
- **入口模块**(`entryModule`)—— 用于模块式入口(如 `main:app`)。
- **服务器类型**(`serverType`)—— `builtin`、uvicorn 等。
- **端口**(`serverPort`)—— 通过[端口管理](/zh/guide/more-features/port-manager)分配。
- **环境变量**(`envVars`)—— 传给进程的键值对。

### 依赖与扩展

- **requirements 文件**(`requirementsFile`)—— 默认 `requirements.txt`。
- **含 pip 依赖**(`hasPipDeps`)—— 依赖是否解析进 `.pypackages`。
- **自定义原生扩展**(`customPythonExtensions`)—— 添加 `.so` 扩展,各带加载顺序。
