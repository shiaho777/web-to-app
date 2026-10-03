# sample-bundles

Heavyweight shared dependency packs for the typed sample projects
(`python-*-shared` `.pypackages`, `go-*-shared` `vendor`), served over
`raw.githubusercontent.com` (+ CN mirrors / jsDelivr fallback) instead of being
embedded in the APK — roughly 30 MB of assets that only users who open those
samples ever need.

At runtime `SampleSharedPackManager.ensurePack()` fetches
`sample-bundles/<pack>.zip` off the default branch, verifies it against the
SHA-256 in `manifest.json`, extracts it under
`filesDir/sample_shared_packs/`, and `SampleProjectExtractor` copies the payload
subdir into the sample project. Bundled assets under
`app/src/main/assets/sample_projects/<pack>/` still take precedence when
present, so a fork can re-embed a pack without code changes.

## Regenerating

```bash
# pack dirs shaped like: <staging>/python-django-shared/.pypackages/...
python3 scripts/build_sample_bundles.py <staging-dir>
```

This writes deterministic zips (sorted entries, epoch timestamps) into
`sample-bundles/` plus an updated `manifest.json` — identical input produces
identical SHA-256, so the manifest only diffs when pack content changes.
