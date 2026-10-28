package com.webtoapp.core.webview

import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Box to pin onto a video that System WebView laid out larger than the page's
 * CSS cap. Null means the current layout already fits and must be left alone.
 */
internal data class VideoFitBox(val width: Int, val height: Int)

/**
 * Inputs are CSS pixels. A zero CSS length means "no independent cap"
 * (auto, a percentage that resolved to the overflowing size, or unknown).
 * [parentHeight] is set only when an ancestor states an explicit height.
 * [viewportHeight] is not a cap: a tall video the page meant to scroll stays
 * tall. [viewportWidth] is a cap so a video cannot be wider than the screen.
 */
internal data class VideoFitInput(
    val laidOutWidth: Int,
    val laidOutHeight: Int,
    val intrinsicWidth: Int,
    val intrinsicHeight: Int,
    val cssWidth: Int = 0,
    val cssHeight: Int = 0,
    val cssMaxWidth: Int = 0,
    val cssMaxHeight: Int = 0,
    val parentWidth: Int = 0,
    val parentHeight: Int = 0,
    val viewportWidth: Int,
    val fullscreen: Boolean = false,
)

/**
 * System WebView sizes a `<video>` from the media's intrinsic pixels and
 * drops the CSS box Chrome keeps (a chat bubble's width and height, or
 * `max-width`). Shrink only when the laid-out box is actually outside that
 * cap. A player that already fits, including a full-width one, is unchanged.
 * When the page caps both axes, keep that box and let the page's `object-fit`
 * crop it. A height-only cap keeps the media aspect ratio. The viewport
 * width clamps either result so the video cannot be wider than the screen.
 *
 * getComputedStyle often reports that intrinsic box, so a real `140px` cap
 * and a cyclic `max-width: 100%` look the same. The script therefore also
 * reads specified `px` / `rem` / `em` / `vw` / `vh` lengths from the page's
 * rules and from ancestors. A chat bubble that grew with the video, while
 * still fitting on screen, is pinned back to that length. A length the page
 * did not set is not a cap.
 */
internal fun fittedVideoBox(input: VideoFitInput): VideoFitBox? {
    if (input.fullscreen) return null
    if (input.intrinsicWidth <= 0 || input.intrinsicHeight <= 0) return null
    if (input.laidOutWidth <= 1 || input.laidOutHeight <= 1) return null
    if (input.viewportWidth <= 1) return null

    var capW = input.viewportWidth
    var hasPageWidth = false
    if (input.cssMaxWidth >= 8) {
        capW = min(capW, input.cssMaxWidth)
        hasPageWidth = true
    }
    if (input.cssWidth >= 8) {
        capW = min(capW, input.cssWidth)
        hasPageWidth = true
    }
    if (input.parentWidth >= 8) {
        capW = min(capW, input.parentWidth)
        hasPageWidth = true
    }
    if (capW <= 1) return null

    var capH = Int.MAX_VALUE
    var hasHeight = false
    if (input.cssMaxHeight >= 8) {
        capH = min(capH, input.cssMaxHeight)
        hasHeight = true
    }
    if (input.cssHeight >= 8) {
        capH = min(capH, input.cssHeight)
        hasHeight = true
    }
    if (input.parentHeight >= 8) {
        capH = min(capH, input.parentHeight)
        hasHeight = true
    }

    val widthOverflow = input.laidOutWidth > capW + 1
    val heightOverflow = hasHeight && input.laidOutHeight > capH + 1
    if (!widthOverflow && !heightOverflow) return null

    val box = if (hasPageWidth && hasHeight) {
        VideoFitBox(capW, capH)
    } else {
        val ratio = input.intrinsicHeight.toDouble() / input.intrinsicWidth.toDouble()
        var w = capW
        var h = (w * ratio).roundToInt().coerceAtLeast(1)
        if (hasHeight && h > capH) {
            h = capH
            w = (h / ratio).roundToInt().coerceAtLeast(1)
            if (w > capW) w = capW
        }
        VideoFitBox(w, h)
    }
    if (box.width >= input.laidOutWidth - 1 && box.height >= input.laidOutHeight - 1) return null
    if (box.width <= 1 || box.height <= 1) return null
    return box
}

/**
 * Document-start script for System WebView. Gecko already honors the CSS box,
 * so it does not receive this. The script implements [fittedVideoBox].
 * The helper sheet only sets `min-width` and `min-height`, and it is inserted
 * first so it cannot override a page `max-width`.
 */
