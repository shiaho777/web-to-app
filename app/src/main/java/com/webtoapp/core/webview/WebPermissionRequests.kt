package com.webtoapp.core.webview

import android.webkit.PermissionRequest
import com.webtoapp.core.logging.AppLogger

/**
 * Answers a WebView [PermissionRequest] without letting the failure kill the activity.
 *
 * [PermissionRequest.grant] throws [IllegalStateException] once the request was
 * already granted or denied. Requesting an undeclared permission throws
 * [IllegalArgumentException] from the permission launcher. Either one previously
 * finished the generated app the moment a page opened the camera.
 */
object WebPermissionRequests {

    private const val TAG = "WebPermission"

    /**
     * @return true when [grant] or [deny] was delivered. False when the request
     * was already cancelled, which is what the system permission dialog does
     * to a WebView capture request.
     */
    fun settle(request: PermissionRequest, granted: Boolean): Boolean {
        return try {
            if (granted) request.grant(request.resources) else request.deny()
            true
        } catch (e: Exception) {
            AppLogger.w(TAG, "settle granted=$granted failed: ${e.message}")
            if (granted) {
                try {
                    request.deny()
                } catch (denyError: Exception) {
                    AppLogger.w(TAG, "deny after failed grant failed: ${denyError.message}")
                }
            }
            false
        }
    }

    /**
     * The page's getUserMedia is already rejected once its [PermissionRequest]
     * dies. A runtime grant cannot revive it. Reload so the page asks again
     * while the OS permission is already held and can be granted in place.
     */
    fun shouldReloadAfterRuntimePermission(
        runtimeGranted: Boolean,
        webRequestSettled: Boolean,
    ): Boolean = runtimeGranted && !webRequestSettled
}

/**
 * Holds the WebView capture request across the Android permission dialog.
 * The dialog pauses the activity, and WebView cancels the request. A later
 * grant then throws, which used to become a permanent camera denial.
 */
class CapturePermissionBridge {

    private var pending: PermissionRequest? = null
    private var cancelled = false
    private var awaitingRuntime = false

    fun remember(request: PermissionRequest) {
        pending = request
        cancelled = false
        awaitingRuntime = true
    }

    fun clear() {
        pending = null
        cancelled = false
        awaitingRuntime = false
    }

    fun onCanceled(request: PermissionRequest) {
        if (pending === request) {
            cancelled = true
            pending = null
        }
    }

    /** @return true when the caller should reload the page */
    fun onRuntimeResult(runtimeGranted: Boolean): Boolean {
        val request = pending
        val wasCancelled = cancelled
        val armed = awaitingRuntime
        pending = null
        cancelled = false
        awaitingRuntime = false
        if (!armed) return false
        val settled = request != null && !wasCancelled &&
            WebPermissionRequests.settle(request, runtimeGranted)
        return WebPermissionRequests.shouldReloadAfterRuntimePermission(runtimeGranted, settled)
    }
}
