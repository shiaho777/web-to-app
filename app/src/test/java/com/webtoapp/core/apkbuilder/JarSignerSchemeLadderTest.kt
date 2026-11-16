package com.webtoapp.core.apkbuilder

import androidx.test.core.app.ApplicationProvider
import com.android.apksig.ApkVerifier
import com.google.common.truth.Truth.assertThat
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.playstore.aab.axml.AxmlBuilder
import com.webtoapp.core.playstore.aab.axml.AxmlConstants
import java.io.File
import java.security.InvalidKeyException
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.UnrecoverableKeyException
import java.security.cert.CertificateException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class JarSignerSchemeLadderTest {

    private val context get() = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Before
    fun cleanPersistedSigningState() {
        listOf(
            "custom_keystore.p12",
            "custom_keystore_password.txt",
            "custom_keystore_alias.txt",
            "custom_keystore_keypass.txt",
            "webtoapp_keystore.p12",
            ".ks_credential"
        ).forEach { File(context.filesDir, it).delete() }
    }

    @Test
    fun `required schemes force v1 below api 24 and v2 at target 30 plus`() {
        assertThat(JarSigner.requiredSigningSchemes(23, 35)).containsExactly(1, 2)
        assertThat(JarSigner.requiredSigningSchemes(23, 28)).containsExactly(1)
        assertThat(JarSigner.requiredSigningSchemes(24, 35)).containsExactly(2)
        assertThat(JarSigner.requiredSigningSchemes(24, 28)).isEmpty()
    }

    @Test
    fun `fallback ladder never drops forced schemes`() {
        val ladder = JarSigner.schemeFallbackLadder(listOf(1, 2, 3), setOf(1, 2), autoFallback = true)

        assertThat(ladder).isEqualTo(listOf(listOf(1, 2, 3), listOf(1, 2)))
        ladder.forEach { config ->
            assertThat(config).containsAtLeast(1, 2)
        }
    }

    @Test
    fun `fallback ladder keeps forced v2 when v1 is optional`() {
        val ladder = JarSigner.schemeFallbackLadder(listOf(1, 2, 3), setOf(2), autoFallback = true)

        assertThat(ladder).isEqualTo(listOf(listOf(1, 2, 3), listOf(1, 2), listOf(2)))
        ladder.forEach { config -> assertThat(config).contains(2) }
    }

    @Test
    fun `fallback ladder without forced schemes still reaches single scheme`() {
        val ladder = JarSigner.schemeFallbackLadder(listOf(1, 2, 3), emptySet(), autoFallback = true)

        assertThat(ladder).isEqualTo(
            listOf(listOf(1, 2, 3), listOf(1, 2), listOf(1))
        )
    }

    @Test
    fun `fallback disabled yields exactly the selected config`() {
        val ladder = JarSigner.schemeFallbackLadder(listOf(1, 2, 3), setOf(2), autoFallback = false)

        assertThat(ladder).isEqualTo(listOf(listOf(1, 2, 3)))
    }

    @Test
    fun `keystore unusable classifier accepts real key material failures`() {
        assertThat(JarSigner.isKeystoreUnusableFailure(KeyStoreException("corrupt store"))).isTrue()
        assertThat(JarSigner.isKeystoreUnusableFailure(UnrecoverableKeyException("bad password"))).isTrue()
        assertThat(JarSigner.isKeystoreUnusableFailure(CertificateException("bad cert"))).isTrue()
        assertThat(JarSigner.isKeystoreUnusableFailure(NoSuchAlgorithmException("no algo"))).isTrue()
        assertThat(JarSigner.isKeystoreUnusableFailure(InvalidKeyException("unsupported key"))).isTrue()
        assertThat(
            JarSigner.isKeystoreUnusableFailure(
                RuntimeException("signing failed", java.io.IOException("wrapper", KeyStoreException("root")))
            )
        ).isTrue()
    }

    @Test
    fun `keystore unusable classifier rejects signing failures that merely mention keys`() {
        assertThat(
            JarSigner.isKeystoreUnusableFailure(RuntimeException("Failed to sign APK: certificate problem"))
        ).isFalse()
        assertThat(
            JarSigner.isKeystoreUnusableFailure(java.io.IOException("Failed to read key entry from APK"))
        ).isFalse()
        assertThat(
            JarSigner.isKeystoreUnusableFailure(IllegalStateException("signature verification failed"))
        ).isFalse()
    }

    private fun minimalApk(minSdk: Int, targetSdk: Int): File {
        val strings = listOf(
            "android",
            "http://schemas.android.com/apk/res/android",
            "manifest",
            "package",
            "com.test.signer",
            "versionCode",
            "1",
            "minSdkVersion",
            minSdk.toString(),
            "targetSdkVersion",
            targetSdk.toString(),
            "uses-sdk",
            "application"
        )
        val axml = AxmlBuilder()
            .stringPool(strings)
            .resourceMap(
                intArrayOf(
                    0, 0, 0, 0, 0, 0x0101021b, 0,
                    0x0101020c, 0, 0x01010270, 0, 0, 0
                )
            )
            .startNamespace(prefixIdx = 0, uriIdx = 1)
            .startElement(
                namespaceIdx = -1,
                nameIdx = 2,
                attributes = listOf(
                    AxmlBuilder.Attr(-1, 3, 4, AxmlConstants.TYPE_STRING, 4),
                    AxmlBuilder.Attr(1, 5, 6, AxmlConstants.TYPE_INT_DEC, 1)
                )
            )
            .startElement(
                namespaceIdx = -1,
                nameIdx = 11,
                attributes = listOf(
                    AxmlBuilder.Attr(1, 7, 8, AxmlConstants.TYPE_INT_DEC, minSdk),
                    AxmlBuilder.Attr(1, 9, 10, AxmlConstants.TYPE_INT_DEC, targetSdk)
                )
            )
            .endElement(namespaceIdx = -1, nameIdx = 11)
            .startElement(namespaceIdx = -1, nameIdx = 12, attributes = emptyList())
            .endElement(namespaceIdx = -1, nameIdx = 12)
            .endElement(namespaceIdx = -1, nameIdx = 2)
            .endNamespace(prefixIdx = 0, uriIdx = 1)
            .build()

        val apk = File(context.cacheDir, "ladder-input-$minSdk-$targetSdk.apk")
        ZipOutputStream(apk.outputStream()).use { zip ->
            zip.putNextEntry(ZipEntry("AndroidManifest.xml"))
            zip.write(axml)
            zip.closeEntry()
            zip.putNextEntry(ZipEntry("assets/dummy.txt"))
            zip.write("payload".toByteArray())
            zip.closeEntry()
        }
        return apk
    }

    private fun verifyDetailed(signer: JarSigner, apk: File, minSdk: Int, targetSdk: Int): Boolean {
        val method = JarSigner::class.java.getDeclaredMethod(
            "verifyApkDetailed",
            File::class.java, String::class.java, Int::class.javaPrimitiveType, Int::class.javaPrimitiveType
        )
        method.isAccessible = true
        return method.invoke(signer, apk, "test", minSdk, targetSdk) as Boolean
    }

    @Test
    fun `v1-only output is rejected for the android 11 plus platform range`() {
        val signer = JarSigner(context)
        signer.setSigningSchemeOptions(
            JarSigner.SigningSchemeOptions(v1Enabled = true, v2Enabled = false, v3Enabled = false, autoFallback = false)
        )
        val input = minimalApk(minSdk = 24, targetSdk = 28)
        val output = File(context.cacheDir, "ladder-v1-only.apk")

        assertThat(signer.sign(input, output, targetSdk = 28, minSdk = 24)).isTrue()

        assertThat(verifyDetailed(signer, output, minSdk = 24, targetSdk = 28)).isTrue()
        assertThat(verifyDetailed(signer, output, minSdk = 23, targetSdk = 35)).isFalse()
    }

    @Test
    fun `forced v2 survives when the user disables it for a target 35 app`() {
        val signer = JarSigner(context)
        signer.setSigningSchemeOptions(
            JarSigner.SigningSchemeOptions(v1Enabled = true, v2Enabled = false, v3Enabled = false, autoFallback = true)
        )
        val input = minimalApk(minSdk = 23, targetSdk = 35)
        val output = File(context.cacheDir, "ladder-forced-v2.apk")

        assertThat(signer.sign(input, output, targetSdk = 35, minSdk = 23)).isTrue()

        val result = ApkVerifier.Builder(output)
            .setMinCheckedPlatformVersion(23)
            .setMaxCheckedPlatformVersion(35)
            .build()
            .verify()
        assertThat(result.isVerified).isTrue()
        assertThat(result.isVerifiedUsingV1Scheme).isTrue()
        assertThat(result.isVerifiedUsingV2Scheme).isTrue()
    }

    @Test
    fun `unusable key regenerates the auto keystore and aborts the build`() {
        val signer = JarSigner(context)
        val originalHash = signer.getCertificateSignatureHash()
        val keystoreFile = File(context.filesDir, "webtoapp_keystore.p12")
        assertThat(keystoreFile.exists()).isTrue()

        val brokenKey = object : PrivateKey {
            override fun getAlgorithm(): String = "WTA_UNSUPPORTED"
            override fun getFormat(): String = "RAW"
            override fun getEncoded(): ByteArray = ByteArray(8)
        }
        val keyField = JarSigner::class.java.getDeclaredField("privateKey")
        keyField.isAccessible = true
        keyField.set(signer, brokenKey)

        val input = minimalApk(minSdk = 23, targetSdk = 35)
        val output = File(context.cacheDir, "ladder-broken-key.apk")

        var thrown: Throwable? = null
        try {
            signer.sign(input, output, targetSdk = 35, minSdk = 23)
        } catch (e: Throwable) {
            thrown = e
        }

        assertThat(thrown).isInstanceOf(IllegalStateException::class.java)
        assertThat(thrown!!.message).isEqualTo(Strings.signKeyRegeneratedRetry)
        assertThat(thrown.cause).isInstanceOf(InvalidKeyException::class.java)
        assertThat(output.exists()).isFalse()
        assertThat(keystoreFile.exists()).isTrue()
        assertThat(signer.getCertificateSignatureHash()).isNotEqualTo(originalHash)
    }

    @Test
    fun `non-keystore signing failure never touches the auto keystore`() {
        val signer = JarSigner(context)
        val keystoreFile = File(context.filesDir, "webtoapp_keystore.p12")
        val originalBytes = keystoreFile.readBytes()
        val originalHash = signer.getCertificateSignatureHash()

        val corruptInput = File(context.cacheDir, "ladder-corrupt-input.apk")
        ZipOutputStream(corruptInput.outputStream()).use { zip ->
            zip.putNextEntry(ZipEntry("AndroidManifest.xml"))
            zip.write(byteArrayOf(0x01, 0x02, 0x03, 0x04))
            zip.closeEntry()
        }
        val output = File(context.cacheDir, "ladder-corrupt-output.apk")

        var thrown: Throwable? = null
        try {
            signer.sign(corruptInput, output, targetSdk = 35, minSdk = 23)
        } catch (e: Throwable) {
            thrown = e
        }

        assertThat(thrown).isInstanceOf(RuntimeException::class.java)
        assertThat(thrown!!.message).isNotEqualTo(Strings.signKeyRegeneratedRetry)
        assertThat(keystoreFile.readBytes()).isEqualTo(originalBytes)
        assertThat(signer.getCertificateSignatureHash()).isEqualTo(originalHash)
    }
}
