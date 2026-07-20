# AGENTS.md

Instructions for coding agents working in this repository.

## Code style

- Do not write code comments
- Prefer minimal, surgical diffs that match existing patterns
- Do not add copyright or license headers unless asked
- Prefer fixing root causes over surface patches
- Do not refactor unrelated code while fixing a bug or adding a feature

## Project layout

| Path | Role |
|------|------|
| `app/` | Full builder host: editor UI, export pipeline, runtimes. Host preview/run uses this full DEX. |
| `shell/` | LITE runtime template only. Built to `app/src/main/assets/template/webview_shell.apk`. |
| `feature-api/` | Shared SPI: `FeatureLoader`, `FeatureIds`, `FeatureModule`, soft-load helpers (`ScriptPackAccess`, …). |
| `feature-compat/` | Heavy runtime sources excluded from LITE; packaged into `app/src/main/assets/features/`. |
| `clone-host/` | Host-side APK clone / identity reshape support library (not the LITE shell). |
| `modules/` | Module Market catalog (`registry.json` + per-module folders). |
| `scripts/` | Build helpers and gates (`slim_shell_strings.py`, `package_fine_feature_packs.py`, `check_capability_chain.py`, `check_feature_pack_parent_types.py`, `strip_shell_apk_bloat.py`). |

Runtime Kotlin is authored under `app/` and synced into `shell` / `feature-compat` by Gradle. Edit the `app/` source once; do not permanently fork copies under shell or feature modules.

User-facing product docs: `README.md`, `.github/docs/README_CN.md`, `.github/CONTRIBUTING.md`, `modules/README.md`.

## How the main pieces connect

```text
Editor (Compose screens in app/)
  ↔ data models (WebApp, configs)
  ↔ export factory (ApkConfig / ApkConfigJsonFactory)
  ↔ CapabilityPlanner  →  feature ids
  ↔ FeaturePackMerger  →  packs into output APK (hard-fail if planned pack missing)
  ↔ ApkBuilder / ApkBuildCache  →  signed generated APK

app/ sources
  → syncShellRuntimeSources  →  shell DEX  →  webview_shell.apk (LITE template)
  → syncFeatureCompatSources →  feature-compat AAR
       → packageFeatureCompatPack / package_fine_feature_packs.py
       → app/src/main/assets/features/<id>/

Generated APK runtime
  WebToAppApplication → FeatureLoader.loadEnabled(enabled.json)
  → PathClassLoader for each pack
  → *Access / ScriptPackAccess / SPI resolve optional code
```

Mental model:

- **Host preview** runs `:app` with nearly all classes on the main classpath.
- **Generated APK** runs LITE template classes only, plus packs the Planner selected.
- A flag in the editor is useless at export unless it flows through **model → ApkConfig JSON → shell config → runtime code**, and any heavy code is either in LITE or loaded via pack + soft-load.
- **Host-only prefs** (for example separate recents tasks via `HostRuntimePrefs`) must not be mistaken for export/shell config.

## i18n

- Host UI strings live in `app/src/main/java/com/webtoapp/core/i18n/Strings.kt` (split across `Strings` / `StringsA` / `StringsB` / `StringsC`).
- Any new user-facing host string needs all 10 languages: Chinese, English, Arabic, Portuguese, Spanish, French, German, Russian, Japanese, Korean.
- Prefer adding properties on the existing split objects; match surrounding style.
- Shell LITE uses slim i18n (`scripts/slim_shell_strings.py` + packs under `app/src/main/assets/shell_i18n/`). Do not copy the full host `Strings.kt` into the shell template.
- Shell-only user text may use the slim table / language packs; keep host and shell string systems separate.
- After adding shell-visible strings, regenerate slim packs with `scripts/slim_shell_strings.py` and keep `app/src/main/assets/shell_i18n/` in sync.

## Android and packaging constraints

