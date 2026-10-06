# Background Music

Plays background music in the generated app, with synced lyrics.

**Where:** the **Background music** card in the [Edit Common Config](/guide/app-actions/edit-common-config/) editor.

## Options

- **Enable** — turn background music on (`bgmEnabled`).
- **Playlists** — add music tracks; supports synced **LRC lyrics** with lyric animations.
- **Play mode** — loop, sequential, or shuffle (`BgmPlayMode`). Shuffle starts on a random track, plays every track once per cycle with no repeats, and reshuffles on wrap. Previous restarts the current track after a few seconds of playback, and otherwise goes to the previous track.
- **Floating player** (`showFloatingPlayer`) — a small bar you can drag. It snaps to the left or right edge and has pause, previous, and next.
- **Notification player** (`showNotificationPlayer`) — system media controls in the notification shade and on the lock screen: pause, skip, and scrub. Either player can be on by itself. An older config that never saved the switches keeps both on.
- **Lyric styling** — custom font, color, stroke, and shadow for lyrics.
- **Online search** — search for music online.

## Notes

- BGM audio files are packaged into the exported APK (and can be encrypted).
- Online music search downloads tracks in their real format — MP3, M4A, AAC, OGG, FLAC, or WAV — all of which show up in the selector.
- Lyric and tag edits persist to the library (sidecar `.lrc` and tag files), surviving refreshes and restarts even for tracks not yet saved into an app config.
