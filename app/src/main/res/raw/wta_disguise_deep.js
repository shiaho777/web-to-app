

try{
if(navigator.mediaDevices&&navigator.mediaDevices.enumerateDevices){
    var _oed=navigator.mediaDevices.enumerateDevices;
    navigator.mediaDevices.enumerateDevices=_mn(function(){
        return _oed.call(navigator.mediaDevices).then(function(devs){
            return devs.map(function(d,i){
                return{deviceId:'device'+i,groupId:'group'+(i%3),kind:d.kind,label:'',
                    toJSON:_mn(function(){return{deviceId:this.deviceId,groupId:this.groupId,kind:this.kind,label:''}})};
            });
        });
    });
}
}catch(e){}

try{
var _RTC=window.RTCPeerConnection||window.webkitRTCPeerConnection;
if(_RTC){
    var PRTC=_mn(function RTCPeerConnection(c,x){
        if(c&&c.iceServers){c.iceServers=c.iceServers.filter(function(s){return s.urls&&!String(s.urls).match(/stun:/i)});}
        var pc=new _RTC(c,x);
        var _olc=pc.createDataChannel;
        var _oCD=Object.getOwnPropertyDescriptor(_RTC.prototype,'localDescription');
        if(_oCD&&_oCD.get){
            Object.defineProperty(pc,'localDescription',{get:_mn(function(){
                var d=_oCD.get.call(this);
                if(d&&d.sdp){d={type:d.type,sdp:d.sdp.replace(/a=candidate:.*\\r\\n/g,'').replace(/c=IN IP4 \\S+/g,'c=IN IP4 0.0.0.0')}};
                return d;
            }),configurable:true});
        }
        return pc;
    });
    PRTC.prototype=_RTC.prototype;
    PRTC.generateCertificate=_RTC.generateCertificate;
    window.RTCPeerConnection=PRTC;
    if(window.webkitRTCPeerConnection)window.webkitRTCPeerConnection=PRTC;
}
}catch(e){}

try{
var _omt=CanvasRenderingContext2D.prototype.measureText;
var _fc={};
CanvasRenderingContext2D.prototype.measureText=_mn(function(text){
    var r=_omt.call(this,text);
    var k=this.font+'|'+text;
    if(!_fc[k])_fc[k]=(Math.random()-0.5)*0.3;
    var ow=r.width;
    Object.defineProperty(r,'width',{get:_mn(function(){return ow+_fc[k]})});
    return r;
});
}catch(e){}

try{
navigator.getBattery=_mn(function getBattery(){
    return Promise.resolve({
        charging:true,chargingTime:0,dischargingTime:Infinity,level:1.0,
        onchargingchange:null,onchargingtimechange:null,ondischargingtimechange:null,onlevelchange:null,
        addEventListener:_mn(function(){}),removeEventListener:_mn(function(){})
    });
});
}catch(e){}

try{
var _oefp=document.elementFromPoint;
document.elementFromPoint=_mn(function(x,y){
    return _oefp.call(this,x,y)||document.body;
});
}catch(e){}

try{
if(window.chrome&&!window.chrome.webstore){
    window.chrome.webstore={
        onInstallStageChanged:{},onDownloadProgress:{},
        install:_mn(function(url,onsuccess,onfailure){if(onfailure)onfailure('Chrome Web Store is no longer available')}),
        onEnabled:{},onDisabled:{}
    };
}
}catch(e){}
