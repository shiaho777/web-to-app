# AGENTS.md

Single source of truth for coding agents in this repository.

Do not split project-architecture / LITE / feature-pack / packaging guidance into extra tutorial MD files.
Update **this file only** when those rules change. Root `README.md` is user-facing product docs — leave it alone unless the user asks.

---

## 1. Code style

- Do not write code comments
- Prefer minimal, surgical diffs that match existing patterns
- Do not add copyright or license headers unless asked

## 2. Project layout

| Path | Role |
|------|------|
| `app/` | Full builder host (UI, APK export, editors). **Host preview uses this full DEX.** |
| `shell/` | LITE runtime template only → `app/src/main/assets/template/webview_shell.apk` |
| `feature-api/` | SPI, `FeatureLoader`, `FeatureIds`, soft-load helpers (`ScriptPackAccess`, …) |
| `feature-compat/` | Heavy runtime sources excluded from LITE; packs into `app/src/main/assets/features/` |
| `modules/` | Module Market catalog (`registry.json` + per-module folders) |
| `scripts/` | Build helpers (`slim_shell_strings.py`, `package_fine_feature_packs.py`, …) |

Runtime Kotlin is **authored under `app/`** and synced into `shell` / `feature-compat` by Gradle. Do not permanently fork copies.

Public user docs (do not treat as agent architecture source of truth): `README.md`, `.github/docs/README_CN.md`, `.github/CONTRIBUTING.md`, `modules/README.md`.

## 3. i18n

- Host UI: 10 languages in `app/.../i18n/Strings.kt` (`Strings` / `StringsA` / `StringsB` / `StringsC`)
- Any new user-facing host string must include all 10: Chinese, English, Arabic, Portuguese, Spanish, French, German, Russian, Japanese, Korean
- Shell LITE uses slim i18n (`scripts/slim_shell_strings.py` + packs under `app/src/main/assets/shell_i18n/`). Do not dump full host `Strings.kt` into LITE

## 4. Android / packaging constraints

- Generated apps keep a low `targetSdk` on the shell path (on-device fork+exec runtimes)
- Avoid new third-party deps unless strongly justified (`app/build.gradle.kts` / `shell/build.gradle.kts`)
- Notification channels: Web Notification polyfill, polling, WebSocket, FCM (developer-owned Firebase). No OEM vendor push SDKs by default
- **One shell template only:** `webview_shell.apk` (LITE). Non-LITE capabilities = feature packs. **Never reintroduce a second full template APK**
- Export incremental rebuild: host-only `app/.../apkbuilder` (`ApkBuildCache` + `ApkBuilder`). Template IDs content-stable (no mtime). Encrypted builds force full rebuild. Do not feed signed/renamed APKs into full `modifyApk` as templates

## 5. Workflow notes

- Do not commit secrets, local properties, or IDE/cache junk
- Do not create commits or push unless the user asks
- Prefer root-cause fixes over surface patches

---

## 6. LITE + feature packs — architecture (mandatory)

Any work on generated APK size, shell runtime, export packaging, engines, servers, extensions, isolation, error pages, or moving code out of the template **must** follow this section.

### 6.1 Big picture

```text
Host builder (:app)
  = full sources in one DEX
  → in-app preview always sees classes
  → FeatureLoader Class.forName often hits host classpath first

Generated user APK
  = LITE template (webview_shell.apk)
  ⊕ packs from CapabilityPlanner + FeaturePackMerger
  → only LITE classes exist unless a pack was planned and merged
  → soft-load via FeatureLoader / *Access / ScriptPackAccess
```

**Preview ≠ export** unless both paths stay valid:

1. Host still has the real class (or can load the pack).
2. LITE uses Access/SPI **or** Planner always injects the pack when the flag is on.
3. Shell call sites never hard-type FQCNs excluded from LITE.

#1 recurring bug: host preview works; export crashes or silently no-ops because a class was excluded from shell without soft-load + Planner.

### 6.2 Architecture locks

| Rule | Detail |
|------|--------|
| One template | Only `app/src/main/assets/template/webview_shell.apk` from `:shell` release |
| Size target | Shrink **generated** WebView APKs via LITE ⊕ packs, not only the host builder |
| Pure WEB default | WEB + system WebView + all optional flags off → `liteOnly`, no packs |
| Pack when needed | Non-LITE capability → feature id → `features/<id>/` + `enabled.json` |
| Parent-first classloader | Pack `PathClassLoader` is child of app loader. **Same FQCN cannot be LITE stub + pack real** — child never wins. LITE holds a **different** Access type; real FQCN lives only in pack |
| Author once | Sources under `app/`; shell/compat sync only |
| Host-only stays host | `core/apkbuilder`, host screens, sample/market, editor-only helpers must not re-enter LITE |
| Capability parity | Size without losing enabled-export behavior |

