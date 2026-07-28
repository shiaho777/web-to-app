# 贡献指南

感谢你为 WebToApp 做贡献。规范的贡献者指南位于仓库中:

👉 [`.github/CONTRIBUTING.md`](https://github.com/shiaho777/web-to-app/blob/main/.github/CONTRIBUTING.md)

## 贡献通道

| 通道 | 你做什么 | 指南 |
| --- | --- | --- |
| `modules/` | 把一个社区模块发布到应用内市场 | [modules/README.md](https://github.com/shiaho777/web-to-app/blob/main/modules/README.md) |
| Issues | 报告 bug 或请求功能 | [GitHub Issues](https://github.com/shiaho777/web-to-app/issues) |
| 代码 | 在 Android 客户端修复 bug 或构建功能 | [CONTRIBUTING.md](https://github.com/shiaho777/web-to-app/blob/main/.github/CONTRIBUTING.md) |
| 文档 | 改进本文档站点 | 在 `docs/` 下编辑并开 PR |

## 基本规则

- **做对的事,而非最小的 diff。** 如果一个修复需要重构、重命名或触及多个文件,就去做。匹配周围代码已有的模式与约定。
- **不要添加版权或许可证头**,除非被要求。
- **不要提交密钥**、`local.properties`、密钥库或 IDE/缓存垃圾。
- **优先用 pull request**,而非直接推送到 `main`。
- **GitHub Issues 和 PR 必须用英文撰写** —— 标题、正文和交付评论。本地聊天可用任何语言。

## 交付循环

交付改动时,端到端走 Issue → 分支 → PR → CI → 合并循环。在 PR 合并且 CI 变绿之前,不要关闭 Issue。`main` 受分支保护,需要 PR 加上通过的 `check` 状态。

## 开 PR 之前

- 改动了 shell 成员、导出打包或配置字段?运行[验证命令](/zh/developer/recipes#验证命令)。
- 新增宿主 UI 字符串?补全[全部 10 种语言](/zh/developer/i18n)。
- 新增影响导出的编辑器设置?走完[整条链路](/zh/developer/config-drift#添加一个影响生成-apk-的设置)。
