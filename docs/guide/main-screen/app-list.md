# App List

The main area of [My Apps](/guide/main-screen/my-apps) is a scrollable list of app cards (filtered by the selected [category](/guide/main-screen/categories) and [search](/guide/main-screen/search)).

## Anatomy of a card

Each card shows:

- **Icon** — your chosen icon, or a type-specific default.
- **Name** and **URL** (or media path / entry file for non-web types).
- **Type chip** — Web, Multi-Web, HTML, Frontend, PHP, WordPress, Node.js, Python, Go, Image, Video, or Gallery.
- **Feature chips** — quick indicators such as activation gating, ad blocking, or announcement (one shown at a time).
- **Health dot** — a small status dot on the icon from URL health monitoring: green (online), amber (slow), red (offline).
- **Preview thumbnail** — a captured screenshot of the site; tap it to re-capture.

## Interactions

| Gesture | Result |
| --- | --- |
| **Tap the card** | Open the preview activity directly (WebView, gallery player, or media player). One back returns to the list. |
| **Tap ⋮ on the card** | Open the [action menu](/guide/app-actions/edit-core-config) |
| **Swipe the card left** | Quick [delete](/guide/app-actions/delete) (with confirmation) |

[About → Separate WebApp tasks](/guide/more-features/about) is off by default. When it is on, each home preview and each desktop shortcut gets its own recents entry. Opening the same app again brings that card forward. A shortcut created on an older build still starts a new card; create it again.

## Sort

**Sort** on My Apps chooses the order. The default is **Recently updated**.

- **Recently updated** — newest edit first.
- **Recently created**.
- **By name**.
- **Custom** — long-press a card, then drag it up or down. A swipe to the left still deletes. Drag is off while the search box has text. A category filter reorders only the apps you can see; the others keep their places. New and duplicated apps go to the top. Dragging does not change the updated time, so switching back to recently updated still follows real edits.
