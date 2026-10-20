package com.webtoapp.core.webview

import android.webkit.JavascriptInterface

/**
 * Page-side half of background playback.
 *
 * The native lifecycle still decides whether the WebView is paused. This script
 * only stops the page from pausing itself while [BackgroundMediaHold.held] is
 * true: visibility stays "visible", and a visibility-triggered `pause()` is
 * ignored. A notification pause sets `__wtaAllowMediaPauseUntil` first so the
 * user can still stop playback from the shade.
 */
object BackgroundMediaScript {
    const val JS_INTERFACE = "WtaBackgroundMedia"

    const val HOLD_ON = "window.__wtaBgHold=true;"
    const val HOLD_OFF = "window.__wtaBgHold=false;"
    const val RESUME = "window.__wtaResumeHeld&&window.__wtaResumeHeld();"

    const val INSTALL = """
        (function(){
          if (window.__wtaBgMediaInstalled) return;
          window.__wtaBgMediaInstalled = true;
          window.__wtaBgHold = !!window.__wtaBgHold;
          var live = [];
          function held() {
            try {
              if (window.WtaBackgroundMedia && window.WtaBackgroundMedia.isHeld()) return true;
            } catch (e) {}
            return !!window.__wtaBgHold;
          }
          function allowedPause() {
            return Date.now() < (window.__wtaAllowMediaPauseUntil || 0);
          }
          var hiddenDesc = Object.getOwnPropertyDescriptor(Document.prototype, 'hidden');
          var visDesc = Object.getOwnPropertyDescriptor(Document.prototype, 'visibilityState');
          function define(name, getter) {
            try { Object.defineProperty(document, name, { configurable: true, get: getter }); } catch (e) {}
          }
          define('hidden', function() {
            if (held()) return false;
            try { return hiddenDesc && hiddenDesc.get ? hiddenDesc.get.call(document) : false; } catch (e) { return false; }
          });
          define('visibilityState', function() {
            if (held()) return 'visible';
            try { return visDesc && visDesc.get ? visDesc.get.call(document) : 'visible'; } catch (e) { return 'visible'; }
          });
          function swallow(e) { if (held()) e.stopImmediatePropagation(); }
          document.addEventListener('visibilitychange', swallow, true);
          document.addEventListener('webkitvisibilitychange', swallow, true);
          window.addEventListener('blur', swallow, true);
          window.addEventListener('pagehide', swallow, true);
          var proto = window.HTMLMediaElement && HTMLMediaElement.prototype;
          if (proto && !proto.__wtaBgPause) {
            var origPause = proto.pause;
            var origPlay = proto.play;
            proto.__wtaBgPause = true;
            proto.play = function() {
              var result = origPlay.apply(this, arguments);
              if (live.indexOf(this) < 0) live.push(this);
              return result;
            };
            proto.pause = function() {
              if (held() && !allowedPause()) return;
              var index = live.indexOf(this);
              if (index >= 0) live.splice(index, 1);
              return origPause.apply(this, arguments);
            };
          }
          document.addEventListener('playing', function(e) {
            if (e.target && live.indexOf(e.target) < 0) live.push(e.target);
          }, true);
          window.__wtaResumeHeld = function() {
            live.forEach(function(el) {
              try {
                if (el && el.paused && !el.ended) {
                  var p = el.play();
                  if (p && p.catch) p.catch(function(){});
                }
              } catch (e) {}
            });
          };
        })();
    """
}

/** Synchronous hold flag read by [BackgroundMediaScript] from page script. */
class BackgroundMediaHold {
    @Volatile
    var held: Boolean = false

    @JavascriptInterface
    fun isHeld(): Boolean = held
}
