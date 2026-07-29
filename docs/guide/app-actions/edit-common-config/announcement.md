# Popup Announcement

Shows an announcement dialog in the generated app at configurable moments.

**Where:** the **Announcement** card in the [Edit Common Config](/guide/app-actions/edit-common-config/) editor.

## Options

- **Enable** — turn announcements on (`announcementEnabled`).
- **Content** — title and body; the body can be plain text or HTML (`announcementContentIsHtml`).
- **Link** — an optional action link with custom text.
- **Template** — visual style (`MINIMAL`, `XIAOHONGSHU`, `GRADIENT`, `GLASSMORPHISM`, `NEON`, `CUTE`, …).
- **Timing** — show on launch, on an interval, or when there is no network.
- **Show once / require confirmation** — control repeat behavior and whether the user must confirm.

## Notes

- Announcements are configured per app and ship inside the exported APK.
