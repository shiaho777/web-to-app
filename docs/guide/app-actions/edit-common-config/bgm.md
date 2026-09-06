# Background Music

Plays background music in the generated app, with synced lyrics.

**Where:** the **Background music** card in the [Edit Common Config](/guide/app-actions/edit-common-config/) editor.

## Options

- **Enable** — turn background music on (`bgmEnabled`).
- **Playlists** — add music tracks; supports synced **LRC lyrics** with lyric animations.
- **Play mode** — loop, sequential, or shuffle (`BgmPlayMode`). Shuffle starts on a random track, plays every track once per cycle with no repeats, and reshuffles on wrap.
- **Lyric styling** — custom font, color, stroke, and shadow for lyrics.
- **Online search** — search for music online.

## Notes

- BGM audio files are packaged into the exported APK (and can be encrypted).
- Online music search downloads tracks in their real format — MP3, M4A, AAC, OGG, FLAC, or WAV — all of which show up in the selector.
- Lyric and tag edits persist to the library (sidecar `.lrc` and tag files), surviving refreshes and restarts even for tracks not yet saved into an app config.
