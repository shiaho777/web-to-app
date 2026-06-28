package com.webtoapp.core.extension

import com.webtoapp.core.i18n.Strings

object BuiltInModules {

    fun getAll(): List<ExtensionModule> = listOf(
        mediaDownloader(),
        videoEnhancer(),
        webAnalyzer(),
        findInPage(),
        advancedDarkMode(),
        privacyProtection(),
        contentEnhancer(),
        elementBlocker()
    )

    private fun mediaDownloader() = ExtensionModule(
        id = "builtin-media-downloader",
        name = Strings.builtinMediaDownloader,
        description = Strings.builtinMediaDownloaderDesc,
        icon = "download",
        category = ModuleCategory.MEDIA,
        tags = listOf(Strings.tagVideo, Strings.tagDownload, "MP4", "bilibili", "douyin", "xiaohongshu", "instagram", "facebook", "tiktok", "youtube"),
        version = ModuleVersion(5, "5.2.0", Strings.versionV4Ui),
        author = ModuleAuthor("WebToApp"),
        builtIn = true,
        enabled = false,
        runAt = ModuleRunTime.DOCUMENT_IDLE,
        permissions = listOf(ModulePermission.DOM_ACCESS, ModulePermission.DOWNLOAD),
        cssCode = MEDIA_DOWNLOADER_CSS,
        panelHtml = MEDIA_DOWNLOADER_PANEL_HTML,
        code = MEDIA_DOWNLOADER_CODE,
        runMode = ModuleRunMode.INTERACTIVE,
    )

    private fun videoEnhancer() = ExtensionModule(
        id = "builtin-video-enhancer",
        name = Strings.builtinVideoEnhancer,
        description = Strings.builtinVideoEnhancerDesc,
        icon = "movie",
        category = ModuleCategory.VIDEO,
        tags = listOf(Strings.tagSpeed, Strings.tagPiP, Strings.tagVideo),
        version = ModuleVersion(4, "4.0.0", Strings.versionV4Ui),
        author = ModuleAuthor("WebToApp"),
        builtIn = true,
        enabled = false,
        runAt = ModuleRunTime.DOCUMENT_IDLE,
        permissions = listOf(ModulePermission.DOM_ACCESS, ModulePermission.MEDIA),
        cssCode = VIDEO_ENHANCER_CSS,
        panelHtml = VIDEO_ENHANCER_PANEL_HTML,
        code = VIDEO_ENHANCER_CODE,
        runMode = ModuleRunMode.INTERACTIVE,
    )

    private fun webAnalyzer() = ExtensionModule(
        id = "builtin-web-analyzer",
        name = Strings.builtinWebAnalyzer,
        description = Strings.builtinWebAnalyzerDesc,
        icon = "search",
        category = ModuleCategory.DEVELOPER,
        tags = listOf(Strings.tagDebug, Strings.tagAnalyze, Strings.tagDevelop),
        version = ModuleVersion(4, "4.0.0", Strings.versionV4Ui),
        author = ModuleAuthor("WebToApp"),
        builtIn = true,
        enabled = false,
        runAt = ModuleRunTime.DOCUMENT_IDLE,
        permissions = listOf(ModulePermission.DOM_ACCESS, ModulePermission.NETWORK),
        cssCode = WEB_ANALYZER_CSS,
        panelHtml = WEB_ANALYZER_PANEL_HTML,
        code = WEB_ANALYZER_CODE,
        runMode = ModuleRunMode.INTERACTIVE,
    )

    private fun findInPage() = ExtensionModule(
        id = "builtin-find-in-page",
        name = Strings.builtinFindInPage,
        description = Strings.builtinFindInPageDesc,
        icon = "search",
        category = ModuleCategory.NAVIGATION,
        tags = listOf(Strings.tagSearch, Strings.tagKeyword, Strings.tagReading),
        version = ModuleVersion(1, "1.0.0", Strings.versionV4Ui),
        author = ModuleAuthor("WebToApp"),
        builtIn = true,
        enabled = false,
        runAt = ModuleRunTime.DOCUMENT_IDLE,
        permissions = listOf(ModulePermission.DOM_ACCESS, ModulePermission.NAVIGATION),
        panelHtml = FIND_IN_PAGE_PANEL_HTML,
        cssCode = FIND_IN_PAGE_CSS,
        code = FIND_IN_PAGE_CODE,
        runMode = ModuleRunMode.INTERACTIVE,
    )

    private fun advancedDarkMode() = ExtensionModule(
        id = "builtin-dark-mode",
        name = Strings.builtinDarkMode,
        description = Strings.builtinDarkModeDesc,
        icon = "dark_mode",
        category = ModuleCategory.THEME,
        tags = listOf(Strings.tagDark, Strings.tagEyeCare, Strings.tagTheme),
        version = ModuleVersion(4, "4.0.0", Strings.versionV4Ui),
        author = ModuleAuthor("WebToApp"),
        builtIn = true,
        enabled = false,
        runAt = ModuleRunTime.DOCUMENT_START,
        permissions = listOf(ModulePermission.CSS_INJECT, ModulePermission.STORAGE),
        panelHtml = DARK_MODE_PANEL_HTML,
        cssCode = DARK_MODE_CSS,
        code = DARK_MODE_CODE,
        runMode = ModuleRunMode.INTERACTIVE,
    )

    private fun privacyProtection() = ExtensionModule(
        id = "builtin-privacy-protection",
        name = Strings.builtinPrivacyProtection,
        description = Strings.builtinPrivacyProtectionDesc,
        icon = "shield",
        category = ModuleCategory.SECURITY,
        tags = listOf(Strings.tagPrivacy, Strings.tagSecurity, Strings.tagAntiTrack),
        version = ModuleVersion(4, "4.0.0", Strings.versionV4Ui),
        author = ModuleAuthor("WebToApp"),
        builtIn = true,
        enabled = false,
        runAt = ModuleRunTime.DOCUMENT_START,
        permissions = listOf(ModulePermission.DOM_ACCESS, ModulePermission.STORAGE),
        panelHtml = PRIVACY_PANEL_HTML,
        cssCode = PRIVACY_CSS,
        code = PRIVACY_PROTECTION_CODE,
        runMode = ModuleRunMode.INTERACTIVE,
    )

    private fun elementBlocker() = ExtensionModule(
        id = "builtin-element-blocker",
        name = Strings.builtinElementBlocker,
        description = Strings.builtinElementBlockerDesc,
        icon = "block",
        category = ModuleCategory.CONTENT_FILTER,
        tags = listOf(Strings.tagBlock, Strings.tagAd, Strings.tagElement),
        version = ModuleVersion(4, "4.0.0", Strings.versionV4Ui),
        author = ModuleAuthor("WebToApp"),
        builtIn = true,
        enabled = false,
        runAt = ModuleRunTime.DOCUMENT_IDLE,
        permissions = listOf(ModulePermission.DOM_ACCESS, ModulePermission.STORAGE),
        panelHtml = ELEMENT_BLOCKER_PANEL_HTML,
        cssCode = ELEMENT_BLOCKER_CSS,
        code = ELEMENT_BLOCKER_CODE,
        runMode = ModuleRunMode.INTERACTIVE,
    )

    private fun contentEnhancer() = ExtensionModule(
        id = "builtin-content-enhancer",
        name = Strings.builtinContentEnhancer,
        description = Strings.builtinContentEnhancerDesc,
        icon = "auto_awesome",
        category = ModuleCategory.CONTENT_ENHANCE,
        tags = listOf(Strings.tagCopy, Strings.tagTranslate, Strings.tagScreenshot),
        version = ModuleVersion(4, "4.0.0", Strings.versionV4Ui),
        author = ModuleAuthor("WebToApp"),
        builtIn = true,
        enabled = false,
        runAt = ModuleRunTime.DOCUMENT_END,
        permissions = listOf(ModulePermission.DOM_ACCESS, ModulePermission.CLIPBOARD),
        panelHtml = CONTENT_ENHANCER_PANEL_HTML,
        cssCode = CONTENT_ENHANCER_CSS,
        code = CONTENT_ENHANCER_CODE,
        runMode = ModuleRunMode.INTERACTIVE,
    )

    private const val MEDIA_DOWNLOADER_PANEL_HTML = """<div class="wta-media-empty"><div class="wta-media-empty-icon">⬇️</div><div id="wta-media-status">Scanning for media...</div></div>"""

    private const val VIDEO_ENHANCER_PANEL_HTML = """<div class="wta-video-empty"><div class="wta-video-empty-icon">🎬</div><div id="wta-video-status">Looking for video...</div></div>"""

    private const val WEB_ANALYZER_PANEL_HTML = """<div class="wta-analyzer-panel"><div class="wta-analyzer-section"><div class="wta-analyzer-section-title" id="wta-analyzer-status">Analyzing page...</div><div id="wta-analyzer-content"></div></div></div>"""

    private const val MEDIA_DOWNLOADER_CSS = """.wta-media-panel{padding:4px}
.wta-media-header{margin-bottom:16px}
.wta-media-title{font-size:15px;font-weight:600;color:var(--wta-on-surface,#1f2937);margin-bottom:4px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}
.wta-media-subtitle{font-size:13px;color:var(--wta-on-surface-variant,#9ca3af)}
.wta-media-btn{width:100%;border:none;padding:14px;border-radius:12px;font-size:15px;font-weight:500;cursor:pointer;display:flex;align-items:center;justify-content:center;gap:8px;margin-bottom:12px;color:#fff;transition:opacity .2s}
.wta-media-btn:active{opacity:.8}
.wta-media-tip{margin-top:16px;padding:12px;background:var(--wta-surface-dim,#fef3f6);border-radius:8px;font-size:12px;color:var(--wta-on-surface-variant,#9ca3af)}
.wta-media-empty{text-align:center;padding:40px;color:var(--wta-on-surface-variant,#9ca3af)}
.wta-media-empty-icon{font-size:48px;margin-bottom:16px}
.wta-media-list{max-height:200px;overflow-y:auto}
.wta-media-item{display:flex;align-items:center;gap:12px;padding:12px;background:var(--wta-surface-dim,#f9fafb);border-radius:8px;margin-bottom:8px}
.wta-media-item-icon{font-size:20px}
.wta-media-item-info{flex:1;font-size:13px;color:var(--wta-on-surface-variant,#4b5563);overflow:hidden;text-overflow:ellipsis;white-space:nowrap}
.wta-media-item-btn{background:var(--wta-surface-dim,#f3f4f6);border:none;padding:6px 12px;border-radius:6px;font-size:12px;cursor:pointer;color:var(--wta-on-surface,#374151)}"""

