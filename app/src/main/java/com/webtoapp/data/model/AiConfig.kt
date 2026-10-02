package com.webtoapp.data.model

import com.google.gson.annotations.SerializedName
import com.webtoapp.core.i18n.Strings

enum class ProviderCategory {
    RECOMMENDED,
    INTERNATIONAL,
    CHINESE,
    AGGREGATOR,
    SELF_HOSTED,
    CUSTOM;

    val displayName: String get() = when (this) {
        RECOMMENDED -> Strings.providerCategoryRecommended
        INTERNATIONAL -> Strings.providerCategoryInternational
        CHINESE -> Strings.providerCategoryChinese
        AGGREGATOR -> Strings.providerCategoryAggregator
        SELF_HOSTED -> Strings.providerCategorySelfHosted
        CUSTOM -> Strings.providerCategoryCustom
    }
}

enum class AiProvider(
    val baseUrl: String,
    val modelsEndpoint: String = "/v1/models",
    val apiKeyUrl: String = "",
    val category: ProviderCategory = ProviderCategory.INTERNATIONAL
) {

    GOOGLE(
        baseUrl = "https://generativelanguage.googleapis.com",
        modelsEndpoint = "/v1beta/models",
        apiKeyUrl = "https://aistudio.google.com/apikey",
        category = ProviderCategory.RECOMMENDED
    ),
    OPENROUTER(
        baseUrl = "https://openrouter.ai/api",
        modelsEndpoint = "/v1/models",
        apiKeyUrl = "https://openrouter.ai/keys",
        category = ProviderCategory.RECOMMENDED
    ),

    OPENAI(
        baseUrl = "https://api.openai.com",
        modelsEndpoint = "/v1/models",
        apiKeyUrl = "https://platform.openai.com/api-keys",
        category = ProviderCategory.INTERNATIONAL
    ),
    ANTHROPIC(
        baseUrl = "https://api.anthropic.com",
        modelsEndpoint = "/v1/models",
        apiKeyUrl = "https://console.anthropic.com/settings/keys",
        category = ProviderCategory.INTERNATIONAL
    ),
    GROK(
        baseUrl = "https://api.x.ai",
        modelsEndpoint = "/v1/models",
        apiKeyUrl = "https://console.x.ai/",
        category = ProviderCategory.INTERNATIONAL
    ),

    TOGETHER(
        baseUrl = "https://api.together.xyz",
        modelsEndpoint = "/v1/models",
        apiKeyUrl = "https://api.together.xyz/settings/api-keys",
        category = ProviderCategory.AGGREGATOR
    ),
    PERPLEXITY(
        baseUrl = "https://api.perplexity.ai",
        modelsEndpoint = "/v1/models",
        apiKeyUrl = "https://www.perplexity.ai/settings/api",
        category = ProviderCategory.AGGREGATOR
    ),
    FIREWORKS(
        baseUrl = "https://api.fireworks.ai/inference",
        modelsEndpoint = "/v1/models",
        apiKeyUrl = "https://fireworks.ai/api-keys",
        category = ProviderCategory.AGGREGATOR
    ),

    DEEPSEEK(
        baseUrl = "https://api.deepseek.com",
        modelsEndpoint = "/v1/models",
        apiKeyUrl = "https://platform.deepseek.com/api_keys",
        category = ProviderCategory.CHINESE
    ),
    QWEN(
        baseUrl = "https://dashscope.aliyuncs.com/compatible-mode",
        modelsEndpoint = "/v1/models",
        apiKeyUrl = "https://dashscope.console.aliyun.com/apiKey",
        category = ProviderCategory.CHINESE
    ),
    GLM(
        baseUrl = "https://open.bigmodel.cn/api/paas",
        modelsEndpoint = "/v4/models",
        apiKeyUrl = "https://open.bigmodel.cn/usercenter/apikeys",
        category = ProviderCategory.CHINESE
    ),

    OLLAMA(
        baseUrl = "http://localhost:11434",
        modelsEndpoint = "/api/tags",
        apiKeyUrl = "https://ollama.com/",
        category = ProviderCategory.SELF_HOSTED
    ),
    LM_STUDIO(
        baseUrl = "http://localhost:1234",
        modelsEndpoint = "/v1/models",
        apiKeyUrl = "https://lmstudio.ai/",
        category = ProviderCategory.SELF_HOSTED
    ),
    VLLM(
        baseUrl = "http://localhost:8000",
        modelsEndpoint = "/v1/models",
        apiKeyUrl = "https://docs.vllm.ai/",
        category = ProviderCategory.SELF_HOSTED
    ),

    CUSTOM(
        baseUrl = "",
        modelsEndpoint = "/v1/models",
        apiKeyUrl = "",
        category = ProviderCategory.CUSTOM
    );

    companion object {
        /**
         * Base URLs of providers removed in the catalog trim. Persisted ApiKeyConfigs
         * referencing a removed provider migrate to CUSTOM with this base URL so the
         * user's endpoint/key setup keeps working (see AiConfigManager).
         */
        val REMOVED_PROVIDER_BASE_URLS: Map<String, String> = mapOf(
            "MISTRAL" to "https://api.mistral.ai",
            "COHERE" to "https://api.cohere.com",
            "AI21" to "https://api.ai21.com",
            "GROQ" to "https://api.groq.com/openai",
            "CEREBRAS" to "https://api.cerebras.ai",
            "SAMBANOVA" to "https://api.sambanova.ai",
            "DEEPINFRA" to "https://api.deepinfra.com",
            "NOVITA" to "https://api.novita.ai",
            "VOLCANO" to "https://ark.cn-beijing.volces.com/api",
            "MOONSHOT" to "https://api.moonshot.cn",
            "MINIMAX" to "https://api.minimax.chat",
            "SILICONFLOW" to "https://api.siliconflow.cn",
            "BAICHUAN" to "https://api.baichuan-ai.com",
            "YI" to "https://api.lingyiwanwu.com",
            "STEPFUN" to "https://api.stepfun.com",
            "HUNYUAN" to "https://api.hunyuan.cloud.tencent.com",
            "SPARK" to "https://spark-api-open.xf-yun.com"
        )
    }

    val displayName: String get() = when (this) {
        GOOGLE -> Strings.providerGoogle
        OPENROUTER -> Strings.providerOpenRouter
        OPENAI -> Strings.providerOpenAI
        ANTHROPIC -> Strings.providerAnthropic
        GROK -> Strings.providerGrok
        TOGETHER -> Strings.providerTogether
        PERPLEXITY -> Strings.providerPerplexity
        FIREWORKS -> Strings.providerFireworks
        DEEPSEEK -> Strings.providerDeepSeek
        QWEN -> Strings.providerQwen
        GLM -> Strings.providerGLM
        OLLAMA -> Strings.providerOllama
        LM_STUDIO -> Strings.providerLmStudio
        VLLM -> Strings.providerVllm
        CUSTOM -> Strings.providerCustom
    }

    val description: String get() = when (this) {
        GOOGLE -> Strings.providerGoogleDesc
        OPENROUTER -> Strings.providerOpenRouterDesc
        OPENAI -> Strings.providerOpenAIDesc
        ANTHROPIC -> Strings.providerAnthropicDesc
        GROK -> Strings.providerGrokDesc
        TOGETHER -> Strings.providerTogetherDesc
        PERPLEXITY -> Strings.providerPerplexityDesc
        FIREWORKS -> Strings.providerFireworksDesc
        DEEPSEEK -> Strings.providerDeepSeekDesc
        QWEN -> Strings.providerQwenDesc
        GLM -> Strings.providerGLMDesc
        OLLAMA -> Strings.providerOllamaDesc
        LM_STUDIO -> Strings.providerLmStudioDesc
        VLLM -> Strings.providerVllmDesc
        CUSTOM -> Strings.providerCustomDesc
    }

    val pricing: String get() = when (this) {
        GOOGLE -> Strings.providerGooglePricing
        OPENROUTER -> Strings.providerOpenRouterPricing
        OPENAI -> Strings.providerOpenAIPricing
        ANTHROPIC -> Strings.providerAnthropicPricing
        GROK -> Strings.providerGrokPricing
        TOGETHER -> Strings.providerTogetherPricing
        PERPLEXITY -> Strings.providerPerplexityPricing
        FIREWORKS -> Strings.providerFireworksPricing
        DEEPSEEK -> Strings.providerDeepSeekPricing
        QWEN -> Strings.providerQwenPricing
        GLM -> Strings.providerGLMPricing
        OLLAMA -> Strings.providerOllamaPricing
        LM_STUDIO -> Strings.providerLmStudioPricing
        VLLM -> Strings.providerVllmPricing
        CUSTOM -> Strings.providerCustomPricing
    }

    val requiresApiKey: Boolean get() = when (this) {
        OLLAMA, LM_STUDIO, VLLM -> false
        else -> true
    }

    val allowCustomBaseUrl: Boolean get() = when (this) {
        CUSTOM, OLLAMA, LM_STUDIO, VLLM -> true
        else -> false
    }
}

