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
        val hostBytes = literalAddressBytes(host) ?: return false
        val sans = runCatching { cert.subjectAlternativeNames }.getOrNull() ?: return false
        for (san in sans) {
            if (san.getOrNull(0) != 7) continue
            val certBytes = when (val value = san.getOrNull(1)) {
                is ByteArray -> value.takeIf { it.size == 4 || it.size == 16 }
                is String -> literalAddressBytes(value.trim())
                else -> null
            } ?: continue
            if (certBytes.contentEquals(hostBytes)) return true
        }
        return false
    }

    private fun literalAddressBytes(host: String): ByteArray? {
        val cleaned = host.trim().removeSurrounding("[", "]")
        if (cleaned.isEmpty()) return null
        val isLiteral = cleaned.contains(':') ||
            (cleaned.all { it.isDigit() || it == '.' } && cleaned.count { it == '.' } == 3)
        if (!isLiteral) return null
        return runCatching { java.net.InetAddress.getByName(cleaned).address }.getOrNull()
    }

    private fun cnValues(cert: X509Certificate): List<String> = runCatching {
        val name = X500Name.getInstance(cert.subjectX500Principal.encoded)
        name.getRDNs(org.bouncycastle.asn1.x500.style.BCStyle.CN)
            .mapNotNull { rdn -> rdn.first?.value?.toString() }
    }.getOrDefault(emptyList())
}
