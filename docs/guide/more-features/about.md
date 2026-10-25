# About

App information and data tools. Open it from [⋮ → About](/guide/main-screen/more).

## Features

- **About** — version, current language, and project links.
- **Update check** — check for a newer version of the builder (shows current version and whether an update is available).
- **Legal disclaimer** — important notices covering permitted use and responsibilities.

## Host switches

These switches live only in the builder. Generated APKs do not read them.

- **Show descriptions** — helper text under feature titles. On by default. Titles, values, errors, and the only status on a control stay visible when it is off.
- **Separate WebApp tasks** — off by default. When on, home preview opens each WebApp in its own recents entry so several can stay open. That is another recents entry, not another process, and it uses more memory. Opening the same preview again brings that card forward. Turning the switch off closes those preview tasks. Desktop shortcuts always open in their own task, and Back returns to the launcher. Turning this switch off leaves shortcut tasks alone. A shortcut pinned by a much older build can still open a new card each time; create it again.
- **Advanced features** — off by default. When on, you can create, preview, and export Node.js, PHP, Python, Go, WordPress, image, and video apps. The code stays in the app either way. Existing projects stay in the list and can still be edited while the switch is off. [Linux Environment](/guide/more-features/linux-environment), [Runtime Management](/guide/more-features/runtime-management), and [Port Manager](/guide/more-features/port-manager) show in the ⋮ menu only while it is on. Server-runtime exports stay on targetSdk 28. WebView exports stay on the shell template's targetSdk 35. Play and AAB still refuse types that exec a process.
- **Local MCP** — off by default. When on, the builder serves the same tools as the in-app [Agent](/guide/more-features/agent) on `127.0.0.1`, plus a read-only preview tool, behind a bearer token. Long-press the address or the token to copy it. **Copy config** copies an `mcp.json`. **Rotate token** issues a new token. Write tools still ask for confirmation on the phone. From a computer, run `adb reverse tcp:<port> tcp:<port>`, then use the URL on the card. Generated APKs do not include this server.

## Notes

- Use [Data Backup](/guide/more-features/data-backup) before major changes or when switching devices.
- WebToApp is open source under [The Unlicense](https://github.com/shiaho777/web-to-app/blob/main/LICENSE).