enum class AiFeature(
    val icon: String,
    val defaultCapabilities: List<ModelCapability> = emptyList()
) {
    @SerializedName("AI_CODING")
    AGENT("Code", listOf(ModelCapability.TEXT, ModelCapability.MULTIMODAL)),
    @SerializedName("AI_CODING_IMAGE")
    AGENT_IMAGE("Image", listOf(ModelCapability.IMAGE_GENERATION)),
    ICON_GENERATION("AutoAwesome", listOf(ModelCapability.IMAGE_GENERATION)),
    MODULE_DEVELOPMENT("Extension", listOf(ModelCapability.TEXT, ModelCapability.MULTIMODAL)),
    LRC_GENERATION("MusicNote", listOf(ModelCapability.TEXT, ModelCapability.MULTIMODAL)),
    TRANSLATION("Translate", listOf(ModelCapability.TEXT, ModelCapability.MULTIMODAL)),
    GENERAL("Chat", listOf(ModelCapability.TEXT, ModelCapability.MULTIMODAL));

    val displayName: String get() = when (this) {
        AGENT -> Strings.featureAgent
        AGENT_IMAGE -> Strings.featureAgentImage
        ICON_GENERATION -> Strings.featureIconGen
        MODULE_DEVELOPMENT -> Strings.featureModuleDev
        LRC_GENERATION -> Strings.featureLrcGen
        TRANSLATION -> Strings.featureTranslate
        GENERAL -> Strings.featureGeneral
    }

    val description: String get() = when (this) {
        AGENT -> Strings.featureAgentDesc
        AGENT_IMAGE -> Strings.featureAgentImageDesc
        ICON_GENERATION -> Strings.featureIconGenDesc
        MODULE_DEVELOPMENT -> Strings.featureModuleDevDesc
        LRC_GENERATION -> Strings.featureLrcGenDesc
        TRANSLATION -> Strings.featureTranslateDesc
        GENERAL -> Strings.featureGeneralDesc
    }
}

