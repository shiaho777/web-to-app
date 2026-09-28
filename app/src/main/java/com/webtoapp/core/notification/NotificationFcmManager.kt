package com.webtoapp.core.notification

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.webtoapp.core.logging.AppLogger
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object NotificationFcmManager {
    private const val TAG = "NotificationFcmManager"
    private const val PREFS = "fcm_notification_config"
    private const val KEY_PROJECT_ID = "fcm_project_id"
    private const val KEY_APPLICATION_ID = "fcm_application_id"
    private const val KEY_API_KEY = "fcm_api_key"
    private const val KEY_SENDER_ID = "fcm_sender_id"
    private const val KEY_REGISTER_URL = "register_url"
    private const val KEY_REGISTER_HEADERS = "register_headers"
    private const val KEY_AUTH_TOKEN = "auth_token"
    private const val KEY_CLICK_URL = "click_url"
    private const val KEY_APP_NAME = "app_name"
    private const val KEY_TOKEN = "fcm_token"
    private const val KEY_ENABLED = "fcm_enabled"

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    data class FcmConfig(
        val projectId: String = "",
        val applicationId: String = "",
        val apiKey: String = "",
        val senderId: String = "",
        val registerUrl: String = "",
        val registerHeaders: String = "",
        val authToken: String = "",
        val clickUrl: String = "",
        val appName: String = "",
        val googleServicesJson: String = ""
    )

    fun parseGoogleServicesJson(raw: String): FcmConfig? {
        if (raw.isBlank()) return null
        return try {
            val root = JSONObject(raw)
            val projectInfo = root.optJSONObject("project_info")
            val clients = root.optJSONArray("client") ?: JSONArray()
            val client = clients.optJSONObject(0)
            val clientInfo = client?.optJSONObject("client_info")
            val apiKeys = client?.optJSONArray("api_key")
            val apiKey = apiKeys?.optJSONObject(0)?.optString("current_key").orEmpty()
            val projectId = projectInfo?.optString("project_id").orEmpty()
            val senderId = projectInfo?.optString("project_number").orEmpty()
            val applicationId = clientInfo?.optString("mobilesdk_app_id").orEmpty()
            if (projectId.isBlank() || applicationId.isBlank() || apiKey.isBlank() || senderId.isBlank()) {
                null
            } else {
                FcmConfig(
                    projectId = projectId,
                    applicationId = applicationId,
                    apiKey = apiKey,
                    senderId = senderId,
                    googleServicesJson = raw
                )
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to parse google-services.json", e)
            null
        }
    }

    fun resolveConfig(input: FcmConfig): FcmConfig {
        val parsed = parseGoogleServicesJson(input.googleServicesJson)
        if (parsed == null) return input
        return input.copy(
            projectId = input.projectId.ifBlank { parsed.projectId },
            applicationId = input.applicationId.ifBlank { parsed.applicationId },
            apiKey = input.apiKey.ifBlank { parsed.apiKey },
            senderId = input.senderId.ifBlank { parsed.senderId }
        )
    }

    fun isConfigured(config: FcmConfig): Boolean {
        val resolved = resolveConfig(config)
        return resolved.projectId.isNotBlank() &&
            resolved.applicationId.isNotBlank() &&
            resolved.apiKey.isNotBlank() &&
            resolved.senderId.isNotBlank()
    }

    fun persist(context: Context, config: FcmConfig) {
        val resolved = resolveConfig(config)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_ENABLED, true)
            .putString(KEY_PROJECT_ID, resolved.projectId)
            .putString(KEY_APPLICATION_ID, resolved.applicationId)
            .putString(KEY_API_KEY, resolved.apiKey)
            .putString(KEY_SENDER_ID, resolved.senderId)
            .putString(KEY_REGISTER_URL, resolved.registerUrl)
            .putString(KEY_REGISTER_HEADERS, resolved.registerHeaders)
            .putString(KEY_AUTH_TOKEN, resolved.authToken)
            .putString(KEY_CLICK_URL, resolved.clickUrl)
            .putString(KEY_APP_NAME, resolved.appName)
            .apply()
    }

    fun load(context: Context): FcmConfig? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(KEY_ENABLED, false)) return null
        val config = FcmConfig(
            projectId = prefs.getString(KEY_PROJECT_ID, "").orEmpty(),
            applicationId = prefs.getString(KEY_APPLICATION_ID, "").orEmpty(),
            apiKey = prefs.getString(KEY_API_KEY, "").orEmpty(),
            senderId = prefs.getString(KEY_SENDER_ID, "").orEmpty(),
            registerUrl = prefs.getString(KEY_REGISTER_URL, "").orEmpty(),
            registerHeaders = prefs.getString(KEY_REGISTER_HEADERS, "").orEmpty(),
            authToken = prefs.getString(KEY_AUTH_TOKEN, "").orEmpty(),
            clickUrl = prefs.getString(KEY_CLICK_URL, "").orEmpty(),
            appName = prefs.getString(KEY_APP_NAME, "").orEmpty()
        )
        return config.takeIf { isConfigured(it) }
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
    }

    fun start(context: Context, config: FcmConfig) {
        val resolved = resolveConfig(config)
        if (!isConfigured(resolved)) {
            AppLogger.w(TAG, "FCM config incomplete, skip start")
            return
        }
        persist(context, resolved)
        try {
            initializeFirebase(context, resolved)
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { token ->
                    saveToken(context, token)
                    registerToken(context, token)
                    AppLogger.i(TAG, "FCM token ready")
                }
                .addOnFailureListener { e ->
                    AppLogger.e(TAG, "Failed to get FCM token", e)
                }
            AppLogger.i(TAG, "FCM initialized for project=${resolved.projectId}")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to start FCM", e)
        }
    }

    fun restoreIfNeeded(context: Context) {
        val config = load(context) ?: return
        start(context, config)
    }

    fun getClickUrl(context: Context): String {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_CLICK_URL, "")
            .orEmpty()
    }

    fun getAppName(context: Context): String {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_APP_NAME, "")
            .orEmpty()
    }

    fun onNewToken(context: Context, token: String) {
        saveToken(context, token)
        registerToken(context, token)
    }

    private fun saveToken(context: Context, token: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_TOKEN, token)
            .apply()
    }

    private fun initializeFirebase(context: Context, config: FcmConfig) {
        val resolved = resolveConfig(config)
        val options = FirebaseOptions.Builder()
            .setProjectId(resolved.projectId)
            .setApplicationId(resolved.applicationId)
            .setApiKey(resolved.apiKey)
            .setGcmSenderId(resolved.senderId)
            .build()
        val existing = FirebaseApp.getApps(context)
        if (existing.isEmpty()) {
            FirebaseApp.initializeApp(context, options)
        } else {
            val app = FirebaseApp.getInstance()
            val current = app.options
            val same = current.projectId == resolved.projectId &&
                current.applicationId == resolved.applicationId &&
                current.apiKey == resolved.apiKey &&
                current.gcmSenderId == resolved.senderId
            if (!same) {
                app.delete()
                FirebaseApp.initializeApp(context, options)
            }
        }
    }

    private fun registerToken(context: Context, token: String) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val registerUrl = prefs.getString(KEY_REGISTER_URL, "").orEmpty()
        if (registerUrl.isBlank()) return
        val registerHeaders = prefs.getString(KEY_REGISTER_HEADERS, "").orEmpty()
        val authToken = prefs.getString(KEY_AUTH_TOKEN, "").orEmpty()
        val appName = prefs.getString(KEY_APP_NAME, "").orEmpty()
        val deviceId = NotificationWebSocketService.getOrCreateDeviceId(context)
        Thread {
            try {
                val payload = JSONObject()
                    .put("token", token)
                    .put("deviceId", deviceId)
                    .put("platform", "android")
                    .put("provider", "fcm")
                    .put("appName", appName)
                    .put("ts", System.currentTimeMillis())
                if (authToken.isNotBlank()) payload.put("authToken", authToken)
                val body = payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
                val builder = Request.Builder().url(registerUrl).post(body)
                parseHeaders(registerHeaders).forEach { (k, v) -> builder.header(k, v) }
                if (authToken.isNotBlank() && !parseHeaders(registerHeaders).keys.any { it.equals("Authorization", true) }) {
                    builder.header("Authorization", "Bearer $authToken")
                }
                builder.header("X-Device-Id", deviceId)
                httpClient.newCall(builder.build()).execute().use { response ->
                    AppLogger.i(TAG, "FCM token register response: ${response.code}")
                }
            } catch (e: Exception) {
                AppLogger.e(TAG, "Failed to register FCM token", e)
            }
        }.start()
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
}
