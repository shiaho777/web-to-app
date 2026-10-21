package com.webtoapp.core.engine

import android.content.Context
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.core.webview.ShareBridge
import java.util.concurrent.ConcurrentHashMap
import org.json.JSONObject
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.WebExtension

/**
 * Gecko has no JavascriptInterface. This built-in extension installs the same
 * page share behavior at document start and forwards the chunks to [ShareBridge].
 */
object GeckoShareExtension {
    private const val LOCATION = "resource://android/assets/web_extensions/wta_share/"
    private const val NATIVE_APP = "wta_share"
    private const val TAG = "GeckoShare"

    private val sessions = ConcurrentHashMap.newKeySet<GeckoSession>()
    private var extension: WebExtension? = null
    private var installing = false
    private var bridge: ShareBridge? = null

    private val delegate = object : WebExtension.MessageDelegate {
        override fun onMessage(
            nativeApp: String,
            message: Any,
            sender: WebExtension.MessageSender
        ): GeckoResult<Any>? {
            val share = bridge
            if (share == null) {
                return GeckoResult.fromValue(error("share unavailable"))
            }
            val json = when (message) {
                is String -> message
                else -> message.toString()
            }
            val payload = try {
                JSONObject(json)
            } catch (e: Exception) {
                return GeckoResult.fromValue(error("bad payload"))
            }
            val response = try {
                handle(share, payload)
            } catch (e: Exception) {
                AppLogger.e(TAG, "Share message failed", e)
                error(e.message ?: "share failed")
            }
            return GeckoResult.fromValue(response)
        }
    }

    fun ensure(runtime: GeckoRuntime, context: Context) {
        if (bridge == null) bridge = ShareBridge(context.applicationContext)
        if (extension != null || installing) {
            sessions.forEach { attach(it) }
            return
        }
        installing = true
        runtime.webExtensionController.installBuiltIn(LOCATION).accept(
            { installed ->
                installing = false
                if (installed == null) {
                    AppLogger.e(TAG, "Share extension install returned null")
                    return@accept
                }
                extension = installed
                AppLogger.d(TAG, "Share extension installed id=${installed.id}")
                sessions.forEach { attach(it) }
            },
            { error ->
                installing = false
                AppLogger.e(TAG, "Share extension install failed: ${error?.message}")
            }
        )
    }

    fun attach(session: GeckoSession) {
        sessions.add(session)
        val installed = extension ?: return
        try {
            session.webExtensionController.setMessageDelegate(installed, delegate, NATIVE_APP)
        } catch (e: Exception) {
            AppLogger.w(TAG, "Attaching share delegate failed: ${e.message}")
        }
    }

    fun detach(session: GeckoSession) {
        sessions.remove(session)
        val installed = extension ?: return
        try {
            session.webExtensionController.setMessageDelegate(installed, null, NATIVE_APP)
        } catch (_: Exception) {
        }
    }

    private fun handle(share: ShareBridge, payload: JSONObject): String {
        return when (payload.optString("op")) {
            "begin" -> json {
                put("ok", true)
                put("id", share.beginShare(
                    payload.optString("title"),
                    payload.optString("text"),
                    payload.optString("url")
                ))
            }
            "file" -> flag(share.beginFile(
                payload.optString("id"),
                payload.optString("name"),
                payload.optString("mime")
            ))
            "chunk" -> flag(share.appendChunk(payload.optString("id"), payload.optString("data")))
            "finish" -> flag(share.finishShare(payload.optString("id")))
            "cancel" -> {
                share.cancelShare(payload.optString("id"))
                flag(true)
            }
            "text" -> {
                share.shareText(
                    payload.optString("title"),
                    payload.optString("text"),
                    payload.optString("url")
                )
                flag(true)
            }
            else -> error("unknown op")
        }
    }

    private fun flag(ok: Boolean): String = json {
        put("ok", ok)
        if (!ok) put("message", "share failed")
    }

    private fun error(message: String): String = json {
        put("ok", false)
        put("message", message)
    }

    private fun json(fill: JSONObject.() -> Unit): String = JSONObject().apply(fill).toString()
}
