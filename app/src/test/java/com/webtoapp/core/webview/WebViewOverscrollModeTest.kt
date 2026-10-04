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

/**
 * The edge stretch is a property of the WebView widget (#1193). Pooled views
 * must be reset, so both states are assigned explicitly.
 */
@RunWith(RobolectricTestRunner::class)
class WebViewOverscrollModeTest {

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

    private fun modeFor(config: WebViewConfig): Int {
        val webView = WebView(context)
        WebViewManager(context, AdBlocker()).configureWebView(
            webView,
            config,
            noopCallbacks,
            pluginsEnabled = false
        )
        return webView.overScrollMode
    }

    @Test
    fun `default keeps the platform edge effect`() {
        assertThat(modeFor(WebViewConfig()))
            .isEqualTo(WebView.OVER_SCROLL_IF_CONTENT_SCROLLS)
    }

    @Test
    fun `turning the effect off stops at the edge`() {
        assertThat(modeFor(WebViewConfig(overscrollEffectEnabled = false)))
            .isEqualTo(WebView.OVER_SCROLL_NEVER)
    }

    @Test
    fun `a later configure restores the effect on the same view`() {
        val webView = WebView(context)
        val manager = WebViewManager(context, AdBlocker())
        manager.configureWebView(
            webView,
            WebViewConfig(overscrollEffectEnabled = false),
            noopCallbacks,
            pluginsEnabled = false
        )
        manager.configureWebView(
            webView,
            WebViewConfig(overscrollEffectEnabled = true),
            noopCallbacks,
            pluginsEnabled = false
        )
        assertThat(webView.overScrollMode).isEqualTo(WebView.OVER_SCROLL_IF_CONTENT_SCROLLS)
    }
}
