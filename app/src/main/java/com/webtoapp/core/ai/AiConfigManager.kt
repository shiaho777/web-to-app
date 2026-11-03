package com.webtoapp.core.ai

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.webtoapp.util.GsonProvider
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.data.model.*
import com.webtoapp.data.model.AiFeature
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private val Context.aiConfigDataStore: DataStore<Preferences> by preferencesDataStore(name = "ai_config")

class AiConfigManager(private val context: Context) {

    companion object {
        private const val TAG = "AiConfigManager"
        private val KEY_API_KEYS = stringPreferencesKey("api_keys")
        private val KEY_API_KEYS_BACKUP = stringPreferencesKey("api_keys_backup")
        private val KEY_SAVED_MODELS = stringPreferencesKey("saved_models")
        private val KEY_DEFAULT_MODEL = stringPreferencesKey("default_model")

        private val gson get() = GsonProvider.gson

        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val KEY_ALIAS = "webtoapp_ai_api_keys"
        private const val ENCRYPTED_PREFIX = "enc_v1:"
        private const val AES_MODE = "AES/GCM/NoPadding"
        private const val GCM_TAG_BITS = 128
        private const val GCM_IV_BYTES = 12
    }

    val apiKeysFlow: Flow<List<ApiKeyConfig>> = context.aiConfigDataStore.data.map { prefs ->
        resolveApiKeys(prefs[KEY_API_KEYS], prefs[KEY_API_KEYS_BACKUP], ::decodeSensitiveJson)
    }

    val savedModelsFlow: Flow<List<SavedModel>> = context.aiConfigDataStore.data.map { prefs ->
        val json = prefs[KEY_SAVED_MODELS] ?: "[]"
        val result = parseSavedModels(json)
        if (result != null) {
            result
        } else {
            AppLogger.e(TAG, "Failed to parse saved models JSON")
            emptyList()
        }
    }

    val defaultModelIdFlow: Flow<String?> = context.aiConfigDataStore.data.map { prefs ->
        prefs[KEY_DEFAULT_MODEL]
    }

    suspend fun addApiKey(config: ApiKeyConfig): Boolean {
        return try {
            context.aiConfigDataStore.edit { prefs ->
                val current = getApiKeys(prefs)
                val updated = current + config
                val jsonStr = gson.toJson(updated)
                prefs[KEY_API_KEYS] = encodeSensitiveJson(jsonStr)
                prefs.remove(KEY_API_KEYS_BACKUP)
                AppLogger.d(TAG, "API key added: ${config.provider.name}, total: ${updated.size}")
            }
            true
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to add API key", e)
            false
        }
    }

    suspend fun updateApiKey(config: ApiKeyConfig): Boolean {
        return try {
            context.aiConfigDataStore.edit { prefs ->
                val current = getApiKeys(prefs)
                val updated = current.map { if (it.id == config.id) config else it }
                val jsonStr = gson.toJson(updated)
                prefs[KEY_API_KEYS] = encodeSensitiveJson(jsonStr)
                prefs.remove(KEY_API_KEYS_BACKUP)
                AppLogger.d(TAG, "API key updated: ${config.provider.name}")
            }
            true
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to update API key", e)
            false
        }
    }

    suspend fun deleteApiKey(id: String): Boolean {
        return try {
            context.aiConfigDataStore.edit { prefs ->
                val current = getApiKeys(prefs)
                val updated = current.filter { it.id != id }
                val jsonStr = gson.toJson(updated)
                prefs[KEY_API_KEYS] = encodeSensitiveJson(jsonStr)
                prefs.remove(KEY_API_KEYS_BACKUP)
                AppLogger.d(TAG, "API key deleted, remaining: ${updated.size}")
            }
            true
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to delete API key", e)
            false
        }
    }

    suspend fun saveModel(model: SavedModel): Boolean {
        return try {
            context.aiConfigDataStore.edit { prefs ->
                val current = getSavedModels(prefs)
                val updated = current + model
                prefs[KEY_SAVED_MODELS] = gson.toJson(updated)
                AppLogger.d(TAG, "Model saved: ${model.model.name}, total: ${updated.size}")
            }
            true
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to save model", e)
            false
        }
    }

    suspend fun updateSavedModel(model: SavedModel): Boolean {
        return try {
            context.aiConfigDataStore.edit { prefs ->
                val current = getSavedModels(prefs)
                val updated = current.map { if (it.id == model.id) model else it }
                prefs[KEY_SAVED_MODELS] = gson.toJson(updated)
                AppLogger.d(TAG, "Model updated: ${model.model.name}")
            }
            true
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to update model", e)
            false
        }
    }