    private const val MEDIA_DOWNLOADER_CODE = """
(function() {
    'use strict';

    const LANG = (navigator.language || 'zh').toLowerCase().startsWith('ar') ? 'ar' :
                 (navigator.language || 'zh').toLowerCase().startsWith('zh') ? 'zh' : 'en';
    const I18N = {
        zh: { name: '媒体下载', noMedia: '未检测到媒体', detected: '检测到 {0} 个媒体', video: '视频', image: '图片', audio: '音频', blob: 'Blob流', download: '下载', blobNotSupported: 'Blob流暂不支持直接下载', downloading: '开始下载...', bilibiliTip: '提示：B站视频和音频分离，需用工具合并', quality: '画质', dlVideo: '下载视频流', dlAudio: '下载音频流', dlMusic: '下载背景音乐', dlAllImg: '下载全部图片', dlAllVid: '下载全部视频', dlNoWm: '下载无水印视频', copied: '链接已复制', platform: '平台', detected2: '检测到 {0} 张图片，{1} 个视频' },
        en: { name: 'Media Download', noMedia: 'No media detected', detected: '{0} media items detected', video: 'Video', image: 'Image', audio: 'Audio', blob: 'Blob', download: 'Download', blobNotSupported: 'Blob stream not supported for direct download', downloading: 'Downloading...', bilibiliTip: 'Tip: Bilibili separates video and audio, merge with tools', quality: 'Quality', dlVideo: 'Download Video', dlAudio: 'Download Audio', dlMusic: 'Download Music', dlAllImg: 'Download all images', dlAllVid: 'Download all videos', dlNoWm: 'Download without watermark', copied: 'Link copied', platform: 'Platform', detected2: '{0} images, {1} videos detected' },
        ar: { name: 'تحميل الوسائط', noMedia: 'لم يتم الكشف عن وسائط', detected: 'تم الكشف عن {0} وسائط', video: 'فيديو', image: 'صورة', audio: 'صوت', blob: 'Blob', download: 'تحميل', blobNotSupported: 'لا يدعم تحميل Blob مباشرة', downloading: 'جاري التحميل...', bilibiliTip: 'تلميح: بيليبيلي يفصل الفيديو والصوت، تحتاج أداة للدمج', quality: 'الجودة', dlVideo: 'تحميل الفيديو', dlAudio: 'تحميل الصوت', dlMusic: 'تحميل الموسيقى', dlAllImg: 'تحميل كل الصور', dlAllVid: 'تحميل كل الفيديوهات', dlNoWm: 'تحميل بدون علامة مائية', copied: 'تم نسخ الرابط', platform: 'المنصة', detected2: '{0} صورة، {1} فيديو' }
    };
    const T = I18N[LANG] || I18N.en;

    const MODULE = { id: (typeof __MODULE_INFO__ !== 'undefined' ? __MODULE_INFO__.id : 'media-downloader'), name: T.name, icon: '⬇️', color: '#667eea' };
    const host = location.hostname;
    let mediaList = [];

    function isBilibili() { return host.includes('bilibili.com'); }
    function isDouyin() { return host.includes('douyin.com'); }
    function isXiaohongshu() { return host.includes('xiaohongshu.com') || host.includes('xhslink.com'); }
    function isInstagram() { return host.includes('instagram.com'); }
    function isFacebook() { return host.includes('facebook.com') || host.includes('fb.watch'); }
    function isTikTok() { return host.includes('tiktok.com'); }
    function isYouTube() { return host.includes('youtube.com') || host.includes('youtu.be'); }

    function getPlatform() {
        if (isBilibili()) return 'bilibili';
        if (isYouTube()) return 'youtube';
        if (isDouyin()) return 'douyin';
        if (isXiaohongshu()) return 'xiaohongshu';
        if (isInstagram()) return 'instagram';
        if (isFacebook()) return 'facebook';
        if (isTikTok()) return 'tiktok';
        return 'generic';
    }

    function detectGenericVideos() {
        mediaList = [];
        document.querySelectorAll('video').forEach((v, i) => {
            const src = v.src || v.currentSrc || v.querySelector('source')?.src;
            if (src) mediaList.push({ type: 'video', src, blob: src.startsWith('blob:'), w: v.videoWidth, h: v.videoHeight, i: mediaList.length });
        });
        return mediaList;
    }

    const QN = { 127:'8K', 120:'4K', 116:'1080P60', 80:'1080P', 64:'720P', 32:'480P' };

    function getBilibiliInfo() {
        const p = window.__playinfo__;
        if (!p?.data) return null;
        const d = p.data, r = { video: null, audio: null, quality: '' };
        if (d.dash) {
            if (d.dash.video?.length) { const v = d.dash.video.sort((a,b) => (b.bandwidth||0)-(a.bandwidth||0))[0]; r.video = v.baseUrl || v.base_url; r.quality = QN[v.id] || v.id+'P'; }
            if (d.dash.audio?.length) { r.audio = d.dash.audio.sort((a,b) => (b.bandwidth||0)-(a.bandwidth||0))[0].baseUrl; }
        } else if (d.durl?.length) { r.video = d.durl[0].url; r.quality = QN[d.quality] || d.quality+'P'; }
        return r;
    }

    function findDouyinVideoData(obj, depth = 0) {
        if (depth > 10 || !obj || typeof obj !== 'object') return null;
        if (obj.video?.play_addr) {
            const url = obj.video.play_addr.url_list?.[0]?.replace('playwm', 'play').replace(/watermark=\d+/, 'watermark=0');
            return { id: obj.aweme_id || obj.id, desc: obj.desc || '', url, author: obj.author?.nickname || '' };
        }
        if (obj.aweme_detail) return findDouyinVideoData(obj.aweme_detail, depth + 1);
        if (obj.aweme_list?.[0]) return findDouyinVideoData(obj.aweme_list[0], depth + 1);
        for (const k of Object.keys(obj)) { const r = findDouyinVideoData(obj[k], depth + 1); if (r) return r; }
        return null;
    }

    function getDouyinVideoData() {
        try {
            if (window._ROUTER_DATA) { const r = findDouyinVideoData(window._ROUTER_DATA); if (r) return r; }
            if (window.__INITIAL_STATE__) { const r = findDouyinVideoData(window.__INITIAL_STATE__); if (r) return r; }
        } catch (e) {}
        return null;
    }

    function detectXiaohongshuMedia() {
        mediaList = [];
        document.querySelectorAll('img[src*="xhscdn"], img[src*="xiaohongshu"]').forEach((img) => {
            let src = img.src.split('?')[0];
            if (src.includes('avatar') || img.width < 100) return;
            if (!mediaList.find(m => m.src === src)) mediaList.push({ type: 'image', src, i: mediaList.length });
        });
        document.querySelectorAll('video').forEach((v) => {
            const src = v.src || v.querySelector('source')?.src;
            if (src && !mediaList.find(m => m.src === src)) mediaList.push({ type: 'video', src, i: mediaList.length });
        });
        return mediaList;
    }

    function getInstagramMedia() {
        mediaList = [];
        try {
            var scripts = document.querySelectorAll('script[type="application/json"]');
            for (var s = 0; s < scripts.length; s++) {
                var text = scripts[s].textContent;
                if (text && text.includes('video_url')) {
                    var match = text.match(/"video_url"\s*:\s*"([^"]+)"/);
                    if (match) {
                        var url = match[1].replace(/\\u0026/g, '&').replace(/\\\//g, '/');
                        mediaList.push({ type: 'video', src: url, i: 0 });
                    }
                }
                if (text && text.includes('display_url') && mediaList.length === 0) {
                    var imgMatch = text.match(/"display_url"\s*:\s*"([^"]+)"/);
                    if (imgMatch) {
                        var imgUrl = imgMatch[1].replace(/\\u0026/g, '&').replace(/\\\//g, '/');
                        mediaList.push({ type: 'image', src: imgUrl, i: 0 });
                    }
                }
            }
        } catch (e) {}
        if (mediaList.length === 0) {
            document.querySelectorAll('video').forEach((v) => {
                var src = v.src || v.currentSrc;
                if (src && !src.startsWith('blob:')) mediaList.push({ type: 'video', src, i: mediaList.length });
            });
        }
        if (mediaList.length === 0) {
            var ogVideo = document.querySelector('meta[property="og:video"]');
            if (ogVideo) mediaList.push({ type: 'video', src: ogVideo.content, i: 0 });
            var ogImg = document.querySelector('meta[property="og:image"]');
            if (ogImg && mediaList.length === 0) mediaList.push({ type: 'image', src: ogImg.content, i: 0 });
        }
        return mediaList;
    }

    function getFacebookMedia() {
        mediaList = [];
        try {
            var ogVideo = document.querySelector('meta[property="og:video"]');
            if (ogVideo && ogVideo.content) {
                mediaList.push({ type: 'video', src: ogVideo.content, i: 0 });
            }
            var ogVideoSecure = document.querySelector('meta[property="og:video:secure_url"]');
            if (ogVideoSecure && ogVideoSecure.content && !mediaList.find(m => m.src === ogVideoSecure.content)) {
                mediaList.push({ type: 'video', src: ogVideoSecure.content, i: mediaList.length });
            }
        } catch (e) {}
        if (mediaList.length === 0) {
            document.querySelectorAll('video').forEach((v) => {
                var src = v.src || v.currentSrc;
                if (src && !src.startsWith('blob:')) mediaList.push({ type: 'video', src, i: mediaList.length });
            });
        }
        if (mediaList.length === 0) {
            try {
                var scripts = document.querySelectorAll('script');
                for (var i = 0; i < scripts.length; i++) {
                    var text = scripts[i].textContent;
                    if (text && text.includes('video_data')) {
                        var matches = text.match(/"playable_url"\s*:\s*"([^"]+)"/g);
                        if (matches) {
                            matches.forEach(function(m) {
                                var url = m.match(/"([^"]+)"$/)[1].replace(/\\\//g, '/');
                                if (!mediaList.find(function(x) { return x.src === url; })) {
                                    mediaList.push({ type: 'video', src: url, i: mediaList.length });
                                }
                            });
                        }
                    }
                }
            } catch (e) {}
        }
        return mediaList;
    }

    function getTikTokMedia() {
        mediaList = [];
        try {
            var sigi = window.SIGI_STATE || window.__SIGI_STATE__;
            if (sigi) {
                var video = sigi.ItemModule?.[Object.keys(sigi.ItemModule)[0]]?.video;
                if (video) {
                    var playAddr = video.playAddr || video.play_addr;
                    if (playAddr) {
                        var url = Array.isArray(playAddr) ? playAddr[0] : (playAddr.url_list?.[0] || playAddr);
                        if (url) mediaList.push({ type: 'video', src: url, i: 0, desc: video.desc || '' });
                    }
                }
            }
            if (mediaList.length === 0) {
                var nextData = window.__NEXT_DATA__;
                if (nextData) {
                    var videoData = nextData.props?.pageProps?.itemInfo?.itemStruct?.video;
                    if (videoData) {
                        var addr = videoData.playAddr || videoData.play_addr;
                        var url = Array.isArray(addr) ? addr[0] : (addr?.url_list?.[0] || addr);
                        if (url) mediaList.push({ type: 'video', src: url, i: 0, desc: videoData.desc || '' });
                    }
                }
            }
        } catch (e) {}
        if (mediaList.length === 0) {
            document.querySelectorAll('video').forEach((v) => {
                var src = v.src || v.currentSrc;
                if (src && !src.startsWith('blob:')) mediaList.push({ type: 'video', src, i: mediaList.length });
            });
        }
        if (mediaList.length === 0) {
            var ogVideo = document.querySelector('meta[property="og:video"]');
            if (ogVideo && ogVideo.content) mediaList.push({ type: 'video', src: ogVideo.content, i: 0 });
        }
        return mediaList;
    }

    function getYouTubeMedia() {
        mediaList = [];
        try {
            var pr = window.ytInitialPlayerResponse;
            if (!pr) {
                var scripts = document.querySelectorAll('script');
                for (var i = 0; i < scripts.length; i++) {
                    var text = scripts[i].textContent;
                    if (text && text.indexOf('ytInitialPlayerResponse') >= 0) {
                        var match = text.match(/ytInitialPlayerResponse\s*=\s*(\{.+?\});/);
                        if (match) { try { pr = JSON.parse(match[1]); } catch(e) {} break; }
                    }
                }
            }
            if (!pr) return mediaList;
            var sd = pr.streamingData;
            if (!sd) return mediaList;

            function addStream(fmt, type) {
                if (!fmt || !fmt.url) return;
                var q = '';
                if (fmt.qualityLabel) q = fmt.qualityLabel;
                else if (fmt.audioQuality) q = fmt.audioQuality.replace('AUDIO_QUALITY_', '').toLowerCase();
                else if (fmt.bitrate) q = Math.round(fmt.bitrate / 1000) + 'kbps';
                mediaList.push({ type: type, src: fmt.url, q: q, mimeType: fmt.mimeType || '', i: mediaList.length });
            }

            (sd.formats || []).forEach(function(f) { addStream(f, 'video'); });
            var bestVideo = null, bestAudio = null;
            (sd.adaptiveFormats || []).forEach(function(f) {
                var mt = (f.mimeType || '');
                if (mt.indexOf('video') >= 0 && mt.indexOf('mp4') >= 0) {
                    if (!bestVideo || (f.bitrate || 0) > (bestVideo.bitrate || 0)) bestVideo = f;
                } else if (mt.indexOf('audio') >= 0 && mt.indexOf('mp4') >= 0) {
                    if (!bestAudio || (f.bitrate || 0) > (bestAudio.bitrate || 0)) bestAudio = f;
                }
            });
            if (bestVideo) addStream(bestVideo, 'video');
            if (bestAudio) addStream(bestAudio, 'audio');
        } catch (e) {}
        return mediaList;
    }

    let audioUrl = null;
    let audioTitle = '';

    function getTikTokAudio() {
        audioUrl = null;
        audioTitle = '';
        try {
            var sigi = window.SIGI_STATE || window.__SIGI_STATE__;
            if (sigi) {
                var keys = Object.keys(sigi.ItemModule || {});
                if (keys.length > 0) {
                    var item = sigi.ItemModule[keys[0]];
                    if (item?.music) {
                        var musicUrl = item.music.playUrl || item.music.play_url;
                        if (musicUrl) {
                            audioUrl = musicUrl;
                            audioTitle = item.music.title || item.music.author || '';
                        }
                    }
                }
            }
            if (!audioUrl && window.__NEXT_DATA__) {
                var videoData = window.__NEXT_DATA__.props?.pageProps?.itemInfo?.itemStruct;
                if (videoData?.music) {
                    var url = videoData.music.playUrl || videoData.music.play_url;
                    if (url) {
                        audioUrl = url;
                        audioTitle = videoData.music.title || videoData.music.authorName || '';
                    }
                }
            }
        } catch (e) {}
        return audioUrl;
    }

    function getDouyinAudio() {
        audioUrl = null;
        audioTitle = '';
        try {
            function findMusic(obj, depth) {
                if (depth > 10 || !obj || typeof obj !== 'object') return null;
                if (obj.music?.play_url?.url_list?.[0]) return { url: obj.music.play_url.url_list[0], title: obj.music.title || obj.music.author || '' };
                if (obj.music?.play_addr?.url_list?.[0]) return { url: obj.music.play_addr.url_list[0], title: obj.music.title || obj.music.author || '' };
                for (var k in obj) { var r = findMusic(obj[k], depth + 1); if (r) return r; }
                return null;
            }
            var src = null;
            if (window._ROUTER_DATA) src = findMusic(window._ROUTER_DATA, 0);
            if (!src && window.__INITIAL_STATE__) src = findMusic(window.__INITIAL_STATE__, 0);
            if (src) { audioUrl = src.url; audioTitle = src.title; }
        } catch (e) {}
        return audioUrl;
    }

    function getInstagramAudio() {
        audioUrl = null;
        audioTitle = '';
        try {
            var scripts = document.querySelectorAll('script[type="application/json"]');
            for (var s = 0; s < scripts.length; s++) {
                var text = scripts[s].textContent;
                if (text && text.includes('audio_url')) {
                    var match = text.match(/"audio_url"\s*:\s*"([^"]+)"/);
                    if (match) {
                        audioUrl = match[1].replace(/\\u0026/g, '&').replace(/\\\//g, '/');
                        return audioUrl;
                    }
                }
            }
        } catch (e) {}
        return audioUrl;
    }

    function getGenericAudio() {
        audioUrl = null;
        audioTitle = '';
        document.querySelectorAll('audio').forEach((a) => {
            if (!audioUrl) {
                var src = a.src || a.querySelector('source')?.src;
                if (src && !src.startsWith('blob:')) audioUrl = src;
            }
        });
        return audioUrl;
    }

    function detectPlatformAudio() {
        var platform = getPlatform();
        if (platform === 'tiktok') return getTikTokAudio();
        if (platform === 'douyin') return getDouyinAudio();
        if (platform === 'instagram') return getInstagramAudio();
        return getGenericAudio();
    }

    function downloadItem(url, filename, headers, type) {
        if (typeof NativeBridge !== 'undefined') {
            if (type === 'audio' && NativeBridge.downloadAudio) {
                if (headers && NativeBridge.downloadWithHeaders) {
                    NativeBridge.downloadWithHeaders(url, filename, JSON.stringify(headers));
                } else {
                    NativeBridge.downloadAudio(url, filename);
                }
            } else if (headers && NativeBridge.downloadWithHeaders) {
                NativeBridge.downloadWithHeaders(url, filename, JSON.stringify(headers));
            } else if (NativeBridge.downloadVideo) {
                NativeBridge.downloadVideo(url, filename);
            }
            __WTA_MODULE_UI__.toast(T.downloading);
        } else if (navigator.clipboard?.writeText) {
            navigator.clipboard.writeText(url);
            __WTA_MODULE_UI__.toast(T.copied);
        }
    }

    function getPanelHtml() {
        const platform = getPlatform();

        if (platform === 'bilibili') {
            const info = getBilibiliInfo();
            const title = document.querySelector('h1.video-title, .video-title')?.textContent || T.video;
            if (!info?.video && !info?.audio) return noMediaPanel('📺');
            let html = '<div class="wta-media-header"><div class="wta-media-title">' + escapeHtml(title) + '</div><div class="wta-media-subtitle" style="color:#fb7299">' + T.quality + ': ' + info.quality + '</div></div>';
            if (info.video) html += '<button class="wta-media-btn" style="background:linear-gradient(135deg,#fb7299,#fc9db8)" data-wta-action="mediaDL" data-wta-arg="bili_video"><span>⬇️</span> ' + T.dlVideo + '</button>';
            if (info.audio) html += '<button class="wta-media-btn" style="background:linear-gradient(135deg,#23ade5,#5bc0de)" data-wta-action="mediaDL" data-wta-arg="bili_audio"><span>🎵</span> ' + T.dlAudio + '</button>';
            html += '<div class="wta-media-tip">' + T.bilibiliTip + '</div>';
            return html;
        }

        if (platform === 'douyin') {
            const data = getDouyinVideoData();
            if (!data?.url) return noMediaPanel('🎵');
            var douyinHtml = '<div class="wta-media-header"><div class="wta-media-title">' + escapeHtml(data.desc || T.video) + '</div><div class="wta-media-subtitle">@' + escapeHtml(data.author) + '</div></div><button class="wta-media-btn" style="background:linear-gradient(135deg,#fe2c55,#ff6b81)" data-wta-action="mediaDL" data-wta-arg="douyin"><span>⬇️</span> ' + T.dlNoWm + '</button>';
            if (getDouyinAudio()) douyinHtml += '<button class="wta-media-btn" style="background:linear-gradient(135deg,#23ade5,#5bc0de)" data-wta-action="mediaDL" data-wta-arg="audio"><span>🎵</span> ' + T.dlMusic + '</button>';
            return douyinHtml;
        }

        if (platform === 'xiaohongshu') {
            detectXiaohongshuMedia();
            if (!mediaList.length) return noMediaPanel('📕');
            const images = mediaList.filter(m => m.type === 'image');
            const videos = mediaList.filter(m => m.type === 'video');
            let html = '<div class="wta-media-subtitle" style="margin-bottom:16px">' + T.detected2.replace('{0}', images.length).replace('{1}', videos.length) + '</div>';
            if (images.length) html += '<button class="wta-media-btn" style="background:linear-gradient(135deg,#ff2442,#ff6b7a)" data-wta-action="mediaDL" data-wta-arg="xhs_all_image"><span>🖼️</span> ' + T.dlAllImg + ' (' + images.length + ')</button>';
            if (videos.length) html += '<button class="wta-media-btn" style="background:linear-gradient(135deg,#667eea,#764ba2)" data-wta-action="mediaDL" data-wta-arg="xhs_all_video"><span>🎬</span> ' + T.dlAllVid + ' (' + videos.length + ')</button>';
            html += '<div class="wta-media-list">';
            mediaList.forEach(function(m, i) {
                html += '<div class="wta-media-item"><span class="wta-media-item-icon">' + (m.type === 'image' ? '🖼️' : '🎬') + '</span><div class="wta-media-item-info">' + (m.type === 'image' ? T.image : T.video) + ' ' + (i+1) + '</div><button class="wta-media-item-btn" data-wta-action="mediaDL" data-wta-arg="xhs_' + i + '">' + T.download + '</button></div>';
            });
            html += '</div>';
            return html;
        }

        if (platform === 'instagram') {
            getInstagramMedia();
            if (!mediaList.length) return noMediaPanel('📷');
            var igHtml = renderMediaListPanel('#E1306C', '📷');
            if (getInstagramAudio()) igHtml += '<button class="wta-media-btn" style="background:linear-gradient(135deg,#23ade5,#5bc0de);margin-top:8px" data-wta-action="mediaDL" data-wta-arg="audio"><span>🎵</span> ' + T.dlAudio + '</button>';
            return igHtml;
        }

        if (platform === 'facebook') {
            getFacebookMedia();
            if (!mediaList.length) return noMediaPanel('📘');
            return renderMediaListPanel('#1877F2', '📘');
        }

        if (platform === 'tiktok') {
            getTikTokMedia();
            if (!mediaList.length) return noMediaPanel('🎵');
            var tiktokHtml = renderMediaListPanel('#000000', '🎵');
            if (getTikTokAudio()) tiktokHtml += '<button class="wta-media-btn" style="background:linear-gradient(135deg,#23ade5,#5bc0de);margin-top:8px" data-wta-action="mediaDL" data-wta-arg="audio"><span>🎵</span> ' + T.dlMusic + '</button>';
            return tiktokHtml;
        }

        if (platform === 'youtube') {
            getYouTubeMedia();
            if (!mediaList.length) return noMediaPanel('▶️');
            var ytItems = mediaList.map(function(m, i) {
                var label = m.type === 'audio' ? T.audio : T.video;
                var sub = m.q || (m.type === 'audio' ? 'm4a' : 'mp4');
                return '<div class="wta-media-item" style="padding:16px;border-radius:12px"><div style="width:48px;height:48px;background:linear-gradient(135deg,#FF0000,#CC0000);border-radius:12px;display:flex;align-items:center;justify-content:center;color:white;font-size:20px">' + (m.type === 'audio' ? '🎵' : '🎬') + '</div><div style="flex:1"><div style="font-weight:600;color:var(--wta-on-surface,#1f2937)">' + label + '</div><div class="wta-media-subtitle">' + sub + '</div></div><button class="wta-media-item-btn" style="background:linear-gradient(135deg,#FF0000,#CC0000);color:#fff;padding:10px 20px;border-radius:8px" data-wta-action="mediaDL" data-wta-arg="yt_' + i + '">' + T.download + '</button></div>';
            }).join('');
            return '<div class="wta-media-subtitle" style="margin-bottom:16px">' + T.detected.replace('{0}', mediaList.length) + '</div>' + ytItems;
        }

        detectGenericVideos();
        if (!mediaList.length) return noMediaPanel('🎬');
        let html = '<div class="wta-media-subtitle" style="margin-bottom:16px">' + T.detected.replace('{0}', mediaList.length) + '</div>';
        mediaList.forEach(function(v, i) {
            html += '<div class="wta-media-item" style="padding:16px;border-radius:12px"><div style="width:48px;height:48px;background:linear-gradient(135deg,#667eea,#764ba2);border-radius:12px;display:flex;align-items:center;justify-content:center;color:white;font-size:20px">🎬</div><div style="flex:1"><div style="font-weight:600;color:var(--wta-on-surface,#1f2937)">' + T.video + ' ' + (i+1) + '</div><div class="wta-media-subtitle">' + (v.w || '?') + 'x' + (v.h || '?') + ' · ' + (v.blob ? T.blob : 'MP4') + '</div></div><button class="wta-media-item-btn" style="background:linear-gradient(135deg,#667eea,#764ba2);color:#fff;padding:10px 20px;border-radius:8px" data-wta-action="mediaDL" data-wta-arg="generic_' + i + '">' + T.download + '</button></div>';
        });
        if (getGenericAudio()) {
            html += '<button class="wta-media-btn" style="background:linear-gradient(135deg,#23ade5,#5bc0de);margin-top:8px" data-wta-action="mediaDL" data-wta-arg="audio"><span>🎵</span> ' + T.audio + ' ' + T.download + '</button>';
        }
        return html;
    }

    function noMediaPanel(icon) {
        return '<div class="wta-media-empty"><div class="wta-media-empty-icon">' + icon + '</div><div>' + T.noMedia + '</div></div>';
    }

    function renderMediaListPanel(color, icon) {
        var html = '<div class="wta-media-subtitle" style="margin-bottom:16px">' + T.detected.replace('{0}', mediaList.length) + '</div>';
        mediaList.forEach(function(m, i) {
            html += '<div class="wta-media-item" style="padding:16px;border-radius:12px">' +
                '<div style="width:48px;height:48px;background:linear-gradient(135deg,' + color + ',' + color + '99);border-radius:12px;display:flex;align-items:center;justify-content:center;color:white;font-size:20px">' + (m.type === 'image' ? '🖼️' : icon) + '</div>' +
                '<div style="flex:1"><div style="font-weight:600;color:var(--wta-on-surface,#1f2937)">' + (m.type === 'image' ? T.image : T.video) + ' ' + (i+1) + '</div></div>' +
                '<button class="wta-media-item-btn" style="background:linear-gradient(135deg,' + color + ',' + color + '99);color:#fff;padding:10px 20px;border-radius:8px" data-wta-action="mediaDL" data-wta-arg="list_' + i + '">' + T.download + '</button></div>';
        });
        return html;
    }

    function escapeHtml(s) {
        return String(s || '').replace(/[&<>"']/g, function(ch) {
            return ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[ch];
        });
    }

    window.__wta_module_action_mediaDL = function(action) {
        const platform = getPlatform();

        if (action.startsWith('bili_')) {
            const info = getBilibiliInfo();
            const type = action.slice(5);
            const url = type === 'video' ? info?.video : info?.audio;
            if (!url) return;
            const fn = 'bilibili_' + type + '_' + Date.now() + (type === 'video' ? '.m4s' : '.m4a');
            downloadItem(url, fn, { Referer: 'https://www.bilibili.com' }, type === 'audio' ? 'audio' : undefined);
            __WTA_MODULE_UI__.closePanel();
            return;
        }

        if (action.startsWith('yt_')) {
            const idx = parseInt(action.slice(3));
            getYouTubeMedia();
            const m = mediaList[idx];
            if (!m) return;
            const ext = m.type === 'audio' ? '.m4a' : '.mp4';
            const fn = 'youtube_' + m.type + '_' + (m.q || Date.now()).replace(/[^a-zA-Z0-9]/g, '') + ext;
            downloadItem(m.src, fn);
            __WTA_MODULE_UI__.closePanel();
            return;
        }

        if (action === 'audio') {
            if (!audioUrl) return;
            var ext = platform === 'bilibili' ? '.m4a' : '.mp3';
            var fn = (audioTitle ? audioTitle.replace(/[^\w\u4e00-\u9fa5]/g, '_').slice(0, 40) : platform + '_audio') + '_' + Date.now() + ext;
            var headers = platform === 'bilibili' ? { Referer: 'https://www.bilibili.com' } : undefined;
            downloadItem(audioUrl, fn, headers, 'audio');
            __WTA_MODULE_UI__.closePanel();
            return;
        }

        if (action === 'douyin') {
            const data = getDouyinVideoData();
            if (!data?.url) return;
            downloadItem(data.url, 'douyin_' + (data.id || Date.now()) + '.mp4');
            __WTA_MODULE_UI__.closePanel();
            return;
        }

        if (action.startsWith('xhs_')) {
            if (action === 'xhs_all_image' || action === 'xhs_all_video') {
                const type = action === 'xhs_all_image' ? 'image' : 'video';
                const items = mediaList.filter(m => m.type === type);
                items.forEach((m, i) => setTimeout(() => {
                    const ext = type === 'image' ? '.jpg' : '.mp4';
                    downloadItem(m.src, 'xhs_' + type + '_' + (Date.now() + i) + ext);
                }, i * 500));
                __WTA_MODULE_UI__.toast(T.downloading);
                __WTA_MODULE_UI__.closePanel();
                return;
            }
            const idx = parseInt(action.slice(4));
            const m = mediaList[idx];
            if (!m) return;
            const ext = m.type === 'image' ? '.jpg' : '.mp4';
            downloadItem(m.src, 'xhs_' + m.type + '_' + Date.now() + ext);
            return;
        }

        if (action.startsWith('generic_')) {
            const idx = parseInt(action.slice(8));
            const v = mediaList[idx];
            if (!v) return;
            if (v.blob) { __WTA_MODULE_UI__.toast(T.blobNotSupported); return; }
            downloadItem(v.src, 'video_' + Date.now() + '.mp4');
            __WTA_MODULE_UI__.closePanel();
            return;
        }

        if (action.startsWith('list_')) {
            const idx = parseInt(action.slice(5));
            const m = mediaList[idx];
            if (!m) return;
            const prefix = platform === 'instagram' ? 'instagram' : platform === 'facebook' ? 'facebook' : 'tiktok';
            const ext = m.type === 'image' ? '.jpg' : '.mp4';
            downloadItem(m.src, prefix + '_' + m.type + '_' + Date.now() + ext);
            __WTA_MODULE_UI__.closePanel();
            return;
        }
    };

    function register() {
        if (typeof __WTA_MODULE_UI__ === 'undefined') { setTimeout(register, 100); return; }
        __WTA_MODULE_UI__.register({ ...MODULE, uiConfig: (typeof __MODULE_UI_CONFIG__ !== 'undefined' ? __MODULE_UI_CONFIG__ : undefined), runMode: (typeof __MODULE_RUN_MODE__ !== 'undefined' ? __MODULE_RUN_MODE__ : 'INTERACTIVE'), onAction: c => c.innerHTML = getPanelHtml() });
    }

    const needsDelay = isBilibili() || isDouyin() || isXiaohongshu() || isInstagram() || isFacebook() || isTikTok();
    const delay = needsDelay ? 1000 : 0;
    if (delay) setTimeout(register, delay);
    else document.readyState === 'loading' ? document.addEventListener('DOMContentLoaded', register) : register();
})();
"""

