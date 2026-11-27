package com.webtoapp.core.webview

import android.net.Uri
import android.webkit.WebResourceRequest
import com.google.common.truth.Truth.assertThat
import java.io.ByteArrayInputStream
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DirectoryFileResponseTest {
    @Test
    fun `byte ranges support bounded open and suffix reads`() {
        assertThat(DirectoryFileResponse.parseRange("bytes=100-199", 400))
            .isEqualTo(DirectoryFileResponse.ByteRange(100, 199))
        assertThat(DirectoryFileResponse.parseRange("bytes=4000000000-", 4000000100))
            .isEqualTo(DirectoryFileResponse.ByteRange(4000000000, 4000000099))
        assertThat(DirectoryFileResponse.parseRange("bytes=-10", 400))
            .isEqualTo(DirectoryFileResponse.ByteRange(390, 399))
        assertThat(DirectoryFileResponse.parseRange("bytes=390-600", 400))
            .isEqualTo(DirectoryFileResponse.ByteRange(390, 399))
    }

    @Test
    fun `invalid and unsatisfiable ranges are rejected`() {
        for (header in listOf("bytes=400-", "bytes=20-9", "bytes=-0", "items=0-1", "bytes=0-1,4-5")) {
            assertThat(DirectoryFileResponse.parseRange(header, 400)).isNull()
        }
        assertThat(DirectoryFileResponse.parseRange("bytes=0-", 0)).isNull()
    }

    @Test
    fun `WebView seeks within the source and reads only the requested bytes`() {
        val bytes = ByteArray(128) { it.toByte() }
        val file = LocalDirectoryStore.StreamFile(bytes.size.toLong(), "audio/wav") { offset ->
            assertThat(offset).isEqualTo(0)
            ByteArrayInputStream(bytes)
        }
        val request = object : WebResourceRequest {
            override fun getUrl(): Uri = Uri.parse("http://127.0.0.1/stream")
            override fun getMethod(): String = "GET"
            override fun isForMainFrame(): Boolean = false
            override fun isRedirect(): Boolean = false
            override fun hasGesture(): Boolean = false
            override fun getRequestHeaders(): Map<String, String> = mapOf("Range" to "bytes=64-67")
        }

        val response = checkNotNull(DirectoryFileResponse.create(file, request, false))
        assertThat(response.statusCode).isEqualTo(206)
        assertThat(response.responseHeaders["Content-Range"]).isEqualTo("bytes 64-67/128")
        response.data.use { input ->
            assertThat(input.available()).isAtLeast(68)
            assertThat(input.skip(64)).isEqualTo(64)
            assertThat(input.readBytes()).isEqualTo(byteArrayOf(64, 65, 66, 67))
        }
    }

}