### 6.3 Dual runtime (preview vs export)

| | Host `:app` | Generated APK |
|--|-------------|---------------|
| DEX | Almost all `app/src` | Shell include−exclude + merged packs |
| Feature classes | On main classpath | Only if kept in LITE or pack loaded |
| `FeatureLoader.loadClass` | Often succeeds via host DEX | Needs pack DEX if excluded from LITE |
| Config | Editor / in-memory | Assets JSON via `ShellModeManager` |

| Symptom | Likely cause |
|---------|----------------|
| Preview OK, export `ClassNotFoundException` | Hard ref to excluded class; no pack/soft-load |
| Preview OK, export feature silently off | Soft-load null; Planner did not attach pack |
| Export always fat | Planner always packs; or heavy code still in LITE |
| Pack code never runs | Same FQCN stub in LITE shadows pack |
| Host compiles, shell does not | LITE still imports excluded type |

### 6.4 End-to-end pipeline

```text
app/src/main/java  (author)
   │
   ├─ syncShellRuntimeSources (include − exclude) → shell → assembleRelease
   │     → app/src/main/assets/template/webview_shell.apk
   │
   └─ syncFeatureCompatSources → feature-compat AAR
         → packageFeatureCompatPack
         → package_fine_feature_packs.py
         → app/src/main/assets/features/<id>/

Export
  ApkConfig/WebApp flags → CapabilityPlanner → feature ids
  → FeaturePackMerger → assets/features/* + enabled.json in output APK

Runtime (generated)
  WebToAppApplication → FeatureLoader.loadEnabled
  → PathClassLoader per pack → *Access / ScriptPackAccess / SPI
```

### 6.5 Where to edit

| Concern | Path |
|---------|------|
| LITE membership | `shell/build.gradle.kts` → `syncShellRuntimeSources` include/exclude |
| Compat / heavy sources | `feature-compat/build.gradle.kts` → `syncFeatureCompatSources` |
| Fine DEX packs | `scripts/package_fine_feature_packs.py` (after `packageFeatureCompatPack`) |
| Pack assets | `app/src/main/assets/features/<id>/` (`feature.json` + `classes.dex`…) |
| Feature ids | `feature-api/.../FeatureIds.kt` |
| Config → packs | `app/.../apkbuilder/CapabilityPlanner.kt` + `CapabilityPlannerTest` |
| Export merge | `FeaturePackMerger` + `ApkBuilder` (`enabled.json`) |
| Runtime load | `FeatureLoader` in shell `WebToAppApplication` |
| Soft-load helpers | `*Access`, `ScriptPackAccess` |

---

## 7. Soft-load pattern

Required whenever implementation leaves LITE.

### 7.1 Steps

1. Keep **config/model** types in LITE if shell JSON still deserializes them (`IsolationConfig`, `ErrorPageConfig`, …).
2. Move **heavy impl** out of shell (exclude) into `feature-compat` sync (+ fine pack prefixes).
3. Add LITE **Access** object: only `FeatureLoader.loadClass` / reflection / `ScriptPackAccess.callStaticString`. No hard import of heavy class from LITE call sites.
4. **Planner:** `need(FeatureIds.XXX, reason)` when export enables the feature. Default-off must not force packs on pure WEB.
5. **Fine pack** in `package_fine_feature_packs.py` for GRANULAR; sources still in feature-compat so COMPAT maps work.
6. Rebuild shell template + packs.
7. Tests: host unit tests on real classes; Planner tests for flag → id; fail closed (no pack → no crash, feature off).

### 7.2 Do / do not

**Do:** fail closed; match existing Access patterns; put moved sources in feature-compat for COMPAT phase.

**Do not:** same FQCN stub in LITE + real in pack; leave `import Heavy` in shell-synced sources; soft-load without Planner; put host-only tools back into LITE.

### 7.3 Reference Access implementations

| Access | Pack / area |
|--------|-------------|
| `IsolationPrivacyAccess` | `shell-privacy` |
| `ForcedRunHardwareAccess` | `shell-forcedrun-hw` |
| `ScriptPackAccess` | `ext-chrome-scripts`, `shell-disguise-js`, `shell-translate-script`, `shell-error-games` |
| `GeckoEngineAccess` | compat / engine-gecko |
| `FcmAccess` | compat / push-fcm |
| `TlsMitmAccess` | compat / net-tls-mitm |

