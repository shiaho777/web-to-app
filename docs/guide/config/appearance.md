# Appearance

Shape the look and feel of the generated app.

## Splash screen

Image or video splash with skip behavior, trim ranges, and fixed orientation. Optionally allow tap-to-skip with a countdown.

::: info Splash countdown placement
When the status bar is visible, the countdown/skip chip automatically sits below it so it is never covered. In pure fullscreen (status bar hidden) it stays at the top corner.
:::

## Background music

Playlists with synced LRC lyrics, lyric animations, custom font/color/stroke/shadow, and online music search.

## Chrome & layout

- **Toolbar, status bar (light & dark), navigation, floating-window mode, and long-press menu styles.**
- **Status bar color** can follow the theme, use a custom color, go fully transparent, or use **PAGE_TOP** (sample the page's top pixels so the chrome matches the content).

## Download location

System Downloads, app-private storage, or a user-picked SAF folder — wired through the full packaging passthrough chain.

## Announcements

Templates for launch, interval, and no-network moments.

## Translation overlay

20 target languages via Google, MyMemory, LibreTranslate, or Lingva engines, with automatic failover. This translates the *content* of generated apps (separate from the host UI language).

## Notifications

Web Notification polyfill, scheduled and persistent notifications with progress, URL-polling foreground service, deep links, boot auto-start, scheduled launch, and background-run service.

---

::: tip Detailed customization guides coming soon
Per-feature configuration details are being written.
:::