    private const val VIDEO_ENHANCER_CSS = """.wta-video-panel{padding:4px}
.wta-video-empty{text-align:center;padding:40px;color:var(--wta-on-surface-variant,#9ca3af)}
.wta-video-empty-icon{font-size:48px;margin-bottom:16px}
.wta-video-section{margin-bottom:16px}
.wta-video-section-title{font-size:14px;color:var(--wta-on-surface-variant,#6b7280);margin-bottom:12px;display:flex;align-items:center;gap:6px}
.wta-video-speed-grid{display:flex;flex-wrap:wrap;gap:8px}
.wta-video-speed-btn{padding:10px 16px;border-radius:10px;border:1px solid var(--wta-outline,#e5e7eb);background:var(--wta-surface-dim,#f9fafb);font-size:14px;cursor:pointer;color:var(--wta-on-surface,#374151);transition:all .2s}
.wta-video-speed-btn:active{background:var(--wta-accent-soft,rgba(99,102,241,.1))}
.wta-video-feature-grid{display:grid;grid-template-columns:1fr 1fr;gap:8px}
.wta-video-feature-btn{padding:14px;border-radius:12px;border:none;background:var(--wta-surface-dim,#f3f4f6);font-size:14px;cursor:pointer;display:flex;flex-direction:column;align-items:center;gap:4px;color:var(--wta-on-surface,#374151);transition:all .2s}
.wta-video-feature-btn:active{background:var(--wta-accent-soft,rgba(99,102,241,.1))}
.wta-video-feature-icon{font-size:20px}
.wta-video-yt-toggle{display:flex;align-items:center;justify-content:space-between;padding:12px 14px;border-radius:12px;background:var(--wta-surface-dim,#f9fafb);margin-bottom:8px}
.wta-video-yt-toggle-info{display:flex;flex-direction:column;gap:2px;min-width:0}
.wta-video-yt-toggle-title{font-size:14px;color:var(--wta-on-surface,#374151);display:flex;align-items:center;gap:6px}
.wta-video-yt-toggle-desc{font-size:11px;color:var(--wta-on-surface-variant,#9ca3af);overflow:hidden;text-overflow:ellipsis;white-space:nowrap}
.wta-video-yt-chip{padding:2px 6px;border-radius:4px;font-size:10px;font-weight:600}
.wta-video-yt-chip-on{background:rgba(205,68,50,.15);color:#cc4432}
.wta-video-yt-chip-off{background:var(--wta-outline,#e5e7eb);color:var(--wta-on-surface-variant,#9ca3af)}"""

