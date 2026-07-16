# AGENTS.md

## What this file is for

`AGENTS.md` is the working brief for **coding agents** (and humans pairing with them) inside this repository.

It is not the product marketing page (`README.md`). It answers:

- What this monorepo contains and how modules relate
- Which constraints must not be broken (shell template, i18n, export cache, deps)
- How host preview and generated APKs differ, and how to keep them aligned
- Standard ways to make common changes without feature loss or silent no-ops
- Easy-to-miss pitfalls specific to LITE shell + feature packs

**When to read what**

| Task | Read first |
|------|------------|
| Any non-trivial change | This whole file once; then the matching recipe |
| UI / strings only | Code style, i18n, recipe §1 |
| Export flag / shell runtime | How pieces connect + recipes §2–§3 + easy-to-miss |
| APK size / move code out of template | LITE + packs + recipe §4 |
| Preview OK, export broken | Recipe §9 |

Update this file when architecture rules or standard workflows change so the next agent session stays aligned.

---

## Code style

- Do not write code comments
- Prefer minimal, surgical diffs that match existing patterns
- Do not add copyright or license headers unless asked
- Prefer fixing root causes over surface patches
- Do not refactor unrelated code while fixing a bug or adding a feature
- Prefer reusing existing helpers, Access objects, and Planner patterns over inventing parallel systems

## Project layout

| Path | Role |
|------|------|
| `app/` | Full builder host: editor UI, export pipeline, runtimes. **Host preview/run uses this full DEX.** |
| `shell/` | LITE runtime template only → built to `app/src/main/assets/template/webview_shell.apk` |
| `feature-api/` | Shared SPI: `FeatureLoader`, `FeatureIds`, `FeatureModule`, soft-load helpers (`ScriptPackAccess`, …) |
| `feature-compat/` | Heavy runtime sources excluded from LITE; packaged into `app/src/main/assets/features/` |
| `modules/` | Module Market catalog (`registry.json` + per-module folders) |
| `scripts/` | Build helpers (`slim_shell_strings.py`, `package_fine_feature_packs.py`, …) |

Runtime Kotlin is **authored under `app/`** and synced into `shell` / `feature-compat` by Gradle. Edit once under `app/`; do not permanently fork copies under shell or feature modules.

**Key runtime / export files (orientation)**

| Area | Typical paths |
|------|----------------|
| Editor models | `app/.../data/model/WebApp.kt`, related config types |
| Export config | `app/.../apkbuilder/ApkConfig.kt`, `ApkConfigJsonFactory.kt`, `ApkBuilder.kt` |
| Capability → packs | `CapabilityPlanner.kt`, `FeaturePackMerger.kt`, `CapabilityPlannerTest` |
| Shell config | `app/.../shell/ShellModeManager.kt` (synced into LITE) |
| Soft-load | `*Access.kt`, `feature-api/.../ScriptPackAccess.kt`, `FeatureLoader.kt` |
| Shell membership | `shell/build.gradle.kts` → `syncShellRuntimeSources` |
| Compat / packs | `feature-compat/build.gradle.kts`, `scripts/package_fine_feature_packs.py` |
| Template artifact | `app/src/main/assets/template/webview_shell.apk` |
| Pack artifacts | `app/src/main/assets/features/<id>/` |

User-facing product docs: `README.md`, `.github/docs/README_CN.md`, `.github/CONTRIBUTING.md`, `modules/README.md`.

## How the main pieces connect

```text
Editor (Compose in app/)
  ↔ data models (WebApp, nested configs)
  ↔ export mapping (ApkConfig / ApkConfigJsonFactory)
  ↔ CapabilityPlanner  →  feature ids (+ reasons)
  ↔ FeaturePackMerger  →  pack files + enabled.json
  ↔ ApkBuilder / ApkBuildCache  →  signed generated APK

app/ sources (single authoring tree)
  → syncShellRuntimeSources (include − exclude)
       → shell DEX → webview_shell.apk  (LITE template)
  → syncFeatureCompatSources
       → feature-compat AAR
       → packageFeatureCompatPack / package_fine_feature_packs.py
       → app/src/main/assets/features/<id>/

Generated APK process
  WebToAppApplication
    → FeatureLoader.loadEnabled(assets/features/enabled.json)
    → PathClassLoader per pack
    → *Access / ScriptPackAccess / SPI resolve optional code
  ShellModeManager loads shell config JSON from assets
    → WebViewManager / shell UI / servers / optional features
```

