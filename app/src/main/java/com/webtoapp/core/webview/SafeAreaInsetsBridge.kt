package com.webtoapp.core.webview

import android.app.Activity
import android.os.Build
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.webtoapp.data.model.DisplayCutoutMode
import kotlin.math.roundToInt

data class SafeAreaInsets(
    val top: Int = 0,
    val right: Int = 0,
    val bottom: Int = 0,
    val left: Int = 0
) {
    fun toJson(): String = "{\"top\":$top,\"right\":$right,\"bottom\":$bottom,\"left\":$left}"

    companion object {
        val ZERO = SafeAreaInsets()
    }
}

object SafeAreaInsetsBridge {

    const val JS_BRIDGE_NAME = "__WTASafeArea"

    const val DOCUMENT_START_SCRIPT = """(function(){
'use strict';
if(window.__wtaSafeAreaInstalled)return;
window.__wtaSafeAreaInstalled=true;
function parse(raw){try{return JSON.parse(raw);}catch(e){return null;}}
function apply(v){
if(!v)return false;
var root=document.documentElement;
if(!root)return false;
var style=document.getElementById('__wta_safe_area_css__');
if(!style){style=document.createElement('style');style.id='__wta_safe_area_css__';(document.head||root).appendChild(style);}
style.textContent=':root{--wta-safe-area-inset-top:'+v.top+'px;--wta-safe-area-inset-right:'+v.right+'px;--wta-safe-area-inset-bottom:'+v.bottom+'px;--wta-safe-area-inset-left:'+v.left+'px;}';
return true;
}
window.__wtaSetSafeAreaInsets=function(raw){return apply(parse(raw));};
var bridge=window.__WTASafeArea;
function current(){return parse(bridge?bridge.getInsets():null);}
if(!apply(current())){
var onReady=function(){if(apply(current()))document.removeEventListener('readystatechange',onReady);};
document.addEventListener('readystatechange',onReady);
document.addEventListener('DOMContentLoaded',function(){apply(current());},{once:true});
}
})();"""

    private val listeners = java.util.concurrent.CopyOnWriteArrayList<(SafeAreaInsets) -> Unit>()

    @Volatile
    private var lastInsets: SafeAreaInsets? = null

    @Volatile
    var currentJson: String = SafeAreaInsets.ZERO.toJson()
        private set

    fun addListener(listener: (SafeAreaInsets) -> Unit): () -> Unit {
        listeners.add(listener)
        return { listeners.remove(listener) }
    }

    fun publish(activity: Activity, mode: DisplayCutoutMode) {
        publish(read(activity, mode, activity.resources.displayMetrics.density))
    }

    fun publish(insets: SafeAreaInsets) {
        if (insets == lastInsets) return
        lastInsets = insets
        currentJson = insets.toJson()
        for (listener in listeners) listener(insets)
    }

    fun updateScript(insets: SafeAreaInsets): String {
        val escaped = insets.toJson().replace("\\", "\\\\").replace("'", "\\'")
        return "(function(){try{window.__wtaSetSafeAreaInsets&&window.__wtaSetSafeAreaInsets('$escaped');}catch(e){}})();"
    }

    fun read(activity: Activity, mode: DisplayCutoutMode, density: Float): SafeAreaInsets {
        if (mode == DisplayCutoutMode.NEVER || density <= 0f ||
            Build.VERSION.SDK_INT < Build.VERSION_CODES.P
        ) return SafeAreaInsets.ZERO
        val decorView = activity.window?.decorView ?: return SafeAreaInsets.ZERO
        val rootInsets = ViewCompat.getRootWindowInsets(decorView) ?: return SafeAreaInsets.ZERO
        val cutout = rootInsets.getInsets(WindowInsetsCompat.Type.displayCutout())
        fun cssPx(px: Int) = (px / density).roundToInt().coerceAtLeast(0)
        return SafeAreaInsets(
            top = cssPx(cutout.top),
            right = cssPx(cutout.right),
            bottom = cssPx(cutout.bottom),
            left = cssPx(cutout.left)
        )
    }
}