### 7.4 SPI (feature-api)

Core types live in `:feature-api`:

- `FeatureModule` — pack entry `install(FeatureContext)`
- `FeatureLoader` — reads `assets/features/enabled.json`, builds `PathClassLoader`, loads dex
- `FeatureRegistry` — SPI registrations
- `FeatureIds` + `LITE_FEATURE_API`
- Planned/partial SPI: `BrowserEngineProvider`, `ServerRuntimeProvider`, `PushChannelProvider`, `NetworkHardeningProvider`, `ExtensionRuntimeProvider`, `ShellAddon`

LITE boot order: Application → `FeatureLoader.loadEnabled` → built-in system WebView path → shell init.

R8: keep `com.webtoapp.core.feature.**`, `FeatureModule` implementors, `ShellActivity`, shell `WebToAppApplication`. Do not blanket-keep `com.webtoapp.**`.

**Parent-first reminder:** soft-load real FQCN only from packs; LITE Access uses a different type name.

---

## 8. LITE membership (whitelist intent)

Shell membership is controlled by `shell/build.gradle.kts` sync include/exclude (source of truth in code). Intent:

### 8.1 Always LITE (pure WEB path)

- `ui/shell/**` shell chrome / router / WebView surface (server branches may still hard-link — high ROI to SPI later)
- `core/shell/**` config load, logger, runtime services
- `core/webview/**` except TLS MITM / advanced proxy pieces moved to packs
- System WebView engine path (`SystemWebViewEngine`, engine interfaces)
- `core/logging/**`, slim `core/i18n/**`, basic `core/errorpage/**` (games soft-loaded)
- Config models still deserialized at runtime
- Minimal util needed by shell (not host-only util list)

### 8.2 Must not enter LITE (denylist intent → packs or host)

| Area | Destination |
|------|-------------|
| `core/gecko/**`, `GeckoViewEngine` | `engine-gecko` / feature-compat |
| `core/nodejs|php|python|golang|wordpress/**` | server-* / compat (still largely in LITE today — split carefully) |
| `NotificationFcm*` | `push-fcm` / compat |
| `TlsMitm*` | `net-tls-mitm` / compat |
| Heavy extension scripts (builtins, panel, polyfill, …) | ext-* fine packs |
| `BrowserDisguiseJsGenerator`, `TranslateScriptProvider` | shell-disguise-js / shell-translate-script |
| Isolation manager + fingerprint + injector | `shell-privacy` |
| `ErrorPageGames` | `shell-error-games` |
| Forced-run hardware controllers | `shell-forcedrun-hw` |
| Host-only: apkbuilder, sample/frontend, CodeSnippets, ModuleTemplates, DebugTestPages, OnlineMusicApi/Downloader, host BgmPlayer, editor util storage helpers, … | never LITE |

### 8.3 Pure WEB without packs must still support

URL load, JS, storage, zoom, desktop mode, custom UA, basic chrome, static HTML/FRONTEND assets, basic splash, essential permissions, basic error UI (without mini-games).

If a “basic” switch secretly needs Gecko/Firebase/BC, Planner must pull compat until code is moved.

---

## 9. CapabilityPlanner

Code: `app/.../apkbuilder/CapabilityPlanner.kt`  
Tests: `CapabilityPlannerTest`

```text
Input:  ApkConfig (+ abi filters, PlannerPhase)
Output: CapabilityPlan(features, reasons, abiFilters, liteOnly)
liteOnly == true  ⇔  features.isEmpty()
```

### 9.1 Phases

| Phase | Behavior |
|-------|----------|
| **COMPAT** (default export) | Many granular ids collapse to `FeatureIds.COMPAT` (`feature-compat` fat pack). Capability must still work. |
| **GRANULAR** | Real fine packs + `closeDependencies` |

When adding an id: ensure COMPAT still covers capability (sources in feature-compat), and GRANULAR deps if A needs B.

### 9.2 LITE-only eligibility

All must hold:

- AppType in WEB / HTML / FRONTEND (others force packs or compat)
- Engine system WebView (Gecko → pack/compat)
- No advanced flags from §9.3

### 9.3 AppType → base feature

