package com.webtoapp.core.activation

import android.util.Base64
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.webtoapp.core.i18n.Strings
import com.webtoapp.data.model.RemoteActivationOfflinePolicy
import com.webtoapp.data.model.RemoteActivationTrustMode
import kotlinx.coroutines.test.runTest
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.BasicConstraints
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.security.spec.ECGenParameterSpec
import java.util.Date

@RunWith(RobolectricTestRunner::class)
class RemoteActivationPinTest {

    private val verifier = RemoteActivationVerifier(ApplicationProvider.getApplicationContext())

    @Test
    fun `normalizeFingerprint accepts hex colon and openssl formats`() {
        val hex = "AB".repeat(32)
        val colonHex = hex.chunked(2).joinToString(":")
        assertThat(ActivationCertPin.normalizeFingerprint(hex.lowercase())).isEqualTo(hex)
        assertThat(ActivationCertPin.normalizeFingerprint(colonHex)).isEqualTo(hex)
        assertThat(ActivationCertPin.normalizeFingerprint("SHA256 Fingerprint=$colonHex")).isEqualTo(hex)
        assertThat(ActivationCertPin.normalizeFingerprint("  $hex  ")).isEqualTo(hex)
    }

    @Test
    fun `normalizeFingerprint rejects non hex and wrong lengths`() {
        assertThat(ActivationCertPin.normalizeFingerprint("")).isNull()
        assertThat(ActivationCertPin.normalizeFingerprint("AB".repeat(31))).isNull()
        assertThat(ActivationCertPin.normalizeFingerprint("ZZ".repeat(32))).isNull()
        assertThat(
            ActivationCertPin.normalizeFingerprint("sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
        ).isNull()
    }

    @Test
    fun `fingerprintOfPem round trips a generated certificate`() {
        val certificate = newSelfSigned("CN=activation.example")
        val pem = toPem(certificate)
        val expected = ActivationCertPin.sha256Hex(certificate.encoded)
        assertThat(ActivationCertPin.fingerprintOfPem(pem)).isEqualTo(expected)
        assertThat(ActivationCertPin.resolveFingerprint(pem)).isEqualTo(expected)
    }

    @Test
    fun `fingerprintOfPem rejects malformed input`() {
        assertThat(ActivationCertPin.fingerprintOfPem("not a certificate")).isNull()
        assertThat(
            ActivationCertPin.fingerprintOfPem(
                "-----BEGIN CERTIFICATE-----\nAAAA\n-----END CERTIFICATE-----"
            )
        ).isNull()
    }

    @Test
    fun `pinned trust manager accepts the matching certificate`() {
        val certificate = newSelfSigned("CN=activation.example")
        val trustManager = PinnedCertificateTrustManager(
            ActivationCertPin.sha256Hex(certificate.encoded)
        )
        trustManager.checkServerTrusted(arrayOf(certificate), "ECDHE_RSA")
    }

    @Test
    fun `pinned trust manager rejects a different certificate`() {
        val pinned = newSelfSigned("CN=pinned.example")
        val other = newSelfSigned("CN=other.example")
        val trustManager = PinnedCertificateTrustManager(
            ActivationCertPin.sha256Hex(pinned.encoded)
        )
        assertThrows(CertificateException::class.java) {
            trustManager.checkServerTrusted(arrayOf(other), "ECDHE_RSA")
        }
    }

    @Test
    fun `validatePinnedConfig accepts a matching host`() {
        val certificate = newSelfSigned("CN=activation.example")
        val error = ActivationCertPin.validatePinnedConfig(
            verifyUrl = "https://activation.example/verify",
            pinnedCertificate = toPem(certificate),
            pinnedCertHost = "activation.example"
        )
        assertThat(error).isNull()
    }

    @Test
    fun `validatePinnedConfig rejects host mismatch and invalid pins`() {
        val certificate = newSelfSigned("CN=activation.example")
        assertThat(
            ActivationCertPin.validatePinnedConfig(
                verifyUrl = "https://activation.example/verify",
                pinnedCertificate = toPem(certificate),
                pinnedCertHost = "other.example"
            )
        ).isNotNull()
        assertThat(
            ActivationCertPin.validatePinnedConfig(
                verifyUrl = "https://activation.example/verify",
                pinnedCertificate = "not-a-pin",
                pinnedCertHost = "activation.example"
            )
        ).isNotNull()
        assertThat(
            ActivationCertPin.validatePinnedConfig(
                verifyUrl = "https://activation.example/verify",
                pinnedCertificate = toPem(certificate),
                pinnedCertHost = ""
            )
        ).isNotNull()
    }

    @Test
    fun `verify rejects an invalid pinned fingerprint before any request`() = runTest {
        val result = verifier.verify(
            appId = -1L,
            request = request(
                trustMode = RemoteActivationTrustMode.PINNED,
                pinnedCertificate = "not-a-pin",
                pinnedCertHost = "example.com"
            )
        )
        assertThat(result).isInstanceOf(ActivationResult.Invalid::class.java)
        assertThat((result as ActivationResult.Invalid).message)
            .isEqualTo(Strings.remoteActivationPinInvalid)
    }

    @Test
    fun `verify rejects a pinned certificate without a host`() = runTest {
        val certificate = newSelfSigned("CN=example.com")
        val result = verifier.verify(
            appId = -1L,
            request = request(
                trustMode = RemoteActivationTrustMode.PINNED,
                pinnedCertificate = ActivationCertPin.sha256Hex(certificate.encoded),
                pinnedCertHost = ""
            )
        )
        assertThat(result).isInstanceOf(ActivationResult.Invalid::class.java)
        assertThat((result as ActivationResult.Invalid).message)
            .isEqualTo(Strings.remoteActivationPinHostMismatch)
    }

    @Test
    fun `verify refuses to apply a pin to a different host`() = runTest {
        val certificate = newSelfSigned("CN=example.com")
        val result = verifier.verify(
            appId = -1L,
            request = request(
                trustMode = RemoteActivationTrustMode.PINNED,
                pinnedCertificate = ActivationCertPin.sha256Hex(certificate.encoded),
                pinnedCertHost = "other.example"
            )
        )
        assertThat(result).isInstanceOf(ActivationResult.Invalid::class.java)
        assertThat((result as ActivationResult.Invalid).message)
            .isEqualTo(Strings.remoteActivationPinHostMismatch)
    }

    private fun request(
        trustMode: RemoteActivationTrustMode,
        pinnedCertificate: String,
        pinnedCertHost: String
    ): RemoteActivationVerifier.RemoteRequest {
        val keyPair = KeyPairGenerator.getInstance("EC").apply {
            initialize(ECGenParameterSpec("secp256r1"))
        }.generateKeyPair()
        return RemoteActivationVerifier.RemoteRequest(
            verifyUrl = "https://example.com/verify",
            publicKeyBase64 = Base64.encodeToString(keyPair.public.encoded, Base64.NO_WRAP),
            offlinePolicy = RemoteActivationOfflinePolicy.DENY,
            code = "TEST-CODE",
            deviceId = "device",
            packageName = "pkg",
            trustMode = trustMode,
            pinnedCertificate = pinnedCertificate,
            pinnedCertHost = pinnedCertHost
        )
    }

    private fun newSelfSigned(subject: String): X509Certificate {
        val keyPair = newKeyPair()
        val name = X500Name(subject)
        val now = Date()
        val builder = JcaX509v3CertificateBuilder(
            name,
            BigInteger.valueOf(System.nanoTime()),
            Date(now.time - 60_000),
            Date(now.time + 86_400_000L),
            name,
            keyPair.public
        )
        builder.addExtension(Extension.basicConstraints, true, BasicConstraints(true))
        val signer = JcaContentSignerBuilder("SHA256withRSA").build(keyPair.private)
        return JcaX509CertificateConverter().getCertificate(builder.build(signer))
    }

    private fun newKeyPair(): KeyPair =
        KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()

    private fun toPem(certificate: X509Certificate): String = buildString {
        appendLine("-----BEGIN CERTIFICATE-----")
        appendLine(Base64.encodeToString(certificate.encoded, Base64.NO_WRAP))
        appendLine("-----END CERTIFICATE-----")
    }
}
