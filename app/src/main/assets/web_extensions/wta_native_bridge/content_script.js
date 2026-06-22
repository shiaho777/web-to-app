// WTA Native Bridge content script (GeckoView).
//
// GeckoView has no addJavascriptInterface. This built-in WebExtension exposes
// a native HTTP bridge to pages and installs fetch/XHR wrappers in the PAGE
// world (not the content-script sandbox) so the page's own network calls get
// routed through native OkHttp, bypassing the browser same-origin / CORS rules.
//
// Only registered by GeckoViewEngine when enableCorsBypass (or
// enablePrivateNetworkBridge) is on. GeckoView has no prior private-network
// bridge, so scope is treated as CORS_BYPASS (any http(s) URL, any method).
//
// run_at: document_start, all_frames, <all_urls>.
(function () {
  if (window.wrappedJSObject.__wta_gecko_bridge_installed__) return;

  const NATIVE_APP = "wta_native_bridge";

  // Runs in the content-script sandbox. Bridges a page-world call into a
  // native message round-trip. Exposed to the page via exportFunction.
  function sendNative(payloadJson) {
    return new Promise((resolve, reject) => {
      try {
        browser.runtime.sendNativeMessage(NATIVE_APP, payloadJson, (response) => {
          const err = browser.runtime.lastError;
          if (err) {
            reject(new Error(typeof err === 'string' ? err : (err.message || 'Native message failed')));
            return;
          }
          resolve(response);
        });
      } catch (e) {
        reject(e);
      }
    });
  }

  // The whole fetch/XHR hijack lives in a function that gets .toSource()'d
  // and eval'd in the page world via wrappedJSObject. That way it runs in the
  // page's own JS context (so replacing window.fetch is visible to page
  // scripts) while still able to call back into sendNative through the
  // exported wrapper.
  const pageWorldSetup = function (sendNativeExported) {
    if (window.__wta_gecko_bridge_installed__) return;
    window.__wta_gecko_bridge_installed__ = true;

    function bytesToBase64(bytes) {
      let binary = '';
      const chunkSize = 0x8000;
      for (let i = 0; i < bytes.length; i += chunkSize) {
        const chunk = bytes.subarray(i, i + chunkSize);
        binary += String.fromCharCode.apply(null, chunk);
      }
      return btoa(binary);
    }
    function textToBase64(t) { return bytesToBase64(new TextEncoder().encode(String(t))); }
    function base64ToBytes(b64) {
      const binary = atob(b64 || '');
      const bytes = new Uint8Array(binary.length);
      for (let i = 0; i < binary.length; i++) bytes[i] = binary.charCodeAt(i);
      return bytes;
    }
    function headersToObject(headers) {
      const obj = {};
      try {
        if (!headers) return obj;
        if (typeof Headers !== 'undefined' && headers instanceof Headers) {
          headers.forEach((v, k) => { obj[k] = String(v); });
        } else if (Array.isArray(headers)) {
          headers.forEach((p) => { if (p && p.length >= 2) obj[String(p[0])] = String(p[1]); });
        } else if (typeof headers === 'object') {
          Object.keys(headers).forEach((k) => { obj[k] = String(headers[k]); });
        }
      } catch (e) {}
      return obj;
    }
    function bodyToBase64(body) {
      if (body == null) return Promise.resolve('');
      if (typeof body === 'string') return Promise.resolve(textToBase64(body));
      if (typeof URLSearchParams !== 'undefined' && body instanceof URLSearchParams) return Promise.resolve(textToBase64(body.toString()));
      if (body instanceof ArrayBuffer) return Promise.resolve(bytesToBase64(new Uint8Array(body)));
      if (ArrayBuffer.isView(body)) return Promise.resolve(bytesToBase64(new Uint8Array(body.buffer, body.byteOffset, body.byteLength)));
      if (typeof Blob !== 'undefined' && body instanceof Blob) {
        return new Promise((resolve, reject) => {
          const reader = new FileReader();
          reader.onload = () => { const r = String(reader.result || ''); resolve(r.indexOf(',') >= 0 ? r.split(',').pop() : ''); };
          reader.onerror = () => reject(reader.error || new Error('Blob read failed'));
          reader.readAsDataURL(body);
        });
      }
      if (typeof Response !== 'undefined') {
        const canWrap = (typeof FormData !== 'undefined' && body instanceof FormData) ||
          (typeof Request !== 'undefined' && body instanceof Request) ||
          (typeof ReadableStream !== 'undefined' && body instanceof ReadableStream);
        if (canWrap) {
          try { return new Response(body).arrayBuffer().then((buf) => bytesToBase64(new Uint8Array(buf))); }
          catch (e) {}
        }
      }
      try { return Promise.resolve(textToBase64(JSON.stringify(body))); }
      catch (e) { return Promise.resolve(textToBase64(String(body))); }
    }
    function normalizeFetchInput(input, init) {
      let url = '', method = 'GET', headers = {}, body = null;
      if (typeof Request !== 'undefined' && input instanceof Request) {
        url = input.url; method = input.method || method;
        headers = headersToObject(input.headers);
        if (!init && method !== 'GET' && method !== 'HEAD') body = input.clone();
      } else { url = String(input); }
      if (init) {
        if (init.method) method = String(init.method);
        Object.assign(headers, headersToObject(init.headers));
        if ('body' in init) body = init.body;
      }
      return { url, method: method.toUpperCase(), headers, body };
    }
    function isHttpUrl(u) {
      try { return new URL(String(u), window.location.href).protocol === 'http:' || new URL(String(u), window.location.href).protocol === 'https:'; }
      catch (e) { return false; }
    }
    function nativeHttpRequest(payload) {
      return bodyToBase64(payload.body).then((bodyBase64) => {
        return sendNativeExported(JSON.stringify({ url: payload.url, method: payload.method || 'GET', headers: payload.headers || {}, bodyBase64 }));
      }).then((raw) => {
        const result = JSON.parse(String(raw || '{}'));
        if (!result.ok) throw new TypeError(result.message || result.error || 'Native HTTP request failed');
        return result;
      });
    }

    window.NativeBridge = window.NativeBridge || {};
    window.NativeBridge.httpRequest = function (requestJson) {
      let parsed; try { parsed = JSON.parse(requestJson); } catch (e) { return Promise.reject(new TypeError('Invalid httpRequest JSON')); }
      return nativeHttpRequest(parsed);
    };

    const nativeFetch = window.fetch ? window.fetch.bind(window) : null;
    if (nativeFetch) {
      window.fetch = function (input, init) {
        const payload = normalizeFetchInput(input, init);
        if (!isHttpUrl(payload.url)) return nativeFetch(input, init);
        return nativeHttpRequest(payload).then((result) => {
          const bytes = base64ToBytes(result.bodyBase64 || '');
          return new Response(bytes, { status: Number(result.status || 0), statusText: String(result.statusText || ''), headers: result.headers || {} });
        });
      };
    }

    const NativeXHR = window.XMLHttpRequest;
    if (NativeXHR) {
      function BridgedXHR() {
        this._xhr = new NativeXHR(); this._listeners = {}; this._headers = {}; this._method = 'GET'; this._url = ''; this._async = true;
        this.readyState = 0; this.response = null; this.responseText = ''; this.responseType = ''; this.responseURL = '';
        this.status = 0; this.statusText = ''; this.timeout = 0; this.withCredentials = false;
        this.onreadystatechange = null; this.onload = null; this.onerror = null; this.onloadend = null; this.upload = {};
        const self = this;
        ['readystatechange', 'load', 'error', 'loadend', 'timeout', 'abort', 'progress'].forEach((name) => {
          self._xhr.addEventListener(name, (event) => { self._syncFromNative(); self._dispatch(name, event); });
        });
      }
      BridgedXHR.prototype._syncFromNative = function () {
        this.readyState = this._xhr.readyState; this.response = this._xhr.response; this.responseText = this._xhr.responseText || '';
        this.responseURL = this._xhr.responseURL || this._url; this.status = this._xhr.status || 0; this.statusText = this._xhr.statusText || '';
      };
      BridgedXHR.prototype._dispatch = function (type, sourceEvent) {
        const event = sourceEvent || { type, target: this, currentTarget: this };
        const list = this._listeners[type] || [];
        for (let i = 0; i < list.length; i++) { try { list[i].call(this, event); } catch (e) { setTimeout(() => { throw e; }); } }
        const handler = this['on' + type]; if (typeof handler === 'function') handler.call(this, event);
      };
      BridgedXHR.prototype.open = function (method, url, async, user, password) {
        this._method = String(method || 'GET').toUpperCase(); this._url = new URL(String(url), window.location.href).href; this._async = async !== false; this.readyState = 1;
        if (!isHttpUrl(this._url)) { this._native = true; return this._xhr.open(method, url, async, user, password); }
        this._native = false; this._dispatch('readystatechange');
      };
      BridgedXHR.prototype.setRequestHeader = function (n, v) { if (this._native) return this._xhr.setRequestHeader(n, v); this._headers[String(n)] = String(v); };
      BridgedXHR.prototype.getResponseHeader = function (n) { if (this._native) return this._xhr.getResponseHeader(n); if (!this._responseHeaders) return null; return this._responseHeaders[String(n).toLowerCase()] || null; };
      BridgedXHR.prototype.getAllResponseHeaders = function () { if (this._native) return this._xhr.getAllResponseHeaders(); const h = this._responseHeaders || {}; return Object.keys(h).map((n) => n + ': ' + h[n]).join('\r\n'); };
      BridgedXHR.prototype.send = function (body) {
        if (this._native) return this._xhr.send(body);
        if (this._async === false) throw new Error('Synchronous bridged XHR is not supported');
        const self = this;
        nativeHttpRequest({ url: this._url, method: this._method, headers: this._headers, body })
          .then((result) => {
            const headers = {}; Object.keys(result.headers || {}).forEach((n) => { headers[n.toLowerCase()] = String(result.headers[n]); });
            self._responseHeaders = headers; self.status = Number(result.status || 0); self.statusText = String(result.statusText || ''); self.responseURL = String(result.url || self._url);
            const bytes = base64ToBytes(result.bodyBase64 || '');
            if (self.responseType === 'arraybuffer') { self.response = bytes.buffer; self.responseText = ''; }
            else if (self.responseType === 'blob') { self.response = new Blob([bytes], { type: headers['content-type'] || '' }); self.responseText = ''; }
            else if (self.responseType === 'json') { self.responseText = new TextDecoder().decode(bytes); try { self.response = JSON.parse(self.responseText); } catch (e) { self.response = null; } }
            else { self.responseText = new TextDecoder().decode(bytes); self.response = self.responseText; }
            self.readyState = 4; self._dispatch('readystatechange'); self._dispatch('load'); self._dispatch('loadend');
          })
          .catch(() => { self.readyState = 4; self._dispatch('readystatechange'); self._dispatch('error', { type: 'error', target: self }); self._dispatch('loadend'); });
      };
      BridgedXHR.prototype.abort = function () { if (this._native) return this._xhr.abort(); };
      BridgedXHR.prototype.addEventListener = function (t, fn) { if (this._native) return this._xhr.addEventListener(t, fn); (this._listeners[t] = this._listeners[t] || []).push(fn); };
      BridgedXHR.prototype.removeEventListener = function (t, fn) { if (this._native) return this._xhr.removeEventListener(t, fn); const list = this._listeners[t] || []; const i = list.indexOf(fn); if (i >= 0) list.splice(i, 1); };
      window.XMLHttpRequest = BridgedXHR;
    }
  };

  // Export sendNative into the page world, then run the setup there.
  // GeckoView/Firefox content scripts run in a sandbox; exportFunction bridges
  // a callable into the page's compartment (Xray-safe).
  try {
    const sandboxSendNative = function (payloadJson) {
      return sendNative(payloadJson);
    };
    if (typeof exportFunction === 'function') {
      exportFunction(sandboxSendNative, window, { defineAs: '__wtaSendNative' });
      window.wrappedJSObject.__wta_gecko_bridge_installed__ = false;
      const setupSrc = '(' + pageWorldSetup.toString() + ')(window.__wtaSendNative);';
      window.wrappedJSObject.eval(setupSrc);
    } else {
      // Non-Firefox-Gecko fallback (shouldn't happen on GeckoView, but be safe).
      pageWorldSetup(sendNative);
    }
  } catch (e) {
    console.error('[WTA Native Bridge] install failed:', e);
  }
})();
