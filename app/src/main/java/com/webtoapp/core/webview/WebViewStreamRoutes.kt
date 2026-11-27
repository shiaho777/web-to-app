package com.webtoapp.core.webview

import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import com.webtoapp.core.logging.AppLogger
import kotlinx.coroutines.Dispatchers
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.io.ByteArrayInputStream
import java.io.Closeable
import java.io.FilterInputStream
import java.io.InputStream
import java.util.UUID
import java.util.WeakHashMap
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.EmptyCoroutineContext

internal object WebViewStreamRoutes {
    private const val PATH = "/.webtoapp/stream/"
    private const val VIRTUAL_ORIGIN = "https://webtoapp-stream.invalid"
    private const val MAX_WAITING_STREAMS = 32
    private const val MAX_FILE_URLS = 128
    private const val CLAIM_TIMEOUT_MS = 30_000L
    private val routeId = Regex("[a-f0-9]{8}(-[a-f0-9]{4}){3}-[a-f0-9]{12}")
    private val lock = Any()
    private val pages = WeakHashMap<WebView, Page>()
    private val destroyed = WeakHashMap<WebView, Boolean>()
    private val handler by lazy { Handler(Looper.getMainLooper()) }

    private data class Route(
        val id: String,
        val oneShot: Boolean,
        val key: Any?,
        @Volatile var open: (WebResourceRequest) -> WebResourceResponse?,
        val close: () -> Unit,
        val expiration: Runnable?
    )

    internal class Page internal constructor(val callerUrl: String, val document: String, val origin: String) : Closeable {
        private val lock = Any()
        private val routes = LinkedHashMap<String, Route>()
        private val claiming = mutableMapOf<String, Route>()
        private val fileUrls = mutableMapOf<Any, Route>()
        private val pendingCalls = mutableSetOf<Closeable>()
        private val activeStreams = mutableMapOf<TrackedStream, String>()
        private var closed = false

        fun register(
            oneShot: Boolean,
            key: Any? = null,
            close: () -> Unit = {},
            open: (WebResourceRequest) -> WebResourceResponse?
        ): String? = synchronized(lock) {
            if (closed || (!oneShot && key == null)) return null
            if (!oneShot) fileUrls[key]?.let {
                it.open = open
                return url(it.id)
            }
            val pendingCount = routes.size - fileUrls.size
            if ((oneShot && pendingCount >= MAX_WAITING_STREAMS) ||
                (!oneShot && fileUrls.size >= MAX_FILE_URLS)
            ) return null

            val id = UUID.randomUUID().toString()
            val expiration = if (oneShot) Runnable { releaseRoute(id) } else null
            val route = Route(id, oneShot, key, open, close, expiration)
            routes[id] = route
            if (!oneShot && key != null) fileUrls[key] = route
            if (expiration != null) handler.postDelayed(expiration, CLAIM_TIMEOUT_MS)
            url(id)
        }

        fun watch(cancel: Closeable): Closeable? = synchronized(lock) {
            if (closed) return null
            pendingCalls.add(cancel)
            Closeable { synchronized(lock) { pendingCalls.remove(cancel) } }
        }

        fun release(url: String) {
            val uri = Uri.parse(url)
            val id = uri.encodedPath.orEmpty().removePrefix(PATH)
            if (originOf(url) != origin || !routeId.matches(id)) return
            val (route, streams) = synchronized(lock) {
                removeRoute(id) to activeStreams.filterValues { it == id }.keys.toList()
            }
            closeStreams(streams, listOfNotNull(route))
        }

        private fun releaseRoute(id: String) {
            val route = synchronized(lock) { removeRoute(id) }
            if (route != null) closeStreams(emptyList(), listOf(route))
        }

        private fun removeRoute(id: String): Route? = (routes.remove(id) ?: claiming.remove(id))?.also {
            if (it.key != null) fileUrls.remove(it.key)
        }

        fun open(id: String, request: WebResourceRequest): WebResourceResponse? {
            val route = synchronized(lock) {
                val candidate = routes[id] ?: return null
                if (candidate.oneShot) {
                    if (request.method != "GET") return null
                    routes.remove(id)
                    claiming[id] = candidate
                }
                candidate
            }
            route.expiration?.let { handler.removeCallbacks(it) }
            val response = try {
                route.open(request)
            } catch (e: Exception) {
                if (route.oneShot) releaseRoute(id)
                AppLogger.w("WebViewStreamRoutes", "Could not open a WebView stream", e)
                return status(500, "Stream unavailable")
            }
            val source = response?.data
            if (source == null) {
                if (route.oneShot) releaseRoute(id)
                return response
            }
            val cancel = if (route.oneShot) route.close else null
            val stream = TrackedStream(source, cancel) { synchronized(lock) { activeStreams.remove(it) } }
            val accepted = synchronized(lock) {
                val registered = if (route.oneShot) claiming.remove(id) else routes[id]
                if (closed || registered !== route) false else {
                    activeStreams[stream] = id
                    true
                }
            }
            if (!accepted) {
                closeResource(stream)
                return null
            }
            response.data = stream
            return response
        }

