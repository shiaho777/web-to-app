package com.webtoapp.core.webview

internal object PrivateNetworkXhrScript {
    const val SOURCE: String = """
                if (NativeXHR) {
                    function BridgedXHR() {
                        this._xhr = new NativeXHR();
                        this._listeners = Object.create(null);
                        this._headers = Object.create(null);
                        this._mode = 'idle';
                        this._generation = 0;
                        this.readyState = 0;
                        this.response = null;
                        this.responseText = '';
                        this.responseURL = '';
                        this.status = 0;
                        this.statusText = '';
                        this.timeout = 0;
                        this.withCredentials = false;

                        var self = this;
                        ['readystatechange', 'loadstart', 'load', 'error', 'loadend', 'timeout', 'abort', 'progress'].forEach(function(name) {
                            self._xhr.addEventListener(name, function(event) {
                                self._onBrowserEvent(name, event);
                            });
                        });
                    }

                    BridgedXHR.UNSENT = 0;
                    BridgedXHR.OPENED = 1;
                    BridgedXHR.HEADERS_RECEIVED = 2;
                    BridgedXHR.LOADING = 3;
                    BridgedXHR.DONE = 4;
                    Object.keys(BridgedXHR).forEach(function(key) { BridgedXHR.prototype[key] = BridgedXHR[key]; });

                    BridgedXHR.prototype._onBrowserEvent = function(name, event) {
                        if (this._mode === 'bridge' || this._mode === 'idle') return;
                        var source = this._xhr;
                        if (source.readyState >= 2 && source.status !== 0) this._receivedHeaders = true;
                        if (this._mode === 'probe' && !this._receivedHeaders && source.readyState === 4 && source.status === 0) {
                            if (name === 'readystatechange') return;
                            if (name === 'error') {
                                this._sendBridge(null, false);
                                return;
                            }
                            if (name === 'timeout' || name === 'abort') {
                                this._syncFromNative();
                                this._dispatch('readystatechange');
                            }
                        }
                        this._syncFromNative();
                        this._dispatch(name, event);
                    };

                    BridgedXHR.prototype._syncFromNative = function() {
                        var source = this._xhr;
                        var type = source.responseType || '';
                        this.readyState = source.readyState;
                        this.response = source.response;
                        this.responseText = (type === '' || type === 'text') ? (source.responseText || '') : '';
                        this.responseURL = source.responseURL || '';
                        this.status = source.status || 0;
                        this.statusText = source.statusText || '';
                        this.responseXML = (type === '' || type === 'document') ? source.responseXML : null;
                    };

                    function invokeXhrListener(listener, target, event) {
                        try {
                            if (typeof listener === 'function') listener.call(target, event);
                            else if (listener && listener.handleEvent) listener.handleEvent(event);
                        } catch (error) {
                            if (typeof reportError === 'function') reportError(error);
                            else setTimeout(function() { throw error; }, 0);
                        }
                    }

                    BridgedXHR.prototype._dispatch = function(type, sourceEvent) {
                        var event = sourceEvent || { type: type, target: this, currentTarget: this };
                        var listeners = (this._listeners[type] || []).slice();
                        for (var i = 0; i < listeners.length; i++) {
                            var entry = listeners[i];
                            if (entry.removed) continue;
                            if (entry.once) this.removeEventListener(type, entry.listener, entry.capture);
                            invokeXhrListener(entry.listener, this, event);
                        }
                        invokeXhrListener(this['on' + type], this, event);
                    };

                    BridgedXHR.prototype.open = function(method, url, async, user, password) {
                        this._generation++;
                        if (this._controller) this._controller.abort();
                        this._controller = null;
                        this._mode = 'idle';
                        this._xhr.abort();
                        this._headers = Object.create(null);
                        this._responseHeaders = null;
                        this._receivedHeaders = false;
                        this._sent = false;
                        this._method = String(method || 'GET').toUpperCase();
                        this._url = new URL(String(url), window.location.href).href;
                        this.responseURL = '';
                        this.response = null;
                        this.responseText = '';
                        this.responseXML = null;
                        this.status = 0;
                        this.statusText = '';

                        var isRead = this._method === 'GET' || this._method === 'HEAD';
                        var isCorsBypass = window.__wta_private_network_scope__ === 'CORS_BYPASS';
                        var bridgeable = async !== false && user == null && password == null &&
                            shouldBridge(this._url) && hasNativeHttpRequest();
                        this._canProbe = bridgeable && isRead;
                        if (bridgeable && !isRead && isCorsBypass) {
                            this._mode = 'bridge';
                            this.readyState = 1;
                            this._dispatch('readystatechange');
                        } else {
                            this._mode = 'native';
                            this._xhr.open(method, url, async !== false, user, password);
                            this._xhr.responseType = this.responseType;
                            if (this._mime) this._xhr.overrideMimeType(this._mime);
                        }
                    };

                    BridgedXHR.prototype.setRequestHeader = function(name, value) {
                        if (this.readyState !== 1 || this._sent) throw new DOMException('XHR is not open', 'InvalidStateError');
                        if (this._mode !== 'bridge') this._xhr.setRequestHeader(name, value);
                        var key = String(name).toLowerCase();
                        this._headers[key] = Object.prototype.hasOwnProperty.call(this._headers, key)
                            ? this._headers[key] + ', ' + value : String(value);
                    };

                    BridgedXHR.prototype.getResponseHeader = function(name) {
                        if (this._mode !== 'bridge') return this._xhr.getResponseHeader(name);
                        if (!this._responseHeaders) return null;
                        return this._responseHeaders[String(name).toLowerCase()] || null;
                    };

                    BridgedXHR.prototype.getAllResponseHeaders = function() {
                        if (this._mode !== 'bridge') return this._xhr.getAllResponseHeaders();
                        var headers = this._responseHeaders || {};
                        return Object.keys(headers).map(function(name) { return name + ': ' + headers[name] + '\r\n'; }).join('');
                    };

                    BridgedXHR.prototype.send = function(body) {
                        if (this.readyState !== 1 || this._sent) throw new DOMException('XHR is not open', 'InvalidStateError');
                        this._sent = true;
                        this._startedAt = performance.now();
                        if (this._mode === 'bridge') {
                            this._sendBridge(body, true);
                        } else {
                            this._mode = this._canProbe && !this.withCredentials ? 'probe' : 'native';
                            this._xhr.timeout = this.timeout;
                            this._xhr.withCredentials = this.withCredentials;
                            this._xhr.send(body);
                        }
                    };

                    BridgedXHR.prototype._sendBridge = async function(body, loadStart) {
                        var controller = new AbortController();
                        this._controller = controller;
                        this._mode = 'bridge';
                        if (loadStart) this._dispatch('loadstart');
                        if (this._controller !== controller) return;
                        var remaining = this.timeout - (performance.now() - this._startedAt);
                        var timer = this.timeout > 0 ? setTimeout(() => {
                            if (this._controller !== controller) return;
                            controller.abort();
                            this._finishBridge('timeout');
                        }, Math.max(0, remaining)) : null;
                        var loaded = 0;
                        var total = 0;
                        var lastProgress = -Infinity;
                        var result;
                        try {
                            result = await nativeHttpRequest({
                                url: this._url, method: this._method, headers: this._headers, body: body
                            }, controller.signal, true);
                            if (this._controller !== controller) {
                                releaseBridgeBody(result);
                                return;
                            }
                            var headers = Object.create(null);
                            Object.keys(result.headers || {}).forEach(function(name) {
                                headers[name.toLowerCase()] = String(result.headers[name]);
                            });
                            this._responseHeaders = headers;
                            this.status = Number(result.status || 0);
                            this.statusText = String(result.statusText || '');
                            this.responseURL = String(result.url || this._url);
                            this.readyState = 2;
                            this._dispatch('readystatechange');
                            if (this._controller !== controller) {
                                releaseBridgeBody(result);
                                return;
                            }
                            total = Math.max(0, Number(result.contentLength || 0));

                            var source = await bridgeBody(result, controller.signal);
                            if (source && typeof source.getReader === 'function') {
                                var reader = source.getReader();
                                source = new ReadableStream({
                                    pull: async (stream) => {
                                        var chunk = await reader.read();
                                        if (chunk.done) return stream.close();
                                        loaded += chunk.value.byteLength;
                                        stream.enqueue(chunk.value);
                                        if (this._controller !== controller) return;
                                        if (this.readyState !== 3) {
                                            this.readyState = 3;
                                            this._dispatch('readystatechange');
                                        }
                                        if (this._controller !== controller) return;
                                        var now = performance.now();
                                        if (now - lastProgress >= 50 || loaded === total) {
                                            lastProgress = now;
                                            this._dispatch('progress', { type: 'progress', target: this, currentTarget: this,
                                                loaded: loaded, total: total, lengthComputable: total > 0 });
                                        }
                                    },
                                    cancel: reason => reader.cancel(reason)
                                });
                            }
                            var mime = this._mime || headers['content-type'] || '';
                            var response = new Response(source, { headers: mime ? { 'Content-Type': mime } : {} });
                            var type = this.responseType;
                            var value;
                            if (type === 'blob') value = await response.blob();
                            else if (type === 'arraybuffer') value = await response.arrayBuffer();
                            else {
                                var text = await response.text();
                                if (type === 'json') {
                                    try { value = JSON.parse(text); } catch (_) { value = null; }
                                } else if (type === 'document') {
                                    var markup = mime.split(';', 1)[0].trim();
                                    value = new DOMParser().parseFromString(text,
                                        markup === 'text/html' ? 'text/html' : 'application/xml');
                                } else {
                                    this.responseText = text;
                                    value = text;
                                }
                            }
                            if (this._controller !== controller) return;
                            this.response = value;
                            this.responseXML = type === 'document' ? value : null;
                            this._finishBridge('load', loaded, total);
                        } catch (error) {
                            if (this._controller === controller) this._finishBridge('error');
                        } finally {
                            if (controller.signal.aborted && result) releaseBridgeBody(result);
                            if (timer !== null) clearTimeout(timer);
                        }
                    };

                    BridgedXHR.prototype._finishBridge = function(type, loaded, total) {
                        var generation = this._generation;
                        this._controller = null;
                        if (type !== 'load') {
                            this.status = 0;
                            this.statusText = '';
                            this.response = null;
                            this.responseText = '';
                            this.responseURL = '';
                            this._responseHeaders = null;
                        }
                        this.readyState = 4;
                        this._dispatch('readystatechange');
                        if (this._generation !== generation) return;
                        this._dispatch(type, { type: type, target: this, currentTarget: this,
                            loaded: loaded || 0, total: total || 0, lengthComputable: total > 0 });
                        if (this._generation === generation) this._dispatch('loadend');
                    };

                    BridgedXHR.prototype.abort = function() {
                        if (this._mode !== 'bridge') return this._xhr.abort();
                        var generation = this._generation;
                        if (this._controller) {
                            this._controller.abort();
                            this._finishBridge('abort');
                        }
                        if (this._generation === generation) this.readyState = 0;
                    };

                    function xhrCapture(options) {
                        return typeof options === 'boolean' ? options : !!(options && options.capture);
                    }

                    BridgedXHR.prototype.addEventListener = function(type, listener, options) {
                        if (listener == null || (options && options.signal && options.signal.aborted)) return;
                        var list = this._listeners[type] || (this._listeners[type] = []);
                        var capture = xhrCapture(options);
                        if (list.some(entry => entry.listener === listener && entry.capture === capture)) return;
                        var entry = { listener: listener, capture: capture, once: !!(options && options.once) };
                        if (options && options.signal) {
                            entry.signal = options.signal;
                            entry.abort = () => this.removeEventListener(type, listener, capture);
                            entry.signal.addEventListener('abort', entry.abort, { once: true });
                        }
                        list.push(entry);
                    };

                    BridgedXHR.prototype.removeEventListener = function(type, listener, options) {
                        var list = this._listeners[type] || [];
                        var capture = xhrCapture(options);
                        var index = list.findIndex(entry => entry.listener === listener && entry.capture === capture);
                        if (index < 0) return;
                        var entry = list.splice(index, 1)[0];
                        entry.removed = true;
                        if (entry.signal) entry.signal.removeEventListener('abort', entry.abort);
                    };

                    BridgedXHR.prototype.overrideMimeType = function(mimeType) {
                        this._mime = mimeType;
                        if (this._mode !== 'bridge') this._xhr.overrideMimeType(mimeType);
                    };

                    Object.defineProperty(BridgedXHR.prototype, 'upload', {
                        get: function() { return this._xhr.upload; }
                    });

                    Object.defineProperty(BridgedXHR.prototype, 'responseType', {
                        configurable: true,
                        get: function() { return this._responseType || ''; },
                        set: function(value) {
                            this._responseType = value;
                            if (this._mode === 'native' || this._mode === 'probe') this._xhr.responseType = value;
                        }
                    });

                    window.XMLHttpRequest = BridgedXHR;
                }
"""
}
