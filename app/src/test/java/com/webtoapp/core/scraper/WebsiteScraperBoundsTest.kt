package com.webtoapp.core.scraper

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class WebsiteScraperBoundsTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()

    private fun scraper() = WebsiteScraper(context)

    private fun chunkedBody(bytes: ByteArray): ResponseBody {
        val buffer = Buffer().write(bytes)
        return object : ResponseBody() {
            override fun contentType() = "application/octet-stream".toMediaType()
            override fun contentLength() = -1L
            override fun source() = buffer
        }
    }

    @Test
    fun `readBoundedBody returns data under the cap without content length`() {
        val payload = ByteArray(1024) { 'a'.code.toByte() }
        val result = scraper().readBoundedBody(chunkedBody(payload), 4096)
        assertThat(result).isEqualTo(payload)
    }

    @Test
    fun `readBoundedBody aborts a chunked body past the cap`() {
        val payload = ByteArray(64 * 1024) { 'a'.code.toByte() }
        val result = scraper().readBoundedBody(chunkedBody(payload), 8 * 1024)
        assertThat(result).isNull()
    }

    @Test
    fun `readBoundedBody aborts a sized body past the cap`() {
        val payload = ByteArray(64 * 1024) { 'a'.code.toByte() }
        val body = payload.toResponseBody("application/octet-stream".toMediaType())
        assertThat(scraper().readBoundedBody(body, 8 * 1024)).isNull()
    }

    @Test
    fun `replaceWholeUrl rewrites a quoted url token`() {
        val content = """<script src="https://x/a.js"></script>"""
        val result = scraper().replaceWholeUrl(content, "https://x/a.js", "js/a.js")
        assertThat(result).isEqualTo("""<script src="js/a.js"></script>""")
    }

    @Test
    fun `replaceWholeUrl does not corrupt a longer sibling map url`() {
        val content = """<script src="https://x/a.js"></script><link href="https://x/a.js.map">"""
        val result = scraper().replaceWholeUrl(content, "https://x/a.js", "js/a.js")
        assertThat(result).isEqualTo("""<script src="js/a.js"></script><link href="https://x/a.js.map">""")
    }

    @Test
    fun `replaceWholeUrl leaves a query variant of a longer url untouched`() {
        val content = """fetch("https://x/a.js?v=2")"""
        val result = scraper().replaceWholeUrl(content, "https://x/a.js", "js/a.js")
        assertThat(result).isNull()
    }

    @Test
    fun `replaceWholeUrl matches a css url token followed by paren`() {
        val content = "background:url(https://x/a.png)"
        val result = scraper().replaceWholeUrl(content, "https://x/a.png", "img/a.png")
        assertThat(result).isEqualTo("background:url(img/a.png)")
    }

    @Test
    fun `replaceWholeUrl matches a url at end of content`() {
        val content = "see https://x/a.js"
        val result = scraper().replaceWholeUrl(content, "https://x/a.js", "js/a.js")
        assertThat(result).isEqualTo("see js/a.js")
    }

    @Test
    fun `replaceWholeUrl returns null when the url is absent`() {
        assertThat(scraper().replaceWholeUrl("nothing here", "https://x/a.js", "js/a.js")).isNull()
    }
}