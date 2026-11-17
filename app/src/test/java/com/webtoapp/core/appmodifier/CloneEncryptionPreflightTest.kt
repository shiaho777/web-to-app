package com.webtoapp.core.appmodifier

import com.google.common.truth.Truth.assertThat
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class CloneEncryptionPreflightTest {

    @get:Rule
    val temp = TemporaryFolder()

    private fun apkWithMeta(metaJson: String?): File {
        val apk = temp.newFile("app-${System.nanoTime()}.apk")
        ZipOutputStream(apk.outputStream()).use { zip ->
            zip.putNextEntry(ZipEntry("AndroidManifest.xml"))
            zip.write("manifest".toByteArray())
            zip.closeEntry()
            if (metaJson != null) {
                zip.putNextEntry(ZipEntry("assets/encryption_meta.json"))
                zip.write(metaJson.toByteArray())
                zip.closeEntry()
            }
        }
        return apk
    }

    @Test
    fun `signature-mode encryption is detected`() {
        val apk = apkWithMeta("""{"version":1,"packageName":"com.example","keyMode":"SIGNATURE"}""")
        assertThat(apkUsesSignatureBoundEncryption(apk)).isTrue()
    }

    @Test
    fun `legacy metadata without keyMode is treated as signature-bound`() {
        val apk = apkWithMeta("""{"version":1,"packageName":"com.example"}""")
        assertThat(apkUsesSignatureBoundEncryption(apk)).isTrue()
    }

    @Test
    fun `embedded-key encryption survives cloning`() {
        val apk = apkWithMeta("""{"version":1,"keyMode":"EMBEDDED","embeddedKey":"abc"}""")
        assertThat(apkUsesSignatureBoundEncryption(apk)).isFalse()
    }

    @Test
    fun `unencrypted apk passes`() {
        val apk = apkWithMeta(null)
        assertThat(apkUsesSignatureBoundEncryption(apk)).isFalse()
    }

    @Test
    fun `corrupt metadata fails open without crashing`() {
        val apk = apkWithMeta("not-json{{{")
        assertThat(apkUsesSignatureBoundEncryption(apk)).isFalse()
    }
}
