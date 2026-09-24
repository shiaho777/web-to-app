package com.webtoapp.core.share

import android.content.Context
import android.webkit.JavascriptInterface
import com.webtoapp.core.logging.AppLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONArray

/**
 * The JS→native leg of the inbound share channel (issue #1038).
 *
 * The `WTAShareInbox` bootstrap calls [consume] when the page takes items or explicitly
 * acknowledges them; the ids are then marked consumed in the inbox index so a later
 * document load or app relaunch no longer re-announces content the page already handled.
 *
 * Registered under [ShareReceiveContract.JS_BRIDGE_NAME] only while the share-receive
 * feature is on. Item ids are unguessable random tokens, so a page can only ever consume
 * what it was actually given.
 */
class ShareInboxBridge(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * @param idsJson a JSON array of item ids. Runs on the WebView's JavaBridge thread —
     *   the actual index rewrite is handed to [scope] (IO); a failure must never
     *   propagate back into the page.
     */
    @JavascriptInterface
    fun consume(idsJson: String?) {
        val ids = try {
            val array = JSONArray(idsJson ?: return)
            (0 until array.length()).mapNotNull { i ->
                array.optString(i).takeIf { it.isNotBlank() }
            }
        } catch (e: Exception) {
            emptyList()
        }
        if (ids.isEmpty()) return

        scope.launch {
            try {
                SharedContentInbox.consume(context, ids)
            } catch (e: Exception) {
                AppLogger.w("ShareInboxBridge", "Failed to consume share items: ${e.message}")
            }
        }
    }
}