internal object VideoElementFit {
    const val SCRIPT: String = """(function(){
'use strict';
if(window.__wtaVideoFit)return;
window.__wtaVideoFit=1;
function px(v,laid){
if(!v||v==='none'||v==='auto')return 0;
var n=parseFloat(v);
if(!(n>=8))return 0;
n=Math.round(n);
if(laid&&n>=laid-1)return 0;
return n;
}
function lengthPx(el,raw){
if(!raw)return 0;
raw=String(raw).trim().toLowerCase();
if(!raw||raw==='none'||raw==='auto'||raw==='initial'||raw==='inherit'||raw==='unset')return 0;
if(raw==='fit-content'||raw==='max-content'||raw==='min-content')return 0;
var m=raw.match(/^(-?\d+(?:\.\d+)?)(px|rem|em|vw|vh)$/);
if(!m)return 0;
var n=parseFloat(m[1]);
if(!(n>0))return 0;
var unit=m[2];
if(unit==='rem')n*=parseFloat(getComputedStyle(document.documentElement).fontSize)||16;
else if(unit==='em')n*=parseFloat(getComputedStyle(el).fontSize)||16;
else if(unit==='vw')n*=(document.documentElement.clientWidth||window.innerWidth||0)/100;
else if(unit==='vh')n*=(window.innerHeight||document.documentElement.clientHeight||0)/100;
n=Math.round(n);
return n>=8?n:0;
}
function scanRules(el,prop,rules,sink){
if(!rules)return;
for(var j=0;j<rules.length;j++){
var rule=rules[j];
if(!rule)continue;
var t=rule.type||0;
if(t===3&&rule.styleSheet){
try{scanRules(el,prop,rule.styleSheet.cssRules,sink);}catch(e){}
continue;
}
if((t===4||t===12)&&rule.cssRules){
var ok=1;
try{
if(t===4&&rule.conditionText&&window.matchMedia&&!matchMedia(rule.conditionText).matches)ok=0;
if(t===12&&window.CSS&&CSS.supports&&rule.conditionText&&!CSS.supports(rule.conditionText))ok=0;
}catch(e){ok=0;}
if(ok)scanRules(el,prop,rule.cssRules,sink);
continue;
}
if(!rule.selectorText||!rule.style||!el.matches)continue;
try{if(!el.matches(rule.selectorText))continue;}catch(e){continue;}
var v=rule.style.getPropertyValue(prop);
if(!v)continue;
sink(lengthPx(el,v),rule.style.getPropertyPriority(prop)==='important');
}
}
function sheetList(){
var list=[],ss=document.styleSheets,i;
if(ss)for(i=0;i<ss.length;i++)list.push(ss[i]);
var adopted=document.adoptedStyleSheets;
if(adopted)for(i=0;i<adopted.length;i++)list.push(adopted[i]);
return list;
}
function specifiedLength(el,prop){
if(!el)return 0;
var normal=0,important=0,hasImp=0;
function sink(n,imp){if(imp){hasImp=1;important=n;}else normal=n;}
var sheets=sheetList();
for(var i=0;i<sheets.length;i++){
try{scanRules(el,prop,sheets[i].cssRules,sink);}catch(e){}
}
if(el.style&&el.style.getPropertyValue){
var iv=el.style.getPropertyValue(prop);
if(iv){
var n=lengthPx(el,iv);
if(el.style.getPropertyPriority(prop)==='important'){hasImp=1;important=n;}
else normal=n;
}
}
return hasImp?important:normal;
}
function blocked(video){
try{if(video.webkitDisplayingFullscreen)return true;}catch(e){}
var fs=document.fullscreenElement||document.webkitFullscreenElement;
return !!(fs&&(fs===video||(fs.contains&&fs.contains(video))));
}
function ancestorBox(video,laidW,laidH,vw){
var w=0,h=0,node=video.parentElement,guard=0;
while(node&&node!==document.body&&node!==document.documentElement&&guard++<8){
if(!w){
var sw=specifiedLength(node,'width');
if(!(sw>=8))sw=specifiedLength(node,'max-width');
if(sw>=8&&laidW-sw>=8)w=sw;
}
if(!h){
var sh=specifiedLength(node,'height');
if(!(sh>=8))sh=specifiedLength(node,'max-height');
if(sh>=8&&laidH-sh>=8)h=sh;
}
if(!w){
var cw=node.clientWidth||0;
if(cw>=8&&laidW-cw>=8)w=Math.round(cw);
}
if(!h){
var ch=node.clientHeight||0;
if(ch>=8&&laidH-ch>=8)h=Math.round(ch);
}
if(w&&h)break;
node=node.parentElement;
}
if(w>=8||laidW<=vw+1)return {w:w,h:h};
var prev=video.style.cssText;
try{
video.style.setProperty('position','absolute','important');
video.style.setProperty('width','0','important');
video.style.setProperty('height','0','important');
video.style.setProperty('min-width','0','important');
video.style.setProperty('min-height','0','important');
video.style.setProperty('max-width','none','important');
node=video.parentElement;guard=0;
while(node&&node!==document.documentElement&&guard++<8){
var cw=node.clientWidth||0;
if(cw>=8){w=Math.round(cw);break;}
node=node.parentElement;
}
}finally{video.style.cssText=prev;}
return {w:w,h:h};
}
function clearFit(video){
if(!video.getAttribute('data-wta-fit'))return;
video.style.width='';
video.style.height='';
var aw=video.getAttribute('data-wta-aw');
var ah=video.getAttribute('data-wta-ah');
if(!aw)video.removeAttribute('width');else video.setAttribute('width',aw);
if(!ah)video.removeAttribute('height');else video.setAttribute('height',ah);
video.removeAttribute('data-wta-fit');
video.removeAttribute('data-wta-aw');
video.removeAttribute('data-wta-ah');
}
function fit(video){
if(!video||video.nodeName!=='VIDEO')return;
if(blocked(video)){clearFit(video);return;}
var iw=video.videoWidth||0,ih=video.videoHeight||0;
if(iw<1||ih<1)return;
var laidW=video.offsetWidth||0,laidH=video.offsetHeight||0;
if(laidW<=1||laidH<=1)return;
var vw=document.documentElement.clientWidth||window.innerWidth||0;
if(vw<=1)return;
var cs=getComputedStyle(video);
var cssW=px(cs.width,laidW),cssH=px(cs.height,laidH);
var maxW=px(cs.maxWidth,laidW),maxH=px(cs.maxHeight,laidH);
var anc=ancestorBox(video,laidW,laidH,vw);
var specW=specifiedLength(video,'width');
var specH=specifiedLength(video,'height');
var specMaxW=specifiedLength(video,'max-width');
var specMaxH=specifiedLength(video,'max-height');
var capW=vw,hasW=0,hasH=0,capH=0;
if(maxW>=8){capW=Math.min(capW,maxW);hasW=1;}
if(cssW>=8){capW=Math.min(capW,cssW);hasW=1;}
if(specMaxW>=8&&laidW-specMaxW>=8){capW=Math.min(capW,specMaxW);hasW=1;}
if(specW>=8&&laidW-specW>=8){capW=Math.min(capW,specW);hasW=1;}
if(anc.w>=8){capW=Math.min(capW,anc.w);hasW=1;}
if(!(capW>1))return;
if(maxH>=8){capH=maxH;hasH=1;}
if(cssH>=8){capH=hasH?Math.min(capH,cssH):cssH;hasH=1;}
if(specMaxH>=8&&laidH-specMaxH>=8){capH=hasH?Math.min(capH,specMaxH):specMaxH;hasH=1;}
if(specH>=8&&laidH-specH>=8){capH=hasH?Math.min(capH,specH):specH;hasH=1;}
if(anc.h>=8){capH=hasH?Math.min(capH,anc.h):anc.h;hasH=1;}
var widthOverflow=laidW>capW+1;
var heightOverflow=!!(hasH&&laidH>capH+1);
if(!widthOverflow&&!heightOverflow)return;
var tw,th;
if(hasW&&hasH){tw=capW;th=capH;}
else{
var ratio=ih/iw;
tw=capW;th=Math.max(1,Math.round(tw*ratio));
if(hasH&&th>capH){th=capH;tw=Math.max(1,Math.round(th/ratio));if(tw>capW)tw=capW;}
}
if(tw>=laidW-1&&th>=laidH-1)return;
if(tw<=1||th<=1)return;
if(!video.getAttribute('data-wta-fit')){
if(video.hasAttribute('width'))video.setAttribute('data-wta-aw',video.getAttribute('width'));
else video.setAttribute('data-wta-aw','');
if(video.hasAttribute('height'))video.setAttribute('data-wta-ah',video.getAttribute('height'));
else video.setAttribute('data-wta-ah','');
}
video.setAttribute('width',String(tw));
video.setAttribute('height',String(th));
video.style.width=tw+'px';
video.style.height=th+'px';
video.setAttribute('data-wta-fit',tw+'x'+th);
}
function arm(video){
if(!video||video.nodeName!=='VIDEO'||video.__wtaArmed)return;
video.__wtaArmed=1;
video.addEventListener('loadedmetadata',function(){fit(video);});
var onResize=function(){fit(video);};
video.addEventListener('resize',onResize);
video.addEventListener('loadeddata',onResize);
if(video.videoWidth)fit(video);
}
function scan(root){
if(!root)return;
if(root.nodeName==='VIDEO')arm(root);
if(root.querySelectorAll){
var list=root.querySelectorAll('video');
for(var i=0;i<list.length;i++)arm(list[i]);
}
}
function boot(){
var root=document.documentElement;
if(!root)return;
if(!document.getElementById('__wta_video_fit')){
var style=document.createElement('style');
style.id='__wta_video_fit';
style.textContent='video{min-width:0;min-height:0;}';
if(root.firstChild)root.insertBefore(style,root.firstChild);
else root.appendChild(style);
}
scan(document);
if(window.__wtaVideoMo)return;
if(typeof MutationObserver==='function'){
window.__wtaVideoMo=new MutationObserver(function(recs){
for(var i=0;i<recs.length;i++){
var nodes=recs[i].addedNodes;
for(var j=0;j<nodes.length;j++){
var n=nodes[j];
if(n&&n.nodeType===1)scan(n);
}
}
});
window.__wtaVideoMo.observe(root,{childList:true,subtree:true});
}
if(!window.__wtaVideoFs){
window.__wtaVideoFs=1;
var refit=function(){scan(document);};
document.addEventListener('fullscreenchange',refit);
document.addEventListener('webkitfullscreenchange',refit);
window.addEventListener('resize',refit);
}
}
if(document.readyState==='loading'){
document.addEventListener('DOMContentLoaded',boot);
}else boot();
})();"""
}
