package com.webtoapp.core.port

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.webtoapp.core.logging.AppLogger

class PortReleaseReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_PORT_RELEASE) return

        val pendingResult = goAsync()
        try {
            val before = PortManager.getAllAllocations().size

            when {
                intent.getBooleanExtra(EXTRA_RELEASE_ALL, false) -> {
                    PortManager.releaseAll()
                }
                intent.hasExtra(EXTRA_OWNER) -> {
                    val owner = intent.getStringExtra(EXTRA_OWNER) ?: ""
                    if (owner.isNotBlank()) PortManager.releaseByOwner(owner)
                }
                else -> {
                    val port = intent.getIntExtra(EXTRA_PORT, -1)
                    if (port > 0) PortManager.release(port)
                }
            }

            val after = PortManager.getAllAllocations().size
            val released = (before - after).coerceAtLeast(0)

            pendingResult.resultCode = RESULT_CODE_OK
            pendingResult.resultData = released.toString()
        } catch (e: Exception) {
            AppLogger.w(TAG, "PortReleaseReceiver failed: ${e.message}")
            pendingResult.resultCode = RESULT_CODE_ERROR
        } finally {
            pendingResult.finish()
        }
    }

    companion object {
        private const val TAG = "PortReleaseReceiver"

        const val ACTION_PORT_RELEASE = "com.webtoapp.action.PORT_RELEASE"

        const val EXTRA_PORT = "wta.port"
        const val EXTRA_OWNER = "wta.owner"
        const val EXTRA_RELEASE_ALL = "wta.releaseAll"

        const val RESULT_CODE_OK = 1
        const val RESULT_CODE_ERROR = -1
    }
}
