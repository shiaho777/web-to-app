package com.webtoapp.core.webview

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebView
import java.lang.ref.WeakReference
import java.util.Locale

internal class PageZoomHost(
    val zoomPercent: Int,
    private val callbacks: Callbacks
) {
    interface Callbacks {
        fun onStarted(url: String)
        fun onFinished(url: String, title: String)
        fun onNavigation(url: String, title: String, canGoBack: Boolean, canGoForward: Boolean)
        fun onError(url: String)
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private var viewRef: WeakReference<WtaWebView>? = null

    val isActive: Boolean
        get() = zoomPercent != 100

    fun attach(view: WtaWebView) {
        viewRef = WeakReference(view)
    }

    fun canWrap(url: String): Boolean {
        val uri = runCatching { Uri.parse(url) }.getOrNull() ?: return false
        val scheme = uri.scheme?.lowercase(Locale.US) ?: return false
        return (scheme == "http" || scheme == "https") && !uri.host.isNullOrBlank()
    }

    @SuppressLint("JavascriptInterface")
    val bridge: Any = object {
        @JavascriptInterface
        fun started(url: String) {
            mainHandler.post { callbacks.onStarted(url) }
        }

        @JavascriptInterface
        fun finished(url: String, title: String) {
            mainHandler.post { callbacks.onFinished(url, title) }
        }

        @JavascriptInterface
        fun navigation(url: String, title: String, canGoBack: Boolean, canGoForward: Boolean) {
            mainHandler.post {
                viewRef?.get()?.updateIframeState(url, title, canGoBack, canGoForward)
                callbacks.onNavigation(url, title, canGoBack, canGoForward)
            }
        }

        @JavascriptInterface
        fun error(url: String) {
            mainHandler.post { callbacks.onError(url) }
        }
    }

    fun load(view: WtaWebView, url: String) {
        val origin = originOf(url) ?: return
        view.loadPageZoomHost(origin, html(url, zoomPercent))
    }

    fun navigateFrame(view: WebView, url: String) {
        view.evaluateJavascript("window.__wtaIframeNavigate&&window.__wtaIframeNavigate(${jsString(url)})", null)
    }

    fun reload(view: WebView) {
        view.evaluateJavascript("window.__wtaIframeReload&&window.__wtaIframeReload()", null)
    }

    fun stop(view: WebView) {
        view.evaluateJavascript(
            "(function(){try{document.getElementById('wta-page-frame').contentWindow.stop();}catch(e){}})()",
            null
        )
    }

    fun back(view: WebView) {
        view.evaluateJavascript("window.__wtaIframeBack&&window.__wtaIframeBack()", null)
    }

    fun forward(view: WebView) {
        view.evaluateJavascript("window.__wtaIframeForward&&window.__wtaIframeForward()", null)
    }

    companion object {
        const val BRIDGE_NAME = "WtaIframeHost"

        private fun originOf(rawUrl: String): String? {
            val uri = runCatching { Uri.parse(rawUrl) }.getOrNull() ?: return null
            val scheme = uri.scheme?.lowercase(Locale.US) ?: return null
            if (scheme != "http" && scheme != "https") return null
            val authority = uri.authority ?: return null
            val hostPort = authority.substringAfterLast('@')
            if (hostPort.isBlank()) return null
            return "$scheme://$hostPort/"
        }

        internal fun html(targetUrl: String, zoomPercent: Int): String {
            val zoom = zoomPercent.coerceIn(1, 1000) / 100.0
            val zoomValue = String.format(Locale.US, "%.6f", zoom)
            return HOST_HTML
                .replace("__TARGET__", jsString(targetUrl))
                .replace("__ZOOM__", zoomValue)
        }

        private fun jsString(value: String): String {
            val out = StringBuilder(value.length + 2)
            out.append('"')
            for (ch in value) {
                when (ch) {
                    '\\' -> out.append("\\\\")
                    '"' -> out.append("\\\"")
                    '\n' -> out.append("\\n")
                    '\r' -> out.append("\\r")
                    '\t' -> out.append("\\t")
                    '<' -> out.append("\\u003c")
                    '>' -> out.append("\\u003e")
                    '\u2028' -> out.append("\\u2028")
                    '\u2029' -> out.append("\\u2029")
                    else -> if (ch.code < 0x20) out.append("\\u").append(ch.code.toString(16).padStart(4, '0')) else out.append(ch)
                }
            }
            out.append('"')
            return out.toString()
        }

        private const val HOST_HTML = """<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover">
<style>
html,body{margin:0;padding:0;width:100%;height:100%;overflow:hidden;background:transparent}
#wta-page-frame{position:absolute;left:0;top:0;display:block;border:0;background:transparent;transform-origin:0 0}
</style>
</head>
<body>
<iframe id="wta-page-frame" allow="camera; microphone; geolocation; fullscreen; clipboard-read; clipboard-write; payment; autoplay; encrypted-media; picture-in-picture; web-share; display-capture" allowfullscreen></iframe>
<script>
(function(){
'use strict';
var TARGET=__TARGET__;
var ZOOM=__ZOOM__;
var frame=document.getElementById('wta-page-frame');
var bridge=window.WtaIframeHost;
var booted=false;
function call(name){
var args=Array.prototype.slice.call(arguments,1);
try{if(bridge&&typeof bridge[name]==='function')bridge[name].apply(bridge,args);}catch(e){}
}
function box(){
var el=document.documentElement;
var w=el?el.clientWidth:0;
var h=el?el.clientHeight:0;
if(!w||!h){w=window.innerWidth;h=window.innerHeight;}
return{w:w,h:h};
}
function size(){
var boxed=box();
var w=Math.max(1,Math.ceil(boxed.w/ZOOM)+1);
var h=Math.max(1,Math.ceil(boxed.h/ZOOM)+1);
frame.style.width=w+'px';
frame.style.height=h+'px';
frame.style.transform='scale('+ZOOM+')';
}
function frameUrl(){
try{return frame.contentWindow.location.href;}catch(e){return TARGET;}
}
function frameTitle(){
try{return frame.contentWindow.document.title||'';}catch(e){return '';}
}
function state(){
var url=frameUrl();
var title=frameTitle();
var back=false,forward=false;
try{
var w=frame.contentWindow;
if(w.navigation&&typeof w.navigation.canGoBack==='boolean'){
back=w.navigation.canGoBack;
forward=w.navigation.canGoForward;
}else{
back=w.history.length>1;
}
}catch(e){}
call('navigation',url,title,back,forward);
}
function hook(){
try{
var w=frame.contentWindow;
if(!w||w.__wtaPageZoomHooked)return;
w.__wtaPageZoomHooked=true;
var push=w.history.pushState;
var replace=w.history.replaceState;
w.history.pushState=function(){var r=push.apply(this,arguments);setTimeout(state,0);return r;};
w.history.replaceState=function(){var r=replace.apply(this,arguments);setTimeout(state,0);return r;};
w.addEventListener('popstate',state);
w.addEventListener('hashchange',state);
var title=w.document.querySelector('title');
if(title&&typeof MutationObserver==='function'){
new MutationObserver(state).observe(title,{childList:true,subtree:true,characterData:true});
}
}catch(e){}
}
frame.addEventListener('load',function(){
var url=frameUrl();
if(!booted||url==='about:blank')return;
size();
hook();
var title=frameTitle();
state();
call('finished',url,title);
});
frame.addEventListener('error',function(){call('error',TARGET);});
window.addEventListener('resize',size);
window.__wtaIframeNavigate=function(url){try{frame.src=url;}catch(e){}};
window.__wtaIframeReload=function(){try{frame.contentWindow.location.reload();return true;}catch(e){try{frame.src=frame.src;return true;}catch(e2){return false;}}};
window.__wtaIframeBack=function(){try{frame.contentWindow.history.back();return true;}catch(e){return false;}};
window.__wtaIframeForward=function(){try{frame.contentWindow.history.forward();return true;}catch(e){return false;}};
window.__wtaIframeAtTop=function(){try{return frame.contentWindow.scrollY<=0;}catch(e){return true;}};
size();
booted=true;
call('started',TARGET);
frame.src=TARGET;
})();
</script>
</body>
</html>"""
    }
}
