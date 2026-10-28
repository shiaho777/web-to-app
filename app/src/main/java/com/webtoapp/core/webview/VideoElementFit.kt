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
function inlinePx(el,prop){
if(!el||!el.style)return 0;
var v=el.style[prop];
if(!v||v.indexOf('%')>=0)return 0;
var n=parseFloat(v);
return n>=8?Math.round(n):0;
}
function blocked(video){
try{if(video.webkitDisplayingFullscreen)return true;}catch(e){}
var fs=document.fullscreenElement||document.webkitFullscreenElement;
return !!(fs&&(fs===video||(fs.contains&&fs.contains(video))));
}
function ancestorBox(video,laidW,laidH,vw){
var w=0,h=0,node=video.parentElement,guard=0;
while(node&&node!==document.body&&node!==document.documentElement&&guard++<8){
if(!w)w=inlinePx(node,'width');
if(!h)h=inlinePx(node,'height');
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
var selfW=inlinePx(video,'width'),selfH=inlinePx(video,'height');
var capW=vw,hasW=0,hasH=0,capH=0;
if(maxW>=8){capW=Math.min(capW,maxW);hasW=1;}
if(cssW>=8){capW=Math.min(capW,cssW);hasW=1;}
if(selfW>=8){capW=Math.min(capW,selfW);hasW=1;}
if(anc.w>=8){capW=Math.min(capW,anc.w);hasW=1;}
if(!(capW>1))return;
if(maxH>=8){capH=maxH;hasH=1;}
if(cssH>=8){capH=hasH?Math.min(capH,cssH):cssH;hasH=1;}
if(selfH>=8){capH=hasH?Math.min(capH,selfH):selfH;hasH=1;}
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
style.textContent='video{max-width:100%;min-width:0;min-height:0;}';
root.appendChild(style);
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
