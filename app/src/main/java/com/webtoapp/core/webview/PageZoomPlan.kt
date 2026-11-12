package com.webtoapp.core.webview

internal data class PageZoomPlan(
    val zoomActive: Boolean,
    val initialScalePercent: Int
)

internal fun planPageZoom(pageZoomPercent: Int, initialScaleField: Int): PageZoomPlan {
    val zoom = if (pageZoomPercent > 0) pageZoomPercent else 100
    if (zoom != 100) return PageZoomPlan(zoomActive = true, initialScalePercent = zoom)
    return PageZoomPlan(zoomActive = false, initialScalePercent = if (initialScaleField > 0) initialScaleField else 0)
}

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
function meta(){return document.querySelector('meta[name="viewport" i]');}
function patched(m){
var c=(m&&m.getAttribute('content'))||'';
return c.indexOf('initial-scale='+value)!==-1;
}
function patch(m){
var parts=(m.getAttribute('content')||'').split(',');
var out=[];
var scale=false,max=false,min=false;
for(var i=0;i<parts.length;i++){
var raw=parts[i];
var kv=raw.split('=');
var key=(kv[0]||'').trim().toLowerCase();
if(!key)continue;
var val=(kv[1]||'').trim();
if(key==='initial-scale'){out.push('initial-scale='+value);scale=true;}
else if(key==='maximum-scale'){var mx=parseFloat(val);out.push('maximum-scale='+((isFinite(mx)&&mx>Z)?val:value));max=true;}
else if(key==='minimum-scale'){var mn=parseFloat(val);out.push('minimum-scale='+((isFinite(mn)&&mn<Z)?val:value));min=true;}
else out.push(raw.trim());
}
if(!scale)out.push('initial-scale='+value);
if(!max)out.push('maximum-scale=5');
if(!min)out.push('minimum-scale=0.25');
m.setAttribute('content',out.join(','));
}
function apply(){
var el=meta();
if(!el){
if(!document.head)return;
el=document.createElement('meta');
el.setAttribute('name','viewport');
el.setAttribute('content','width=device-width, initial-scale='+value);
document.head.appendChild(el);
return;
}
if(patched(el))return;
patch(el);
}
function watch(){
if(window.__wtaPageZoomWatched)return;
window.__wtaPageZoomWatched=true;
var root=document.documentElement||document;
if(typeof MutationObserver==='function'&&root){
new MutationObserver(function(){apply();})
.observe(root,{childList:true,subtree:true,attributes:true,attributeFilter:['content']});
}
}
if(window.__wtaPageZoomPercent===PCT){apply();return;}
window.__wtaPageZoomPercent=PCT;
if(document.readyState==='loading'){
watch();
document.addEventListener('DOMContentLoaded',function(){watch();apply();});
}else{
apply();
watch();
}
})();"""
