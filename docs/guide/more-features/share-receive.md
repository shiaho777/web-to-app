# Receive Shared Content

Lets a generated app appear in the Android share sheet, so content can be shared **into** it — a screenshot from the gallery, a photo from a chat, a link from a browser.

This is the mirror image of the outbound direction: `navigator.share` pushes the page's content out to the system. This feature pulls the system's content in.

Turn it on per app under **Special Settings → Basic Toggles** in the [Edit Common Config](/guide/app-actions/edit-common-config/) editor.

## What the user sees

1. Take a screenshot and tap **Share**.
2. The generated app is listed as a share target alongside the gallery, chat apps, and so on.
3. Tap it. The app opens (or comes to the front) and the content is waiting for the page.

## Two delivery channels

Once content arrives it has to reach the page. There are two ways, and they cover different cases.

### Page event

The app dispatches a `wta:share` DOM event on `window` and `document`, and also exposes a small API:

```js
// Subscribe to share batches. Returns an unsubscribe function.
const off = window.WTAShareInbox.onShare((items) => {
  for (const item of items) {
    // item.type     "image" | "video" | "audio" | "text" | "file"
    // item.mimeType "image/png"
    // item.name     "Screenshot_2026-09-17.png"
    // item.size     bytes
    // item.inline   true when dataUrl / text carries the content
    // item.dataUrl  "data:image/png;base64,…"  (when inline)
    // item.text     the shared text            (for text shares)
    // item.fileUrl  "file:///…"                (on-device copy)
  }
});

// Or pull on demand — items received in this document and not yet taken.
const pending = window.WTAShareInbox.peek();
const taken = window.WTAShareInbox.take();
```

The `wta:share` event carries the same array as `event.detail.items`:

```js
window.addEventListener('wta:share', (e) => {
  console.log(e.detail.items);
});
```

This channel is **zero taps** but only works on pages that listen — a custom HTML/frontend app, or an [extension module](/guide/more-features/extension-modules) that declares the `DOM_ACCESS` permission and registers a listener. Third-party sites do not know about it.

Images up to 5 MB are inlined as a base64 `data:` URL. Larger ones are still stored and still offered to the file chooser, but reach the page as metadata only (`inline: false`) — pushing multi-megabyte strings through the WebView bridge is neither reliable nor kind to memory.

### File chooser

Every upload control on every site has to go through the WebView's file chooser. When the page opens one, the app can answer it with the shared content instead of showing the system picker.

This channel works on **any** site — including ones that know nothing about this feature.

With **Ask before using** on (the default), a short prompt offers the shared file or the system picker. With it off, the upload is answered directly.

## Settings

| Setting | Effect |
| --- | --- |
| **Receive images** | Registers the app as a share target for images. |
| **Receive text and links** | Also accepts plain-text shares. Off by default: it makes the app appear for every shared link in every app. |
| **Delivery method** | `Page event`, `Pre-fill file chooser`, or `Both` (default). |
| **Ask before using** | Only shown when the file chooser channel is enabled. |

The feature is **off by default**. Enabling it changes the generated manifest — the app gains an `ACTION_SEND` entry point — which carries the same reputation cost that keeps geolocation and notification options opt-in.

## Limits

- **Third-party sites cannot be submitted to automatically.** Web content is not allowed to set a file input's value programmatically, and a site that does not listen for `wta:share` will never hear it. For those sites the realistic flow is: share it in, then tap the page's upload control and the shared content is used. Submitting the form, or synthesising a click on the upload button, is deliberately out of scope.
- **Received content expires after 1 hour.** The inbox is a hand-off buffer, not storage.
- **Limits per share:** 10 items, 64 MB per file, 192 MB in the inbox. Anything outside these is dropped, and every limit is enforced while streaming the copy — a sender is an untrusted third party.
- **Payloads are copied out of the sender's URI immediately**, while the one-shot read grant is valid; the URI itself is never stored.

## Notes

- Only `image/*` is registered unless **Receive text and links** is on.
- The intent-filter is injected at export time, so a build with the feature off is manifest-identical to one made before the option existed.
