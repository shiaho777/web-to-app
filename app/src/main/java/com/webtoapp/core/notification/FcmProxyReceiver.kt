package com.webtoapp.core.notification

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import com.webtoapp.core.logging.AppLogger

/**
 * Receives `com.google.android.c2dm.intent.RECEIVE` pushes from Google Play
 * services — the same contract Firebase's own `FirebaseInstanceIdReceiver`
 * implements (that receiver ships inside the FCM feature dex and cannot be
 * declared as a manifest component).
 *
 * GMS wraps the real service intent in the `wrapped_intent` extra; we unwrap it
 * (or fall back to the broadcast's own extras) and forward to
 * [NotificationFcmService], which hands off to the feature-stack implementation.
 */
class FcmProxyReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "FcmProxyReceiver"
        private const val EXTRA_WRAPPED_INTENT = "wrapped_intent"
    }

    override fun onReceive(context: Context, intent: Intent) {
        try {
            val wrapped = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(EXTRA_WRAPPED_INTENT, Intent::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra<Intent>(EXTRA_WRAPPED_INTENT)
            }
            val forward = (wrapped?.let { Intent(it) } ?: Intent()).apply {
                component = ComponentName(context, NotificationFcmService::class.java)
                if (wrapped == null && intent.extras != null) {
                    putExtras(intent.extras!!)
                }
                // Token refresh arrives as NEW_TOKEN in the wrapped action —
                // keep it so the impl can tell token events from messages.
                if (action == null) {
                    action = "com.google.firebase.MESSAGING_EVENT"
                }
            }
            context.startService(forward)
            if (isOrderedBroadcast) {
                setResultCode(Activity.RESULT_OK)
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to forward FCM broadcast", e)
        }
    }
}
