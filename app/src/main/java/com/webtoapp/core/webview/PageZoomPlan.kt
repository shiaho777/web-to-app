package com.webtoapp.core.webview

import kotlin.math.roundToInt

/**
 * Whole-page zoom for [WebViewManager.configureWebView] (#654, #1264).
 *
 * `pageZoomPercent` is the build-time per-app zoom (100 = default). It scales
 * text, layout, images, and canvas together by rewriting the viewport: layout
 * width = unzoomed CSS width / zoom, with `initial-scale` locked to match.
 * [android.webkit.WebView.setInitialScale] cannot do this — with
 * `useWideViewPort` a page `<meta name="viewport">` (`width=device-width`,
 * often `maximum-scale=1`) wins, and the page stays at 100%.
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
 * Document-start script that locks the top frame's viewport to [pageZoomPercent].
 * Null when zoom is the default (100, or legacy 0). Idempotent across
 * document-start plus later `evaluateJavascript` calls. Ignores subframes.
 * Re-applies when the page rewrites the meta, which is what defeated
 * `setInitialScale` on dashboard apps such as OpenChamber (#1264).
 *
 * [pinchEnabled] is `WebViewConfig.zoomEnabled`: when true the user can still
 * pinch away from the initial zoom; when false the scale is locked.
 *
 * [nativeCssWidth] is the WebView's current width in CSS pixels. It matches
 * what `width=device-width` resolves to. Zero falls back to `screen.width`
 * inside the script. `innerWidth` is not used: before a viewport meta exists,
 * wide-viewport mode reports the legacy ~980px layout width.
 */
internal fun pageZoomDocumentScript(
    pageZoomPercent: Int,
    pinchEnabled: Boolean,
    nativeCssWidth: Int = 0,
): String? {
    val plan = planPageZoom(pageZoomPercent, 0)
    if (!plan.zoomActive) return null
    val native = nativeCssWidth.coerceAtLeast(0)
    return PAGE_ZOOM_JS
        .replace("PCT_PLACEHOLDER", plan.initialScalePercent.toString())
        .replace("PINCH_PLACEHOLDER", if (pinchEnabled) "1" else "0")
        .replace("NATIVE_PLACEHOLDER", native.toString())
}

private const val PAGE_ZOOM_JS = """(function(){
'use strict';
try{if(window.top!==window)return;}catch(e){return;}
var PCT=PCT_PLACEHOLDER;
var Z=PCT/100;
var PINCH=PINCH_PLACEHOLDER;
var NATIVE=NATIVE_PLACEHOLDER;
if(window.__wtaPageZoomPercent!==PCT){
window.__wtaPageZoomPercent=PCT;
window.__wtaPageZoomBase=0;
window.__wtaPageZoomScreen=0;
window.__wtaPageZoomLocked=0;
window.__wtaPageZoomBound=false;
}
var writing=false;
function baseWidth(){
if(window.__wtaPageZoomLocked&&window.__wtaPageZoomBase>=2)return window.__wtaPageZoomBase;
var native=NATIVE;
if(!(native>=2))native=window.__wtaPageZoomNativeCss||0;
if(native>=2){
window.__wtaPageZoomBase=native;
window.__wtaPageZoomScreen=window.screen.width||native;
window.__wtaPageZoomLocked=1;
return native;
}
if(window.__wtaPageZoomBase>=2)return window.__wtaPageZoomBase;
var sw=window.screen.width||0;
if(sw<2)return 0;
window.__wtaPageZoomBase=sw;
window.__wtaPageZoomScreen=sw;
return sw;
}
function contentFor(base){
var layout=Math.max(1,Math.round(base/Z));
var scale=base/layout;
function n(v){return (Math.round(v*1000)/1000).toString();}
var minS,maxS,scalable;
if(PINCH){minS=Math.min(scale,0.25);maxS=Math.max(scale,5);scalable='yes';}
else{minS=scale;maxS=scale;scalable='no';}
return 'width='+layout+',initial-scale='+n(scale)+',minimum-scale='+n(minS)+',maximum-scale='+n(maxS)+',user-scalable='+scalable;
}
function metas(){
var all=document.getElementsByTagName('meta');
var out=[];
for(var i=0;i<all.length;i++){
var name=(all[i].getAttribute('name')||'').toLowerCase();
if(name==='viewport')out.push(all[i]);
}
return out;
}
function apply(){
if(document.documentElement&&typeof MutationObserver==='function'&&!window.__wtaPageZoomRoot){
if(!window.__wtaPageZoomMo){
window.__wtaPageZoomMo=new MutationObserver(function(){if(!writing)apply();});
}
window.__wtaPageZoomMo.observe(document.documentElement,{childList:true});
window.__wtaPageZoomRoot=true;
}
var base=baseWidth();
if(!base)return;
var head=document.head||document.getElementsByTagName('head')[0];
if(!head)return;
var content=contentFor(base);
var list=metas();
var changed=false;
if(list.length===0){
var created=document.createElement('meta');
created.setAttribute('name','viewport');
writing=true;
created.setAttribute('content',content);
head.appendChild(created);
writing=false;
list=[created];
changed=true;
}else{
for(var i=0;i<list.length;i++){
if(list[i].getAttribute('content')!==content){
writing=true;
list[i].setAttribute('content',content);
writing=false;
changed=true;
}
}
}
if(!changed&&window.__wtaPageZoomBound)return;
bind(list);
window.__wtaPageZoomBound=true;
}
function bind(list){
if(typeof MutationObserver!=='function')return;
if(!window.__wtaPageZoomMo){
window.__wtaPageZoomMo=new MutationObserver(function(){if(!writing)apply();});
}else{
window.__wtaPageZoomMo.disconnect();
}
var root=document.documentElement;
if(root)window.__wtaPageZoomMo.observe(root,{childList:true});
var head=document.head;
if(head)window.__wtaPageZoomMo.observe(head,{childList:true});
for(var i=0;i<list.length;i++){
window.__wtaPageZoomMo.observe(list[i],{attributes:true,attributeFilter:['content']});
}
}
apply();
if(document.readyState==='loading'){
document.addEventListener('DOMContentLoaded',function(){apply();});
}
if(!window.__wtaPageZoomResize){
window.__wtaPageZoomResize=true;
var timer=null;
window.addEventListener('resize',function(){
clearTimeout(timer);
timer=setTimeout(function(){
if(!(window.__wtaPageZoomBase>=2)){apply();return;}
var sw=window.screen.width||0;
var prev=window.__wtaPageZoomScreen||0;
if(sw>=2&&prev>=2&&Math.abs(sw-prev)>=2){
window.__wtaPageZoomBase=Math.max(1,Math.round(window.__wtaPageZoomBase*sw/prev));
window.__wtaPageZoomScreen=sw;
window.__wtaPageZoomBound=false;
apply();
}
},200);
});
}
})();"""
