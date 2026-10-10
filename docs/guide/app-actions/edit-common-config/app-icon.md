# App Icon

The icon shown for your app in the app list and, after export, on the device launcher.

**Where:** the **Basic info** card at the top of the [Edit Common Config](/guide/app-actions/edit-common-config/) editor.

## Options

- **Pick an image** — choose an image from your device (`image/*`). The selected image becomes the app icon.
- **Choose from icon library** — pick a previously saved icon (`savedIconPath`). The library supports manual **Upload image**: pick from the gallery and it is saved to the library and auto-selected.
- **Default** — if you set no icon, a type-specific default icon is used.
- **Icon background** — the plate behind an adaptive icon. A transparent image defaults to white. Pick a color when the mark needs a dark plate. An opaque image keeps the color of its own edges.

## Notes

- At export, the icon is written into the APK's resources (the binary resource table is patched). See [APK Export Config](/guide/app-actions/edit-common-config/apk-export).
