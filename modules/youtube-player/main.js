(function() {
    'use strict';

    const TAG = "YouTubePlayer";

    function log(msg) { console.log(`[${TAG}] ${msg}`); }

    function extractYouTubeVideoId(url) {
        const patterns = [
            /(?:youtube\.com\/watch\?v=|youtu\.be\/|youtube\.com\/embed\/|m\.youtube\.com\/watch\?v=)([a-zA-Z0-9_-]{11})/,
            /v=([a-zA-Z0-9_-]{11})/
        ];
        for (const pattern of patterns) {
            const match = url.match(pattern);
            if (match && match[1]) return match[1];
        }
        return null;
    }

    function playStream(streamUrl, playBtn) {
        if (!streamUrl) {
            alert('Failed to start playback');
            return;
        }

        log('Playing: ' + streamUrl);

        const wrapper = document.createElement('div');
        wrapper.style.cssText = 'position:fixed;top:0;left:0;width:100%;height:100%;z-index:99999;background:#000;display:flex;flex-direction:column;';

        const closeBtn = document.createElement('button');
        closeBtn.textContent = '✕';
        closeBtn.style.cssText = 'position:absolute;top:20px;right:20px;width:50px;height:50px;background:rgba(0,0,0,0.7);color:#fff;border:2px solid #fff;border-radius:50%;font-size:32px;cursor:pointer;z-index:100000;display:flex;align-items:center;justify-content:center;transition:all 0.3s;';
        closeBtn.onmouseover = () => { closeBtn.style.background = 'rgba(255,0,0,0.8)'; closeBtn.style.transform = 'scale(1.1)'; };
        closeBtn.onmouseout = () => { closeBtn.style.background = 'rgba(0,0,0,0.7)'; closeBtn.style.transform = 'scale(1)'; };

        const resetBtn = () => { if (playBtn) { playBtn.textContent = '▶'; playBtn.disabled = false; } };
        const close = () => { wrapper.remove(); resetBtn(); document.removeEventListener('keydown', onEsc); };

        closeBtn.onclick = close;

        const onEsc = (e) => { if (e.key === 'Escape') close(); };
        document.addEventListener('keydown', onEsc);

        wrapper.appendChild(closeBtn);

        const videoContainer = document.createElement('div');
        videoContainer.style.cssText = 'flex:1;background:#000;display:flex;justify-content:center;align-items:center;position:relative;';

        const iframe = document.createElement('iframe');
        iframe.src = streamUrl;
        iframe.style.cssText = 'width:100%;height:100%;border:none;';
        iframe.allow = 'accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; fullscreen';
        iframe.allowFullscreen = true;
        videoContainer.appendChild(iframe);

        wrapper.appendChild(videoContainer);
        document.body.appendChild(wrapper);
    }

    function createPlayButton() {
        const currentUrl = window.location.href;

        const playBtn = document.createElement('button');
        playBtn.textContent = '▶';
        playBtn.title = 'Play Fullscreen';
        playBtn.style.cssText = 'position:fixed;bottom:20px;right:20px;width:60px;height:60px;background:linear-gradient(135deg,#FF0000,#CC0000);color:#fff;border:2px solid #FF0000;border-radius:50%;cursor:pointer;font-size:28px;font-weight:bold;z-index:9998;display:flex;align-items:center;justify-content:center;box-shadow:0 4px 12px rgba(0,0,0,0.5);transition:all 0.3s;';

        playBtn.onmouseover = () => { playBtn.style.transform = 'scale(1.15)'; playBtn.style.boxShadow = '0 6px 16px rgba(255,0,0,0.6)'; };
        playBtn.onmouseout = () => { playBtn.style.transform = 'scale(1)'; playBtn.style.boxShadow = '0 4px 12px rgba(0,0,0,0.5)'; };

        playBtn.onclick = () => {
            playBtn.textContent = '⏳';
            playBtn.disabled = true;

            const videoId = extractYouTubeVideoId(currentUrl);
            if (!videoId) {
                log('No video ID found at: ' + currentUrl);
                playBtn.textContent = '▶';
                playBtn.disabled = false;
                return;
            }

            const streamUrl = 'https://www.youtube-nocookie.com/embed/' + videoId + '?autoplay=1';
            log('Playing YouTube: ' + videoId);
            playStream(streamUrl, playBtn);
        };

        document.body.appendChild(playBtn);
    }

    createPlayButton();
    log('YouTube Player loaded - one-click fullscreen playback');
})();
