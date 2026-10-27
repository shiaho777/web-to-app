package com.webtoapp.core.webview

import android.webkit.WebView
import com.webtoapp.core.logging.AppLogger

object CloudflareCompat {

    private const val TAG = "CloudflareCompat"

    private val CLOUDFLARE_CHALLENGE_HOSTS = setOf(
        "challenges.cloudflare.com",
        "cf-turnstile.com",
        "turnstile.cloudflare.com"
    )

    private val CLOUDFLARE_PAGE_INDICATORS = listOf(
        "challenges.cloudflare.com",
        "cf-turnstile",
        "cdn-cgi/challenge-platform",
        "__CF\$cv\$params",
        "cf-browser-verification",
        "cf-challenge",
        "Just a moment",
        "Attention Required",
        "Checking your browser",
        "Enable JavaScript and cookies to continue"
    )

    fun isCloudflareChallenge(url: String?): Boolean {
        if (url.isNullOrEmpty()) return false
        val host = try {
            android.net.Uri.parse(url).host?.lowercase() ?: return false
        } catch (e: Exception) {
            return false
        }
        if (CLOUDFLARE_CHALLENGE_HOSTS.any { host == it || host.endsWith(".$it") }) return true
        val lower = url.lowercase()
        return lower.contains("cdn-cgi/challenge-platform") ||
            lower.contains("__cf_chl") ||
            lower.contains("cf-browser-verification")
    }

    fun hasCloudflareSignal(
        url: String?,
        reasonPhrase: String? = null,
        responseHeaders: Map<String, String>? = null
    ): Boolean {
        if (isCloudflareChallenge(url)) return true

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

        if (CLOUDFLARE_PAGE_INDICATORS.any { haystack.contains(it.lowercase()) }) return true

        val server = responseHeaders?.entries
            ?.firstOrNull { it.key.equals("server", ignoreCase = true) }
            ?.value
            ?.lowercase()
            .orEmpty()
        if (server.contains("cloudflare")) return true

        val hasCfRay = responseHeaders?.keys?.any { it.equals("cf-ray", ignoreCase = true) } == true
        val hasCfChallenge = responseHeaders?.keys?.any {
            it.equals("cf-mitigated", ignoreCase = true) ||
                it.equals("cf-challenge", ignoreCase = true) ||
                it.startsWith("cf-chl", ignoreCase = true)
        } == true
        return hasCfChallenge || (hasCfRay && (
            haystack.contains("just a moment") ||
                haystack.contains("challenge") ||
                haystack.contains("turnstile") ||
                haystack.contains("attention required")
        ))
    }

    fun stripWebViewMarker(userAgent: String): String {
        if (!userAgent.contains("wv")) return userAgent

        return userAgent
            .replace("; wv)", ")")
            .replace(";wv)", ")")
            .replace(" wv)", ")")
    }

    fun shouldEnableThirdPartyCookies(enableCloudflareCompat: Boolean): Boolean = enableCloudflareCompat

    fun generateCompatJs(): String = CLOUDFLARE_COMPAT_JS

    fun injectCompat(webView: WebView, url: String?) {
        try {
            webView.evaluateJavascript(CLOUDFLARE_COMPAT_JS, null)
            AppLogger.d(TAG, "Cloudflare compat JS injected for: ${url?.take(60)}")
        } catch (e: Exception) {
            AppLogger.w(TAG, "Cloudflare compat JS injection failed", e)
        }
    }

    /**
     * Hosts whose documents must stay untouched. Turnstile decides inside its
     * own iframe; a script that rewrites `Function.prototype.toString` or
     * invents `chrome.runtime` there makes the check spin on "Verifying...".
     */
    fun isChallengeFrameHost(host: String?): Boolean {
        val h = host?.trim()?.lowercase() ?: return false
        return CHALLENGE_FRAME_SUFFIXES.any { suffix -> h == suffix || h.endsWith(".$suffix") }
    }

