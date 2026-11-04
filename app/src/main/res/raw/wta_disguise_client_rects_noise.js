
try{
var _gbcr=Element.prototype.getBoundingClientRect;
var _gcr=Element.prototype.getClientRects;
var _rn=function(){return(Math.random()-0.5)*0.25};
Element.prototype.getBoundingClientRect=_mn(function(){
    var r=_gbcr.call(this);return new DOMRect(r.x+_rn(),r.y+_rn(),r.width+_rn(),r.height+_rn());
});
Element.prototype.getClientRects=_mn(function(){
    var l=_gcr.call(this),a=[];
    for(var i=0;i<l.length;i++){var r=l[i];a.push(new DOMRect(r.x+_rn(),r.y+_rn(),r.width+_rn(),r.height+_rn()));}
    return a;
});
var _rgbcr=Range.prototype.getBoundingClientRect;
if(_rgbcr)Range.prototype.getBoundingClientRect=_mn(function(){
    var r=_rgbcr.call(this);return new DOMRect(r.x+_rn(),r.y+_rn(),r.width+_rn(),r.height+_rn());
});
}catch(e){}
