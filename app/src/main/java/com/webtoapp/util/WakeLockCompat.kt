package com.webtoapp.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.PowerManager
import androidx.core.content.ContextCompat
import com.webtoapp.core.logging.AppLogger

/**
 * Fail-soft WakeLock helpers.
 *
 * Generated APKs only declare android.permission.WAKE_LOCK when an enabled
 * feature needs it, and OEM permission managers (MIUI/HyperOS) can revoke even
 * install-time "normal" permissions. A denied WakeLock.acquire() throws
 * SecurityException — that must degrade to running without the lock, never
 * crash the app (issue #1034).
 */
object WakeLockCompat {

    private const val TAG = "WakeLockCompat"

    /**
     * Acquires a fresh partial wake lock for [timeoutMs], or returns null when
     * the permission is missing or the acquire fails. The wake lock is a
     * best-effort optimization — callers must keep working without it.
     */
    fun acquire(context: Context, tag: String, timeoutMs: Long): PowerManager.WakeLock? {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WAKE_LOCK) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            AppLogger.w(TAG, "WAKE_LOCK not granted; continuing without wake lock ($tag)")
            return null
        }
        return try {
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, tag).apply {
                setReferenceCounted(false)
                acquire(timeoutMs)
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "WakeLock acquire failed ($tag)", e)
            null
        }
    }

    fun release(wakeLock: PowerManager.WakeLock?) {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock.release()
            }
        } catch (e: Exception) {
            AppLogger.w(TAG, "WakeLock release failed", e)
        }
    }
}
