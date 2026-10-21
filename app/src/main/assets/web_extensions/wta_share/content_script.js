// Document-start share polyfill for GeckoView. The page owns the Promise.
// Native calls happen when the user actually shares, not at parse time.
(function () {
  if (window.wrappedJSObject && window.wrappedJSObject.__wtaShareInstalled) return;

  const NATIVE_APP = "wta_share";

  function sandboxSend(payloadJson, resolve, reject) {
    const payload = payloadJson == null ? "" : String(payloadJson);
    const ok = (value) => {
      try { resolve(value == null ? "" : String(value)); } catch (e) {}
    };
    const fail = (value) => {
      const message = value == null ? "share failed" : String(value);
      try { reject(message); } catch (e) {}
    };
    try {
      browser.runtime.sendNativeMessage(NATIVE_APP, payload, (response) => {
        const err = browser.runtime.lastError;
        if (err) {
          fail(typeof err === "string" ? err : (err.message || "share failed"));
          return;
        }
        ok(response);
      });
    } catch (e) {
      fail(e && e.message ? e.message : e);
    }
  }

  const pageWorldSetup = function (send) {
    if (window.__wtaShareInstalled) return;
    window.__wtaShareInstalled = true;
    function call(op, fields) {
      return new Promise(function (resolve, reject) {
        send(JSON.stringify(Object.assign({ op: op }, fields || {})), function (raw) {
          try {
            var parsed = JSON.parse(String(raw || "{}"));
            if (!parsed.ok) reject(new Error(parsed.message || "share failed"));
            else resolve(parsed);
          } catch (e) {
            reject(e);
          }
        }, function (message) {
          reject(new Error(String(message || "share failed")));
        });
      });
    }
    var nativeCanShare = (typeof navigator.canShare === "function") ? navigator.canShare.bind(navigator) : null;
    var CHUNK = 48000;
    function bytesToBase64(bytes) {
      var binary = "";
      var step = 0x8000;
      for (var i = 0; i < bytes.length; i += step) {
        binary += String.fromCharCode.apply(null, bytes.subarray(i, i + step));
      }
      return btoa(binary);
    }
    function readFile(file) {
      return new Promise(function (resolve, reject) {
        var reader = new FileReader();
        reader.onload = function () { resolve(new Uint8Array(reader.result)); };
        reader.onerror = function () { reject(reader.error || new Error("read failed")); };
        reader.readAsArrayBuffer(file);
      });
    }
    function shareFiles(data) {
      var files = Array.prototype.slice.call(data.files || []);
      var sessionId = null;
      return call("begin", { title: data.title || "", text: data.text || "", url: data.url || "" }).then(function (parsed) {
        sessionId = parsed.id;
        if (!sessionId) throw new Error("share failed");
        var chain = Promise.resolve();
        files.forEach(function (file) {
          chain = chain.then(function () { return readFile(file); }).then(function (bytes) {
            return call("file", { id: sessionId, name: file.name || "shared_file", mime: file.type || "" }).then(function () {
              var b64 = bytesToBase64(bytes);
              var next = Promise.resolve();
              for (var offset = 0; offset < b64.length; offset += CHUNK) {
                (function (part) {
                  next = next.then(function () { return call("chunk", { id: sessionId, data: part }); });
                })(b64.slice(offset, offset + CHUNK));
              }
              return next;
            });
          });
        });
        return chain;
      }).then(function () {
        return call("finish", { id: sessionId });
      }).catch(function (error) {
        if (sessionId) call("cancel", { id: sessionId }).catch(function () {});
        throw error;
      });
    }
    navigator.canShare = function (data) {
      try {
        if (data && data.files && data.files.length > 0) return true;
        if (nativeCanShare) return !!nativeCanShare(data);
      } catch (e) {}
      if (!data) return false;
      return !!(data.text || data.url || data.title);
    };
    navigator.share = function (data) {
      data = data || {};
      if (data.files && data.files.length > 0) return shareFiles(data);
      return call("text", {
        title: data.title || "",
        text: data.text || "",
        url: data.url || ""
      }).then(function () {});
    };
  };

  try {
    if (typeof exportFunction === "function") {
      exportFunction(sandboxSend, window, { defineAs: "__wtaShareSend", allowCrossOriginArguments: true });
      var setupSrc = "(" + pageWorldSetup.toString() + ")(window.__wtaShareSend);";
      window.wrappedJSObject.eval(setupSrc);
    } else {
      pageWorldSetup(sandboxSend);
    }
  } catch (e) {}
})();
