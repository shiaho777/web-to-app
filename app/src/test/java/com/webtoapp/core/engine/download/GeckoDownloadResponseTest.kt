package com.webtoapp.core.engine.download

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class GeckoDownloadResponseTest {

    @Test
    fun `blob and data uris stream through the response body`() {
        assertThat(GeckoDownloadResponse.isStreamed("blob:https://example.com/2f1c")).isTrue()
        assertThat(GeckoDownloadResponse.isStreamed("data:application/pdf;base64,AAA")).isTrue()
        assertThat(GeckoDownloadResponse.isStreamed("BLOB:https://example.com/2f1c")).isTrue()
        assertThat(GeckoDownloadResponse.isStreamed("https://example.com/file.pdf")).isFalse()
        assertThat(GeckoDownloadResponse.isStreamed("file:///sdcard/file.pdf")).isFalse()
    }

    @Test
    fun `convenience headers supply filename type and length for blob downloads`() {
        val meta = GeckoDownloadResponse.metadata(
            mapOf(
                "webresponseinfo-filename" to "report.pdf",
                "webresponseinfo-contenttype" to "application/pdf",
                "webresponseinfo-contentlength" to "4096"
            )
        )

        assertThat(meta.mimeType).isEqualTo("application/pdf")
        assertThat(meta.contentLength).isEqualTo(4096L)
        assertThat(meta.contentDisposition).isEqualTo("attachment; filename=\"report.pdf\"")
    }

    @Test
    fun `header lookup ignores case and mime parameters`() {
        val meta = GeckoDownloadResponse.metadata(
            mapOf(
                "Content-Type" to "video/mp4; charset=utf-8",
                "Content-Disposition" to "attachment; filename=\"clip.mp4\"",
                "Content-Length" to "1024"
            )
        )

        assertThat(meta.mimeType).isEqualTo("video/mp4")
        assertThat(meta.contentDisposition).isEqualTo("attachment; filename=\"clip.mp4\"")
        assertThat(meta.contentLength).isEqualTo(1024L)
    }

    @Test
    fun `missing headers fall back to octet stream and unknown length`() {
        val meta = GeckoDownloadResponse.metadata(emptyMap())

        assertThat(meta.mimeType).isEqualTo("application/octet-stream")
        assertThat(meta.contentDisposition).isEmpty()
        assertThat(meta.contentLength).isEqualTo(-1L)
    }

    @Test
    fun `content disposition wins over the convenience filename`() {
        val meta = GeckoDownloadResponse.metadata(
            mapOf(
                "content-disposition" to "attachment; filename=\"from-header.bin\"",
                "webresponseinfo-filename" to "from-convenience.bin"
            )
        )

        assertThat(meta.contentDisposition).isEqualTo("attachment; filename=\"from-header.bin\"")
    }
}