**Mental model**

- **Host preview** = `:app` with nearly all classes on the main classpath. Easy to “see” heavy types even when they would not ship in a pure-WEB export.
- **Generated APK** = LITE template classes only, plus packs the Planner selected for that export.
- An editor switch only works after export if it flows through  
  **model → ApkConfig / JSON → shell config → runtime code**,  
  and heavy implementation is either still in LITE or reachable via **pack + soft-load**.

## Glossary (short)

| Term | Meaning |
|------|---------|
| **Host** | The WebToApp builder app (`:app`) developers run |
| **LITE / shell template** | Minimal runtime APK embedded as `webview_shell.apk` and used as the base of generated apps |
| **Generated APK** | User-facing app produced by export (`ApkBuilder`) |
| **Feature pack** | Prebuilt DEX (and optional assets/natives) under `features/<id>/`, merged at export |
| **feature-compat** | Fat pack / bucket used in default COMPAT planner phase for advanced capabilities |
| **Fine pack** | Smaller split pack (e.g. `shell-privacy`) built for GRANULAR phase |
| **Soft-load / Access** | LITE-side helper that loads a pack class by name; no hard type dependency on the heavy FQCN |
| **liteOnly** | Planner result with zero feature packs; smallest pure-WEB style export |
| **Parent-first classloader** | Pack classes cannot override a same-named class already in the app (LITE) loader |

## i18n

- Host UI strings live in `app/src/main/java/com/webtoapp/core/i18n/Strings.kt` (split across `Strings` / `StringsA` / `StringsB` / `StringsC`).
- Any new user-facing **host** string needs all 10 languages: Chinese, English, Arabic, Portuguese, Spanish, French, German, Russian, Japanese, Korean.
- Prefer adding properties on the existing split objects; match surrounding style.
- Shell LITE uses slim i18n (`scripts/slim_shell_strings.py` + packs under `app/src/main/assets/shell_i18n/`). Do not copy the full host `Strings.kt` into the shell template.
- Keep host and shell string systems separate. Generated-app copy that must ship in LITE goes through the slim path; editor-only copy stays in host `Strings*`.

## Android and packaging constraints

- Generated apps keep a **low `targetSdk`** on the shell path because they rely on on-device fork+exec runtimes. Do not raise shell targetSdk casually.
- Avoid new third-party dependencies unless strongly justified (`app/build.gradle.kts` / `shell/build.gradle.kts`). Prefer platform APIs and existing modules.
- Notification push channels: Web Notification polyfill, polling, WebSocket, FCM (developer-owned Firebase config). Do not add OEM vendor push SDKs by default.
- **One shell template only:** `webview_shell.apk` (LITE). Extra capabilities ship as feature packs. Do not reintroduce a second full/fat shell template APK.
- Export incremental rebuild lives in host-only `app/.../apkbuilder` (`ApkBuildCache` + `ApkBuilder`):
  - Template / entry identities must be **content-stable** (no mtime-based keys).
  - Encrypted builds always force a full rebuild.
  - Do not feed signed or renamed APKs back into full `modifyApk` as templates.
- Prefer arm64-lean export unless the product path explicitly needs multi-ABI.
- Size work targets **generated** WebView APKs (LITE ⊕ packs), not only the host builder APK size.

## Workflow

- Do not commit secrets, `local.properties`, keystores, or IDE/cache junk.
- Do not create commits or push unless the user asks.
- When changing export, shell membership, or packs: rebuild what you touched; confirm pure WEB with optional flags off still plans as `liteOnly`.
- When changing Planner or feature ids: update/extend `CapabilityPlannerTest`.
- When changing shared runtime under `app/`: remember shell will pick it up only if the sync include/exclude still allows it.

