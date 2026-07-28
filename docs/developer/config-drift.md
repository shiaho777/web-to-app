# Config Field Drift

The most common silent failure in WebToApp: a feature works in preview but is quietly skipped in the exported APK because a config field name drifted between the export factory and the shell config class.

## Why it happens

The shell reads its config with **Gson**, and Gson **silently drops** unknown or missing fields — no exception, no log. So if the JSON key written by the export factory does not exactly match the `@SerializedName` on the shell config field, the feature just… doesn't run, with no error.

The three things that must stay aligned:

1. The payload keys in `ApkConfigJsonFactory.kt` (the `"key" to value` pairs).
2. The `@SerializedName("key")` annotations in `ShellModeManager.kt`'s shell config classes.
3. The `ApkConfig` fields they map from.

## The gate

A CI gate checks this automatically:

```bash
./gradlew :app:checkConfigFieldDrift --no-configuration-cache
# or directly:
python3 scripts/check_config_field_drift.py
```

It parses the payload keys out of `ApkConfigJsonFactory.kt` and the `@SerializedName` annotations out of `ShellModeManager.kt`, then reports any mismatch (subject to an allowlist in `scripts/config_field_drift_allowlist.json`).

**Run it whenever you change config fields.** It is part of the Android CI `check` job.

## Diagnosis checklist

When a feature "works in preview, broken after export," check in this order:

1. Did the shell config JSON actually contain the field at runtime?
2. Does the field name in `ApkConfig` JSON match the `@SerializedName` in shell config? Run `checkConfigFieldDrift`.
3. Is the runtime use site shell-synced (not host-only)?
4. For adblock specifically: confirm `adBlockEnabled` mapping, host filter rebuild from cached subscriptions, and export rule compile without wiping host state.
5. Rebuild the template after sync changes (stale template is a frequent miss).

## Adding a setting that affects the generated APK

Trace and update **all** of:

1. Model (`WebApp` / nested config) and editor UI binding.
2. Export mapping (`ApkBuilder` / `ApkConfig` / `ApkConfigJsonFactory`).
3. Shell config types (`ShellModeManager` / shell config data classes) if the runtime reads them.
4. Runtime use site in shell-synced code.
5. Unit tests for export wiring when flags change.

Missing any step usually yields: editor shows the switch, export ignores it, or export embeds config the runtime never reads.
