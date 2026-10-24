# NativeBridge

`window.NativeBridge` is the page-side bridge to Android. It is not a hidden private API. Turn it on per app, then call the methods below from the page. The same bridge is available in preview and in the exported APK when the app uses the system WebView.

**Where:** editor → **Special settings** → **Native bridge** (`enableNativeBridge`). It is on for newly created apps. Each ability has its own switch under `nativeBridgeCapabilities`. A method whose switch is off returns without doing the work.

GeckoView does not host this object. On that engine only the CORS-bypass / private-network `httpRequest` entry point is available. Clipboard, vibration, sharing, and the directory helper need the system WebView.

## Call pattern

```javascript
NativeBridge.showToast('Saved');
NativeBridge.vibrate(200);
const copied = NativeBridge.copyToClipboard('hello');
if (copied) NativeBridge.showToast('Copied');
NativeBridge.share('Title', 'Body', 'https://example.com');
```

Methods are synchronous unless the section says the result comes back through a `window` callback.

## Capabilities

| Switch | Methods it gates |
| --- | --- |
| Clipboard | `copyToClipboard`, `getClipboardText` |
| Vibration | `vibrate`, `vibratePattern` |
| Toast | `showToast` |
| Download | `saveImageToGallery`, `saveVideoToGallery`, `downloadVideo`, `downloadAudio`, `downloadWithHeaders`, `saveToFile` |
| Open external | `openUrl`, `openApp` |
| Device info | `getDeviceInfo`, `getAppInfo` |
| Security info | `isDeveloperOptionsEnabled`, `isAdbEnabled`, `isDebuggable`, `getSecurityInfo` |
| Network info | `isNetworkAvailable`, `getNetworkType` |
| Logging | `log` |
| Find in page | `findInPage`, `findNextInPage`, `clearFindInPage` |
| Orientation | `setOrientation`, `getOrientation`, `lockOrientation`, `unlockOrientation` |
| Brightness | `setScreenBrightness` |
| Screen wake | `setKeepScreenOn` |
| Fullscreen | `enterFullscreen`, `exitFullscreen`, `isFullscreen` |
| Print | covered by the separate print bridge |
| Notification | system notifications. Off by default |
| Google sign-in | `googleSignIn`. Off by default. Needs a Web client id |
| Folder | `WtaDirectory`. Off by default |

## Clipboard and vibration

```javascript
NativeBridge.vibrate();          // 100ms
NativeBridge.vibrate(500);
NativeBridge.vibratePattern('100,200,100,200');

const ok = NativeBridge.copyToClipboard('text to copy');
const text = NativeBridge.getClipboardText(); // empty string on failure
```

`getClipboardText` needs the user to grant clipboard access when the platform asks.

## Share and open

```javascript
NativeBridge.share('Title', 'Body text', 'https://example.com');
NativeBridge.shareImage('https://example.com/image.jpg', 'Photo');
NativeBridge.openUrl('https://example.com');
NativeBridge.openApp('com.tencent.mm'); // returns false when the package is missing
```

Page-level `navigator.share` is a different switch: **Share from the page** (`enableShareBridge`), on by default. `NativeBridge.share` is this bridge.

## Gallery and files

```javascript
NativeBridge.saveImageToGallery('https://example.com/image.jpg', 'my_image.jpg');
NativeBridge.saveVideoToGallery('https://example.com/video.mp4', 'my_video.mp4');
NativeBridge.downloadAudio('https://example.com/audio.m4a', 'my_audio.m4a');
NativeBridge.downloadWithHeaders(
  'https://example.com/video.mp4',
  'video.mp4',
  JSON.stringify({ Referer: 'https://example.com' })
);
NativeBridge.saveToFile('note body', 'note.txt', 'text/plain');
```

System-download media goes into `Pictures/<gallery folder>` (and the matching movie or music folder). The folder name is **Gallery folder** on the download card. Blank stays `WebToApp`.

## Screen, orientation, find

