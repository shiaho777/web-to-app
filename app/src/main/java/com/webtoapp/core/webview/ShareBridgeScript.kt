package com.webtoapp.core.webview

/**
 * Document-start replacement for `navigator.share` / `canShare`.
 *
 * File shares always go through [ShareBridge], including on ordinary https
 * pages. Text-only shares stay on the engine when it already has `navigator.share`.
 * [NativeShareBridge] methods may return a Promise (Gecko) or a plain value (WebView).
 */
object ShareBridgeScript {
    const val INSTALL = """
        (function() {
          if (window.__wtaShareInstalled) return;
          if (typeof NativeShareBridge === 'undefined') return;
          window.__wtaShareInstalled = true;

          var nativeShare = (typeof navigator.share === 'function') ? navigator.share.bind(navigator) : null;
          var nativeCanShare = (typeof navigator.canShare === 'function') ? navigator.canShare.bind(navigator) : null;
          var CHUNK = 48000;

          function call(fn) {
            try {
              var value = fn();
              if (value && typeof value.then === 'function') return value;
              return Promise.resolve(value);
            } catch (e) {
              return Promise.reject(e);
            }
          }

          function bytesToBase64(bytes) {
            var binary = '';
            var step = 0x8000;
            for (var i = 0; i < bytes.length; i += step) {
              binary += String.fromCharCode.apply(null, bytes.subarray(i, i + step));
            }
            return btoa(binary);
          }

          function readFile(file) {
            return new Promise(function(resolve, reject) {
              var reader = new FileReader();
              reader.onload = function() { resolve(new Uint8Array(reader.result)); };
              reader.onerror = function() { reject(reader.error || new Error('read failed')); };
              reader.readAsArrayBuffer(file);
            });
          }

          function shareFiles(data) {
            var files = Array.prototype.slice.call(data.files || []);
            var sessionId = null;
            return call(function() {
              return NativeShareBridge.beginShare(data.title || '', data.text || '', data.url || '');
            }).then(function(id) {
              if (!id) throw new Error('share failed');
              sessionId = String(id);
              var chain = Promise.resolve();
              files.forEach(function(file) {
                chain = chain.then(function() { return readFile(file); }).then(function(bytes) {
                  return call(function() {
                    return NativeShareBridge.beginFile(sessionId, file.name || 'shared_file', file.type || '');
                  }).then(function(ok) {
                    if (!ok) throw new Error('share failed');
                    var b64 = bytesToBase64(bytes);
                    var next = Promise.resolve();
                    for (var offset = 0; offset < b64.length || (b64.length === 0 && offset === 0); offset += CHUNK) {
                      if (b64.length === 0) break;
                      (function(part) {
                        next = next.then(function() {
                          return call(function() { return NativeShareBridge.appendChunk(sessionId, part); })
                            .then(function(wrote) { if (!wrote) throw new Error('share failed'); });
                        });
                      })(b64.slice(offset, offset + CHUNK));
                    }
                    return next;
                  });
                });
              });
              return chain;
            }).then(function() {
              return call(function() { return NativeShareBridge.finishShare(sessionId); });
            }).then(function(ok) {
              if (!ok) throw new Error('share failed');
            }).catch(function(error) {
              if (sessionId) {
                try { NativeShareBridge.cancelShare(sessionId); } catch (ignore) {}
              }
              throw error;
            });
          }

          navigator.canShare = function(data) {
            try {
              if (data && data.files && data.files.length > 0) return true;
              if (nativeCanShare) return !!nativeCanShare(data);
            } catch (e) {}
            if (!data) return false;
            return !!(data.text || data.url || data.title);
          };

          navigator.share = function(data) {
            data = data || {};
            if (data.files && data.files.length > 0) return shareFiles(data);
            if (nativeShare) {
              try { return nativeShare(data); } catch (e) { return Promise.reject(e); }
            }
            return call(function() {
              return NativeShareBridge.shareText(data.title || '', data.text || '', data.url || '');
            });
          };
        })();
    """
}
