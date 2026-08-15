package com.webtoapp.core.webview

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * Local network access permission, introduced in Android 16 (opt-in) and enforced from
 * Android 17 / SDK 37 for apps targeting 37+; some OEM ROMs enforce it regardless of
 * targetSdk. Without the grant, traffic to LAN hosts is blocked deep in the network
 * stack: WebView fails with ERR_LOCAL_NETWORK_PERMISSION_MISSING and plain TCP sockets
 * silently time out. Loopback (127.0.0.1) is never restricted, so on-device local server
 * runtimes keep working.
 *
 * The permission constant only exists from SDK 37 while this project compiles against
 * SDK 36, hence the hardcoded string.
 */
object LocalNetworkPermission {
    const val PERMISSION = "android.permission.ACCESS_LOCAL_NETWORK"

    /** SDKs below 36 grant local network access implicitly via INTERNET. */
    fun isGranted(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < 36) return true
        return ContextCompat.checkSelfPermission(context, PERMISSION) == PackageManager.PERMISSION_GRANTED
    }

    fun isPrivateNetworkUrl(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        val host = try {
            android.net.Uri.parse(url).host ?: return false
        } catch (_: Exception) {
            return false
        }
        return isPrivateNetworkHost(host)
    }

    fun isPrivateNetworkHost(host: String): Boolean {
        val h = host.trim().lowercase().removeSuffix(".")
        if (h.isEmpty()) return false

        // Loopback is exempt from the permission by design.
        if (h == "localhost" || h == "::1" || h == "127.0.0.1" || h.startsWith("127.")) return false

        if (h == "local" || h.endsWith(".local")) return true

        val v4 = h.removeSurrounding("[", "]")
        val parts = v4.split(".")
        if (parts.size == 4 && parts.all { it.toIntOrNull() in 0..255 }) {
            val a = parts[0].toInt()
            val b = parts[1].toInt()
            return when {
                a == 10 -> true                              // 10.0.0.0/8
                a == 192 && b == 168 -> true                 // 192.168.0.0/16
                a == 172 && b in 16..31 -> true              // 172.16.0.0/12
                a == 169 && b == 254 -> true                 // 169.254.0.0/16 link-local
                else -> false
            }
        }

        // IPv6 without dots: unique-local fc00::/7 and link-local fe80::/10.
        if (!h.contains(".")) {
            val firstGroup = h.substringBefore(":").takeIf { it.isNotEmpty() }?.toIntOrNull(16)
            if (firstGroup != null) {
                if ((firstGroup shr 8) == 0xfc || (firstGroup shr 8) == 0xfd) return true
                if ((firstGroup and 0xffc0) == 0xfe80) return true
            }
        }
        return false
    }

    fun isLocalNetworkBlockedError(description: String?): Boolean {
        return description?.contains("ERR_LOCAL_NETWORK", ignoreCase = true) == true
    }

    /**
     * A permission prompt is warranted only on SDKs that have the permission, when it is
     * not granted yet, and when either the failing request targeted the local network
     * (LAN denials surface as timeouts) or the WebView error explicitly names it.
     */
    fun shouldRequest(context: Context, failedUrl: String?, errorDescription: String? = null): Boolean {
        if (Build.VERSION.SDK_INT < 36) return false
        if (isGranted(context)) return false
        return isLocalNetworkBlockedError(errorDescription) || isPrivateNetworkUrl(failedUrl)
    }

    fun request(activity: Activity): Boolean {
        return try {
            activity.requestPermissions(arrayOf(PERMISSION), 4354)
            true
        } catch (_: Exception) {
            false
        }
    }
}
