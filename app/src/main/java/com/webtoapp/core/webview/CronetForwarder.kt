package com.webtoapp.core.webview

import android.content.Context
import com.webtoapp.core.featurestack.FeatureStackLoader
import com.webtoapp.core.featurestack.api.CronetStack
import com.webtoapp.core.logging.AppLogger
import java.io.OutputStream

/**
 * Upstream HTTP engine for forced HTTP/3 (issue #721) — facade over the optional
 * `cronet` feature stack (`assets/feature_stacks/cronet.dex`).
 *
 * The Cronet implementation lives in a separately packaged DEX so generated APKs
 * can leave the ~1 MB Chromium Java layer out entirely via the 功能栈 build option.
 * When the dex is absent the bridge simply stays on the classic raw relay — every
 * call here fails soft.
 *
 * Why HTTP/3 goes through Cronet at all: both sides of the bridge speak plain
 * HTTP/1.1 while the upstream leg speaks whatever Chromium negotiates (h3
 * preferred, h2 fallback) — the outbound fingerprint IS real Chromium, which is
 * why this lives next to the fingerprint-spoofing feature instead of conflicting
 * with it.
 */
object CronetForwarder {

    private const val TAG = "CronetForwarder"

    private fun stack(context: Context): CronetStack? =
        FeatureStackLoader.load(context, FeatureStackLoader.STACK_CRONET)

    /**
     * Cheap gate for the bridge: the stack is available and its engine is either
     * live or buildable (native lib embedded / downloaded). Otherwise the bridge
     * stays on the classic raw relay.
     */
    fun isReady(context: Context): Boolean =
        stack(context)?.isReady() ?: false

    /** Called by the bridge when the Cronet upstream activates; warms the engine and,
     *  when the native library is missing, kicks a one-shot background download so the
     *  app self-heals. */
    fun start(appContext: Context, echUpstream: Boolean = false) {
        val impl = stack(appContext)
        if (impl == null) {
            AppLogger.d(TAG, "Cronet feature stack unavailable, staying on classic relay")
            return
        }
        impl.start(echUpstream)
    }

    fun stop() {
        // stop() has no context; an engine can only exist after a load, so a cached
        // slot is enough — nothing to stop when the stack never loaded.
        FeatureStackLoader.peek(FeatureStackLoader.STACK_CRONET)
            ?.let { it as? CronetStack }
            ?.stop()
    }

    /**
     * Forwards one HTTP/1.1 request through Cronet and writes the serialized HTTP/1.1
     * response (chunked body framing) to [sink]. Blocks the caller until the response
     * completes. Returns false when the stack is unavailable or the exchange failed
     * before anything was written (caller synthesizes a 502 / falls back).
     */
    fun forward(
        context: Context,
        method: String,
        url: String,
        headers: List<Pair<String, String>>,
        body: ByteArray?,
        sink: OutputStream,
        clientWantsClose: Boolean,
        hostForHint: String,
        portForHint: Int
    ): Boolean {
        val impl = stack(context) ?: return false
        return impl.forward(
            method, url, headers, body, sink, clientWantsClose, hostForHint, portForHint
        )
    }
}
