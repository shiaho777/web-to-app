package com.webtoapp.core.webview

import android.webkit.CookieManager
import android.webkit.WebResourceResponse
import com.webtoapp.core.logging.AppLogger
import okhttp3.Authenticator
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.Credentials
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayInputStream
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit

/**
 * Fallback proxy path for devices whose system WebView predates PROXY_OVERRIDE
 * (WebView provider < 67, e.g. frozen Android 7.x boxes without Play updates).
 *
 * AndroidX WebKit is only a shim: [androidx.webkit.ProxyController] silently does
 * nothing there, so [PacProxyManager] arms this bridge instead and WebView traffic
 * is fetched through OkHttp with the same proxy settings. Anything that cannot go
 * through [shouldInterceptRequest] (WebSocket, WebRTC, plugins) still connects
 * directly — an documented downgrade, not a full tunnel.
 *
 * Fail-closed by design: when a proxy is configured, fetch failures return HTTP
 * 502 instead of falling back to a direct connection, so a dead proxy can never
 * silently leak the real IP. Only bypass-listed hosts and non-http(s) schemes
 * return null (let WebView handle them).
 *
 * Runs on Chromium's IO thread; all blocking calls below are safe there.
 * Shell-synced: used by generated APKs, reads no host-only state.
 */
object LegacyProxyFetchBridge {

    private const val TAG = "LegacyProxyFetch"

    private const val CONNECT_TIMEOUT_MS = 15_000L
    private const val READ_TIMEOUT_MS = 30_000L
    private const val WRITE_TIMEOUT_MS = 30_000L

    private const val FAIL_LOG_THROTTLE_MS = 10_000L

    enum class Kind { HTTP, SOCKS, BROKEN }

    /**
     * @param kind HTTP/HTTPS-forward proxy or SOCKS5. BROKEN marks a configured
     * proxy OkHttp cannot speak (e.g. HTTPS upstream): every request fail-closes.
     */
    data class Spec(
        val kind: Kind,
        val host: String = "",
        val port: Int = 0,
        val username: String = "",
        val password: String = "",
        val bypassRules: List<String> = emptyList()
    )

    /** Hosts that must never be proxied: local runtimes and bridges live here. */
    private val ALWAYS_BYPASS = setOf("localhost", "127.0.0.1", "[::1]", "::1", "10.0.2.2")

    /** Hop-by-hop / framing headers OkHttp owns; forwarding them corrupts the exchange. */
    private val STRIP_REQUEST_HEADERS = setOf(
        "host", "connection", "keep-alive", "proxy-authorization",
        "transfer-encoding", "content-length", "upgrade", "trailer"
    )

    @Volatile
    private var spec: Spec? = null

    private val lock = Any()
    private var client: OkHttpClient? = null
    private var clientSpec: Spec? = null

    @Volatile
    private var lastFailLogAt = 0L
    @Volatile
    private var lastPostLogAt = 0L

    fun configure(newSpec: Spec?) {
        spec = newSpec
        if (newSpec == null) {
            synchronized(lock) {
                client?.dispatcher?.executorService?.shutdown()
                client?.connectionPool?.evictAll()
                client = null
                clientSpec = null
            }
            AppLogger.d(TAG, "Legacy proxy fallback cleared")
        } else {
            AppLogger.i(TAG, "Legacy proxy fallback armed: ${newSpec.kind} ${newSpec.host}:${newSpec.port}")
        }
    }

    fun clear() = configure(null)

    fun isArmed(): Boolean = spec != null

    /**
     * Resolve STATIC settings to a [Spec]. Returns null when there is nothing to
     * do (mode NONE / DIRECT intent). HTTPS upstream proxies cannot be spoken by
     * OkHttp 4 and resolve to [Kind.BROKEN] (fail-closed) instead of null.
     */
    fun resolveStatic(
        mode: String,
        host: String,
        port: Int,
        type: String,
        bypassRules: List<String>,
        username: String,
        password: String
    ): Spec? {
        if (mode.equals("NONE", ignoreCase = true)) return null
        if (!mode.equals("STATIC", ignoreCase = true)) return null
        if (host.isBlank() || port <= 0) {
            AppLogger.w(TAG, "Legacy proxy: invalid static endpoint, staying direct")
            return null
        }
        return when (type.uppercase()) {
            "SOCKS5", "SOCKS" -> Spec(Kind.SOCKS, host, port, username, password, bypassRules)
            "HTTPS" -> {
                AppLogger.w(TAG, "Legacy proxy: HTTPS upstream unsupported, failing closed")
                Spec(Kind.BROKEN, host, port, username, password, bypassRules)
            }
            else -> Spec(Kind.HTTP, host, port, username, password, bypassRules)
        }
    }

