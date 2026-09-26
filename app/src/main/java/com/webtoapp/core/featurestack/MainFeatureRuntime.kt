package com.webtoapp.core.featurestack

import android.content.Context
import com.webtoapp.core.download.DependencyDownloadEngine
import com.webtoapp.core.featurestack.api.FeatureRuntime
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.core.notification.NotificationFcmManager
import com.webtoapp.core.notification.PushNotificationHelper
import kotlinx.coroutines.runBlocking
import java.io.File

/**
 * Main-runtime implementation of the feature-stack runtime services. Dex-loaded
 * stack code calls through this object instead of touching renamed host classes.
 */
object MainFeatureRuntime : FeatureRuntime {

    override fun log(level: Int, tag: String, message: String, throwable: Throwable?) {
        when (level) {
            FeatureRuntime.LOG_DEBUG -> AppLogger.d(tag, message, throwable)
            FeatureRuntime.LOG_INFO -> AppLogger.i(tag, message, throwable)
            FeatureRuntime.LOG_WARN -> AppLogger.w(tag, message, throwable)
            FeatureRuntime.LOG_ERROR -> AppLogger.e(tag, message, throwable)
            else -> AppLogger.i(tag, message, throwable)
        }
    }

    override fun localizedString(key: String): String = when (key) {
        KEY_FCM_CHANNEL_NAME -> Strings.fcmNotificationChannelName
        KEY_FCM_CHANNEL_DESC -> Strings.fcmNotificationChannelDescription
        KEY_GENERIC_NOTIFICATION_LABEL -> Strings.genericNotificationLabel
        else -> key
    }

    override fun ensureNotificationChannel(
        context: Context,
        channelId: String,
        name: String,
        description: String
    ) {
        PushNotificationHelper.ensureChannel(context, channelId, name, description)
    }

    override fun showNotification(
        context: Context,
        channelId: String,
        title: String,
        body: String,
        clickUrl: String?
    ) {
        PushNotificationHelper.show(
            context = context,
            channelId = channelId,
            title = title,
            body = body,
            clickUrl = clickUrl ?: ""
        )
    }

    override fun fcmAppName(context: Context): String =
        NotificationFcmManager.getAppName(context)

    override fun fcmClickUrl(context: Context): String? =
        NotificationFcmManager.getClickUrl(context)

    override fun downloadFile(
        context: Context,
        urls: List<String>,
        destPath: String,
        sha256Hex: String?,
        displayName: String,
        maxRetryPerUrl: Int,
        retryDelayMs: Long
    ): Boolean = runBlocking {
        DependencyDownloadEngine.downloadFileWithFallback(
            urls = urls,
            destFile = File(destPath),
            displayName = displayName,
            context = context,
            maxRetryPerUrl = maxRetryPerUrl,
            retryDelayMs = retryDelayMs,
            expectedSha256For = { _ -> sha256Hex }
        )
    }

    private const val KEY_FCM_CHANNEL_NAME = "fcm_channel_name"
    private const val KEY_FCM_CHANNEL_DESC = "fcm_channel_description"
    private const val KEY_GENERIC_NOTIFICATION_LABEL = "generic_notification_label"
}
