package com.webtoapp.core.webview

import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class TlsMitmCaManagerTest {

    private lateinit var caDir: File

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        caDir = File(context.filesDir, "test_mitm_ca")
        caDir.deleteRecursively()
        caDir.mkdirs()
        TlsMitmCaManager.clearCache()
    }

    @Test
    fun `init generates CA and certificate`() {
        TlsMitmCaManager.init(caDir)

        assertThat(TlsMitmCaManager.isCaInitialized()).isTrue()
        val caCert = TlsMitmCaManager.getCaCertificate()
        assertThat(caCert).isNotNull()
        assertThat(caCert!!.issuerX500Principal.name).contains("WebToApp")
    }

    @Test
    fun `init loads existing CA on second call`() {
        TlsMitmCaManager.init(caDir)
        val firstCert = TlsMitmCaManager.getCaCertificate()
        assertThat(firstCert).isNotNull()

        TlsMitmCaManager.clearCache()

        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val newCaDir = File(context.filesDir, "test_mitm_ca_reload")
        newCaDir.deleteRecursively()
        newCaDir.mkdirs()

        TlsMitmCaManager.init(newCaDir)
        val secondCert = TlsMitmCaManager.getCaCertificate()
        assertThat(secondCert).isNotNull()
        assertThat(secondCert!!.encoded).isEqualTo(firstCert!!.encoded)

        newCaDir.deleteRecursively()
    }

    @Test
    fun `getOrCreateLeafEntry returns valid entry for hostname`() {
        TlsMitmCaManager.init(caDir)

        val entry = TlsMitmCaManager.getOrCreateLeafEntry("example.com")
        assertThat(entry).isNotNull()
        assertThat(entry!!.certificateChain).isNotEmpty()
        assertThat(entry.certificateChain[0]).isInstanceOf(java.security.cert.X509Certificate::class.java)

        val leafCert = entry.certificateChain[0] as java.security.cert.X509Certificate
        assertThat(leafCert.subjectX500Principal.name).contains("example.com")
    }

    @Test
    fun `leaf certificate is signed by local CA`() {
        TlsMitmCaManager.init(caDir)

        val entry = TlsMitmCaManager.getOrCreateLeafEntry("test.example.org")
        assertThat(entry).isNotNull()

        val leafCert = entry!!.certificateChain[0] as java.security.cert.X509Certificate
        val caCert = TlsMitmCaManager.getCaCertificate()!!

        assertThat(leafCert.issuerX500Principal).isEqualTo(caCert.subjectX500Principal)
        assertThat(TlsMitmCaManager.isSignedByLocalCa(leafCert)).isTrue()
    }

    @Test
    fun `isSignedByLocalCa returns false for untrusted cert`() {
        TlsMitmCaManager.init(caDir)

        val keyPairGenerator = java.security.KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        val pair = keyPairGenerator.generateKeyPair()

        val issuer = javax.security.auth.x500.X500Principal("CN=Other CA, O=Other")
        val name = org.bouncycastle.asn1.x500.X500Name(issuer.name)
        val serial = java.math.BigInteger.valueOf(System.currentTimeMillis())
        val notBefore = java.util.Date()
        val notAfter = java.util.Date(notBefore.time + 365L * 24 * 60 * 60 * 1000)

        val certBuilder = org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder(
            name, serial, notBefore, notAfter, name, pair.public
        )
        val holder = certBuilder.build(
            org.bouncycastle.operator.jcajce.JcaContentSignerBuilder("SHA256withRSA").build(pair.private)
        )
        val untrustedCert = org.bouncycastle.cert.jcajce.JcaX509CertificateConverter().getCertificate(holder)

        assertThat(TlsMitmCaManager.isSignedByLocalCa(untrustedCert)).isFalse()
    }

    @Test
    fun `isSignedByLocalCa returns false for null cert`() {
        TlsMitmCaManager.init(caDir)
        assertThat(TlsMitmCaManager.isSignedByLocalCa(null)).isFalse()
    }

    @Test
    fun `leaf cache returns same entry for same host`() {
        TlsMitmCaManager.init(caDir)

        val entry1 = TlsMitmCaManager.getOrCreateLeafEntry("cached.example.com")
        val entry2 = TlsMitmCaManager.getOrCreateLeafEntry("cached.example.com")

        assertThat(entry1).isNotNull()
        assertThat(entry2).isNotNull()
        assertThat(entry2!!.certificateChain[0]).isEqualTo(entry1!!.certificateChain[0])
    }

    @Test
    fun `createKeyManagerFactory returns valid factory`() {
        TlsMitmCaManager.init(caDir)

        val kmf = TlsMitmCaManager.createKeyManagerFactory("keymgr.example.com")
        assertThat(kmf).isNotNull()
        assertThat(kmf!!.keyManagers).isNotEmpty()
    }

    @Test
    fun `host name is case insensitive for leaf cache`() {
        TlsMitmCaManager.init(caDir)

        val entry1 = TlsMitmCaManager.getOrCreateLeafEntry("CaseTest.Example.COM")
        val entry2 = TlsMitmCaManager.getOrCreateLeafEntry("casetest.example.com")

        assertThat(entry1).isNotNull()
        assertThat(entry2).isNotNull()
        assertThat(entry2!!.certificateChain[0]).isEqualTo(entry1!!.certificateChain[0])
    }
}
