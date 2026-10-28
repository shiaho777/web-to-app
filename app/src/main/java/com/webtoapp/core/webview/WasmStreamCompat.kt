package com.webtoapp.core.webview

/**
 * Keeps a large WebAssembly download moving inside System WebView.
 *
 * Godot's web loader wraps the wasm response in a progress stream and then
 * reads only `response.clone().body`. The unread branch of that tee applies
 * backpressure, so a multi-megabyte `.wasm` stops mid-transfer and the splash
 * never finishes. Draining the unread branch lets the clone keep flowing.
 *
 * The same script retries a WebGL context that the GPU rejected only because
 * `failIfMajorPerformanceCaveat` was set. Godot's feature check uses a bare
 * `getContext("webgl2")`; the real context comes later with that flag.
 */
internal object WasmStreamCompat {
    const val SCRIPT: String = """(function(){
'use strict';
if(window.__wtaWasmStream)return;
window.__wtaWasmStream=1;
var origClone=Response.prototype.clone;
Response.prototype.clone=function(){
var cloned=origClone.call(this);
try{
var ct=(this.headers&&this.headers.get('content-type'))||'';
var url=this.url||'';
if(ct.indexOf('wasm')<0&&!/\.wasm(\?|#|$)/i.test(url))return cloned;
var body=this.body;
if(body&&!body.locked&&body.getReader){
var reader=body.getReader();
var pump=function(){reader.read().then(function(r){if(!r.done)return pump();}).catch(function(){});};
pump();
}
}catch(e){}
return cloned;
};
var origGetContext=HTMLCanvasElement.prototype.getContext;
HTMLCanvasElement.prototype.getContext=function(type,attrs){
var ctx=origGetContext.call(this,type,attrs);
if(ctx||(type!=='webgl'&&type!=='webgl2')||!attrs||!attrs.failIfMajorPerformanceCaveat)return ctx;
var copy={};
for(var k in attrs)if(Object.prototype.hasOwnProperty.call(attrs,k))copy[k]=attrs[k];
copy.failIfMajorPerformanceCaveat=false;
return origGetContext.call(this,type,copy);
};
})();"""
}
