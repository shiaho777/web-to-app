package com.webtoapp.core.webview

import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.webtoapp.core.logging.AppLogger
import java.util.concurrent.atomic.AtomicInteger
import java.util.WeakHashMap

object WebScrollTracker {

    private const val JS_INTERFACE_NAME = "_wtaScrollBridge"
    private const val INSTALLED_FLAG = "__wtaScrollTrackerInstalled"

    private val states = WeakHashMap<WebView, AtomicInteger>()

    class Bridge(private val state: AtomicInteger) {
        @JavascriptInterface
        fun report(canScrollUp: Int) {
            state.set(if (canScrollUp != 0) 1 else 0)
        }
    }

    fun install(webView: WebView) {
        if (states.containsKey(webView)) return
        val state = AtomicInteger(0)
        states[webView] = state
        try {
            webView.addJavascriptInterface(Bridge(state), JS_INTERFACE_NAME)
        } catch (e: Exception) {
            AppLogger.w("WebScrollTracker", "Failed to register JS interface", e)
        }
    }

    fun reset(webView: WebView) {
        states[webView]?.set(0)
    }

    fun injectScript(webView: WebView) {
        try {
            webView.evaluateJavascript(INJECTION_SCRIPT, null)
        } catch (e: Exception) {
            AppLogger.w("WebScrollTracker", "Script injection failed", e)
        }
    }

    fun scrollUpBlocked(webView: WebView?, nativeScrollY: Int): Boolean {
        if (webView == null) return false
        if (nativeScrollY > 0) return true
        return (states[webView]?.get() ?: 0) != 0
    }

    private val INJECTION_SCRIPT = """
(function(){
    if(window.__wtaScrollTrackerInstalled) return;
    window.__wtaScrollTrackerInstalled = true;
    function report(canUp){
        try { window._wtaScrollBridge.report(canUp ? 1 : 0); } catch(e) {}
    }
    function chainCanScrollUp(el){
        try {
            var docTop = Math.max(
                window.pageYOffset || 0,
                document.documentElement ? document.documentElement.scrollTop : 0,
                document.body ? document.body.scrollTop : 0
            );
            if (docTop > 0) return true;
            var cur = el;
            while (cur && cur.nodeType === 1) {
                try {
                    var cs = window.getComputedStyle(cur);
                    if (cs && /auto|scroll|overlay/.test(cs.overflowY + ' ' + cs.overflow)) {
                        if (cur.scrollTop > 0) return true;
                    }
                } catch(e) {}
                cur = cur.parentNode;
            }
            return false;
        } catch(e) { return false; }
    }
    function reportFromTarget(e){
        var t = e.target;
        if (t && t.nodeType !== 1) t = (t.parentElement || null);
        report(chainCanScrollUp(t));
    }
    try {
        document.addEventListener('touchstart', reportFromTarget, {passive:true, capture:true});
        document.addEventListener('scroll', reportFromTarget, {passive:true, capture:true});
        window.addEventListener('scroll', function(){ report(chainCanScrollUp(document.documentElement)); }, {passive:true, capture:true});
        report(chainCanScrollUp(document.documentElement));
    } catch(e) {}
})();
    """.trimIndent()
}
