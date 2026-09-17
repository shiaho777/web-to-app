package com.webtoapp.core.share

import android.util.Base64
import com.webtoapp.core.logging.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Serialisation of inbox items into the shape the page sees (issue #943).
 *
 * Two artifacts:
 *
 * * [SHARE_INBOX_BOOTSTRAP_JS] — installed once, at document-start, so a page that wants the
 *   share content can start listening before its own scripts run. Listener registration is the
 *   only thing the page has to do; nothing fires unless a share actually arrives.
 * * [buildShareBatch] — the per-delivery `__WTA_SHARE_PUSH__(…)` call, built when native has
 *   items and the page is ready.
 *
 * Payloads that fit [ShareReceiveContract.INLINE_MAX_BYTES] carry a base64 `dataUrl` so the
 * page can use the bytes with no further round-trip. Larger ones are persisted and offered to
 * the file chooser, but reach the page as metadata only — pushing multi-megabyte strings
 * through `evaluateJavascript` is unreliable and needlessly inflates memory.
 */
private const val TAG = "SharePagePayload"

/**
 * Document-start bootstrap for the page-facing share API.
 *
 * Native re-announces the queue on every document load (so a reload or an in-page navigation
 * does not lose a share that has not been used yet), which means the same payload can be
 * pushed more than once into the *same* document — e.g. a share that lands while the page is
 * still loading, then again on `onPageFinished`. `seen` makes that idempotent: an id is
 * delivered to a document exactly once, and a fresh document starts with an empty `seen`.
 */
val SHARE_INBOX_BOOTSTRAP_JS: String = """
    (function() {
        'use strict';
        if (window.${ShareReceiveContract.JS_NAMESPACE} && window.${ShareReceiveContract.JS_NAMESPACE}.__wta__) return;

        var queue = [];
        var listeners = [];
        var seen = {};
        var seenCount = 0;
        var SEEN_LIMIT = 128;

        function remember(id) {
            if (!id || seen[id]) return false;
            if (seenCount >= SEEN_LIMIT) { seen = {}; seenCount = 0; }
            seen[id] = true;
            seenCount++;
            return true;
        }

        function emit(batch) {
            var detail = { items: batch };
            try {
                window.dispatchEvent(new CustomEvent('${ShareReceiveContract.JS_EVENT_NAME}', { detail: detail }));
            } catch (e) { }
            try {
                if (document && document.dispatchEvent) {
                    document.dispatchEvent(new CustomEvent('${ShareReceiveContract.JS_EVENT_NAME}', { detail: detail }));
                }
            } catch (e) { }
            for (var i = 0; i < listeners.length; i++) {
                try { listeners[i](batch.slice()); } catch (e) { }
            }
        }

        window.${ShareReceiveContract.JS_NAMESPACE} = {
            __wta__: true,
            version: 1,
            /** Subscribe to share batches. Returns an unsubscribe function. */
            onShare: function(fn) {
                if (typeof fn !== 'function') return function() {};
                listeners.push(fn);
                return function() {
                    var i = listeners.indexOf(fn);
                    if (i >= 0) listeners.splice(i, 1);
                };
            },
            /** Items received in this document and not yet taken, without draining. */
            peek: function() { return queue.slice(); },
            /** Drain the items received in this document. */
            take: function() {
                var items = queue.slice();
                queue = [];
                return items;
            },
            /** Forget everything received in this document (does not affect native storage). */
            clear: function() { queue = []; }
        };

        window.__WTA_SHARE_PUSH__ = function(items) {
            if (!items || !items.length) return;
            var fresh = [];
            for (var i = 0; i < items.length; i++) {
                if (remember(items[i] && items[i].id)) fresh.push(items[i]);
            }
            if (!fresh.length) return;
            queue = queue.concat(fresh);
            emit(fresh);
        };
    })();
""".trimIndent()

/** JSON is a subset of JS literals, except the two Unicode line terminators. */
private fun jsSafe(json: String): String =
    json.replace("\u2028", "\\u2028").replace("\u2029", "\\u2029")

private fun coarseTypeOf(mimeType: String): String = when {
    mimeType.startsWith("image/") -> "image"
    mimeType.startsWith("video/") -> "video"
    mimeType.startsWith("audio/") -> "audio"
    mimeType == ShareReceiveContract.MIME_TEXT -> "text"
    else -> "file"
}

/**
 * Build the `__WTA_SHARE_PUSH__(…)` argument for [items]: a JSON array of page-facing items,
 * escaped for direct interpolation into a JS expression.
 */
suspend fun buildShareBatch(items: List<SharedItem>): String =
    withContext(Dispatchers.IO) {
        val array = JSONArray()

        for (item in items) {
            val json = JSONObject().apply {
                put("id", item.id)
                put("kind", item.kind)
                put("type", coarseTypeOf(item.mimeType))
                put("mimeType", item.mimeType)
                put("name", item.name)
                put("size", item.size)
                put("receivedAt", item.receivedAt)
            }

            if (item.isText) {
                // Text has no byte payload to inline — the string itself is the content. A
                // `data:` URL would need percent-encoding to stay valid and buys nothing.
                json.put("inline", true)
                json.put("text", item.text.orEmpty())
                array.put(json)
                continue
            }

            val file = item.path?.let(::File)
            if (file == null || !file.exists()) continue

            json.put("fileUrl", "file://" + file.absolutePath)

            if (file.length() <= ShareReceiveContract.INLINE_MAX_BYTES) {
                try {
                    val base64 = Base64.encodeToString(file.readBytes(), Base64.NO_WRAP)
                    json.put("inline", true)
                    json.put("dataUrl", "data:" + item.mimeType + ";base64," + base64)
                } catch (e: Exception) {
                    AppLogger.w(TAG, "Failed to inline ${item.name}: ${e.message}")
                    json.put("inline", false)
                }
            } else {
                json.put("inline", false)
            }

            array.put(json)
        }

        jsSafe(array.toString())
    }
