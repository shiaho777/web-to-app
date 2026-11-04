package com.webtoapp.core.appearance

import android.content.res.Resources
import com.webtoapp.R
import com.webtoapp.core.logging.AppLogger

object BrowserDisguiseJsGenerator {

    private const val TAG = "BrowserDisguiseJs"

    private fun readJs(resources: Resources, resId: Int): String =
        resources.openRawResource(resId).bufferedReader().use { it.readText() }

    fun generate(config: BrowserDisguiseConfig, resources: Resources): String {
        if (!config.enabled) return ""

        val sb = StringBuilder(32768)
        sb.append("(function(){'use strict';\n")
        sb.append("if(window.__wta_browser_disguise__)return;\n")
        sb.append("window.__wta_browser_disguise__=1;\n\n")

        sb.append(readJs(resources, R.raw.wta_disguise_prototype_guard))

        sb.append(readJs(resources, R.raw.wta_disguise_surface_api_core))

        sb.append(generateCanvasNoiseJs(config.canvasNoiseIntensity))
        sb.append(generateWebGLSpoofJs(config.webglRenderer))
        sb.append(readJs(resources, R.raw.wta_disguise_audio_noise))
        sb.append(generateScreenSpoofJs(config.screenProfile))
        sb.append(readJs(resources, R.raw.wta_disguise_client_rects_noise))
        sb.append(generateTimezoneSpoofJs(config.targetTimezone))
        sb.append(generateLanguageSpoofJs(config.targetLanguages))
        sb.append(generatePlatformSpoofJs(config.targetPlatform))
        sb.append(generateHardwareConcurrencyJs(config.targetConcurrency))
        sb.append(generateDeviceMemoryJs(config.targetMemoryGB))
        sb.append(generateUserAgentDataJs(config.targetPlatform))

        sb.append(readJs(resources, R.raw.wta_disguise_deep))

        sb.append(readJs(resources, R.raw.wta_disguise_iframe_propagation))
        sb.append(readJs(resources, R.raw.wta_disguise_final_seal))

        sb.append("\n})();")

        val js = sb.toString()
        AppLogger.d(TAG, "Generated browser surface script: ${js.length} chars")
        return js
    }



    private fun generateCanvasNoiseJs(intensity: Float): String = """
try{
var _ctdu=HTMLCanvasElement.prototype.toDataURL;
var _ctb=HTMLCanvasElement.prototype.toBlob;
var _cgid=CanvasRenderingContext2D.prototype.getImageData;
var _seed=Math.random()*999|0;
function _cn(c){
    try{
        var ctx=c.getContext('2d');if(!ctx)return;
        var w=c.width,h=c.height;if(!w||!h||w>4096||h>4096)return;
        var d=_cgid.call(ctx,0,0,Math.min(w,256),Math.min(h,64));
        var p=d.data,s=_seed;
        for(var i=0;i<p.length;i+=4){
            s=(s*16807+1)&0x7fffffff;
            p[i]=(p[i]+((s%5)-2)*$intensity*255)&255;
            s=(s*16807+1)&0x7fffffff;
            p[i+1]=(p[i+1]+((s%5)-2)*$intensity*255)&255;
        }
        ctx.putImageData(d,0,0);
    }catch(e){}
}
HTMLCanvasElement.prototype.toDataURL=_mn(function(){_cn(this);return _ctdu.apply(this,arguments)});
HTMLCanvasElement.prototype.toBlob=_mn(function(){_cn(this);return _ctb.apply(this,arguments)});
CanvasRenderingContext2D.prototype.getImageData=_mn(function(){
    var r=_cgid.apply(this,arguments);var d=r.data;
    for(var i=0;i<d.length;i+=4){d[i]=(d[i]+((Math.random()*3-1)*$intensity*255|0))&255;}
    return r;
});
}catch(e){}
"""