- Generated apps keep a low `targetSdk` on the shell path because they rely on on-device fork+exec runtimes. Do not raise shell targetSdk casually.
- Avoid new third-party dependencies unless strongly justified (`app/build.gradle.kts` / `shell/build.gradle.kts`). Prefer platform APIs and existing modules.
- Notification push channels: Web Notification polyfill, polling, WebSocket, FCM (developer-owned Firebase config). Do not add OEM vendor push SDKs by default.
- Foreground services and notification helpers in shell must use `SafeNotificationChannels` (or equivalent fail-soft create). Channel creation failures must not crash FGS startup.
- **One shell template only:** `webview_shell.apk` (LITE). Extra capabilities ship as feature packs. Do not reintroduce a second full/fat shell template APK.
- **Shell R8 must not break pack soft-load.** LITE currently ships with `-dontobfuscate` and broad keeps for parent types (`kotlin.*`, Gson, OkHttp, shared `com.webtoapp.*` models, etc.). Feature packs resolve parent FQCNs via the shell ClassLoader; re-enabling obfuscation without a complete parent-type strategy will brick exported apps. See `shell/proguard-rules.pro` and `scripts/check_feature_pack_parent_types.py`.
- Export incremental rebuild lives in host-only `app/.../apkbuilder` (`ApkBuildCache` + `ApkBuilder`):
  - Modes: `FULL` / `CONTENT_OVERLAY` / `REUSE_UNSIGNED`.
  - Template / entry identities must be **content-stable** (no mtime-based keys).
  - Encrypted builds always force a full rebuild.
  - Do not feed signed or renamed APKs back into full `modifyApk` as templates.
- Default export ABI lean: prefer arm64 unless the product path explicitly needs multi-ABI.
- Port coordination: `PortManager` + `PortConflictMode` (`REASSIGN` / `AUTO_KILL` / `ALERT`) with real stop handlers. Local server runtimes must allocate through PortManager and clean up on stop.
- Local server / Linux env DNS: fork+exec runtimes (Node / PHP / Python / Go / WordPress / Linux env) should wire through `LocalDnsBridgeProxy` when they need host DNS/proxy bridging.
- Large runtime downloads use `NetworkModule.downloadClient` (extended timeouts), not the default short-lived client.
- HTML / FRONTEND packaged shells need file-scheme access via `ShellWebViewConfig` (`allowFileAccess` / local-file detection). Do not regress pure file-based HTML loads.

## Workflow

- Do not commit secrets, `local.properties`, keystores, or IDE/cache junk.
- Do not create commits, push, open PRs, or file Issues unless the user asks to deliver / ship / push / open a PR (or equivalent).
- When changing export or shell packaging, rebuild the template/packs you touched and sanity-check pure WEB still plans as lite-only when optional flags are off.
- When changing Planner or feature ids, update/extend `CapabilityPlannerTest` (including the special-settings matrix).
- When changing soft-load parents, pack class membership, or shell R8 keeps, run `checkFeaturePackParentTypes` and `checkCapabilityChain`.

### Delivery (Issue + PR + CI)

