
try{
var _oce=document.createElement;
document.createElement=_mn(function(tag){
    var el=_oce.apply(this,arguments);
    if(typeof tag==='string'&&tag.toLowerCase()==='iframe'){
        el.addEventListener('load',function(){
            try{
                var w=el.contentWindow;
                if(!w||w.__wta_browser_disguise__)return;
                w.__wta_browser_disguise__=1;
                Object.defineProperty(w.navigator,'webdriver',{get:function(){return false},configurable:true});
                Object.defineProperty(w.navigator,'vendor',{get:function(){return'Google Inc.'},configurable:true});
                Object.defineProperty(w.navigator,'plugins',{get:function(){return navigator.plugins},configurable:true});
                if(!w.chrome)w.chrome=window.chrome;
                Object.defineProperty(w.navigator,'languages',{get:function(){return navigator.languages},configurable:true});
                Object.defineProperty(w.navigator,'platform',{get:function(){return navigator.platform},configurable:true});
                if(w.navigator.userAgentData){
                    Object.defineProperty(w.navigator,'userAgentData',{get:function(){return navigator.userAgentData},configurable:true});
                }
            }catch(e){}
        });
    }
    return el;
});
}catch(e){}
