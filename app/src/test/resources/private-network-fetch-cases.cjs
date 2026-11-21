const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');

const script = fs.readFileSync(process.argv[2], 'utf8');
const origin = 'http://localhost:5182';
const api = 'http://127.0.0.1:5183/write';

function setup({ scope = 'CORS_BYPASS', browser, bridge, bridgeAvailable = true } = {}) {
    const calls = [];
    const window = {
        location: new URL(origin),
        __wta_private_network_scope__: scope,
        fetch: async request => {
            calls.push({ transport: 'browser', request });
            if (browser) return browser(request);
            throw new TypeError('Network error');
        },
    };
    if (bridgeAvailable) {
        window.NativeBridge = {
            httpRequestAsync: (raw, id) => {
                const payload = JSON.parse(raw);
                calls.push({ transport: 'bridge', payload });
                const respond = result => window.__wtaNativeHttpResponse(id, JSON.stringify(result));
                if (bridge) bridge(payload, respond);
                else queueMicrotask(() => respond({ ok: true, status: 204 }));
            },
        };
    }
    vm.runInNewContext(script, {
        window, URL, Request, Response, Headers, Blob, ReadableStream, DOMException,
        TextEncoder, Uint8Array, URLSearchParams, ArrayBuffer, atob, btoa,
    });
    return { window, calls, routes: () => calls.map(call => call.transport) };
}

function requestBody(payload) {
    return Buffer.from(payload.bodyBase64, 'base64').toString();
}

async function test() {
    for (const method of ['POST', 'PUT', 'PATCH', 'DELETE']) {
        const { window, calls, routes } = setup();
        const response = await window.fetch(api, { method, body: 'unchanged' });
        assert.equal(response.status, 204);
        assert.equal(response.body, null);
        assert.deepEqual(routes(), ['bridge']);
        assert.equal(calls[0].payload.method, method);
        assert.equal(calls[0].payload.headers.origin, origin);
        assert.equal(requestBody(calls[0].payload), 'unchanged');
    }

    {
        const { window, calls } = setup();
        const request = new Request(api, { method: 'POST', body: 'existing body', headers: { 'X-Old': 'old' } });
        await window.fetch(request, { headers: { 'X-New': 'new' } });
        assert.equal(calls.length, 1);
        assert.equal(requestBody(calls[0].payload), 'existing body');
        assert.equal(calls[0].payload.headers['x-new'], 'new');
        assert.equal(calls[0].payload.headers['x-old'], undefined);
    }

    {
        const { window, calls } = setup();
        const form = new FormData();
        form.append('title', '你好');
        form.append('file', new Blob([new Uint8Array([42, 0, 255])]), 'bytes.dat');
        await window.fetch(api, { method: 'POST', body: form });
        const { payload } = calls[0];
        const contentType = payload.headers['content-type'];
        const boundary = contentType.match(/^multipart\/form-data; boundary=(.+)$/)?.[1];
        assert.ok(boundary, contentType);
        const data = Buffer.from(payload.bodyBase64, 'base64');
        assert.ok(data.includes('--' + boundary));
        assert.ok(data.includes('你好'));
        assert.ok(data.includes(Buffer.from([42, 0, 255])));
    }

    {
        const original = new Response('streamed read');
        const { window, routes } = setup({ browser: () => original });
        assert.equal(await window.fetch(api), original);
        assert.deepEqual(routes(), ['browser']);
    }

    for (const method of ['GET', 'HEAD']) {
        const { window, routes } = setup();
        assert.equal((await window.fetch(api, { method })).body, null);
        assert.deepEqual(routes(), ['browser', 'bridge']);
    }

    for (const options of [{ credentials: 'include' }, { mode: 'no-cors' }, { redirect: 'manual' }, { keepalive: true }]) {
        const { window, routes } = setup();
        await assert.rejects(window.fetch(api, { ...options, method: 'POST', body: 'one' }), /Network error/);
        assert.deepEqual(routes(), ['browser']);
    }

    for (const [url, options] of [[origin + '/write', {}], [api, { scope: 'LOCAL_ONLY' }], [api, { bridgeAvailable: false }]]) {
        const { window, routes } = setup(options);
        await assert.rejects(window.fetch(url, { method: 'POST' }), /Network error/);
        assert.deepEqual(routes(), ['browser']);
    }

    {
        const { window, routes } = setup({ bridge: (_payload, respond) => respond({ ok: false, error: 'REQUEST_FAILED' }) });
        await assert.rejects(window.fetch(api, { method: 'POST' }), /REQUEST_FAILED/);
        assert.deepEqual(routes(), ['bridge']);
    }

    {
        const { window, routes } = setup();
        const controller = new AbortController();
        controller.abort();
        await assert.rejects(window.fetch(api, { method: 'POST', signal: controller.signal }), { name: 'AbortError' });
        assert.deepEqual(routes(), []);
    }

    {
        const controller = new AbortController();
        let respondLater;
        const { window, routes } = setup({ bridge: (_payload, respond) => {
            respondLater = respond;
            controller.abort();
        } });
        await assert.rejects(window.fetch(api, { method: 'POST', signal: controller.signal }), { name: 'AbortError' });
        respondLater({ ok: true, status: 200 });
        assert.deepEqual(routes(), ['bridge']);
        assert.deepEqual(Object.keys(window.__wtaNativeHttpPending), []);
    }
}

test().catch(error => {
    console.error(error);
    process.exitCode = 1;
});
