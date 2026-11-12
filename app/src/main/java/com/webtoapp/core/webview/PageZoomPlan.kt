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

internal fun pageZoomDocumentScript(pageZoomPercent: Int): String? {
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
var KEY='__wtaPageZoomBudget';
var gaveUp=false;
function meta(){return document.querySelector('meta[name="viewport" i]');}
function scaleOf(m){
var parts=((m&&m.getAttribute('content'))||'').split(',');
for(var i=0;i<parts.length;i++){
var kv=parts[i].split('=');
if((kv[0]||'').trim().toLowerCase()==='initial-scale')return parseFloat((kv[1]||'').trim());
}
return NaN;
}
function stop(){
gaveUp=true;
if(window.__wtaPageZoomMo){window.__wtaPageZoomMo.disconnect();window.__wtaPageZoomMo=null;}
}
function budgetOk(){
try{
var s=window.sessionStorage;
if(!s)return true;
var now=Date.now();
var raw=s.getItem(KEY);
var n=0,t=now;
if(raw){var p=raw.split(':');n=parseInt(p[0],10)||0;t=parseInt(p[1],10)||now;}
if(now-t>10000){n=0;t=now;}
if(n>=8){stop();return false;}
s.setItem(KEY,(n+1)+':'+t);
return true;
}catch(e){return true;}
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
if(gaveUp)return;
var el=meta();
if(!el){
if(!document.head)return;
if(!budgetOk())return;
el=document.createElement('meta');
el.setAttribute('name','viewport');
el.setAttribute('content','width=device-width, initial-scale='+value);
document.head.appendChild(el);
return;
}
if(scaleOf(el)===Z)return;
if(!budgetOk())return;
patch(el);
}
function watch(){
if(window.__wtaPageZoomWatched)return;
window.__wtaPageZoomWatched=true;
var root=document.documentElement||document;
if(typeof MutationObserver==='function'&&root){
window.__wtaPageZoomMo=new MutationObserver(function(){apply();});
window.__wtaPageZoomMo.observe(root,{childList:true,subtree:true,attributes:true,attributeFilter:['content']});
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