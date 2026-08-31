# Activation Code Verification

Gate the generated app behind activation codes, so it runs only after a valid code is entered.

**Where:** the **Activation code** card in the [Edit Common Config](/guide/app-actions/edit-common-config/) editor.

## Options

- **Enable** — turn activation gating on (`activationEnabled`).
- **Activation codes** — the list of valid codes (`activationCodeList`).
- **Require every time** — ask for the code on every launch, not just the first (`activationRequireEveryTime`).
- **Dialog config** — customize the activation dialog (title, subtitle, input label, button text).
- **Remote activation** — verify against your own HTTPS endpoint signed with EC P-256, with an offline policy (`activationRemoteConfig`).
- **Device binding (one-time codes)** — remote-verification only (`activationRemoteConfig.deviceBound`). The verification request carries a device identifier and the server enforces per-code seats: each code is limited to 1 device by default (first device to activate claims the seat), so a code behaves as a one-time / single-device code. Other devices are rejected with the server's message. The claimed seat survives uninstall + reinstall on the same device. Requires a verification server that enforces binding — the [reference worker](https://github.com/shiaho777/web-to-app/blob/main/examples/remote-activation-worker/README.md) does this out of the box via `maxDevices`.

## Notes

- Local verification checks codes on-device; remote verification calls your server. See the [remote activation reference](https://github.com/shiaho777/web-to-app/blob/main/.github/docs/remote-activation.md).
- Device binding is impossible with local verification: the app has no shared state between devices, so any "one-time" claim on a purely local code cannot be enforced. Use remote verification with **Device binding** enabled for one-time codes.
- The **usage limit** on a local usage-limited code counts app launches on the device after activation — it is not a per-code redemption limit and resets if app data is cleared.
- The app list shows an activation chip on gated apps.
