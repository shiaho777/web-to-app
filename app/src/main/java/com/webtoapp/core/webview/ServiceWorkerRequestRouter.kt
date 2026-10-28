package com.webtoapp.core.webview

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import androidx.webkit.ServiceWorkerClientCompat
import androidx.webkit.ServiceWorkerControllerCompat
import com.webtoapp.core.logging.AppLogger

/**
 * One process-wide service-worker intercept. Android allows a single
 * [ServiceWorkerClientCompat]; the offline-cache worker and the web-notification
 * bridge both need it, and whichever installed last used to replace the other.
 */
internal object ServiceWorkerRequestRouter {
    private val handlers = java.util.concurrent.CopyOnWriteArrayList<
        (WebResourceRequest) -> WebResourceResponse?
        >()

    @Volatile
    private var installed = false

    fun addHandler(handler: (WebResourceRequest) -> WebResourceResponse?) {
        handlers.add(handler)
        ensureInstalled()
    }

    fun ensureInstalled() {
        if (installed) return
        synchronized(this) {
            if (installed) return
            try {
                ServiceWorkerControllerCompat.getInstance().setServiceWorkerClient(
                    object : ServiceWorkerClientCompat() {
                        override fun shouldInterceptRequest(
                            request: WebResourceRequest
                        ): WebResourceResponse? {
                            for (handler in handlers) {
                                val response = try {
                                    handler(request)
                                } catch (e: Exception) {
                                    AppLogger.w(
                                        "ServiceWorkerRequestRouter",
                                        "Service worker intercept failed for ${request.url}",
                                        e
                                    )
                                    null
                                }
                                if (response != null) return response
                            }
                            return null
                        }
                    }
                )
                installed = true
            } catch (t: Throwable) {
                // Robolectric has no service-worker controller, and a failed class
                // init throws ExceptionInInitializerError. The page WebViewClient
                // still serves the bridge. Don't retry a broken controller.
                installed = true
                AppLogger.w(
                    "ServiceWorkerRequestRouter",
                    "Service worker intercept install failed",
                    t
                )
            }
        }
    }
}
