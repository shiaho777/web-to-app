# 接收分享内容

让生成的 App 出现在 Android 系统分享面板中，把内容分享**进** App —— 相册里的截图、聊天里的照片、浏览器里的链接。

这是"出向"的镜像：`navigator.share` 把页面内容推给系统，而这个功能把系统内容拉进页面。

在[编辑通用配置](/zh/guide/app-actions/edit-common-config/)编辑器的**特殊设置 → 基础开关**中按 App 开启。

## 用户看到的

1. 截图后点**分享**。
2. 生成的 App 与相册、聊天软件等一起出现在分享目标列表中。
3. 点它。App 打开（或切到前台），内容已经在页面里等着了。

## 两条投递通道

内容进来后要送到网页上。有两条路，各自覆盖不同的场景。

### 页面事件

App 会在 `window` 和 `document` 上派发 `wta:share` DOM 事件，同时暴露一个小 API：

```js
// 订阅分享批次，返回取消订阅函数。
const off = window.WTAShareInbox.onShare((items) => {
  for (const item of items) {
    // item.type     "image" | "video" | "audio" | "text" | "file"
    // item.mimeType "image/png"
    // item.name     "Screenshot_2026-09-17.png"
    // item.size     字节数
    // item.inline   true 表示 dataUrl / text 里带着内容
    // item.dataUrl  "data:image/png;base64,…"  （内联时）
    // item.text     分享的文本                  （文本分享）
    // item.fileUrl  "file:///…"                （本机副本）
  }
});

// 或者按需拉取 —— 当前文档收到且尚未取走的内容。
const pending = window.WTAShareInbox.peek();
const taken = window.WTAShareInbox.take();
```

`wta:share` 事件通过 `event.detail.items` 携带同一个数组：

```js
window.addEventListener('wta:share', (e) => {
  console.log(e.detail.items);
});
```

这条通道**零点击**，但只对会监听的页面有效 —— 你自己写的 HTML/前端 App，或者声明了 `DOM_ACCESS` 权限并注册监听的[扩展模块](/zh/guide/more-features/extension-modules)。第三方网站并不知道这个事件。

5 MB 以内的图片会以内联 base64 `data:` URL 交付。更大的图片仍会保存、仍会提供给文件选择器，但只以元数据形式到达页面（`inline: false`）—— 把几 MB 的字符串塞过 WebView 桥既不可靠，也浪费内存。

### 文件选择器

任何网站上任何上传控件都必然经过 WebView 的文件选择器。页面打开选择器时，App 可以直接用分享进来的内容应答，而不是弹出系统选择器。

这条通道对**任意**网站生效 —— 包括完全不知道这个功能存在的网站。

**使用前先询问**打开时（默认），会弹一个小提示让用户选择使用分享的文件还是走系统选择器；关闭后直接用分享内容应答上传。

## 设置

| 设置项 | 作用 |
| --- | --- |
| **接收图片** | 把 App 注册为图片分享目标。 |
| **接收文本与链接** | 同时接收纯文本分享。默认关闭：打开后，任何 App 里分享链接都会出现本 App。 |
| **投递方式** | `页面事件`、`预填文件选择器` 或 `两者都使用`（默认）。 |
| **使用前先询问** | 仅在启用文件选择器通道时显示。 |

功能**默认关闭**。开启会改变生成的清单 —— App 多出一个 `ACTION_SEND` 入口 —— 其信誉成本与定位、通知等选项相同，因此保持 opt-in。

## 限制

- **第三方网站无法自动提交。** Web 内容不允许以编程方式设置文件输入框的值，不监听 `wta:share` 的网站也永远收不到该事件。对这类网站，现实流程是：分享进来，然后在页面点击上传控件，分享的内容会被使用。自动提交表单、或模拟点击上传按钮，都在刻意排除的范围之外。
- **收到的内容 1 小时后过期。** 收件箱是交接缓冲区，不是存储。
- **单次分享上限：** 10 个条目、单文件 64 MB、收件箱 192 MB。超出会被丢弃，且每项限制都在流式复制过程中执行 —— 发送方是不可信的第三方。
- **内容会在发送方 URI 的一次性读权限仍有效时立即复制出来**，URI 本身不会被保存。

## 说明

- 除非打开**接收文本与链接**，否则只注册 `image/*`。
- intent-filter 在导出时注入，因此关闭该功能的构建产物与引入此选项之前完全一致。
