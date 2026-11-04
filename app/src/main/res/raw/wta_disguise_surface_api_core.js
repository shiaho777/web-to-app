

try{Object.defineProperty(navigator,'webdriver',{get:_mn(function(){return false}),enumerable:true,configurable:true});}catch(e){}

try{Object.defineProperty(navigator,'vendor',{get:_mn(function(){return'Google Inc.'}),enumerable:true,configurable:true});}catch(e){}

try{
Object.defineProperty(navigator,'appCodeName',{get:_mn(function(){return'Mozilla'}),enumerable:true,configurable:true});
Object.defineProperty(navigator,'product',{get:_mn(function(){return'Gecko'}),enumerable:true,configurable:true});
Object.defineProperty(navigator,'productSub',{get:_mn(function(){return'20030107'}),enumerable:true,configurable:true});
}catch(e){}

try{Object.defineProperty(navigator,'doNotTrack',{get:_mn(function(){return null}),enumerable:true,configurable:true});}catch(e){}

try{Object.defineProperty(navigator,'maxTouchPoints',{get:_mn(function(){return 0}),enumerable:true,configurable:true});}catch(e){}

try{Object.defineProperty(navigator,'pdfViewerEnabled',{get:_mn(function(){return true}),enumerable:true,configurable:true});}catch(e){}

try{Object.defineProperty(navigator,'cookieEnabled',{get:_mn(function(){return true}),enumerable:true,configurable:true});}catch(e){}

try{Object.defineProperty(navigator,'onLine',{get:_mn(function(){return true}),enumerable:true,configurable:true});}catch(e){}

try{navigator.javaEnabled=_mn(function(){return false});}catch(e){}

try{Object.defineProperty(navigator,'globalPrivacyControl',{get:_mn(function(){return false}),enumerable:true,configurable:true});}catch(e){}

try{
if(!window.chrome)window.chrome={};
if(!window.chrome.runtime)window.chrome.runtime={
    OnInstalledReason:{CHROME_UPDATE:'chrome_update',INSTALL:'install',SHARED_MODULE_UPDATE:'shared_module_update',UPDATE:'update'},
    OnRestartRequiredReason:{APP_UPDATE:'app_update',OS_UPDATE:'os_update',PERIODIC:'periodic'},
    PlatformArch:{ARM:'arm',ARM64:'arm64',MIPS:'mips',MIPS64:'mips64',X86_32:'x86-32',X86_64:'x86-64'},
    PlatformNaclArch:{ARM:'arm',MIPS:'mips',MIPS64:'mips64',X86_32:'x86-32',X86_64:'x86-64'},
    PlatformOs:{ANDROID:'android',CROS:'cros',FUCHSIA:'fuchsia',LINUX:'linux',MAC:'mac',OPENBSD:'openbsd',WIN:'win'},
    RequestUpdateCheckStatus:{NO_UPDATE:'no_update',THROTTLED:'throttled',UPDATE_AVAILABLE:'update_available'},
    connect:_mn(function(e){return{name:'',onDisconnect:{addListener:function(){},removeListener:function(){},hasListener:function(){return false}},onMessage:{addListener:function(){},removeListener:function(){},hasListener:function(){return false}},postMessage:function(){},disconnect:function(){}};}),
    sendMessage:_mn(function(){}),
    id:undefined,
    getManifest:_mn(function(){return{}}),
    getURL:_mn(function(p){return'';}),
    getPlatformInfo:_mn(function(cb){if(cb)cb({os:'win',arch:'x86-64',nacl_arch:'x86-64'})}),
    getBackgroundPage:_mn(function(){}),
    setUninstallURL:_mn(function(){}),
    requestUpdateCheck:_mn(function(cb){if(cb)cb('no_update')})
};
if(!window.chrome.app)window.chrome.app={
    InstallState:{DISABLED:'disabled',INSTALLED:'installed',NOT_INSTALLED:'not_installed'},
    RunningState:{CANNOT_RUN:'cannot_run',READY_TO_RUN:'ready_to_run',RUNNING:'running'},
    getDetails:_mn(function(){return null}),
    getIsInstalled:_mn(function(){return false}),
    installState:_mn(function(){return'not_installed'}),
    isInstalled:false,
    runningState:_mn(function(){return'cannot_run'})
};
if(!window.chrome.loadTimes)window.chrome.loadTimes=_mn(function(){
    var n=performance.now()/1000;
    return{requestTime:n-0.3,startLoadTime:n-0.25,commitLoadTime:n-0.1,
        finishDocumentLoadTime:n-0.05,finishLoadTime:n,firstPaintTime:n-0.08,
        firstPaintAfterLoadTime:0,navigationType:'Other',
        wasFetchedViaSpdy:true,wasNpnNegotiated:true,npnNegotiatedProtocol:'h2',
        wasAlternateProtocolAvailable:false,connectionInfo:'h2'};
});
if(!window.chrome.csi)window.chrome.csi=_mn(function(){
    return{onloadT:Date.now(),startE:Date.now()-300,pageT:performance.now(),tran:15};
});
}catch(e){}

