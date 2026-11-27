package com.webtoapp.util

import com.google.common.truth.Truth.assertThat
import java.io.ByteArrayInputStream
import java.io.EOFException
import org.junit.Test
import org.junit.Assert.assertThrows

class DownloadArtifactTest {
    @Test
    fun `APK detection recognizes names and response MIME types`() {
        assertThat(DownloadArtifact.isApk("example.APK", "application/octet-stream")).isTrue()
        assertThat(DownloadArtifact.isApk("build", "application/vnd.android.package-archive; charset=utf-8")).isTrue()
        assertThat(DownloadArtifact.isApk("empty.txt", "text/plain")).isFalse()
    }

    @Test
    fun `an empty APK produces a failure before copying`() {
        val error = assertThrows(EOFException::class.java) {
            DownloadArtifact.requireContent(ByteArrayInputStream(byteArrayOf()), "Empty APK")
        }
        assertThat(error).hasMessageThat().isEqualTo("Empty APK")
    }

    @Test
    fun `checking the first byte preserves the payload`() {
        val bytes = byteArrayOf(0x50, 0x4b, 0x03, 0x04)
        val stream = DownloadArtifact.requireContent(ByteArrayInputStream(bytes), "Empty APK")
        assertThat(stream.readBytes()).isEqualTo(bytes)
    }
}
