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
| `modules/` | Module Market catalog (`registry.json` + per-module folders). |
| `scripts/` | Build helpers (`slim_shell_strings.py`, `package_fine_feature_packs.py`, …). |

Runtime Kotlin is authored under `app/` and synced into `shell` / `feature-compat` by Gradle. Edit the `app/` source once; do not permanently fork copies under shell or feature modules.

User-facing product docs: `README.md`, `.github/docs/README_CN.md`, `.github/CONTRIBUTING.md`, `modules/README.md`.

## How the main pieces connect

```text
Editor (Compose screens in app/)
  ↔ data models (WebApp, configs)
  ↔ export factory (ApkConfig / ApkConfigJsonFactory)
  ↔ CapabilityPlanner  →  feature ids
  ↔ FeaturePackMerger  →  packs into output APK
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

## i18n

- Host UI strings live in `app/src/main/java/com/webtoapp/core/i18n/Strings.kt` (split across `Strings` / `StringsA` / `StringsB` / `StringsC`).
- Any new user-facing host string needs all 10 languages: Chinese, English, Arabic, Portuguese, Spanish, French, German, Russian, Japanese, Korean.
- Prefer adding properties on the existing split objects; match surrounding style.
- Shell LITE uses slim i18n (`scripts/slim_shell_strings.py` + packs under `app/src/main/assets/shell_i18n/`). Do not copy the full host `Strings.kt` into the shell template.
- Shell-only user text may use the slim table / language packs; keep host and shell string systems separate.

## Android and packaging constraints

- Generated apps keep a low `targetSdk` on the shell path because they rely on on-device fork+exec runtimes. Do not raise shell targetSdk casually.
- Avoid new third-party dependencies unless strongly justified (`app/build.gradle.kts` / `shell/build.gradle.kts`). Prefer platform APIs and existing modules.
- Notification push channels: Web Notification polyfill, polling, WebSocket, FCM (developer-owned Firebase config). Do not add OEM vendor push SDKs by default.
- **One shell template only:** `webview_shell.apk` (LITE). Extra capabilities ship as feature packs. Do not reintroduce a second full/fat shell template APK.
- Export incremental rebuild lives in host-only `app/.../apkbuilder` (`ApkBuildCache` + `ApkBuilder`):
  - Template / entry identities must be **content-stable** (no mtime-based keys).
  - Encrypted builds always force a full rebuild.
  - Do not feed signed or renamed APKs back into full `modifyApk` as templates.
- Default export ABI lean: prefer arm64 unless the product path explicitly needs multi-ABI.

## Workflow

- Do not commit secrets, `local.properties`, keystores, or IDE/cache junk.
- Do not create commits or push unless the user asks.
- When changing export or shell packaging, rebuild the template/packs you touched and sanity-check pure WEB still plans as lite-only when optional flags are off.
- When changing Planner or feature ids, update/extend `CapabilityPlannerTest`.

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

Most common failure: preview works; exported APK crashes or silently skips the feature because code left LITE without soft-load + Planner wiring.

### Architecture rules

| Rule | Detail |
|------|--------|
| One template | `app/src/main/assets/template/webview_shell.apk` from `:shell` release only |
| Size target | Shrink **generated** WebView APKs (LITE ⊕ packs), not only the host builder app |
| Pure WEB default | WEB + system WebView + optional flags off → `liteOnly`, no packs |
| Pack when needed | Non-LITE capability → feature id → `features/<id>/` + `enabled.json` |
| Parent-first classloader | Pack loader is a child of the app loader. The same FQCN cannot be an empty LITE stub and a real pack class — the parent wins. LITE holds a differently named Access helper; the real FQCN lives only in the pack |
| Author once | Sources under `app/`; shell and feature-compat only sync |
| Host-only stays host | `core/apkbuilder`, host screens, sample/market, editor-only helpers must not re-enter LITE |

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

### Soft-load pattern

Use this when implementation leaves LITE but runtime may still need it when a flag is on.

1. Keep config/model types in LITE if shell JSON still deserializes them (`IsolationConfig`, `ErrorPageConfig`, …).
2. Exclude heavy impl from shell; include it in feature-compat sync (and fine-pack prefixes when GRANULAR).
3. Add a LITE Access object that only uses `FeatureLoader.loadClass` / reflection / `ScriptPackAccess.callStaticString`. No hard import of the heavy class from shell call sites.
4. Wire `CapabilityPlanner.need(FeatureIds.XXX, reason)` when export enables the feature. Default-off features must not force packs on pure WEB.
5. Fail closed: missing pack → feature off, log, no crash.
6. Rebuild template + packs; cover Planner with a unit test.

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

Code: `CapabilityPlanner.kt`. Tests: `CapabilityPlannerTest`.

```text
Input:  ApkConfig (+ abi filters, PlannerPhase)
Output: CapabilityPlan(features, reasons, abiFilters, liteOnly)
liteOnly == true  ⇔  features.isEmpty()
```

| Phase | Behavior |
|-------|----------|
| **COMPAT** (default export) | Many granular ids collapse to `FeatureIds.COMPAT` (`feature-compat`). Capability must still work. |
| **GRANULAR** | Real fine packs + `closeDependencies` |

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
| Floating window / BGM / translate / autostart / background | matching `shell-*` ids |

**GRANULAR deps (implemented):**  
`server-wordpress` → `server-php`; `shell-forcedrun` → `shell-forcedrun-hw`; `ext-modules` → `ext-builtins` + `ext-panel` + `ext-chrome-scripts`; `shell-disguise` → `shell-disguise-js`; `shell-translate` → `shell-translate-script`.

Do not `need()` a granular id that has no pack folder unless COMPAT still supplies the code via `feature-compat`.

### Packaging

**LITE template**

- Build: `:shell:assembleRelease` + `:app:syncShellTemplateApk`
- Output: `app/src/main/assets/template/webview_shell.apk`
- Template cache identity must stay content-stable

**feature-compat**

- Syncs heavy sources excluded from LITE
- `packageFeatureCompatPack` → `app/src/main/assets/features/feature-compat/`
- After excluding something from shell that is still needed when a flag is on, **include it in feature-compat sync** so COMPAT exports keep working

**Fine packs**

- `scripts/package_fine_feature_packs.py` filters classes from the feature-compat AAR → d8 → `features/<id>/`
- Current packs include: `feature-compat`, `ext-builtins`, `ext-panel`, `ext-chrome-scripts`, `shell-forcedrun-hw`, `shell-disguise-js`, `shell-translate-script`, `shell-privacy`, `shell-error-games`

**Export merge**

- `FeaturePackMerger` stages pack files and builds `enabled.json`
- `ApkBuilder` writes them into the generated APK

### LITE membership (intent; code sync is authoritative)

Usually stay in LITE: shell UI chrome, system WebView path, shell config load, logging, slim i18n, basic error pages (games soft-loaded), config models still deserialized at runtime, minimal util needed by shell.

Usually out of LITE (pack or host): Gecko/FCM/TLS MITM heavies, large extension script blobs, isolation generators, forced-run hardware, translate/disguise JS providers, host-only editor tools (apkbuilder, samples, CodeSnippets, ModuleTemplates, OnlineMusic search APIs, …).

Still largely in LITE and high coupling (split only with full soft-load + Planner): server runtimes, AdBlocker, much of the extension host. Do not half-exclude these.

Pure WEB without packs must still do URL load, JS, storage, basic chrome, static HTML/FRONTEND assets, basic splash/permissions, basic error UI.

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
6. Unit tests for Planner when ids/flags change  

Missing any step usually yields: editor shows the switch, export ignores it, or export embeds config the runtime never reads.

### 3. Change shell runtime behavior used by every generated app

1. Edit the source under `app/` (shared runtime).  
2. Confirm the file is included by `syncShellRuntimeSources` (or intentionally excluded + soft-loaded).  
3. Rebuild shell template if you need to validate packaging.  
4. Keep changes surgical; shell has a low targetSdk and a thin dependency set.

### 4. Move heavy code out of LITE (size work)

1. Confirm pure WEB default does not need it (or keep a tiny LITE fallback).  
2. `rg` all call sites; switch LITE path to Access/SPI.  
3. Exclude from `shell/build.gradle.kts`; include in feature-compat if still needed when enabled.  
4. Add/adjust fine pack prefixes when GRANULAR should ship a small pack.  
5. `FeatureIds` + Planner + test.  
6. Rebuild template + packs; verify pure WEB stays `liteOnly` and flag-on still works.  
7. Mentally simulate: host preview / export flag ON / export flag OFF.

### 5. Add a host-only feature (editor, market, tooling)

1. Keep implementation under host-only packages (`core/apkbuilder`, host screens, sample/market, …).  
2. Exclude from shell sync if anything might accidentally be included.  
3. Do not pull host-only deps into `shell/build.gradle.kts`.

### 6. Touch APK export or incremental rebuild

1. Prefer `ApkBuildCache` / content hashes over timestamps.  
2. Encrypted builds stay full rebuild.  
3. Do not use signed outputs as templates.  
4. If template bytes change, ensure cache keys invalidate correctly.  
5. Keep Planner + pack merge consistent with what the runtime can load.

### 7. Change notifications / engines / network hardening

1. Prefer existing channel abstractions (polyfill / polling / WebSocket / FCM).  
2. Gecko, FCM, TLS MITM are pack/compat territory — go through Access + Planner, not hard LITE deps.  
3. Do not add OEM push SDKs by default.

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
6. Rebuild template/packs after sync exclude changes (stale template is a frequent miss).

---

## Easy-to-miss points

- **Shared sources are authored in `app/`.** Editing only a file under `shell/src` is usually wrong; it will be overwritten on sync or diverge from host.
- **Exclude lists are not enough.** Every former hard reference from remaining LITE code must be removed or soft-loaded, or shell compilation / runtime breaks.
- **COMPAT vs GRANULAR.** Default export collapses many ids into `feature-compat`. Fine packs alone do not help COMPAT if the classes never entered feature-compat.
- **Default-off features must stay cheap.** Do not make pure WEB pull packs “just in case.”
- **Config field names drift.** Editor model, `ApkConfig`, JSON factory, and shell config must stay aligned; Gson silently drops unknown/missing fields.
- **Shell i18n ≠ host i18n.** Full `Strings.kt` is host-scale; LITE cannot afford it.
- **Low targetSdk and fork/exec runtimes** constrain “modernize the shell SDK” changes.
- **Incremental export cache** keys must be content-based; mtime and resigned APKs create false hits/misses.
- **Soft-load returns null** is a valid flag-off path; do not crash. But flag-on + null means Planner/pack wiring is incomplete.
- **Measuring the wrong APK.** Host builder size is not the generated WebView app size.
- **Half-migrating servers/adblock/extensions** without Access + Planner is worse than leaving them in LITE temporarily.

---

## Forbidden / high-risk mistakes

- Second full shell template APK  
- Same FQCN stub in LITE + real implementation in a pack  
- Excluding a class but leaving imports/constructors in shell-synced sources  
- Soft-load without Planner (flag ON → silent no-op)  
- Putting host-only tools back into LITE for convenience  
- OEM push SDKs or unjustified heavy dependencies in LITE  
- Feeding signed/renamed APKs into template/modify paths  
- Committing secrets, keystores, or local machine config  

---

## Verify commands

```bash
./gradlew :shell:assembleRelease :app:syncShellTemplateApk --no-configuration-cache
./gradlew :feature-compat:packageFeatureCompatPack --no-configuration-cache
python3 -c "import zipfile,os; p='app/src/main/assets/template/webview_shell.apk'; print(os.path.getsize(p)); z=zipfile.ZipFile(p); print([(i.filename,i.compress_size) for i in sorted(z.infolist(),key=lambda x:-x.compress_size)[:8]])"
./gradlew :app:testDebugUnitTest --tests "com.webtoapp.core.apkbuilder.CapabilityPlannerTest" -x :shell:assembleRelease -x :app:syncShellTemplateApk --no-configuration-cache
```

Use these when you change shell membership, packs, Planner, or export packaging. For host-only UI/string work, targeted compile/tests on `:app` are usually enough.

---

## Implementation snapshot

Landed: single LITE template, FeatureLoader + `enabled.json`, feature-compat + fine packs, soft-load Access patterns above, CapabilityPlanner COMPAT/GRANULAR + tests, slim shell i18n, arm64-lean export defaults.

Still open: server runtimes out of LITE via SPI, AdBlocker pack-if-enabled, deeper extension-host split, manifest-delta merge for pack components, CI size gates, real `FeatureModule` installers on more fine packs.
