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
- **Shuffle on loop** (`shuffleOnLoop`).
- **Video auto-next** (`videoAutoNext`) — advance to the next item when a video ends.
- **Remember position** (`rememberPosition`) — resume playback position.
- **Enable audio** (`enableAudio`).

### View

- **Default view** (`defaultView`) — `GRID`, `LIST`, or `TIMELINE`.
- **Grid columns** (`gridColumns`).
- **Sort order** (`sortOrder`) — `CUSTOM`, `NAME_ASC`/`NAME_DESC`, `DATE_ASC`/`DATE_DESC`, or `TYPE`.
- **Show thumbnail bar** (`showThumbnailBar`).
- **Show media info** (`showMediaInfo`).

### Display

- **Orientation** (`orientation`).
- **Background color** (`backgroundColor`).

## Notes

- Preview launches the gallery player activity directly (not a WebView).
- For a single image or video, use [Media](/guide/app-types/media).
