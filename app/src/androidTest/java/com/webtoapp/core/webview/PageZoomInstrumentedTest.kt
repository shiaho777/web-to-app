package com.webtoapp.core.webview

import android.os.SystemClock
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PageZoomInstrumentedTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val finishedUrls = ArrayBlockingQueue<String>(8)
    private lateinit var webView: WebView

    @After
    fun destroyWebView() {
        if (::webView.isInitialized) instrumentation.runOnMainSync { webView.destroy() }
    }

    @Test
    fun zoomOutSurvivesLateViewportChangesAndFillsTheWindow() {
        loadPage(70)

        val report = viewportReport()
        assertScaleAndHeight(report.getJSONObject("first"), 0.7)
        assertScaleAndHeight(report.getJSONObject("settled"), 0.7)
        assertTrue(report.getBoolean("isTopLevel"))
        assertEquals(12, report.getInt("writes"))
        assertEquals("width=device-width,initial-scale=1", report.getString("viewport"))
    }

    @Test
    fun zoomInPersistsAcrossNativeNavigationAndReload() {
        loadPage(150)
        assertSettledScale(1.5)

        val nextUrl = LANDING_URL.replace("/start", "/next")
        instrumentation.runOnMainSync { webView.loadUrl(nextUrl) }
        awaitPage(nextUrl)
        assertSettledScale(1.5)

        instrumentation.runOnMainSync { webView.reload() }
        awaitPage(nextUrl)
        assertSettledScale(1.5)

        instrumentation.runOnMainSync {
            assertTrue("The first top-level URL should remain in WebView history", webView.canGoBack())
            webView.goBack()
        }
        awaitPage(LANDING_URL)
        assertSettledScale(1.5)
    }

    private fun loadPage(zoomPercent: Int) {
        instrumentation.runOnMainSync {
            webView = WebView(instrumentation.targetContext).apply {
                settings.javaScriptEnabled = true
                settings.useWideViewPort = true
                applySystemPageZoom(zoomPercent)
                webViewClient = object : WebViewClient() {
                    override fun shouldInterceptRequest(
                        view: WebView,
                        request: WebResourceRequest
                    ): WebResourceResponse? {
                        if (request.url.host != "zoom.example.test" || !request.isForMainFrame) {
                            return null
                        }
                        return WebResourceResponse(
                            "text/html",
                            "UTF-8",
                            200,
                            "OK",
                            mapOf("Content-Security-Policy" to "frame-ancestors 'none'"),
                            PAGE.byteInputStream()
                        )
                    }

                    override fun onPageFinished(view: WebView, url: String) {
                        finishedUrls.offer(url)
                    }
                }
                measure(
                    View.MeasureSpec.makeMeasureSpec(600, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(800, View.MeasureSpec.EXACTLY)
                )
                layout(0, 0, measuredWidth, measuredHeight)
                loadUrl(LANDING_URL)
            }
        }
        awaitPage(LANDING_URL)
    }

    private fun awaitPage(url: String) {
        assertEquals(url, finishedUrls.poll(20, TimeUnit.SECONDS))
        instrumentation.runOnMainSync { assertEquals(url, webView.url) }
    }

    private fun viewportReport(): JSONObject {
        val deadline = SystemClock.uptimeMillis() + 10_000
        while (SystemClock.uptimeMillis() < deadline) {
            val ready = CountDownLatch(1)
            var result: String? = null
            instrumentation.runOnMainSync {
                webView.evaluateJavascript("window.zoomReport || null") {
                    result = it
                    ready.countDown()
                }
            }
            assertTrue(ready.await(3, TimeUnit.SECONDS))
            val json = result
            if (json != null && json.startsWith("{")) return JSONObject(json)
            SystemClock.sleep(40)
        }
        error("WebView did not report its viewport")
    }

    private fun assertScaleAndHeight(report: JSONObject, scale: Double) {
        assertEquals("visual viewport scale", scale, report.getDouble("scale"), 0.02)
        assertEquals("100dvh element height", report.getDouble("viewportHeight"), report.getDouble("height"), 3.0)
    }

    private fun assertSettledScale(scale: Double) {
        val settled = viewportReport().getJSONObject("settled")
        assertEquals("visual viewport scale", scale, settled.getDouble("scale"), 0.02)
    }

    private companion object {
        const val LANDING_URL = "https://zoom.example.test/start"

        val PAGE = """
            <!doctype html>
            <style>body{margin:0}main{height:100vh;height:100dvh}</style>
            <main></main>
            <script>
            const measure = () => ({
              scale: visualViewport.scale,
              viewportHeight: visualViewport.height,
              height: document.querySelector('main').getBoundingClientRect().height
            });
            requestAnimationFrame(() => {
              const first = measure();
              const meta = document.createElement('meta');
              meta.name = 'viewport';
              document.head.appendChild(meta);
              let writes = 0;
              const timer = setInterval(() => {
                writes++;
                meta.content = 'width=device-width,initial-scale=' + (writes % 2 ? '0.9' : '1');
                if (writes === 12) {
                  clearInterval(timer);
                  requestAnimationFrame(() => {
                    window.zoomReport = {first, settled: measure(), writes,
                      isTopLevel: window.top === window, viewport: meta.content};
                  });
                }
              }, 20);
            });
            </script>
        """.trimIndent()
    }
}