    /**
     * Resolve already-parsed PAC entries (same extraction as [PacProxyManager])
     * to a single [Spec]. Only the first usable entry is honored; multi-proxy
     * PAC failover lists degrade to primary-only on this path.
     */
    fun resolvePacEntries(
        entries: List<String>,
        bypassRules: List<String>,
        username: String,
        password: String
    ): Spec? {
        for (entry in entries) {
            val trimmed = entry.trim()
            when {
                trimmed.equals("DIRECT", ignoreCase = true) -> return null
                trimmed.startsWith("SOCKS5 ", ignoreCase = true) ||
                    trimmed.startsWith("SOCKS ", ignoreCase = true) -> {
                    val server = trimmed.substringAfter(" ").trim()
                    val (h, p) = splitHostPort(server) ?: continue
                    return Spec(Kind.SOCKS, h, p, username, password, bypassRules)
                }
                trimmed.startsWith("HTTPS ", ignoreCase = true) -> {
                    AppLogger.w(TAG, "Legacy proxy: HTTPS PAC entry unsupported, failing closed")
                    val server = trimmed.substringAfter(" ").trim()
                    val (h, p) = splitHostPort(server) ?: return Spec(
                        Kind.BROKEN, "", 0, username, password, bypassRules
                    )
                    return Spec(Kind.BROKEN, h, p, username, password, bypassRules)
                }
                trimmed.startsWith("PROXY ", ignoreCase = true) -> {
                    val server = trimmed.substringAfter(" ").trim()
                    val (h, p) = splitHostPort(server) ?: continue
                    return Spec(Kind.HTTP, h, p, username, password, bypassRules)
                }
                trimmed.isNotBlank() -> {
                    val (h, p) = splitHostPort(trimmed) ?: continue
                    return Spec(Kind.HTTP, h, p, username, password, bypassRules)
                }
            }
        }
        return null
    }

    fun splitHostPort(server: String): Pair<String, Int>? {
        val idx = server.lastIndexOf(':')
        if (idx <= 0) return null
        val port = server.substring(idx + 1).toIntOrNull() ?: return null
        val host = server.substring(0, idx).trim().trim('[', ']')
        if (host.isEmpty() || port <= 0) return null
        return host to port
    }

    fun isBypassed(urlHost: String?, bypassRules: List<String>): Boolean {
        val host = urlHost?.trim()?.lowercase()?.trim('[', ']') ?: return true
        if (host.isEmpty()) return true
        if (host in ALWAYS_BYPASS) return true
        for (raw in bypassRules) {
            val rule = raw.trim().lowercase().trim('[', ']')
            if (rule.isEmpty()) continue
            if (rule == "*") return true
            if (rule.startsWith("*.")) {
                val suffix = rule.removePrefix("*")
                if (host.endsWith(suffix)) return true
            } else if (rule.startsWith(".")) {
                if (host.endsWith(rule)) return true
            } else if (host == rule) {
                return true
            }
        }
        return false
    }

