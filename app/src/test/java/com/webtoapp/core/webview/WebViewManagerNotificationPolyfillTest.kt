package com.webtoapp.core.webview

import android.content.Context
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
 * Regression for the packaged-app notification gap: HTML / FRONTEND apps are
 * served by LocalHttpServer on 127.0.0.1, which `shouldMinimizeLocalRuntimeInjection`
 * treats as a local-runtime page — and `injectNotificationPolyfill` used to bail on
 * that gate, so `window.Notification` stayed undefined even with the
 * `enableNotificationPolyfill` toggle on (remote-https WEB apps got it; LAN-hosted
 * dev sites on 192.168.x.x / *.local hit the same skip). The polyfill only *adds*
 * APIs backed by NativeBridge and the toggle is opt-in, so it must install on
 * local-runtime pages like every other additive shim (#1023 pattern).
 */
@RunWith(RobolectricTestRunner::class)
class WebViewManagerNotificationPolyfillTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    /** Records every evaluateJavascript call; ShadowWebView only keeps the last. */
    private class RecordingWebView(context: Context) : WebView(context) {
        val scripts = mutableListOf<String>()

        override fun evaluateJavascript(script: String, resultCallback: ValueCallback<String?>?) {
            scripts.add(script)
            super.evaluateJavascript(script, resultCallback)
        }
    }

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

    private fun evaluatedScriptsOnPageStart(
        url: String,
        config: WebViewConfig = WebViewConfig(enableNotificationPolyfill = true)
    ): List<String> {
        val webView = RecordingWebView(context)
        val manager = WebViewManager(context, AdBlocker())
        manager.configureWebView(webView, config, noopCallbacks, pluginsEnabled = false)
        shadowOf(webView).webViewClient.onPageStarted(webView, url, null)
        return webView.scripts
    }

    @Test
    fun `notification polyfill is injected on a loopback-hosted page`() {
        val scripts = evaluatedScriptsOnPageStart("http://127.0.0.1:3000/index.html")

        assertThat(scripts.any { it.contains("__webtoapp_notification_polyfill__") }).isTrue()
    }

    @Test
    fun `notification polyfill is injected on a LAN-hosted page`() {
        val scripts = evaluatedScriptsOnPageStart("http://192.168.1.10:8080/app/page")

        assertThat(scripts.any { it.contains("__webtoapp_notification_polyfill__") }).isTrue()
    }

    @Test
    fun `notification polyfill stays off on local pages when toggle disabled`() {
        val scripts = evaluatedScriptsOnPageStart(
            "http://127.0.0.1:3000/index.html",
            WebViewConfig(enableNotificationPolyfill = false)
        )

        assertThat(scripts.none { it.contains("__webtoapp_notification_polyfill__") }).isTrue()
    }

    @Test
    fun `notification polyfill still injected on public https page`() {
        val scripts = evaluatedScriptsOnPageStart("https://example.com/app")

        assertThat(scripts.any { it.contains("__webtoapp_notification_polyfill__") }).isTrue()
    }

    @Test
    fun `notification polyfill still injected on file page`() {
        val scripts = evaluatedScriptsOnPageStart("file:///android_asset/app/index.html")

        assertThat(scripts.any { it.contains("__webtoapp_notification_polyfill__") }).isTrue()
    }
}
