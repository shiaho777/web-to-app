# 激活码验证

用激活码为生成的应用设门,使其仅在输入有效码后运行。

**位置:**[编辑通用配置](/zh/guide/app-actions/edit-common-config/)编辑器中的 **激活码** 卡片。

## 选项

- **启用** —— 打开激活门控(`activationEnabled`)。
- **激活码** —— 有效码列表(`activationCodeList`)。
- **每次验证** —— 每次启动都要求输入码,而非仅首次(`activationRequireEveryTime`)。
- **对话框配置** —— 自定义激活对话框(标题、副标题、输入框标签、按钮文本)。
- **远程激活** —— 对你自己经 EC P-256 签名的 HTTPS 端点校验,带离线策略(`activationRemoteConfig`)。

## 说明

- 本地校验在设备上检查码;远程校验调用你的服务器。见[远程激活参考](https://github.com/shiaho777/web-to-app/blob/main/.github/docs/remote-activation.md)。
- 应用列表会在设门的应用上显示激活标签。