enum class ModelCapability {
    TEXT, MULTIMODAL, IMAGE_GENERATION;

    val displayName: String get() = when (this) {
        TEXT -> Strings.capabilityText
        MULTIMODAL -> Strings.capabilityMultimodal
        IMAGE_GENERATION -> Strings.capabilityImageGen
    }

    val description: String get() = when (this) {
        TEXT -> Strings.capabilityTextDesc
        MULTIMODAL -> Strings.capabilityMultimodalDesc
        IMAGE_GENERATION -> Strings.capabilityImageGenDesc
    }
}

fun ModelCapability.getLocalizedDisplayName(): String {
    return when (this) {
        ModelCapability.TEXT -> com.webtoapp.core.i18n.Strings.textGeneration
        ModelCapability.MULTIMODAL -> com.webtoapp.core.i18n.Strings.multimodalModel
        ModelCapability.IMAGE_GENERATION -> com.webtoapp.core.i18n.Strings.imageGeneration
    }
}

fun ModelCapability.getLocalizedDescription(): String {
    return when (this) {
        ModelCapability.TEXT -> com.webtoapp.core.i18n.Strings.basicTextDialogue
        ModelCapability.MULTIMODAL -> com.webtoapp.core.i18n.Strings.multimodalModelDesc
        ModelCapability.IMAGE_GENERATION -> com.webtoapp.core.i18n.Strings.generateImages
    }
}

data class CapabilityFeatureMapping(
    val capability: ModelCapability,
    val enabledFeatures: Set<AiFeature>
) {
    companion object {

        fun getDefaultMappings(): List<CapabilityFeatureMapping> {
            return ModelCapability.entries.map { capability ->
                CapabilityFeatureMapping(
                    capability = capability,
                    enabledFeatures = AiFeature.entries.filter { feature ->
                        feature.defaultCapabilities.contains(capability)
                    }.toSet()
                )
            }
        }
    }
}

