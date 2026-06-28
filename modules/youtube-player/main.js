(function() {
    'use strict';

    const TAG = "StreamExtractor";
    const CODEPEN_PROXY_PEN = 'oNPzxKo'; // Public CodePen proxy pen - no setup needed

    let logMessages = [];

    function log(msg) {
        console.log(`[${TAG}] ${msg}`);
        logMessages.push(msg);
        console.log('LOG HISTORY:', logMessages);
    }

    // ============ VIDEO ID EXTRACTORS ============

    function extractYouTubeVideoId(url) {
        const patterns = [
            /(?:youtube\.com\/watch\?v=|youtu\.be\/|youtube\.com\/embed\/|m\.youtube\.com\/watch\?v=)([a-zA-Z0-9_-]{11})/,
            /v=([a-zA-Z0-9_-]{11})/
        ];
        for (let pattern of patterns) {
            const match = url.match(pattern);
            if (match && match[1]) return match[1];
        }
        return null;
    }

    // ============ YOUTUBE EXTRACTION ============

    async function extractYouTubeStream(youtubeUrl) {
        const videoId = extractYouTubeVideoId(youtubeUrl);
        if (!videoId) {
            log('❌ Could not extract video ID from URL');
            return null;
        }

        log(`🎬 Extracting YouTube video: ${videoId}`);

        // Method: youtube-nocookie embed (works for all videos)
        log('Method: Using youtube-nocookie embed...');
        const nocookieUrl = `https://www.youtube-nocookie.com/embed/${videoId}?autoplay=1`;
        return nocookieUrl;
    }

    // ============ CODEPEN FALLBACK ============

    function getCodePenProxyUrl(streamUrl) {
        const encodedUrl = encodeURIComponent(streamUrl);
        const proxyUrl = `https://cdpn.io/pen/debug/${CODEPEN_PROXY_PEN}?v=${encodedUrl}`;
        log(`🔄 Using CodePen proxy fallback: ${CODEPEN_PROXY_PEN}`);
        return proxyUrl;
    }

    // ============ VIDEO PLAYER ============

    function playStream(streamUrl, title = 'Video', isEmbed = false, fallbackIfFails = true, playBtn = null) {
        if (!streamUrl) {
            alert('❌ Failed to extract stream URL');
            return;
        }

        log(`▶️ Playing: ${streamUrl.substring(0, 80)}...`);

        const wrapper = document.createElement('div');
        wrapper.style.cssText = `
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            z-index: 99999;
            background: #000;
            display: flex;
            flex-direction: column;
        `;

        // Close button (top-right corner)
        const closeBtn = document.createElement('button');
        closeBtn.textContent = '✕';
        closeBtn.style.cssText = `
            position: absolute;
            top: 20px;
            right: 20px;
            width: 50px;
            height: 50px;
            background: rgba(0, 0, 0, 0.7);
            color: white;
            border: 2px solid white;
            border-radius: 50%;
            font-size: 32px;
            cursor: pointer;
            z-index: 100000;
            display: flex;
            align-items: center;
            justify-content: center;
            transition: all 0.3s;
        `;

        closeBtn.onmouseover = () => {
            closeBtn.style.background = 'rgba(255, 0, 0, 0.8)';
            closeBtn.style.transform = 'scale(1.1)';
        };
        closeBtn.onmouseout = () => {
            closeBtn.style.background = 'rgba(0, 0, 0, 0.7)';
            closeBtn.style.transform = 'scale(1)';
        };

        closeBtn.onclick = () => {
            wrapper.remove();
            closeBtn.remove();
            // Reset play button if provided
            if (playBtn) {
                playBtn.textContent = '▶';
                playBtn.disabled = false;
            }
        };

        wrapper.appendChild(closeBtn);

        // Video container (fullscreen)
        const videoContainer = document.createElement('div');
        videoContainer.style.cssText = `
            flex: 1;
            background: #000;
            display: flex;
            justify-content: center;
            align-items: center;
            position: relative;
        `;

        if (isEmbed) {
            // Use iframe for embed URLs
            const iframe = document.createElement('iframe');
            iframe.src = streamUrl;
            iframe.style.cssText = `
                width: 100%;
                height: 100%;
                border: none;
            `;
            iframe.allow = 'accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; fullscreen';
            iframe.allowFullscreen = true;
            videoContainer.appendChild(iframe);
        } else {
            // Use video player for stream URLs
            const video = document.createElement('video');
            video.src = streamUrl;
            video.controls = true;
            video.autoplay = true;
            video.style.cssText = `
                max-width: 100%;
                max-height: 100%;
                background: #000;
            `;
            
            video.addEventListener('error', (e) => {
                log(`❌ Video player error: ${e.target.error.code}`);
                
                // Try CodePen fallback automatically
                if (fallbackIfFails) {
                    log('🔄 Attempting CodePen proxy fallback...');
                    const fallbackUrl = getCodePenProxyUrl(streamUrl);
                    if (fallbackUrl) {
                        videoContainer.innerHTML = '';
                        const fallbackIframe = document.createElement('iframe');
                        fallbackIframe.src = fallbackUrl;
                        fallbackIframe.style.cssText = `
                            width: 100%;
                            height: 100%;
                            border: none;
                        `;
                        fallbackIframe.allow = 'accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; fullscreen';
                        fallbackIframe.allowFullscreen = true;
                        videoContainer.appendChild(fallbackIframe);
                        log('✅ CodePen proxy fallback loaded');
                        return;
                    }
                }

                // Show error if no fallback
                const errorMsg = document.createElement('div');
                errorMsg.style.cssText = `
                    color: white;
                    text-align: center;
                    position: absolute;
                    top: 50%;
                    left: 50%;
                    transform: translate(-50%, -50%);
                `;
                errorMsg.innerHTML = `
                    <div style="font-size: 20px; margin-bottom: 10px;">❌ Cannot play video</div>
                    <div style="font-size: 12px; margin-bottom: 20px;">Direct playback failed & CodePen fallback also failed</div>
                    <details style="text-align: left; color: #aaa; font-size: 10px; max-width: 80vw;">
                        <summary>Debug Info</summary>
                        <pre>${logMessages.join('\n')}</pre>
                    </details>
                `;
                videoContainer.innerHTML = '';
                videoContainer.appendChild(errorMsg);
            });

            videoContainer.appendChild(video);
        }

        wrapper.appendChild(videoContainer);
        document.body.appendChild(wrapper);

        // ESC to close
        const handleEsc = (e) => {
            if (e.key === 'Escape') {
                wrapper.remove();
                document.removeEventListener('keydown', handleEsc);
                // Reset play button if provided
                if (playBtn) {
                    playBtn.textContent = '▶';
                    playBtn.disabled = false;
                }
            }
        };
        document.addEventListener('keydown', handleEsc);
    }

    // ============ UI CONTROLS ============

    function createPlayButton() {
        const currentUrl = window.location.href;

        // Create floating play button
        const playBtn = document.createElement('button');
        playBtn.textContent = '▶';
        playBtn.title = 'Play Fullscreen';
        playBtn.style.cssText = `
            position: fixed;
            bottom: 20px;
            right: 20px;
            width: 60px;
            height: 60px;
            background: linear-gradient(135deg, #0066cc, #0052a3);
            color: white;
            border: 2px solid #0066cc;
            border-radius: 50%;
            cursor: pointer;
            font-size: 28px;
            font-weight: bold;
            z-index: 9998;
            display: flex;
            align-items: center;
            justify-content: center;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.5);
            transition: all 0.3s;
        `;

        playBtn.onmouseover = () => {
            playBtn.style.background = 'linear-gradient(135deg, #0052a3, #003d7a)';
            playBtn.style.transform = 'scale(1.15)';
            playBtn.style.boxShadow = '0 6px 16px rgba(0, 102, 204, 0.6)';
        };
        playBtn.onmouseout = () => {
            playBtn.style.background = 'linear-gradient(135deg, #0066cc, #0052a3)';
            playBtn.style.transform = 'scale(1)';
            playBtn.style.boxShadow = '0 4px 12px rgba(0, 0, 0, 0.5)';
        };

        playBtn.onclick = async () => {
            playBtn.textContent = '⏳';
            playBtn.disabled = true;

            try {
                // YouTube
                if (currentUrl.includes('youtube.com') || currentUrl.includes('youtu.be') || currentUrl.includes('m.youtube.com')) {
                    const videoId = extractYouTubeVideoId(currentUrl);
                    if (videoId) {
                        const streamUrl = `https://www.youtube-nocookie.com/embed/${videoId}?autoplay=1`;
                        log(`✅ Playing YouTube: ${videoId}`);
                        playStream(streamUrl, 'YouTube', true, true, playBtn);
                    }
                    return;
                }

            } catch (e) {
                log(`❌ Error: ${e.message}`);
                playBtn.textContent = '▶';
                playBtn.disabled = false;
            }
        };

        document.body.appendChild(playBtn);
    }

    // Initialize
    createPlayButton();
    log('✅ YouTube Stream Extractor v5 loaded - One-click fullscreen playback');
})();