| AppType | Feature id (granular) | COMPAT |
|---------|----------------------|--------|
| WEB / HTML / FRONTEND | (none) if flags clean | (none) |
| IMAGE / VIDEO / GALLERY | `shell-media` | compat |
| MULTI_WEB | `shell-multiweb` | compat |
| NODEJS_APP | `server-nodejs` | compat |
| PHP_APP | `server-php` | compat |
| PYTHON_APP | `server-python` | compat |
| GO_APP | `server-go` | compat |
| WORDPRESS | `server-wordpress` (+ php dep when split) | compat |

### 9.4 Flags → feature id

| Condition | Feature id | COMPAT |
|-----------|------------|--------|
| Gecko engine | `engine-gecko` | compat |
| TLS fingerprint | `net-tls-mitm` | compat |
| Proxy enabled | `net-proxy` | compat |
| DoH / advanced DNS | `net-doh` | compat |
| Notification FCM | `push-fcm` | compat |
| Notification other channels | `notify-channels` | compat |
| Extensions / modules / userscripts | `ext-modules` | compat |
| Activation | `shell-activation` | compat |
| Announcement | `shell-announcement` | compat |
| Ad block / ads | `ads-adblock` | compat |
| Disguise (browser/device/…) | `shell-disguise` | compat |
| Isolation / privacy fingerprint | `shell-privacy` | compat |
| Error page mini-game | `shell-error-games` | compat |
| Device actions / blackTech | `shell-device-actions` | compat |
| Forced run | `shell-forcedrun` | compat |
| Floating window | `shell-floating` | compat |
| BGM | `shell-bgm` | compat |
| Translate | `shell-translate` | compat |
| Autostart | `shell-autostart` | compat |
| Background run | `shell-background` | compat |

### 9.5 GRANULAR dependencies (implemented)

| Parent | Also add |
|--------|----------|
| `server-wordpress` | `server-php` |
| `shell-forcedrun` | `shell-forcedrun-hw` |
| `ext-modules` | `ext-builtins`, `ext-panel`, `ext-chrome-scripts` |
| `shell-disguise` | `shell-disguise-js` |
| `shell-translate` | `shell-translate-script` |
| isolation | `shell-privacy` (leaf) |
| mini-game | `shell-error-games` (leaf) |

Do not `need()` a GRANULAR-only id without a pack folder unless COMPAT collapse still supplies the code.

---

## 10. Packaging

### 10.1 LITE template

- Build: `./gradlew :shell:assembleRelease :app:syncShellTemplateApk --no-configuration-cache`
- Output: `app/src/main/assets/template/webview_shell.apk`
- Content-stable template IDs; no second full template; no signed user APKs as templates
- Shell packaging strips known bloat (BC pqc, kotlin metadata junk, unused natives when liteOnly)

### 10.2 feature-compat

- Module `:feature-compat` syncs heavy sources excluded from LITE
- `compileOnly` against shell release classes + `feature-api`
- Task `packageFeatureCompatPack` → `app/src/main/assets/features/feature-compat/`
- Layout: `feature.json`, `classes.dex` (+ `classes2.dex`), optional natives/assets/manifest-delta
- Entry: `com.webtoapp.feature.compat.CompatFeatureModule`
- After excluding from shell, **always** add moved sources here if enabled export still needs them under COMPAT

### 10.3 Fine packs

- Script: `scripts/package_fine_feature_packs.py` (filters classes from feature-compat AAR → d8 → pack folder)
- Layout: `app/src/main/assets/features/<id>/{feature.json,classes.dex}`
- Prefer real `FeatureModule` entry when practical; soft-load Access still works once PathClassLoader is registered

### 10.4 Current fine pack inventory

| id | Role | Typical trigger |
|----|------|-----------------|
| `feature-compat` | Fat bucket (Gecko/FCM/TLS + synced heavies) | COMPAT / advanced needs |
| `ext-builtins` | Built-in modules / chrome store | extensions |
| `ext-panel` | Extension panel script | extensions |
| `ext-chrome-scripts` | Polyfill / mobile compat / userscript window | extensions |
| `shell-forcedrun-hw` | Hardware forced-run | forced run |
| `shell-disguise-js` | Browser disguise JS | disguise |
| `shell-translate-script` | Translate script | translate |
| `shell-privacy` | Isolation + fingerprint + injector | isolation |
| `shell-error-games` | Error page mini-games | `showMiniGame` |

Ids without a folder are **planned** only.

### 10.5 Export merge

`FeaturePackMerger.prepare` stages pack files + builds `enabled.json`.  
`ApkBuilder` writes them into the output APK.  
Optional: strip unused natives/bloat when `liteOnly`.

### 10.6 Size KPI (generated, arm64-v8a)