    private const val VIDEO_ENHANCER_CODE = """
(function() {
    'use strict';

    const LANG = (navigator.language || 'zh').toLowerCase().startsWith('ar') ? 'ar' :
                 (navigator.language || 'zh').toLowerCase().startsWith('zh') ? 'zh' : 'en';
    const I18N = {
        zh: { name: '视频增强', noVideo: '未检测到视频', speed: '播放速度', speedSet: '播放速度: ', features: '功能', pip: '画中画', loop: '循环播放', back10: '后退10秒', fwd10: '前进10秒', pipOn: '已开启画中画', pipOff: '退出画中画', pipUnavail: '画中画不可用', loopOn: '已开启循环', loopOff: '已关闭循环', fwd: '前进', back: '后退', sec: '秒', ytSection: 'YouTube 净化', ytAdSkip: '自动跳过广告', ytAdSkipDesc: '监视并点击跳过按钮', ytAutoQuality: '最高画质', ytAutoQualityDesc: '自动切换到最高清晰度', ytBgPlay: '后台播放', ytBgPlayDesc: '切到后台不暂停', ytSponsor: '跳过赞助段', ytSponsorDesc: 'SponsorBlock 赞助/片头', ytAdSkipped: '已跳过广告', ytQualitySet: '画质已设为最高', ytSponsorSkipped: '已跳过赞助段', ytSponsorFail: 'SponsorBlock 暂不可用', on: '已开启', off: '已关闭' },
        en: { name: 'Video Enhance', noVideo: 'No video detected', speed: 'Playback Speed', speedSet: 'Speed: ', features: 'Features', pip: 'PiP', loop: 'Loop', back10: 'Back 10s', fwd10: 'Forward 10s', pipOn: 'PiP enabled', pipOff: 'PiP disabled', pipUnavail: 'PiP unavailable', loopOn: 'Loop enabled', loopOff: 'Loop disabled', fwd: 'Forward ', back: 'Back ', sec: 's', ytSection: 'YouTube Cleanup', ytAdSkip: 'Auto-skip ads', ytAdSkipDesc: 'Watch and click skip buttons', ytAutoQuality: 'Max quality', ytAutoQualityDesc: 'Auto-select top resolution', ytBgPlay: 'Background play', ytBgPlayDesc: 'Keep audio when backgrounded', ytSponsor: 'Skip sponsors', ytSponsorDesc: 'SponsorBlock intro/sponsor', ytAdSkipped: 'Ad skipped', ytQualitySet: 'Quality set to max', ytSponsorSkipped: 'Sponsor segment skipped', ytSponsorFail: 'SponsorBlock unavailable', on: 'On', off: 'Off' },
        ar: { name: 'تحسين الفيديو', noVideo: 'لم يتم الكشف عن فيديو', speed: 'سرعة التشغيل', speedSet: 'السرعة: ', features: 'الميزات', pip: 'صورة داخل صورة', loop: 'تكرار', back10: 'رجوع 10ث', fwd10: 'تقديم 10ث', pipOn: 'تم تفعيل PiP', pipOff: 'تم إيقاف PiP', pipUnavail: 'PiP غير متاح', loopOn: 'تم تفعيل التكرار', loopOff: 'تم إيقاف التكرار', fwd: 'تقديم ', back: 'رجوع ', sec: 'ث', ytSection: 'تنظيف يوتيوب', ytAdSkip: 'تخطي الإعلانات تلقائياً', ytAdSkipDesc: 'مراقبة والنقر على تخطي', ytAutoQuality: 'أعلى جودة', ytAutoQualityDesc: 'اختيار أعلى دقة تلقائياً', ytBgPlay: 'التشغيل في الخلفية', ytBgPlayDesc: 'إبقاء الصوت في الخلفية', ytSponsor: 'تخطي الرعاة', ytSponsorDesc: 'SponsorBlock مقدمة/رعاية', ytAdSkipped: 'تم تخطي الإعلان', ytQualitySet: 'تم ضبط الجودة على الأعلى', ytSponsorSkipped: 'تم تخطي مقطع الراعي', ytSponsorFail: 'SponsorBlock غير متاح', on: 'مفعل', off: 'متوقف' }
    };
    const T = I18N[LANG] || I18N.en;

    const MODULE = { id: (typeof __MODULE_INFO__ !== 'undefined' ? __MODULE_INFO__.id : 'video-enhancer'), name: T.name, icon: '🎬', color: '#8b5cf6' };
    let currentSpeed = 1.0;
    const speeds = [0.5, 0.75, 1.0, 1.25, 1.5, 2.0, 3.0];

    const host = location.hostname;
    const isYouTube = host.includes('youtube.com') || host.includes('youtu.be');
    const YT_KEY = 'wta_venh_yt';
    let ytCfg = { adSkip: true, autoQuality: true, bgPlay: true, sponsor: false };
    try { const saved = localStorage.getItem(YT_KEY); if (saved) ytCfg = Object.assign(ytCfg, JSON.parse(saved)); } catch(e) {}

    function getVideo() { return document.querySelector('video'); }

    function setSpeed(speed) {
        const v = getVideo();
        if (v) { v.playbackRate = speed; currentSpeed = speed; __WTA_MODULE_UI__.toast(T.speedSet + speed + 'x'); }
    }

    function togglePiP() {
        const v = getVideo();
        if (!v) return;
        if (document.pictureInPictureElement) { document.exitPictureInPicture(); __WTA_MODULE_UI__.toast(T.pipOff); }
        else { v.requestPictureInPicture().then(() => __WTA_MODULE_UI__.toast(T.pipOn)).catch(() => __WTA_MODULE_UI__.toast(T.pipUnavail)); }
    }

    function saveYt() { try { localStorage.setItem(YT_KEY, JSON.stringify(ytCfg)); } catch(e) {} }

    let ytAdObserver = null;
    let ytQualityDone = false;
    let ytSponsorTimer = null;
    let ytSponsorSegments = [];
    let ytSponsorVideoId = null;

    function clickAdSkip() {
        const btn = document.querySelector('.ytp-ad-skip-button, .ytp-ad-skip-button-modern, [id^="skip-button"]');
        if (btn) { btn.click(); __WTA_MODULE_UI__.toast(T.ytAdSkipped); return true; }
        return false;
    }

    function selectMaxQuality() {
        try {
            const settingsBtn = document.querySelector('.ytp-settings-button');
            if (!settingsBtn) return false;
            settingsBtn.click();
            const menuItems = document.querySelectorAll('.ytp-menuitem');
            let qualityItem = null;
            menuItems.forEach(function(m) { if (m.textContent && m.textContent.toLowerCase().indexOf('quality') >= 0) qualityItem = m; });
            if (!qualityItem) { document.body.click(); return false; }
            qualityItem.click();
            const options = document.querySelectorAll('.ytp-quality-menu .ytp-menuitem, .ytp-panel-menu .ytp-menuitem');
            let picked = false;
            options.forEach(function(o, i) { if (!picked && i === 0) { o.click(); picked = true; } });
            if (!picked) document.body.click();
            return picked;
        } catch(e) { return false; }
    }

    function fetchSponsorSegments(videoId) {
        if (!ytCfg.sponsor) return;
        if (ytSponsorVideoId === videoId && ytSponsorSegments.length) return;
        ytSponsorVideoId = videoId;
        ytSponsorSegments = [];
        const url = 'https://sponsor.ajay.app/api/skipSegments?videoID=' + encodeURIComponent(videoId) + '&categories=["sponsor","intro","outro","selfpromo"]';
        fetch(url).then(function(r) { return r.status === 200 ? r.json() : []; }).then(function(segs) {
            ytSponsorSegments = (segs || []).map(function(s) { return { start: s.segment[0], end: s.segment[1] }; });
        }).catch(function() { ytSponsorSegments = []; });
    }

    function getYouTubeVideoId() {
        const url = location.href;
        const m = url.match(/[?&]v=([a-zA-Z0-9_-]{11})/) || url.match(/youtu\.be\/([a-zA-Z0-9_-]{11})/);
        return m ? m[1] : null;
    }

    function checkSponsor() {
        if (!ytCfg.sponsor || !ytSponsorSegments.length) return;
        const v = getVideo();
        if (!v) return;
        const t = v.currentTime;
        for (var i = 0; i < ytSponsorSegments.length; i++) {
            var s = ytSponsorSegments[i];
            if (t >= s.start && t < s.end - 0.3) { v.currentTime = s.end; __WTA_MODULE_UI__.toast(T.ytSponsorSkipped); break; }
        }
    }

    function startYouTubeWatchers() {
        if (!isYouTube || ytAdObserver) return;

        ytAdObserver = new MutationObserver(function() {
            if (ytCfg.adSkip) clickAdSkip();
            if (ytCfg.autoQuality && !ytQualityDone) {
                const v = getVideo();
                if (v && v.readyState >= 2) { if (selectMaxQuality()) { ytQualityDone = true; __WTA_MODULE_UI__.toast(T.ytQualitySet); } }
            }
        });
        ytAdObserver.observe(document.body, { childList: true, subtree: true });

        if (ytCfg.sponsor) {
            const vid = getYouTubeVideoId();
            if (vid) fetchSponsorSegments(vid);
            ytSponsorTimer = setInterval(checkSponsor, 1000);
        }

        document.addEventListener('visibilitychange', function() {
            if (ytCfg.bgPlay && document.hidden) {
                const v = getVideo();
                if (v && v.paused) v.play().catch(function(){});
            }
        });
    }

    function stopYouTubeWatchers() {
        if (ytAdObserver) { ytAdObserver.disconnect(); ytAdObserver = null; }
        if (ytSponsorTimer) { clearInterval(ytSponsorTimer); ytSponsorTimer = null; }
        ytQualityDone = false;
    }

    function restartYtWatchers() {
        stopYouTubeWatchers();
        if (ytCfg.adSkip || ytCfg.autoQuality || ytCfg.bgPlay || ytCfg.sponsor) startYouTubeWatchers();
    }

    function ytToggleRow(action, label, desc, enabled) {
        return '<div class="wta-video-yt-toggle">' +
            '<div class="wta-video-yt-toggle-info">' +
            '<div class="wta-video-yt-toggle-title">' + label +
            '<span class="wta-video-yt-chip ' + (enabled ? 'wta-video-yt-chip-on' : 'wta-video-yt-chip-off') + '">' + (enabled ? T.on : T.off) + '</span></div>' +
            '<div class="wta-video-yt-toggle-desc">' + desc + '</div></div>' +
            '<button class="wta-video-feature-btn" style="padding:8px 16px;flex-direction:row" data-wta-action="' + action + '">' +
            '<span style="font-size:16px">' + (enabled ? '✓' : '○') + '</span></button></div>';
    }

    function getPanelHtml() {
        const v = getVideo();
        const html = '<div class="wta-video-panel">' +
            '<div class="wta-video-section"><div class="wta-video-section-title">' + T.speed + '</div>' +
            '<div class="wta-video-speed-grid">' +
            speeds.map(function(s) { return '<button class="wta-video-speed-btn' + (currentSpeed === s ? ' wta-video-speed-active' : '') + '" data-wta-action="setSpeed" data-wta-arg="' + s + '">' + s + 'x</button>'; }).join('') +
            '</div></div>';

        const featuresHtml = '<div class="wta-video-section"><div class="wta-video-section-title">' + T.features + '</div>' +
            '<div class="wta-video-feature-grid">' +
            '<button class="wta-video-feature-btn" data-wta-action="togglePiP"><span class="wta-video-feature-icon">📺</span>' + T.pip + '</button>' +
            '<button class="wta-video-feature-btn" data-wta-action="toggleLoop"><span class="wta-video-feature-icon">🔁</span>' + T.loop + '</button>' +
            '<button class="wta-video-feature-btn" data-wta-action="skipBack"><span class="wta-video-feature-icon">⏪</span>' + T.back10 + '</button>' +
            '<button class="wta-video-feature-btn" data-wta-action="skipFwd"><span class="wta-video-feature-icon">⏩</span>' + T.fwd10 + '</button>' +
            '</div></div>';

        if (!v && !isYouTube) return '<div class="wta-video-empty"><div class="wta-video-empty-icon">🎬</div><div>' + T.noVideo + '</div></div>';
        if (!isYouTube) return html + featuresHtml + '</div>';

        return html + featuresHtml +
            '<div class="wta-video-section"><div class="wta-video-section-title">▶️ ' + T.ytSection + '</div>' +
            ytToggleRow('ytAdSkip', T.ytAdSkip, T.ytAdSkipDesc, ytCfg.adSkip) +
            ytToggleRow('ytAutoQuality', T.ytAutoQuality, T.ytAutoQualityDesc, ytCfg.autoQuality) +
            ytToggleRow('ytBgPlay', T.ytBgPlay, T.ytBgPlayDesc, ytCfg.bgPlay) +
            ytToggleRow('ytSponsor', T.ytSponsor, T.ytSponsorDesc, ytCfg.sponsor) +
            '</div></div>';
    }

    window.__wta_module_action_setSpeed = function(s) { setSpeed(parseFloat(s)); };
    window.__wta_module_action_togglePiP = togglePiP;
    window.__wta_module_action_toggleLoop = function() { var v = getVideo(); if (v) { v.loop = !v.loop; __WTA_MODULE_UI__.toast(v.loop ? T.loopOn : T.loopOff); } };
    window.__wta_module_action_skipBack = function() { var v = getVideo(); if (v) { v.currentTime -= 10; __WTA_MODULE_UI__.toast(T.back + '10' + T.sec); } };
    window.__wta_module_action_skipFwd = function() { var v = getVideo(); if (v) { v.currentTime += 10; __WTA_MODULE_UI__.toast(T.fwd + '10' + T.sec); } };
    window.__wta_module_action_ytAdSkip = function() { ytCfg.adSkip = !ytCfg.adSkip; saveYt(); restartYtWatchers(); };
    window.__wta_module_action_ytAutoQuality = function() { ytCfg.autoQuality = !ytCfg.autoQuality; saveYt(); restartYtWatchers(); };
    window.__wta_module_action_ytBgPlay = function() { ytCfg.bgPlay = !ytCfg.bgPlay; saveYt(); restartYtWatchers(); };
    window.__wta_module_action_ytSponsor = function() {
        ytCfg.sponsor = !ytCfg.sponsor; saveYt();
        if (ytCfg.sponsor) {
            const vid = getYouTubeVideoId();
            if (vid) fetchSponsorSegments(vid);
            if (!ytSponsorTimer) ytSponsorTimer = setInterval(checkSponsor, 1000);
        }
    };

    function register() {
        if (typeof __WTA_MODULE_UI__ === 'undefined') { setTimeout(register, 100); return; }
        __WTA_MODULE_UI__.register({ ...MODULE, uiConfig: (typeof __MODULE_UI_CONFIG__ !== 'undefined' ? __MODULE_UI_CONFIG__ : undefined), runMode: (typeof __MODULE_RUN_MODE__ !== 'undefined' ? __MODULE_RUN_MODE__ : 'INTERACTIVE'), onAction: c => c.innerHTML = getPanelHtml() });
        if (isYouTube) {
            if (ytCfg.adSkip || ytCfg.autoQuality || ytCfg.bgPlay || ytCfg.sponsor) {
                setTimeout(startYouTubeWatchers, 800);
            }
            let lastUrl = location.href;
            setInterval(function() { if (location.href !== lastUrl) { lastUrl = location.href; ytQualityDone = false; if (ytCfg.sponsor) { const vid = getYouTubeVideoId(); if (vid) fetchSponsorSegments(vid); } } }, 1500);
        }
    }
    document.readyState === 'loading' ? document.addEventListener('DOMContentLoaded', register) : register();
})();
"""

