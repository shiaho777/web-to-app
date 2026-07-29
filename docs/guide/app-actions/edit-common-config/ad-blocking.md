# Ad Blocking

Blocks ads in the generated app using hosts rules and cosmetic filtering.

**Where:** the **Ad blocking** card in the [Edit Common Config](/guide/app-actions/edit-common-config/) editor.

## Options

- **Enable** — turn ad blocking on (`adBlockEnabled`).
- **Rules** — custom blocking rules (`adBlockRules`).
- **Subscriptions** — filter subscription URLs (`adBlockSubscriptions`).

## Notes

- Manage filter lists and subscriptions globally in [Hosts Ad Blocking](/guide/more-features/hosts-adblock) (20 built-in community lists).
- Ad blocking is wired for both preview and export: the host blocker serves preview, and the compiled rule set ships inside the exported APK.
