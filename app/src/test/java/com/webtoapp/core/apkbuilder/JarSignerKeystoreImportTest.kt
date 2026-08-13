package com.webtoapp.core.apkbuilder

import com.google.common.truth.Truth.assertThat
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.Date
import javax.security.auth.x500.X500Principal

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class JarSignerKeystoreImportTest {

    companion object {
        @JvmStatic
        @BeforeClass
        fun forceModernPkcs12Algorithms() {
            // Write PKCS12 exactly like modern keytool / Android Studio does (PBES2 + AES-256).
            // The stripped platform BouncyCastle on many Android versions cannot decrypt these,
            // which is the root cause of issue #523: a `.jks`-named file that is actually
            // PKCS12 fails to import with a generic "wrong password" error.
            System.setProperty("keystore.pkcs12.certProtectionAlgorithm", "PBEWithHmacSHA256AndAES_256")
            System.setProperty("keystore.pkcs12.keyProtectionAlgorithm", "PBEWithHmacSHA256AndAES_256")
            System.setProperty("keystore.pkcs12.macAlgorithm", "HmacPBESHA256")
        }
    }

    private val app get() = RuntimeEnvironment.getApplication()

    private fun generateKeyPair(): KeyPair =
        KeyPairGenerator.getInstance("RSA").apply {
            initialize(2048, SecureRandom())
        }.generateKeyPair()

    private fun selfSignedCertificate(keyPair: KeyPair): X509Certificate {
        val subject = X500Principal("CN=WebToApp Keystore Import Test")
        val now = System.currentTimeMillis()
        val holder = JcaX509v3CertificateBuilder(
            subject,
            BigInteger.valueOf(now),
            Date(now),
            Date(now + 365L * 24L * 60L * 60L * 1000L),
            subject,
            keyPair.public
        ).build(JcaContentSignerBuilder("SHA256withRSA").build(keyPair.private))
        return JcaX509CertificateConverter().getCertificate(holder)
    }

    private fun tempKeystore(name: String): File =
        File(app.cacheDir, name).apply { parentFile?.mkdirs() }

    @Test
    fun `modern PBES2 pkcs12 keystore imports successfully`() {
        val keyPair = generateKeyPair()
        val ks = KeyStore.getInstance("PKCS12")
        ks.load(null, null)
        ks.setKeyEntry("release", keyPair.private, "store123".toCharArray(), arrayOf(selfSignedCertificate(keyPair)))
        val file = tempKeystore("modern.p12")
        file.outputStream().use { ks.store(it, "store123".toCharArray()) }

        val signer = JarSigner(app)
        val result = signer.importKeystore(file, "store123", null)

        assertThat(result).isEqualTo(JarSigner.KeystoreImportResult.Success("release", "PKCS12"))
        assertThat(signer.getSignerType()).isEqualTo(JarSigner.SignerType.PKCS12_CUSTOM)
        assertThat(signer.getCertificateInfo()).isNotNull()
    }

    @Test
    fun `jceks keystore imports via full bouncy castle provider`() {
        val keyPair = generateKeyPair()
        val ks = KeyStore.getInstance("JCEKS")
        ks.load(null, null)
        ks.setKeyEntry("release", keyPair.private, "store123".toCharArray(), arrayOf(selfSignedCertificate(keyPair)))
        val file = tempKeystore("legacy.jceks")
        file.outputStream().use { ks.store(it, "store123".toCharArray()) }

        val signer = JarSigner(app)
        val result = signer.importKeystore(file, "store123", null)

        assertThat(result).isEqualTo(JarSigner.KeystoreImportResult.Success("release", "JCEKS"))
    }

    @Test
    fun `jks with separate key password imports when key password provided`() {
        val keyPair = generateKeyPair()
        val ks = KeyStore.getInstance("JKS")
        ks.load(null, null)
        ks.setKeyEntry(
            "suvoutsav",
            keyPair.private,
            "keypass".toCharArray(),
            arrayOf(selfSignedCertificate(keyPair))
        )
        val file = tempKeystore("separate.jks")
        file.outputStream().use { ks.store(it, "storepass".toCharArray()) }

        val signer = JarSigner(app)
        val result = signer.importKeystore(file, "storepass", "keypass")

        assertThat(result).isEqualTo(JarSigner.KeystoreImportResult.Success("suvoutsav", "JKS"))
    }

    @Test
    fun `jks separate key password reports key password rejected when missing or wrong`() {
        val keyPair = generateKeyPair()
        val ks = KeyStore.getInstance("JKS")
        ks.load(null, null)
        ks.setKeyEntry(
            "suvoutsav",
            keyPair.private,
            "keypass".toCharArray(),
            arrayOf(selfSignedCertificate(keyPair))
        )
        val file = tempKeystore("separate2.jks")
        file.outputStream().use { ks.store(it, "storepass".toCharArray()) }

        val signer = JarSigner(app)

        val missing = signer.importKeystore(file, "storepass", null)
        assertThat(missing)
            .isEqualTo(JarSigner.KeystoreImportResult.KeyPasswordRejected(keyPasswordFieldUsed = false))

        val wrong = signer.importKeystore(file, "storepass", "wrongkey")
        assertThat(wrong)
            .isEqualTo(JarSigner.KeystoreImportResult.KeyPasswordRejected(keyPasswordFieldUsed = true))
    }

    @Test
    fun `wrong store password reports store password rejected`() {
        val keyPair = generateKeyPair()
        val ks = KeyStore.getInstance("JKS")
        ks.load(null, null)
        ks.setKeyEntry("k", keyPair.private, "storepass".toCharArray(), arrayOf(selfSignedCertificate(keyPair)))
        val file = tempKeystore("storepass.jks")
        file.outputStream().use { ks.store(it, "storepass".toCharArray()) }

        val signer = JarSigner(app)
        val result = signer.importKeystore(file, "typo-password", null)

        assertThat(result).isEqualTo(JarSigner.KeystoreImportResult.StorePasswordRejected)
    }

    @Test
    fun `certificate-only keystore reports no key entry`() {
        val keyPair = generateKeyPair()
        val ks = KeyStore.getInstance("JKS")
        ks.load(null, null)
        ks.setCertificateEntry("cert0", selfSignedCertificate(keyPair))
        val file = tempKeystore("certonly.jks")
        file.outputStream().use { ks.store(it, "storepass".toCharArray()) }

        val signer = JarSigner(app)
        val result = signer.importKeystore(file, "storepass", null)

        assertThat(result).isEqualTo(JarSigner.KeystoreImportResult.NoKeyEntry)
    }

    @Test
    fun `garbage file reports unsupported format`() {
        val file = tempKeystore("garbage.bin")
        file.writeBytes(byteArrayOf(0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07))

        val signer = JarSigner(app)
        val result = signer.importKeystore(file, "whatever", null)

        assertThat(result).isEqualTo(JarSigner.KeystoreImportResult.UnsupportedFormat)
    }
}
