# Contributing

Thank you for contributing to WebToApp. The canonical contributor guide lives in the repository:

👉 [`.github/CONTRIBUTING.md`](https://github.com/shiaho777/web-to-app/blob/main/.github/CONTRIBUTING.md)

## Contribution lanes

| Lane | What you do | Guide |
| --- | --- | --- |
| `modules/` | Publish a community module to the in-app market | [modules/README.md](https://github.com/shiaho777/web-to-app/blob/main/modules/README.md) |
| Issues | Report a bug or request a feature | [GitHub Issues](https://github.com/shiaho777/web-to-app/issues) |
| Code | Fix a bug or build a feature in the Android client | [CONTRIBUTING.md](https://github.com/shiaho777/web-to-app/blob/main/.github/CONTRIBUTING.md) |
| Docs | Improve this documentation site | Edit under `docs/` and open a PR |

## Ground rules

- **Do what is right, not the smallest diff.** If a fix calls for refactoring, renaming, or touching multiple files, do it. Match the patterns and conventions already in the surrounding code.
- **Do not add copyright or license headers** unless asked.
- **Do not commit secrets**, `local.properties`, keystores, or IDE/cache junk.
- **Prefer a pull request** over direct pushes to `main`.
- **GitHub Issues and PRs must be written in English** — titles, bodies, and delivery comments. Local chat may be any language.

## Delivery loop

When delivering a change, run the Issue → branch → PR → CI → merge loop end-to-end. Do not close the Issue until the PR is merged and CI is green. `main` is branch-protected and requires a PR plus a passing `check` status.

## Before you open a PR

- Changed shell membership, export packaging, or config fields? Run the [verify commands](/developer/recipes#verify-commands).
- New host UI string? Add all [10 languages](/developer/i18n).
- New editor setting that affects export? Walk the [full chain](/developer/config-drift#adding-a-setting-that-affects-the-generated-apk).
