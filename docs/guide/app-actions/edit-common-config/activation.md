# Activation Code Verification

Gate the generated app behind activation codes, so it runs only after a valid code is entered.

**Where:** the **Activation code** card in the [Edit Common Config](/guide/app-actions/edit-common-config/) editor.

## Options

- **Enable** — turn activation gating on (`activationEnabled`).
- **Activation codes** — the list of valid codes (`activationCodeList`).
- **Require every time** — ask for the code on every launch, not just the first (`activationRequireEveryTime`).
- **Dialog config** — customize the activation dialog (title, subtitle, input label, button text).
- **Remote activation** — verify against your own HTTPS endpoint signed with EC P-256, with an offline policy (`activationRemoteConfig`).

## Notes

- Local verification checks codes on-device; remote verification calls your server. See the [remote activation reference](https://github.com/shiaho777/web-to-app/blob/main/.github/docs/remote-activation.md).
- The app list shows an activation chip on gated apps.
