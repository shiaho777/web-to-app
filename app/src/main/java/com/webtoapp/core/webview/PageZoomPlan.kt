package com.webtoapp.core.webview

import kotlin.math.roundToInt

/**
 * Whole-page zoom for [WebViewManager.configureWebView] (#654, #1264).
 *
 * `pageZoomPercent` is the build-time per-app zoom (100 = default). It scales
 * text, layout, images, and canvas together with CSS `zoom` on the root
 * element. Rewriting `<meta name="viewport">` reloaded the document on
 * Android WebView whenever the page restored its own meta (#1317).
 * [android.webkit.WebView.setInitialScale] cannot do this either — with
 * `useWideViewPort` a page `<meta name="viewport">` (`width=device-width`,
 * often `maximum-scale=1`) wins, and the page stays at 100% (#1264).
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
    val initialScalePercent: Int
)

internal fun planPageZoom(pageZoomPercent: Int, initialScaleField: Int): PageZoomPlan {
    // 0 = legacy data without the field; treat as 100 (default).
    val zoom = if (pageZoomPercent > 0) pageZoomPercent else 100
    if (zoom != 100) return PageZoomPlan(zoomActive = true, initialScalePercent = zoom)
    // Default path preserves the dormant `initialScale` field semantics.
    return PageZoomPlan(zoomActive = false, initialScalePercent = if (initialScaleField > 0) initialScaleField else 0)
}

/**
 * Layout viewport for [baseCssWidth] (the WebView's CSS width before zoom).
 * Null when zoom is default or the base width is not known yet.
 *
 * `layoutWidth * scale == baseCssWidth`, so `width` and `initial-scale` agree
 * and the browser does not drop one of them to resolve a conflict.
 * Keep the JS in [pageZoomDocumentScript] on this same formula.
 */
internal data class PageZoomViewport(
    val layoutWidthPx: Int,
    val scale: Double,
)

internal fun planPageZoomViewport(baseCssWidth: Int, pageZoomPercent: Int): PageZoomViewport? {
    val plan = planPageZoom(pageZoomPercent, 0)
    if (!plan.zoomActive || baseCssWidth <= 0) return null
    val zoom = plan.initialScalePercent / 100.0
    val layout = (baseCssWidth / zoom).roundToInt().coerceAtLeast(1)
    return PageZoomViewport(layoutWidthPx = layout, scale = baseCssWidth.toDouble() / layout)
}

/**
 * Document-start script that scales the top frame with CSS `zoom`.
 * Null when zoom is the default (100, or legacy 0). Idempotent across
 * document-start plus later `evaluateJavascript` calls. Ignores subframes.
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
    if (!plan.zoomActive) return null
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
