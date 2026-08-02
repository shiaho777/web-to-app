package com.webtoapp.core.ai

import android.content.Context
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.core.network.NetworkModule
import com.webtoapp.data.model.AiProvider
import com.webtoapp.data.model.ModelCapability
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.File

/**
 * A model entry from the models.dev catalog.
 */
data class CatalogModel(
    val id: String,
    val name: String,
    val providerId: String,
    val providerName: String,
    val description: String = "",
    val contextLength: Int = 0,
    val maxOutputTokens: Int = 0,
    val inputPrice: Double = 0.0,
    val outputPrice: Double = 0.0,
    val reasoning: Boolean = false,
    val toolCall: Boolean = false,
    val modalitiesInput: List<String> = emptyList(),
    val modalitiesOutput: List<String> = emptyList(),
    val aiProvider: AiProvider? = null
) {
    val supportsVision: Boolean get() = "image" in modalitiesInput
    val isImageGeneration: Boolean get() = "image" in modalitiesOutput

    val capabilities: List<ModelCapability>
        get() = when {
            isImageGeneration -> listOf(ModelCapability.IMAGE_GENERATION)
            supportsVision -> listOf(ModelCapability.MULTIMODAL)
            else -> listOf(ModelCapability.TEXT)
        }
}

data class CatalogProvider(val id: String, val name: String)

sealed class ModelCatalogState {
    object Idle : ModelCatalogState()
    object Loading : ModelCatalogState()
    data class Loaded(val modelCount: Int, val fromCache: Boolean) : ModelCatalogState()
    data class Error(val message: String) : ModelCatalogState()
}

/**
 * Fetches and caches the models.dev open model catalog (https://models.dev/api.json),
 * replacing the old bundled LiteLLM registry. Serves accurate model metadata
 * (capabilities, context length, pricing) for enrichment and for the catalog browser.
 *
 * The catalog is loaded synchronously from the disk cache on first access (fast path)
 * and refreshed asynchronously from the network; lookups read the in-memory index and
 * return null when the catalog is not yet available so callers can fall back to heuristics.
 */
class ModelsDevRepository private constructor(private val context: Context) {

