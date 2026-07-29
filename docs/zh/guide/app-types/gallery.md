# 画廊

带浏览和播放控制的媒体集合,由专门的画廊播放器播放。

## 适用场景

相册、作品集和离线媒体查看器。

## 核心配置

由 `GalleryConfig` 支撑。

### 内容

- **媒体项**(`items`)—— 图片/视频,各带路径、类型、名称、时长、缩略图和大小。
- **分类**(`categories`)—— 把媒体项整理进有名、有色、有序的分组。

### 播放

- **播放模式**(`playMode`)—— `SEQUENTIAL`、`SHUFFLE` 或 `SINGLE_LOOP`。
- **图片间隔**(`imageInterval`)—— 幻灯片中每张图片的秒数。
- **循环**(`loop`)与 **自动播放**(`autoPlay`)。
- **循环时随机**(`shuffleOnLoop`)。
- **视频自动下一个**(`videoAutoNext`)—— 视频结束时前进到下一项。
- **记忆位置**(`rememberPosition`)—— 恢复播放位置。
- **启用音频**(`enableAudio`)。

### 视图

- **默认视图**(`defaultView`)—— `GRID`、`LIST` 或 `TIMELINE`。
- **网格列数**(`gridColumns`)。
- **排序**(`sortOrder`)—— `CUSTOM`、`NAME_ASC`/`NAME_DESC`、`DATE_ASC`/`DATE_DESC` 或 `TYPE`。
- **显示缩略图栏**(`showThumbnailBar`)。
- **显示媒体信息**(`showMediaInfo`)。

### 显示

- **方向**(`orientation`)。
- **背景色**(`backgroundColor`)。

## 说明

- 预览直接启动画廊播放器 activity(而非 WebView)。
- 单张图片或单个视频请用[媒体](/zh/guide/app-types/media)。
