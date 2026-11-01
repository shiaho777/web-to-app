package com.webtoapp.core.webview

import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import java.io.BufferedOutputStream
import java.io.File
import java.io.OutputStream
import java.net.ServerSocket
import java.util.Collections
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.json.JSONTokener
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-end proof against the 3D project that reported the crash.
 *
 * Runs the project's own `index.html`, its `three.r128.js` + `GLTFLoader.js`, and the
 * real 62 MB `scene.glb`, inside this app's WebView with the private-network XHR wrapper
 * installed exactly the way the app installs it (document-start script).
 *
 * The wrapper used to swallow `responseType`, so `GLTFLoader` received a String and three.js
 * failed with `Unexpected token 'g', "glTF0!..." is not valid JSON`. Success here means
 * `window.__3.model` exists — the callback the page itself only runs after the GLB parsed.
 *
 * The fixture is pushed by hand (62 MB does not belong in a test APK):
 *   adb push <decrypted>/html /data/local/tmp/wta3d_html
 *   adb shell run-as com.webtoapp cp -r /data/local/tmp/wta3d_html files/wta3d/html
 */
@RunWith(AndroidJUnit4::class)
class LargeGltfLoadInstrumentedTest {

    private lateinit var root: File
    private var server: ServerSocket? = null
    private var webView: WebView? = null
    private val console = Collections.synchronizedList(mutableListOf<String>())

