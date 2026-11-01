package com.webtoapp.core.crypto

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest
import javax.crypto.spec.SecretKeySpec
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * On-device proof for the fix that replaced whole-buffer decryption.
 *
 * The original crash was an OutOfMemoryError on a 62 MB asset: the old path held
 * the asset bytes, a header copy, an IV-stripped copy and the plaintext at once
 * (~4x the file size) on a 192 MB heap. These tests run the real streaming path
 * with a payload of exactly that size, so a regression to whole-buffer handling
 * fails here instead of on a user's phone.
 */
@RunWith(AndroidJUnit4::class)
class LargeAssetStreamingInstrumentedTest {

    private val secretKey = SecretKeySpec(ByteArray(32) { it.toByte() }, "AES")
    private val assetName = "html/scene.glb"
    private val payloadBytes = 62L * 1024 * 1024
    private val tagBytes = (CryptoConstants.AES_GCM_TAG_SIZE / 8).toLong()

    @Test
    fun sixtyTwoMegabyteAssetStreamsWithinTheHeapLimit() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val encryptedFile = File(context.cacheDir, "large_asset_stream_test.enc")
        val pathBytes = assetName.toByteArray(Charsets.UTF_8)
        val iv = AesCryptoEngine.newRandomIv()

        try {
            val cipher = AesCryptoEngine.newEncryptCipher(secretKey, iv, pathBytes)
            encryptedFile.outputStream().buffered().use { out ->
                out.write(pathLengthHeader(pathBytes.size))
                out.write(pathBytes)
                out.write(iv)
                val written = AesCryptoEngine.encryptStreaming(
                    plain = DeterministicStream(payloadBytes),
                    cipher = cipher,
                    sink = out,
                )
                // GCM appends the 16-byte tag, so the sink receives plaintext + tag.
                assertEquals(payloadBytes + tagBytes, written)
            }

            assertEquals(
                4L + pathBytes.size + iv.size + payloadBytes + tagBytes,
                encryptedFile.length(),
            )

            val input = encryptedFile.inputStream()
            val header = ByteArray(4)
            readFully(input, header)
            val embeddedPathLength = ((header[0].toInt() and 0xFF) shl 24) or
                ((header[1].toInt() and 0xFF) shl 16) or
                ((header[2].toInt() and 0xFF) shl 8) or
                (header[3].toInt() and 0xFF)
            assertEquals(pathBytes.size, embeddedPathLength)

            val associatedData = ByteArray(embeddedPathLength)
            readFully(input, associatedData)
            assertEquals(assetName, String(associatedData, Charsets.UTF_8))

            val readIv = ByteArray(CryptoConstants.AES_GCM_IV_SIZE)
            readFully(input, readIv)

            // This is the assumption openEncryptedAsset() relies on for
            // Content-Length: available() is the remaining *decoded* byte count.
            val plainLength = input.available().toLong() - tagBytes
            assertEquals(payloadBytes, plainLength)

            val digest = MessageDigest.getInstance("SHA-256")
            val countingSink = object : OutputStream() {
                var total = 0L
                override fun write(b: Int) {
                    digest.update(b.toByte())
                    total++
                }

                override fun write(b: ByteArray, off: Int, len: Int) {
                    digest.update(b, off, len)
                    total += len
                }
            }

            AesCryptoEngine.decryptStreaming(
                encrypted = input,
                secretKey = secretKey,
                iv = readIv,
                associatedData = associatedData,
                sink = countingSink,
            )

            assertEquals(payloadBytes, countingSink.total)
            assertEquals(expectedDigest(payloadBytes).toHex(), digest.digest().toHex())
        } finally {
            encryptedFile.delete()
        }
    }

    @Test
    fun assetInputStreamReportsRemainingDecodedBytes() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val assetPath = "template/webview_shell.apk"
        val stream = context.assets.open(assetPath)
        val reported = stream.available().toLong()
        val actual = stream.use { drain(it) }
        assertEquals(actual, reported)
    }

    private fun drain(input: InputStream): Long {
        val buffer = ByteArray(64 * 1024)
        var total = 0L
        while (true) {
            val read = input.read(buffer)
            if (read < 0) break
            total += read
        }
        return total
    }

    private fun expectedDigest(total: Long): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        DeterministicStream(total).use { stream ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val read = stream.read(buffer)
                if (read < 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest()
    }

    private fun readFully(input: InputStream, target: ByteArray) {
        var offset = 0
        while (offset < target.size) {
            val read = input.read(target, offset, target.size - offset)
            if (read < 0) throw java.io.EOFException("truncated header")
            offset += read
        }
    }

    private fun pathLengthHeader(length: Int): ByteArray = byteArrayOf(
        ((length shr 24) and 0xFF).toByte(),
        ((length shr 16) and 0xFF).toByte(),
        ((length shr 8) and 0xFF).toByte(),
        (length and 0xFF).toByte(),
    )

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    /** Deterministic bytes produced on demand — never materialises the payload. */
    private class DeterministicStream(private val total: Long) : InputStream() {
        private var position = 0L

        override fun read(): Int {
            if (position >= total) return -1
            val value = valueAt(position).toInt() and 0xFF
            position++
            return value
        }

        override fun read(b: ByteArray, off: Int, len: Int): Int {
            if (position >= total) return -1
            val count = minOf(len.toLong(), total - position).toInt()
            for (i in 0 until count) {
                b[off + i] = valueAt(position + i)
            }
            position += count
            return count
        }

        private fun valueAt(index: Long): Byte =
            ((index * 31 + (index shr 8) + (index shr 16)) and 0xFF).toByte()
    }
}
