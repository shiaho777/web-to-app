# Contributing to WebToApp

Thanks for taking the time to help. WebToApp moves fastest when contributions
stay **small and well-scoped** — pick one of the lanes below and ignore the
rest.

> **English** · [简体中文](#贡献-webtoapp中文)

This guide targets **WebToApp 2.5.5** (`versionCode 61`).

---

## Lanes

| You want to… | Go to | Effort |
| --- | --- | --- |
| Publish a JS/CSS module to the in-app **Module Market** | [`modules/README.md`](../modules/README.md) | hours |
| File a bug, request a feature, or ask a question | [GitHub Issues](https://github.com/shiaho777/web-to-app/issues) | minutes |
| Fix a bug or build a feature in the Android client | This guide ↓ | days |

If you're not sure which lane fits, open an issue or a discussion first. There
is no need to write code before there's agreement on the shape of the change.

---

## Module Market submissions

The fastest way to ship something useful to every WebToApp user is to publish
a module. The canonical Module Market guide lives in
[`modules/README.md`](../modules/README.md). Use that file for the full
schema, field rules, reviewer checklist, and CI validation details. The short
version here is only meant to help you pick the right contribution lane:

1. Fork the repo.
2. Add `modules/<your-module>/module.json` and `main.js` (plus `style.css` if
   you need CSS).
3. Add an entry to `modules/registry.json`.
4. Open a PR.

The market has **no backend**. Clients read `registry.json` and
`submissions.json`, and only merged modules show up in the catalog. Module
changes are validated in CI by `.github/scripts/ci/validate_modules.py`:

```bash
python3 .github/scripts/ci/validate_modules.py
```

---

## Code contributions

> The canonical working guide for AI coding agents and deep code
> contributors is [`AGENTS.md`](../AGENTS.md) — it documents how the editor,
> export pipeline, shell template, and runtime connect, the dual preview/export
> paths, config-field drift rules, packaging constraints, and common change
> recipes. Read it before any non-trivial change to the build, shell sync,
> export packaging, or config fields.

### Before you write code

- Search [issues](https://github.com/shiaho777/web-to-app/issues) for prior
  discussion of the same idea.
- For non-trivial changes, open an issue first describing the problem and the
  approach you have in mind. This is much cheaper than rewriting after review.
- **Avoid adding new dependencies.** The lists in `app/build.gradle.kts` and
  `shell/build.gradle.kts` are intentionally restrained — the project signs and
  packages APKs in-process, and the shell template pins `targetSdk = 28` on
  purpose because generated apps rely on fork+exec native runtimes. New
  dependencies need a strong justification, and host-only dependencies must
  never leak into the shell template.

### Local setup

You'll need:

- Android Studio Hedgehog or newer
- JDK 17
- The Gradle wrapper pins Gradle 9.4.1 — no system Gradle install required

```bash
git clone https://github.com/shiaho777/web-to-app.git
cd web-to-app
./gradlew assembleDebug
```

The repo has three Gradle modules: **`app`** (the full builder and host),
**`shell`** (the runtime template embedded in generated APKs), and
**`clone-host`** (the host code used by app cloning). Shared runtime code is
authored **only** under `app/` and synchronized into `shell/` at build time
(`shell/build/generated/` — never edit `shell/src` by hand; it is
regenerated and partly git-ignored).

Run the checks before submitting:

```bash
./gradlew :app:compileStandardDebugKotlin -x syncCloneHostDex --no-configuration-cache
./gradlew :app:testStandardDebugUnitTest --no-configuration-cache -PskipShellTemplateSync=true
./gradlew :app:checkConfigFieldDrift --no-configuration-cache
```

If you touched shell-synced runtime code or export packaging, also rebuild the
template you touched:

```bash
./gradlew :shell:assembleRelease :app:syncShellTemplateApk --no-configuration-cache
```

> Native code (`node_launcher`, `go_exec_loader`, the APK optimizer) builds
> via CMake per-ABI and needs the Android NDK + CMake installed through the
> SDK Manager. CI installs `cmake;3.22.1` and `ndk;28.2.13676358`.

### Where things live

| Area | Path |
| --- | --- |
| App types & central config (`AppType`, `WebApp`) | `app/src/main/java/com/webtoapp/data/model/` |
| On-device APK builder / signer | `app/src/main/java/com/webtoapp/core/apkbuilder/` |
| Server runtimes (Node / PHP / Python / Go / WordPress) | `app/src/main/java/com/webtoapp/core/{nodejs,php,python,golang,wordpress}/` |
| WebView engine, native bridge, fingerprint disguise | `app/src/main/java/com/webtoapp/core/{webview,engine,appearance}/` |
| Shell config + generated-APK entry points | `app/src/main/java/com/webtoapp/core/shell/` |
| Preview players (`ui/gallery/`, `ui/media/`) vs packaged players (`ui/shell/`) | `app/src/main/java/com/webtoapp/ui/` |
| Extension modules, Module Market, Agent | `app/src/main/java/com/webtoapp/core/{extension,market,agent}/` |
| Compose UI screens & design system | `app/src/main/java/com/webtoapp/ui/` |
| DI graph (source of truth) | `app/src/main/java/com/webtoapp/di/AppModule.kt` |

### Preview ≠ export: the rule behind most rejections

The host app (preview) and generated APKs (export) run **different code**:

| | Preview (`:app`) | Exported APK |
| --- | --- | --- |
| UI | `ui/gallery/`, `ui/media/`, `WebViewManager` live | `ui/shell/*` synced copy |
| Config | Editor / in-memory models | JSON assets via `ShellModeManager` |

A feature is done only when it works on **both** paths. Concretely, an editor
setting that must affect generated APKs has to flow through the full chain —
model → `ApkConfig` JSON → shell config → shell-synced runtime — and a
player-screen behavior has to be implemented in **both** the preview and the
shell composable. Three gates enforce this; run them, don't argue with them:

- `checkConfigFieldDrift` — model/JSON/shell field names must match
  (Gson silently drops mismatches).
- `WebViewConfigBooleanCoverageTest` — every new `WebViewConfig` Boolean must
  be listed in `flipAllBooleans()` and survive the export round-trip.
- `ShellUiParityTest` — every model field read by host player UI must also be
  read by shell player UI (or carry an explicit `allow` reason). Add your new
  per-type player pair to it.

### Coding conventions

WebToApp leans on the Kotlin and Jetpack Compose patterns already in the
codebase. A few rules worth calling out:

- **Comments explain *why*, not *what*.** Non-obvious decisions, cross-file
  contracts, and issue references (`#781`) get a short comment; clear names
  and small functions carry the rest. Don't narrate readable code.
- **Build new UI on the Wta design system.** Everything renders through
  `com.webtoapp.ui.design`. The older `Premium*` / `Enhanced*` / `Settings*`
  components are retained as permanent alias layers over the Wta internals —
  don't add new ones, and you don't need to rip them out. A build-time audit
  (`.github/scripts/audit_ui_design_system.py`, wired into `build.gradle.kts`) tracks
  legacy UI debt against `.github/scripts/ui_design_allowlist.txt`.
- **Reuse the design tokens** in `ui/design/WtaTokens.kt` for spacing, radius,
  alpha, and elevation. Don't hard-code numbers. Editor config cards share one
  layout grammar (see `AGENTS.md` recipe 12) — copy the neighbouring cards,
  don't invent your own, and verify on the emulator, not just by compiling.
- **Strings must cover all 10 supported languages.** UI copy lives in
  `core/i18n/Strings.kt` (facade `object Strings` + in-file `StringsA`…`StringsE`,
  split only for the JVM constant pool). Supported: Chinese, English, Arabic,
  Portuguese, Spanish, French, German, Russian, Japanese, Korean.
  Every new or changed user-visible `when (Strings.lang)` block **must** have
  real translations for **all 10** branches with no `else ->` — do not leave
  pt/es/fr/de/ru/ja/ko as English placeholders. Brand names and pure format
  tokens may match English. If you truly cannot translate one language, use
  English there and flag it in the PR.
- **Never load user-visible text from `R.string`.** Resource files only cover
  zh/en/ar, so lookups silently fall back to Chinese for the other 7 locales.
  `R.string` is reserved for non-localised resources.
- **No new top-level singletons** unless you discuss it first. The DI graph in
  `di/AppModule.kt` is the source of truth.
- **The Native Bridge is capability-gated.** Any method exposed to web content
  via `@JavascriptInterface` must be guarded by the per-capability allow-list
  (`NativeBridgeCapabilities`). Never expose a native capability to arbitrary
  pages without a gate. PRs that bypass this will be rejected.
- **Preview and packaged runtime are two paths.** See *Preview ≠ export*
  above. Shared runtime sources are authored only under `app/` and synced into
  `shell/` at build time — do not hand-edit anything under `shell/src`.
- **Avoid catching `Exception` to silence errors.** If recovery is impossible,
  log via `AppLogger` and re-throw or return a failed `Result`.

### Security

Anything touching the WebView, file IO, APK signing, or the native bridge has
downstream impact on **every generated app**, so it gets extra scrutiny.
Specifically, PRs will be declined if they:

- store or expose credentials/secrets to web content,
- widen the native surface exposed to arbitrary pages without a capability
  gate,
- weaken APK signing, isolation, or the fingerprint-disguise defaults,
- bundle unrelated changes inside a large diff.

### Commit messages

We don't enforce Conventional Commits, but a clear subject line and a wrapped
body that explains *why* the change exists make reviews much faster. Example:

```
ModuleMarket: cache registry.json for an hour

Repeatedly hitting raw.githubusercontent.com on every screen open is wasteful
and triggers GitHub's anonymous rate limit on slow connections. Cache the
parsed registry under cache/module_market/ and treat anything fresher than
an hour as authoritative; the refresh button always bypasses the cache.
```

### Pull requests

- Branch from `main`. Keep PRs focused — one logical change per PR.
- **Write Issues and PRs in English** — titles, bodies, and review threads.
- Describe the user-visible effect in the PR body, not just the code change.
- The standard loop is Issue → branch → PR (`Fixes #N`) → green CI → merge.
  Issues close on merge, never on PR open or red CI.
- If your PR touches the build system, native code, or APK packaging, attach
  the output of the template rebuild above (or note the failure if it fails
  on your machine).
- CI runs on every PR. A green CI is required before merge.
- Never commit secrets, keystores, `local.properties`, or IDE/cache junk.

### Reviewer expectations

Maintainer reviews look for:

- **Correctness.** Does the change do what the PR description says? Was it
  verified by execution (tests, build output), not just by reading?
- **Safety.** See *Security* above.
- **Scope discipline.** Drive-by refactors expand the review surface. Land
  them in a separate PR.
- **Style fit.** See *Coding conventions* above.
- **Both paths.** Preview-only features and export-only wiring are the two
  classic failure modes — see *Preview ≠ export* above.

---

## Code of conduct

Feedback is required to be precise. "This code is garbage" is acceptable only
when accompanied by which code, why, and what would be less so. The maintainer
(**shiaho**) has on file a standing waiver covering criticism of himself, his
judgment, and his life choices. Praise unaccompanied by a working patch is
logged and otherwise disregarded.

---

<a id="贡献-webtoapp中文"></a>
## 贡献 WebToApp（中文）

非常感谢你愿意花时间。WebToApp 的迭代速度取决于"小而聚焦"的贡献——下面三条路
里挑一条走，其他的先忽略。

本指南对应 **WebToApp 2.5.5**（`versionCode 61`）。

### 你想做什么？

| 你想…… | 路径 | 投入 |
| --- | --- | --- |
| 给应用内的 **模块市场** 提交一个 JS/CSS 模块 | [`modules/README.md`](../modules/README.md) | 几小时 |
| 报 Bug、提 Feature、问问题 | [GitHub Issues](https://github.com/shiaho777/web-to-app/issues) | 几分钟 |
| 修 Bug 或在 Android 客户端里做新功能 | 见下方代码贡献小节 | 几天 |

不确定走哪条，先开 issue 或 discussion——动手前对齐方向，比写完再返工便宜
得多。

### 模块市场贡献

让你的工作触达每一个用户最快的方式就是提一个模块。Schema、审核 Checklist 和
CI 校验细节的主文档是 [`modules/README.md`](../modules/README.md)。这里仅保
留一个极简入口，方便你先判断自己是不是走这条贡献路线：

1. Fork 本仓库
2. 新建 `modules/<你的模块>/module.json` 和 `main.js`（需要 CSS 时再加
   `style.css`）
3. 在 `modules/registry.json` 里加一行索引
4. 提 PR

市场**没有后端**。客户端读取 `registry.json` 和 `submissions.json`，只有已合
并的模块才会在市场出现。模块改动会经过 CI 的
`.github/scripts/ci/validate_modules.py` 校验，提 PR 前建议先本地跑：

```bash
python3 .github/scripts/ci/validate_modules.py
```

### 代码贡献

> 面向 AI 编码助手与深度代码贡献者的权威工作指南是
> [`AGENTS.md`](../AGENTS.md) —— 它讲清了编辑器、导出流水线、shell 模板与运行时
> 之间如何连接,以及双路径预览 / 导出、配置字段漂移规则、打包约束和常见改动
> 配方。对构建、shell 同步、导出打包或配置字段的任何非平凡改动,动手前请先读。

**动手前**

- 在 [issues](https://github.com/shiaho777/web-to-app/issues) 里先搜一下
  类似讨论
- 较大的改动请先开 issue 说明要解决的问题和方案
- **谨慎引入新依赖**。`app/build.gradle.kts` 与 `shell/build.gradle.kts` 的依
  赖列表刻意保持精简——本项目全程在设备内签名打包 APK，shell 模板也特意把
  `targetSdk` 锁在 28，因为生成应用依赖 `fork`、`exec` 原生运行时。新依赖需
  要充分理由，且宿主专用依赖绝不能漏进 shell 模板。

**本地环境**

- Android Studio Hedgehog 或更新版本
- JDK 17
- Gradle wrapper 已锁定 Gradle 9.4.1，无需系统安装 Gradle

```bash
git clone https://github.com/shiaho777/web-to-app.git
cd web-to-app
./gradlew assembleDebug
```

仓库有三个 Gradle 模块：**`app`**（完整构建器和宿主）、**`shell`**（嵌入生
成 APK 的运行时模板）和 **`clone-host`**（应用克隆使用的宿主代码）。共享运
行时代码以 `app/` 为唯一事实来源，构建时同步到 `shell/`（`shell/build/` 下，
不要手改 `shell/src`——它是再生成的产物，部分被 git 忽略）。

提交前请跑通：

```bash
./gradlew :app:compileStandardDebugKotlin -x syncCloneHostDex --no-configuration-cache
./gradlew :app:testStandardDebugUnitTest --no-configuration-cache -PskipShellTemplateSync=true
./gradlew :app:checkConfigFieldDrift --no-configuration-cache
```

动到 shell 同步的运行时代码或导出打包，还要重建你碰过的模板：

```bash
./gradlew :shell:assembleRelease :app:syncShellTemplateApk --no-configuration-cache
```

> 原生代码（`node_launcher`、`go_exec_loader`、APK 优化器）按 ABI 经 CMake
> 编译，需要通过 SDK Manager 安装 Android NDK + CMake。CI 安装的是
> `cmake;3.22.1` 与 `ndk;28.2.13676358`。

**代码大致位置**

| 区域 | 路径 |
| --- | --- |
| 应用类型与核心配置（`AppType`、`WebApp`） | `app/src/main/java/com/webtoapp/data/model/` |
| 设备端 APK 打包 / 签名 | `app/src/main/java/com/webtoapp/core/apkbuilder/` |
| 服务端运行时（Node / PHP / Python / Go / WordPress） | `app/src/main/java/com/webtoapp/core/{nodejs,php,python,golang,wordpress}/` |
| WebView 引擎、原生桥、指纹伪装 | `app/src/main/java/com/webtoapp/core/{webview,engine,appearance}/` |
| Shell 配置与生成 APK 入口 | `app/src/main/java/com/webtoapp/core/shell/` |
| 预览播放器（`ui/gallery/`、`ui/media/`）vs 打包播放器（`ui/shell/`） | `app/src/main/java/com/webtoapp/ui/` |
| 扩展模块、模块市场、Agent | `app/src/main/java/com/webtoapp/core/{extension,market,agent}/` |
| Compose UI 与设计系统 | `app/src/main/java/com/webtoapp/ui/` |
| DI 依赖图（单一事实来源） | `app/src/main/java/com/webtoapp/di/AppModule.kt` |

**预览 ≠ 导出：大多数打回的原因**

宿主应用（预览）和生成的 APK（导出）跑的是**两套代码**：

| | 预览（`:app`） | 导出的 APK |
| --- | --- | --- |
| 界面 | `ui/gallery/`、`ui/media/`、实时 `WebViewManager` | `ui/shell/*` 的同步副本 |
| 配置 | 编辑器 / 内存模型 | 经 `ShellModeManager` 读取的 JSON 资源 |

一个功能只有两边都通才算做完。编辑器开关要影响生成 APK，必须走完整条
链——模型 → `ApkConfig` JSON → shell 配置 → shell 同步的运行时代码；而播放
器界面的行为要在预览和壳两套 composable 里各实现一遍。三道门禁负责兜底，
跑过它们，别跟它们争：

- `checkConfigFieldDrift`——模型 / JSON / shell 字段名必须一致（Gson 会静默
  丢掉对不上的字段）。
- `WebViewConfigBooleanCoverageTest`——`WebViewConfig` 每新增一个 Boolean 都
  必须进 `flipAllBooleans()` 并走完导出往返。
- `ShellUiParityTest`——宿主播放器读的每个模型字段，壳播放器也必须读（或写
  明 `allow` 理由）。新增每类播放器时照样子扩展。

**代码风格**

- **注释解释 *为什么*，不复述 *干了什么*。** 反直觉的决策、跨文件约定、
  issue 引用（`#781`）值得一句短注释；命名清晰的小函数不需要解说。不要给
  易读的代码写旁白。
- **新 UI 一律构建在 Wta 设计系统之上**——所有界面通过 `com.webtoapp.ui.design`
  渲染。旧的 `Premium*` / `Enhanced*` / `Settings*` 组件作为 Wta 内部实现的
  永久别名层保留——不要再新增，也无需强行替换。构建期有一个审计脚本
  （`.github/scripts/audit_ui_design_system.py`，接进 `build.gradle.kts`）按
  `.github/scripts/ui_design_allowlist.txt` 跟踪历史 UI 债务。
- 复用 `ui/design/WtaTokens.kt` 里的设计 token（间距、圆角、透明度、高度），
  别硬编码数字。编辑器配置卡片共用一套排版语法（见 `AGENTS.md` recipe 12）——
  照抄相邻卡片，不要自创，跑模拟器验效果，不要只编译。
- **字符串必须覆盖全部 10 种已支持语言**：文案在 `core/i18n/Strings.kt`
  （facade `object Strings` + 同文件 `StringsA`…`StringsE`，拆分只为常量池）。
  已支持：中 / 英 / 阿 / 葡 / 西 / 法 / 德 / 俄 / 日 / 韩。
  新增或修改面向用户的 `when (Strings.lang)` **必须**为 10 个分支写真实翻译，
  且不许写 `else ->`；禁止把 pt/es/fr/de/ru/ja/ko 继续当英文占位。品牌名、
  纯格式符可与英文相同。某语实在不会翻可暂填英文并在 PR 标明。
- **面向用户的文案绝不用 `R.string`。** 资源文件只有 zh/en/ar 三套，其他 7
  语会静默回落到中文。`R.string` 只保留给无需本地化的资源。
- 引入新的全局单例前请先讨论；`di/AppModule.kt` 是单一事实来源
- **原生桥是按能力门禁的**：任何通过 `@JavascriptInterface` 暴露给网页的方法，
  都必须经过逐能力白名单（`NativeBridgeCapabilities`）。绝不要在没有门禁的
  情况下把原生能力暴露给任意页面。绕过门禁的 PR 会被拒
- **预览和打包运行时是两条路径**：见上文《预览 ≠ 导出》。共享运行时代码只改
  `app/`，构建时同步进 `shell/`；不要手改 `shell/src` 下的任何东西
- 不要 catch 然后吞掉异常；用 `AppLogger` 记录后重新抛出或返回失败的 `Result`

**安全**

动到 WebView、文件 IO、APK 签名或原生桥的改动会影响**每一个生成出来的
应用**，因此审核更严。出现以下情况的 PR 会被拒：

- 把凭据 / 密钥存储或暴露给网页内容
- 在没有能力门禁的情况下扩大暴露给任意页面的原生面
- 削弱 APK 签名、隔离或指纹伪装的默认强度
- 在一个大 diff 里夹带不相关的改动

**Commit 消息**

不强制 Conventional Commits，但请把"为什么"写进正文。示例见英文段。

**Pull Request**

- 从 `main` 分出分支，每个 PR 只解决一件事
- **Issue 与 PR 请用英文写**——标题、正文、评审讨论
- PR 描述写"用户看得到的效果"，不只是代码 diff
- 标准流程是 Issue → 分支 → PR（`Fixes #N`）→ CI 变绿 → 合并。
  Issue 只在合并时关闭，开 PR 时不关、CI 红时不关
- 改动涉及构建系统、原生代码或 APK 打包时，附上上面模板重建命令的结果
- CI 必须绿色才会合并
- 不要提交密钥、keystore、`local.properties` 或 IDE / 缓存垃圾

**Review 标准**

- **正确性**——是否真的做了 PR 描述里写的事；是否经过执行验证（测试、
  构建产物），而不只是"读起来对"
- **安全**——见上面《安全》小节
- **聚焦**——顺手做的重构请单独发 PR
- **风格**——参考上面那条
- **两条路径**——只在预览生效的功能和只在导出接线的配置是两类经典翻车——见
  上文《预览 ≠ 导出》

### 行为准则

反馈须精准。"这代码是坨屎"仅在同时说明"哪段代码、为何如此、怎样才不至于"
时方予受理。维护者(**shiaho**)已就针对其本人、其判断力及其人生选择的批评
出具长期豁免一份并存档。未附可用补丁的赞美,予以记录,余不受理。