data class AiModel(
    val id: String,
    val name: String,
    val provider: AiProvider,
    val capabilities: List<ModelCapability> = listOf(ModelCapability.TEXT),
    val contextLength: Int = 4096,
    val inputPrice: Double = 0.0,
    val outputPrice: Double = 0.0,
    val isCustom: Boolean = false
)

enum class ApiFormat {
    OPENAI_COMPATIBLE,
    ANTHROPIC,
    OPENAI_RESPONSES,
    GOOGLE_GEMINI;

    val displayName: String get() = when (this) {
        OPENAI_COMPATIBLE -> "Chat Completions (/chat/completions)"
        ANTHROPIC -> "Anthropic Messages (/v1/messages)"
        OPENAI_RESPONSES -> "Responses (/responses)"
        GOOGLE_GEMINI -> "Google Gemini"
    }
}

data class ApiKeyConfig(
    val id: String = java.util.UUID.randomUUID().toString(),
    val provider: AiProvider,
    val apiKey: String,
    val baseUrl: String? = null,
    val customModelsEndpoint: String? = null,
    val customChatEndpoint: String? = null,
    val apiFormat: ApiFormat = ApiFormat.OPENAI_COMPATIBLE,
    val alias: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {

    val displayName: String get() = alias?.takeIf { it.isNotBlank() } ?: provider.displayName

    fun getEffectiveModelsEndpoint(): String {
        return customModelsEndpoint?.takeIf { it.isNotBlank() } ?: provider.modelsEndpoint
    }

    fun getEffectiveChatEndpoint(): String {
        customChatEndpoint?.takeIf { it.isNotBlank() }?.let { return it }
        return when (provider) {
            AiProvider.ANTHROPIC -> "/v1/messages"
            AiProvider.GOOGLE -> "/v1beta/models"
            AiProvider.OLLAMA -> "/api/chat"
            AiProvider.CUSTOM -> when (apiFormat) {
                ApiFormat.ANTHROPIC -> "/v1/messages"
                ApiFormat.OPENAI_RESPONSES -> "/v1/responses"
                else -> "/v1/chat/completions"
            }
            else -> "/v1/chat/completions"
        }
    }
}

data class SavedModel(
    val id: String = java.util.UUID.randomUUID().toString(),
    val model: AiModel,
    val apiKeyId: String,
    val alias: String? = null,
    val capabilities: List<ModelCapability>,
    val featureMappings: Map<ModelCapability, Set<AiFeature>> = emptyMap(),
    val isDefault: Boolean = false,
    val userContextLength: Int? = null,
    val createdAt: Long = System.currentTimeMillis()
) {

    val effectiveContextLength: Int get() = userContextLength ?: model.contextLength

    fun getSupportedFeatures(): Set<AiFeature> {
        val features = mutableSetOf<AiFeature>()
        capabilities.forEach { capability ->

            val mappedFeatures = featureMappings[capability]
                ?: AiFeature.entries.filter { it.defaultCapabilities.contains(capability) }.toSet()
            features.addAll(mappedFeatures)
        }
        return features
    }

    fun supportsFeature(feature: AiFeature): Boolean {
        return getSupportedFeatures().contains(feature)
    }

    fun getFeaturesForCapability(capability: ModelCapability): Set<AiFeature> {
        return featureMappings[capability]
            ?: AiFeature.entries.filter { it.defaultCapabilities.contains(capability) }.toSet()
    }
}

data class AiSettings(
    val apiKeys: List<ApiKeyConfig> = emptyList(),
    val savedModels: List<SavedModel> = emptyList(),
    val defaultModelId: String? = null
)

enum class LrcTaskStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED
}

