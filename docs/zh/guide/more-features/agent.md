# Agent

应用内的提示词驱动编码助手。从 [⋮ → Agent](/zh/guide/main-screen/more) 打开。

## 能生成什么

网页应用、扩展模块、油猴脚本、MV3 Chrome 扩展和本地运行时项目。

## 功能

- **会话** —— 每段对话有自己的标题和历史。
- **技能** —— 内置技能引导生成:`debug`、`explain`、`optimize`、`refactor`、`i18n`、`imagery`,加上各技术栈技能(`react-app`、`nodejs-app`、`php-app`、`python-app`、`go-app`、`vue-app`、`wordpress-app`、`html-app`、`multi-web-app`)和模块技能(`module-js`、`module-style`、`module-userscript`、`module-chrome-mv3`)。你可以在技能编辑器中编辑或添加技能。
- **计划模式** —— 先提出计划,等你批准后再应用改动(以计划模式徽标显示)。
- **韧性** —— 遇到 429/5xx 响应自动退避重试。

## 配置

Agent使用 [AI 设置](/zh/guide/more-features/ai-settings) 中配置的模型和密钥。

## 说明

Agent生成的是*源码*。要安装生成的扩展,请通过[扩展模块](/zh/guide/more-features/extension-modules)流程保存。