    private fun generateWebGLSpoofJs(renderer: WebGLRenderer): String = """
try{
var VEND='${renderer.vendor}',REND='${renderer.renderer}';
var _gp1=WebGLRenderingContext.prototype.getParameter;
var _gp2=typeof WebGL2RenderingContext!=='undefined'?WebGL2RenderingContext.prototype.getParameter:null;
function _wgp(orig,ctx,pname){
    if(pname===0x9245||pname===0x1F00)return VEND;
    if(pname===0x9246||pname===0x1F01)return REND;
    if(pname===0x8B8C)return 16384; // MAX_VARYING_VECTORS
    return orig.call(ctx,pname);
}
WebGLRenderingContext.prototype.getParameter=_mn(function(p){return _wgp(_gp1,this,p)});
if(_gp2)WebGL2RenderingContext.prototype.getParameter=_mn(function(p){return _wgp(_gp2,this,p)});
var _ge1=WebGLRenderingContext.prototype.getExtension;
WebGLRenderingContext.prototype.getExtension=_mn(function(name){
    if(name==='WEBGL_debug_renderer_info')return{UNMASKED_VENDOR_WEBGL:0x9245,UNMASKED_RENDERER_WEBGL:0x9246};
    return _ge1.call(this,name);
});
if(typeof WebGL2RenderingContext!=='undefined'){
    var _ge2=WebGL2RenderingContext.prototype.getExtension;
    WebGL2RenderingContext.prototype.getExtension=_mn(function(name){
        if(name==='WEBGL_debug_renderer_info')return{UNMASKED_VENDOR_WEBGL:0x9245,UNMASKED_RENDERER_WEBGL:0x9246};
        return _ge2.call(this,name);
    });
}
var _gspf=WebGLRenderingContext.prototype.getShaderPrecisionFormat;
WebGLRenderingContext.prototype.getShaderPrecisionFormat=_mn(function(){
    var r=_gspf.apply(this,arguments);
    return{rangeMin:r?r.rangeMin:127,rangeMax:r?r.rangeMax:127,precision:r?r.precision:23};
});
}catch(e){}
"""


    private fun generateScreenSpoofJs(profile: ScreenProfile): String = """
try{
Object.defineProperty(screen,'width',{get:_mn(function(){return ${profile.width}}),enumerable:true,configurable:true});
Object.defineProperty(screen,'height',{get:_mn(function(){return ${profile.height}}),enumerable:true,configurable:true});
Object.defineProperty(screen,'availWidth',{get:_mn(function(){return ${profile.width}}),enumerable:true,configurable:true});
Object.defineProperty(screen,'availHeight',{get:_mn(function(){return ${profile.height - 40}}),enumerable:true,configurable:true});
Object.defineProperty(screen,'colorDepth',{get:_mn(function(){return ${profile.colorDepth}}),enumerable:true,configurable:true});
Object.defineProperty(screen,'pixelDepth',{get:_mn(function(){return ${profile.colorDepth}}),enumerable:true,configurable:true});
Object.defineProperty(window,'devicePixelRatio',{get:_mn(function(){return ${profile.pixelRatio}}),enumerable:true,configurable:true});
Object.defineProperty(screen,'orientation',{get:_mn(function(){return{angle:0,type:'landscape-primary',onchange:null,addEventListener:_mn(function(){}),removeEventListener:_mn(function(){})}}),enumerable:true,configurable:true});
}catch(e){}
"""


    private fun generateTimezoneSpoofJs(timezone: String): String = """
try{
var _idtf=Intl.DateTimeFormat;
Intl.DateTimeFormat=_mn(function DateTimeFormat(){
    var a=Array.from(arguments);if(!a[1])a[1]={};a[1].timeZone='$timezone';
    return new _idtf(a[0],a[1]);
});
Intl.DateTimeFormat.prototype=_idtf.prototype;
Intl.DateTimeFormat.supportedLocalesOf=_idtf.supportedLocalesOf;
var _iro=_idtf.prototype.resolvedOptions;
_idtf.prototype.resolvedOptions=_mn(function(){var r=_iro.call(this);r.timeZone='$timezone';return r});
var _tzMap={'America/New_York':300,'America/Los_Angeles':480,'America/Chicago':360,'America/Denver':420,'Europe/London':0,'Europe/Paris':-60,'Europe/Berlin':-60,'Asia/Tokyo':-540,'Asia/Shanghai':-480,'Asia/Kolkata':-330,'Asia/Seoul':-540,'Australia/Sydney':-600,'Pacific/Auckland':-720};
var _tzo=_tzMap['$timezone'];if(_tzo===undefined)_tzo=new Date().getTimezoneOffset();
Date.prototype.getTimezoneOffset=_mn(function(){return _tzo});
var _dts=Date.prototype.toString;
Date.prototype.toString=_mn(function(){
    var s=_dts.call(this);
    var tz=Intl.DateTimeFormat('en-US',{timeZone:'$timezone',timeZoneName:'short'}).format(this);
    return s;
});
}catch(e){}
"""

