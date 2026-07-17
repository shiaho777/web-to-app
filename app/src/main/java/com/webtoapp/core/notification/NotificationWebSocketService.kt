package com.webtoapp.core.notification

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.SystemClock
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.webtoapp.core.i18n.Strings
import com.webtoapp.util.SafeNotificationChannels
import com.webtoapp.core.logging.AppLogger
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

class NotificationWebSocketService : Service() {

    companion object {
        private const val TAG = "NotificationWebSocketService"
        private const val CHANNEL_ID = "websocket_notification_channel"
        private const val FOREGROUND_NOTIFICATION_ID = 10003
        private const val PREFS_NAME = "websocket_notification_config"
        private const val ACTION_RESTART = "com.webtoapp.action.RESTART_WEBSOCKET_NOTIFICATION"
        private const val ACTION_STOP = "com.webtoapp.action.STOP_WEBSOCKET_NOTIFICATION"
        private const val RESTART_REQUEST_CODE = 41003
        private const val RESTART_DELAY_MS = 30_000L
        private const val MIN_RECONNECT_MS = 2_000L
        private const val MAX_RECONNECT_MS = 60_000L
        private const val PING_INTERVAL_MS = 30_000L

        private const val EXTRA_WS_URL = "ws_url"
        private const val EXTRA_WS_HEADERS = "ws_headers"
        private const val EXTRA_REGISTER_URL = "register_url"
        private const val EXTRA_REGISTER_HEADERS = "register_headers"
        private const val EXTRA_AUTH_TOKEN = "auth_token"
        private const val EXTRA_CLICK_URL = "click_url"
        private const val EXTRA_APP_NAME = "app_name"
        private const val PREF_DEVICE_ID = "device_id"

        @Volatile
        private var isRunning = false

        fun start(
            context: Context,
            appName: String = "",
            wsUrl: String,
            wsHeaders: String = "",
            registerUrl: String = "",
            registerHeaders: String = "",
            authToken: String = "",
            clickUrl: String = ""
        ) {
            if (isRunning) {
                AppLogger.w(TAG, "WebSocket notification service already running")
                return
            }
            if (wsUrl.isBlank()) {
                AppLogger.w(TAG, "WebSocket URL is blank, skip start")
                return
            }

            val intent = Intent(context, NotificationWebSocketService::class.java).apply {
                putExtra(EXTRA_WS_URL, wsUrl)
                putExtra(EXTRA_WS_HEADERS, wsHeaders)
                putExtra(EXTRA_REGISTER_URL, registerUrl)
                putExtra(EXTRA_REGISTER_HEADERS, registerHeaders)
                putExtra(EXTRA_AUTH_TOKEN, authToken)
                putExtra(EXTRA_CLICK_URL, clickUrl)
                putExtra(EXTRA_APP_NAME, appName)
            }

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
                AppLogger.i(TAG, "WebSocket notification service start requested")
            } catch (e: Exception) {
                AppLogger.e(TAG, "Failed to start WebSocket notification service", e)
            }
        }

        fun stop(context: Context) {
            try {
                clearPersistedConfig(context)
                cancelRestart(context)
                context.stopService(Intent(context, NotificationWebSocketService::class.java))
                AppLogger.i(TAG, "WebSocket notification service stop requested")
            } catch (e: Exception) {
                AppLogger.e(TAG, "Failed to stop WebSocket notification service", e)
            }
        }

        fun isServiceRunning(): Boolean = isRunning

