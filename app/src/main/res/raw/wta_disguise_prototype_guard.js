
var _ht=new WeakSet();
var _ots=Function.prototype.toString;
function _mn(fn){_ht.add(fn);return fn;}
Function.prototype.toString=_mn(function(){
    if(_ht.has(this))return'function '+this.name+'() { [native code] }';
    return _ots.call(this);
});
var _ogopd=Object.getOwnPropertyDescriptor;
Object.getOwnPropertyDescriptor=_mn(function(obj,prop){
    var d=_ogopd.call(Object,obj,prop);
    if(d&&d.get&&_ht.has(d.get)){d.configurable=true;}
    return d;
});
