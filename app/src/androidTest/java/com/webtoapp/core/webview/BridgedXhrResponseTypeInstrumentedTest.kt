package com.webtoapp.core.webview

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.ByteArrayInputStream
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.json.JSONObject
import org.json.JSONTokener
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * On-device proof that a binary `responseType` survives the private-network XHR wrapper.
 *
 * The wrapper (`BridgedXHR`) used to keep `responseType` on itself and never forward it
 * to the native request. The native request therefore stayed on the default text type,
 * every binary loader received a String, and three.js reported
 * `Unexpected token 'g', "glTF0!..." is not valid JSON` for a 62 MB GLB.
 *
 * These tests run the real script in a real WebView against a real HTTP response, set
 * the type the way `GLTFLoader` does, and read back what the page actually receives.
 * They fail if either half regresses: the forwarding, or the responseText handling.
 */
@RunWith(AndroidJUnit4::class)
class BridgedXhrResponseTypeInstrumentedTest {

    private val payload = ByteArray(1 * 1024 * 1024) { index -> ((index * 31 + 7) and 0xFF).toByte() }
    private var webView: WebView? = null
    private var pageFinished = CountDownLatch(1)

    @Before
    fun loadPage() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            val view = WebView(instrumentation.targetContext)
            view.settings.javaScriptEnabled = true
            view.settings.domStorageEnabled = true
            view.webViewClient = object : WebViewClient() {
                override fun shouldInterceptRequest(
                    view: WebView,
                    request: WebResourceRequest
                ): WebResourceResponse? {
                    val path = request.url.path.orEmpty()
                    return when {
                        path.endsWith("/bin.dat") -> WebResourceResponse(
                            "application/octet-stream",
                            null,
                            ByteArrayInputStream(payload)
                        )

                        path.endsWith("/text.txt") -> WebResourceResponse(
                            "text/plain",
                            "UTF-8",
                            ByteArrayInputStream(TEXT_BODY.toByteArray())
                        )

                        else -> null
                    }
                }

                override fun onPageFinished(view: WebView, url: String) {
                    pageFinished.countDown()
                }
            }
            view.loadDataWithBaseURL(BASE_URL, PAGE, "text/html", "UTF-8", null)
            webView = view
        }
        assertTrue("page never finished loading", pageFinished.await(30, TimeUnit.SECONDS))
    }

    @After
    fun tearDown() {
        val view = webView
        webView = null
        if (view != null) {
            InstrumentationRegistry.getInstrumentation().runOnMainSync { view.destroy() }
        }
    }

    @Test
    fun binaryResponseTypeReachesThePageAsObjectBytes() {
        val result = runProbe()

        assertEquals(200, result.getInt("status"))
        assertEquals("arraybuffer", result.getString("responseType"))
        assertEquals("[object ArrayBuffer]", result.getString("responseClass"))
        assertEquals(payload.size, result.getInt("byteLength"))
        assertEquals(7, result.getInt("firstByte"))
    }

    @Test
    fun binaryResponseNeverArrivesAsText() {
        val result = runProbe()

        // The defect this pins: `response` was a String, so a GLB went to JSON.parse.
        assertEquals("[object ArrayBuffer]", result.getString("responseClass"))
        // responseText has no text form under a binary type; it must stay a readable
        // empty string rather than throwing InvalidStateError at the page.
        assertEquals("string:0", result.getString("responseText"))
    }

    @Test
    fun defaultResponseTypeStillDeliversText() {
        val result = runProbe()

        assertEquals("[object String]", result.getString("textClass"))
        assertEquals(TEXT_BODY, result.getString("textValue"))
    }

    @Test
    fun readyStateConstantsSurviveTheWrapper() {
        val result = runProbe()

        // A page that writes `xhr.readyState === XMLHttpRequest.DONE` reads the
        // constant off the constructor the wrapper replaced.
        assertEquals(4, result.getInt("doneConstant"))
        assertEquals(0, result.getInt("unsentConstant"))
    }

    private fun runProbe(): JSONObject {
        val view = requireNotNull(webView) { "WebView is not ready" }

        eval(view, WebViewManager.PrivateNetworkApiBridgeScriptHolder.SCRIPT)
        eval(view, PROBE_SCRIPT)

        val deadline = System.currentTimeMillis() + 30_000
        while (System.currentTimeMillis() < deadline) {
            val raw = unquote(eval(view, "window.__wtaXhrProbe ? JSON.stringify(window.__wtaXhrProbe) : ''"))
            if (raw.isNotEmpty()) return JSONObject(raw)
            Thread.sleep(100)
        }
        throw AssertionError(
            "probe did not finish: " + unquote(eval(view, "String(window.__wtaXhrProbeError || '')"))
        )
    }

    private fun eval(view: WebView, script: String): String {
        val result = arrayOfNulls<String>(1)
        val latch = CountDownLatch(1)
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            view.evaluateJavascript(script) { value ->
                result[0] = value
                latch.countDown()
            }
        }
        check(latch.await(30, TimeUnit.SECONDS)) { "JS evaluation timed out" }
        return result[0] ?: "null"
    }

    /** `evaluateJavascript` hands back a JSON literal; a string result arrives quoted. */
    private fun unquote(raw: String): String {
        if (raw.isEmpty() || raw == "null") return ""
        return JSONTokener(raw).nextValue() as? String ?: raw
    }

    private companion object {
        /** Private host, so the wrapper installs (it is a local packaged page). */
        const val BASE_URL = "http://127.0.0.1:8080/"
        const val TEXT_BODY = "hello-from-webtoapp"

        const val PAGE = """
            <!doctype html><html><head><meta charset="utf-8"><title>probe</title></head>
            <body></body></html>
        """

        const val PROBE_SCRIPT = """
            (function() {
                window.__wtaXhrProbe = null;
                var out = {};
                function get(url, type, next) {
                    var xhr = new XMLHttpRequest();
                    xhr.open('GET', url, true);
                    if (type) { xhr.responseType = type; }
                    xhr.onload = function() { next(xhr); };
                    xhr.onerror = function() {
                        window.__wtaXhrProbeError = 'network:' + url;
                        next(xhr);
                    };
                    xhr.send();
                }
                try {
                    get('/bin.dat', 'arraybuffer', function(xhr) {
                        out.status = xhr.status;
                        out.doneConstant = XMLHttpRequest.DONE;
                        out.unsentConstant = XMLHttpRequest.UNSENT;
                        out.responseType = xhr.responseType;
                        out.responseClass = Object.prototype.toString.call(xhr.response);
                        out.byteLength = (xhr.response && xhr.response.byteLength != null)
                            ? xhr.response.byteLength : -1;
                        out.firstByte = out.byteLength > 0
                            ? new Uint8Array(xhr.response)[0] : -1;
                        var text = 'unreadable';
                        try {
                            var t = xhr.responseText;
                            text = typeof t + ':' + t.length;
                        } catch (e) {
                            text = 'threw:' + (e && e.name);
                        }
                        out.responseText = text;
                        get('/text.txt', '', function(second) {
                            out.textClass = Object.prototype.toString.call(second.response);
                            out.textValue = String(second.response || '');
                            window.__wtaXhrProbe = out;
                        });
                    });
                } catch (e) {
                    window.__wtaXhrProbeError = String(e && (e.name + ': ' + e.message));
                }
            })();
        """
    }
}