try{
var _mkPlg=function(n,d,f,mt){
    var p={name:n,description:d,filename:f,length:mt.length};
    mt.forEach(function(m,i){p[i]=m;m.enabledPlugin=p;});
    return p;
};
var mt0={type:'application/pdf',suffixes:'pdf',description:'Portable Document Format'};
var mt1={type:'application/x-google-chrome-pdf',suffixes:'pdf',description:'Portable Document Format'};
var mt2={type:'application/x-nacl',suffixes:'',description:'Native Client Executable'};
var mt3={type:'application/x-pnacl',suffixes:'',description:'Portable Native Client Executable'};
var fakePlugins=[
    _mkPlg('PDF Viewer','Portable Document Format','internal-pdf-viewer',[mt0]),
    _mkPlg('Chrome PDF Plugin','Portable Document Format','internal-pdf-viewer',[mt1]),
    _mkPlg('Chrome PDF Viewer','','mhjfbmdgcfjbbpaeojofohoefgiehjai',[{type:'application/pdf',suffixes:'pdf',description:''}]),
    _mkPlg('Native Client','','internal-nacl-plugin',[mt2,mt3]),
    _mkPlg('Chromium PDF Plugin','Portable Document Format','internal-pdf-viewer',[{type:'application/x-google-chrome-pdf',suffixes:'pdf',description:'Portable Document Format'}])
];
fakePlugins.length=5;
fakePlugins.item=_mn(function(i){return this[i]||null});
fakePlugins.namedItem=_mn(function(n){for(var i=0;i<this.length;i++){if(this[i].name===n)return this[i]}return null});
fakePlugins.refresh=_mn(function(){});
Object.setPrototypeOf(fakePlugins,PluginArray.prototype);
Object.defineProperty(navigator,'plugins',{get:_mn(function(){return fakePlugins}),enumerable:true});
var fakeMime=[mt0,mt1,mt2,mt3];
fakeMime.length=4;
fakeMime.item=_mn(function(i){return this[i]||null});
fakeMime.namedItem=_mn(function(n){for(var i=0;i<this.length;i++){if(this[i].type===n)return this[i]}return null});
Object.setPrototypeOf(fakeMime,MimeTypeArray.prototype);
Object.defineProperty(navigator,'mimeTypes',{get:_mn(function(){return fakeMime}),enumerable:true});
}catch(e){}

try{
var fakeConn={
    effectiveType:'4g',downlink:10+Math.random()*5|0,rtt:50+(Math.random()*30|0),
    saveData:false,type:'wifi',downlinkMax:100,onchange:null,ontypechange:null,
    addEventListener:_mn(function(){}),removeEventListener:_mn(function(){}),dispatchEvent:_mn(function(){return true})
};
Object.defineProperty(navigator,'connection',{get:_mn(function(){return fakeConn}),enumerable:true,configurable:true});
}catch(e){}

try{
if(navigator.permissions){
    var _oq=navigator.permissions.query;
    navigator.permissions.query=_mn(function(desc){
        var n=desc&&desc.name||'';
        if(n==='notifications'||n==='push'||n==='geolocation'||n==='camera'||n==='microphone'||n==='midi'||n==='clipboard-read'||n==='clipboard-write'){
            return Promise.resolve({state:'prompt',name:n,onchange:null,addEventListener:_mn(function(){}),removeEventListener:_mn(function(){})});
        }
        try{return _oq.call(navigator.permissions,desc)}catch(e){
            return Promise.resolve({state:'prompt',name:n,onchange:null,addEventListener:_mn(function(){}),removeEventListener:_mn(function(){})});
        }
    });
}
}catch(e){}

