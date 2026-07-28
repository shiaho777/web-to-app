# Media Types

These types build media-focused apps. They don't use a WebView by default — preview launches a dedicated media player or gallery activity.

## Media

A single-image or single-video app.

- **Image** — displays a picture full-screen.
- **Video** — plays a video with configurable auto-play, looping, audio, and fill-screen (crop vs fit).
- **Source** — a local media file packaged into the app (optionally encrypted).
- **Good for** — single-image viewers, course media, looping signage-style video.

## Gallery

A collection of media with browsing and playback controls.

- **Organization** — categorized media.
- **Views** — grid, list, or timeline.
- **Playback** — shuffle, single-loop, sorting, thumbnail bar, overlays, auto-next, and playback memory.
- **Good for** — albums, portfolios, offline viewers.

## Preview behavior

When you tap a Media or Gallery app on [My Apps](/guide/main-screen), WebToApp launches the matching player activity directly (media player for Image/Video, gallery player for Gallery) rather than a WebView. Other types launch the WebView runtime.
