# Gallery

A collection of media with browsing and playback controls, played by a dedicated gallery player.

## When to use

Albums, portfolios, and offline media viewers.

## Core config

Backed by `GalleryConfig`.

### Content

- **Items** (`items`) — the images/videos, each with path, type, name, duration, thumbnail, and size.
- **Categories** (`categories`) — organize items into named, colored, ordered categories.

### Playback

- **Play mode** (`playMode`) — `SEQUENTIAL`, `SHUFFLE`, or `SINGLE_LOOP`.
- **Image interval** (`imageInterval`) — seconds per image in a slideshow.
- **Loop** (`loop`) and **auto-play** (`autoPlay`).
- **Shuffle on loop** (`shuffleOnLoop`) — reshuffle the order every time playback wraps around (only meaningful with loop on).
- **Video auto-next** (`videoAutoNext`) — advance to the next item when a video ends.
- **Remember position** (`rememberPosition`) — resume playback position (per gallery in preview; fixed slot in exported apps).
- **Enable audio** (`enableAudio`).

### View

- **Default view** (`defaultView`) — `GRID`, `LIST`, or `TIMELINE`. The overview doubles as the player entry: tap an item to jump into the pager, back returns to the overview.
- **Grid columns** (`gridColumns`).
- **Sort order** (`sortOrder`) — `CUSTOM`, `NAME_ASC`/`NAME_DESC`, `DATE_ASC`/`DATE_DESC`, or `TYPE`.
- **Show thumbnail bar** (`showThumbnailBar`).
- **Show media info** (`showMediaInfo`).

### Display

- **Orientation** (`orientation`).
- **Background color** (`backgroundColor`).

## Notes

- Preview launches the gallery player activity directly (not a WebView).
- The fullscreen image viewer in exported apps supports pinch-to-zoom (1x–5x around the focal point), pan while zoomed, and double-tap to toggle 1x/3x.
- For a single image or video, use [Media](/guide/app-types/media).
