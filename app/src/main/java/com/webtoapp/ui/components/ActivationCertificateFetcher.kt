package com.webtoapp.ui.components

import com.webtoapp.core.activation.ActivationCertPin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.net.InetSocketAddress
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.SNIHostName
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

data class FetchedServerCertificate(
    val host: String,
    val sha256: String,
    val subject: String,
    val issuer: String,
    val notBefore: Long,
    val notAfter: Long,
    val subjectAltNames: List<String>
)

suspend fun fetchServerCertificate(url: String): Result<FetchedServerCertificate> =
    withContext(Dispatchers.IO) {
        runCatching {
            val httpUrl = url.trim().toHttpUrlOrNull()
                ?: throw IllegalArgumentException("Invalid verification URL")
            if (!httpUrl.isHttps) throw IllegalArgumentException("HTTPS is required")
            val host = httpUrl.host
            val port = if (httpUrl.port != -1) httpUrl.port else 443

            val probeTrustManager = object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>, authType: String) = Unit
                override fun checkServerTrusted(chain: Array<out X509Certificate>, authType: String) = Unit
                override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
            }
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, arrayOf<TrustManager>(probeTrustManager), SecureRandom())

            val socket = sslContext.socketFactory.createSocket() as SSLSocket
            socket.use {
                it.useClientMode = true
                it.connect(InetSocketAddress(host, port), 10_000)
                it.soTimeout = 10_000
                if (!isIpLiteral(host)) {
                    it.sslParameters = it.sslParameters.apply {
                        serverNames = listOf(SNIHostName(host))
                    }
                }
                it.startHandshake()
                val certificate = it.session.peerCertificates.firstOrNull() as? X509Certificate
                    ?: throw CertificateException("Server did not present a certificate")
                FetchedServerCertificate(
                    host = host,
                    sha256 = ActivationCertPin.sha256Hex(certificate.encoded),
                    subject = certificate.subjectX500Principal.name,
                    issuer = certificate.issuerX500Principal.name,
                    notBefore = certificate.notBefore.time,
                    notAfter = certificate.notAfter.time,
                    subjectAltNames = certificate.subjectAlternativeNames
                        ?.mapNotNull { entry ->
                            when (entry.getOrNull(0)) {
                                2 -> entry.getOrNull(1)?.let { "DNS:$it" }
                                7 -> entry.getOrNull(1)?.let { "IP:$it" }
                                else -> null
                            }
                        }
                        .orEmpty()
                )
            }
        }
    }

private fun isIpLiteral(host: String): Boolean =
    host.matches(Regex("^\\d{1,3}(\\.\\d{1,3}){3}$")) || host.contains(':')