    /** Proxy bypass rules so a local DNS or TLS bridge is not the TLS fingerprint. */
    fun challengeProxyBypassRules(): List<String> =
        CHALLENGE_FRAME_SUFFIXES.flatMap { listOf(it, "*.$it") }

    /**
     * Runs [script] everywhere except challenge iframes. The kernel-flavor and
     * device-disguise scripts stay out of this wrapper: those frames must see
     * the same identity as the parent (#1121).
     */
    fun exemptChallengeFrames(script: String): String = """
(function(){
try{
var h=(location.hostname||'').toLowerCase();
if(h==='challenges.cloudflare.com'||h.endsWith('.challenges.cloudflare.com')||h==='cf-turnstile.com'||h.endsWith('.cf-turnstile.com')||h==='turnstile.cloudflare.com'||h.endsWith('.turnstile.cloudflare.com')||h==='hcaptcha.com'||h.endsWith('.hcaptcha.com')||h==='recaptcha.net'||h.endsWith('.recaptcha.net')||h==='arkoselabs.com'||h.endsWith('.arkoselabs.com')||h==='funcaptcha.com'||h.endsWith('.funcaptcha.com'))return;
}catch(e){}
$script
})();
""".trimIndent()

    private val CHALLENGE_FRAME_SUFFIXES = listOf(
        "challenges.cloudflare.com",
        "cf-turnstile.com",
        "turnstile.cloudflare.com",
        "hcaptcha.com",
        "recaptcha.net",
        "arkoselabs.com",
        "funcaptcha.com"
    )

    private val CLOUDFLARE_COMPAT_JS = """(function(){
'use strict';
try{
var __cfh=(location.hostname||'').toLowerCase();
if(__cfh==='challenges.cloudflare.com'||__cfh.endsWith('.challenges.cloudflare.com')||__cfh==='cf-turnstile.com'||__cfh.endsWith('.cf-turnstile.com')||__cfh==='turnstile.cloudflare.com'||__cfh.endsWith('.turnstile.cloudflare.com'))return;
}catch(e){}
if(window.__wta_cf_compat__)return;
window.__wta_cf_compat__=1;

function _mn(fn){return fn;}

try{
Object.defineProperty(navigator,'webdriver',{
    get:_mn(function(){return false}),
    enumerable:true,configurable:true
});
}catch(e){}

try{
Object.defineProperty(document,'visibilityState',{
    get:_mn(function(){return'visible'}),
    enumerable:true,configurable:true
});
Object.defineProperty(document,'hidden',{
    get:_mn(function(){return false}),
    enumerable:true,configurable:true
});
}catch(e){}

try{
if(!navigator.plugins||navigator.plugins.length===0){
    var fakePlugin={name:'Chrome PDF Plugin',filename:'internal-pdf-viewer',description:'Portable Document Format',length:1};
    Object.defineProperty(navigator,'plugins',{
        get:_mn(function(){return[fakePlugin,fakePlugin,fakePlugin]}),
        enumerable:true,configurable:true
    });
}
}catch(e){}

try{
if(!navigator.languages||navigator.languages.length===0){
    Object.defineProperty(navigator,'languages',{
        get:_mn(function(){return['en-US','en']}),
        enumerable:true,configurable:true
    });
}
}catch(e){}

try{
if(typeof Notification!=='undefined'){
    Object.defineProperty(Notification,'permission',{
        get:_mn(function(){return'default'}),
        enumerable:true,configurable:true
    });
}
}catch(e){}

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

try{
document.hasFocus=_mn(function(){return true});
}catch(e){}

try{
if(typeof window.outerWidth==='number'&&window.outerWidth===0){
    Object.defineProperty(window,'outerWidth',{get:_mn(function(){return window.innerWidth}),configurable:true});
    Object.defineProperty(window,'outerHeight',{get:_mn(function(){return window.innerHeight}),configurable:true});
}
}catch(e){}

})();"""

}
