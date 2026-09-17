package com.webtoapp.core.agent.files

import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import java.io.IOException
import java.io.InputStream
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Regression coverage for the attachment-as-reference rework: uploads are
 * stream-copied into the sandbox and reads are windowed, so multi-GB files can
 * never be materialised into heap memory (the old `readBytes()` attach path
 * OOM-crashed before the model even ran).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ProjectFileManagerTest {

    private lateinit var files: ProjectFileManager
    private val sid = "test-session"

    @Before
    fun setUp() = runBlocking {
        files = ProjectFileManager(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun `writeStream copies content and reports the real size`() {
        val data = ByteArray(3 * 1024 * 1024) { (it % 251).toByte() }

        val info = files.writeStream(sid, "uploads/big.bin", data.inputStream())

        assertThat(info).isNotNull()
        assertThat(info!!.sizeBytes).isEqualTo(data.size.toLong())
        assertThat(files.readBytes(sid, "uploads/big.bin")).isEqualTo(data)
    }

    @Test
    fun `writeStream removes the partial file when the source fails mid-copy`() {
        val failing = object : InputStream() {
            private var n = 0
            override fun read(): Int = if (n++ < 100) 'A'.code else throw IOException("boom")
            override fun read(b: ByteArray, off: Int, len: Int): Int {
                if (n >= 100) throw IOException("boom")
                val c = minOf(len, 100 - n)
                java.util.Arrays.fill(b, off, off + c, 'A'.code.toByte())
                n += c
                return c
            }
        }

        assertThat(files.writeStream(sid, "uploads/part.bin", failing)).isNull()
        assertThat(files.exists(sid, "uploads/part.bin")).isFalse()
    }

    @Test
    fun `readTextWindow returns a bounded offset window with remaining count`() {
        files.writeText(sid, "a.txt", (1..100).joinToString("\n") { "line $it" })

        val w = files.readTextWindow(sid, "a.txt", offset = 10, limit = 5)!!

        assertThat(w.binary).isFalse()
        assertThat(w.lines).containsExactly(
            "line 11", "line 12", "line 13", "line 14", "line 15"
        ).inOrder()
        assertThat(w.remainingLines).isEqualTo(85)
        assertThat(w.remainingCapped).isFalse()
        assertThat(w.totalBytes).isGreaterThan(0L)
    }

    @Test
    fun `readTextWindow on an empty file yields an empty window`() {
        files.writeText(sid, "empty.txt", "")

        val w = files.readTextWindow(sid, "empty.txt", 0, 10)!!

        assertThat(w.binary).isFalse()
        assertThat(w.lines).isEmpty()
        assertThat(w.remainingLines).isEqualTo(0)
    }

    @Test
    fun `readTextWindow rejects binary content without reading it`() {
        files.writeBytes(sid, "b.bin", byteArrayOf(1, 2, 0, 3, 4))

        val w = files.readTextWindow(sid, "b.bin", 0, 10)!!

        assertThat(w.binary).isTrue()
        assertThat(w.lines).isEmpty()
    }

    @Test
    fun `readTextWindow truncates over-long lines`() {
        files.writeText(sid, "long.txt", "x".repeat(5000))

        val w = files.readTextWindow(sid, "long.txt", 0, 10)!!

        assertThat(w.lines).hasSize(1)
        assertThat(w.lines[0].length).isLessThan(5000)
        assertThat(w.lines[0]).endsWith("…")
    }

    @Test
    fun `readTextWindow handles CRLF endings`() {
        files.writeBytes(sid, "crlf.txt", "a\r\nb\r\nc".toByteArray())

        val w = files.readTextWindow(sid, "crlf.txt", 0, 10)!!

        assertThat(w.lines).containsExactly("a", "b", "c").inOrder()
    }

    @Test
    fun `countLines gives exact counts below the cap and -1 above it`() {
        files.writeText(sid, "many.txt", (1..300).joinToString("\n") { "l$it" })

        assertThat(files.countLines(sid, "many.txt", maxLines = 1000)).isEqualTo(300)
        assertThat(files.countLines(sid, "many.txt", maxLines = 100)).isEqualTo(-1)
    }

    @Test
    fun `countLines returns -1 for binary files`() {
        files.writeBytes(sid, "c.bin", byteArrayOf(9, 0, 9))

        assertThat(files.countLines(sid, "c.bin")).isEqualTo(-1)
    }
}
