package com.webtoapp.core.webview

import com.google.common.truth.Truth.assertThat
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.BasicConstraints
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.junit.After
import org.junit.Test
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.cert.X509Certificate
import java.util.Date

class CustomCaValidatorTest {

    @After
    fun tearDown() {
        CustomCaTrustStore.reset()
    }

    @Test
    fun `leaf signed by imported ca is trusted`() {
        val (caCert, caKey) = newCa("CN=Imported Root")
        val leaf = newLeaf("CN=server.example", caCert, caKey)
        CustomCaTrustStore.initFromDer(listOf(caCert.encoded))

        assertThat(CustomCaTrustStore.hasAnchors()).isTrue()
        assertThat(CustomCaTrustStore.isServerCertTrusted(leaf)).isTrue()
    }

    @Test
    fun `leaf signed by a different ca is not trusted`() {
        val (caA, keyA) = newCa("CN=Trusted Root")
        val (caB, keyB) = newCa("CN=Untrusted Root")
        val leaf = newLeaf("CN=server.example", caB, keyB)
        CustomCaTrustStore.initFromDer(listOf(caA.encoded))

        assertThat(CustomCaTrustStore.isServerCertTrusted(leaf)).isFalse()
    }

    @Test
    fun `empty store trusts nothing`() {
        val (caCert, caKey) = newCa("CN=Root")
        val leaf = newLeaf("CN=server.example", caCert, caKey)
        CustomCaTrustStore.initFromDer(emptyList())

        assertThat(CustomCaTrustStore.hasAnchors()).isFalse()
        assertThat(CustomCaTrustStore.isServerCertTrusted(leaf)).isFalse()
    }

    @Test
    fun `validator accepts leaf directly even when anchor lacks full pkix path`() {
        // Self-signed cert presented as the server leaf AND imported as the anchor: a degenerate but
        // common real case (a site serves its self-signed root directly).
        val (selfSigned, _) = newCa("CN=Self Signed Site")
        CustomCaTrustStore.initFromDer(listOf(selfSigned.encoded))

        assertThat(CustomCaTrustStore.isServerCertTrusted(selfSigned)).isTrue()
    }

    // --- helpers (mirror NetworkTrustStorageParseTest) ---

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