        override fun close() {
            val resources = synchronized(lock) {
                if (closed) return
                closed = true
                val copy = Triple(routes.values + claiming.values, pendingCalls.toList(), activeStreams.keys.toList())
                routes.clear()
                claiming.clear()
                fileUrls.clear()
                pendingCalls.clear()
                activeStreams.clear()
                copy
            }
            resources.second.forEach(::closeResource)
            closeStreams(resources.third, resources.first)
        }

        private fun closeStreams(streams: List<TrackedStream>, routes: List<Route>) {
            if (streams.isEmpty() && routes.isEmpty()) return
            val release = Runnable {
                routes.forEach(::dispose)
                streams.forEach(TrackedStream::abort)
            }
            if (Looper.myLooper() == Looper.getMainLooper()) {
                Dispatchers.IO.dispatch(EmptyCoroutineContext, release)
            } else {
                release.run()
            }
        }

        private fun url(id: String) = origin + PATH + id

        private fun dispose(route: Route) {
            route.expiration?.let { handler.removeCallbacks(it) }
            closeResource(Closeable { route.close() })
        }
    }

    private class TrackedStream(
        input: InputStream,
        private val cancellation: (() -> Unit)?,
        private val onClosed: (TrackedStream) -> Unit
    ) : FilterInputStream(input) {
        private val closed = AtomicBoolean()

        fun abort() {
            cancellation?.let { closeResource(Closeable { it() }) }
            closeResource(this)
        }

        override fun close() {
            if (!closed.compareAndSet(false, true)) return
            try {
                super.close()
            } finally {
                onClosed(this)
            }
        }
    }

    fun onPageStarted(view: WebView, url: String) {
        val previous = synchronized(lock) {
            if (destroyed.containsKey(view)) return
            pages.put(view, newPage(url))
        }
        previous?.close()
    }

    fun destroy(view: WebView) {
        synchronized(lock) {
            destroyed[view] = true
            pages.remove(view)
        }?.close()
    }

    fun page(view: WebView, url: String): Page? = synchronized(lock) {
        if (destroyed.containsKey(view)) return null
        pages.getOrPut(view) { newPage(url) }.takeIf { it.document == documentKey(url) }
    }

    fun current(view: WebView): Page? = synchronized(lock) { pages[view] }

    fun isCurrent(view: WebView, page: Page): Boolean = synchronized(lock) { pages[view] === page }

    fun intercept(view: WebView, request: WebResourceRequest): WebResourceResponse? {
        val url = request.url
        val path = url.encodedPath.orEmpty()
        if (!path.startsWith(PATH)) return null
        val id = path.removePrefix(PATH)
        if (!routeId.matches(id)) return null
        if (request.isForMainFrame || (request.method != "GET" && request.method != "HEAD")) {
            return status(404, "Not Found")
        }
        val page = synchronized(lock) { pages[view] } ?: return status(404, "Not Found")
        val requestOrigin = request.requestHeaders?.entries?.firstOrNull { it.key.equals("Origin", true) }?.value
        val ownerOrigin = originOf(page.document) ?: "null"
        if (requestOrigin != null && requestOrigin != ownerOrigin) return status(403, "Forbidden")
        if (originOf(url.toString()) != page.origin) return status(404, "Not Found")
        return page.open(id, request) ?: status(404, "Not Found")
    }

    fun status(code: Int, reason: String, headers: Map<String, String> = emptyMap()): WebResourceResponse =
        WebResourceResponse("application/octet-stream", null, code, reason, headers, ByteArrayInputStream(ByteArray(0)))

    private fun closeResource(resource: Closeable) {
        try {
            resource.close()
        } catch (e: Exception) {
            AppLogger.w("WebViewStreamRoutes", "Could not close a WebView stream", e)
        }
    }

    private fun newPage(url: String): Page = Page(url, documentKey(url), originOf(url) ?: VIRTUAL_ORIGIN)

    private fun documentKey(page: String): String = originOf(page)
        ?: Uri.parse(page).buildUpon().fragment(null).build().toString()

    private fun originOf(page: String): String? = page.toHttpUrlOrNull()?.newBuilder()
        ?.username("")?.password("")?.encodedPath("/")?.query(null)?.fragment(null)?.build()?.toString()?.removeSuffix("/")
}
