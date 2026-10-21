package com.webtoapp.core.webview

import android.util.Base64
import com.google.common.truth.Truth.assertThat
import java.io.File
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ShareUploadTest {

    @get:Rule
    val folder = TemporaryFolder()

    @Test
    fun `caption keeps the text and the url`() {
        assertThat(joinShareCaption("hello", "https://example.com"))
            .isEqualTo("hello\nhttps://example.com")
        assertThat(joinShareCaption("  hello  ", null)).isEqualTo("hello")
        assertThat(joinShareCaption(null, " https://example.com ")).isEqualTo("https://example.com")
    }

    @Test
    fun `mime falls back to the file name and groups mixed types`() {
        assertThat(shareMime("photo.JPG", "")).isEqualTo("image/jpeg")
        assertThat(shareMime("photo.jpg", "application/octet-stream")).isEqualTo("image/jpeg")
        assertThat(shareMime("note.txt", "text/plain")).isEqualTo("text/plain")
        assertThat(shareMime("page.html", "")).isEqualTo("text/html")
        assertThat(shareMime("page.htm", "application/octet-stream")).isEqualTo("text/html")
        assertThat(shareMime("letter.doc", "")).isEqualTo("application/msword")
        assertThat(shareMime("letter.docx", "")).isEqualTo(
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        )
        assertThat(shareMime("unknown.zzznotreal", "")).isEqualTo("application/octet-stream")
        assertThat(commonShareMime(listOf("image/jpeg", "image/png"))).isEqualTo("image/*")
        assertThat(commonShareMime(listOf("image/png", "application/pdf"))).isEqualTo("*/*")
        assertThat(sanitizeShareFileName("../../a b.png")).isEqualTo("a_b.png")
    }

    @Test
    fun `chunks reassemble a file even when a quartet is split`() {
        val bytes = ByteArray(1000) { it.toByte() }
        val encoded = Base64.encodeToString(bytes, Base64.NO_WRAP)
        val upload = ShareUpload("Title", "caption")
        val dir = folder.newFolder("share")
        assertThat(upload.beginFile(dir, "shot.png", "")).isTrue()
        assertThat(upload.appendChunk(encoded.substring(0, 3))).isTrue()
        assertThat(upload.appendChunk(encoded.substring(3))).isTrue()
        val files = upload.finish()
        assertThat(files).hasSize(1)
        assertThat(files!![0].mime).isEqualTo("image/png")
        assertThat(File(dir, files[0].storedName).readBytes()).isEqualTo(bytes)
    }

    @Test
    fun `two files stay separate and a huge chunk is refused`() {
        val upload = ShareUpload()
        val dir = folder.newFolder("multi")
        assertThat(upload.beginFile(dir, "a.png", "image/png")).isTrue()
        assertThat(upload.appendChunk(Base64.encodeToString(byteArrayOf(1, 2, 3), Base64.NO_WRAP))).isTrue()
        assertThat(upload.beginFile(dir, "b.png", "image/png")).isTrue()
        assertThat(upload.appendChunk(Base64.encodeToString(byteArrayOf(4), Base64.NO_WRAP))).isTrue()
        assertThat(upload.finish()).hasSize(2)

        val huge = ShareUpload()
        assertThat(huge.beginFile(dir, "big.bin", "application/octet-stream")).isTrue()
        assertThat(huge.appendChunk("A".repeat(ShareUpload.MAX_CHUNK_CHARS + 1))).isFalse()
    }
}
