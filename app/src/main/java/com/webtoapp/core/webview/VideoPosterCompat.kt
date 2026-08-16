package com.webtoapp.core.webview

import android.webkit.WebView
import com.webtoapp.core.logging.AppLogger

/**
 * Android WebView renders a `<video>` without a `poster` attribute through an
 * internal pseudo-URL (`android-webview-video-poster:default_video_poster/<id>`)
 * that only exists inside WebView. Site scripts that feed every media URL
 * through fetch / canvas / lazy-loader pipelines cannot load that scheme and
 * fail with "No 'Access-Control-Allow-Origin' header" (or a CSP violation),
 * which can break their player init (#563). Browsers never generate the
 * pseudo-URL, so sites cannot guard against it themselves.
 *
 * Giving each poster-less video a transparent data-URI poster stops WebView
 * from generating the pseudo-URL at all, which removes the whole error class.
 */
object VideoPosterCompat {

    fun injectScript(webView: WebView) {
        try {
            webView.evaluateJavascript(INJECTION_SCRIPT, null)
        } catch (e: Exception) {
            AppLogger.w("VideoPosterCompat", "Script injection failed", e)
        }
    }

    internal val INJECTION_SCRIPT = """
(function(){
    if (window.__wtaVideoPosterShimInstalled) return;
    window.__wtaVideoPosterShimInstalled = true;
    var POSTER = 'data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7';
    function patch(video) {
        try {
            if (!video.hasAttribute('poster')) {
                video.setAttribute('poster', POSTER);
            }
        } catch (e) {}
    }
    function scan(root) {
        try {
            if (root && root.nodeName === 'VIDEO') patch(root);
            if (root && root.querySelectorAll) {
                var videos = root.querySelectorAll('video');
                for (var i = 0; i < videos.length; i++) patch(videos[i]);
            }
        } catch (e) {}
    }
    scan(document);
    try {
        var observer = new MutationObserver(function (mutations) {
            for (var i = 0; i < mutations.length; i++) {
                var added = mutations[i].addedNodes;
                for (var j = 0; j < added.length; j++) scan(added[j]);
            }
        });
        observer.observe(document.documentElement || document.body || document, { childList: true, subtree: true });
    } catch (e) {}
})();
    """.trimIndent()
}
