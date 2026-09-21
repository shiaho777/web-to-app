package com.webtoapp.core.apkbuilder

import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Per-app signing identity (`ApkExportConfig.perAppSigningEnabled`): one RSA-3072 identity
 * per package name, reused across rebuilds, isolated across packages.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class PerAppSigningIdentityTest {

    private val context get() = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Test
    fun `same package reuses the same identity`() {
        val pkg = "com.test.reuse.${System.nanoTime().toString(16)}"
        val first = PerAppSigningIdentity.identityFor(context, pkg)
        val second = PerAppSigningIdentity.identityFor(context, pkg)

        assertThat(first.createdNow).isTrue()
        assertThat(second.createdNow).isFalse()
        assertThat(second.certSha256Hex()).isEqualTo(first.certSha256Hex())
        assertThat(second.privateKey.encoded).isEqualTo(first.privateKey.encoded)
    }

    @Test
    fun `different packages get different identities`() {
        val suffix = System.nanoTime().toString(16)
        val a = PerAppSigningIdentity.identityFor(context, "com.test.a.$suffix")
        val b = PerAppSigningIdentity.identityFor(context, "com.test.b.$suffix")

        assertThat(a.certSha256Hex()).isNotEqualTo(b.certSha256Hex())
        assertThat(a.storeFile.name).isNotEqualTo(b.storeFile.name)
    }

    @Test
    fun `generated identity is rsa 3072 self signed`() {
        val id = PerAppSigningIdentity.identityFor(context, "com.test.algo.${System.nanoTime().toString(16)}")

        assertThat(id.privateKey.algorithm).isEqualTo("RSA")
        val modulusBits = (id.privateKey as java.security.interfaces.RSAPrivateKey).modulus.bitLength()
        assertThat(modulusBits).isEqualTo(3072)
        assertThat(id.certificate.issuerX500Principal).isEqualTo(id.certificate.subjectX500Principal)
        assertThat(id.certificate.subjectX500Principal.name).contains("CN=com.test.algo.")
    }

    @Test
    fun `corrupt store fails loudly instead of rotating identity`() {
        val pkg = "com.test.corrupt.${System.nanoTime().toString(16)}"
        val id = PerAppSigningIdentity.identityFor(context, pkg)
        // Scribble over the store — the next lookup must fail, not mint a new key.
        id.storeFile.writeBytes(byteArrayOf(0x0B, 0xAD.toByte(), 0xF0.toByte(), 0x0D))

        var threw = false
        try {
            PerAppSigningIdentity.identityFor(context, pkg)
        } catch (e: IllegalStateException) {
            threw = true
        }
        assertThat(threw).isTrue()
    }

    @Test
    fun `existingFingerprint never creates a store`() {
        val pkg = "com.test.fresh.${System.nanoTime().toString(16)}"
        assertThat(PerAppSigningIdentity.existingFingerprint(context, pkg)).isNull()

        PerAppSigningIdentity.identityFor(context, pkg)
        assertThat(PerAppSigningIdentity.existingFingerprint(context, pkg)).isNotNull()
    }
}