    suspend fun deleteSavedModel(id: String): Boolean {
        return try {
            context.aiConfigDataStore.edit { prefs ->
                val current = getSavedModels(prefs)
                val updated = current.filter { it.id != id }
                prefs[KEY_SAVED_MODELS] = gson.toJson(updated)
                AppLogger.d(TAG, "Model deleted, remaining: ${updated.size}")
            }
            true
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to delete model", e)
            false
        }
    }

    suspend fun setDefaultModel(modelId: String?) {
        context.aiConfigDataStore.edit { prefs ->
            if (modelId != null) {
                prefs[KEY_DEFAULT_MODEL] = modelId
            } else {
                prefs.remove(KEY_DEFAULT_MODEL)
            }
        }
    }

    suspend fun getModelsByCapability(capability: ModelCapability): Flow<List<SavedModel>> {
        return savedModelsFlow.map { models ->
            models.filter { it.capabilities.contains(capability) }
        }
    }

    fun getModelsByFeature(feature: AiFeature): Flow<List<SavedModel>> {
        return savedModelsFlow.map { models ->
            models.filter { it.supportsFeature(feature) }
        }
    }

    fun getDefaultModelForFeature(feature: AiFeature): Flow<SavedModel?> {
        return savedModelsFlow.map { models ->

            models.find { it.isDefault && it.supportsFeature(feature) }
                ?: models.find { it.supportsFeature(feature) }
        }
    }

    suspend fun getApiKeyById(id: String): ApiKeyConfig? {

        val prefs = context.aiConfigDataStore.data.first()
        return getApiKeys(prefs).find { it.id == id }
    }

    suspend fun getSavedModelById(id: String): SavedModel? {
        val prefs = context.aiConfigDataStore.data.first()
        return getSavedModels(prefs).find { it.id == id }
    }

    internal fun resolveApiKeys(
        stored: String?,
        backup: String?,
        decode: (String) -> String?
    ): List<ApiKeyConfig> {
        val decoded = stored?.let(decode)
        if (decoded != null) {
            val result = parseApiKeyConfigs(decoded)
            if (result != null) return result
            AppLogger.e(TAG, "Failed to deserialize API keys from primary")
        }

        if (backup != null) {
            val result = parseApiKeyConfigs(backup)
            if (result != null && result.isNotEmpty()) {
                AppLogger.w(TAG, "Recovered ${result.size} API keys from backup")
                return result
            }
            if (result == null) {
                AppLogger.e(TAG, "Failed to deserialize API keys from backup")
            }
        }

        if (stored != null && decoded == null) {
            AppLogger.w(TAG, "Stored API keys are unreadable on this install; starting empty so new keys can be saved")
        }
        return emptyList()
    }

    private fun getApiKeys(prefs: Preferences): List<ApiKeyConfig> =
        resolveApiKeys(prefs[KEY_API_KEYS], prefs[KEY_API_KEYS_BACKUP], ::decodeSensitiveJson)

    private fun getSavedModels(prefs: Preferences): List<SavedModel> {
        val json = prefs[KEY_SAVED_MODELS] ?: return emptyList()
        return parseSavedModels(json) ?: run {
            AppLogger.e(TAG, "Failed to deserialize saved models")
            emptyList()
        }
    }

    private fun encodeSensitiveJson(plainJson: String): String {
        val encrypted = try {
            encrypt(plainJson)
        } catch (e: Exception) {
            // FAIL-CLOSED: never persist plaintext API keys. Throwing aborts
            // the DataStore edit atomically; the public save wrappers convert
            // this to a failed save instead of a silent plaintext write.
            AppLogger.e(TAG, "API key encryption failed, aborting write", e)
            throw IllegalStateException("API key encryption failed, refusing plaintext fallback", e)
        }
        return "$ENCRYPTED_PREFIX$encrypted"
    }

    private fun decodeSensitiveJson(stored: String): String? {
        if (!stored.startsWith(ENCRYPTED_PREFIX)) return stored
        val payload = stored.removePrefix(ENCRYPTED_PREFIX)
        return try {
            decrypt(payload)
        } catch (e: Exception) {
            AppLogger.e(TAG, "Decryption failed, attempting plain JSON fallback", e)

            try {
                gson.fromJson<List<*>>(payload, List::class.java)

                payload
            } catch (_: Exception) {
                null
            }
        }
    }

