package com.webtoapp.core.webview

import android.graphics.Bitmap
import android.net.Uri
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.webtoapp.core.adblock.AdBlocker
import com.webtoapp.data.model.WebViewConfig
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

/**
 * Regression for #1023: pages served from LAN/loopback hosts take the
 * `minimizeLocalRuntimeInjection` path, which used to skip the whole
 * compatibility-script batch — including ORIENTATION_POLYFILL_JS — so
 * `screen.orientation.lock()` silently fell back to the stock WebView API
 * (which rejects unless fullscreen) even though the toggle was on.
 */
@RunWith(RobolectricTestRunner::class)
class WebViewManagerOrientationPolyfillTest {

    private val context: android.content.Context = ApplicationProvider.getApplicationContext()

    private val noopCallbacks = object : WebViewCallbacks {
        override fun onPageStarted(url: String?) {}
        override fun onPageFinished(url: String?) {}
        override fun onProgressChanged(progress: Int) {}
        override fun onTitleChanged(title: String?) {}
        override fun onIconReceived(icon: Bitmap?) {}
        override fun onError(errorCode: Int, description: String) {}
        override fun onSslError(error: String) {}
        override fun onExternalLink(url: String) {}
        override fun onShowCustomView(view: android.view.View?, callback: WebChromeClient.CustomViewCallback?) {}
        override fun onHideCustomView() {}
        override fun onGeolocationPermission(origin: String?, callback: GeolocationPermissions.Callback?) {}
        override fun onPermissionRequest(request: PermissionRequest?) {}
        override fun onShowFileChooser(
            filePathCallback: ValueCallback<Array<Uri>>?,
            fileChooserParams: WebChromeClient.FileChooserParams?
        ): Boolean = false

        override fun onDownloadStart(
            url: String,
            userAgent: String,
            contentDisposition: String,
            mimeType: String,
            contentLength: Long
        ) {}
    }

    private fun lastEvaluatedJsOnPageStart(url: String, config: WebViewConfig = WebViewConfig()): String? {
        val webView = WebView(context)
        val manager = WebViewManager(context, AdBlocker())
        manager.configureWebView(webView, config, noopCallbacks, pluginsEnabled = false)
        shadowOf(webView).webViewClient.onPageStarted(webView, url, null)
        return shadowOf(webView).lastEvaluatedJavascript
    }

    @Test
    fun `orientation polyfill is injected on a LAN-hosted page`() {
        val js = lastEvaluatedJsOnPageStart("http://192.168.1.10:8080/app/page")

        assertThat(js).isNotNull()
        assertThat(js).contains("__webtoapp_orientation_polyfill__")
        assertThat(js).contains("bridge.setOrientation('landscape')")
    }

    @Test
    fun `orientation polyfill is injected on a loopback-hosted page`() {
        val js = lastEvaluatedJsOnPageStart("http://127.0.0.1:3000/index.html")

        assertThat(js).isNotNull()
        assertThat(js).contains("__webtoapp_orientation_polyfill__")
    }

    @Test
    fun `orientation polyfill stays off on LAN page when toggle disabled`() {
        val js = lastEvaluatedJsOnPageStart(
            "http://192.168.1.10:8080/app/page",
            WebViewConfig(enableOrientationPolyfill = false)
        )

        // The last eval is the private-network bridge fallback; whatever it is,
        // the orientation shim must not have been installed at all.
        if (js != null) {
            assertThat(js).doesNotContain("__webtoapp_orientation_polyfill__")
        }
    }

    @Test
    fun `orientation polyfill still injected on public https page`() {
        val js = lastEvaluatedJsOnPageStart("https://example.com/app")

        assertThat(js).isNotNull()
        assertThat(js).contains("__webtoapp_orientation_polyfill__")
    }

    @Test
    fun `orientation polyfill still injected on file page`() {
        val js = lastEvaluatedJsOnPageStart("file:///android_asset/app/index.html")

        assertThat(js).isNotNull()
        assertThat(js).contains("__webtoapp_orientation_polyfill__")
    }
}
