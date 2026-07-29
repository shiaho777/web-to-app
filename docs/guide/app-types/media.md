# Media

A single-image or single-video app played by a dedicated media player.

## When to use

Single-image viewers, course media, or looping signage-style video.

## Variants

- **Image** — displays a picture full-screen.
- **Video** — plays a video with configurable playback.

## Core config

Backed by `MediaConfig`.

### Source

- **Media path** (`mediaPath`) — a local media file packaged into the app (optionally encrypted).

### Playback

- **Auto-play** (`autoPlay`) — start playback on launch.
- **Loop** (`loop`) — repeat playback.
- **Enable audio** (`enableAudio`) — sound on/off.

### Display

- **Fill screen** (`fillScreen`) — crop to fill vs fit.
- **Orientation** (`orientation`) — portrait/landscape.
- **Background color** (`backgroundColor`).
- **Keep screen on** (`keepScreenOn`).

## Notes

- Preview launches the media player activity directly (not a WebView).
- For a *collection* of media, use [Gallery](/guide/app-types/gallery).