    @Before
    fun requireFixture() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        root = File(context.filesDir, "wta3d/html")
        assertTrue(
            "fixture missing at ${root.absolutePath}; push the decrypted html/ folder there",
            File(root, "index.html").isFile &&
                File(root, "scene.glb").length() > 60_000_000L
        )
    }

    @After
    fun tearDown() {
        val view = webView
        webView = null
        if (view != null) {
            InstrumentationRegistry.getInstrumentation().runOnMainSync { view.destroy() }
        }
        runCatching { server?.close() }
    }

    @Test
    fun sixtyTwoMegabyteGlbLoadsThroughTheWrapper() {
        val port = startServer(root)
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val loaded = CountDownLatch(1)

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val view = WebView(context)
            view.settings.javaScriptEnabled = true
            view.settings.domStorageEnabled = true
            view.settings.allowFileAccess = false
            view.webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(message: ConsoleMessage): Boolean {
                    console.add("${message.messageLevel()} ${message.message()}")
                    return true
                }
            }
            view.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    loaded.countDown()
                }
            }
            // Document-start scripts only apply to loads registered after the call, so
            // this has to happen before loadUrl — the same order the app uses.
            assertTrue(
                "document-start scripts are unavailable on this WebView",
                WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT)
            )
            WebViewCompat.addDocumentStartJavaScript(
                view,
                WebViewManager.PrivateNetworkApiBridgeScriptHolder.SCRIPT,
                setOf("*")
            )
            webView = view
            view.loadUrl("http://127.0.0.1:$port/index.html")
        }

        assertTrue("page never finished loading", loaded.await(60, TimeUnit.SECONDS))

        val outcome = awaitModel(requireNotNull(webView), timeoutMs = 180_000)
        val vi = outcome.verdict
        assertTrue(
            "model did not load: $vi\nconsole:\n" + console.takeLast(20).joinToString("\n"),
            vi.startsWith("ok")
        )
        // Pin the real scene: the expected GLB has 204 meshes / ~954k triangles.
        // A stub or a partial parse would not reach these numbers.
        val meshes = Regex("meshes=(\\d+)").find(vi)?.groupValues?.get(1)?.toInt() ?: -1
        val triangles = Regex("tris=(\\d+)").find(vi)?.groupValues?.get(1)?.toInt() ?: -1
        // Without the wrapper in place this test proves nothing: the page would use the
        // stock XMLHttpRequest and pass whether or not the wrapper forwards responseType.
        assertTrue(
            "the XHR wrapper was not installed, so this run proves nothing: $vi",
            vi.contains("wrapper=BridgedXHR")
        )
        assertTrue("expected the full scene, got $vi", meshes >= 150)
        assertTrue("expected real geometry, got $vi", triangles >= 500_000)
        // 62 MB cannot cross WebView and parse to 1.7M vertices instantly; a verdict
        // this fast would mean the probe read a stale marker.
        assertTrue("suspiciously fast (${outcome.elapsedMs}ms): $vi", outcome.elapsedMs >= 200)
        println("[gltf] $vi in ${outcome.elapsedMs}ms")
    }

    /** Polls the page's own success marker and its error banner. */
    private fun awaitModel(view: WebView, timeoutMs: Long): Outcome {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val started = System.currentTimeMillis()
        val deadline = started + timeoutMs
        var last = ""
        while (System.currentTimeMillis() < deadline) {
            val raw = eval(instrumentation, view, PROBE)
            last = unquote(raw)
            val elapsed = System.currentTimeMillis() - started
            when {
                last.startsWith("ok") -> return Outcome(last, elapsed)
                last.startsWith("err") -> return Outcome(last, elapsed)
            }
            Thread.sleep(500)
        }
        return Outcome("timeout($last)", System.currentTimeMillis() - started)
    }

    private fun eval(
        instrumentation: android.app.Instrumentation,
        view: WebView,
        script: String
    ): String {
        val holder = arrayOfNulls<String>(1)
        val latch = CountDownLatch(1)
        instrumentation.runOnMainSync {
            view.evaluateJavascript(script) { value ->
                holder[0] = value
                latch.countDown()
            }
        }
        check(latch.await(30, TimeUnit.SECONDS)) { "JS evaluation timed out" }
        return holder[0] ?: "null"
    }

    private fun unquote(raw: String): String {
        if (raw.isEmpty() || raw == "null") return ""
        return JSONTokener(raw).nextValue() as? String ?: raw
    }

    private data class Outcome(val verdict: String, val elapsedMs: Long)

    /** Minimal static file server: correct Content-Length, streams the body. */
    private fun startServer(directory: File): Int {
        val socket = ServerSocket(0, 16, java.net.InetAddress.getByName("127.0.0.1"))
        server = socket
        Thread({
            while (!socket.isClosed) {
                val client = runCatching { socket.accept() }.getOrNull() ?: break
                Thread { serve(client, directory) }.apply { isDaemon = true }.start()
            }
        }, "fixture-http").apply { isDaemon = true }.start()
        return socket.localPort
    }

    private fun serve(client: java.net.Socket, directory: File) {
        client.use { connection ->
            val reader = connection.getInputStream().bufferedReader()
            val requestLine = reader.readLine().orEmpty()
            while (true) {
                val header = reader.readLine() ?: break
                if (header.isEmpty()) break
            }
            val path = requestLine.split(' ').getOrNull(1)?.substringBefore('?') ?: "/"
            val file = File(directory, path.removePrefix("/").ifEmpty { "index.html" })
            val out = BufferedOutputStream(connection.getOutputStream())
            if (!file.isFile || !file.canonicalPath.startsWith(directory.canonicalPath)) {
                out.write("HTTP/1.1 404 Not Found\r\nContent-Length: 0\r\nConnection: close\r\n\r\n".toByteArray())
                out.flush()
                return@use
            }
            val header = buildString {
                append("HTTP/1.1 200 OK\r\n")
                append("Content-Type: ").append(contentType(file.name)).append("\r\n")
                append("Content-Length: ").append(file.length()).append("\r\n")
                append("Cache-Control: no-store\r\n")
                append("Connection: close\r\n\r\n")
            }
            out.write(header.toByteArray())
            file.inputStream().use { input -> input.copyTo(out, 64 * 1024) }
            out.flush()
        }
    }

    private fun contentType(name: String): String = when {
        name.endsWith(".html") -> "text/html; charset=utf-8"
        name.endsWith(".js") -> "application/javascript; charset=utf-8"
        name.endsWith(".glb") -> "model/gltf-binary"
        name.endsWith(".png") -> "image/png"
        else -> "application/octet-stream"
    }

    private fun OutputStream.write(bytes: ByteArray) = write(bytes, 0, bytes.size)

    private companion object {
        const val PROBE = """
            (function() {
                var wrapperActive = (window.__wta_private_network_api_bridge__ === true)
                    && (String(window.XMLHttpRequest).indexOf('BridgedXHR') >= 0);
                var err = document.getElementById('err');
                var text = err ? String(err.textContent || '') : '';
                if (text) return 'err:' + text.replace(/\s+/g, ' ').slice(0, 200);
                var d = window.__3;
                if (!d || !d.model) return '';
                var meshes = 0, tris = 0;
                d.model.traverse(function(o) {
                    if (o.isMesh) {
                        meshes++;
                        var g = o.geometry;
                        if (g && g.index) tris += g.index.count / 3;
                        else if (g && g.attributes && g.attributes.position) tris += g.attributes.position.count / 3;
                    }
                });
                return 'ok wrapper=' + (wrapperActive ? 'BridgedXHR' : 'NATIVE')
                    + ' meshes=' + meshes + ' tris=' + Math.round(tris);
            })();
        """
    }
}