    companion object {
        private const val TAG = "ModelsDev"
        private const val API_URL = "https://models.dev/api.json"
        private const val CACHE_DIR_NAME = "models_dev"
        private const val CACHE_FILE = "api.json"
        private const val CACHE_TTL_MS = 24 * 60 * 60 * 1000L

        @Volatile
        private var INSTANCE: ModelsDevRepository? = null

        fun getInstance(context: Context): ModelsDevRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ModelsDevRepository(context.applicationContext).also { repo ->
                    repo.loadFromCacheSync()
                    repo.warmUp()
                    INSTANCE = repo
                }
            }
        }

        // models.dev provider id -> AiProvider (best effort)
        private val PROVIDER_TO_AI: Map<String, AiProvider> = mapOf(
            "openai" to AiProvider.OPENAI,
            "anthropic" to AiProvider.ANTHROPIC,
            "google" to AiProvider.GOOGLE,
            "deepseek" to AiProvider.DEEPSEEK,
            "openrouter" to AiProvider.OPENROUTER,
            "groq" to AiProvider.GROQ,
            "mistral" to AiProvider.MISTRAL,
            "cohere" to AiProvider.COHERE,
            "xai" to AiProvider.GROK,
            "perplexity" to AiProvider.PERPLEXITY,
            "deepinfra" to AiProvider.DEEPINFRA,
            "cerebras" to AiProvider.CEREBRAS,
            "zhipuai" to AiProvider.GLM,
            "minimax" to AiProvider.MINIMAX,
            "siliconflow" to AiProvider.SILICONFLOW,
            "fireworks-ai" to AiProvider.FIREWORKS,
            "togetherai" to AiProvider.TOGETHER,
            "novita-ai" to AiProvider.NOVITA,
            "moonshotai" to AiProvider.MOONSHOT,
            "stepfun" to AiProvider.STEPFUN,
            "lmstudio" to AiProvider.LM_STUDIO,
            "ollama-cloud" to AiProvider.OLLAMA
        )

        // AiProvider -> models.dev provider ids (for provider-scoped lookup)
        private val AI_TO_PROVIDER_IDS: Map<AiProvider, List<String>> = buildMap {
            PROVIDER_TO_AI.forEach { (pid, ai) -> put(ai, (get(ai) ?: emptyList()) + pid) }
            put(AiProvider.OLLAMA, (get(AiProvider.OLLAMA) ?: emptyList()) + "ollama")
        }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val httpClient = NetworkModule.defaultClient
    private val cacheDir: File by lazy { File(context.cacheDir, CACHE_DIR_NAME).apply { mkdirs() } }

    private val _state = MutableStateFlow<ModelCatalogState>(ModelCatalogState.Idle)
    val state: StateFlow<ModelCatalogState> = _state.asStateFlow()

    @Volatile private var byId: Map<String, CatalogModel> = emptyMap()
    @Volatile private var byProvider: Map<String, List<CatalogModel>> = emptyMap()
    @Volatile private var providerList: List<CatalogProvider> = emptyList()

    private fun warmUp() {
        scope.launch { refresh(force = false) }
    }

    suspend fun refresh(force: Boolean = false) = withContext(Dispatchers.IO) {
        val cacheFile = File(cacheDir, CACHE_FILE)
        if (!force && byId.isNotEmpty() && cacheFile.exists() && cacheAgeMs() < CACHE_TTL_MS) {
            return@withContext
        }
        _state.value = ModelCatalogState.Loading
        val raw = fetchCatalog()
        if (raw != null && parseAndIndex(raw)) {
            runCatching { cacheFile.writeText(raw) }
            _state.value = ModelCatalogState.Loaded(byId.size, fromCache = false)
            return@withContext
        }
        // Network failed: fall back to a stale cache if we have nothing in memory.
        if (byId.isEmpty() && cacheFile.exists()) {
            val cached = runCatching { cacheFile.readText() }.getOrNull()
            if (cached != null && parseAndIndex(cached)) {
                _state.value = ModelCatalogState.Loaded(byId.size, fromCache = true)
                return@withContext
            }
        }
        if (byId.isNotEmpty()) {
            _state.value = ModelCatalogState.Loaded(byId.size, fromCache = true)
        } else {
            _state.value = ModelCatalogState.Error("Could not load the models.dev catalog.")
        }
    }

    private fun loadFromCacheSync() {
        val cacheFile = File(cacheDir, CACHE_FILE)
        if (!cacheFile.exists()) return
        val raw = runCatching { cacheFile.readText() }.getOrNull() ?: return
        if (parseAndIndex(raw) && byId.isNotEmpty()) {
            _state.value = ModelCatalogState.Loaded(byId.size, fromCache = true)
        }
    }

    private fun fetchCatalog(): String? {
        return try {
            val req = Request.Builder().url(API_URL).get().build()
            httpClient.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) resp.body?.string()
                else {
                    AppLogger.w(TAG, "fetch -> HTTP ${resp.code}")
                    null
                }
            }
        } catch (e: Exception) {
            AppLogger.w(TAG, "fetch failed: ${e.message}")
            null
        }
    }

    private fun parseAndIndex(raw: String): Boolean {
        return try {
            val root = JsonParser.parseString(raw).asJsonObject
            val newById = HashMap<String, CatalogModel>()
            val newByProvider = HashMap<String, MutableList<CatalogModel>>()
            val newProviders = mutableListOf<CatalogProvider>()
            root.entrySet().forEach { (providerId, provEl) ->
                if (!provEl.isJsonObject) return@forEach
                val provObj = provEl.asJsonObject
                val providerName = provObj.get("name")?.asString ?: providerId
                newProviders += CatalogProvider(providerId, providerName)
                val aiProvider = PROVIDER_TO_AI[providerId]
                val modelsEl = provObj.get("models")?.takeIf { it.isJsonObject }?.asJsonObject
                    ?: return@forEach
                modelsEl.entrySet().forEach { (modelId, modelEl) ->
                    if (!modelEl.isJsonObject) return@forEach
                    val model = parseModel(modelId, modelEl.asJsonObject, providerId, providerName, aiProvider)
                    newByProvider.getOrPut(providerId) { mutableListOf() }.add(model)
                    if (model.id !in newById) newById[model.id] = model
                    newById["$providerId/${model.id}"] = model
                }
            }
            byId = newById
            byProvider = newByProvider
            providerList = newProviders
            AppLogger.i(TAG, "Indexed ${newByProvider.values.sumOf { it.size }} models from models.dev")
            true
        } catch (e: Exception) {
            AppLogger.e(TAG, "parse failed: ${e.message}")
            false
        }
    }

    private fun parseModel(
        modelId: String,
        m: JsonObject,
        providerId: String,
        providerName: String,
        aiProvider: AiProvider?
    ): CatalogModel {
        val limit = m.get("limit")?.takeIf { it.isJsonObject }?.asJsonObject
        val cost = m.get("cost")?.takeIf { it.isJsonObject }?.asJsonObject
        val modalities = m.get("modalities")?.takeIf { it.isJsonObject }?.asJsonObject
        val context = limit?.get("context")?.asInt ?: 0
        val input = limit?.get("input")?.asInt ?: 0
        val output = limit?.get("output")?.asInt ?: 0
        return CatalogModel(
            id = m.get("id")?.asString ?: modelId,
            name = m.get("name")?.asString ?: modelId,
            providerId = providerId,
            providerName = providerName,
            description = m.get("description")?.asString ?: "",
            contextLength = context.takeIf { it > 0 } ?: input,
            maxOutputTokens = output,
            inputPrice = cost?.get("input")?.asDouble ?: 0.0,
            outputPrice = cost?.get("output")?.asDouble ?: 0.0,
            reasoning = m.get("reasoning")?.asBoolean ?: false,
            toolCall = m.get("tool_call")?.asBoolean ?: false,
            modalitiesInput = modalities?.get("input")?.takeIf { it.isJsonArray }?.asJsonArray
                ?.mapNotNull { runCatching { it.asString }.getOrNull() } ?: emptyList(),
            modalitiesOutput = modalities?.get("output")?.takeIf { it.isJsonArray }?.asJsonArray
                ?.mapNotNull { runCatching { it.asString }.getOrNull() } ?: emptyList(),
            aiProvider = aiProvider
        )
    }

    private fun cacheAgeMs(): Long {
        val f = File(cacheDir, CACHE_FILE)
        if (!f.exists()) return Long.MAX_VALUE
        return System.currentTimeMillis() - f.lastModified()
    }

    // ---- Lookup API (drop-in for the old LiteLLMModelRegistry) ----

    fun findModel(modelId: String, provider: AiProvider? = null): CatalogModel? {
        if (byId.isEmpty()) return null
        if (provider != null) {
            AI_TO_PROVIDER_IDS[provider]?.forEach { pid ->
                byProvider[pid]?.forEach { if (it.id == modelId) return it }
            }
        }
        byId[modelId]?.let { return it }
        val bareId = if (modelId.contains("/")) modelId.substringAfter("/") else modelId
        if (bareId != modelId) byId[bareId]?.let { return it }
        if (provider != null) {
            AI_TO_PROVIDER_IDS[provider]?.forEach { pid ->
                byProvider[pid]?.forEach { if (it.id == bareId) return it }
            }
        }
        return null
    }

    fun getContextLength(modelId: String, provider: AiProvider? = null): Int? =
        findModel(modelId, provider)?.contextLength?.takeIf { it > 0 }

    fun getMaxOutputTokens(modelId: String, provider: AiProvider? = null): Int? =
        findModel(modelId, provider)?.maxOutputTokens?.takeIf { it > 0 }

    fun getInputPrice(modelId: String, provider: AiProvider? = null): Double? =
        findModel(modelId, provider)?.inputPrice?.takeIf { it > 0.0 }

    fun getOutputPrice(modelId: String, provider: AiProvider? = null): Double? =
        findModel(modelId, provider)?.outputPrice?.takeIf { it > 0.0 }

    fun getCapabilities(modelId: String, provider: AiProvider? = null): List<ModelCapability>? =
        findModel(modelId, provider)?.capabilities

    fun getModelsForProvider(provider: AiProvider): List<CatalogModel> {
        val ids = AI_TO_PROVIDER_IDS[provider] ?: return emptyList()
        return ids.flatMap { byProvider[it].orEmpty() }
    }

    fun getRecommendedModels(provider: AiProvider): List<String> {
        val deprecated = listOf("preview", "experimental")
        return getModelsForProvider(provider)
            .map { it.id }
            .filter { id ->
                val l = id.lowercase()
                !deprecated.any { l.endsWith(it) } && !l.startsWith("ft:")
            }
            .distinct()
            .take(50)
    }

    // ---- Catalog browsing (for the UI) ----

    fun allModels(): List<CatalogModel> = byProvider.values.flatten()

    fun allProviders(): List<CatalogProvider> = providerList

    fun getModelsByProviderId(providerId: String): List<CatalogModel> = byProvider[providerId].orEmpty()

    fun isLoaded(): Boolean = byId.isNotEmpty()
}