    private fun generateLanguageSpoofJs(languages: List<String>): String {
        val langArr = languages.joinToString(",") { "'$it'" }
        val primary = languages.firstOrNull() ?: "en-US"
        return """
try{
Object.defineProperty(navigator,'language',{get:_mn(function(){return '$primary'}),enumerable:true,configurable:true});
Object.defineProperty(navigator,'languages',{get:_mn(function(){return Object.freeze([$langArr])}),enumerable:true,configurable:true});
}catch(e){}
"""
    }

    private fun generatePlatformSpoofJs(platform: String): String = """
try{
Object.defineProperty(navigator,'platform',{get:_mn(function(){return '$platform'}),enumerable:true,configurable:true});
Object.defineProperty(navigator,'oscpu',{get:_mn(function(){return undefined}),enumerable:true,configurable:true});
Object.defineProperty(navigator,'maxTouchPoints',{get:_mn(function(){return ${if (platform.contains("Win") || platform.contains("Mac")) "0" else "1"}}),enumerable:true,configurable:true});
}catch(e){}
"""

    private fun generateHardwareConcurrencyJs(cores: Int): String = """
try{Object.defineProperty(navigator,'hardwareConcurrency',{get:_mn(function(){return $cores}),enumerable:true,configurable:true});}catch(e){}
"""

    private fun generateDeviceMemoryJs(memoryGB: Int): String = """
try{Object.defineProperty(navigator,'deviceMemory',{get:_mn(function(){return $memoryGB}),enumerable:true,configurable:true});}catch(e){}
"""

    private fun generateUserAgentDataJs(platform: String): String {
        val (pName, pVersion) = when {
            platform.contains("Win") -> "Windows" to "15.0.0"
            platform.contains("Mac") -> "macOS" to "14.5.0"
            platform.contains("Linux") -> "Linux" to "6.5.0"
            else -> "Windows" to "15.0.0"
        }
        return """
try{
var _uad={
    brands:Object.freeze([
        Object.freeze({brand:'Chromium',version:'131'}),
        Object.freeze({brand:'Google Chrome',version:'131'}),
        Object.freeze({brand:'Not_A Brand',version:'24'})
    ]),
    mobile:false,
    platform:'$pName',
    getHighEntropyValues:_mn(function(hints){
        return Promise.resolve({
            architecture:'x86',
            bitness:'64',
            brands:[{brand:'Chromium',version:'131.0.0.0'},{brand:'Google Chrome',version:'131.0.0.0'},{brand:'Not_A Brand',version:'24.0.0.0'}],
            fullVersionList:[{brand:'Chromium',version:'131.0.6778.139'},{brand:'Google Chrome',version:'131.0.6778.139'},{brand:'Not_A Brand',version:'24.0.0.0'}],
            mobile:false,
            model:'',
            platform:'$pName',
            platformVersion:'$pVersion',
            uaFullVersion:'131.0.6778.139',
            wow64:false,
            formFactor:''
        });
    }),
    toJSON:_mn(function(){return{brands:this.brands,mobile:this.mobile,platform:this.platform}})
};
Object.defineProperty(navigator,'userAgentData',{get:_mn(function(){return _uad}),enumerable:true,configurable:true});
}catch(e){}
"""
    }



}
