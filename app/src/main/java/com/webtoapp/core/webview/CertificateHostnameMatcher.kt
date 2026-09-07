package com.webtoapp.core.webview

import org.bouncycastle.asn1.x500.X500Name
import java.security.cert.X509Certificate
import java.util.Locale

/**
 * RFC 6125 hostname matching for [TlsUpstreamConnector]: the bridge must verify that the
 * upstream server's certificate actually covers the host it was contacted as. Plain
 * [javax.net.ssl.SSLSocket] handshakes validate the chain but never the hostname, so without
 * this check the fingerprint-spoofing path would accept a valid certificate for the wrong
 * domain (a static-exhaustion / mis-issuance hole an active attacker can aim at).
 *
 * Pure JVM, unit-testable: SubjectAlternativeName dNSName / iPAddress entries win (wildcards
 * match exactly one left-most label), CN is only consulted when the certificate has no SANs
 * of either kind (legacy certs).
 */
object CertificateHostnameMatcher {

    fun matches(host: String, cert: X509Certificate): Boolean {
        val normalizedHost = host.trim().lowercase(Locale.ROOT).trimEnd('.').removeSurrounding("[", "]")
        if (normalizedHost.isEmpty()) return false

        // CONNECT targets arrive with IPv6 brackets already stripped by the caller.
        val isIpv6 = normalizedHost.contains(':')
        val isIpv4 = normalizedHost.isNotEmpty() &&
            normalizedHost.all { it.isDigit() || it == '.' } &&
            normalizedHost.count { it == '.' } == 3
        if (isIpv6 || isIpv4) {
            return matchesIpAddress(normalizedHost, cert)
        }

        val sans = runCatching { cert.subjectAlternativeNames }.getOrNull()
        var sawDnsSan = false
        if (sans != null) {
            for (san in sans) {
                val type = san.getOrNull(0)
                val value = san.getOrNull(1) as? String ?: continue
                when (type) {
                    2 -> { // dNSName
                        sawDnsSan = true
                        if (matchesDnsName(normalizedHost, value.trim().lowercase(Locale.ROOT))) return true
                    }
                    7 -> { // iPAddress
                        if (matchesIpAddress(normalizedHost.removeSurrounding("[", "]"), cert)) return true
                    }
                }
            }
        }

        // CN fallback only for certificates without any DNS SAN entry.
        if (sawDnsSan) return false
        return cnValues(cert).any { cn ->
            matchesDnsName(normalizedHost, cn.trim().lowercase(Locale.ROOT).trimEnd('.'))
        }
    }

    private fun matchesDnsName(host: String, pattern: String): Boolean {
        if (pattern.isEmpty() || host.isEmpty()) return false
        if (pattern == host) return true
        if (!pattern.startsWith("*.")) return false
        // "*.example.com" matches exactly one left-most label: "a.example.com" yes,
        // "example.com" / "a.b.example.com" no. The wildcard label itself must not be empty.
        val suffix = pattern.substring(1) // ".example.com"
        if (!host.endsWith(suffix)) return false
        val label = host.substring(0, host.length - suffix.length)
        return label.isNotEmpty() && !label.contains('.')
    }

    private fun matchesIpAddress(host: String, cert: X509Certificate): Boolean {
        val sans = runCatching { cert.subjectAlternativeNames }.getOrNull() ?: return false
        for (san in sans) {
            if (san.getOrNull(0) != 7) continue
            when (val value = san.getOrNull(1)) {
                is ByteArray -> {
                    val certAddr = bytesToIpAddressString(value) ?: continue
                    if (certAddr == host) return true
                }
                is String -> {
                    if (value.trim() == host) return true
                }
            }
        }
        return false
    }

    private fun bytesToIpAddressString(bytes: ByteArray): String? = when (bytes.size) {
        4 -> bytes.joinToString(".") { (it.toInt() and 0xFF).toString() }
        16 -> bytes.toList().chunked(2).joinToString(":") {
            ((it[0].toInt() and 0xFF) shl 8 or (it[1].toInt() and 0xFF)).toString(16)
        }
        else -> null
    }

    private fun cnValues(cert: X509Certificate): List<String> = runCatching {
        val name = X500Name.getInstance(cert.subjectX500Principal.encoded)
        name.getRDNs(org.bouncycastle.asn1.x500.style.BCStyle.CN)
            .mapNotNull { rdn -> rdn.first?.value?.toString() }
    }.getOrDefault(emptyList())
}
