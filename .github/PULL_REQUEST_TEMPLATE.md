## What

<!-- What does this PR change, and why? Link the issue it resolves. -->

Fixes #

## Verification

<!-- Report the actual commands you ran and their results. Green CI is not a substitute for verifying the behavior you changed. -->

- [ ] `:app:compileStandardDebugKotlin` (or the targeted compile for the touched module)
- [ ] Relevant unit tests run and green (name them)
- [ ] UI changes verified on a device/emulator, not just compiled (screenshot or uiautomator check)
- [ ] Export/shell changes: template rebuilt (`:shell:assembleRelease :app:syncShellTemplateApk`) and an exported APK exercised

## Checklist

- [ ] Issue filed first and linked with `Fixes #N` (delivery loop: Issue → PR → CI green → squash merge)
- [ ] New user-visible strings cover all 10 languages in inline `when (Strings.lang)` blocks, no `else ->`, no `R.string`
- [ ] Editor settings that must affect exported APKs flow through model → ApkConfig JSON → shell config → runtime, with coverage tests updated (`WebViewConfigBooleanCoverageTest`, `ShellUiParityTest`)
- [ ] `checkConfigFieldDrift` passes if config fields changed
- [ ] New third-party dependencies are justified and registered in `app/src/main/assets/legal/third_party_licenses.json`
- [ ] No secrets, keystores, `local.properties`, or build artifacts committed
- [ ] Helper copy gated by the About descriptions switch where appropriate