    internal fun parseApiKeyConfigs(json: String): List<ApiKeyConfig>? {
        val parsed = parseJsonArray(json, ApiKeyConfig::class.java) ?: return null
        val raw = runCatching { JsonParser.parseString(json).asJsonArray }.getOrNull()
        return parsed.mapIndexed { index, config ->
            val element = raw?.get(index)
            val legacyName = if (config.provider == null && element != null) {
                element.asJsonObject?.get("provider")?.takeIf { it.isJsonPrimitive }?.asString
            } else {
                null
            }
            if (legacyName != null) {
                AppLogger.i(TAG, "Migrating removed provider '$legacyName' to CUSTOM")
            }
            // Rebuild via the constructor: Gson leaves absent fields null and the data
            // class copy()/constructor reject nulls on non-null params.
            ApiKeyConfig(
                id = config.id,
                provider = config.provider ?: AiProvider.CUSTOM,
                apiKey = config.apiKey,
                baseUrl = config.baseUrl?.takeIf { it.isNotBlank() }
                    ?: (if (config.provider == null) legacyName?.let { AiProvider.REMOVED_PROVIDER_BASE_URLS[it] } else null),
                customModelsEndpoint = config.customModelsEndpoint,
                customChatEndpoint = config.customChatEndpoint,
                apiFormat = config.apiFormat ?: ApiFormat.OPENAI_COMPATIBLE,
                alias = config.alias,
                isActive = config.isActive,
                createdAt = config.createdAt
            )
        }
    }

    internal fun parseSavedModels(json: String): List<SavedModel>? {
        val parsed = parseJsonArray(json, SavedModel::class.java) ?: return null
        val raw = runCatching { JsonParser.parseString(json).asJsonArray }.getOrNull()
        return parsed.mapIndexed { index, saved ->
            val element = raw?.get(index)
            val providerRemoved = saved.model.provider == null && element != null
            if (providerRemoved) {
                val legacyName = element?.asJsonObject?.get("model")?.asJsonObject
                    ?.get("provider")?.takeIf { it.isJsonPrimitive }?.asString
                AppLogger.i(TAG, "Migrating saved model on removed provider '${legacyName ?: "?"}' to CUSTOM")
            }
            SavedModel(
                id = saved.id,
                model = AiModel(
                    id = saved.model.id,
                    name = saved.model.name,
                    provider = saved.model.provider ?: AiProvider.CUSTOM,
                    capabilities = saved.model.capabilities ?: listOf(ModelCapability.TEXT),
                    contextLength = saved.model.contextLength,
                    inputPrice = saved.model.inputPrice,
                    outputPrice = saved.model.outputPrice,
                    isCustom = saved.model.isCustom || saved.model.provider == null
                ),
                apiKeyId = saved.apiKeyId,
                alias = saved.alias,
                capabilities = saved.capabilities ?: listOf(ModelCapability.TEXT),
                featureMappings = saved.featureMappings ?: emptyMap(),
                isDefault = saved.isDefault,
                userContextLength = saved.userContextLength,
                createdAt = saved.createdAt
            )
        }
    }

    private fun <T> parseJsonArray(json: String, clazz: Class<T>): List<T>? {
        return try {
            val parsed = JsonParser.parseString(json)
            if (parsed.isJsonNull) return emptyList()
            if (!parsed.isJsonArray) return null

            val jsonArray = parsed.asJsonArray
            val result = jsonArray.mapNotNull { element ->
                try {
                    gson.fromJson(element, clazz)
                } catch (_: Exception) {
                    null
                }
            }

            if (jsonArray.size() > 0 && result.isEmpty()) null else result
        } catch (_: Exception) {
            null
        }
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        val existing = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        if (existing != null) return existing

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setUserAuthenticationRequired(false)
            .build()
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    private fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
        val iv = cipher.iv
        val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        val combined = ByteArray(iv.size + encrypted.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(encrypted, 0, combined, iv.size, encrypted.size)
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    private fun decrypt(base64CipherText: String): String {
        val decoded = Base64.decode(base64CipherText, Base64.NO_WRAP)
        if (decoded.size <= GCM_IV_BYTES) return ""

        val iv = decoded.copyOfRange(0, GCM_IV_BYTES)
        val encrypted = decoded.copyOfRange(GCM_IV_BYTES, decoded.size)

        val cipher = Cipher.getInstance(AES_MODE)
        val spec = GCMParameterSpec(GCM_TAG_BITS, iv)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateSecretKey(), spec)
        val plainBytes = cipher.doFinal(encrypted)
        return String(plainBytes, Charsets.UTF_8)
    }
}