    private const val WEB_ANALYZER_CSS = """.wta-analyzer-panel{padding:4px}
.wta-analyzer-section{margin-bottom:20px}
.wta-analyzer-section-title{font-size:14px;color:var(--wta-on-surface-variant,#6b7280);margin-bottom:12px}
.wta-analyzer-info-row{display:flex;justify-content:space-between;padding:8px 0;border-bottom:1px solid var(--wta-outline,rgba(0,0,0,.06))}
.wta-analyzer-label{color:var(--wta-on-surface-variant,#6b7280);font-size:13px}
.wta-analyzer-value{color:var(--wta-on-surface,#1f2937);font-weight:500;font-size:13px;max-width:60%;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;text-align:right}
.wta-analyzer-perf-grid{display:grid;grid-template-columns:1fr 1fr;gap:8px}
.wta-analyzer-perf-card{background:var(--wta-surface-dim,#ecfdf5);padding:12px;border-radius:8px;text-align:center}
.wta-analyzer-perf-num{font-size:20px;font-weight:600;color:#059669}
.wta-analyzer-perf-label{font-size:11px;color:var(--wta-on-surface-variant,#6b7280)}
.wta-analyzer-stat-grid{display:grid;grid-template-columns:repeat(4,1fr);gap:8px}
.wta-analyzer-stat{background:var(--wta-surface-dim,#f9fafb);padding:10px;border-radius:8px;text-align:center}
.wta-analyzer-stat-icon{font-size:16px}
.wta-analyzer-stat-num{font-size:16px;font-weight:600;color:var(--wta-on-surface,#1f2937)}
.wta-analyzer-stat-label{font-size:10px;color:var(--wta-on-surface-variant,#9ca3af)}"""

    private const val WEB_ANALYZER_CODE = """
(function() {
    'use strict';

    // 多语言支持
    const LANG = (navigator.language || 'zh').toLowerCase().startsWith('ar') ? 'ar' :
                 (navigator.language || 'zh').toLowerCase().startsWith('zh') ? 'zh' : 'en';
    const I18N = {
        zh: { name: '网页分析', pageInfo: '页面信息', title: '标题', domain: '域名', perf: '性能数据', loadTime: '加载时间(ms)', domReady: 'DOM就绪(ms)', stats: '元素统计', scripts: '脚本', styles: '样式', images: '图片', links: '链接', forms: '表单', iframes: '内嵌框架', videos: '视频' },
        en: { name: 'Web Analyzer', pageInfo: 'Page Info', title: 'Title', domain: 'Domain', perf: 'Performance', loadTime: 'Load Time(ms)', domReady: 'DOM Ready(ms)', stats: 'Element Stats', scripts: 'Scripts', styles: 'Styles', images: 'Images', links: 'Links', forms: 'Forms', iframes: 'Iframes', videos: 'Videos' },
        ar: { name: 'محلل الويب', pageInfo: 'معلومات الصفحة', title: 'العنوان', domain: 'النطاق', perf: 'الأداء', loadTime: 'وقت التحميل(ms)', domReady: 'DOM جاهز(ms)', stats: 'إحصائيات العناصر', scripts: 'السكريبتات', styles: 'الأنماط', images: 'الصور', links: 'الروابط', forms: 'النماذج', iframes: 'الإطارات', videos: 'الفيديوهات' }
    };
    const T = I18N[LANG] || I18N.en;

    const MODULE = { id: (typeof __MODULE_INFO__ !== 'undefined' ? __MODULE_INFO__.id : 'web-analyzer'), name: T.name, icon: '🔍', color: '#059669' };

    function getPageInfo() {
        const scripts = document.querySelectorAll('script[src]').length;
        const styles = document.querySelectorAll('link[rel="stylesheet"]').length;
        const images = document.querySelectorAll('img').length;
        const links = document.querySelectorAll('a[href]').length;
        const forms = document.querySelectorAll('form').length;
        const iframes = document.querySelectorAll('iframe').length;
        const videos = document.querySelectorAll('video').length;

        return { scripts, styles, images, links, forms, iframes, videos };
    }

    function getPanelHtml() {
        const info = getPageInfo();
        const perf = performance.timing;
        const loadTime = perf.loadEventEnd - perf.navigationStart;
        const domReady = perf.domContentLoadedEventEnd - perf.navigationStart;

        return '<div class="wta-analyzer-panel">' +
            '<div class="wta-analyzer-section"><div class="wta-analyzer-section-title">' + T.pageInfo + '</div>' +
            '<div class="wta-analyzer-info-row"><span class="wta-analyzer-label">' + T.title + '</span><span class="wta-analyzer-value">' + (document.title || '-') + '</span></div>' +
            '<div class="wta-analyzer-info-row"><span class="wta-analyzer-label">' + T.domain + '</span><span class="wta-analyzer-value">' + location.hostname + '</span></div></div>' +
            '<div class="wta-analyzer-section"><div class="wta-analyzer-section-title">' + T.perf + '</div>' +
            '<div class="wta-analyzer-perf-grid">' +
            '<div class="wta-analyzer-perf-card"><div class="wta-analyzer-perf-num">' + (loadTime > 0 ? loadTime : '-') + '</div><div class="wta-analyzer-perf-label">' + T.loadTime + '</div></div>' +
            '<div class="wta-analyzer-perf-card" style="background:var(--wta-surface-dim,#eff6ff)"><div class="wta-analyzer-perf-num" style="color:#3b82f6">' + (domReady > 0 ? domReady : '-') + '</div><div class="wta-analyzer-perf-label">' + T.domReady + '</div></div></div></div>' +
            '<div class="wta-analyzer-section"><div class="wta-analyzer-section-title">' + T.stats + '</div>' +
            '<div class="wta-analyzer-stat-grid">' +
            [['📜', info.scripts, T.scripts], ['🎨', info.styles, T.styles], ['🖼️', info.images, T.images], ['🔗', info.links, T.links],
             ['📝', info.forms, T.forms], ['📺', info.iframes, T.iframes], ['🎬', info.videos, T.videos]].map(function(s) {
                return '<div class="wta-analyzer-stat"><div class="wta-analyzer-stat-icon">' + s[0] + '</div><div class="wta-analyzer-stat-num">' + s[1] + '</div><div class="wta-analyzer-stat-label">' + s[2] + '</div></div>';
            }).join('') +
            '</div></div></div>';
    }

    function register() {
        if (typeof __WTA_MODULE_UI__ === 'undefined') { setTimeout(register, 100); return; }
        __WTA_MODULE_UI__.register({ ...MODULE, uiConfig: (typeof __MODULE_UI_CONFIG__ !== 'undefined' ? __MODULE_UI_CONFIG__ : undefined), runMode: (typeof __MODULE_RUN_MODE__ !== 'undefined' ? __MODULE_RUN_MODE__ : 'INTERACTIVE'), onAction: c => c.innerHTML = getPanelHtml() });
    }
    document.readyState === 'loading' ? document.addEventListener('DOMContentLoaded', register) : register();
})();
"""

    private const val FIND_IN_PAGE_PANEL_HTML = """<div class="wta-mod-panel wta-find-panel">
<div class="wta-find-bar">
<span class="wta-find-icon">🔎</span>
<input id="wta-find-query" class="wta-find-input" placeholder="..." data-wta-action="findInput">
<button class="wta-find-search-btn" data-wta-action="findSearch">⌕</button>
</div>
<div class="wta-find-nav">
<div id="wta-find-status" class="wta-find-status"></div>
<div class="wta-find-nav-btns">
<button class="wta-find-nav-btn" data-wta-action="findPrev">↑</button>
<button class="wta-find-nav-btn" data-wta-action="findNext">↓</button>
<button class="wta-find-clear-btn" data-wta-action="findClear"></button>
</div>
</div>
</div>"""

