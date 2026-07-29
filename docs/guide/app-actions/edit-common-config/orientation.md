# Screen Orientation

Locks or constrains the app's screen orientation.

**Where:** the **Screen orientation** card in the [Edit Common Config](/guide/app-actions/edit-common-config/) editor.

## Options

- **Landscape mode** — quick toggle to force landscape (`landscapeMode`).
- **Orientation mode** — fine-grained control (`orientationMode`):
  - `PORTRAIT`
  - `LANDSCAPE`
  - `REVERSE_PORTRAIT`
  - `REVERSE_LANDSCAPE`
  - `SENSOR_PORTRAIT`
  - `SENSOR_LANDSCAPE`

## Notes

- Selecting a landscape mode sets `landscapeMode` accordingly; sensor modes follow the device within a constraint.
- Splash and media have their own orientation settings (see [Splash Animation](/guide/app-actions/edit-common-config/splash)).
