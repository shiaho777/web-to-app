# 激活码验证

用激活码为生成的应用设门,使其仅在输入有效码后运行。

**位置:**[编辑通用配置](/zh/guide/app-actions/edit-common-config/)编辑器中的 **激活码** 卡片。

## 选项

- **启用** —— 打开激活门控(`activationEnabled`)。
- **激活码** —— 有效码列表(`activationCodeList`)。
- **每次验证** —— 每次启动都要求输入码,而非仅首次(`activationRequireEveryTime`)。
- **对话框配置** —— 自定义激活对话框(标题、副标题、输入框标签、按钮文本)。
- **远程激活** —— 对你自己经 EC P-256 签名的 HTTPS 端点校验,带离线策略(`activationRemoteConfig`)。
- **设备绑定(一码一次)** —— 仅远程验证可用(`activationRemoteConfig.deviceBound`)。激活请求携带设备标识,由服务器执行每码占座:每个码默认限 1 台设备(首台激活的设备占座),即一次性/单设备码;其他设备激活会被拒绝并显示服务器消息。同一设备卸载重装不丢失占座。需要能执行绑定的验证服务器——[参考 Worker](https://github.com/shiaho777/web-to-app/blob/main/examples/remote-activation-worker/README.md) 通过 `maxDevices` 开箱即用。

## 说明

- 本地校验在设备上检查码;远程校验调用你的服务器。见[远程激活参考](https://github.com/shiaho777/web-to-app/blob/main/.github/docs/remote-activation.md)。
- 本地校验无法实现设备绑定:应用在设备间没有任何共享状态,纯本地的"一码一次"无法强制执行。一次性码请使用远程验证并开启 **设备绑定**。
- 本地"次数限制"码的次数指激活后本机的应用启动次数,不是该码可被兑换的次数;清除应用数据后会重置。
- 应用列表会在设门的应用上显示激活标签。