---

## LITE shell and feature packs

### Dual runtime (preview vs export)

| | Host `:app` | Generated APK |
|--|-------------|---------------|
| DEX | Almost all `app/src` | Shell sync include−exclude + merged packs |
| Optional features | Usually on main classpath | Only if kept in LITE **or** a pack was planned and loaded |
| `FeatureLoader.loadClass` | Often hits host DEX first via `Class.forName` | Needs pack DEX when the class was excluded from LITE |
| Config | Editor / in-memory models | Assets JSON via `ShellModeManager` |

Keep both paths valid:

1. Host still has the real implementation (or can load the pack).
2. LITE call sites use Access/SPI, **or** the Planner always injects the pack when the flag is on.
3. Nothing still synced into shell hard-types an FQCN that was excluded from LITE.

Most common failure: preview works; exported APK crashes or silently skips the feature because code left LITE without soft-load + Planner wiring.

### Architecture rules

| Rule | Detail |
|------|--------|
| One template | Only `app/src/main/assets/template/webview_shell.apk` from `:shell` release |
| Pure WEB default | WEB + system WebView + optional flags off → `liteOnly`, no packs |
| Pack when needed | Non-LITE capability → feature id → `features/<id>/` + `enabled.json` |
| Parent-first classloader | Pack `PathClassLoader` is a child of the app loader. **Same FQCN cannot be LITE stub + pack real** — parent wins. LITE holds a differently named Access type; real FQCN lives only in the pack |
| Author once | Sources under `app/`; shell and feature-compat only sync |
| Host-only stays host | `core/apkbuilder`, host screens, sample/market, editor-only helpers must not re-enter LITE |
| Capability parity | Smaller base template must not drop behavior when the matching export flag is on |

### Where each concern is edited

| Concern | Path |
|---------|------|
| LITE membership | `shell/build.gradle.kts` → `syncShellRuntimeSources` include/exclude |
| feature-compat sources | `feature-compat/build.gradle.kts` → `syncFeatureCompatSources` |
| Fine DEX packs | `scripts/package_fine_feature_packs.py` (after `packageFeatureCompatPack`) |
| Pack assets | `app/src/main/assets/features/<id>/` (`feature.json` + `classes.dex` …) |
| Feature id constants | `feature-api/.../FeatureIds.kt` |
| Config → packs | `app/.../apkbuilder/CapabilityPlanner.kt` + `CapabilityPlannerTest` |
| Export merge | `FeaturePackMerger` + `ApkBuilder` (`enabled.json`) |
| Runtime load | `FeatureLoader` in shell `WebToAppApplication` |
| Soft-load helpers | `*Access`, `ScriptPackAccess` |

### Soft-load pattern

Use when implementation leaves LITE but runtime may still need it when a flag is on.

1. Keep **config/model** types in LITE if shell JSON still deserializes them (`IsolationConfig`, `ErrorPageConfig`, …).
2. Exclude heavy impl from shell; include it in feature-compat sync (and fine-pack prefixes for GRANULAR).
3. Add a LITE **Access** object: only `FeatureLoader.loadClass` / reflection / `ScriptPackAccess.callStaticString`. No hard import of the heavy class from shell call sites.
4. Wire `CapabilityPlanner.need(FeatureIds.XXX, reason)` when export enables the feature. Default-off must not force packs on pure WEB.
5. Fail closed: missing pack → feature off, log, no crash.
6. Rebuild template + packs; cover Planner with a unit test.

**Reference Access types**

| Access | Pack / area |
|--------|-------------|
| `IsolationPrivacyAccess` | `shell-privacy` |
| `ForcedRunHardwareAccess` | `shell-forcedrun-hw` |
| `ScriptPackAccess` | `ext-chrome-scripts`, `shell-disguise-js`, `shell-translate-script`, `shell-error-games` |
| `GeckoEngineAccess` | feature-compat / engine-gecko |
| `FcmAccess` | feature-compat / push-fcm |
| `TlsMitmAccess` | feature-compat / net-tls-mitm |

