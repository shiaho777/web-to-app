package com.webtoapp.core.apkbuilder

import com.google.common.truth.Truth.assertThat
import java.io.File
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.Date
import javax.security.auth.x500.X500Principal
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Simulates an app restart between import and the next startup load: a fresh
 * [JarSigner] instance re-runs initializeKey -> tryLoadCustomPkcs12 against the
 * persisted keystore + sidecar files (issue #531: "custom signing certificate
 * keeps being removed").
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class JarSignerRestartTest {

    companion object {
        @JvmStatic
        @BeforeClass
        fun forceModernPkcs12Algorithms() {
            System.setProperty("keystore.pkcs12.certProtectionAlgorithm", "PBEWithHmacSHA256AndAES_256")
            System.setProperty("keystore.pkcs12.keyProtectionAlgorithm", "PBEWithHmacSHA256AndAES_256")
            System.setProperty("keystore.pkcs12.macAlgorithm", "HmacPBESHA256")
        }
    }

    private val app get() = RuntimeEnvironment.getApplication()

    @Before
    fun cleanPersistedSigningState() {
        // Each test must start from a pristine filesDir: a leftover custom
        // keystore from a previous test would flip the signer type.
        listOf(
            "custom_keystore.p12",
            "custom_keystore_password.txt",
            "custom_keystore_alias.txt",
            "custom_keystore_keypass.txt",
            "webtoapp_keystore.p12",
            ".ks_credential"
        ).forEach { File(app.filesDir, it).delete() }
    }

    private fun generateKeyPair(): KeyPair =
        KeyPairGenerator.getInstance("RSA").apply { initialize(2048, SecureRandom()) }.generateKeyPair()

    private fun selfSignedCertificate(keyPair: KeyPair): X509Certificate {
        val subject = X500Principal("CN=Restart Test")
        val now = System.currentTimeMillis()
        val holder = JcaX509v3CertificateBuilder(
            subject, BigInteger.valueOf(now), Date(now),
            Date(now + 365L * 24L * 60L * 60L * 1000), subject, keyPair.public
        ).build(JcaContentSignerBuilder("SHA256withRSA").build(keyPair.private))
        return JcaX509CertificateConverter().getCertificate(holder)
    }

    private fun tempKeystore(name: String): File =
        File(app.cacheDir, name).apply { parentFile?.mkdirs() }

    @Test
    fun `imported PBES2 pkcs12 survives a restart`() {
        val keyPair = generateKeyPair()
        val ks = KeyStore.getInstance("PKCS12")
        ks.load(null, null)
        ks.setKeyEntry("release", keyPair.private, "store123".toCharArray(), arrayOf(selfSignedCertificate(keyPair)))
        val file = tempKeystore("restart-modern.p12")
        file.outputStream().use { ks.store(it, "store123".toCharArray()) }

        val importer = JarSigner(app)
        val result = importer.importKeystore(file, "store123", null)
        assertThat(result).isEqualTo(JarSigner.KeystoreImportResult.Success("release", "PKCS12"))

        val restarted = JarSigner(app)
        assertThat(restarted.getSignerType()).isEqualTo(JarSigner.SignerType.PKCS12_CUSTOM)
        assertThat(restarted.getCertificateInfo()).isNotNull()
    }

    @Test
    fun `converted jks survives a restart`() {
        val keyPair = generateKeyPair()
        val ks = KeyStore.getInstance("JKS")
        ks.load(null, null)
        ks.setKeyEntry("suvoutsav", keyPair.private, "keypass".toCharArray(), arrayOf(selfSignedCertificate(keyPair)))
        val file = tempKeystore("restart-separate.jks")
        file.outputStream().use { ks.store(it, "storepass".toCharArray()) }

        val importer = JarSigner(app)
        val result = importer.importKeystore(file, "storepass", "keypass")
        assertThat(result).isEqualTo(JarSigner.KeystoreImportResult.Success("suvoutsav", "JKS"))

        val restarted = JarSigner(app)
        assertThat(restarted.getSignerType()).isEqualTo(JarSigner.SignerType.PKCS12_CUSTOM)
    }

    @Test
    fun `pkcs12 with user-entered separate key password survives a restart`() {
        val keyPair = generateKeyPair()
        val ks = KeyStore.getInstance("PKCS12")
        ks.load(null, null)
        ks.setKeyEntry("release", keyPair.private, "store123".toCharArray(), arrayOf(selfSignedCertificate(keyPair)))
        val file = tempKeystore("restart-keypass.p12")
        file.outputStream().use { ks.store(it, "store123".toCharArray()) }

        // User fills the (meaningless for PKCS12) key password field — the
        // sidecar persists it and the startup load must still recover via the
        // store-password retry.
        val importer = JarSigner(app)
        val result = importer.importKeystore(file, "store123", "notTheStorePass")
        assertThat(result).isEqualTo(JarSigner.KeystoreImportResult.Success("release", "PKCS12"))

        val restarted = JarSigner(app)
        assertThat(restarted.getSignerType()).isEqualTo(JarSigner.SignerType.PKCS12_CUSTOM)
    }
}
