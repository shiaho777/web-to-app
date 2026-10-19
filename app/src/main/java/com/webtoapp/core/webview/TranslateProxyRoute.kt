package com.webtoapp.core.webview

import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URL

/**
 * Egress for translation requests.
 *
 * WebView proxy settings ([androidx.webkit.ProxyController], the local SOCKS
 * bridge, the DNS/hosts bridge) do not apply to [java.net.HttpURLConnection].
 * A page that only loads because the app has a proxy would otherwise translate
 * via a direct connection and fail. [PacProxyManager] and the legacy fetch
 * bridge publish the same HTTP or SOCKS endpoint they just armed.
 */
internal object TranslateProxyRoute {

    enum class Kind { DIRECT, HTTP, SOCKS }

    data class Endpoint(val kind: Kind, val host: String, val port: Int)

    @Volatile
    var endpoint: Endpoint = Endpoint(Kind.DIRECT, "", 0)
        private set

    fun useHttp(host: String, port: Int) {
        if (host.isBlank() || port <= 0) {
            clear()
            return
        }
        endpoint = Endpoint(Kind.HTTP, host, port)
    }

    fun useSocks(host: String, port: Int) {
        if (host.isBlank() || port <= 0) {
            clear()
            return
        }
        endpoint = Endpoint(Kind.SOCKS, host, port)
    }

    fun clear() {
        endpoint = Endpoint(Kind.DIRECT, "", 0)
    }

    fun open(url: URL): HttpURLConnection {
        val current = endpoint
        val proxy = when (current.kind) {
            Kind.DIRECT -> null
            Kind.HTTP -> Proxy(Proxy.Type.HTTP, InetSocketAddress(current.host, current.port))
            Kind.SOCKS -> Proxy(Proxy.Type.SOCKS, InetSocketAddress(current.host, current.port))
        }
        val connection = if (proxy == null) url.openConnection() else url.openConnection(proxy)
        return connection as HttpURLConnection
    }
}
