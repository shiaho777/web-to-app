package com.webtoapp.core.port

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.webtoapp.core.logging.AppLogger

/**
 * Remote port release for cooperating WTA apps (the host's Port Manager
 * screen). The receiver is exported because generated APKs are signed with
 * independent per-app keys — a signature permission would sever the feature.
 *
 * Since no pre-shared secret exists between independently built apps, two
 * layers replace it:
 *
 *  1. A single-use session token issued by [PortQueryReceiver]. A blind
 *     broadcast carries no token and is rejected — the caller must complete
 *     the query → token → release handshake first.
 *  2. On API 34+, [getSentFromUid] additionally verifies the sender package
 *     is WTA-marked (defense in depth — the marker alone is spoofable, the
 *     token alone is protocol-obscurity; together they raise the bar).
 *
 * Residual: a determined attacker can still run the handshake and spoof the
 * marker. The token must therefore be treated as a fence against casual /
 * accidental abuse, not a capability in the cryptographic sense.
 */
class PortReleaseReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_PORT_RELEASE) return

        val pendingResult = goAsync()
        try {
            if (!verifyCaller(context, intent)) {
                pendingResult.resultCode = RESULT_CODE_ERROR
                pendingResult.resultData = "unauthorized"
                return
            }

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

    /**
     * The token is the primary gate on every API level; the WTA-marker sender
     * check is additive hardening where the platform exposes the sender uid.
     */
    private fun verifyCaller(context: Context, intent: Intent): Boolean {
        if (!PortSessionAuth.consume(intent.getStringExtra(EXTRA_SESSION_TOKEN))) {
            AppLogger.w(TAG, "Rejected port release: missing/invalid session token")
            return false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val uid = runCatching { sentFromUid }.getOrDefault(-1)
            if (uid >= 0) {
                val pkgs = context.packageManager.getPackagesForUid(uid).orEmpty()
                if (pkgs.isNotEmpty() && pkgs.any { !WtaAppPortDiscovery.isWtaApp(context, it) }) {
                    AppLogger.w(TAG, "Rejected port release from non-WTA sender uid $uid")
                    return false
                }
            }
        }
        return true
    }

    companion object {
        private const val TAG = "PortReleaseReceiver"

        const val ACTION_PORT_RELEASE = "com.webtoapp.action.PORT_RELEASE"

        const val EXTRA_PORT = "wta.port"
        const val EXTRA_OWNER = "wta.owner"
        const val EXTRA_RELEASE_ALL = "wta.releaseAll"
        const val EXTRA_SESSION_TOKEN = "wta.sessionToken"

        const val RESULT_CODE_OK = 1
        const val RESULT_CODE_ERROR = -1
    }
}