    private const val FIND_IN_PAGE_CSS = """.wta-find-panel{display:flex;flex-direction:column;gap:12px;padding:4px}
.wta-find-bar{display:flex;align-items:center;gap:8px;background:var(--wta-surface-dim,#f8fafc);border:1px solid var(--wta-outline,#e5e7eb);border-radius:12px;padding:8px 10px}
.wta-find-icon{font-size:20px}
.wta-find-input{flex:1;min-width:0;border:none;outline:none;background:transparent;color:var(--wta-on-surface,#111827);font-size:15px;line-height:24px}
.wta-find-search-btn{background:none;border:none;font-size:18px;cursor:pointer;color:var(--wta-on-surface-variant,#6b7280);padding:4px}
.wta-find-nav{display:flex;align-items:center;justify-content:space-between}
.wta-find-status{font-size:13px;color:var(--wta-on-surface-variant,#9ca3af)}
.wta-find-nav-btns{display:flex;gap:6px}
.wta-find-nav-btn,.wta-find-clear-btn{padding:6px 12px;border-radius:8px;border:1px solid var(--wta-outline,#e5e7eb);background:var(--wta-surface-dim,#f3f4f6);font-size:13px;cursor:pointer;color:var(--wta-on-surface,#374151);transition:all .2s}
.wta-find-nav-btn:active,.wta-find-clear-btn:active{background:var(--wta-accent-soft,rgba(99,102,241,.1))}"""

    private const val FIND_IN_PAGE_CODE = """
(function() {
    'use strict';

    const LANG = (navigator.language || 'zh').toLowerCase().startsWith('ar') ? 'ar' :
                 (navigator.language || 'zh').toLowerCase().startsWith('zh') ? 'zh' : 'en';
    const I18N = {
        zh: { name: '页内查找', placeholder: '在当前页面查找', prev: '上一个', next: '下一个', clear: '清除', noMatch: '未找到匹配项', nativeUnavailable: '当前内核不支持原生页内查找', enterKeyword: '请输入关键词', searching: '正在查找...', tip: '使用 WebView 原生查找，高亮结果并自动定位', matchCount: '{0} / {1}' },
        en: { name: 'Find in page', placeholder: 'Find in current page', prev: 'Previous', next: 'Next', clear: 'Clear', noMatch: 'No matches found', nativeUnavailable: 'Native find is unavailable in this engine', enterKeyword: 'Enter a keyword', searching: 'Searching...', tip: 'Uses native WebView search to highlight and jump between matches', matchCount: '{0} / {1}' },
        ar: { name: 'بحث في الصفحة', placeholder: 'ابحث في الصفحة الحالية', prev: 'السابق', next: 'التالي', clear: 'مسح', noMatch: 'لا توجد نتائج', nativeUnavailable: 'البحث الأصلي غير متاح في هذا المحرك', enterKeyword: 'أدخل كلمة البحث', searching: 'جاري البحث...', tip: 'يستخدم بحث WebView الأصلي لتمييز النتائج والتنقل بينها', matchCount: '{0} / {1}' }
    };
    const T = I18N[LANG] || I18N.en;
    const MODULE = { id: (typeof __MODULE_INFO__ !== 'undefined' ? __MODULE_INFO__.id : 'find-in-page'), name: T.name, icon: '🔎', color: '#2563eb' };

    let currentQuery = '';
    let currentState = { supported: false, activeMatchOrdinal: -1, numberOfMatches: 0, doneCounting: true, displayIndex: 0 };

    function esc(value) {
        return String(value || '').replace(/[&<>"']/g, function(ch) {
            return ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[ch];
        });
    }

    function parseState(raw) {
        if (!raw) return currentState;
        if (typeof raw === 'object') return raw;
        try { return JSON.parse(raw); } catch(e) { return currentState; }
    }

    function setState(nextState) {
        currentState = Object.assign({}, currentState, parseState(nextState));
        refreshPanel();
    }

    function callNative(name) {
        if (typeof NativeBridge === 'undefined' || typeof NativeBridge[name] !== 'function') return null;
        try {
            if (name === 'findInPage') return NativeBridge.findInPage(currentQuery);
            if (name === 'findNextInPage') return NativeBridge.findNextInPage(arguments[1] !== false);
            if (name === 'clearFindInPage') return NativeBridge.clearFindInPage();
        } catch(e) {
            console.warn('[Find in page] Native call failed:', e);
        }
        return null;
    }

    function fallbackFind(forward) {
        if (!currentQuery) return false;
        try {
            if (typeof window.find === 'function') {
                return window.find(currentQuery, false, forward === false, true, false, false, false);
            }
        } catch(e) {
            console.warn('[Find in page] window.find fallback failed:', e);
        }
        return false;
    }

    function doSearch() {
        currentQuery = (document.getElementById('wta-find-query')?.value || '').trim();
        if (!currentQuery) {
            __WTA_MODULE_UI__.toast(T.enterKeyword);
            return;
        }

        const nativeState = callNative('findInPage');
        if (nativeState) {
            setState(nativeState);
            setTimeout(function() {
                if (currentState.doneCounting && currentState.numberOfMatches === 0) {
                    __WTA_MODULE_UI__.toast(T.noMatch);
                }
            }, 500);
            return;
        }

        currentState = { supported: false, activeMatchOrdinal: -1, numberOfMatches: fallbackFind(true) ? 1 : 0, doneCounting: true, displayIndex: 0 };
        __WTA_MODULE_UI__.toast(currentState.numberOfMatches ? T.nativeUnavailable : T.noMatch);
        refreshPanel();
    }

    function go(forward) {
        if (!currentQuery) {
            doSearch();
            return;
        }
        const nativeState = callNative('findNextInPage', forward);
        if (nativeState) {
            setState(nativeState);
        } else {
            fallbackFind(forward);
        }
    }

    function clearSearch() {
        currentQuery = '';
        const input = document.getElementById('wta-find-query');
        if (input) input.value = '';
        const nativeState = callNative('clearFindInPage');
        currentState = parseState(nativeState) || { supported: false, activeMatchOrdinal: -1, numberOfMatches: 0, doneCounting: true, displayIndex: 0 };
        refreshPanel();
    }

    function statusText() {
        if (!currentQuery) return T.tip;
        if (!currentState.doneCounting) return T.searching;
        if (!currentState.numberOfMatches) return T.noMatch;
        return T.matchCount
            .replace('{0}', currentState.displayIndex || Math.max(0, currentState.activeMatchOrdinal + 1))
            .replace('{1}', currentState.numberOfMatches);
    }

    function updatePanelUI() {
        var status = document.getElementById('wta-find-status');
        if (status) { status.textContent = statusText(); status.style.color = currentState.numberOfMatches ? 'var(--wta-primary,#2563eb)' : 'var(--wta-on-surface-variant,#9ca3af)'; }
        var input = document.getElementById('wta-find-query');
        if (input && !input.value && currentQuery) input.value = currentQuery;
        var clearBtn = document.querySelector('.wta-find-clear-btn');
        if (clearBtn) clearBtn.textContent = T.clear;
    }

    function refreshPanel() {
        updatePanelUI();
        if (typeof __WTA_MODULE_UI__ !== 'undefined') {
            setTimeout(function() {
                var input = document.getElementById('wta-find-query');
                if (input) { input.focus(); input.setSelectionRange(input.value.length, input.value.length); }
            }, 50);
        }
    }

    window.__wta_module_action_findInput = function() {};
    window.__wta_module_action_findSearch = doSearch;
    window.__wta_module_action_findNext = function() { go(true); };
    window.__wta_module_action_findPrev = function() { go(false); };
    window.__wta_module_action_findClear = clearSearch;
    window.__wtaFindInPageNativeUpdate = setState;

    function register() {
        if (typeof __WTA_MODULE_UI__ === 'undefined') { setTimeout(register, 100); return; }
        __WTA_MODULE_UI__.register({ ...MODULE, uiConfig: (typeof __MODULE_UI_CONFIG__ !== 'undefined' ? __MODULE_UI_CONFIG__ : undefined), runMode: (typeof __MODULE_RUN_MODE__ !== 'undefined' ? __MODULE_RUN_MODE__ : 'INTERACTIVE'), onAction: function() { updatePanelUI(); setTimeout(function(){ var input = document.getElementById('wta-find-query'); if (input) input.focus(); }, 60); } });
    }

    document.readyState === 'loading' ? document.addEventListener('DOMContentLoaded', register) : register();
})();
"""

    private const val DARK_MODE_PANEL_HTML = """<div class="wta-mod-panel wta-dark-panel">
<div class="wta-dark-icon" id="wta-dark-icon">☀️</div>
<div class="wta-dark-status" id="wta-dark-status"></div>
<div class="wta-mod-desc" id="wta-dark-desc"></div>
<button class="wta-mod-btn" data-wta-action="toggleDark" id="wta-dark-btn"></button>
</div>"""

    private const val DARK_MODE_CSS = """.wta-dark-panel{text-align:center;padding:20px}
.wta-dark-icon{font-size:64px;margin-bottom:20px}
.wta-dark-status{font-size:18px;font-weight:600;color:var(--wta-on-surface,#1f2937);margin-bottom:8px}
.wta-dark-panel .wta-mod-desc{font-size:13px;color:var(--wta-on-surface-variant,#9ca3af);margin-bottom:24px}
.wta-mod-btn{width:100%;padding:14px;border-radius:12px;border:none;font-size:15px;font-weight:500;cursor:pointer;transition:all .2s}
.wta-mod-btn-primary{background:linear-gradient(135deg,var(--wta-primary,#6366f1),#8b5cf6);color:var(--wta-on-primary,#fff)}
.wta-mod-btn-secondary{background:var(--wta-surface-dim,#f3f4f6);color:var(--wta-on-surface,#374151)}"""

    private const val DARK_MODE_CODE = """
(function() {
    'use strict';

    // 多语言支持
    const LANG = (navigator.language || 'zh').toLowerCase().startsWith('ar') ? 'ar' :
                 (navigator.language || 'zh').toLowerCase().startsWith('zh') ? 'zh' : 'en';
    const I18N = {
        zh: { name: '深色模式', enabled: '已开启深色模式', disabled: '已关闭深色模式', statusOn: '深色模式已开启', statusOff: '深色模式已关闭', desc: '智能反色，保护眼睛', turnOff: '关闭深色模式', turnOn: '开启深色模式' },
        en: { name: 'Dark Mode', enabled: 'Dark mode enabled', disabled: 'Dark mode disabled', statusOn: 'Dark Mode On', statusOff: 'Dark Mode Off', desc: 'Smart inversion, protect your eyes', turnOff: 'Turn Off Dark Mode', turnOn: 'Turn On Dark Mode' },
        ar: { name: 'الوضع الداكن', enabled: 'تم تفعيل الوضع الداكن', disabled: 'تم إيقاف الوضع الداكن', statusOn: 'الوضع الداكن مفعل', statusOff: 'الوضع الداكن موقف', desc: 'عكس ذكي، حماية العين', turnOff: 'إيقاف الوضع الداكن', turnOn: 'تفعيل الوضع الداكن' }
    };
    const T = I18N[LANG] || I18N.en;

    const MODULE = { id: (typeof __MODULE_INFO__ !== 'undefined' ? __MODULE_INFO__.id : 'dark-mode'), name: T.name, icon: '🌙', color: '#6366f1' };
    const STORAGE_KEY = 'wta_dark_mode';
    let enabled = false;
    try { enabled = localStorage.getItem(STORAGE_KEY) === 'true'; } catch(e) { /* localStorage unavailable */ }
    let styleEl = null;

    const darkCSS = 'html,html body{filter:invert(1) hue-rotate(180deg)!important;-webkit-filter:invert(1) hue-rotate(180deg)!important;background:#111!important}' +
        'img,video,picture,canvas,svg,iframe,[style*="background-image"],embed,object{filter:invert(1) hue-rotate(180deg)!important;-webkit-filter:invert(1) hue-rotate(180deg)!important}';

    function toggle() {
        enabled = !enabled;
        try { localStorage.setItem(STORAGE_KEY, enabled); } catch(e) { /* localStorage unavailable */ }
        apply();
        if (typeof __WTA_MODULE_UI__ !== 'undefined') __WTA_MODULE_UI__.toast(enabled ? T.enabled : T.disabled);
    }

    function getStyleParent() {
        return document.head || document.documentElement || document.querySelector('head');
    }

    function apply() {
        if (enabled) {
            if (!styleEl || !styleEl.parentNode) {
                styleEl = document.createElement('style');
                styleEl.id = 'wta-dark-mode';
                styleEl.setAttribute('type', 'text/css');
                var parent = getStyleParent();
                if (parent) {
                    parent.appendChild(styleEl);
                } else {
                    // DOCUMENT_START: head doesn't exist yet, retry when available
                    var observer = new MutationObserver(function(mutations, obs) {
                        var p = getStyleParent();
                        if (p) { p.appendChild(styleEl); styleEl.textContent = darkCSS; obs.disconnect(); }
                    });
                    var observerTarget = document.documentElement || document.body;
                    if (observerTarget instanceof Node) {
                        observer.observe(observerTarget, { childList: true, subtree: true });
                    }
                    return;
                }
            }
            styleEl.textContent = darkCSS;
        } else if (styleEl) {
            styleEl.textContent = '';
        }
    }

    function updatePanelUI() {
        var icon = document.getElementById('wta-dark-icon');
        var status = document.getElementById('wta-dark-status');
        var desc = document.getElementById('wta-dark-desc');
        var btn = document.getElementById('wta-dark-btn');
        if (icon) icon.textContent = enabled ? '🌙' : '☀️';
        if (status) status.textContent = enabled ? T.statusOn : T.statusOff;
        if (desc) desc.textContent = T.desc;
        if (btn) {
            btn.textContent = enabled ? ('☀️ ' + T.turnOff) : ('🌙 ' + T.turnOn);
            btn.className = 'wta-mod-btn ' + (enabled ? 'wta-mod-btn-secondary' : 'wta-mod-btn-primary');
        }
    }

    window.__wta_module_action_toggleDark = function() { toggle(); updatePanelUI(); };

    function register() {
        if (typeof __WTA_MODULE_UI__ === 'undefined') { setTimeout(register, 100); return; }
        __WTA_MODULE_UI__.register({ ...MODULE, uiConfig: (typeof __MODULE_UI_CONFIG__ !== 'undefined' ? __MODULE_UI_CONFIG__ : undefined), runMode: (typeof __MODULE_RUN_MODE__ !== 'undefined' ? __MODULE_RUN_MODE__ : 'INTERACTIVE'), onAction: function() { updatePanelUI(); } });
    }

    apply();
    // Re-apply when DOM is ready (in case early apply failed)
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', function() { if (enabled) apply(); register(); });
    } else {
        register();
    }
})();
"""