    /**
     * Fetch [url] through the armed proxy. Returns null when the fallback does
     * not apply (disarmed, bypassed host, non-http(s) scheme) so the caller falls
     * through to normal loading; returns a 502 response on fetch failure so a
     * dead proxy can never silently downgrade to direct.
     */
    fun intercept(url: String, method: String?, requestHeaders: Map<String, String>): WebResourceResponse? {
        val active = spec ?: return null
        if (!url.startsWith("http://", ignoreCase = true) &&
            !url.startsWith("https://", ignoreCase = true)
        ) return null

        val targetHost = try {
            android.net.Uri.parse(url).host
        } catch (_: Exception) {
            null
        }
        if (isBypassed(targetHost, active.bypassRules)) return null

        if (active.kind == Kind.BROKEN) {
            throttledFailLog("proxy type unsupported on this path, failing closed: $url")
            return errorResponse(502, "Bad Gateway")
        }

        val httpMethod = (method ?: "GET").uppercase()
        if (httpMethod == "POST" || httpMethod == "PUT" || httpMethod == "PATCH") {
            throttledPostLog(url)
        }

        return try {
            val httpUrl = url.toHttpUrlOrNull() ?: return null
            val builder = Request.Builder().url(httpUrl)
            for ((k, v) in requestHeaders) {
                if (k.lowercase() in STRIP_REQUEST_HEADERS) continue
                if (k.equals("cookie", ignoreCase = true)) continue
                try {
                    builder.header(k, v)
                } catch (_: Exception) {
                }
            }
            val body = if (httpMethod == "GET" || httpMethod == "HEAD") {
                null
            } else {
                // shouldInterceptRequest exposes no request body; forward the
                // method/headers/cookies without one (privacy over fidelity).
                ByteArray(0).toRequestBody(null)
            }
            builder.method(httpMethod, body)

            val okClient = clientFor(active)
            val response = okClient.newCall(builder.build()).execute()
            // NOTE: no .use{} here — the body stream is handed to WebView, which
            // owns (and closes) it. Closing the response here would close the
            // stream out from under the renderer.
            try {
                val code = response.code
                val message = response.message.ifBlank { if (code < 400) "OK" else "Error" }
                val respHeaders = LinkedHashMap<String, String>()
                for (name in response.headers.names()) {
                    // OkHttp already consumed chunk framing; forwarding it would corrupt the body.
                    if (name.equals("transfer-encoding", ignoreCase = true)) continue
                    respHeaders[name] = response.headers.values(name).joinToString(", ")
                }
                val contentType = response.header("Content-Type") ?: "text/plain"
                val mime = contentType.substringBefore(";").trim().ifEmpty { "text/plain" }
                val charset = Regex("charset=([^;]+)", RegexOption.IGNORE_CASE)
                    .find(contentType)?.groupValues?.get(1)?.trim()?.ifEmpty { null } ?: "utf-8"
                val stream = try {
                    response.body?.byteStream()
                } catch (_: Exception) {
                    null
                } ?: ByteArrayInputStream(ByteArray(0))
                WebResourceResponse(mime, charset, code, message, respHeaders, stream)
            } catch (e: Exception) {
                try {
                    response.close()
                } catch (_: Exception) {
                }
                throttledFailLog("response mapping failed, failing closed: ${url.take(120)} (${e.message})")
                errorResponse(502, "Bad Gateway")
            }
        } catch (e: Exception) {
            throttledFailLog("fetch failed, failing closed: ${url.take(120)} (${e.message})")
            errorResponse(502, "Bad Gateway")
        }
    }

    private fun errorResponse(code: Int, reason: String): WebResourceResponse {
        return WebResourceResponse("text/plain", "UTF-8", code, reason, emptyMap(), ByteArrayInputStream(ByteArray(0)))
    }

    private fun clientFor(active: Spec): OkHttpClient {
        synchronized(lock) {
            val cached = client
            if (cached != null && clientSpec == active) return cached
            val proxyType = if (active.kind == Kind.SOCKS) Proxy.Type.SOCKS else Proxy.Type.HTTP
            val proxyAddress = InetSocketAddress(active.host, active.port)
            val user = active.username
            val pass = active.password
            val built = OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .readTimeout(READ_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .writeTimeout(WRITE_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .proxy(Proxy(proxyType, proxyAddress))
                .proxyAuthenticator(
                    if (user.isNotBlank()) {
                        Authenticator { _, response ->
                            if (response.request.header("Proxy-Authorization") != null) {
                                return@Authenticator null
                            }
                            response.request.newBuilder()
                                .header("Proxy-Authorization", Credentials.basic(user, pass))
                                .build()
                        }
                    } else {
                        Authenticator.NONE
                    }
                )
                .cookieJar(WebViewCookieJar())
                .build()
            client = built
            clientSpec = active
            return built
        }
    }

    private fun throttledFailLog(msg: String) {
        val now = System.currentTimeMillis()
        if (now - lastFailLogAt >= FAIL_LOG_THROTTLE_MS) {
            lastFailLogAt = now
            AppLogger.w(TAG, msg)
        } else {
            AppLogger.d(TAG, msg)
        }
    }

    private fun throttledPostLog(url: String) {
        val now = System.currentTimeMillis()
        if (now - lastPostLogAt >= FAIL_LOG_THROTTLE_MS) {
            lastPostLogAt = now
            AppLogger.w(TAG, "Forwarding $url without body (unavailable on this path)")
        }
    }

    /**
     * Bridges OkHttp's cookie jar to the WebView store so sessions survive
     * redirects and subresource hops exactly as with direct loading.
     */
    internal class WebViewCookieJar : CookieJar {
        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            val cm = try {
                CookieManager.getInstance()
            } catch (_: Exception) {
                return
            }
            for (c in cookies) {
                try {
                    cm.setCookie(url.toString(), c.toString())
                } catch (_: Exception) {
                }
            }
            try {
                cm.flush()
            } catch (_: Exception) {
            }
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            val header = try {
                CookieManager.getInstance().getCookie(url.toString())
            } catch (_: Exception) {
                null
            } ?: return emptyList()
            return header.split(";").mapNotNull { part ->
                val kv = part.trim()
                if (kv.isEmpty() || !kv.contains("=")) return@mapNotNull null
                try {
                    Cookie.parse(url, kv)
                } catch (_: Exception) {
                    null
                }
            }
        }
    }
}
