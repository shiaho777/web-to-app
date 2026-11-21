package com.webtoapp.core.activation

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.security.MessageDigest
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

object ActivationCertPin {

    private const val FINGERPRINT_HEX_LENGTH = 64

    fun hostOf(url: String): String? =
        url.trim().toHttpUrlOrNull()?.host?.takeIf { it.isNotBlank() }

    fun sha256Hex(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256")
            .digest(bytes)
            .joinToString(separator = "") { byte -> "%02X".format(byte) }

    fun normalizeFingerprint(raw: String): String? {
        var cleaned = raw.trim()
        if (cleaned.startsWith("SHA256 Fingerprint=", ignoreCase = true)) {
            cleaned = cleaned.substringAfter('=').trim()
        }
        if (cleaned.startsWith("sha256//", ignoreCase = true)) {
            cleaned = cleaned.substringAfter("//").trim()
        }
        cleaned = cleaned
            .replace(":", "")
            .replace(" ", "")
            .replace("\n", "")
            .replace("\r", "")
            .replace("\t", "")
            .uppercase()
        if (cleaned.length != FINGERPRINT_HEX_LENGTH) return null
        return cleaned.takeIf { value -> value.all { it in '0'..'9' || it in 'A'..'F' } }
    }

    fun fingerprintOfPem(pem: String): String? {
        val trimmed = pem.trim()
        if (!trimmed.contains("BEGIN CERTIFICATE")) return null
        return try {
            val cleaned = trimmed
                .replace("-----BEGIN CERTIFICATE-----", "")
                .replace("-----END CERTIFICATE-----", "")
                .replace("\\s".toRegex(), "")
            val der = android.util.Base64.decode(cleaned, android.util.Base64.DEFAULT)
            val certificate = CertificateFactory.getInstance("X.509")
                .generateCertificate(der.inputStream()) as X509Certificate
            sha256Hex(certificate.encoded)
        } catch (_: Exception) {
            null
        }
    }

    fun resolveFingerprint(raw: String): String? {
        if (raw.isBlank()) return null
        return if (raw.contains("BEGIN CERTIFICATE")) fingerprintOfPem(raw) else normalizeFingerprint(raw)
    }

    fun validatePinnedConfig(
        verifyUrl: String,
        pinnedCertificate: String,
        pinnedCertHost: String
    ): String? {
        if (resolveFingerprint(pinnedCertificate) == null) {
            return "pinned certificate is not a SHA-256 fingerprint or PEM certificate"
        }
        val host = hostOf(verifyUrl) ?: return "verifyUrl is not a valid URL"
        if (pinnedCertHost.isBlank()) return "pinnedCertHost must be set in pinned trust mode"
        if (!pinnedCertHost.equals(host, ignoreCase = true)) {
            return "pinnedCertHost does not match verifyUrl host"
        }
        return null
    }
}

internal class PinnedCertificateMismatchException :
    CertificateException("Server certificate fingerprint mismatch")

internal class PinnedCertificateTrustManager(
    private val expectedFingerprint: String
) : X509TrustManager {

    override fun checkClientTrusted(chain: Array<out X509Certificate>, authType: String) {
        throw CertificateException("Client certificate authentication is not supported")
    }

    override fun checkServerTrusted(chain: Array<out X509Certificate>, authType: String) {
        val leaf = chain.firstOrNull() ?: throw CertificateException("Empty server certificate chain")
        leaf.checkValidity()
        val actual = ActivationCertPin.sha256Hex(leaf.encoded)
        if (!actual.equals(expectedFingerprint, ignoreCase = true)) {
            throw PinnedCertificateMismatchException()
        }
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
}
