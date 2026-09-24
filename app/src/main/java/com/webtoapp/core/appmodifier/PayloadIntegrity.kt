package com.webtoapp.core.appmodifier

import android.content.Context
import android.util.Base64
import com.webtoapp.core.logging.AppLogger
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Integrity tag for intent payloads that must cross an exported boundary.
 *
 * `SplashLauncherActivity` is exported because pinned home-screen shortcuts are
 * replayed by the launcher app — the intent (and its extras) is effectively
 * caller-controlled input. Since the payload is authored by [AppCloner] and
 * consumed by the activity inside the same app, a keyed HMAC authenticates it:
 * the key is generated once into private storage and never leaves the device.
 */
object PayloadIntegrity {

    private const val TAG = "PayloadIntegrity"
    private const val KEY_FILE = "splash_payload_integrity.key"
    private const val KEY_BYTES = 32
    private const val MAC_ALGORITHM = "HmacSHA256"

    @Volatile
    private var cachedKey: SecretKeySpec? = null

    fun sign(context: Context, payload: String): String {
        val mac = Mac.getInstance(MAC_ALGORITHM).apply { init(key(context)) }
        return Base64.encodeToString(
            mac.doFinal(payload.toByteArray(Charsets.UTF_8)),
            Base64.NO_WRAP
        )
    }

    fun verify(context: Context, payload: String?, signature: String?): Boolean {
        if (payload.isNullOrEmpty() || signature.isNullOrBlank()) return false
        val expected = runCatching { sign(context, payload) }.getOrNull() ?: return false
        val expectedBytes = decode(expected) ?: return false
        val providedBytes = decode(signature) ?: return false
        return MessageDigest.isEqual(expectedBytes, providedBytes)
    }

    private fun decode(value: String): ByteArray? =
        runCatching { Base64.decode(value, Base64.DEFAULT) }.getOrNull()

    @Synchronized
    private fun key(context: Context): SecretKeySpec {
        cachedKey?.let { return it }
        val appContext = context.applicationContext
        val file = java.io.File(appContext.filesDir, KEY_FILE)
        val stored = if (file.isFile) {
            runCatching { file.readBytes() }.getOrNull()?.takeIf { it.size == KEY_BYTES }
        } else null
        val keyBytes = stored ?: ByteArray(KEY_BYTES).also { bytes ->
            SecureRandom().nextBytes(bytes)
            runCatching {
                appContext.openFileOutput(KEY_FILE, Context.MODE_PRIVATE).use { it.write(bytes) }
            }.onFailure { AppLogger.w(TAG, "Failed to persist payload key: ${it.message}") }
        }
        return SecretKeySpec(keyBytes, MAC_ALGORITHM).also { cachedKey = it }
    }
}
