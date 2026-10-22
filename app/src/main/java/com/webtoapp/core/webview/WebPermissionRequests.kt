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

    fun settle(request: PermissionRequest, granted: Boolean) {
        try {
            if (granted) request.grant(request.resources) else request.deny()
        } catch (e: Exception) {
            AppLogger.w(TAG, "settle granted=$granted failed: ${e.message}")
            if (granted) {
                try {
                    request.deny()
                } catch (denyError: Exception) {
                    AppLogger.w(TAG, "deny after failed grant failed: ${denyError.message}")
                }
            }
        }
    }
}