    private const val PRIVACY_PANEL_HTML = """<div class="wta-mod-panel wta-privacy-panel">
<div class="wta-privacy-header"><div class="wta-privacy-icon">🛡️</div><div class="wta-mod-desc" id="wta-privacy-subtitle"></div></div>
<div class="wta-privacy-item" data-wta-action="privacyToggle" data-wta-arg="tracking" id="wta-priv-tracking"><span class="wta-privacy-item-icon">🚫</span><div class="wta-privacy-item-text"><div class="wta-privacy-item-name" id="wta-priv-tracking-name"></div><div class="wta-privacy-item-desc" id="wta-priv-tracking-desc"></div></div><div class="wta-toggle"><div class="wta-toggle-track"></div><div class="wta-toggle-thumb"></div></div></div>
<div class="wta-privacy-item" data-wta-action="privacyToggle" data-wta-arg="fingerprint" id="wta-priv-fingerprint"><span class="wta-privacy-item-icon">🎭</span><div class="wta-privacy-item-text"><div class="wta-privacy-item-name" id="wta-priv-fingerprint-name"></div><div class="wta-privacy-item-desc" id="wta-priv-fingerprint-desc"></div></div><div class="wta-toggle"><div class="wta-toggle-track"></div><div class="wta-toggle-thumb"></div></div></div>
<div class="wta-privacy-item" data-wta-action="privacyToggle" data-wta-arg="cookies" id="wta-priv-cookies"><span class="wta-privacy-item-icon">🍪</span><div class="wta-privacy-item-text"><div class="wta-privacy-item-name" id="wta-priv-cookies-name"></div><div class="wta-privacy-item-desc" id="wta-priv-cookies-desc"></div></div><div class="wta-toggle"><div class="wta-toggle-track"></div><div class="wta-toggle-thumb"></div></div></div>
</div>"""

    private const val PRIVACY_CSS = """.wta-privacy-panel{padding:16px}
.wta-privacy-header{text-align:center;margin-bottom:16px}
.wta-privacy-icon{font-size:48px;margin-bottom:8px}
.wta-privacy-item{display:flex;align-items:center;gap:12px;padding:16px;background:var(--wta-surface-dim,#f9fafb);border-radius:12px;margin-bottom:8px;cursor:pointer;transition:background .2s}
.wta-privacy-item:active{background:var(--wta-accent-soft,rgba(99,102,241,.06))}
.wta-privacy-item-icon{font-size:24px}
.wta-privacy-item-text{flex:1}
.wta-privacy-item-name{font-weight:500;color:var(--wta-on-surface,#1f2937)}
.wta-privacy-item-desc{font-size:12px;color:var(--wta-on-surface-variant,#9ca3af)}
.wta-toggle{position:relative;width:48px;height:28px;flex-shrink:0}
.wta-toggle-track{position:absolute;cursor:pointer;top:0;left:0;right:0;bottom:0;background:var(--wta-outline,#d1d5db);border-radius:14px;transition:.3s}
.wta-toggle-thumb{position:absolute;height:24px;width:24px;left:2px;bottom:2px;background:white;border-radius:50%;transition:.3s;box-shadow:0 1px 3px rgba(0,0,0,.2)}
.wta-privacy-item[data-state="on"] .wta-toggle-track{background:#22c55e}
.wta-privacy-item[data-state="on"] .wta-toggle-thumb{left:22px}"""

    private const val PRIVACY_PROTECTION_CODE = """
(function() {
    'use strict';

    // 多语言支持
    const LANG = (navigator.language || 'zh').toLowerCase().startsWith('ar') ? 'ar' :
                 (navigator.language || 'zh').toLowerCase().startsWith('zh') ? 'zh' : 'en';
    const I18N = {
        zh: { name: '隐私保护', subtitle: '保护您的隐私安全', tracking: '阻止追踪', trackingDesc: '拦截常见追踪脚本', fingerprint: '指纹保护', fingerprintDesc: '模糊设备指纹信息', cookies: '清理Cookies', cookiesDesc: '退出时清理Cookies', enabled: '已开启', disabled: '已关闭' },
        en: { name: 'Privacy Protection', subtitle: 'Protect your privacy', tracking: 'Block Tracking', trackingDesc: 'Block common tracking scripts', fingerprint: 'Fingerprint Protection', fingerprintDesc: 'Blur device fingerprint info', cookies: 'Clear Cookies', cookiesDesc: 'Clear cookies on exit', enabled: 'Enabled', disabled: 'Disabled' },
        ar: { name: 'حماية الخصوصية', subtitle: 'حماية خصوصيتك', tracking: 'حظر التتبع', trackingDesc: 'حظر سكريبتات التتبع', fingerprint: 'حماية البصمة', fingerprintDesc: 'تمويه بصمة الجهاز', cookies: 'مسح Cookies', cookiesDesc: 'مسح Cookies عند الخروج', enabled: 'مفعل', disabled: 'موقف' }
    };
    const T = I18N[LANG] || I18N.en;

    const MODULE = { id: (typeof __MODULE_INFO__ !== 'undefined' ? __MODULE_INFO__.id : 'privacy'), name: T.name, icon: '🛡️', color: '#dc2626' };
    const STORAGE_KEY = 'wta_privacy';
    let settings = JSON.parse(localStorage.getItem(STORAGE_KEY) || '{"tracking":true,"fingerprint":true,"cookies":false}');

    function save() { localStorage.setItem(STORAGE_KEY, JSON.stringify(settings)); }

    function applyProtection() {
        if (settings.tracking) {
            const blocked = ['google-analytics.com', 'googletagmanager.com', 'facebook.net', 'doubleclick.net', 'hotjar.com'];
            const origFetch = window.fetch;
            window.fetch = function(url, opts) {
                if (blocked.some(b => url.toString().includes(b))) { console.log('[Privacy] Blocked:', url); return Promise.reject(); }
                return origFetch.apply(this, arguments);
            };
        }
        if (settings.fingerprint) {
            Object.defineProperty(navigator, 'hardwareConcurrency', { get: () => 4 });
            Object.defineProperty(navigator, 'deviceMemory', { get: () => 8 });
            Object.defineProperty(screen, 'colorDepth', { get: () => 24 });
        }
    }

    function updatePanelUI() {
        var sub = document.getElementById('wta-privacy-subtitle');
        if (sub) sub.textContent = T.subtitle;
        ['tracking', 'fingerprint', 'cookies'].forEach(function(key) {
            var el = document.getElementById('wta-priv-' + key);
            if (el) el.setAttribute('data-state', settings[key] ? 'on' : 'off');
            var nameEl = document.getElementById('wta-priv-' + key + '-name');
            if (nameEl) nameEl.textContent = T[key];
            var descEl = document.getElementById('wta-priv-' + key + '-desc');
            if (descEl) descEl.textContent = T[key + 'Desc'];
        });
    }

    window.__wta_module_action_privacyToggle = function(key) {
        settings[key] = !settings[key];
        save();
        if (typeof __WTA_MODULE_UI__ !== 'undefined') __WTA_MODULE_UI__.toast(settings[key] ? T.enabled : T.disabled);
        updatePanelUI();
    };

    function register() {
        if (typeof __WTA_MODULE_UI__ === 'undefined') { setTimeout(register, 100); return; }
        __WTA_MODULE_UI__.register({ ...MODULE, uiConfig: (typeof __MODULE_UI_CONFIG__ !== 'undefined' ? __MODULE_UI_CONFIG__ : undefined), runMode: (typeof __MODULE_RUN_MODE__ !== 'undefined' ? __MODULE_RUN_MODE__ : 'INTERACTIVE'), onAction: function() { updatePanelUI(); } });
    }

    applyProtection();
    document.readyState === 'loading' ? document.addEventListener('DOMContentLoaded', register) : register();
})();
"""

    private const val CONTENT_ENHANCER_PANEL_HTML = """<div class="wta-mod-panel">
<div class="wta-tool-grid">
<button class="wta-tool-btn" data-wta-action="enableCopy"><span class="wta-tool-icon">📋</span><span class="wta-tool-label" id="wta-tool-copy"></span></button>
<button class="wta-tool-btn" data-wta-action="copyText"><span class="wta-tool-icon">📝</span><span class="wta-tool-label" id="wta-tool-text"></span></button>
<button class="wta-tool-btn" data-wta-action="copyHtml"><span class="wta-tool-icon">📝</span><span class="wta-tool-label" id="wta-tool-html"></span></button>
<button class="wta-tool-btn" data-wta-action="toTop"><span class="wta-tool-icon">⬆️</span><span class="wta-tool-label" id="wta-tool-top"></span></button>
<button class="wta-tool-btn" data-wta-action="toBottom"><span class="wta-tool-icon">⬇️</span><span class="wta-tool-label" id="wta-tool-bottom"></span></button>
</div>
</div>"""

    private const val CONTENT_ENHANCER_CSS = """.wta-tool-grid{display:grid;grid-template-columns:1fr 1fr;gap:12px;padding:4px}
.wta-tool-btn{display:flex;flex-direction:column;align-items:center;gap:8px;padding:16px;border-radius:12px;border:1px solid var(--wta-outline,#e5e7eb);background:var(--wta-surface-dim,#f9fafb);cursor:pointer;color:var(--wta-on-surface,#374151);transition:all .2s}
.wta-tool-btn:active{background:var(--wta-accent-soft,rgba(99,102,241,.1))}
.wta-tool-icon{font-size:24px}
.wta-tool-label{font-size:13px;font-weight:500}"""

    private const val CONTENT_ENHANCER_CODE = """
(function() {
    'use strict';

    // 多语言支持
    const LANG = (navigator.language || 'zh').toLowerCase().startsWith('ar') ? 'ar' :
                 (navigator.language || 'zh').toLowerCase().startsWith('zh') ? 'zh' : 'en';
    const I18N = {
        zh: { name: '内容增强', enableCopy: '解除复制限制', copyText: '复制页面文本', copyHtml: '复制页面HTML', toTop: '回到顶部', toBottom: '滚动到底部', copyEnabled: '已解除复制限制', textCopied: '页面文本已复制', htmlCopied: '页面HTML已复制', atTop: '已回到顶部', atBottom: '已到达底部' },
        en: { name: 'Content Enhance', enableCopy: 'Enable Copy', copyText: 'Copy Page Text', copyHtml: 'Copy Page HTML', toTop: 'To Top', toBottom: 'To Bottom', copyEnabled: 'Copy restriction removed', textCopied: 'Page text copied', htmlCopied: 'Page HTML copied', atTop: 'At top', atBottom: 'At bottom' },
        ar: { name: 'تحسين المحتوى', enableCopy: 'تفعيل النسخ', copyText: 'نسخ نص الصفحة', copyHtml: 'نسخ HTML', toTop: 'إلى الأعلى', toBottom: 'إلى الأسفل', copyEnabled: 'تم إزالة قيود النسخ', textCopied: 'تم نسخ النص', htmlCopied: 'تم نسخ HTML', atTop: 'في الأعلى', atBottom: 'في الأسفل' }
    };
    const T = I18N[LANG] || I18N.en;

    const MODULE = { id: (typeof __MODULE_INFO__ !== 'undefined' ? __MODULE_INFO__.id : 'content-enhancer'), name: T.name, icon: '✨', color: '#f59e0b' };

    function enableCopy() {
        document.body.style.userSelect = 'auto';
        document.body.style.webkitUserSelect = 'auto';
        ['copy', 'cut', 'paste', 'selectstart', 'contextmenu'].forEach(e => {
            document.addEventListener(e, ev => ev.stopPropagation(), true);
        });
        const style = document.createElement('style');
        style.textContent = '*{user-select:auto!important;-webkit-user-select:auto!important}';
        document.head.appendChild(style);
        __WTA_MODULE_UI__.toast(T.copyEnabled);
    }

    function copyPageText() {
        const text = document.body.innerText;
        navigator.clipboard?.writeText(text).then(() => __WTA_MODULE_UI__.toast(T.textCopied));
    }

    function copyPageHtml() {
        const html = document.documentElement.outerHTML;
        navigator.clipboard?.writeText(html).then(() => __WTA_MODULE_UI__.toast(T.htmlCopied));
    }

    function scrollToTop() { window.scrollTo({ top: 0, behavior: 'smooth' }); __WTA_MODULE_UI__.toast(T.atTop); }
    function scrollToBottom() { window.scrollTo({ top: document.body.scrollHeight, behavior: 'smooth' }); __WTA_MODULE_UI__.toast(T.atBottom); }

    function updatePanelUI() {
        var ids = ['wta-tool-copy', 'wta-tool-text', 'wta-tool-html', 'wta-tool-top', 'wta-tool-bottom'];
        var keys = ['enableCopy', 'copyText', 'copyHtml', 'toTop', 'toBottom'];
        ids.forEach(function(id, i) { var el = document.getElementById(id); if (el) el.textContent = T[keys[i]]; });
    }

    window.__wta_module_action_enableCopy = enableCopy;
    window.__wta_module_action_copyText = copyPageText;
    window.__wta_module_action_copyHtml = copyPageHtml;
    window.__wta_module_action_toTop = scrollToTop;
    window.__wta_module_action_toBottom = scrollToBottom;

    function register() {
        if (typeof __WTA_MODULE_UI__ === 'undefined') { setTimeout(register, 100); return; }
        __WTA_MODULE_UI__.register({ ...MODULE, uiConfig: (typeof __MODULE_UI_CONFIG__ !== 'undefined' ? __MODULE_UI_CONFIG__ : undefined), runMode: (typeof __MODULE_RUN_MODE__ !== 'undefined' ? __MODULE_RUN_MODE__ : 'INTERACTIVE'), onAction: function() { updatePanelUI(); } });
    }
    document.readyState === 'loading' ? document.addEventListener('DOMContentLoaded', register) : register();
})();
"""

