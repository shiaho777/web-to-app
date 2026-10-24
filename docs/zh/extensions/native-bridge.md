# NativeBridge

`window.NativeBridge` 是网页调用 Android 能力的桥，不是未公开的私有接口。按应用打开后，在系统 WebView 里直接调用下面的方法。预览和导出的 APK 用的是同一套。

**位置：** 编辑器 → **特殊设置** → **原生桥**（`enableNativeBridge`）。新建应用默认打开。每一项能力还有自己的开关，在 `nativeBridgeCapabilities` 里。开关关掉时，对应方法直接返回，不执行。

GeckoView 没有这个对象。那种内核只保留 CORS 绕过 / 私有网络的 `httpRequest`。剪贴板、震动、分享和文件夹都要系统 WebView。

## 调用方式

```javascript
NativeBridge.showToast('已保存');
NativeBridge.vibrate(200);
const copied = NativeBridge.copyToClipboard('hello');
if (copied) NativeBridge.showToast('已复制');
NativeBridge.share('标题', '正文', 'https://example.com');
```

除了写明走 `window` 回调的方法，其余调用是同步的。

## 能力开关

| 开关 | 管到的方法 |
| --- | --- |
| 剪贴板 | `copyToClipboard`、`getClipboardText` |
| 震动 | `vibrate`、`vibratePattern` |
| Toast | `showToast` |
| 下载 | `saveImageToGallery`、`saveVideoToGallery`、`downloadVideo`、`downloadAudio`、`downloadWithHeaders`、`saveToFile` |
| 外部打开 | `openUrl`、`openApp` |
| 设备信息 | `getDeviceInfo`、`getAppInfo` |
| 安全信息 | `isDeveloperOptionsEnabled`、`isAdbEnabled`、`isDebuggable`、`getSecurityInfo` |
| 网络信息 | `isNetworkAvailable`、`getNetworkType` |
| 日志 | `log` |
| 页内查找 | `findInPage`、`findNextInPage`、`clearFindInPage` |
| 屏幕方向 | `setOrientation`、`getOrientation`、`lockOrientation`、`unlockOrientation` |
| 亮度 | `setScreenBrightness` |
| 屏幕常亮 | `setKeepScreenOn` |
| 全屏 | `enterFullscreen`、`exitFullscreen`、`isFullscreen` |
| 打印 | 走单独的打印桥 |
| 通知 | 系统通知，默认关 |
| Google 登录 | `googleSignIn`，默认关，还要填 Web client id |
| 文件夹 | `WtaDirectory`，默认关 |

## 剪贴板和震动

```javascript
NativeBridge.vibrate();          // 100 毫秒
NativeBridge.vibrate(500);
NativeBridge.vibratePattern('100,200,100,200');

const ok = NativeBridge.copyToClipboard('要复制的文字');
const text = NativeBridge.getClipboardText(); // 失败时是空字符串
```

`getClipboardText` 在系统要求时需要用户允许读取剪贴板。

## 分享和打开

```javascript
NativeBridge.share('标题', '正文', 'https://example.com');
NativeBridge.shareImage('https://example.com/image.jpg', '图片');
NativeBridge.openUrl('https://example.com');
NativeBridge.openApp('com.tencent.mm'); // 没安装这个包时返回 false
```

网页里的 `navigator.share` 是另一个开关：**网页分享**（`enableShareBridge`），默认开。`NativeBridge.share` 走的是这座桥。

## 相册和文件

```javascript
NativeBridge.saveImageToGallery('https://example.com/image.jpg', 'my_image.jpg');
NativeBridge.saveVideoToGallery('https://example.com/video.mp4', 'my_video.mp4');
NativeBridge.downloadAudio('https://example.com/audio.m4a', 'my_audio.m4a');
NativeBridge.downloadWithHeaders(
  'https://example.com/video.mp4',
  'video.mp4',
  JSON.stringify({ Referer: 'https://example.com' })
);
NativeBridge.saveToFile('笔记正文', 'note.txt', 'text/plain');
```

系统下载进相册时，图片放在 `Pictures/相册子目录`，视频和音频进对应的影片、音乐目录。子目录名在下载卡片的 **相册子目录**。留空就是 `WebToApp`。

## 屏幕、方向、查找

