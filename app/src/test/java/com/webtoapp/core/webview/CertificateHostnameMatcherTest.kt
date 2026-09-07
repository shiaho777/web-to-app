package com.webtoapp.core.webview

import com.google.common.truth.Truth.assertThat
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.GeneralName
import org.bouncycastle.asn1.x509.GeneralNames
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.junit.Test
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.cert.X509Certificate
import java.util.Date

class CertificateHostnameMatcherTest {

    @Test
    fun `exact dns san matches`() {
        val cert = newCert(sans = listOf("api.example.com"))
        assertThat(CertificateHostnameMatcher.matches("api.example.com", cert)).isTrue()
        assertThat(CertificateHostnameMatcher.matches("other.example.com", cert)).isFalse()
    }

    @Test
    fun `wildcard san matches exactly one label`() {
        val cert = newCert(sans = listOf("*.example.com"))
        assertThat(CertificateHostnameMatcher.matches("a.example.com", cert)).isTrue()
        assertThat(CertificateHostnameMatcher.matches("example.com", cert)).isFalse()
        assertThat(CertificateHostnameMatcher.matches("a.b.example.com", cert)).isFalse()
    }

    @Test
    fun `dns san suppresses cn fallback`() {
        val cert = newCert(cn = "evil.example", sans = listOf("good.example"))
        assertThat(CertificateHostnameMatcher.matches("good.example", cert)).isTrue()
        assertThat(CertificateHostnameMatcher.matches("evil.example", cert)).isFalse()
    }

    @Test
    fun `cn fallback only when no dns san present`() {
        val cert = newCert(cn = "legacy.example")
        assertThat(CertificateHostnameMatcher.matches("legacy.example", cert)).isTrue()
        assertThat(CertificateHostnameMatcher.matches("other.example", cert)).isFalse()
    }

    @Test
    fun `ipv4 address matches only via ip san`() {
        val cert = newCert(ipSans = listOf("203.0.113.7"))
        assertThat(CertificateHostnameMatcher.matches("203.0.113.7", cert)).isTrue()
        assertThat(CertificateHostnameMatcher.matches("203.0.113.8", cert)).isFalse()
    }

    @Test
    fun `case and trailing dot are normalized`() {
        val cert = newCert(sans = listOf("API.Example.COM"))
        assertThat(CertificateHostnameMatcher.matches("api.example.com.", cert)).isTrue()
    }

    private fun newCert(cn: String = "localhost", sans: List<String> = emptyList(), ipSans: List<String> = emptyList()): X509Certificate {
        val keyPair: KeyPair = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()
        val issuer = X500Name("CN=$cn")
        val builder = JcaX509v3CertificateBuilder(
            issuer,
            BigInteger.valueOf(System.currentTimeMillis()),
            Date(System.currentTimeMillis() - 60_000L),
            Date(System.currentTimeMillis() + 3_600_000L),
            issuer,
            keyPair.public
        )
        val generalNames = mutableListOf<GeneralName>()
        sans.forEach { generalNames += GeneralName(GeneralName.dNSName, it) }
        ipSans.forEach {
            generalNames += GeneralName(
                GeneralName.iPAddress,
                org.bouncycastle.asn1.DEROctetString(java.net.InetAddress.getByName(it).address)
            )
        }
        if (generalNames.isNotEmpty()) {
            builder.addExtension(
                Extension.subjectAlternativeName,
                false,
                GeneralNames(generalNames.toTypedArray())
            )
        }
        return JcaX509CertificateConverter().getCertificate(
            builder.build(JcaContentSignerBuilder("SHA256withRSA").build(keyPair.private))
        )
    }
}