data class LrcTask(
    val id: String = java.util.UUID.randomUUID().toString(),
    val bgmItemId: String,
    val bgmName: String,
    val bgmPath: String,
    val modelId: String,
    val status: LrcTaskStatus = LrcTaskStatus.PENDING,
    val progress: Int = 0,
    val resultLrc: LrcData? = null,
    val errorMessage: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

object PresetLrcThemes {
    val themes = listOf(
        LrcTheme(
            id = "default",
            name = Strings.lrcThemeDefault,
            textColor = "#FFFFFF",
            highlightColor = "#FFD700",
            backgroundColor = "#80000000",
            animationType = LrcAnimationType.FADE
        ),
        LrcTheme(
            id = "karaoke",
            name = Strings.lrcThemeKaraoke,
            textColor = "#FFFFFF",
            highlightColor = "#FF4081",
            backgroundColor = "#00000000",
            strokeColor = "#000000",
            strokeWidth = 2f,
            animationType = LrcAnimationType.KARAOKE
        ),
        LrcTheme(
            id = "neon",
            name = Strings.lrcThemeNeon,
            textColor = "#00FFFF",
            highlightColor = "#FF00FF",
            backgroundColor = "#40000000",
            shadowEnabled = true,
            animationType = LrcAnimationType.FADE
        ),
        LrcTheme(
            id = "minimal",
            name = Strings.lrcThemeMinimal,
            fontSize = 16f,
            textColor = "#CCCCCC",
            highlightColor = "#FFFFFF",
            backgroundColor = "#00000000",
            animationType = LrcAnimationType.SLIDE_UP
        ),
        LrcTheme(
            id = "classic",
            name = Strings.lrcThemeClassic,
            fontSize = 20f,
            textColor = "#FFE4B5",
            highlightColor = "#FFD700",
            backgroundColor = "#60000000",
            animationType = LrcAnimationType.TYPEWRITER
        ),
        LrcTheme(
            id = "dark",
            name = Strings.lrcThemeDark,
            textColor = "#AAAAAA",
            highlightColor = "#4FC3F7",
            backgroundColor = "#E0000000",
            animationType = LrcAnimationType.SCALE
        ),
        LrcTheme(
            id = "romantic",
            name = Strings.lrcThemeRomantic,
            textColor = "#FFB6C1",
            highlightColor = "#FF69B4",
            backgroundColor = "#40000000",
            animationType = LrcAnimationType.FADE
        ),
        LrcTheme(
            id = "energetic",
            name = Strings.lrcThemeEnergetic,
            fontSize = 22f,
            textColor = "#FFEB3B",
            highlightColor = "#FF5722",
            backgroundColor = "#00000000",
            strokeColor = "#000000",
            strokeWidth = 3f,
            animationType = LrcAnimationType.SCALE
        )
    )

    fun getById(id: String): LrcTheme? = themes.find { it.id == id }
}

fun AiProvider.getLocalizedDisplayName(): String = displayName

fun AiFeature.getLocalizedDisplayName(): String {
    return when (this) {
        AiFeature.AGENT -> "HTML ${com.webtoapp.core.i18n.Strings.coding}"
        AiFeature.AGENT_IMAGE -> "HTML ${com.webtoapp.core.i18n.Strings.coding} (${com.webtoapp.core.i18n.Strings.image})"
        AiFeature.ICON_GENERATION -> com.webtoapp.core.i18n.Strings.featureIconGeneration
        AiFeature.MODULE_DEVELOPMENT -> com.webtoapp.core.i18n.Strings.featureModuleDevelopment
        AiFeature.LRC_GENERATION -> com.webtoapp.core.i18n.Strings.featureLrcGeneration
        AiFeature.TRANSLATION -> com.webtoapp.core.i18n.Strings.featureTranslation
        AiFeature.GENERAL -> com.webtoapp.core.i18n.Strings.featureGeneralChat
    }
}

fun AiFeature.getLocalizedDescription(): String {
    return when (this) {
        AiFeature.AGENT -> Strings.agentDesc
        AiFeature.AGENT_IMAGE -> Strings.agentImageDesc
        AiFeature.ICON_GENERATION -> Strings.iconGenerationDesc
        AiFeature.MODULE_DEVELOPMENT -> Strings.moduleDevelopmentDesc
        AiFeature.LRC_GENERATION -> Strings.lrcGenerationDesc
        AiFeature.TRANSLATION -> Strings.translationDesc
        AiFeature.GENERAL -> Strings.generalChatDesc
    }
}

fun AiProvider.getLocalizedDescription(): String = description

fun AiProvider.getLocalizedPricing(): String = pricing
