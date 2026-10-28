package com.webtoapp.core.webview

import android.Manifest
import android.app.Application
import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.nio.charset.StandardCharsets

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class WebNotificationBridgeTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `bridge script loads the same-origin worker and patches showNotification`() {
        val url = "https://qiuqiu-machine.pages.dev/__wta_sw_bridge__?wta=1&src=" +
            "https%3A%2F%2Fqiuqiu-machine.pages.dev%2Fsw.js%3Fv%3D8"
        val response = WebNotificationBridge.intercept(context, url)
        assertThat(response).isNotNull()
        val script = response!!.data.readBytes().toString(StandardCharsets.UTF_8)
        assertThat(script).contains("importScripts(\"https://qiuqiu-machine.pages.dev/sw.js?v=8\")")
        assertThat(script).contains("ServiceWorkerRegistration.prototype.showNotification")
        assertThat(script).contains("__wtaShowNotification")
        assertThat(script).contains("/__wta_notify__")
    }

    @Test
    fun `bridge script rejects a worker from another origin`() {
        val url = "https://qiuqiu-machine.pages.dev/__wta_sw_bridge__?src=" +
            "https%3A%2F%2Fevil.example%2Fsw.js"
        assertThat(WebNotificationBridge.intercept(context, url)).isNull()
    }

    @Test
    fun `notify url posts the system notification the worker could not show`() {
        shadowOf(context as Application).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        val title = java.net.URLEncoder.encode("秋秋机 · 通知测试", "UTF-8")
        val body = java.net.URLEncoder.encode("这是一条测试通知", "UTF-8")
        val url = "https://qiuqiu-machine.pages.dev/__wta_notify__?title=$title&body=$body&tag=notif-test"
        val response = WebNotificationBridge.intercept(context, url)
        assertThat(response).isNotNull()
        val manager = context.getSystemService(NotificationManager::class.java)
        val posted = shadowOf(manager).allNotifications
        assertThat(posted).isNotEmpty()
        val notification = posted.first()
        assertThat(notification.extras.getString(android.app.Notification.EXTRA_TITLE))
            .isEqualTo("秋秋机 · 通知测试")
        assertThat(notification.extras.getString(android.app.Notification.EXTRA_TEXT))
            .isEqualTo("这是一条测试通知")
    }

    @Test
    fun `page polyfill rewrites service worker registration through the bridge`() {
        val script = WebViewManager.NotificationPolyfillHolder.SCRIPT
        assertThat(script).contains(WebNotificationBridge.BRIDGE_PATH)
        assertThat(script).contains(WebNotificationBridge.WRAPPER_VERSION)
        assertThat(script).contains("__wtaShowNotification")
        assertThat(script).contains("navigator.serviceWorker.register")
        assertThat(script).contains("window.top !== window")
    }
}
