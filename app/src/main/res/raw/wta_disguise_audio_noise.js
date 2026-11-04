
try{
if(typeof AudioContext!=='undefined'){
    var _aco=AudioContext.prototype.createOscillator;
    var _aca=AudioContext.prototype.createAnalyser;
    var _acd=AudioContext.prototype.createDynamicsCompressor;
    AudioContext.prototype.createOscillator=_mn(function(){
        var osc=_aco.apply(this,arguments);
        var _gv=Object.getOwnPropertyDescriptor(osc.frequency.__proto__,'value');
        if(_gv&&_gv.get){
            var _og=_gv.get;
            Object.defineProperty(osc.frequency,'value',{
                get:function(){return _og.call(this)+(Math.random()-0.5)*0.0001;},
                set:_gv.set,enumerable:true,configurable:true
            });
        }
        return osc;
    });
    var _agffd=AnalyserNode.prototype.getFloatFrequencyData;
    if(_agffd)AnalyserNode.prototype.getFloatFrequencyData=_mn(function(arr){
        _agffd.call(this,arr);for(var i=0;i<arr.length;i++)arr[i]+=(Math.random()-0.5)*0.0001;
    });
    var _agftd=AnalyserNode.prototype.getFloatTimeDomainData;
    if(_agftd)AnalyserNode.prototype.getFloatTimeDomainData=_mn(function(arr){
        _agftd.call(this,arr);for(var i=0;i<arr.length;i++)arr[i]+=(Math.random()-0.5)*0.00001;
    });
}
if(typeof OfflineAudioContext!=='undefined'){
    var _oac=OfflineAudioContext;
    window.OfflineAudioContext=_mn(function OfflineAudioContext(){
        var ctx=new _oac(...arguments);
        var _osr=ctx.startRendering;
        ctx.startRendering=_mn(function(){
            return _osr.call(this).then(function(buf){
                var d=buf.getChannelData(0);
                for(var i=0;i<d.length;i++)d[i]+=(Math.random()-0.5)*0.00001;
                return buf;
            });
        });
        return ctx;
    });
    window.OfflineAudioContext.prototype=_oac.prototype;
}
}catch(e){}
