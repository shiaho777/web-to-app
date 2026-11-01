package com.webtoapp.core.crypto

import com.google.common.truth.Truth.assertThat
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.crypto.AEADBadTagException
import javax.crypto.spec.SecretKeySpec
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * The streaming decrypt path exists because the whole-buffer path cannot carry
 * a large asset: decrypting a 60 MB model allocated ~4x its size (asset bytes,
 * header copy, IV-stripped copy, plaintext) and OOM-killed the shell on a
 * 192 MB heap. These tests pin the contract that replaced it.
 */
class AesCryptoEngineStreamingTest {

    private val secretKey = SecretKeySpec(ByteArray(32) { it.toByte() }, "AES")

    private fun encryptWithIv(plain: ByteArray, aad: ByteArray?): Pair<ByteArray, ByteArray> {
        val iv = ByteArray(CryptoConstants.AES_GCM_IV_SIZE) { (it * 7).toByte() }
        val cipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(
            javax.crypto.Cipher.ENCRYPT_MODE,
            secretKey,
            javax.crypto.spec.GCMParameterSpec(CryptoConstants.AES_GCM_TAG_SIZE, iv)
        )
        aad?.let { cipher.updateAAD(it) }
        return iv to cipher.doFinal(plain)
    }

    @Test
    fun `streaming decrypt matches whole buffer decrypt`() {
        val plain = ByteArray(300_000) { (it % 251).toByte() }
        val aad = "html/scene.glb".toByteArray()
        val (iv, cipherText) = encryptWithIv(plain, aad)

        val sink = ByteArrayOutputStream()
        val written = AesCryptoEngine.decryptStreaming(
            encrypted = ByteArrayInputStream(cipherText),
            secretKey = secretKey,
            iv = iv,
            associatedData = aad,
            sink = sink,
        )

        assertThat(written).isEqualTo(plain.size.toLong())
        assertThat(sink.toByteArray()).isEqualTo(plain)
    }

    @Test
    fun `streaming decrypt handles payloads smaller than one buffer`() {
        val plain = "tiny".toByteArray()
        val aad = "html/index.html".toByteArray()
        val (iv, cipherText) = encryptWithIv(plain, aad)

        val sink = ByteArrayOutputStream()
        AesCryptoEngine.decryptStreaming(
            encrypted = ByteArrayInputStream(cipherText),
            secretKey = secretKey,
            iv = iv,
            associatedData = aad,
            sink = sink,
        )

        assertThat(sink.toString(Charsets.UTF_8.name())).isEqualTo("tiny")
    }

    @Test
    fun `streaming decrypt fails when aad does not match`() {
        val plain = ByteArray(4096) { 1 }
        val (iv, cipherText) = encryptWithIv(plain, "html/scene.glb".toByteArray())

        assertThrows(AEADBadTagException::class.java) {
            AesCryptoEngine.decryptStreaming(
                encrypted = ByteArrayInputStream(cipherText),
                secretKey = secretKey,
                iv = iv,
                associatedData = "html/other.glb".toByteArray(),
                sink = ByteArrayOutputStream(),
            )
        }
    }

    @Test
    fun `decrypting stream yields the plaintext through small reads`() {
        val plain = ByteArray(200_000) { (it % 97).toByte() }
        val aad = "html/scene.glb".toByteArray()
        val (iv, cipherText) = encryptWithIv(plain, aad)

        val cipher = AesCryptoEngine.newDecryptCipher(secretKey, iv, aad)
        val stream: InputStream = AesCryptoEngine.GcmDecryptingInputStream(
            ByteArrayInputStream(cipherText),
            cipher,
            bufferSize = 1024,
        )

        val collected = ByteArrayOutputStream()
        val chunk = ByteArray(333)
        stream.use {
            while (true) {
                val read = it.read(chunk)
                if (read < 0) break
                collected.write(chunk, 0, read)
            }
        }

        assertThat(collected.toByteArray()).isEqualTo(plain)
    }

    @Test
    fun `decrypting stream surfaces a tampered body as a bad tag`() {
        val plain = ByteArray(8192) { 3 }
        val aad = "html/scene.glb".toByteArray()
        val (iv, cipherText) = encryptWithIv(plain, aad)
        cipherText[cipherText.size / 2] = (cipherText[cipherText.size / 2] + 1).toByte()

        val cipher = AesCryptoEngine.newDecryptCipher(secretKey, iv, aad)
        val stream = AesCryptoEngine.GcmDecryptingInputStream(
            ByteArrayInputStream(cipherText),
            cipher,
            bufferSize = 1024,
        )

        assertThrows(AEADBadTagException::class.java) {
            stream.use {
                val sink = ByteArrayOutputStream()
                val buffer = ByteArray(1024)
                while (true) {
                    val read = it.read(buffer)
                    if (read < 0) break
                    sink.write(buffer, 0, read)
                }
            }
        }
    }