Default target: [shiaho777/web-to-app](https://github.com/shiaho777/web-to-app). Prefer a pull request over direct pushes to `main` when delivering code. Human-facing wording of the same loop lives in [CONTRIBUTING.md](.github/CONTRIBUTING.md) (EN/中文 · Pull requests); keep those docs in sync when this process changes.

**Language (required):** GitHub **Issues and PRs must be written in English** — titles, bodies, labels text you author, and delivery comments on the Issue/PR. Local chat with the user may be Chinese or any language; do not copy that language into Issue/PR text. Commit messages for delivery branches should also be English unless the user explicitly asks otherwise.

GitHub already runs CI on PRs (see `.github/workflows/`, especially `android-ci.yml` and path-filtered jobs like `modules-check.yml`). Treat CI as part of the delivery gate, not an afterthought: **do not treat a change as done, and do not close the Issue, until the PR is green and merged.**

When the user asks to deliver a change, run this loop end-to-end:

1. **Issue first.**  
   - If work already tracks a GitHub Issue, use that Issue.  
   - If not, open a new Issue on `shiaho777/web-to-app` that states the problem / goal, scope, and acceptance notes. Keep the title short and actionable.  
   - Write the Issue **in English** (title + body).

2. **Implement on a branch.**  
   - Branch from current `main` (prefix `codex/` unless the user specifies otherwise).  
   - Commit only the intended files; no secrets or local junk.

3. **Open a PR against `main`.**  
   - PR title summarizes the change (**English**).  
   - PR body must be **English** and:  
     - Explain what changed and why.  
     - Name the Issue being solved.  
     - Link the Issue (full URL and/or `#N`).  
     - Include a GitHub closing keyword so **merge** closes the Issue, e.g. `Fixes #123` or `Closes #123` (one primary Issue per PR when possible).  
   - Do **not** close the Issue when the PR is only opened. Closing belongs to **successful merge after CI**, via `Fixes` / `Closes` (or a maintainer after merge if auto-close missed).

4. **Notify the Issue.**  
   - Comment on the Issue **in English** with the PR URL and a one-line summary (e.g. “Implemented in https://github.com/shiaho777/web-to-app/pull/456 — waiting on CI.”).  
   - **If you cannot merge or close Issues:** still open the PR, comment on the Issue with both links, and ask a maintainer to merge when CI is green (Issue will close via `Fixes #N` on merge).

5. **CI gate (required).**  
   - After opening or updating the PR, wait for required checks. The merge gate is job id **`check`** from workflow `Android CI Build` (`.github/workflows/android-ci.yml`). Path-filtered jobs (e.g. `modules-check.yml`) also apply when those paths change.  
   - CI does **not** close Issues. Issue close is merge-time only (`Fixes #N` / `Closes #N` in the PR body, or a maintainer after merge).  
   - If CI fails: fix on the same branch, push, and re-run until green. Do not merge red CI. Do not close the Issue while CI is red or the PR is still open.  
   - If CI is flaky or blocked by infra, report the failing job URL and logs to the user; do not silently skip the gate unless the user explicitly overrides.  
   - Prefer local preflight for the areas you touched (unit tests, planner/pack checks) before relying only on remote CI.

6. **Merge only when green.**  
   - **Maintainer / sufficient permission:** merge the PR only after required CI is green (and any requested review is done). Let `Fixes #N` auto-close the Issue on merge; if it did not, close the Issue manually with a comment pointing at the merged PR.  
   - **No merge permission:** leave the PR open, keep Issue open, and hand off: “CI green / CI red + next step.” Do not ask to close the Issue before merge.

7. **Hand off.**  
   - Report branch name, Issue URL, PR URL, CI status (green / red + job link), and merge state to the user.

**Why CI owns the close timing**

```text
Issue open  →  PR open (Fixes #N)  →  CI runs  →  CI green  →  merge  →  Issue auto-closes
                     │
                     └→ CI red → fix & push → CI again (Issue stays open)
```

This avoids closing Issues for unmerged or broken work, and matches branch-protection / required-check setups on `main`.

Exceptions (only when the user explicitly overrides): tiny doc-only edits they ask to push straight to `main`, emergency hotfixes they request as direct push, or “skip Issue/PR/CI” instructions for that turn.

---

## LITE shell and feature packs

### Dual runtime (preview vs export)

| | Host `:app` | Generated APK |
|--|-------------|---------------|
| DEX | Almost all `app/src` | Shell sync include−exclude + merged packs |
| Optional features | Usually on main classpath | Only if kept in LITE or a pack was planned and loaded |
| `FeatureLoader.loadClass` | Often hits host DEX first via `Class.forName` | Needs pack DEX when excluded from LITE |
| Config | Editor / in-memory models | Assets JSON via `ShellModeManager` |

**Preview ≠ export** unless both paths stay valid:

1. Host still has the real implementation (or can load the pack).
2. LITE call sites use Access/SPI, or the Planner always injects the pack when the flag is on.
3. Nothing still synced into shell hard-types an FQCN that was excluded from LITE.
4. Shell parent ClassLoader still exposes every type the pack DEX links against (R8/keep issue).

Most common failure: preview works; exported APK crashes or silently skips the feature because code left LITE without soft-load + Planner wiring, or shell R8 renamed/shrunk a parent type the pack needs.

### Architecture rules

| Rule | Detail |
|------|--------|
| One template | `app/src/main/assets/template/webview_shell.apk` from `:shell` release only |
| Size target | Shrink **generated** WebView APKs (LITE ⊕ packs), not only the host builder app |
| Pure WEB default | WEB + system WebView + optional flags off → `liteOnly`, no packs |
| Pack when needed | Non-LITE capability → feature id → `features/<id>/` + `enabled.json` |
| Parent-first classloader | Pack loader is a child of the app loader. The same FQCN cannot be an empty LITE stub and a real pack class — the parent wins. LITE holds a differently named Access helper; the real FQCN lives only in the pack |
| Parent types stay resolvable | Pack code links against shell/kotlin/gson/okhttp/shared models by original FQCN. Shell R8 keeps / `-dontobfuscate` protect this. Do not strip pack-parent types from LITE |
| Author once | Sources under `app/`; shell and feature-compat only sync |
| Host-only stays host | `core/apkbuilder`, host screens, sample/market, editor-only helpers, `HostRuntimePrefs` must not re-enter LITE |
| Missing pack hard-fail | If Planner plans an id, `FeaturePackMerger` must find `assets/features/<id>/` (or staged cache). Export throws; do not silently ship a broken capability |
| Pack zip paths | Staged entries must live under `assets/features/<id>/…` only |

### Where each concern is edited

| Concern | Path |
|---------|------|
| What enters LITE | `shell/build.gradle.kts` → `syncShellRuntimeSources` include/exclude |
| What enters feature-compat | `feature-compat/build.gradle.kts` → `syncFeatureCompatSources` |
| Fine DEX packs | `scripts/package_fine_feature_packs.py` (after `packageFeatureCompatPack`) |
| Pack assets on disk | `app/src/main/assets/features/<id>/` (`feature.json` + `classes.dex` …) |
| Feature id constants | `feature-api/.../FeatureIds.kt` |
| Config → packs | `app/.../apkbuilder/CapabilityPlanner.kt` + `CapabilityPlannerTest` |
| Export merge | `FeaturePackMerger` + `ApkBuilder` writes `assets/features/enabled.json` |
| Runtime load | `FeatureLoader.loadEnabled` in shell `WebToAppApplication` |
| Soft-load helpers | `*Access` objects, `ScriptPackAccess` |
| Parent-type gate | `scripts/check_feature_pack_parent_types.py` → `:app:checkFeaturePackParentTypes` |
| Capability chain gate | `scripts/check_capability_chain.py` → `:app:checkCapabilityChain` |
| Shell minify policy | `shell/proguard-rules.pro` (`-dontobfuscate` + pack-parent keeps) |

### Soft-load pattern

Use this when implementation leaves LITE but runtime may still need it when a flag is on.

1. Keep config/model types in LITE if shell JSON still deserializes them (`IsolationConfig`, `ErrorPageConfig`, …).
2. Exclude heavy impl from shell; include it in feature-compat sync (and fine-pack prefixes when GRANULAR).
3. Add a LITE Access object that only uses `FeatureLoader.loadClass` / reflection / `ScriptPackAccess.callStaticString`. No hard import of the heavy class from shell call sites.
4. Wire `CapabilityPlanner.need(FeatureIds.XXX, reason)` when export enables the feature. Default-off features must not force packs on pure WEB.
5. Fail closed at runtime: missing pack → feature off, log, no crash. Fail closed at export: planned pack missing from assets → hard error.
6. Ensure shell keeps still expose every type the pack links against; run parent-type + capability chain checks.
7. Rebuild template + packs; cover Planner with a unit test (matrix cases for special settings).

Reference Access types:

| Access | Pack / area |
|--------|-------------|
| `IsolationPrivacyAccess` | `shell-privacy` |
| `ForcedRunHardwareAccess` | `shell-forcedrun-hw` |
| `ScriptPackAccess` | `ext-chrome-scripts`, `shell-disguise-js`, `shell-translate-script`, `shell-error-games` |
| `GeckoEngineAccess` | feature-compat / engine-gecko |
| `FcmAccess` | feature-compat / push-fcm |
| `TlsMitmAccess` | feature-compat / net-tls-mitm |

SPI surface (`feature-api`): `FeatureModule`, `FeatureLoader`, `FeatureRegistry`, `FeatureIds`, plus providers such as `BrowserEngineProvider`, `ServerRuntimeProvider`, `PushChannelProvider` (use existing patterns before inventing new loaders).

### CapabilityPlanner

Code: `CapabilityPlanner.kt`. Tests: `CapabilityPlannerTest` (includes special-settings matrix).

```text
Input:  ApkConfig (+ abi filters, PlannerPhase)
Output: CapabilityPlan(features, reasons, abiFilters, liteOnly)
liteOnly == true  ⇔  features.isEmpty()
```

After planning, GRANULAR runs `closeDependencies` then `materializePackIds`:

- Virtual parents (`ext-modules`, `shell-disguise`, `shell-translate`, `shell-forcedrun`) are dropped once children exist.
- IDs without a fine pack fall back to `feature-compat`.
- Export hard-fails if any planned pack is missing from `assets/features/`.

| Phase | Behavior |
|-------|----------|
| **COMPAT** (default export) | Many granular ids collapse to `FeatureIds.COMPAT` (`feature-compat`). Capability must still work. |
| **GRANULAR** | Real fine packs + `closeDependencies` + `materializePackIds` |

**AppType base needs**

| AppType | Granular id | COMPAT |
|---------|-------------|--------|
| WEB / HTML / FRONTEND | none if flags clean | none |
| IMAGE / VIDEO / GALLERY | `shell-media` | compat |
| MULTI_WEB | `shell-multiweb` | compat |
| NODEJS_APP | `server-nodejs` | compat |
| PHP_APP | `server-php` | compat |
| PYTHON_APP | `server-python` | compat |
| GO_APP | `server-go` | compat |
| WORDPRESS | `server-wordpress` (+ php when split) | compat |

**Common flags → feature id**

| Condition | Feature id |
|-----------|------------|
| Gecko engine | `engine-gecko` |
| TLS fingerprint | `net-tls-mitm` |
| Proxy enabled | `net-proxy` |
| DoH / advanced DNS | `net-doh` |
| FCM notification | `push-fcm` |
| Other notification channels | `notify-channels` |
| Extensions / modules / userscripts | `ext-modules` |
| Activation / announcement | `shell-activation` / `shell-announcement` |
| Ad block / ads | `ads-adblock` |
| Disguise | `shell-disguise` (+ `shell-disguise-js` in GRANULAR) |
| Isolation / privacy | `shell-privacy` |
| Error page mini-game | `shell-error-games` |
| Forced run | `shell-forcedrun` (+ `shell-forcedrun-hw`) |
| Floating window / BGM / translate / autostart / background / device actions | matching `shell-*` ids |

**GRANULAR deps (implemented):**  
`server-wordpress` → `server-php`; `shell-forcedrun` → `shell-forcedrun-hw`; `ext-modules` → `ext-builtins` + `ext-panel` + `ext-chrome-scripts`; `shell-disguise` → `shell-disguise-js`; `shell-translate` → `shell-translate-script`.

Do not `need()` a granular id that has no pack folder unless COMPAT still supplies the code via `feature-compat` (or the code is still in LITE).

### Packaging

**LITE template**

- Build: `:shell:assembleRelease` + `:app:syncShellTemplateApk`
- Output: `app/src/main/assets/template/webview_shell.apk`
- Template cache identity must stay content-stable
- Shell release minify keeps parent types for packs; do not casually drop `-dontobfuscate` or pack-parent keeps

**feature-compat**

- Syncs heavy sources excluded from LITE
- `packageFeatureCompatPack` → `app/src/main/assets/features/feature-compat/`
- After excluding something from shell that is still needed when a flag is on, **include it in feature-compat sync** so COMPAT exports keep working

**Fine packs**

- `scripts/package_fine_feature_packs.py` filters classes from the feature-compat AAR → d8 → `features/<id>/`
- Current packs include: `feature-compat`, `ext-builtins`, `ext-panel`, `ext-chrome-scripts`, `shell-forcedrun-hw`, `shell-disguise-js`, `shell-translate-script`, `shell-privacy`, `shell-error-games`

**Export merge**

- `FeaturePackMerger` stages pack files under `assets/features/<id>/`, builds `enabled.json`, reports `missingFeatures`
- `ApkBuilder` hard-fails when `missingFeatures` is non-empty
- Tests: `FeaturePackMergerTest`, `FeaturePackPathGuardTest`

### LITE membership (intent; code sync is authoritative)

Usually stay in LITE: shell UI chrome, system WebView path, shell config load, logging, slim i18n, basic error pages (games soft-loaded), config models still deserialized at runtime, minimal util needed by shell, PortManager, `LocalDnsBridgeProxy`, SafeNotificationChannels, activation/announcement dialogs currently synced for shell UI.

Usually out of LITE (pack or host): Gecko/FCM/TLS MITM heavies, large extension script blobs, isolation generators, forced-run hardware, translate/disguise JS providers, host-only editor tools (apkbuilder, samples, CodeSnippets, ModuleTemplates, OnlineMusic search APIs, HostRuntimePrefs, …).

Still largely in LITE and high coupling (split only with full soft-load + Planner): server runtimes, AdBlocker, much of the extension host. Do not half-exclude these.

Pure WEB without packs must still do URL load, JS, storage, basic chrome, static HTML/FRONTEND assets (with needed file access), basic splash/permissions, basic error UI.

---

## Common change recipes

These are the default approaches for everyday work. Follow the chain end-to-end; stopping at UI or host-only code is how preview and export diverge.

### 1. Add or change a host UI string

1. Add the property in the correct `Strings*` split with all 10 languages.
2. Reference it from Compose/UI the same way neighbors do.
3. Do not put host-only copy into shell slim i18n unless the generated app must show it.

### 2. Add an editor setting that must affect the generated APK

Trace and update **all** of:

1. Model (`WebApp` / nested config) and editor UI binding  
2. Export mapping (`ApkBuilder` / `ApkConfig` / `ApkConfigJsonFactory`)  
3. Shell config types (`ShellModeManager` / shell config data classes) if runtime reads them  
4. Runtime use site in shell-synced code (or soft-load Access if heavy)  
5. If the feature is optional and heavy: `FeatureIds` + `CapabilityPlanner` + pack membership  
6. Unit tests for Planner / export wiring when ids/flags change (`CapabilityPlannerTest`, focused `*ExportWiringTest` when needed)

Missing any step usually yields: editor shows the switch, export ignores it, or export embeds config the runtime never reads.

### 3. Change shell runtime behavior used by every generated app

1. Edit the source under `app/` (shared runtime).  
2. Confirm the file is included by `syncShellRuntimeSources` (or intentionally excluded + soft-loaded).  
3. Rebuild shell template if you need to validate packaging.  
4. Keep changes surgical; shell has a low targetSdk and a thin dependency set.
5. If you touch FGS / notification channel creation, fail soft via `SafeNotificationChannels`.

### 4. Move heavy code out of LITE (size work)

1. Confirm pure WEB default does not need it (or keep a tiny LITE fallback).  
2. `rg` all call sites; switch LITE path to Access/SPI.  
3. Exclude from `shell/build.gradle.kts`; include in feature-compat if still needed when enabled.  
4. Add/adjust fine pack prefixes when GRANULAR should ship a small pack.  
5. `FeatureIds` + Planner + test.  
6. Rebuild template + packs; verify pure WEB stays `liteOnly` and flag-on still works.  
7. Run `checkCapabilityChain` and `checkFeaturePackParentTypes`.  
8. Mentally simulate: host preview / export flag ON / export flag OFF.

### 5. Add a host-only feature (editor, market, tooling)

1. Keep implementation under host-only packages (`core/apkbuilder`, host screens, sample/market, `core/host`, …).  
2. Exclude from shell sync if anything might accidentally be included.  
3. Do not pull host-only deps into `shell/build.gradle.kts`.  
4. Example: optional separate recents tasks for WebApp preview (`HostRuntimePrefs` + About toggle + document activities). This must not flow into generated APK config.
5. Host-only multi-entry features still need **every host launch path** wired (see “Primary-path migration half-wire” below). Separate tasks are not export config, but they still fail if only one preview entry is complete.

### 6. Touch APK export or incremental rebuild

1. Prefer `ApkBuildCache` / content hashes over timestamps.  
2. Encrypted builds stay full rebuild.  
3. Do not use signed outputs as templates.  
4. If template bytes change, ensure cache keys invalidate correctly.  
5. Keep Planner + pack merge consistent with what the runtime can load.  
6. Planned packs missing from `assets/features/` must hard-fail, not warn-and-continue.

### 7. Change notifications / engines / network hardening

1. Prefer existing channel abstractions (polyfill / polling / WebSocket / FCM).  
2. Gecko, FCM, TLS MITM are pack/compat territory — go through Access + Planner, not hard LITE deps.  
3. Do not add OEM push SDKs by default.  
4. FGS channel create paths must tolerate OEM/channel failures.

### 8. Module Market / `modules/`

1. Follow `modules/README.md` catalog layout (`registry.json` + module folders).  
2. Runtime consumption still goes through the extension/shell paths above if it ships inside generated APKs.

### 9. Fix “works in preview, broken after export”

Checklist in order:

1. Is the class still in the LITE template, or only on the host DEX?  
2. If excluded, is there Access/soft-load and does `enabled.json` include the pack when the flag is on?  
3. Did Planner see the same flag the editor set (`ApkConfig` mapping)?  
4. Did shell config JSON actually contain the field at runtime?  
5. Parent-first: is a same-FQCN stub in LITE shadowing the pack?  
6. Did shell R8 rename/shrink a type the pack links against? Run parent-type check and inspect `shell/proguard-rules.pro`.  
7. Rebuild template/packs after sync exclude changes (stale template is a frequent miss).  
8. For adblock: confirm `adBlockEnabled` mapping, host filter rebuild from cached subscriptions, and export rule compile without wiping host state (`AdBlockExportWiringTest` / host runtime tests).

### 10. Local server runtime / download path

1. Allocate ports through `PortManager` with the configured conflict policy; implement real stop handlers.  
2. Wire fork+exec processes into `LocalDnsBridgeProxy` when they need host DNS/proxy env.  
3. Use `NetworkModule.downloadClient` for large dependency / engine / runtime downloads.

---

## Primary-path migration half-wire (host)

This is a **host** failure mode, distinct from “works in preview, broken after export.”

### What it looks like

- UI toggle / pref exists and appears to save.
- One code path behaves correctly.
- The **path users actually take after a migration** does not.
- Symptom: “this feature is dead / does nothing,” with no export crash.

### Concrete case: WebApp separate recents tasks

| Piece | Role |
|-------|------|
| About toggle + `HostRuntimePrefs` | Host-only pref (not ApkConfig) |
| `WebViewDocumentActivity` + `documentLaunchMode="always"` | Correct document-task Activity for the **WebView** entry |
| `ShellPreviewLauncher` → `ShellActivity` (`singleTask`) | **Current primary** host preview entry for most app types |
| Intent flags alone (`NEW_DOCUMENT` / `MULTIPLE_TASK`) on `singleTask` | **Not sufficient** to create stable multi-document recents tasks |
| Global single-slot preview session | Concurrent multi-app preview overwrites config |

What went wrong historically:

1. Separate tasks were implemented on the WebView launch path (`WebViewDocumentActivity`).
2. Host preview later standardized on **Shell** (`ShellPreviewLauncher` → `ShellActivity`).
3. Flags were copied onto Shell intents, but a Shell **document** Activity and multi-session preview state were not.
4. Users hit the Shell path → toggle looked real, behavior did not.

Landed repair pattern (do not regress):

- `ShellDocumentActivity` (host-only; excluded from shell sync) with `documentLaunchMode="always"`, parallel to `WebViewDocumentActivity`.
- `ShellPreviewLauncher` targets `ShellDocumentActivity` when the pref is on; otherwise `ShellActivity`.
- `ShellPreviewSession` keys concurrent preview configs by app id (not a single global slot).
- About toggle off finishes both WebView and Shell document tasks.

### Rule when migrating a host entry point

If a host capability depends on **how** an app is launched (Activity class, `launchMode`, `documentLaunchMode`, intent flags, task description, per-app session state):

1. List **all** current host launch entries (today at least: Shell preview, WebView fallback, shortcuts/export helpers, media/gallery preview if in scope).
2. Implement the full behavior on the **primary** entry first (whatever Home / open-app actually calls).
3. Port or consciously skip secondary entries; do not leave “flags only” stubs that look complete.
4. If multi-instance is implied (separate tasks, multi-window, concurrent previews), ban global single-slot session state for that capability.
5. Turning the pref **off** must clean up (finish document tasks, clear sessions) as carefully as turning it **on**.
6. Do not treat “old path works” as done after the primary path moved.

### Not the same as export dual-runtime

| Failure | Primary check |
|---------|----------------|
| Preview OK, export broken | LITE membership, Access/SPI, Planner, pack assets, shell config JSON, R8 parents |
| Host toggle OK on one entry, dead on primary entry | Launch Activity matrix, document vs singleTask, host session multi-instance |

Both are “half-wire.” Do not fix one class of bug with the other checklist alone.

---

## Easy-to-miss points

- **Shared sources are authored in `app/`.** Editing only a file under `shell/src` is usually wrong; it will be overwritten on sync or diverge from host.
- **Exclude lists are not enough.** Every former hard reference from remaining LITE code must be removed or soft-loaded, or shell compilation / runtime breaks.
- **COMPAT vs GRANULAR.** Default export collapses many ids into `feature-compat`. Fine packs alone do not help COMPAT if the classes never entered feature-compat.
- **materializePackIds fallback.** Unknown GRANULAR ids become `feature-compat`; virtual parents disappear after children are added. Tests must cover special-settings matrices, not only pure WEB.
- **Default-off features must stay cheap.** Do not make pure WEB pull packs “just in case.”
- **Config field names drift.** Editor model, `ApkConfig`, JSON factory, and shell config must stay aligned; Gson silently drops unknown/missing fields.
- **Shell i18n ≠ host i18n.** Full `Strings.kt` is host-scale; LITE cannot afford it. Regenerate slim packs after shell-visible string changes.
- **Low targetSdk and fork/exec runtimes** constrain “modernize the shell SDK” changes.
- **Incremental export cache** keys must be content-based; mtime and resigned APKs create false hits/misses.
- **Soft-load returns null** is a valid flag-off path; do not crash. But flag-on + null means Planner/pack wiring is incomplete.
- **Export missing pack is not soft.** Runtime fail-closed ≠ export warn-and-ship.
- **Shell R8 is part of the pack ABI.** Obfuscating or over-shrinking LITE breaks PathClassLoader soft-load even when DEX packs are correct.
- **Pack zip paths** must stay under `assets/features/…`; path regressions break `FeatureLoader` root resolution.
- **Host-only prefs ≠ export flags.** Separate tasks / About host toggles do not replace ApkConfig wiring.
- **Primary-path migration half-wire.** After host preview moved to Shell, capabilities implemented only on WebView (or only via intent flags on `singleTask`) look enabled but do nothing. Wire the **current** primary launcher, not the historical one.
- **Document tasks need a document Activity.** `FLAG_ACTIVITY_NEW_DOCUMENT` on `ShellActivity`/`singleTask` is not a substitute for `documentLaunchMode="always"` (see `ShellDocumentActivity` / `WebViewDocumentActivity`).
- **Host multi-preview needs multi-session state.** A single global `ShellPreviewSession` slot breaks concurrent separate-task previews; key by app id.
- **Measuring the wrong APK.** Host builder size is not the generated WebView app size.
- **Half-migrating servers/adblock/extensions** without Access + Planner is worse than leaving them in LITE temporarily.
- **Adblock is wired again for preview + export**, but still largely LITE-resident; do not claim it is pack-split until exclude + Access + Planner are complete.

---

## Forbidden / high-risk mistakes

- Second full shell template APK  
- Same FQCN stub in LITE + real implementation in a pack  
- Excluding a class but leaving imports/constructors in shell-synced sources  
- Soft-load without Planner (flag ON → silent no-op)  
- Planner without pack assets (export should hard-fail; do not weaken this)  
- Staging feature packs outside `assets/features/<id>/`  
- Re-enabling shell R8 obfuscation / aggressive shrink without a full pack-parent keep strategy and gate green  
- Putting host-only tools or host prefs back into LITE for convenience  
- Leaving host capabilities only on a legacy launch path after the primary entry moved (e.g. WebView document tasks while Home opens Shell)  
- Relying on `NEW_DOCUMENT` flags alone for separate recents tasks without a `documentLaunchMode="always"` Activity  
- Global single-slot host preview session state when multi-app / separate-task preview is supported  
- OEM push SDKs or unjustified heavy dependencies in LITE  
- Feeding signed/renamed APKs into template/modify paths  
- Crashing FGS when notification channel creation fails  
- Committing secrets, keystores, or local machine config  

---

## Verify commands

```bash
./gradlew :shell:assembleRelease :app:syncShellTemplateApk --no-configuration-cache
./gradlew :feature-compat:packageFeatureCompatPack --no-configuration-cache
./gradlew :feature-compat:packageFineFeaturePacks --no-configuration-cache
python3 -c "import zipfile,os; p='app/src/main/assets/template/webview_shell.apk'; print(os.path.getsize(p)); z=zipfile.ZipFile(p); print([(i.filename,i.compress_size) for i in sorted(z.infolist(),key=lambda x:-x.compress_size)[:8]])"
./gradlew :app:testDebugUnitTest --tests "com.webtoapp.core.apkbuilder.CapabilityPlannerTest" -x :shell:assembleRelease -x :app:syncShellTemplateApk --no-configuration-cache
./gradlew :app:testDebugUnitTest --tests "com.webtoapp.core.apkbuilder.FeaturePackMergerTest" --tests "com.webtoapp.core.apkbuilder.FeaturePackPathGuardTest" --tests "com.webtoapp.core.apkbuilder.AdBlockExportWiringTest" -x :shell:assembleRelease -x :app:syncShellTemplateApk --no-configuration-cache
./gradlew :app:checkFeaturePackParentTypes --no-configuration-cache
./gradlew :app:checkCapabilityChain --no-configuration-cache
python3 scripts/check_capability_chain.py
python3 scripts/check_feature_pack_parent_types.py
```

Use these when you change shell membership, packs, Planner, R8 keeps, or export packaging. For host-only UI/string work, targeted compile/tests on `:app` are usually enough.

Related focused tests often worth running after nearby edits: `ApkBuildCacheTest`, `ApkConfigWiringGuardTest`, `AdBlockerHostRuntimeTest`, `PortManagerTest`.

---

## Implementation snapshot

Landed:

- Single LITE template (`webview_shell.apk`) + FeatureLoader + `enabled.json`
- feature-compat + fine packs + soft-load Access patterns above
- CapabilityPlanner COMPAT/GRANULAR, `closeDependencies`, `materializePackIds` (virtual parents + feature-compat fallback)
- Export hard-fail on missing planned packs; pack entries constrained under `assets/features/`
- Capability chain + parent-type verification scripts/Gradle gates; expanded Planner matrix tests
- Shell `-dontobfuscate` + pack-parent keeps so soft-load survives minify
- Slim shell i18n packs; arm64-lean export defaults
- Incremental `ApkBuildCache` (`FULL` / `CONTENT_OVERLAY` / `REUSE_UNSIGNED`); encrypted builds always full
- Notification channels: polyfill, polling, WebSocket, FCM (BYO Firebase) via existing abstractions
- PortManager conflict policies + real stop handlers
- Adblock preview + export wiring restored (still LITE-resident)
- HTML/FRONTEND file-access for packaged local shells
- SafeNotificationChannels fail-soft path for shell FGS
- Host-only optional separate recents tasks for WebApp preview (`HostRuntimePrefs` + `ShellDocumentActivity` / `WebViewDocumentActivity` + multi-id `ShellPreviewSession`; not ApkConfig)
- LocalDnsBridgeProxy wiring for local server runtimes (including Node.js)
- Runtime downloads via `NetworkModule.downloadClient`

