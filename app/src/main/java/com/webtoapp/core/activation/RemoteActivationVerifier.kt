package com.webtoapp.core.activation

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.core.network.NetworkModule
import com.webtoapp.data.model.RemoteActivationOfflinePolicy
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.security.KeyFactory
import java.security.SecureRandom
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.util.concurrent.TimeUnit

class RemoteActivationVerifier(private val context: Context) {

    companion object {
        private const val TAG = "RemoteActivation"
        private const val NONCE_BYTES = 24
        private const val CACHE_GRACE_MS = 0L
    }

    data class RemoteRequest(
        val verifyUrl: String,
        val publicKeyBase64: String,
        val offlinePolicy: RemoteActivationOfflinePolicy,
        val code: String,
        val deviceId: String,
        val packageName: String,
        val deliverUrl: Boolean = false
    )

    private val secureRandom = SecureRandom()

    private val httpClient by lazy {
        NetworkModule.customClient {
            connectTimeout(10, TimeUnit.SECONDS)
            readTimeout(15, TimeUnit.SECONDS)
            writeTimeout(10, TimeUnit.SECONDS)
        }
    }

    suspend fun verify(appId: Long, request: RemoteRequest): ActivationResult {
        val urlValidation = validateUrl(request.verifyUrl)
        if (urlValidation != null) return urlValidation

        val publicKey = parsePublicKey(request.publicKeyBase64)
            ?: return ActivationResult.Invalid(remoteMisconfiguredMessage())

        val nonce = generateNonce()
        val timestamp = System.currentTimeMillis()
        val bodyJson = buildRequestBody(request, nonce, timestamp)

        val response = try {
            withContext(Dispatchers.IO) {
                executeRequest(request.verifyUrl, bodyJson)
            }
        } catch (e: Exception) {
            AppLogger.w(TAG, "Remote verification network failure: ${e.message}")
            return handleOffline(appId, request)
        }

        if (response == null) {
            return handleOffline(appId, request)
        }

        val parsed = parseResponse(response)
            ?: return ActivationResult.Invalid(remoteRejectedMessage())

        if (!verifySignature(publicKey, parsed, nonce, request.deliverUrl)) {
            AppLogger.w(TAG, "Remote verification signature mismatch: app=$appId")
            return ActivationResult.Invalid(remoteSignatureFailedMessage())
        }

        if (!parsed.ok) {
            clearCache(appId)
            return ActivationResult.Invalid(parsed.message.ifBlank { remoteRejectedMessage() })
        }

        if (parsed.expiresAt != null && parsed.expiresAt <= System.currentTimeMillis()) {
            clearCache(appId)
            return ActivationResult.Expired
        }

        saveCache(appId, request, parsed)
        AppLogger.i(TAG, "Remote verification success: app=$appId")
        return ActivationResult.Success(if (request.deliverUrl) parsed.url else null)
    }

    suspend fun resolveCachedStartup(appId: Long, request: RemoteRequest): Boolean {
        if (request.offlinePolicy == RemoteActivationOfflinePolicy.ALLOW) return true
        if (request.offlinePolicy == RemoteActivationOfflinePolicy.DENY) return false
        return readValidCache(appId, request) != null
    }

    private suspend fun handleOffline(appId: Long, request: RemoteRequest): ActivationResult {
        return when (request.offlinePolicy) {
            RemoteActivationOfflinePolicy.ALLOW -> ActivationResult.Success(getCachedRemoteUrl(appId))
            RemoteActivationOfflinePolicy.DENY -> ActivationResult.Invalid(remoteOfflineDeniedMessage())
            RemoteActivationOfflinePolicy.ALLOW_CACHED -> {
                if (readValidCache(appId, request) != null) {
                    ActivationResult.Success(getCachedRemoteUrl(appId))
                } else {
                    ActivationResult.Invalid(remoteOfflineNoCacheMessage())
                }
            }
        }
    }