```javascript
NativeBridge.setOrientation('landscape'); // portrait | landscape | auto
NativeBridge.setKeepScreenOn(true);
NativeBridge.setScreenBrightness(0.8);    // -1 follows the system
NativeBridge.enterFullscreen();

const state = JSON.parse(NativeBridge.findInPage('keyword'));
NativeBridge.findNextInPage(true);
NativeBridge.clearFindInPage();
```

## Google sign-in

Off until the editor enables it and a Web client id is set. The result is not the return value.

```javascript
window.NativeBridgeGoogleSignInResult = function (requestId, payload) {
  if (payload.ok) {
    // Verify payload.idToken on your server before creating a session.
  }
};
NativeBridge.googleSignIn('sign-in-' + Date.now());
```

`payload` is `{ ok: true, idToken, displayName, profilePictureUri, googleUserId }` or `{ ok: false, error, message }`. Error codes include `DISABLED`, `NO_CLIENT_ID`, `NO_ACTIVITY`, `CALLER_NOT_ALLOWED`, `CANCELLED`, `CREDENTIAL_ERROR`, and `UNAVAILABLE`.

## Folder (`WtaDirectory`)

Off until **Folder** is enabled. The page must call `pick()` from a user gesture. The grant stays on that installed app. An exported APK is a different package and asks again. GeckoView does not include this helper.

```javascript
const grant = await WtaDirectory.pick();
if (!grant.granted) return;
const entries = WtaDirectory.list('').entries;
const font = await WtaDirectory.read('fonts/MyFont.ttf');
await WtaDirectory.write('exports/image.png', pngBytes);
```

| Method | Result |
| --- | --- |
| `directoryInfo()` | `{ ok, granted, name?, writable? }` |
| `pickDirectory(requestId)` | Opens the system picker. The result arrives at `window.NativeBridgeDirectoryResult` |
| `listDirectory(relativePath)` | `{ ok, entries, truncated }`. An empty path is the granted root |
| `readDirectoryFile(relativePath, offset, length)` | One chunk, at most 256 KiB, base64 in `data` |
| `writeDirectoryFile(relativePath, base64, append)` | Writes one chunk. `append: false` creates or replaces |
| `deleteDirectoryEntry(relativePath)` | Deletes a file or a child directory, never the granted root |
| `releaseDirectory()` | Drops the grant |

`WtaDirectory.pick`, `read`, `write`, and `list` are the helpers injected with the capability. The object names above are the underlying `NativeBridge` methods.

## Device and network

```javascript
const device = JSON.parse(NativeBridge.getDeviceInfo());
const app = JSON.parse(NativeBridge.getAppInfo());
const security = JSON.parse(NativeBridge.getSecurityInfo());
NativeBridge.isNetworkAvailable();
NativeBridge.getNetworkType(); // "wifi" | "mobile" | "none" | "unknown"
NativeBridge.log('debug line');
```

## Keys the page can handle

These are DOM events, not `NativeBridge` methods. They exist in preview and in the exported APK.

**Back.** Editor → back behavior → **Run JavaScript**. Each system Back press dispatches a cancelable `wta:back` on `document`. `preventDefault()` keeps the app on the page. If the page does not handle the event, Back still walks history. When that history is empty, one Back leaves the app.

```javascript
document.addEventListener('wta:back', function (event) {
  if (closeMyPanel()) event.preventDefault();
});
```

**Volume.** **Send volume keys to the page** dispatches `wta:volume`. `event.detail.key` is `VolumeUp`, `VolumeDown`, or `VolumeMute`. **Do not change the system volume** swallows the key. Leave that switch off and the page still hears the event while the system volume changes.

```javascript
document.addEventListener('wta:volume', function (event) {
  if (event.detail.key === 'VolumeUp') stepForward();
});
```

## Downloads named by the page

An `<a download="chapter.txt" href="https://...">` link, a `blob:` URL created from a `File`, and `window.open` of that blob keep the page's file name. The system browser already did this. The WebView download listener only sees the URL, so the shell reads the `download` attribute and `File.name` itself.