```javascript
NativeBridge.setOrientation('landscape'); // portrait | landscape | auto
NativeBridge.setKeepScreenOn(true);
NativeBridge.setScreenBrightness(0.8);    // -1 跟随系统
NativeBridge.enterFullscreen();

const state = JSON.parse(NativeBridge.findInPage('keyword'));
NativeBridge.findNextInPage(true);
NativeBridge.clearFindInPage();
```

## Google 登录

要在编辑器里打开，并填好 Web client id。结果不从返回值来。

```javascript
window.NativeBridgeGoogleSignInResult = function (requestId, payload) {
  if (payload.ok) {
    // 先在自己的服务器上校验 payload.idToken，再建立会话。
  }
};
NativeBridge.googleSignIn('sign-in-' + Date.now());
```

成功时 `payload` 是 `{ ok: true, idToken, displayName, profilePictureUri, googleUserId }`，失败时是 `{ ok: false, error, message }`。错误码包括 `DISABLED`、`NO_CLIENT_ID`、`NO_ACTIVITY`、`CALLER_NOT_ALLOWED`、`CANCELLED`、`CREDENTIAL_ERROR`、`UNAVAILABLE`。

## 文件夹（`WtaDirectory`）

先打开 **文件夹**。`pick()` 必须由用户点击触发。授权留在当前安装的应用里。导出的 APK 是另一个包名，要再选一次。GeckoView 没有这组方法。

```javascript
const grant = await WtaDirectory.pick();
if (!grant.granted) return;
const entries = WtaDirectory.list('').entries;
const font = await WtaDirectory.read('fonts/MyFont.ttf');
await WtaDirectory.write('exports/image.png', pngBytes);
```

| 方法 | 结果 |
| --- | --- |
| `directoryInfo()` | `{ ok, granted, name?, writable? }` |
| `pickDirectory(requestId)` | 打开系统目录选择器，结果送到 `window.NativeBridgeDirectoryResult` |
| `listDirectory(relativePath)` | `{ ok, entries, truncated }`。空路径是授权根 |
| `readDirectoryFile(relativePath, offset, length)` | 一块最多 256 KiB，`data` 是 base64 |
| `writeDirectoryFile(relativePath, base64, append)` | 写一块。`append: false` 是新建或覆盖 |
| `deleteDirectoryEntry(relativePath)` | 删除文件或子目录，不能删授权根 |
| `releaseDirectory()` | 丢掉授权 |

`WtaDirectory.pick`、`read`、`write`、`list` 是打开这项能力后注入的简便方法。表里的名字是底层 `NativeBridge` 方法。

## 设备和网络

```javascript
const device = JSON.parse(NativeBridge.getDeviceInfo());
const app = JSON.parse(NativeBridge.getAppInfo());
const security = JSON.parse(NativeBridge.getSecurityInfo());
NativeBridge.isNetworkAvailable();
NativeBridge.getNetworkType(); // "wifi" | "mobile" | "none" | "unknown"
NativeBridge.log('调试一行');
```

## 网页可以接的按键

这是 DOM 事件，不是 `NativeBridge` 的方法。预览和导出的 APK 都会发。

**返回键。** 编辑器里把返回键行为设为 **执行 JS**。每次系统返回都会在 `document` 上派发可取消的 `wta:back`。`preventDefault()` 就留在页面里。网页没处理时，仍然按历史返回。历史已经空了时，再按一次返回就离开应用。

```javascript
document.addEventListener('wta:back', function (event) {
  if (closeMyPanel()) event.preventDefault();
});
```

**音量键。** 打开 **把音量键交给网页** 后会派发 `wta:volume`。`event.detail.key` 是 `VolumeUp`、`VolumeDown` 或 `VolumeMute`。再打开 **接管时不改系统音量**，系统音量就不会变。这个开关关掉时，网页仍能收到事件，系统音量照常变。

```javascript
document.addEventListener('wta:volume', function (event) {
  if (event.detail.key === 'VolumeUp') stepForward();
});
```

## 网页指定的下载文件名

`<a download="chapter.txt" href="https://...">`、用 `File` 做出来的 `blob:` 地址，以及 `window.open` 打开这个 blob，都会沿用网页给的文件名。系统浏览器本来就会这样。WebView 的下载回调只看得到网址，所以壳自己读 `download` 属性和 `File.name`。
