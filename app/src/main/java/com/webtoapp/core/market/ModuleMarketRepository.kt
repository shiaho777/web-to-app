package com.webtoapp.core.market

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.webtoapp.BuildConfig
import com.webtoapp.core.extension.ConfigItemType
import com.webtoapp.core.extension.ExtensionManager
import com.webtoapp.core.extension.ExtensionModule
import com.webtoapp.core.extension.ModuleCategory
import com.webtoapp.core.extension.ModuleConfigItem
import com.webtoapp.core.extension.ModulePermission
import com.webtoapp.core.extension.ModuleRunTime
import com.webtoapp.core.extension.ModuleSourceType
import com.webtoapp.core.extension.ModuleVersion
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.core.network.NetworkModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.File
import java.io.IOException

class ModuleMarketRepository private constructor(
    private val context: Context,
    private val extensionManager: ExtensionManager
) {

    companion object {
        private const val TAG = "ModuleMarket"

        private const val OWNER = "shiahonb777"
        private const val REPO = "web-to-app"
        private const val BRANCH = "main"
        private const val MODULES_DIR = "modules"

        private const val REGISTRY_TTL_MS = 60 * 60 * 1000L

        private const val CACHE_DIR_NAME = "module_market"
        private const val REGISTRY_CACHE_FILE = "registry.json"
        private const val SUBMISSIONS_CACHE_FILE = "submissions.json"

        @Volatile
        private var INSTANCE: ModuleMarketRepository? = null

        fun getInstance(context: Context, extensionManager: ExtensionManager): ModuleMarketRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ModuleMarketRepository(context.applicationContext, extensionManager).also { INSTANCE = it }
            }
        }

        private val SOURCES: List<String> = listOf(
            "https://raw.githubusercontent.com/$OWNER/$REPO/$BRANCH/$MODULES_DIR",

            "https://cdn.jsdelivr.net/gh/$OWNER/$REPO@$BRANCH/$MODULES_DIR"
        )
    }

    private val gson: Gson = GsonBuilder().setLenient().create()
    private val httpClient = NetworkModule.defaultClient

    private val cacheDir: File by lazy {
        File(context.cacheDir, CACHE_DIR_NAME).apply { mkdirs() }
    }

    private val _state = MutableStateFlow<MarketState>(MarketState.Idle)
    val state: StateFlow<MarketState> = _state.asStateFlow()

    val views: kotlinx.coroutines.flow.Flow<List<MarketModuleView>> =
        combine(_state, extensionManager.modules, extensionManager.builtInModules) { st, user, builtIn ->
            val loaded = st as? MarketState.Loaded
            val entries = loaded?.entries ?: emptyList()
            val submissions = loaded?.submissions ?: emptyMap()
            val installed = (user + builtIn).associateBy { it.id }
            entries.mapNotNull { entry ->

                val submission = submissions[entry.id] ?: return@mapNotNull null

                val local = installed[entry.id]
                if (local == null) {
                    MarketModuleView(entry, MarketInstallState.NotInstalled, null, submission)
                } else {
                    val cmp = compareSemver(entry.version, local.version.name)
                    val state = if (cmp > 0) MarketInstallState.UpdateAvailable else MarketInstallState.UpToDate
                    MarketModuleView(entry, state, local.version.name, submission)
                }
            }
        }

    suspend fun refresh(force: Boolean = false) = withContext(Dispatchers.IO) {
        val cachedRegistry = readCachedRegistry()
        val cachedSubmissions = readCachedSubmissions()
        if (!force && cachedRegistry != null && cachedSubmissions != null && cacheAgeMs() < REGISTRY_TTL_MS) {
            _state.value = MarketState.Loaded(
                entries = filterEntries(cachedRegistry.modules),
                submissions = cachedSubmissions.submissions,
                fromCache = true
            )
            return@withContext
        }

        _state.value = MarketState.Loading
        val rawRegistry = fetchRaw("registry.json")

        val rawSubmissions = fetchRaw("submissions.json")

        if (rawRegistry == null) {
            if (cachedRegistry != null && cachedSubmissions != null) {
                _state.value = MarketState.Loaded(
                    entries = filterEntries(cachedRegistry.modules),
                    submissions = cachedSubmissions.submissions,
                    fromCache = true
                )
            } else {
                _state.value = MarketState.Error("Could not reach the module market. Check your network and try again.")
            }
            return@withContext
        }

        val parsedRegistry = try {
            gson.fromJson(rawRegistry, ModuleMarketRegistry::class.java)
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to parse registry.json", e)
            null
        }

        if (parsedRegistry == null) {
            _state.value = MarketState.Error("Module registry was malformed.")
            return@withContext
        }

        val parsedSubmissions: ModuleSubmissionsRegistry = if (rawSubmissions != null) {
            try {
                gson.fromJson(rawSubmissions, ModuleSubmissionsRegistry::class.java)
                    ?: ModuleSubmissionsRegistry()
            } catch (e: Exception) {
                AppLogger.e(TAG, "Failed to parse submissions.json", e)
                cachedSubmissions ?: ModuleSubmissionsRegistry()
            }
        } else {

            cachedSubmissions ?: ModuleSubmissionsRegistry()
        }

        writeCachedRegistry(rawRegistry)
        if (rawSubmissions != null) writeCachedSubmissions(rawSubmissions)

        _state.value = MarketState.Loaded(
            entries = filterEntries(parsedRegistry.modules),
            submissions = parsedSubmissions.submissions,
            fromCache = false
        )
    }

    suspend fun install(entry: ModuleMarketEntry): Result<ExtensionModule> = withContext(Dispatchers.IO) {
        try {
            val manifestRaw = fetchRaw("${entry.path}/module.json")
                ?: return@withContext Result.failure(IOException("module.json download failed"))
            val mainJs = fetchRaw("${entry.path}/main.js")
                ?: return@withContext Result.failure(IOException("main.js download failed"))
            val styleCss = if (entry.hasCss) fetchRaw("${entry.path}/style.css").orEmpty() else ""

            val manifest = try {
                gson.fromJson(manifestRaw, RemoteManifest::class.java)
            } catch (e: Exception) {
                return@withContext Result.failure(IllegalStateException("module.json is malformed", e))
            }

            val effectiveId = manifest.id?.takeIf { it.isNotBlank() } ?: entry.id
            val existing = extensionManager.getAllModules().firstOrNull { it.id == effectiveId }
            val preservedConfig: Map<String, String> = if (existing != null) {
                val newKeys = manifest.configItems.map { it.key }.toSet()
                existing.configValues.filterKeys { it in newKeys }
            } else {
                emptyMap()
            }

            val module = manifest.toExtensionModule(
                fallbackId = entry.id,
                fallbackName = entry.name,
                code = mainJs,
                cssCode = styleCss,
                preservedConfig = preservedConfig
            )

            extensionManager.addModule(module)
        } catch (e: Exception) {
            AppLogger.e(TAG, "Install failed for ${entry.id}", e)
            Result.failure(e)
        }
    }

    fun githubUrl(entry: ModuleMarketEntry): String =
        "https://github.com/$OWNER/$REPO/tree/$BRANCH/$MODULES_DIR/${entry.path}"

    fun resolveIconUrl(entry: ModuleMarketEntry): String? {
        val raw = entry.iconUrl?.trim().orEmpty()
        if (raw.isEmpty()) return null
        if (raw.startsWith("https://") || raw.startsWith("http://")) return raw

        val normalised = raw.removePrefix("./").removePrefix("/")
        return "${SOURCES.first()}/${entry.path}/$normalised"
    }

    val contributingUrl: String =
        "https://github.com/$OWNER/$REPO/blob/$BRANCH/$MODULES_DIR/README.md"

    private fun fetchRaw(relativePath: String): String? {
        for (base in SOURCES) {
            val url = "$base/$relativePath"
            try {
                val req = Request.Builder()
                    .url(url)
                    .header("User-Agent", "WebToApp/${BuildConfig.VERSION_NAME}")
                    .get()
                    .build()
                httpClient.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful) {
                        return resp.body?.string()
                    }
                    AppLogger.w(TAG, "fetch $url -> HTTP ${resp.code}")
                }
            } catch (e: Exception) {
                AppLogger.w(TAG, "fetch $url failed: ${e.message}")
            }
        }
        return null
    }

    private fun readCachedRegistry(): ModuleMarketRegistry? {
        val file = File(cacheDir, REGISTRY_CACHE_FILE)
        if (!file.exists()) return null
        return try {
            gson.fromJson(file.readText(), ModuleMarketRegistry::class.java)
        } catch (e: Exception) {
            null
        }
    }

    private fun writeCachedRegistry(raw: String) {
        try {
            File(cacheDir, REGISTRY_CACHE_FILE).writeText(raw)
        } catch (e: Exception) {
            AppLogger.w(TAG, "Failed to write registry cache: ${e.message}")
        }
    }

    private fun readCachedSubmissions(): ModuleSubmissionsRegistry? {
        val file = File(cacheDir, SUBMISSIONS_CACHE_FILE)
        if (!file.exists()) return null
        return try {
            gson.fromJson(file.readText(), ModuleSubmissionsRegistry::class.java)
        } catch (e: Exception) {
            null
        }
    }

    private fun writeCachedSubmissions(raw: String) {
        try {
            File(cacheDir, SUBMISSIONS_CACHE_FILE).writeText(raw)
        } catch (e: Exception) {
            AppLogger.w(TAG, "Failed to write submissions cache: ${e.message}")
        }
    }

    private fun cacheAgeMs(): Long {
        val file = File(cacheDir, REGISTRY_CACHE_FILE)
        if (!file.exists()) return Long.MAX_VALUE
        return System.currentTimeMillis() - file.lastModified()
    }

    private fun filterEntries(entries: List<ModuleMarketEntry>): List<ModuleMarketEntry> {

        return entries.filter { it.minAppVersion <= BuildConfig.VERSION_CODE }
    }

    private data class RemoteManifest(
        val id: String? = null,
        val name: String? = null,
        val description: String? = null,
        val icon: String? = null,
        val category: String? = null,
        val tags: List<String> = emptyList(),
        val version: ModuleVersion? = null,
        val author: com.webtoapp.core.extension.ModuleAuthor? = null,
        val runAt: String? = null,
        val urlMatches: List<com.webtoapp.core.extension.UrlMatchRule> = emptyList(),
        val permissions: List<String> = emptyList(),
        val configItems: List<ModuleConfigItem> = emptyList()
    ) {
        fun toExtensionModule(
            fallbackId: String,
            fallbackName: String,
            code: String,
            cssCode: String,
            preservedConfig: Map<String, String> = emptyMap()
        ): ExtensionModule {
            return ExtensionModule(
                id = id?.takeIf { it.isNotBlank() } ?: fallbackId,
                name = name?.takeIf { it.isNotBlank() } ?: fallbackName,
                description = description.orEmpty(),
                icon = icon ?: "package",
                category = parseEnum(category, ModuleCategory.OTHER),
                tags = tags,
                version = version ?: ModuleVersion(),
                author = author,
                code = code,
                cssCode = cssCode,
                runAt = parseEnum(runAt, ModuleRunTime.DOCUMENT_END),
                urlMatches = urlMatches,
                permissions = permissions.mapNotNull { p ->
                    runCatching { ModulePermission.valueOf(p) }.getOrNull()
                },
                configItems = configItems,
                configValues = preservedConfig,
                enabled = true,
                builtIn = false,
                sourceType = ModuleSourceType.CUSTOM
            )
        }

        private inline fun <reified T : Enum<T>> parseEnum(value: String?, default: T): T {
            if (value.isNullOrBlank()) return default
            return runCatching { enumValueOf<T>(value) }.getOrElse { default }
        }
    }
}

sealed class MarketState {
    object Idle : MarketState()
    object Loading : MarketState()
    data class Loaded(
        val entries: List<ModuleMarketEntry>,
        val submissions: Map<String, ModuleSubmission>,
        val fromCache: Boolean
    ) : MarketState()
    data class Error(val message: String) : MarketState()
}

internal fun compareSemver(a: String, b: String): Int {
    val ap = a.split(".").mapNotNull { it.toIntOrNull() }
    val bp = b.split(".").mapNotNull { it.toIntOrNull() }
    if (ap.isEmpty() || bp.isEmpty()) return a.compareTo(b)
    val len = maxOf(ap.size, bp.size)
    for (i in 0 until len) {
        val av = ap.getOrElse(i) { 0 }
        val bv = bp.getOrElse(i) { 0 }
        if (av != bv) return av - bv
    }
    return 0
}

@Suppress("unused")
private val configItemTypeAnchor: Class<ConfigItemType> = ConfigItemType::class.java
