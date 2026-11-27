const assert = require('node:assert/strict');
const {readFileSync} = require('node:fs');
const vm = require('node:vm');

const script = readFileSync(process.argv[2], 'utf8');
const body = new Uint8Array([10, 20, 30]);
const httpCalls = [];
const nativeCalls = [];
const canceled = [];
const released = [];
let reply;
let outcome = 'success';

class BrowserXHR {
    constructor() {
        this.listeners = new Map();
        this.readyState = 0;
        this.status = 0;
    }
    addEventListener(type, listener) {
        this.listeners.set(type, listener);
    }
    emit(type) {
        this.listeners.get(type)?.({type, currentTarget: this, target: this});
    }
    open(method, url) {
        this.method = method;
        this.url = url;
        this.readyState = 1;
        this.emit('readystatechange');
    }
    send() {
        nativeCalls.push(this.method);
        this.emit('loadstart');
        queueMicrotask(() => {
            if (outcome === 'success' || outcome === 'partial') {
                this.status = 200;
                this.readyState = 2;
                this.emit('readystatechange');
            }
            this.readyState = 4;
            this.status = outcome === 'success' ? 200 : 0;
            this.response = outcome === 'success' ? body.buffer : null;
            this.emit('readystatechange');
            this.emit(outcome === 'success' ? 'load' : 'error');
            this.emit('loadend');
        });
    }
    abort() {}
    setRequestHeader() {}
    getResponseHeader() {}
    getAllResponseHeaders() {}
}

const window = {
    location: new URL('http://127.0.0.1:9998/app'),
    __wta_private_network_scope__: 'CORS_BYPASS',
    XMLHttpRequest: BrowserXHR,
    fetch: async () => new Response(body),
    NativeBridge: {
        httpRequestStreamAsync(raw, id) {
            const request = JSON.parse(raw);
            httpCalls.push(request);
            const respond = () => window.__wtaNativeHttpResponse(id, JSON.stringify({
                ok: true, status: 200, streamUrl: '/.webtoapp/stream/test',
                url: request.url, headers: {}, contentLength: body.length
            }));
            if (outcome === 'hold') reply = respond;
            else queueMicrotask(respond);
        },
        cancelHttpRequest(id) { canceled.push(id); },
        releaseStreamUrl(url) { released.push(url); },
        httpRequest() {
            assert.fail('A streamed XHR must not use the buffered bridge');
        }
    }
};
vm.runInNewContext(script, {
    window, URL, performance, AbortController, DOMException, setTimeout, clearTimeout, Response,
    TextEncoder, TextDecoder, Uint8Array, btoa, atob, ReadableStream, ArrayBuffer
});

async function request(method, withCredentials = false) {
    const xhr = new window.XMLHttpRequest();
    const completed = new Promise(resolve => { xhr.onloadend = resolve; });
    const events = [];
    xhr.onload = () => events.push('load');
    xhr.onerror = () => events.push('error');
    xhr.onprogress = event => events.push(event.loaded);
    xhr.open(method, 'http://127.0.0.1:9999/data');
    xhr.responseType = 'arraybuffer';
    xhr.withCredentials = withCredentials;
    xhr.send();
    await completed;
    return { xhr, events };
}

(async () => {
    let result = await request('GET');
    assert.equal(result.xhr.status, 200);
    assert.deepEqual(result.events, ['load']);
    assert.equal(httpCalls.length, 0);

    outcome = 'cors';
    result = await request('GET');
    assert.deepEqual(Array.from(new Uint8Array(result.xhr.response)), Array.from(body));
    assert.deepEqual(result.events, [3, 'load']);
    assert.equal(httpCalls.length, 1);

    result = await request('GET', true);
    assert.deepEqual(result.events, ['error']);
    assert.equal(httpCalls.length, 1);

    outcome = 'partial';
    result = await request('GET');
    assert.deepEqual(result.events, ['error']);
    assert.equal(httpCalls.length, 1);

    result = await request('POST');
    assert.deepEqual(result.events, [3, 'load']);
    assert.deepEqual(nativeCalls, ['GET','GET','GET','GET']);
    assert.deepEqual(httpCalls.map(call => call.method), ['GET','POST']);

    const canceledXhr = new window.XMLHttpRequest();
    const canceledEvents = [];
    const settled = () => new Promise(resolve => setTimeout(resolve, 0));
    canceledXhr.onabort = () => canceledEvents.push('abort');
    canceledXhr.onload = () => canceledEvents.push('load');
    canceledXhr.onloadend = () => canceledEvents.push('loadend');
    outcome = 'hold';
    canceledXhr.open('POST', 'http://127.0.0.1:9999/hold');
    canceledXhr.send();
    await settled();
    canceledXhr.abort();
    assert.equal(canceled.length, 1);
    reply();
    await settled();
    assert.deepEqual(canceledEvents, ['abort', 'loadend']);
    assert.equal(canceledXhr.readyState, 0);

    const timed = new window.XMLHttpRequest();
    const timedEvent = new Promise(resolve => { timed.ontimeout = resolve; });
    timed.open('POST', 'http://127.0.0.1:9999/timeout');
    timed.timeout = 20;
    timed.send();
    await timedEvent;
    assert.equal(canceled.length, 2);
    assert.equal(timed.status, 0);

    const reopened = new window.XMLHttpRequest();
    const reentrantEvents = [];
    const removed = new AbortController();
    reopened.addEventListener('loadend', () => assert.fail('removed listener fired'), { signal: removed.signal });
    removed.abort();
    reopened.addEventListener('loadend', () => reentrantEvents.push('once'), { once: true });
    const finished = new Promise(resolve => { reopened.onloadend = resolve; });
    reopened.onload = () => reentrantEvents.push('load');
    reopened.open('POST', 'http://127.0.0.1:9999/old');
    reopened.onreadystatechange = () => {
        if (reopened.readyState !== 2) return;
        reopened.onreadystatechange = null;
        outcome = 'success';
        reopened.open('POST', 'http://127.0.0.1:9999/new');
        reopened.send();
    };
    reopened.send();
    await settled();
    reply();
    await finished;
    assert.deepEqual(reentrantEvents, ['load', 'once']);
    assert.ok(released.includes('/.webtoapp/stream/test'));
})().catch(error => {
    console.error(error);
    process.exitCode = 1;
});