    @Test
    fun `streaming encrypt produces the same envelope streaming decrypt reads`() {
        val plain = ByteArray(250_000) { (it % 199).toByte() }
        val assetPath = "html/scene.glb"
        val encryptor = AssetEncryptor(secretKey)

        val envelope = ByteArrayOutputStream()
        val written = encryptor.encryptStreaming(
            input = ByteArrayInputStream(plain),
            assetPath = assetPath,
            output = envelope,
        )
        val bytes = envelope.toByteArray()
        assertThat(written).isEqualTo(bytes.size.toLong())

        // Header layout: 4-byte path length, path, 12-byte IV, ciphertext + tag.
        val pathLength = ((bytes[0].toInt() and 0xFF) shl 24) or
            ((bytes[1].toInt() and 0xFF) shl 16) or
            ((bytes[2].toInt() and 0xFF) shl 8) or
            (bytes[3].toInt() and 0xFF)
        val embeddedPath = String(bytes, 4, pathLength, Charsets.UTF_8)
        assertThat(embeddedPath).isEqualTo(assetPath)

        val ivOffset = 4 + pathLength
        val iv = bytes.copyOfRange(ivOffset, ivOffset + CryptoConstants.AES_GCM_IV_SIZE)
        val cipherText = bytes.copyOfRange(ivOffset + CryptoConstants.AES_GCM_IV_SIZE, bytes.size)

        val sink = ByteArrayOutputStream()
        AesCryptoEngine.decryptStreaming(
            encrypted = ByteArrayInputStream(cipherText),
            secretKey = secretKey,
            iv = iv,
            associatedData = embeddedPath.toByteArray(Charsets.UTF_8),
            sink = sink,
        )

        assertThat(sink.toByteArray()).isEqualTo(plain)
    }

    @Test
    fun `streaming encrypt into a zip entry survives a round trip through the zip`() {
        val plain = ByteArray(180_000) { (it % 241).toByte() }
        val assetPath = "html/scene.glb"
        val encryptor = AssetEncryptor(secretKey)

        val archive = ByteArrayOutputStream()
        java.util.zip.ZipOutputStream(archive).use { zipOut ->
            val entry = java.util.zip.ZipEntry("assets/$assetPath.enc")
            entry.method = java.util.zip.ZipEntry.DEFLATED
            zipOut.putNextEntry(entry)
            val written = encryptor.encryptStreaming(
                input = ByteArrayInputStream(plain),
                assetPath = assetPath,
                output = zipOut,
            )
            assertThat(written).isGreaterThan(plain.size.toLong())
            zipOut.closeEntry()
        }

        val zipIn = java.util.zip.ZipInputStream(ByteArrayInputStream(archive.toByteArray()))
        val entry = zipIn.nextEntry
        assertThat(entry.name).isEqualTo("assets/$assetPath.enc")

        val envelope = zipIn.readBytes()
        val pathLength = ((envelope[0].toInt() and 0xFF) shl 24) or
            ((envelope[1].toInt() and 0xFF) shl 16) or
            ((envelope[2].toInt() and 0xFF) shl 8) or
            (envelope[3].toInt() and 0xFF)
        val embeddedPath = String(envelope, 4, pathLength, Charsets.UTF_8)
        val ivOffset = 4 + pathLength
        val iv = envelope.copyOfRange(ivOffset, ivOffset + CryptoConstants.AES_GCM_IV_SIZE)
        val cipherText = envelope.copyOfRange(ivOffset + CryptoConstants.AES_GCM_IV_SIZE, envelope.size)

        val sink = ByteArrayOutputStream()
        AesCryptoEngine.decryptStreaming(
            encrypted = ByteArrayInputStream(cipherText),
            secretKey = secretKey,
            iv = iv,
            associatedData = embeddedPath.toByteArray(Charsets.UTF_8),
            sink = sink,
        )

        assertThat(embeddedPath).isEqualTo(assetPath)
        assertThat(sink.toByteArray()).isEqualTo(plain)
    }
}