SPI surface in `feature-api`: `FeatureModule`, `FeatureLoader`, `FeatureRegistry`, `FeatureIds`, plus providers such as `BrowserEngineProvider`, `ServerRuntimeProvider`, `PushChannelProvider`. Prefer extending these patterns before inventing a new load mechanism.

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

**AppType → base need**

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

Do not `need()` a granular id with no pack folder unless COMPAT still supplies the code via `feature-compat`.

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

### LITE membership (intent; Gradle sync is authoritative)

Usually **in** LITE: shell chrome UI, system WebView path, shell config load, logging, slim i18n, basic error pages (mini-games soft-loaded), config models deserialized at runtime, minimal util the shell still needs.

Usually **out** of LITE (pack or host): Gecko / FCM / TLS MITM heavies, large extension script blobs, isolation generators, forced-run hardware, translate/disguise JS providers, host-only editor tools (apkbuilder, samples, CodeSnippets, ModuleTemplates, OnlineMusic search APIs, …).

Still largely in LITE and tightly coupled (split only with full soft-load + Planner): server runtimes, AdBlocker, much of the extension host. Prefer a complete migration over a half-exclude.

Pure WEB without packs must still support URL load, JS, storage, basic chrome, static HTML/FRONTEND assets, basic splash/permissions, and basic error UI.

---

## Common change recipes

Follow the chain end-to-end. Stopping at UI or host-only code is the usual way preview and export diverge.

### 1. Add or change a host UI string

1. Add the property in the correct `Strings*` split with all 10 languages.
2. Reference it from Compose/UI the same way neighbors do.
3. Do not put host-only copy into shell slim i18n unless the generated app must show it.

### 2. Add an editor setting that must affect the generated APK

Update **all** of:

1. Model (`WebApp` / nested config) and editor UI binding  
2. Export mapping (`ApkBuilder` / `ApkConfig` / `ApkConfigJsonFactory`)  
3. Shell config types (`ShellModeManager` / shell config data classes) if runtime reads them  
4. Runtime use site in shell-synced code (or soft-load Access if heavy)  
5. If optional and heavy: `FeatureIds` + `CapabilityPlanner` + pack membership  
6. Planner unit tests when ids/flags change  

Missing a step usually yields: editor shows the switch, export ignores it, or export embeds config the runtime never reads.

### 3. Change shell runtime behavior used by every generated app

1. Edit the source under `app/` (shared runtime).  
2. Confirm membership in `syncShellRuntimeSources` (or intentional exclude + soft-load).  
3. Rebuild the shell template when validating packaging.  
4. Keep changes surgical; shell has a low targetSdk and a thin dependency set.

### 4. Move heavy code out of LITE (size work)

1. Confirm pure WEB default does not need it (or keep a tiny LITE fallback).  
2. `rg` all call sites; switch LITE path to Access/SPI.  
3. Exclude from `shell/build.gradle.kts`; include in feature-compat if still needed when enabled.  
4. Add/adjust fine pack prefixes when GRANULAR should ship a small pack.  
5. `FeatureIds` + Planner + test.  
6. Rebuild template + packs; verify pure WEB stays `liteOnly` and flag-on still works.  
7. Simulate: host preview / export flag ON / export flag OFF.

### 5. Add a host-only feature (editor, market, tooling)

1. Keep implementation under host-only packages (`core/apkbuilder`, host screens, sample/market, …).  
2. Exclude from shell sync if it might be pulled in accidentally.  
3. Do not pull host-only deps into `shell/build.gradle.kts`.

### 6. Touch APK export or incremental rebuild

1. Prefer `ApkBuildCache` / content hashes over timestamps.  
2. Encrypted builds stay full rebuild.  
3. Do not use signed outputs as templates.  
4. If template bytes change, ensure cache keys invalidate correctly.  
5. Keep Planner + pack merge consistent with what the runtime can load.

