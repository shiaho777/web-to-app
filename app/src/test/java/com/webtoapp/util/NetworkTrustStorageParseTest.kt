package com.webtoapp.util

import com.google.common.truth.Truth.assertThat
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.BasicConstraints
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.junit.Test
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.cert.X509Certificate
import java.util.Base64
import java.util.Date

class NetworkTrustStorageParseTest {

    @Test
    fun `parses single PEM certificate`() {
        val (caCert, _) = newCa("CN=Test Root")
        val parsed = NetworkTrustStorage.parseCertificates(caCert.toPem())
        assertThat(parsed).hasSize(1)
        assertThat(parsed[0].subjectX500Principal.name).contains("Test Root")
    }

    @Test
    fun `parses PEM chain`() {
        val (caCert, caKey) = newCa("CN=Chain Root")
        val leaf = newLeaf("CN=leaf", caCert, caKey)
        val chain = caCert.toPem() + leaf.toPem()
        val parsed = NetworkTrustStorage.parseCertificates(chain)
        assertThat(parsed).hasSize(2)
    }

    @Test
    fun `parses DER certificate`() {
        val (caCert, _) = newCa("CN=DER Root")
        val parsed = NetworkTrustStorage.parseCertificates(caCert.encoded)
        assertThat(parsed).hasSize(1)
    }

    @Test
    fun `parses PEM with UTF-8 BOM`() {
        val (caCert, _) = newCa("CN=BOM Root")
        val bom = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
        val pem = caCert.toPem()
        val withBom = bom + pem
        val parsed = NetworkTrustStorage.parseCertificates(withBom)
        assertThat(parsed).hasSize(1)
    }

    @Test
    fun `parses UTF-16LE encoded PEM`() {
        val (caCert, _) = newCa("CN=UTF16 Root")
        val pem = caCert.toPem()
        // Windows "Unicode" save: UTF-16LE with BOM.
        val utf16 = (String(pem, StandardCharsets.US_ASCII))
            .toByteArray(StandardCharsets.UTF_16LE)
        val withBom = byteArrayOf(0xFF.toByte(), 0xFE.toByte()) + utf16
        val parsed = NetworkTrustStorage.parseCertificates(withBom)
        assertThat(parsed).hasSize(1)
    }

    @Test
    fun `parses TRUSTED CERTIFICATE pem label`() {
        val (caCert, _) = newCa("CN=Trusted Label")
        val pem = String(caCert.toPem(), StandardCharsets.US_ASCII)
            .replace("BEGIN CERTIFICATE", "BEGIN TRUSTED CERTIFICATE")
            .replace("END CERTIFICATE", "END TRUSTED CERTIFICATE")
        val parsed = NetworkTrustStorage.parseCertificates(pem.toByteArray(StandardCharsets.US_ASCII))
        assertThat(parsed).hasSize(1)
    }

    @Test
    fun `parses raw base64 without pem armor`() {
        val (caCert, _) = newCa("CN=Bare Base64")
        val b64 = Base64.getMimeEncoder().encodeToString(caCert.encoded).replace("\r", "").replace("\n", "")
        val parsed = NetworkTrustStorage.parseCertificates(b64.toByteArray(StandardCharsets.US_ASCII))
        assertThat(parsed).hasSize(1)
    }

    @Test
    fun `private key blob reports private key reason`() {
        val keyPair = newKeyPair()
        val pkcs8 = keyPair.private.encoded
        val pem = "-----BEGIN PRIVATE KEY-----\n" +
            Base64.getMimeEncoder().encodeToString(pkcs8) +
            "\n-----END PRIVATE KEY-----\n"
        val ex = runCatching { NetworkTrustStorage.parseCertificates(pem.toByteArray()) }.exceptionOrNull()
        assertThat(ex).isInstanceOf(NetworkTrustStorage.InvalidCertificateException::class.java)
        assertThat((ex as NetworkTrustStorage.InvalidCertificateException).reason)
            .isEqualTo(NetworkTrustStorage.InvalidReason.PRIVATE_KEY)
    }

    @Test
    fun `unrecognized blob reports unrecognized reason`() {
        val ex = runCatching {
            NetworkTrustStorage.parseCertificates("not a certificate at all".toByteArray())
        }.exceptionOrNull()
        assertThat(ex).isInstanceOf(NetworkTrustStorage.InvalidCertificateException::class.java)
        assertThat((ex as NetworkTrustStorage.InvalidCertificateException).reason)
            .isEqualTo(NetworkTrustStorage.InvalidReason.UNRECOGNIZED)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `empty blob throws`() {
        NetworkTrustStorage.parseCertificates(ByteArray(0))
    }

    // --- helpers ---

    private fun X509Certificate.toPem(): ByteArray =
        ("-----BEGIN CERTIFICATE-----\n" +
            Base64.getMimeEncoder().encodeToString(encoded) +
            "\n-----END CERTIFICATE-----\n").toByteArray(StandardCharsets.US_ASCII)

    private fun newKeyPair(): KeyPair =
        KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()

    private fun newCa(subject: String): Pair<X509Certificate, KeyPair> {
        val kp = newKeyPair()
        val name = X500Name(subject)
        val now = Date()
        val builder = JcaX509v3CertificateBuilder(
            name, BigInteger.valueOf(System.currentTimeMillis()),
            now, Date(now.time + 365L * 24 * 3600 * 1000),
            name, kp.public
        )
        builder.addExtension(Extension.basicConstraints, true, BasicConstraints(true))
        val signer = JcaContentSignerBuilder("SHA256withRSA").build(kp.private)
        val cert = JcaX509CertificateConverter().getCertificate(builder.build(signer))
        return cert to kp
    }

    private fun newLeaf(subject: String, issuer: X509Certificate, issuerKey: KeyPair): X509Certificate {
        val kp = newKeyPair()
        val issuerName = X500Name(issuer.subjectX500Principal.name)
        val now = Date()
        val builder = JcaX509v3CertificateBuilder(
            issuerName, BigInteger.valueOf(System.currentTimeMillis() + 1),
            now, Date(now.time + 365L * 24 * 3600 * 1000),
            X500Name(subject), kp.public
        )
        val signer = JcaContentSignerBuilder("SHA256withRSA").build(issuerKey.private)
        return JcaX509CertificateConverter().getCertificate(builder.build(signer))
    }
}
