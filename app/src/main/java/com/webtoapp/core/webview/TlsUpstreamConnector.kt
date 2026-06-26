package com.webtoapp.core.webview

import com.webtoapp.core.logging.AppLogger
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object TlsUpstreamConnector {

    private const val TAG = "TlsUpstreamConnector"
    private const val CONNECT_TIMEOUT_MS = 15_000
    private const val READ_TIMEOUT_MS = 60_000

    data class TlsResult(
        val sslSocket: SSLSocket,
        val negotiatedProtocol: String?
    )

    fun connect(
        host: String,
        port: Int,
        template: TlsFingerprintTemplate,
        customCipherSuites: List<String> = emptyList(),
        upstreamSocks: LocalHttpToSocksBridge.Upstream? = null
    ): TlsResult {
        val rawSocket = if (upstreamSocks != null) {
            LocalHttpToSocksBridge.Socks5Connector.connect(
                upstreamSocks.host,
                upstreamSocks.port,
                host,
                port,
                upstreamSocks.username,
                upstreamSocks.password
            )
        } else {
            Socket().apply {
                tcpNoDelay = true
                connect(InetSocketAddress(host, port), CONNECT_TIMEOUT_MS)
                soTimeout = READ_TIMEOUT_MS
            }
        }

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, arrayOf<TrustManager>(AcceptAllTrustManager()), null)

        val factory = FingerprintSslSocketFactory(
            sslContext.socketFactory,
            template,
            customCipherSuites
        )

        val sslSocket = factory.createSocket(rawSocket, host, port, true) as SSLSocket
        sslSocket.startHandshake()

        val negotiatedProtocol = try {
            sslSocket.applicationProtocol
        } catch (_: Exception) {
            null
        }

        AppLogger.d(TAG, "TLS upstream connected to $host:$port, template=${template.id}, alpn=$negotiatedProtocol")
        return TlsResult(sslSocket, negotiatedProtocol)
    }

    private class FingerprintSslSocketFactory(
        private val delegate: SSLSocketFactory,
        private val template: TlsFingerprintTemplate,
        private val customCipherSuites: List<String>
    ) : SSLSocketFactory() {

        private fun configure(socket: SSLSocket) {
            val ciphers = if (customCipherSuites.isNotEmpty()) {
                customCipherSuites
            } else {
                template.cipherSuites
            }
            val supported = socket.supportedCipherSuites.toSet()
            val filtered = ciphers.filter { it in supported }
            if (filtered.isNotEmpty()) {
                socket.enabledCipherSuites = filtered.toTypedArray()
            }

            val supportedProtocols = socket.supportedProtocols.toSet()
            val filteredProtocols = template.protocols.filter { it in supportedProtocols }
            if (filteredProtocols.isNotEmpty()) {
                socket.enabledProtocols = filteredProtocols.toTypedArray()
            }

            try {
                val params = socket.sslParameters
                params.applicationProtocols = template.alpn.toTypedArray()
                socket.sslParameters = params
            } catch (_: Exception) {
            }
        }

        override fun getDefaultCipherSuites(): Array<String> = delegate.defaultCipherSuites

        override fun getSupportedCipherSuites(): Array<String> = delegate.supportedCipherSuites

        override fun createSocket(s: Socket?, host: String?, port: Int, autoClose: Boolean): Socket {
            val socket = delegate.createSocket(s, host, port, autoClose) as SSLSocket
            configure(socket)
            return socket
        }

        override fun createSocket(host: String?, port: Int): Socket {
            val socket = delegate.createSocket(host, port) as SSLSocket
            configure(socket)
            return socket
        }

        override fun createSocket(host: String?, port: Int, localHost: InetAddress?, localPort: Int): Socket {
            val socket = delegate.createSocket(host, port, localHost, localPort) as SSLSocket
            configure(socket)
            return socket
        }

        override fun createSocket(host: InetAddress?, port: Int): Socket {
            val socket = delegate.createSocket(host, port) as SSLSocket
            configure(socket)
            return socket
        }

        override fun createSocket(address: InetAddress?, port: Int, localAddress: InetAddress?, localPort: Int): Socket {
            val socket = delegate.createSocket(address, port, localAddress, localPort) as SSLSocket
            configure(socket)
            return socket
        }
    }

    private class AcceptAllTrustManager : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out java.security.cert.X509Certificate>?, authType: String?) {}
        override fun checkServerTrusted(chain: Array<out java.security.cert.X509Certificate>?, authType: String?) {}
        override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = emptyArray()
    }
}