### 7. Change notifications / engines / network hardening

1. Prefer existing channel abstractions (polyfill / polling / WebSocket / FCM).  
2. Gecko, FCM, TLS MITM are pack/compat territory — Access + Planner, not hard LITE deps.  
3. Do not add OEM push SDKs by default.

### 8. Module Market / `modules/`

1. Follow `modules/README.md` catalog layout (`registry.json` + module folders).  
2. Runtime consumption still goes through extension/shell paths above if it ships inside generated APKs.

### 9. Fix “works in preview, broken after export”

Check in order:

1. Is the class still in the LITE template, or only on the host DEX?  
2. If excluded: Access/soft-load present, and `enabled.json` includes the pack when the flag is on?  
3. Did Planner see the same flag the editor set (`ApkConfig` mapping)?  
4. Did shell config JSON actually contain the field at runtime?  
5. Parent-first: is a same-FQCN stub in LITE shadowing the pack?  
6. Rebuild template/packs after sync exclude changes (stale template is a frequent miss).

### 10. Add or change a feature id / pack

1. Add constant in `FeatureIds` if needed.  
2. Teach `CapabilityPlanner` when to `need()` it; add GRANULAR deps if required.  
3. Ensure classes exist in feature-compat (COMPAT) and/or fine-pack prefixes (GRANULAR).  
4. Wire runtime via Access/SPI.  
5. Extend `CapabilityPlannerTest`.  
6. Rebuild packs; smoke flag ON/OFF.

---

## Easy-to-miss points

- **Shared sources are authored in `app/`.** Editing only under `shell/src` is usually wrong; sync overwrites or diverges from host.
- **Exclude lists are not enough.** Every former hard reference from remaining LITE code must be removed or soft-loaded.
- **COMPAT vs GRANULAR.** Default export often collapses to `feature-compat`. Fine packs alone do not help COMPAT if classes never entered feature-compat.
- **Default-off features must stay cheap.** Do not make pure WEB pull packs “just in case.”
- **Config field names drift.** Editor model, `ApkConfig`, JSON factory, and shell config must stay aligned; Gson silently drops unknown/missing fields.
- **Shell i18n ≠ host i18n.** Full `Strings.kt` is host-scale; LITE cannot afford it.
- **Low targetSdk and fork/exec runtimes** constrain “modernize the shell SDK” changes.
- **Incremental export cache** keys must be content-based; mtime and resigned APKs create false hits/misses.
- **Soft-load returns null** is valid when the flag is off — do not crash. Flag-on + null means Planner/pack wiring is incomplete.
- **Measuring the wrong APK.** Host builder size is not the generated WebView app size.
- **Half-migrating servers/adblock/extensions** without Access + Planner is worse than leaving them in LITE temporarily.
- **Stale template.** After membership or pack changes, rebuild `webview_shell.apk` / packs before judging size or behavior.
- **Host classpath hides missing packs.** `FeatureLoader` may resolve classes from the host DEX during preview even when a generated APK would not contain them.

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

Use these when shell membership, packs, Planner, or export packaging change. Host-only UI/string work usually needs only targeted `:app` compile/tests.

| Check | Expect |
|-------|--------|
| Pure WEB, flags off | Planner `liteOnly`, empty feature list |
| Flag on | Planner lists the id; pack/compat present; soft-load succeeds after load |
| Flag off | No crash; soft-load empty/skip |
| Shell compile | No hard refs to excluded FQCNs |
| Template | Size and `classes.dex` tracked when doing size work |

---

## Implementation snapshot

**Landed:** single LITE template; `FeatureLoader` + `enabled.json`; feature-compat + fine packs; soft-load Access patterns above; CapabilityPlanner COMPAT/GRANULAR + tests; slim shell i18n; arm64-lean export defaults.

**Still open:** server runtimes out of LITE via SPI; AdBlocker pack-if-enabled; deeper extension-host split; manifest-delta merge for pack components; CI size gates; real `FeatureModule` installers on more fine packs.
