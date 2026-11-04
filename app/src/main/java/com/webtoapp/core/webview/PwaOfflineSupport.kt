package com.webtoapp.core.webview

import android.webkit.WebView
import com.webtoapp.core.logging.AppLogger

object PwaOfflineSupport {

    private const val TAG = "PwaOfflineSupport"

    enum class CacheStrategy {

        CACHE_FIRST,

        NETWORK_FIRST,

        STALE_WHILE_REVALIDATE
    }

    data class OfflineConfig(
        val enabled: Boolean = false,
        val strategy: CacheStrategy = CacheStrategy.NETWORK_FIRST,
        val maxCacheSizeMb: Int = 50,
        val maxAgeHours: Int = 24 * 7,
        val cacheableExtensions: List<String> = DEFAULT_CACHEABLE_EXTENSIONS,
        val excludePatterns: List<String> = emptyList()
    )

    private val DEFAULT_CACHEABLE_EXTENSIONS = listOf(
        "html", "htm", "css", "js", "json",
        "png", "jpg", "jpeg", "gif", "webp", "svg", "ico",
        "woff", "woff2", "ttf", "eot",
        "mp3", "mp4", "webm"
    )

    private const val SW_SCRIPT_PATH = "/__wta_offline_sw__.js"

    private const val SW_SCRIPT_MARKER = "__wta_offline_sw__"

    @Volatile
    private var currentSwScript: String? = null

    @Volatile
    private var serviceWorkerControllerInstalled = false

    fun generateSwScriptBody(config: OfflineConfig): String {
        val strategyName = when (config.strategy) {
            CacheStrategy.CACHE_FIRST -> "cache-first"
            CacheStrategy.NETWORK_FIRST -> "network-first"
            CacheStrategy.STALE_WHILE_REVALIDATE -> "stale-while-revalidate"
        }

        val extensionsArray = config.cacheableExtensions.joinToString(",") { "'$it'" }
        val excludeArray = config.excludePatterns.joinToString(",") { "'$it'" }
        val maxAgeMs = config.maxAgeHours.toLong() * 3600 * 1000
        val maxCacheBytes = config.maxCacheSizeMb.toLong() * 1024 * 1024

        return """
const CACHE_NAME = 'wta-offline-v1';
const CACHE_PREFIX = 'wta-offline-';
const STRATEGY = '${strategyName}';
const MAX_AGE_MS = ${maxAgeMs};
const MAX_CACHE_BYTES = ${maxCacheBytes};
const CACHEABLE_EXTENSIONS = [${extensionsArray}];
const EXCLUDE_PATTERNS = [${excludeArray}];

function isNavigation(request) {
    return request.mode === 'navigate' || request.destination === 'document';
}

function hasExcludedPattern(url) {
    return EXCLUDE_PATTERNS.some(function(pattern) {
        return url.indexOf(pattern) !== -1;
    });
}

function mayBeCacheable(request) {
    const destination = request.destination;
    if (destination === 'document' || destination === 'script' || destination === 'style' || destination === 'image' || destination === 'font') return true;
    try {
        const ext = new URL(request.url).pathname.split('.').pop().toLowerCase();
        return CACHEABLE_EXTENSIONS.indexOf(ext) !== -1;
    } catch (e) { return false; }
}

function shouldCache(request) {
    if (hasExcludedPattern(request.url)) return false;
    if (isNavigation(request)) return true;
    if (!mayBeCacheable(request)) return false;
    try {
        const u = new URL(request.url);
        return u.protocol === 'https:' || u.protocol === 'http:';
    } catch (e) { return false; }
}

async function trimCache() {
    try {
        const cache = await caches.open(CACHE_NAME);
        const keys = await cache.keys();
        let totalSize = 0;
        const entries = [];
        for (const req of keys) {
            const resp = await cache.match(req);
            if (resp) {
                const blob = await resp.clone().blob();
                const ts = resp.headers.get('x-wta-cached-at');
                entries.push({ req: req, size: blob.size, ts: ts ? parseInt(ts) : 0 });
                totalSize += blob.size;
            }
        }
        const now = Date.now();
        for (const e of entries) {
            if (e.ts > 0 && (now - e.ts) > MAX_AGE_MS) {
                await cache.delete(e.req);
                totalSize -= e.size;
            }
        }
        if (totalSize > MAX_CACHE_BYTES) {
            entries.sort(function(a, b) { return a.ts - b.ts; });
            for (const e of entries) {
                if (totalSize <= MAX_CACHE_BYTES) break;
                await cache.delete(e.req);
                totalSize -= e.size;
            }
        }
    } catch (e) { }
}

async function addToCache(request, response) {
    try {
        const cache = await caches.open(CACHE_NAME);
        if (response.type === 'opaque') {
            await cache.put(request, response.clone());
            return;
        }
        const headers = new Headers(response.headers);
        headers.set('x-wta-cached-at', String(Date.now()));
        const cachedResponse = new Response(await response.clone().blob(), {
            status: response.status,
            statusText: response.statusText,
            headers: headers
        });
        await cache.put(request, cachedResponse);
    } catch (e) { }
}

async function offlineFallback(request) {
    const cached = await caches.match(request);
    if (cached) return cached;
    const ignoredSearch = await caches.match(request, { ignoreSearch: true });
    if (ignoredSearch) return ignoredSearch;
    if (isNavigation(request)) {
        try {
            const scopeRoot = await caches.match(new URL('./', request.url).href);
            if (scopeRoot) return scopeRoot;
        } catch (e) { }
    }
    return new Response('Offline', { status: 503, statusText: 'Offline' });
}

async function cacheFirst(request) {
    const cached = await caches.match(request);
    if (cached) return cached;
    try {
        const response = await fetch(request);
        if (response && response.ok && shouldCache(request)) await addToCache(request, response);
        return response;
    } catch (e) {
        return offlineFallback(request);
    }
}

async function networkFirst(request) {
    try {
        const response = await fetch(request);
        if (response && response.ok && shouldCache(request)) await addToCache(request, response);
        return response;
    } catch (e) {
        return offlineFallback(request);
    }
}

async function staleWhileRevalidate(request) {
    const cached = await caches.match(request);
    const fetchPromise = fetch(request).then(function(response) {
        if (response && response.ok && shouldCache(request)) addToCache(request, response);
        return response;
    }).catch(function() { return null; });
    if (cached) return cached;
    const response = await fetchPromise;
    if (response) return response;
    return offlineFallback(request);
}

self.addEventListener('install', function(event) {
    self.skipWaiting();
});

self.addEventListener('activate', function(event) {
    event.waitUntil(
        caches.keys().then(function(names) {
            return Promise.all(names.filter(function(n) {
                return n.indexOf(CACHE_PREFIX) === 0 && n !== CACHE_NAME;
            }).map(function(n) { return caches.delete(n); }));
        }).then(function() { return self.clients.claim(); })
    );
    trimCache();
});

self.addEventListener('fetch', function(event) {
    const request = event.request;
    if (request.method !== 'GET') return;
    if (hasExcludedPattern(request.url)) return;
    if (!isNavigation(request) && !mayBeCacheable(request)) return;
    let handler;
    switch (STRATEGY) {
        case 'cache-first': handler = cacheFirst; break;
        case 'stale-while-revalidate': handler = staleWhileRevalidate; break;
        default: handler = networkFirst;
    }
    event.respondWith(handler(request));
});
        """.trimIndent()
    }

