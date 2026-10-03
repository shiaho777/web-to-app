package com.webtoapp.ui.webview

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Process
import com.webtoapp.core.logging.AppLogger
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.ConcurrentHashMap

/**
 * Agent-owned preview tasks. The preview tool opens a [WebViewDocumentActivity]
 * itself, whether or not the About-screen separate-tasks switch is on, and
 * later calls find that same task by its document URI.
 */
object PreviewSessions {
    private const val TAG = "PreviewSessions"
    private const val OPEN_TIMEOUT_MS = 8_000L
    private const val SURFACE_TIMEOUT_MS = 8_000L

    /** Stable identity for a preview task. The document URI can be re-encoded in transit. */
    const val EXTRA_SESSION_KEY = "wta_preview_session"

    fun sessionKey(intent: Intent): String? {
        val explicit = intent.getStringExtra(EXTRA_SESSION_KEY)
        if (!explicit.isNullOrEmpty()) return explicit
        return intent.dataString
    }

    private val live = ConcurrentHashMap<String, WebViewDocumentActivity>()
    private val waiting = ConcurrentHashMap<String, CompletableDeferred<WebViewDocumentActivity>>()

    fun documentUriForApp(appId: Long): Uri = Uri.parse("webtoapp://preview/app/$appId")

    fun documentUriForUrl(url: String): Uri =
        Uri.parse("webtoapp://preview/url").buildUpon().appendQueryParameter("u", url).build()

    fun onActivityReady(activity: WebViewDocumentActivity) {
        val key = sessionKey(activity.intent) ?: return
        live[key] = activity
        waiting.remove(key)?.complete(activity)
    }

    fun onActivityGone(activity: WebViewDocumentActivity) {
        val key = sessionKey(activity.intent) ?: return
        live.remove(key, activity)
    }

    fun find(key: String): WebViewDocumentActivity? {
        val activity = live[key] ?: return null
        if (activity.isFinishing || activity.isDestroyed) {
            live.remove(key, activity)
            return null
        }
        return activity
    }

    /**
     * Opens [intent] as its own recents task, or returns the task already
     * showing that document. Fails when the host is in the background and no
     * task exists yet: Android will not start an activity from there.
     */
    suspend fun open(context: Context, intent: Intent): Result<WebViewDocumentActivity> {
        val key = sessionKey(intent)
            ?: return Result.failure(IllegalStateException("preview intent has no document uri"))
        find(key)?.let { existing ->
            try {
                startActivity(context, intent)
            } catch (e: Exception) {
                AppLogger.w(TAG, "bring-to-front failed: ${e.message}")
            }
            return Result.success(existing)
        }
        if (!appInForeground(context)) {
            return Result.failure(
                IllegalStateException("WebToApp is in the background. Bring it to the front, then call preview again.")
            )
        }
        val deferred = CompletableDeferred<WebViewDocumentActivity>()
        val previous = waiting.put(key, deferred)
        previous?.cancel()
        return try {
            startActivity(context, intent)
            val activity = withTimeoutOrNull(OPEN_TIMEOUT_MS) { deferred.await() }
            if (activity == null) {
                waiting.remove(key, deferred)
                Result.failure(IllegalStateException("preview did not open within ${OPEN_TIMEOUT_MS}ms"))
            } else {
                Result.success(activity)
            }
        } catch (e: Exception) {
            waiting.remove(key, deferred)
            AppLogger.w(TAG, "startActivity failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun awaitSurface(activity: WebViewDocumentActivity) {
        val deadline = System.currentTimeMillis() + SURFACE_TIMEOUT_MS
        while (System.currentTimeMillis() < deadline) {
            if (activity.browserSurface != null) return
            delay(50)
        }
    }

    private suspend fun startActivity(context: Context, intent: Intent) {
        try {
            withContext(Dispatchers.Main) {
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            AppLogger.w(TAG, "startActivity failed: ${e.message}")
            throw e
        }
    }

    private fun appInForeground(context: Context): Boolean {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return false
        val mine = am.runningAppProcesses?.firstOrNull { it.pid == Process.myPid() } ?: return false
        return mine.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND ||
            mine.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE
    }
}
