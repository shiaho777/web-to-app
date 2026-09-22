# 发布到市场

插件市场是由 GitHub 承载的社区插件目录，没有后端——App 直接从本仓库拉取目录文件，投稿就是普通的 PR 流程。

::: info 权威规则
提交规则、字段 Schema、审核 Checklist 和 CI 校验以 [`modules/README.md`](https://github.com/shiaho777/web-to-app/blob/main/modules/README.md) 为准。本页是快速上手。
:::

## 目录结构

```
modules/
├── registry.json        # 面向 App 的目录索引
├── submissions.json     # CI 生成的 PR/贡献者元数据
├── README.md            # 投稿指南
└── <plugin-folder>/     # 每个插件包
```

App 同时拉取 `registry.json` 和 `submissions.json`，**只展示两边都存在的插件**，保证市场目录和已合并 PR 严格对齐。

## 添加插件

1. 在 `modules/` 下建一个 **kebab-case** 文件夹：

   ```
   modules/my-plugin/
   ├── plugin.json    # 必需
   ├── plugin.html    # 必需 —— 页面脚本（`<script type="hcj/page">`）+ 面板文档合一
   └── icon.png       # 可选，≤256KB
   ```

   旧版 `main.js` / `style.css` / `panel.html` 多文件包仍可通过校验并安装；新投稿请用单文件 `plugin.html`。

2. 在 `registry.json` 中加条目：

   ```json
   {
     "id": "my-plugin",
     "path": "my-plugin",
     "name": "My Plugin",
     "description": "做什么的",
     "icon": "star",
     "category": "CONTENT_ENHANCE",
     "tags": ["demo"],
     "version": "1.0.0",
     "author": { "name": "You" },
     "runAt": "DOCUMENT_END",
     "permissions": ["STORAGE"],
     "urlMatches": [{ "pattern": "*://example.com/*" }],
     "hasCss": false
   }
   ```

   注册表保留旧字段名（大写下划线的 `runAt`、对象形式的 `urlMatches`、`hasCss`），保证旧客户端可用——`plugin.json` 用新字段名（`document_end`、`matches`）。

3. 保持 `plugin.json` 与 `registry.json` 一致——`id`、`name`、`version` 必须相同。已退役的 `module.json` 格式会被 CI 拒绝。

4. 提 PR，CI 自动跑校验。

## CI 校验什么

`python3 .github/scripts/ci/validate_modules.py` 检查：

- JSON 合法性与必填字段（`plugin.json`、`registry.json`）
- 枚举取值（`runAt`、`permissions`、registry 枚举）
- `plugin.json` ↔ `registry.json` 一致性（`id`/`name`/`version`）
- kebab-case 文件夹名；无孤儿/幽灵条目；`id`/`path` 不重复
- 必需文件齐全；`hasCss` 与 `style.css` 是否存在一致
- `iconUrl` 大小/扩展名限制
- 页面脚本（`hcj/page` 块 / `main.js`）无顶层 `return`

## 浏览器扩展另算

社区市场**只收录 HCJ 插件包**。MV3 浏览器扩展不是社区目录——**浏览器扩展** tab 实时搜索 Chrome Web Store。见 [Chrome MV3 扩展](/zh/extensions/chrome-mv3)。

## 镜像

目录文件和插件图标优先走全球镜像，`raw.githubusercontent.com` 与 jsDelivr 自动兜底，各地（含中国大陆）都快。客户端默认缓存一小时，插件合入后无需发版即可触达。