try{
if(!window.Notification){
    window.Notification=_mn(function Notification(title,opt){this.title=title;this.body=(opt||{}).body||'';this.onclick=null;this.onclose=null;this.onerror=null;this.onshow=null;});
    Notification.permission='default';
    Notification.requestPermission=_mn(function(cb){var r='default';if(cb)cb(r);return Promise.resolve(r);});
    Notification.maxActions=2;
    Notification.prototype.close=_mn(function(){});
    Notification.prototype.addEventListener=_mn(function(){});
    Notification.prototype.removeEventListener=_mn(function(){});
}else if(typeof Notification!=='undefined'&&Notification.permission==='denied'){
    Object.defineProperty(Notification,'permission',{get:_mn(function(){return'default'}),configurable:true});
}
}catch(e){}

try{
if(!navigator.credentials){
    Object.defineProperty(navigator,'credentials',{get:_mn(function(){return{
        create:_mn(function(){return Promise.resolve(null)}),
        get:_mn(function(){return Promise.resolve(null)}),
        preventSilentAccess:_mn(function(){return Promise.resolve()}),
        store:_mn(function(){return Promise.resolve()})
    }}),enumerable:true,configurable:true});
}
}catch(e){}

try{
if(!navigator.keyboard){
    Object.defineProperty(navigator,'keyboard',{get:_mn(function(){return{
        getLayoutMap:_mn(function(){return Promise.resolve(new Map())}),
        lock:_mn(function(){return Promise.resolve()}),
        unlock:_mn(function(){})
    }}),enumerable:true,configurable:true});
}
}catch(e){}

try{
if(!navigator.locks){
    Object.defineProperty(navigator,'locks',{get:_mn(function(){return{
        request:_mn(function(n,cb){if(typeof cb==='function')return Promise.resolve(cb({name:n,mode:'exclusive'}));return Promise.resolve()}),
        query:_mn(function(){return Promise.resolve({held:[],pending:[]})})
    }}),enumerable:true,configurable:true});
}
}catch(e){}

try{
['usb','hid','serial','bluetooth'].forEach(function(api){
    if(!navigator[api]){
        Object.defineProperty(navigator,api,{get:_mn(function(){return{
            getDevices:_mn(function(){return Promise.resolve([])}),
            requestDevice:_mn(function(){return Promise.reject(new DOMException('User cancelled','NotFoundError'))}),
            addEventListener:_mn(function(){}),removeEventListener:_mn(function(){})
        }}),enumerable:true,configurable:true});
    }
});
}catch(e){}

try{
if(navigator.storage&&navigator.storage.estimate){
    var _oe=navigator.storage.estimate;
    navigator.storage.estimate=_mn(function(){
        return _oe.call(navigator.storage).then(function(e){
            return{quota:2147483648,usage:Math.floor(Math.random()*1e8),usageDetails:e.usageDetails||{}};
        });
    });
}
}catch(e){}


try{Object.defineProperty(window,'isSecureContext',{get:_mn(function(){return true}),enumerable:true,configurable:true});}catch(e){}

try{Object.defineProperty(window,'crossOriginIsolated',{get:_mn(function(){return false}),enumerable:true,configurable:true});}catch(e){}

try{if(!window.clientInformation)Object.defineProperty(window,'clientInformation',{get:_mn(function(){return navigator}),enumerable:true,configurable:true});}catch(e){}

try{
Object.defineProperty(window,'outerWidth',{get:_mn(function(){return window.innerWidth||1920}),enumerable:true,configurable:true});
Object.defineProperty(window,'outerHeight',{get:_mn(function(){return(window.innerHeight||1080)+85}),enumerable:true,configurable:true});
}catch(e){}

try{
Object.defineProperty(window,'screenX',{get:_mn(function(){return 0}),enumerable:true,configurable:true});
Object.defineProperty(window,'screenY',{get:_mn(function(){return 0}),enumerable:true,configurable:true});
Object.defineProperty(window,'screenLeft',{get:_mn(function(){return 0}),enumerable:true,configurable:true});
Object.defineProperty(window,'screenTop',{get:_mn(function(){return 0}),enumerable:true,configurable:true});
}catch(e){}

try{document.hasFocus=_mn(function(){return true});}catch(e){}

try{
var _omm=window.matchMedia;
window.matchMedia=_mn(function(q){
    var r=_omm?_omm.call(window,q):null;
    if(r)return r;
    return{matches:false,media:q,onchange:null,
        addEventListener:_mn(function(){}),removeEventListener:_mn(function(){}),
        addListener:_mn(function(){}),removeListener:_mn(function(){})};
});
}catch(e){}