    internal fun generateRegistrationScript(): String = """
        (function() {
            if (window.__WTA_SW_INJECTED__) return;
            window.__WTA_SW_INJECTED__ = true;
            if (!('serviceWorker' in navigator)) return;
            navigator.serviceWorker.getRegistrations().then(function(regs) {
                var siteWorker = regs.some(function(r) {
                    var worker = r.active || r.installing || r.waiting;
                    var script = worker ? worker.scriptURL : '';
                    return script !== '' && script.indexOf('$SW_SCRIPT_MARKER') === -1;
                });
                if (siteWorker) {
                    console.log('[WTA] Site service worker detected, skipping injected offline worker');
                    return;
                }
                var existing = regs.some(function(r) {
                    return r.active && r.active.scriptURL.indexOf('$SW_SCRIPT_PATH') !== -1;
                });
                if (existing) return;
                var scope = new URL('./', location.href).pathname;
                navigator.serviceWorker.register('$SW_SCRIPT_PATH', { scope: scope })
                    .then(function() { console.log('[WTA] Offline service worker registered at ' + scope); })
                    .catch(function(err) {
                        console.warn('[WTA] Offline service worker registration failed:', err.message);
                    });
            });
        })();
    """.trimIndent()

    private fun installServiceWorkerController() {
        if (serviceWorkerControllerInstalled) return
        serviceWorkerControllerInstalled = true
        ServiceWorkerRequestRouter.addHandler { request ->
            if (request.url?.path != SW_SCRIPT_PATH) return@addHandler null
            val script = currentSwScript ?: return@addHandler null
            android.webkit.WebResourceResponse(
                "application/javascript",
                "UTF-8",
                java.io.ByteArrayInputStream(script.toByteArray(Charsets.UTF_8))
            )
        }
    }

    fun injectServiceWorker(webView: WebView, config: OfflineConfig) {
        if (!config.enabled) return

        installServiceWorkerController()
        currentSwScript = generateSwScriptBody(config)
        webView.evaluateJavascript(generateRegistrationScript()) { result ->
            AppLogger.d(TAG, "Service Worker registration script result: $result")
        }
        AppLogger.i(TAG, "PWA offline support injected (strategy: ${config.strategy})")
    }
}
