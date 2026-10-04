# 广告拦截

使用 hosts 规则和 cosmetic 过滤在生成的应用中拦截广告。

**位置:**[编辑通用配置](/zh/guide/app-actions/edit-common-config/)编辑器中的 **广告拦截** 卡片。

## 选项

- **启用** —— 打开去广告(`adBlockEnabled`)。
- **规则** —— 自定义拦截规则(`adBlockRules`)。
- **订阅** —— 过滤订阅 URL(`adBlockSubscriptions`),从[Hosts 拦截](/zh/guide/more-features/hosts-adblock)中的内置列表与导入的自定义源中选取。

## 说明

- 在 [Hosts 拦截](/zh/guide/more-features/hosts-adblock)中全局管理过滤列表和订阅(内置 20 个社区列表)。
- 去广告同时贯通预览与导出:宿主去广告器服务预览,规则文本随导出的 APK 一起发布,由应用在启动时解析。列表很大时,进度显示 **Compiling ad-block rules...**,不再停在模板重打包那一步。