    private const val ELEMENT_BLOCKER_PANEL_HTML = """<div class="wta-mod-panel wta-blocker-panel">
<button class="wta-blocker-select-btn" data-wta-action="enterSelectMode"><span>👆</span><span id="wta-blocker-select-label"></span></button>
<div id="wta-blocker-list" class="wta-blocker-list"></div>
<button class="wta-blocker-clear-btn" data-wta-action="clearAllBlocks" id="wta-blocker-clear"></button>
</div>"""

    private const val ELEMENT_BLOCKER_CSS = """.wta-blocker-panel{padding:4px}
.wta-blocker-select-btn{width:100%;background:linear-gradient(135deg,#ef4444,#f87171);color:#fff;border:none;padding:14px;border-radius:12px;font-size:15px;font-weight:500;cursor:pointer;display:flex;align-items:center;justify-content:center;gap:8px;transition:opacity .2s}
.wta-blocker-select-btn:active{opacity:.8}
.wta-blocker-count{font-size:14px;color:var(--wta-on-surface-variant,#6b7280);margin:12px 0}
.wta-blocker-list{max-height:200px;overflow-y:auto}
.wta-blocker-item{display:flex;align-items:center;gap:8px;padding:10px;background:var(--wta-surface-dim,#f9fafb);border-radius:8px;margin-bottom:6px;font-size:13px;color:var(--wta-on-surface,#991b1b)}
.wta-blocker-item-selector{flex:1;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;font-family:monospace;font-size:12px}
.wta-blocker-item-btn{background:#fee2e2;border:none;cursor:pointer;font-size:14px;color:#dc2626;padding:4px 8px;border-radius:4px;transition:all .2s}
.wta-blocker-item-btn:active{background:#fecaca}
.wta-blocker-clear-btn{width:100%;padding:12px;border-radius:10px;border:1px solid var(--wta-outline,#fecaca);background:var(--wta-surface-dim,#fff5f5);color:#dc2626;font-size:14px;cursor:pointer;transition:all .2s;margin-top:12px}
.wta-blocker-clear-btn:active{background:#fef2f2}
.wta-blocker-empty{text-align:center;padding:24px;color:var(--wta-on-surface-variant,#9ca3af)}
.wta-blocker-empty-icon{font-size:32px;margin-bottom:8px}"""

    private const val ELEMENT_BLOCKER_CODE = """
(function() {
    'use strict';

    // 多语言支持
    const LANG = (navigator.language || 'zh').toLowerCase().startsWith('ar') ? 'ar' :
                 (navigator.language || 'zh').toLowerCase().startsWith('zh') ? 'zh' : 'en';
    const I18N = {
        zh: {
            name: '元素屏蔽',
            blocked: '已屏蔽元素',
            unblocked: '已取消屏蔽',
            selected: '已选中',
            dblClickToBlock: '双击屏蔽',
            selectMode: '选择模式：单击选择，双击屏蔽，按 ESC 退出',
            clearedAll: '已清除所有屏蔽',
            selectElement: '选择要屏蔽的元素',
            blockedCount: '已屏蔽 {0} 个元素',
            delete: '删除',
            clearAll: '清除所有屏蔽',
            clickToSelect: '点击上方按钮选择要屏蔽的元素'
        },
        en: {
            name: 'Element Blocker',
            blocked: 'Element blocked',
            unblocked: 'Unblocked',
            selected: 'Selected',
            dblClickToBlock: 'double-click to block',
            selectMode: 'Select mode: click to select, double-click to block, ESC to exit',
            clearedAll: 'All blocks cleared',
            selectElement: 'Select element to block',
            blockedCount: '{0} elements blocked',
            delete: 'Delete',
            clearAll: 'Clear all blocks',
            clickToSelect: 'Click the button above to select elements'
        },
        ar: {
            name: 'مانع العناصر',
            blocked: 'تم حظر العنصر',
            unblocked: 'تم إلغاء الحظر',
            selected: 'محدد',
            dblClickToBlock: 'انقر مرتين للحظر',
            selectMode: 'وضع التحديد: انقر للتحديد، انقر مرتين للحظر، ESC للخروج',
            clearedAll: 'تم مسح جميع الحظر',
            selectElement: 'حدد العنصر للحظر',
            blockedCount: 'تم حظر {0} عنصر',
            delete: 'حذف',
            clearAll: 'مسح جميع الحظر',
            clickToSelect: 'انقر على الزر أعلاه لتحديد العناصر'
        }
    };
    const T = I18N[LANG] || I18N.en;

    const MODULE = { id: (typeof __MODULE_INFO__ !== 'undefined' ? __MODULE_INFO__.id : 'element-blocker'), name: T.name, icon: '🚫', color: '#ef4444' };
    const STORAGE_KEY = 'wta_blocked_elements';
    let blockedSelectors = JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]');
    let selectMode = false;
    let hoveredElement = null;
    let highlightOverlay = null;

    // Create高亮覆盖层
    function createOverlay() {
        if (highlightOverlay) return;
        highlightOverlay = document.createElement('div');
        highlightOverlay.id = 'wta-element-highlight';
        highlightOverlay.style.cssText = 'position:fixed;pointer-events:none;z-index:2147483646;border:2px solid #ef4444;background:rgba(239,68,68,0.15);transition:all 0.1s ease;display:none';
        document.body.appendChild(highlightOverlay);
    }

    // Generate元素的唯一选择器
    function getSelector(el) {
        if (!el || el === document.body || el === document.documentElement) return null;
        if (el.id) return '#' + CSS.escape(el.id);

        let path = [];
        while (el && el !== document.body && el !== document.documentElement) {
            let selector = el.tagName.toLowerCase();
            if (el.className && typeof el.className === 'string') {
                const classes = el.className.trim().split(/\s+/).filter(c => c && !c.includes('wta-'));
                if (classes.length) selector += '.' + classes.map(c => CSS.escape(c)).join('.');
            }
            const parent = el.parentElement;
            if (parent) {
                const siblings = Array.from(parent.children).filter(c => c.tagName === el.tagName);
                if (siblings.length > 1) {
                    const idx = siblings.indexOf(el) + 1;
                    selector += ':nth-of-type(' + idx + ')';
                }
            }
            path.unshift(selector);
            el = parent;
            if (path.length >= 4) break;
        }
        return path.join(' > ');
    }

    // App屏蔽规则
    function applyBlockedRules() {
        let styleEl = document.getElementById('wta-blocked-styles');
        if (!styleEl) {
            styleEl = document.createElement('style');
            styleEl.id = 'wta-blocked-styles';
            document.head.appendChild(styleEl);
        }
        if (blockedSelectors.length) {
            styleEl.textContent = blockedSelectors.map(s => s + '{display:none!important}').join('');
        } else {
            styleEl.textContent = '';
        }
    }

    // Save屏蔽规则
    function saveRules() {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(blockedSelectors));
        applyBlockedRules();
    }

    // 屏蔽元素
    function blockElement(selector) {
        if (!selector || blockedSelectors.includes(selector)) return;
        blockedSelectors.push(selector);
        saveRules();
        __WTA_MODULE_UI__.toast(T.blocked);
    }

    // Cancel屏蔽
    function unblockElement(index) {
        blockedSelectors.splice(index, 1);
        saveRules();
        __WTA_MODULE_UI__.toast(T.unblocked);
        __WTA_MODULE_UI__.updatePanel(MODULE.id, getPanelHtml());
    }

    // 鼠标移动事件
    function onMouseMove(e) {
        if (!selectMode) return;
        const el = document.elementFromPoint(e.clientX, e.clientY);
        if (!el || el === highlightOverlay || el.closest('#wta-module-panel') || el.closest('#wta-module-fab')) {
            if (highlightOverlay) highlightOverlay.style.display = 'none';
            hoveredElement = null;
            return;
        }
        hoveredElement = el;
        const rect = el.getBoundingClientRect();
        if (highlightOverlay) {
            highlightOverlay.style.display = 'block';
            highlightOverlay.style.left = rect.left + 'px';
            highlightOverlay.style.top = rect.top + 'px';
            highlightOverlay.style.width = rect.width + 'px';
            highlightOverlay.style.height = rect.height + 'px';
        }
    }

    // 单击选择
    function onClick(e) {
        if (!selectMode || !hoveredElement) return;
        e.preventDefault();
        e.stopPropagation();
        const selector = getSelector(hoveredElement);
        if (selector) {
            __WTA_MODULE_UI__.toast(T.selected + ': ' + hoveredElement.tagName.toLowerCase() + ' (' + T.dblClickToBlock + ')');
        }
    }

    // 双击屏蔽
    function onDblClick(e) {
        if (!selectMode || !hoveredElement) return;
        e.preventDefault();
        e.stopPropagation();
        const selector = getSelector(hoveredElement);
        if (selector) {
            blockElement(selector);
            exitSelectMode();
        }
    }

    // 进入选择模式
    function enterSelectMode() {
        selectMode = true;
        createOverlay();
        document.addEventListener('mousemove', onMouseMove, true);
        document.addEventListener('click', onClick, true);
        document.addEventListener('dblclick', onDblClick, true);
        document.body.style.cursor = 'crosshair';
        __WTA_MODULE_UI__.toast(T.selectMode);
        __WTA_MODULE_UI__.closePanel();

        // ESC 退出
        document.addEventListener('keydown', function escHandler(e) {
            if (e.key === 'Escape') {
                exitSelectMode();
                document.removeEventListener('keydown', escHandler);
            }
        });
    }

    // 退出选择模式
    function exitSelectMode() {
        selectMode = false;
        hoveredElement = null;
        if (highlightOverlay) highlightOverlay.style.display = 'none';
        document.removeEventListener('mousemove', onMouseMove, true);
        document.removeEventListener('click', onClick, true);
        document.removeEventListener('dblclick', onDblClick, true);
        document.body.style.cursor = '';
    }

    // 清除所有屏蔽
    function clearAll() {
        blockedSelectors = [];
        saveRules();
        __WTA_MODULE_UI__.toast(T.clearedAll);
        __WTA_MODULE_UI__.updatePanel(MODULE.id, getPanelHtml());
    }

    function getPanelHtml() {
        let html = '<div class="wta-blocker-panel">' +
            '<button class="wta-blocker-select-btn" data-wta-action="enterSelectMode"><span>👆</span> ' + T.selectElement + '</button>';

        html += '<div class="wta-blocker-count">' + T.blockedCount.replace('{0}', blockedSelectors.length) + '</div>';

        if (blockedSelectors.length) {
            html += '<div class="wta-blocker-list">';
            blockedSelectors.forEach(function(selector, i) {
                html += '<div class="wta-blocker-item">' +
                    '<span class="wta-blocker-item-selector">' + selector.replace(/</g, '&lt;') + '</span>' +
                    '<button class="wta-blocker-item-btn" data-wta-action="unblock" data-wta-arg="' + i + '">✕</button></div>';
            });
            html += '</div>';
            html += '<button class="wta-blocker-clear-btn" data-wta-action="clearAllBlocks">' + T.clearAll + '</button>';
        } else {
            html += '<div class="wta-blocker-empty"><div class="wta-blocker-empty-icon">🎯</div><div>' + T.clickToSelect + '</div></div>';
        }

        return html + '</div>';
    }

    window.__wta_module_action_enterSelectMode = enterSelectMode;
    window.__wta_module_action_unblock = function(i) { unblockElement(parseInt(i)); };
    window.__wta_module_action_clearAllBlocks = clearAll;

    function register() {
        if (typeof __WTA_MODULE_UI__ === 'undefined') { setTimeout(register, 100); return; }
        __WTA_MODULE_UI__.register({ ...MODULE, uiConfig: (typeof __MODULE_UI_CONFIG__ !== 'undefined' ? __MODULE_UI_CONFIG__ : undefined), runMode: (typeof __MODULE_RUN_MODE__ !== 'undefined' ? __MODULE_RUN_MODE__ : 'INTERACTIVE'), onAction: c => c.innerHTML = getPanelHtml() });
    }

    applyBlockedRules();
    document.readyState === 'loading' ? document.addEventListener('DOMContentLoaded', register) : register();
})();
"""
}
