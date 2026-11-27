package com.webtoapp.core.webview

import android.net.Uri
import android.os.Looper
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import java.io.ByteArrayInputStream
import java.io.Closeable
import java.io.InputStream
import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class WebViewStreamRoutesTest {
    private val pageUrl = "https://example.com/player"
    private val view = WebView(ApplicationProvider.getApplicationContext())

    @After
    fun destroy() {
        WebViewStreamRoutes.destroy(view)
        view.destroy()
    }

    @Test
    fun `one shot reads do not evict a reused media URL`() {
        WebViewStreamRoutes.onPageStarted(view, pageUrl)
        val page = checkNotNull(WebViewStreamRoutes.current(view))
        val media = checkNotNull(page.register(false, "media") { response(ByteArrayInputStream(byteArrayOf(7))) })
        assertThat(page.register(false, "media") { response() }).isEqualTo(media)
        repeat(160) {
            val transient = checkNotNull(page.register(true) { response() })
            assertThat(fetch(transient, "HEAD").statusCode).isEqualTo(404)
            fetch(transient).data.use { assertThat(it.read()).isEqualTo(42) }
            assertThat(fetch(transient).statusCode).isEqualTo(404)
        }
        fetch(media).data.use { assertThat(it.read()).isEqualTo(42) }
        page.release(media)
        assertThat(fetch(media).statusCode).isEqualTo(404)
    }

    @Test
    fun `unclaimed responses expire and release capacity`() {
        WebViewStreamRoutes.onPageStarted(view, pageUrl)
        val page = checkNotNull(WebViewStreamRoutes.current(view))
        val expired = CountDownLatch(32)
        val urls = List(32) { checkNotNull(page.register(true, close = expired::countDown) { response() }) }
        assertThat(page.register(true) { response() }).isNull()
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(31))
        assertThat(expired.await(5, TimeUnit.SECONDS)).isTrue()
        assertThat(fetch(urls.first()).statusCode).isEqualTo(404)
        assertThat(page.register(true) { response() }).isNotNull()
    }

    @Test
    fun `navigation within the same origin cancels pending and active work`() {
        WebViewStreamRoutes.onPageStarted(view, pageUrl)
        val page = checkNotNull(WebViewStreamRoutes.current(view))
        val pending = CountDownLatch(1)
        checkNotNull(page.watch(Closeable { pending.countDown() }))
        val active = CountDownLatch(1)
        val canceled = CountDownLatch(1)
        val url = checkNotNull(page.register(true, close = canceled::countDown) {
            response(object : ByteArrayInputStream(byteArrayOf(42)) {
                override fun close() { active.countDown() }
            })
        })
        assertThat(fetch(url).statusCode).isEqualTo(200)
        WebViewStreamRoutes.onPageStarted(view, pageUrl)
        assertThat(WebViewStreamRoutes.isCurrent(view, page)).isFalse()
        assertThat(page.register(true) { response() }).isNull()
        assertThat(pending.count).isEqualTo(0L)
        assertThat(canceled.await(5, TimeUnit.SECONDS)).isTrue()
        assertThat(active.await(5, TimeUnit.SECONDS)).isTrue()
        assertThat(fetch(url).statusCode).isEqualTo(404)
        WebViewStreamRoutes.destroy(view)
        WebViewStreamRoutes.onPageStarted(view, pageUrl)
        assertThat(WebViewStreamRoutes.page(view, pageUrl)).isNull()
    }

    private fun response(input: InputStream = ByteArrayInputStream(byteArrayOf(42))) =
        WebResourceResponse("application/octet-stream", null, 200, "OK", emptyMap(), input)

    private fun fetch(url: String, method: String = "GET"): WebResourceResponse =
        checkNotNull(WebViewStreamRoutes.intercept(view, object : WebResourceRequest {
            override fun getUrl(): Uri = Uri.parse(url)
            override fun getMethod(): String = method
            override fun isForMainFrame(): Boolean = false
            override fun isRedirect(): Boolean = false
            override fun hasGesture(): Boolean = false
            override fun getRequestHeaders(): Map<String, String> = emptyMap()
        }))
}
