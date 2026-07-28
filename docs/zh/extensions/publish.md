# 发布到市场

模块市场是一个由 GitHub 支撑的社区 JS/CSS 扩展模块目录。没有后端 —— 应用直接从本仓库拉取目录文件,因此贡献就是一个普通的 pull request 流程。

::: info 规范规则
权威的提交规则、字段 schema、审核清单和 CI 校验位于 [`modules/README.md`](https://github.com/shiaho777/web-to-app/blob/main/modules/README.md)。本页是快速入门。
:::

## 目录布局

```
modules/
├── registry.json        # 面向应用的目录
├── submissions.json     # CI 生成的 PR / 贡献者元数据
├── README.md            # 贡献者指南
└── <module-folder>/     # 每个模块
```

应用同时拉取 `registry.json` 和 `submissions.json`,并**只显示两者中都存在的模块**,使应用内目录与实际合并的 PR 保持一致。

## 添加一个模块

1. 在 `modules/` 下创建一个 **kebab-case** 文件夹:

   ```
   modules/my-module/
   ├── module.json    # 必需
   ├── main.js        # 必需
   ├── style.css      # 可选
   └── icon.png       # 可选,≤256KB
   ```

2. 向 `registry.json` 添加一个条目:

   ```json
   {
     "id": "my-module",
     "path": "my-module",
     "name": "My Module",
     "description": "它做什么",
     "icon": "star",
     "category": "CONTENT_ENHANCE",
     "tags": ["demo"],
     "version": "1.0.0",
     "author": { "name": "You" },
     "runAt": "DOCUMENT_END",
     "permissions": ["DOM_ACCESS"],
     "urlMatches": [{ "pattern": "*://example.com/*" }],
     "hasCss": false
   }
   ```

   注意:在 `registry.json` 中,`version` 是一个 **semver 字符串**(不同于 `module.json` 里是对象),并有两个额外字段 —— `path` 和 `hasCss`。

3. 保持 `registry.json` 与 `module.json` 一致 —— `id`、`name`、`version`、`runAt` 和 `permissions` 必须相符。CI 会强制这一点。

4. 开一个 pull request。CI 会自动运行校验器。

## CI 校验什么

`python3 .github/scripts/ci/validate_modules.py` 检查:

- JSON 合法性与必填字段
- 允许的枚举值(`category`、`runAt`、`permissions`、`configItems.type`)
- `registry.json` ↔ `module.json` 一致性
- kebab-case 文件夹名;无孤儿或鬼条目;无重复 `id`/`path`
- 必需文件存在;`hasCss` 与 `style.css` 的存在一致
- `iconUrl` 大小/扩展名限制
- `main.js` 中无顶层 `return`
- `getConfig` 键与声明的 `configItems` 对应

## 浏览器扩展不同

社区市场**只收录 JS/CSS 模块**。MV3 浏览器扩展不是社区目录 —— **浏览器扩展** 标签页改为实时搜索 Chrome 网上应用店。见 [Chrome MV3 扩展](/zh/extensions/chrome-mv3)。

## 镜像

目录文件和模块图标先经由全局镜像,以 `raw.githubusercontent.com` 和 jsDelivr 作为自动回退,使商店在各地(包括中国大陆)都能快速加载。默认客户端缓存为一小时,因此合并的模块无需应用更新即可传播。
