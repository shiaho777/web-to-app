package com.webtoapp.core.webview

import android.webkit.WebView
import com.webtoapp.core.logging.AppLogger

object CloudflareCompat {

    private const val TAG = "CloudflareCompat"

    private val CLOUDFLARE_CHALLENGE_HOSTS = setOf(
        "challenges.cloudflare.com",
        "cf-turnstile.com"
    )

    private val CLOUDFLARE_PAGE_INDICATORS = listOf(
        "challenges.cloudflare.com",
        "cf-turnstile",
        "cdn-cgi/challenge-platform",
        "__CF\$cv\$params",
        "cf-ray",
        "Just a moment"
    )

    fun isCloudflareChallenge(url: String?): Boolean {
        if (url.isNullOrEmpty()) return false
        val host = try {
            android.net.Uri.parse(url).host?.lowercase() ?: return false
        } catch (e: Exception) {
            return false
        }
        return CLOUDFLARE_CHALLENGE_HOSTS.any { host == it || host.endsWith(".$it") }
    }

    fun hasCloudflareSignal(
        url: String?,
        reasonPhrase: String? = null,
        responseHeaders: Map<String, String>? = null
    ): Boolean {
        val haystack = buildString {
            append(url.orEmpty())
            append('\n')
            append(reasonPhrase.orEmpty())
            responseHeaders?.forEach { (key, value) ->
                append('\n')
                append(key)
                append(": ")
                append(value)
            }
        }.lowercase()

        return CLOUDFLARE_PAGE_INDICATORS.any { haystack.contains(it.lowercase()) } ||
            responseHeaders?.keys?.any { it.equals("cf-ray", ignoreCase = true) } == true ||
            responseHeaders?.values?.any { it.equals("cloudflare", ignoreCase = true) } == true
    }

    fun stripWebViewMarker(userAgent: String): String {
        if (!userAgent.contains("wv")) return userAgent

        return userAgent
            .replace("; wv)", ")")
            .replace(";wv)", ")")
            .replace(" wv)", ")")
    }

    fun generateCompatJs(): String = CLOUDFLARE_COMPAT_JS

    fun injectCompat(webView: WebView, url: String?) {
        try {
            webView.evaluateJavascript(CLOUDFLARE_COMPAT_JS, null)
            AppLogger.d(TAG, "Cloudflare compat JS injected for: ${url?.take(60)}")
        } catch (e: Exception) {
            AppLogger.w(TAG, "Cloudflare compat JS injection failed", e)
        }
    }

    private val CLOUDFLARE_COMPAT_JS = """(function(){
'use strict';
if(window.__wta_cf_compat__)return;
window.__wta_cf_compat__=1;

// ── toString 保护：让后续 hook 的 toString() 返回 [native code] ──
var _ht=new WeakSet();
var _ots=Function.prototype.toString;
function _mn(fn){_ht.add(fn);return fn;}
try{
Function.prototype.toString=_mn(function(){
    if(_ht.has(this))return'function '+this.name+'() { [native code] }';
    return _ots.call(this);
});
}catch(e){}

// ── 1. navigator.webdriver → false ──
try{
Object.defineProperty(navigator,'webdriver',{
    get:_mn(function(){return false}),
    enumerable:true,configurable:true
});
}catch(e){}

// ── 2. window.chrome 基础结构 ──
try{
if(!window.chrome)window.chrome={};
if(!window.chrome.runtime)window.chrome.runtime={
    connect:_mn(function(){return{onDisconnect:{addListener:function(){}},onMessage:{addListener:function(){}},postMessage:function(){},disconnect:function(){}}}),
    sendMessage:_mn(function(){}),
    id:undefined
};
if(!window.chrome.csi)window.chrome.csi=_mn(function(){
    return{onloadT:Date.now(),startE:Date.now()-300,pageT:performance.now(),tran:15};
});
if(!window.chrome.loadTimes)window.chrome.loadTimes=_mn(function(){
    var n=performance.now()/1000;
    return{requestTime:n-0.3,startLoadTime:n-0.25,commitLoadTime:n-0.1,
        finishDocumentLoadTime:n-0.05,finishLoadTime:n,firstPaintTime:n-0.08,
        firstPaintAfterLoadTime:0,navigationType:'Other',
        wasFetchedViaSpdy:true,wasNpnNegotiated:true,npnNegotiatedProtocol:'h2',
        wasAlternateProtocolAvailable:false,connectionInfo:'h2'};
});
}catch(e){}

// ── 3. document.visibilityState → 'visible' ──
try{
Object.defineProperty(document,'visibilityState',{
    get:_mn(function(){return'visible'}),
    configurable:true
});
Object.defineProperty(document,'hidden',{
    get:_mn(function(){return false}),
    configurable:true
});
}catch(e){}

// ── 4. screen 属性修复（某些 WebView 初始化时 screen 为 0）──
try{
if(screen.width===0)Object.defineProperty(screen,'width',{get:_mn(function(){return 412}),configurable:true});
if(screen.height===0)Object.defineProperty(screen,'height',{get:_mn(function(){return 915}),configurable:true});
if(screen.availWidth===0)Object.defineProperty(screen,'availWidth',{get:_mn(function(){return 412}),configurable:true});
if(screen.availHeight===0)Object.defineProperty(screen,'availHeight',{get:_mn(function(){return 891}),configurable:true});
if(screen.colorDepth===0)Object.defineProperty(screen,'colorDepth',{get:_mn(function(){return 24}),configurable:true});
}catch(e){}

// ── 5. Worker 构造函数存在性 ──
try{
if(!window.Worker){
    window.Worker=_mn(function(url){
        this.postMessage=function(){};
        this.terminate=function(){};
        this.onmessage=null;
        this.onerror=null;
    });
}
}catch(e){}

// ── 6. Notification.permission（Cloudflare 检测环境完整性）──
try{
if(!window.Notification){
    window.Notification=_mn(function(){});
    window.Notification.permission='default';
    window.Notification.requestPermission=_mn(function(cb){
        if(cb)cb('default');
        return Promise.resolve('default');
    });
}
}catch(e){}

// ── 7. navigator.permissions.query 补丁 ──
try{
if(navigator.permissions&&navigator.permissions.query){
    var _opq=navigator.permissions.query.bind(navigator.permissions);
    navigator.permissions.query=_mn(function(desc){
        if(desc&&desc.name==='notifications'){
            return Promise.resolve({state:'prompt',onchange:null});
        }
        return _opq(desc);
    });
}
}catch(e){}

// ── 8. document.hasFocus() → true ──
try{
document.hasFocus=_mn(function(){return true});
}catch(e){}

})();"""

}