try{
var _opn=performance.now;
var _po=(Math.random()-0.5)*0.1;
performance.now=_mn(function(){return Math.round(_opn.call(performance)*10)/10+_po});
if(performance.timeOrigin){
    var _oto=performance.timeOrigin;
    Object.defineProperty(performance,'timeOrigin',{get:_mn(function(){return Math.round(_oto)}),enumerable:true,configurable:true});
}
}catch(e){}

try{
var autoFlags=['__selenium_unwrapped','__webdriver_evaluate','__webdriver_script_function',
    '__webdriver_script_func','__webdriver_script_fn','_Selenium_IDE_Recorder',
    '__fxdriver_evaluate','__driver_evaluate','__webdriver_unwrapped',
    '__lastWatirAlert','__lastWatirConfirm','__lastWatirPrompt',
    '_phantom','__nightmare','callPhantom','_phantomas',
    'domAutomation','domAutomationController','_WEBDRIVER_ELEM_CACHE',
    'webdriver','_webdriver','__webview_bridge','__wta_injected'];
autoFlags.forEach(function(f){try{if(f in window){delete window[f];Object.defineProperty(window,f,{get:_mn(function(){return undefined}),configurable:true})}}catch(e){}});
}catch(e){}

try{
var _opst=Error.prepareStackTrace;
Error.prepareStackTrace=_mn(function(err,stack){
    stack=stack.filter(function(f){
        var fn=(f.getFileName&&f.getFileName())||'';
        return fn.indexOf('injectedScript')===-1&&fn.indexOf('evaluateJavascript')===-1&&fn.indexOf('__wta')===-1&&fn.indexOf('userScript')===-1;
    });
    return _opst?_opst(err,stack):err.toString()+'\\n'+stack.map(function(f){return'    at '+f}).join('\\n');
});
}catch(e){}

try{
var _OE=Error;
window.Error=_mn(function Error(msg){
    var e=new _OE(msg);
    var _os=Object.getOwnPropertyDescriptor(e,'stack');
    if(_os&&_os.value){
        e.stack=_os.value.split('\\n').filter(function(l){
            return l.indexOf('evaluateJavascript')===-1&&l.indexOf('injectedScript')===-1;
        }).join('\\n');
    }
    return e;
});
window.Error.prototype=_OE.prototype;
window.Error.captureStackTrace=_OE.captureStackTrace;
window.Error.stackTraceLimit=_OE.stackTraceLimit;
['TypeError','RangeError','ReferenceError','SyntaxError','URIError','EvalError'].forEach(function(et){
    try{window[et].prototype.constructor=window[et]}catch(e){}
});
}catch(e){}

try{
var _ocd=console.debug;
console.debug=_mn(function(){
    if(arguments.length===1&&typeof arguments[0]==='string'&&arguments[0].indexOf('devtools')!==-1)return;
    return _ocd.apply(console,arguments);
});
}catch(e){}

try{
if(document.documentElement){
    document.documentElement.removeAttribute('webdriver');
    document.documentElement.removeAttribute('selenium');
    document.documentElement.removeAttribute('driver');
}
}catch(e){}

try{
if(!Intl.ListFormat)Intl.ListFormat=_mn(function(l,o){this.format=_mn(function(a){return a.join(', ')});this.formatToParts=_mn(function(a){return a.map(function(v){return{type:'element',value:v}})});this.resolvedOptions=_mn(function(){return{locale:'en-US',type:'conjunction',style:'long'}})});
if(!Intl.Segmenter)Intl.Segmenter=_mn(function(l,o){this.segment=_mn(function(s){var r=[];for(var i=0;i<s.length;i++)r.push({segment:s[i],index:i,input:s});return r;});this.resolvedOptions=_mn(function(){return{locale:'en-US',granularity:'grapheme'}})});
}catch(e){}

try{
if(!window.visualViewport){
    window.visualViewport={
        width:window.innerWidth,height:window.innerHeight,
        offsetLeft:0,offsetTop:0,pageLeft:0,pageTop:0,
        scale:1,onresize:null,onscroll:null,
        addEventListener:_mn(function(){}),removeEventListener:_mn(function(){})
    };
}
}catch(e){}

try{if(navigator.brave)delete navigator.brave;}catch(e){}

try{
Object.keys(window).forEach(function(k){if(k.match(/^cdc_/)||k.match(/^\$cdc_/)){delete window[k];}});
Object.keys(document).forEach(function(k){if(k.match(/^cdc_/)||k.match(/^\$cdc_/)){delete document[k];}});
}catch(e){}
