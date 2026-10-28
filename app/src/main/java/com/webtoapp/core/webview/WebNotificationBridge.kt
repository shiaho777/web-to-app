package com.webtoapp.core.webview

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.net.Uri
import android.os.Build
import android.webkit.WebResourceResponse
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.logging.AppLogger
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

/**
 * System WebView has no Notifications implementation: `window.Notification` is
 * missing and `ServiceWorkerRegistration.showNotification` is missing inside the
 * worker. Sites such as 秋秋机 post a message to their service worker, and the
 * worker calls `showNotification`. Replacing `new Notification()` on the page
 * never sees that call.
 *
 * The page polyfill rewrites `navigator.serviceWorker.register` onto
 * [BRIDGE_PATH]. This object serves a wrapper script for that URL. The wrapper
 * loads the real worker and replaces `showNotification` so the call comes back
 * to [post], which is the same shade entry as [NativeBridge.showWebNotification].
 */
internal object WebNotificationBridge {
    const val BRIDGE_PATH = "/__wta_sw_bridge__"
    const val NOTIFY_PATH = "/__wta_notify__"

    /** Bumped when the wrapper script changes, so an installed worker updates. */
    const val WRAPPER_VERSION = "1"

    private const val CHANNEL_ID = "webapp_notifications"

    @Volatile
    private var routingInstalled = false

    @Volatile
    private var enabled = false

    @Volatile
    private var appContext: Context? = null

    fun install(context: Context) {
        appContext = context.applicationContext
        enabled = true
        if (routingInstalled) return
        synchronized(this) {
            if (routingInstalled) return
            routingInstalled = true
            ServiceWorkerRequestRouter.addHandler { request ->
                if (!enabled) return@addHandler null
                val ctx = appContext ?: return@addHandler null
                val url = request.url?.toString() ?: return@addHandler null
                intercept(ctx, url)
            }
        }
    }

    fun intercept(context: Context, rawUrl: String): WebResourceResponse? {
        val uri = runCatching { Uri.parse(rawUrl) }.getOrNull() ?: return null
        return when (uri.path) {
            BRIDGE_PATH -> bridgeResponse(uri)
            NOTIFY_PATH -> {
                notifyRequest(uri)?.let { request ->
                    post(context, request.title, request.body, request.tag)
                }
                emptyResponse()
            }
            else -> null
        }
    }

    fun bridgeScript(src: String): String {
        val quoted = jsString(src)
        val notifyPath = jsString(NOTIFY_PATH)
        return """
            importScripts($quoted);
            self.addEventListener('install', function () { self.skipWaiting(); });
            self.addEventListener('activate', function (event) {
              event.waitUntil(self.clients.claim());
            });
            ServiceWorkerRegistration.prototype.showNotification = function (title, options) {
              var opts = options || {};
              var payload = {
                __wtaShowNotification: 1,
                title: String(title || ''),
                body: String(opts.body || ''),
                tag: String(opts.tag || '')
              };
              return self.clients.matchAll({ type: 'window', includeUncontrolled: true }).then(function (clients) {
                var i;
                if (clients && clients.length) {
                  for (i = 0; i < clients.length; i++) {
                    try { clients[i].postMessage(payload); } catch (e) {}
                  }
                  return;
                }
                var q = $notifyPath + '?title=' + encodeURIComponent(payload.title)
                  + '&body=' + encodeURIComponent(payload.body)
                  + '&tag=' + encodeURIComponent(payload.tag);
                return fetch(q, { cache: 'no-store' });
              });
            };
        """.trimIndent()
    }

    /**
     * Posts one web notification. A channel created earlier at a lower importance
     * stays at that importance; Android does not apply a later create call.
     */
    fun post(context: Context, title: String, body: String, tag: String): Boolean {
        return try {
            if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    Strings.webAppNotificationChannelName,
                    NotificationManager.IMPORTANCE_HIGH
                )
                manager.createNotificationChannel(channel)
            }
            val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                ?.apply { addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP) }
            val pendingIntent = PendingIntent.getActivity(
                context,
                (tag.ifBlank { title }).hashCode(),
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(context.applicationInfo.icon)
                .setContentTitle(title.ifBlank { context.applicationInfo.loadLabel(context.packageManager).toString() })
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .build()
            NotificationManagerCompat.from(context).notify(
                (tag.ifBlank { "$title|$body" }).hashCode(),
                notification
            )
            true
        } catch (e: SecurityException) {
            AppLogger.e("WebNotificationBridge", "Notification permission unavailable", e)
            false
        } catch (e: Exception) {
            AppLogger.e("WebNotificationBridge", "Failed to show web notification", e)
            false
        }
    }

    private fun bridgeResponse(uri: Uri): WebResourceResponse? {
        val src = uri.getQueryParameter("src") ?: return null
        val srcUri = runCatching { Uri.parse(src) }.getOrNull() ?: return null
        if (!srcUri.scheme.equals("https", true) && !srcUri.scheme.equals("http", true)) return null
        if (!sameOrigin(uri, srcUri)) return null
        val bytes = bridgeScript(src).toByteArray(StandardCharsets.UTF_8)
        return WebResourceResponse(
            "application/javascript",
            "UTF-8",
            200,
            "OK",
            mapOf(
                "Cache-Control" to "no-store",
                "Service-Worker-Allowed" to "/"
            ),
            ByteArrayInputStream(bytes)
        )
    }

    private fun notifyRequest(uri: Uri): NotifyPayload? {
        val title = uri.getQueryParameter("title") ?: return null
        val body = uri.getQueryParameter("body").orEmpty()
        val tag = uri.getQueryParameter("tag").orEmpty()
        return NotifyPayload(title, body, tag)
    }

    private fun emptyResponse(): WebResourceResponse {
        return WebResourceResponse(
            "text/plain",
            "UTF-8",
            200,
            "OK",
            mapOf("Cache-Control" to "no-store"),
            ByteArrayInputStream(ByteArray(0))
        )
    }

    private fun sameOrigin(page: Uri, script: Uri): Boolean {
        return page.scheme.equals(script.scheme, true) &&
            page.host.equals(script.host, true) &&
            effectivePort(page) == effectivePort(script)
    }

    private fun effectivePort(uri: Uri): Int {
        if (uri.port != -1) return uri.port
        return if (uri.scheme.equals("https", true)) 443 else 80
    }

    private fun jsString(value: String): String {
        val out = StringBuilder(value.length + 2)
        out.append('"')
        for (ch in value) {
            when (ch) {
                '\\', '"', '\'' -> out.append('\\').append(ch)
                '\n' -> out.append("\\n")
                '\r' -> out.append("\\r")
                '<' -> out.append("\\u003c")
                else -> if (ch.code < 0x20) {
                    out.append("\\u").append(ch.code.toString(16).padStart(4, '0'))
                } else {
                    out.append(ch)
                }
            }
        }
        out.append('"')
        return out.toString()
    }

    private data class NotifyPayload(val title: String, val body: String, val tag: String)
}