| Scenario | Goal |
|----------|------|
| Pure WEB, no packs | Keep LITE template small (order of ~2–3 MB class; continue careful shrinks) |
| Optional flags | Only required packs |
| Advanced COMPAT features | May pull fat `feature-compat`; **must not** regress capability |

---

## 11. Checklists

### 11.1 Move code out of LITE (size)

- [ ] Pure WEB default does not need this code (or tiny LITE fallback exists)
- [ ] `rg`: no hard imports of excluded FQCN from remaining shell sources
- [ ] Exclude in `shell/build.gradle.kts`; include in feature-compat if runtime still needs when enabled
- [ ] Access/soft-load at every LITE call site
- [ ] `FeatureIds` + `CapabilityPlanner` + unit test
- [ ] Fine pack prefixes if GRANULAR should ship a small pack
- [ ] Rebuild template + packs
- [ ] Measure template; pure WEB still `liteOnly`
- [ ] Simulate: host preview / export flag ON / export flag OFF

### 11.2 Add a new runtime feature

- [ ] Classify: **LITE always** | **pack if enabled** | **host-only**
- [ ] If pack: Access/SPI only from LITE; no hard heavy types
- [ ] Export config drives `CapabilityPlanner.need(...)`
- [ ] Shell config JSON / `ShellModeManager` / `ApkConfigJsonFactory` consistent
- [ ] Do not assume host Koin/full DEX in generated shell
- [ ] Editor-only UI stays host-side

### 11.3 Decision tree

```text
Needed at runtime in generated APK?
  NO  → host-only; exclude from shell; stop
  YES → Needed for pure WEB defaults?
          YES → keep in LITE (or tiny fallback)
          NO  → pack-if-enabled:
                  Access + exclude + feature-compat + Planner + pack
```

---

## 12. Forbidden / high-risk mistakes

- Second full shell template APK
- Same FQCN stub in LITE + real in pack
- Exclude class but leave hard imports/ctors in LITE sources
- Soft-load without Planner (flag ON → silent no-op)
- Host-only tools back into LITE “for convenience”
- Measuring only host APK size when the goal is generated WebView APKs
- Signed/renamed outputs as templates; unstable template IDs
- OEM push SDKs / unjustified heavy deps in LITE
- Half-excluding server/extension/adblock without Access + Planner (breaks export)

---

## 13. Verify commands

```bash
./gradlew :shell:assembleRelease :app:syncShellTemplateApk --no-configuration-cache
./gradlew :feature-compat:packageFeatureCompatPack --no-configuration-cache
python3 -c "import zipfile,os; p='app/src/main/assets/template/webview_shell.apk'; print(os.path.getsize(p)); z=zipfile.ZipFile(p); print([(i.filename,i.compress_size) for i in sorted(z.infolist(),key=lambda x:-x.compress_size)[:8]])"
./gradlew :app:testDebugUnitTest --tests "com.webtoapp.core.apkbuilder.CapabilityPlannerTest" -x :shell:assembleRelease -x :app:syncShellTemplateApk --no-configuration-cache
```

| Check | Expect |
|-------|--------|
| Template size | Track APK + `classes.dex` compress/raw |
| Pure WEB | `liteOnly`, empty features |
| Flag ON | Planner id present; pack/compat exists; soft-load works |
| Flag OFF | No crash; feature skipped |
| Shell compile | No refs to excluded FQCNs |

---

## 14. Implementation status (snapshot)

**Landed**

- Single LITE template + FeatureLoader + enabled.json merge
- feature-compat fat pack + fine packs (ext-*, shell-*, privacy, error-games, …)
- Soft-load Access patterns listed in §7.3
- CapabilityPlanner COMPAT/GRANULAR + unit tests
- Shell excludes for moved/host-only sources; slim i18n; arm64-lean export defaults

**Still open (next size/SPI slices)**

- Server runtimes out of LITE (nodejs/php/python/go/wordpress) via SPI
- AdBlocker pack-if-enabled
- Extension host further SPI split
- Manifest-delta merge for pack components
- CI size gate for LITE template + pure WEB export
- Prefer real `FeatureModule` installers on all fine packs

---

## 15. Doc maintenance

- **All agent architecture / LITE / pack / planner / packaging rules live in this file only.**
- Do not recreate `.github/docs/shell-lite/*` tutorial trees or parallel playbooks.
- Root `README.md` stays product-facing; do not dump agent internals there.
- When architecture changes: edit AGENTS.md in place; keep sections short, imperative, table-first.
- Success bar: pure WEB stays small; every enabled export switch still works; host preview and export stay aligned for runtime features.