        fun restoreIfNeeded(context: Context) {
            if (isRunning) return
            val intent = restoreIntent(context) ?: return
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
                AppLogger.i(TAG, "WebSocket notification service restored from persistence")
            } catch (e: Exception) {
                AppLogger.e(TAG, "Failed to restore WebSocket notification service", e)
            }
        }

        private fun clearPersistedConfig(context: Context) {
            val deviceId = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(PREF_DEVICE_ID, null)
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply()
            if (!deviceId.isNullOrBlank()) {
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
                    .putString(PREF_DEVICE_ID, deviceId)
                    .apply()
            }
        }

        private fun persistConfig(
            context: Context,
            appName: String,
            wsUrl: String,
            wsHeaders: String,
            registerUrl: String,
            registerHeaders: String,
            authToken: String,
            clickUrl: String
        ) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
                .putString(EXTRA_APP_NAME, appName)
                .putString(EXTRA_WS_URL, wsUrl)
                .putString(EXTRA_WS_HEADERS, wsHeaders)
                .putString(EXTRA_REGISTER_URL, registerUrl)
                .putString(EXTRA_REGISTER_HEADERS, registerHeaders)
                .putString(EXTRA_AUTH_TOKEN, authToken)
                .putString(EXTRA_CLICK_URL, clickUrl)
                .apply()
        }

        private fun restoreIntent(context: Context): Intent? {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val wsUrl = prefs.getString(EXTRA_WS_URL, "") ?: ""
            if (wsUrl.isBlank()) return null
            return Intent(context, NotificationWebSocketService::class.java).apply {
                putExtra(EXTRA_APP_NAME, prefs.getString(EXTRA_APP_NAME, "") ?: "")
                putExtra(EXTRA_WS_URL, wsUrl)
                putExtra(EXTRA_WS_HEADERS, prefs.getString(EXTRA_WS_HEADERS, "") ?: "")
                putExtra(EXTRA_REGISTER_URL, prefs.getString(EXTRA_REGISTER_URL, "") ?: "")
                putExtra(EXTRA_REGISTER_HEADERS, prefs.getString(EXTRA_REGISTER_HEADERS, "") ?: "")
                putExtra(EXTRA_AUTH_TOKEN, prefs.getString(EXTRA_AUTH_TOKEN, "") ?: "")
                putExtra(EXTRA_CLICK_URL, prefs.getString(EXTRA_CLICK_URL, "") ?: "")
            }
        }

        fun scheduleRestart(context: Context) {
            try {
                val intent = Intent(context, NotificationWebSocketService::class.java).apply {
                    action = ACTION_RESTART
                }
                val pending = PendingIntent.getService(
                    context,
                    RESTART_REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val triggerAt = SystemClock.elapsedRealtime() + RESTART_DELAY_MS
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, pending)
                } else {
                    alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, pending)
                }
            } catch (e: Exception) {
                AppLogger.e(TAG, "Failed to schedule WebSocket restart", e)
            }
        }

        private fun cancelRestart(context: Context) {
            try {
                val intent = Intent(context, NotificationWebSocketService::class.java).apply {
                    action = ACTION_RESTART
                }
                val pending = PendingIntent.getService(
                    context,
                    RESTART_REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                alarmManager.cancel(pending)
            } catch (_: Exception) {
            }
        }

        fun getOrCreateDeviceId(context: Context): String {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val existing = prefs.getString(PREF_DEVICE_ID, null)
            if (!existing.isNullOrBlank()) return existing
            val androidId = try {
                Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            } catch (_: Exception) {
                null
            }
            val deviceId = if (!androidId.isNullOrBlank()) {
                "android_$androidId"
            } else {
                "android_${UUID.randomUUID()}"
            }
            prefs.edit().putString(PREF_DEVICE_ID, deviceId).apply()
            return deviceId
        }
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private var wakeLock: PowerManager.WakeLock? = null
    private var webSocket: WebSocket? = null
    private var okHttpClient: OkHttpClient? = null
    private val connecting = AtomicBoolean(false)
    private val shouldReconnect = AtomicBoolean(true)
    private val reconnectAttempt = AtomicInteger(0)
    private val notificationSeq = AtomicInteger(0)
    private val lastMessageAt = AtomicLong(0L)

    private var wsUrl: String = ""
    private var wsHeaders: String = ""
    private var registerUrl: String = ""
    private var registerHeaders: String = ""
    private var authToken: String = ""
    private var clickUrl: String = ""
    private var appName: String = ""
    private var deviceId: String = ""

    private val reconnectRunnable = Runnable { connectWebSocket(force = true) }
    private val pingRunnable = object : Runnable {
        override fun run() {
            try {
                val socket = webSocket
                if (socket != null) {
                    socket.send("""{"type":"ping","ts":${System.currentTimeMillis()}}""")
                }
            } catch (_: Exception) {
            }
            if (isRunning) {
                mainHandler.postDelayed(this, PING_INTERVAL_MS)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        deviceId = getOrCreateDeviceId(this)
        okHttpClient = OkHttpClient.Builder()
            .pingInterval(20, TimeUnit.SECONDS)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            shouldReconnect.set(false)
            stopSelfGracefully()
            return START_NOT_STICKY
        }

        if (intent?.action == ACTION_RESTART) {
            val restored = restoreIntent(this)
            if (restored != null) {
                return onStartCommand(restored, flags, startId)
            }
            stopSelf()
            return START_NOT_STICKY
        }

        wsUrl = intent?.getStringExtra(EXTRA_WS_URL).orEmpty().ifBlank {
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString(EXTRA_WS_URL, "") ?: ""
        }
        wsHeaders = intent?.getStringExtra(EXTRA_WS_HEADERS).orEmpty().ifBlank {
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString(EXTRA_WS_HEADERS, "") ?: ""
        }
        registerUrl = intent?.getStringExtra(EXTRA_REGISTER_URL).orEmpty().ifBlank {
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString(EXTRA_REGISTER_URL, "") ?: ""
        }
        registerHeaders = intent?.getStringExtra(EXTRA_REGISTER_HEADERS).orEmpty().ifBlank {
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString(EXTRA_REGISTER_HEADERS, "") ?: ""
        }
        authToken = intent?.getStringExtra(EXTRA_AUTH_TOKEN).orEmpty().ifBlank {
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString(EXTRA_AUTH_TOKEN, "") ?: ""
        }
        clickUrl = intent?.getStringExtra(EXTRA_CLICK_URL).orEmpty().ifBlank {
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString(EXTRA_CLICK_URL, "") ?: ""
        }
        appName = intent?.getStringExtra(EXTRA_APP_NAME).orEmpty().ifBlank {
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString(EXTRA_APP_NAME, "") ?: ""
        }

        if (wsUrl.isBlank()) {
            AppLogger.w(TAG, "Missing WebSocket URL, stopping service")
            stopSelf()
            return START_NOT_STICKY
        }

        persistConfig(this, appName, wsUrl, wsHeaders, registerUrl, registerHeaders, authToken, clickUrl)
        shouldReconnect.set(true)
        isRunning = true

        try {
            val notification = createForegroundNotification()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    FOREGROUND_NOTIFICATION_ID,
                    notification,
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(FOREGROUND_NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "startForeground failed", e)
            stopSelf()
            return START_NOT_STICKY
        }

        Thread {
            try {
                maybeRegisterDevice()
            } catch (e: Exception) {
                AppLogger.e(TAG, "Device registration failed", e)
            }
            mainHandler.post { connectWebSocket(force = true) }
        }.start()

        mainHandler.removeCallbacks(pingRunnable)
        mainHandler.postDelayed(pingRunnable, PING_INTERVAL_MS)

        AppLogger.i(TAG, "WebSocket notification service started: url=$wsUrl")
        return START_STICKY
    }

    override fun onDestroy() {
        isRunning = false
        connecting.set(false)
        mainHandler.removeCallbacks(reconnectRunnable)
        mainHandler.removeCallbacks(pingRunnable)
        closeWebSocket()
        releaseWakeLock()
        if (shouldReconnect.get() && wsUrl.isNotBlank()) {
            scheduleRestart(this)
        }
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        if (shouldReconnect.get() && wsUrl.isNotBlank()) {
            scheduleRestart(this)
        }
    }

    private fun stopSelfGracefully() {
        isRunning = false
        connecting.set(false)
        mainHandler.removeCallbacks(reconnectRunnable)
        mainHandler.removeCallbacks(pingRunnable)
        closeWebSocket()
        releaseWakeLock()
        stopSelf()
    }

    private fun connectWebSocket(force: Boolean) {
        if (!shouldReconnect.get()) return
        if (wsUrl.isBlank()) return
        if (!force && webSocket != null) return
        if (!connecting.compareAndSet(false, true)) return

        closeWebSocket()
        acquireWakeLock()

        try {
            val builder = Request.Builder().url(normalizeWsUrl(wsUrl))
            parseHeaders(wsHeaders).forEach { (k, v) -> builder.header(k, v) }
            if (authToken.isNotBlank() && !parseHeaders(wsHeaders).keys.any { it.equals("Authorization", true) }) {
                builder.header("Authorization", "Bearer $authToken")
            }
            builder.header("X-Device-Id", deviceId)
            if (appName.isNotBlank()) {
                builder.header("X-App-Name", appName)
            }

            val client = okHttpClient ?: return
            webSocket = client.newWebSocket(builder.build(), object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    connecting.set(false)
                    reconnectAttempt.set(0)
                    lastMessageAt.set(System.currentTimeMillis())
                    AppLogger.i(TAG, "WebSocket connected")
                    try {
                        val hello = JSONObject()
                            .put("type", "hello")
                            .put("deviceId", deviceId)
                            .put("platform", "android")
                            .put("appName", appName)
                            .put("ts", System.currentTimeMillis())
                        if (authToken.isNotBlank()) hello.put("token", authToken)
                        webSocket.send(hello.toString())
                    } catch (e: Exception) {
                        AppLogger.e(TAG, "Failed to send hello", e)
                    }
                    updateForegroundNotification(connected = true)
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    lastMessageAt.set(System.currentTimeMillis())
                    handleIncomingMessage(text)
                }

                override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                    lastMessageAt.set(System.currentTimeMillis())
                    handleIncomingMessage(bytes.utf8())
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    webSocket.close(1000, null)
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    connecting.set(false)
                    AppLogger.w(TAG, "WebSocket closed: $code $reason")
                    updateForegroundNotification(connected = false)
                    scheduleReconnect()
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    connecting.set(false)
                    AppLogger.e(TAG, "WebSocket failure", t)
                    updateForegroundNotification(connected = false)
                    scheduleReconnect()
                }
            })
        } catch (e: Exception) {
            connecting.set(false)
            AppLogger.e(TAG, "Failed to connect WebSocket", e)
            scheduleReconnect()
        } finally {
            releaseWakeLock()
        }
    }

    private fun scheduleReconnect() {
        if (!shouldReconnect.get() || !isRunning) return
        val attempt = reconnectAttempt.incrementAndGet()
        val delay = (MIN_RECONNECT_MS * (1L shl (attempt - 1).coerceAtMost(5)))
            .coerceAtMost(MAX_RECONNECT_MS)
        mainHandler.removeCallbacks(reconnectRunnable)
        mainHandler.postDelayed(reconnectRunnable, delay)
        AppLogger.i(TAG, "Reconnect scheduled in ${delay}ms (attempt=$attempt)")
    }

    private fun closeWebSocket() {
        try {
            webSocket?.close(1000, "shutdown")
        } catch (_: Exception) {
        }
        try {
            webSocket?.cancel()
        } catch (_: Exception) {
        }
        webSocket = null
    }

    private fun maybeRegisterDevice() {
        if (registerUrl.isBlank()) return
        val client = okHttpClient ?: return
        val payload = JSONObject()
            .put("deviceId", deviceId)
            .put("platform", "android")
            .put("appName", appName)
            .put("ts", System.currentTimeMillis())
        if (authToken.isNotBlank()) payload.put("token", authToken)

        val body = payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val builder = Request.Builder().url(registerUrl).post(body)
        parseHeaders(registerHeaders).forEach { (k, v) -> builder.header(k, v) }
        if (authToken.isNotBlank() && !parseHeaders(registerHeaders).keys.any { it.equals("Authorization", true) }) {
            builder.header("Authorization", "Bearer $authToken")
        }
        builder.header("X-Device-Id", deviceId)

        client.newCall(builder.build()).execute().use { response ->
            AppLogger.i(TAG, "Register response: ${response.code}")
        }
    }

    private fun handleIncomingMessage(raw: String) {
        val text = raw.trim()
        if (text.isBlank()) return
        try {
            when {
                text.startsWith("{") -> {
                    val obj = JSONObject(text)
                    val type = obj.optString("type", "").lowercase()
                    if (type == "ping" || type == "pong" || type == "ack" || type == "hello") return
                    if (type == "notifications" || obj.has("notifications")) {
                        val arr = obj.optJSONArray("notifications") ?: return
                        handleNotificationArray(arr)
                        return
                    }
                    showPushNotification(parseNotification(obj))
                }
                text.startsWith("[") -> {
                    handleNotificationArray(JSONArray(text))
                }
                else -> {
                    showPushNotification(
                        PushNotification(
                            title = appName.ifBlank { Strings.genericNotificationLabel },
                            body = text,
                            url = clickUrl
                        )
                    )
                }
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to handle WebSocket message", e)
        }
    }

    private fun handleNotificationArray(arr: JSONArray) {
        for (i in 0 until arr.length()) {
            val item = arr.optJSONObject(i) ?: continue
            showPushNotification(parseNotification(item))
        }
    }

    private fun parseNotification(obj: JSONObject): PushNotification {
        val title = obj.optString("title", "")
            .ifBlank { obj.optString("subject", "") }
            .ifBlank { appName.ifBlank { Strings.genericNotificationLabel } }
        val body = obj.optString("body", "")
            .ifBlank { obj.optString("message", "") }
            .ifBlank { obj.optString("content", "") }
        val url = obj.optString("url", "")
            .ifBlank { obj.optString("clickUrl", "") }
            .ifBlank { obj.optString("link", "") }
            .ifBlank { clickUrl }
        return PushNotification(title = title, body = body, url = url)
    }

    private fun showPushNotification(notif: PushNotification) {
        try {
            val notificationManager = NotificationManagerCompat.from(this)
            if (!notificationManager.areNotificationsEnabled()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) return

            val baseId = notificationSeq.incrementAndGet()
            val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                if (notif.url.isNotBlank()) {
                    putExtra("notification_click_url", notif.url)
                }
            }

            val pendingIntent = if (intent != null) {
                PendingIntent.getActivity(
                    this,
                    baseId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            } else null

            val iconResId = applicationInfo.icon.takeIf { it != 0 } ?: android.R.drawable.ic_menu_info_details
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(iconResId)
                .setContentTitle(notif.title)
                .setContentText(notif.body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(notif.body))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .apply {
                    if (pendingIntent != null) setContentIntent(pendingIntent)
                }
                .build()

            try {
                notificationManager.notify(
                    Math.abs(("ws_${System.currentTimeMillis()}_$baseId").hashCode()),
                    notification
                )
            } catch (e: SecurityException) {
                AppLogger.e(TAG, "Notification permission unavailable", e)
                return
            }
            AppLogger.d(TAG, "Showed WebSocket notification: ${notif.title}")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to show WebSocket notification", e)
        }
    }

    private fun createNotificationChannel() {
        SafeNotificationChannels.ensure(
            context = this,
            id = CHANNEL_ID,
            name = Strings.websocketNotificationChannelName.ifBlank { "Notifications" },
            importance = NotificationManager.IMPORTANCE_DEFAULT,
            description = Strings.websocketNotificationChannelDescription
        ) {
            setShowBadge(true)
        }
    }

    private fun createForegroundNotification(connected: Boolean = false): Notification {
        val stopIntent = Intent(this, NotificationWebSocketService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val iconResId = applicationInfo.icon.takeIf { it != 0 } ?: android.R.drawable.ic_menu_info_details
        val title = (if (appName.isNotBlank()) appName else Strings.genericAppLabel) +
            " - " + Strings.websocketNotificationServiceTitle
        val text = if (connected) {
            Strings.websocketNotificationForegroundConnected.format(wsUrl)
        } else {
            Strings.websocketNotificationForegroundText.format(wsUrl)
        }
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(iconResId)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
            .setShowWhen(false)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                Strings.stop,
                stopPendingIntent
            )
            .build()
    }

    private fun updateForegroundNotification(connected: Boolean) {
        try {
            val manager = getSystemService(NotificationManager::class.java)
            manager.notify(FOREGROUND_NOTIFICATION_ID, createForegroundNotification(connected))
        } catch (_: Exception) {
        }
    }

    private fun normalizeWsUrl(url: String): String {
        val trimmed = url.trim()
        return when {
            trimmed.startsWith("ws://") || trimmed.startsWith("wss://") -> trimmed
            trimmed.startsWith("https://") -> "wss://" + trimmed.removePrefix("https://")
            trimmed.startsWith("http://") -> "ws://" + trimmed.removePrefix("http://")
            else -> trimmed
        }
    }

    private fun parseHeaders(raw: String): Map<String, String> {
        if (raw.isBlank()) return emptyMap()
        val result = linkedMapOf<String, String>()
        raw.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .forEach { line ->
                val idx = line.indexOf(':')
                if (idx > 0) {
                    val key = line.substring(0, idx).trim()
                    val value = line.substring(idx + 1).trim()
                    if (key.isNotEmpty()) result[key] = value
                }
            }
        return result
    }

    private fun acquireWakeLock() {
        synchronized(this) {
            if (wakeLock == null) {
                val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
                wakeLock = powerManager.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    "WebToApp:WebSocketWakeLock"
                ).apply { setReferenceCounted(false) }
            }
            wakeLock?.let {
                if (!it.isHeld) {
                    it.acquire(15_000L)
                }
            }
        }
    }

    private fun releaseWakeLock() {
        synchronized(this) {
            wakeLock?.let {
                if (it.isHeld) {
                    try {
                        it.release()
                    } catch (_: Exception) {
                    }
                }
            }
        }
    }

    private data class PushNotification(
        val title: String,
        val body: String,
        val url: String
    )
}