    private fun validateUrl(url: String): ActivationResult? {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) return ActivationResult.Invalid(remoteMisconfiguredMessage())
        if (!trimmed.startsWith("https://", ignoreCase = true)) {
            AppLogger.w(TAG, "Remote verify URL rejected (not https)")
            return ActivationResult.Invalid(remoteInsecureUrlMessage())
        }
        return null
    }

    private fun generateNonce(): String {
        val bytes = ByteArray(NONCE_BYTES)
        secureRandom.nextBytes(bytes)
        return android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
    }

    private fun buildRequestBody(request: RemoteRequest, nonce: String, timestamp: Long): String {
        val obj = JsonObject()
        obj.addProperty("code", request.code)
        obj.addProperty("deviceId", request.deviceId)
        obj.addProperty("packageName", request.packageName)
        obj.addProperty("nonce", nonce)
        obj.addProperty("ts", timestamp)
        return obj.toString()
    }

    private fun executeRequest(url: String, bodyJson: String): String? {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val httpRequest = Request.Builder()
            .url(url.trim())
            .post(bodyJson.toRequestBody(mediaType))
            .header("Accept", "application/json")
            .build()

        httpClient.newCall(httpRequest).execute().use { resp ->
            if (!resp.isSuccessful) {
                AppLogger.w(TAG, "Remote verify HTTP ${resp.code}")
                return null
            }
            return resp.body?.string()
        }
    }

    internal data class RemoteResponse(
        val ok: Boolean,
        val expiresAt: Long?,
        val remainingUses: Int?,
        val message: String,
        val nonce: String,
        val signature: String,
        val url: String?
    )

    private fun parseResponse(raw: String): RemoteResponse? {
        return try {
            val element = JsonParser.parseString(raw)
            if (!element.isJsonObject) return null
            val obj = element.asJsonObject
            RemoteResponse(
                ok = obj.get("ok")?.takeIf { !it.isJsonNull }?.asBoolean ?: false,
                expiresAt = obj.get("expiresAt")?.takeIf { !it.isJsonNull }?.asLong,
                remainingUses = obj.get("remainingUses")?.takeIf { !it.isJsonNull }?.asInt,
                message = obj.get("message")?.takeIf { !it.isJsonNull }?.asString ?: "",
                nonce = obj.get("nonce")?.takeIf { !it.isJsonNull }?.asString ?: "",
                signature = obj.get("sig")?.takeIf { !it.isJsonNull }?.asString ?: "",
                url = obj.get("url")?.takeIf { !it.isJsonNull }?.asString
            )
        } catch (e: Exception) {
            AppLogger.w(TAG, "Remote response parse failure: ${e.message}")
            null
        }
    }

    private fun parsePublicKey(base64: String): java.security.PublicKey? {
        val trimmed = base64.trim()
        if (trimmed.isEmpty()) return null
        return try {
            val cleaned = trimmed
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("\\s".toRegex(), "")
            val der = android.util.Base64.decode(cleaned, android.util.Base64.DEFAULT)
            val keySpec = X509EncodedKeySpec(der)
            KeyFactory.getInstance("EC").generatePublic(keySpec)
        } catch (e: Exception) {
            AppLogger.w(TAG, "Public key parse failure: ${e.message}")
            null
        }
    }

    internal fun verifySignature(
        publicKey: java.security.PublicKey,
        response: RemoteResponse,
        expectedNonce: String,
        includeUrl: Boolean
    ): Boolean {
        if (response.signature.isBlank()) return false
        if (response.nonce != expectedNonce) return false
        return try {
            val signed = canonicalSignedPayload(response, includeUrl)
            val sigBytes = android.util.Base64.decode(response.signature, android.util.Base64.DEFAULT)
            val verifier = Signature.getInstance("SHA256withECDSA")
            verifier.initVerify(publicKey)
            verifier.update(signed.toByteArray(Charsets.UTF_8))
            verifier.verify(sigBytes)
        } catch (e: Exception) {
            AppLogger.w(TAG, "Signature verification error: ${e.message}")
            false
        }
    }

    internal fun canonicalSignedPayload(response: RemoteResponse, includeUrl: Boolean): String {
        val obj = JsonObject()
        obj.addProperty("ok", response.ok)
        obj.addProperty("expiresAt", response.expiresAt ?: 0L)
        obj.addProperty("remainingUses", response.remainingUses ?: -1)
        obj.addProperty("nonce", response.nonce)
        // When deliverUrl is enabled the delivered URL is part of the signed payload, so a MITM
        // cannot swap it while keeping a valid signature. Servers that don't deliver a URL sign
        // the legacy payload (without url), which stays backward compatible.
        if (includeUrl) {
            obj.addProperty("url", response.url ?: "")
        }
        return obj.toString()
    }

    private suspend fun readValidCache(appId: Long, request: RemoteRequest): Long? {
        val prefs = context.activationDataStore.data.first()
        val cachedCode = prefs[stringPreferencesKey("remote_code_$appId")] ?: return null
        if (normalize(cachedCode) != normalize(request.code)) return null
        val expiresAt = prefs[longPreferencesKey("remote_expires_$appId")] ?: return null
        if (expiresAt != 0L && System.currentTimeMillis() > expiresAt + CACHE_GRACE_MS) return null
        return expiresAt
    }

    private suspend fun saveCache(appId: Long, request: RemoteRequest, response: RemoteResponse) {
        context.activationDataStore.edit { prefs ->
            prefs[stringPreferencesKey("remote_code_$appId")] = request.code
            prefs[longPreferencesKey("remote_expires_$appId")] = response.expiresAt ?: 0L
            prefs[longPreferencesKey("remote_verified_at_$appId")] = System.currentTimeMillis()
            if (request.deliverUrl) {
                prefs[stringPreferencesKey("remote_url_$appId")] = response.url ?: ""
            }
        }
    }

    suspend fun getCachedRemoteUrl(appId: Long): String? {
        val url = context.activationDataStore.data.first()[stringPreferencesKey("remote_url_$appId")]
        return url?.takeIf { it.isNotBlank() }
    }

    private suspend fun clearCache(appId: Long) {
        context.activationDataStore.edit { prefs ->
            prefs.remove(stringPreferencesKey("remote_code_$appId"))
            prefs.remove(longPreferencesKey("remote_expires_$appId"))
            prefs.remove(longPreferencesKey("remote_verified_at_$appId"))
            prefs.remove(stringPreferencesKey("remote_url_$appId"))
        }
    }

    private fun normalize(code: String): String = code.trim().uppercase()

    private fun remoteMisconfiguredMessage(): String =
        com.webtoapp.core.i18n.Strings.remoteActivationMisconfigured

    private fun remoteInsecureUrlMessage(): String =
        com.webtoapp.core.i18n.Strings.remoteActivationInsecureUrl

    private fun remoteRejectedMessage(): String =
        com.webtoapp.core.i18n.Strings.remoteActivationRejected

    private fun remoteSignatureFailedMessage(): String =
        com.webtoapp.core.i18n.Strings.remoteActivationSignatureFailed

    private fun remoteOfflineDeniedMessage(): String =
        com.webtoapp.core.i18n.Strings.remoteActivationOfflineDenied

    private fun remoteOfflineNoCacheMessage(): String =
        com.webtoapp.core.i18n.Strings.remoteActivationOfflineNoCache
}
