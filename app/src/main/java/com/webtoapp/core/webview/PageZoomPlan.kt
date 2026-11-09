package com.webtoapp.core.webview

/**
 * Whole-page zoom for [WebViewManager.configureWebView] (#654, #1264).
 *
 * `pageZoomPercent` is the build-time per-app zoom (100 = default). Rewriting
 * `<meta name="viewport">` reloaded the document on Android WebView whenever
 * the page restored its own meta (#1317).
 * [android.webkit.WebView.setInitialScale] cannot do this either — with
 * `useWideViewPort` a page `<meta name="viewport">` (`width=device-width`,
 * often `maximum-scale=1`) wins, and the page stays at 100% (#1264).
 *
 * Zooming out is a view-level scale, not CSS `zoom`. `zoom` is a post-layout
 * transform: a `height:100vh` container stays one viewport tall and is then
 * painted at 90%, which leaves a band of the window unpainted (#1508). The
 * host lays the WebView out at `1/scale` and scales it back down, so the CSS
 * viewport itself grows and `100vh` still means "one screen". Zooming in
 * keeps CSS `zoom`: the page overflows instead of leaving a gap, and a
 * re-layout renders text sharper than an upscaled view layer.
 *
 * Pure logic, no Android dependencies: unit-tested on plain JVM.
 */
internal data class PageZoomPlan(
    /** True when an explicit non-100 zoom is configured. */
    val zoomActive: Boolean,
    /**
     * Percent to apply. 0 on the default path means "leave WebView scale alone"
     * unless [initialScaleField] itself is non-zero (legacy `initialScale`).
     */
    val initialScalePercent: Int,
    /** Factor the host applies to the surface view. 1 when the view is not scaled. */
    val viewScale: Float,
    /** True when [pageZoomDocumentScript] should install CSS `zoom` on the root. */
    val cssZoomActive: Boolean
)

internal fun planPageZoom(pageZoomPercent: Int, initialScaleField: Int): PageZoomPlan {
    // 0 = legacy data without the field; treat as 100 (default).
    val zoom = if (pageZoomPercent > 0) pageZoomPercent else 100
    return when {
        zoom < 100 -> PageZoomPlan(
            zoomActive = true,
            initialScalePercent = zoom,
            viewScale = zoom / 100f,
            cssZoomActive = false
        )
        zoom > 100 -> PageZoomPlan(
            zoomActive = true,
            initialScalePercent = zoom,
            viewScale = 1f,
            cssZoomActive = true
        )
        // Default path preserves the dormant `initialScale` field semantics.
        else -> PageZoomPlan(
            zoomActive = false,
            initialScalePercent = if (initialScaleField > 0) initialScaleField else 0,
            viewScale = 1f,
            cssZoomActive = false
        )
    }
}

/**
 * Document-start script that scales the top frame with CSS `zoom`.
 * Null when zoom is the default (100, or legacy 0), or when the host owns the
 * zoom through [PageZoomPlan.viewScale]. Idempotent across document-start plus
 * later `evaluateJavascript` calls. Ignores subframes.
 *
 * Rewriting `<meta name="viewport">` made Android WebView reload the document.
 * Dashboard apps such as OpenChamber put their own viewport back, the script
 * wrote it again, and the page refreshed in a loop (#1264, #1317). CSS zoom
 * scales text, layout, images, and canvas without a navigation.
 *
 * Pinch is owned by `WebViewConfig.zoomEnabled` (`setSupportZoom`). [pinchEnabled]
 * and [nativeCssWidth] stay in the signature so existing call sites compile;
 * the script does not read them.
 */
@Suppress("UNUSED_PARAMETER")
internal fun pageZoomDocumentScript(
    pageZoomPercent: Int,
    pinchEnabled: Boolean,
    nativeCssWidth: Int = 0,
): String? {
    val plan = planPageZoom(pageZoomPercent, 0)
    if (!plan.cssZoomActive) return null
    return PAGE_ZOOM_JS.replace("PCT_PLACEHOLDER", plan.initialScalePercent.toString())
}

private const val PAGE_ZOOM_JS = """(function(){
'use strict';
try{if(window.top!==window)return;}catch(e){return;}
var PCT=PCT_PLACEHOLDER;
var Z=PCT/100;
var value=String(Z);
function root(){return document.documentElement;}
function apply(){
var el=root();
if(!el)return;
if(el.style.zoom===value)return;
el.style.zoom=value;
}
if(window.__wtaPageZoomPercent===PCT){
apply();
return;
}
window.__wtaPageZoomPercent=PCT;
apply();
if(document.readyState==='loading'){
document.addEventListener('DOMContentLoaded',function(){apply();});
}
if(typeof MutationObserver==='function'&&root()&&!window.__wtaPageZoomMo){
var writing=false;
window.__wtaPageZoomMo=new MutationObserver(function(){
if(writing)return;
var el=root();
if(!el||el.style.zoom===value)return;
writing=true;
el.style.zoom=value;
writing=false;
});
window.__wtaPageZoomMo.observe(root(),{attributes:true,attributeFilter:['style']});
}
})();"""
