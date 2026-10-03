package com.webtoapp.core.host

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.security.SecureRandom
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

private val Context.hostRuntimeDataStore: DataStore<Preferences> by preferencesDataStore(name = "host_runtime_prefs")

data class McpSettings(
    val enabled: Boolean,
    val token: String,
    val port: Int
)

/**
 * Host-only runtime switches. Not shell-synced: generated APKs never read this.
 *
 * [KEY_SEPARATE_TASKS] is the About-screen "WebApp 独立任务" toggle. When on,
 * home preview and desktop shortcuts open each WebApp as its own recents task.
 *
 * [KEY_MCP_ENABLED] is the About-screen local MCP switch. The token and port
 * travel with it so an external agent can be pointed at 127.0.0.1.
 *
 * [KEY_ADVANCED_FEATURES] reveals Node.js, PHP, Python, Go, WordPress, and
 * image/video. Off by default. The runtimes stay in the app either way.
 */
@SuppressLint("StaticFieldLeak")
class HostRuntimePrefs private constructor(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    @Volatile
    private var cachedSeparateTasks: Boolean? = null

    @Volatile
    private var cachedMcp: McpSettings? = null

    @Volatile
    private var cachedAdvancedFeatures: Boolean? = null

    val separateTasksFlow: StateFlow<Boolean> = context.hostRuntimeDataStore.data.map { prefs ->
        prefs[KEY_SEPARATE_TASKS] ?: false
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = false
    )

    val mcpFlow: StateFlow<McpSettings> = context.hostRuntimeDataStore.data.map { prefs ->
        prefs.toMcpSettings()
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = McpSettings(enabled = false, token = "", port = DEFAULT_MCP_PORT)
    )

    val advancedFeaturesFlow: StateFlow<Boolean> = context.hostRuntimeDataStore.data.map { prefs ->
        prefs[KEY_ADVANCED_FEATURES] ?: false
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = false
    )

    init {
        scope.launch {
            separateTasksFlow.collect { cachedSeparateTasks = it }
        }
        scope.launch {
            mcpFlow.collect { cachedMcp = it }
        }
        scope.launch {
            advancedFeaturesFlow.collect { cachedAdvancedFeatures = it }
        }
    }

    /**
     * Launch paths call this on the main thread. DataStore's `first()` must not
     * be `runBlocking`'d there (it deadlocks against the main-dispatcher read).
     * The eagerly collected flow is the main-thread source of truth.
     */
    fun isSeparateTasksEnabledBlocking(): Boolean {
        cachedSeparateTasks?.let { return it }
        return if (Looper.myLooper() == Looper.getMainLooper()) {
            separateTasksFlow.value.also { cachedSeparateTasks = it }
        } else {
            try {
                runBlocking {
                    val value = context.hostRuntimeDataStore.data.first()[KEY_SEPARATE_TASKS] ?: false
                    cachedSeparateTasks = value
                    value
                }
            } catch (_: Exception) {
                separateTasksFlow.value.also { cachedSeparateTasks = it }
            }
        }
    }

    suspend fun setSeparateTasksEnabled(enabled: Boolean) {
        context.hostRuntimeDataStore.edit { prefs ->
            prefs[KEY_SEPARATE_TASKS] = enabled
        }
        cachedSeparateTasks = enabled
    }

    fun isAdvancedFeaturesEnabledBlocking(): Boolean {
        cachedAdvancedFeatures?.let { return it }
        return if (Looper.myLooper() == Looper.getMainLooper()) {
            advancedFeaturesFlow.value.also { cachedAdvancedFeatures = it }
        } else {
            try {
                runBlocking {
                    val value = context.hostRuntimeDataStore.data.first()[KEY_ADVANCED_FEATURES] ?: false
                    cachedAdvancedFeatures = value
                    value
                }
            } catch (_: Exception) {
                advancedFeaturesFlow.value.also { cachedAdvancedFeatures = it }
            }
        }
    }

    suspend fun setAdvancedFeaturesEnabled(enabled: Boolean) {
        context.hostRuntimeDataStore.edit { prefs ->
            prefs[KEY_ADVANCED_FEATURES] = enabled
        }
        cachedAdvancedFeatures = enabled
    }

    fun currentMcpBlocking(): McpSettings {
        cachedMcp?.let { return it }
        return if (Looper.myLooper() == Looper.getMainLooper()) {
            mcpFlow.value.also { cachedMcp = it }
        } else {
            try {
                runBlocking {
                    context.hostRuntimeDataStore.data.first().toMcpSettings().also { cachedMcp = it }
                }
            } catch (_: Exception) {
                mcpFlow.value.also { cachedMcp = it }
            }
        }
    }

    suspend fun setMcpEnabled(enabled: Boolean): McpSettings {
        var written: McpSettings? = null
        context.hostRuntimeDataStore.edit { prefs ->
            prefs[KEY_MCP_ENABLED] = enabled
            if (enabled && prefs[KEY_MCP_TOKEN].isNullOrBlank()) {
                prefs[KEY_MCP_TOKEN] = newToken()
            }
            if (enabled && (prefs[KEY_MCP_PORT] ?: 0) <= 0) {
                prefs[KEY_MCP_PORT] = DEFAULT_MCP_PORT
            }
            written = prefs.toMcpSettings()
        }
        return (written ?: currentMcpBlocking()).also { cachedMcp = it }
    }

    suspend fun rotateMcpToken(): McpSettings {
        var written: McpSettings? = null
        context.hostRuntimeDataStore.edit { prefs ->
            prefs[KEY_MCP_TOKEN] = newToken()
            written = prefs.toMcpSettings()
        }
        return (written ?: currentMcpBlocking()).also { cachedMcp = it }
    }

    suspend fun setMcpPort(port: Int): McpSettings {
        var written: McpSettings? = null
        context.hostRuntimeDataStore.edit { prefs ->
            prefs[KEY_MCP_PORT] = port
            written = prefs.toMcpSettings()
        }
        return (written ?: currentMcpBlocking()).also { cachedMcp = it }
    }

    private fun Preferences.toMcpSettings(): McpSettings = McpSettings(
        enabled = this[KEY_MCP_ENABLED] ?: false,
        token = this[KEY_MCP_TOKEN].orEmpty(),
        port = this[KEY_MCP_PORT]?.takeIf { it > 0 } ?: DEFAULT_MCP_PORT
    )

    companion object {
        const val DEFAULT_MCP_PORT = 17321

        private val KEY_SEPARATE_TASKS = booleanPreferencesKey("webapp_separate_tasks")
        private val KEY_ADVANCED_FEATURES = booleanPreferencesKey("advanced_features")
        private val KEY_MCP_ENABLED = booleanPreferencesKey("host_mcp_enabled")
        private val KEY_MCP_TOKEN = stringPreferencesKey("host_mcp_token")
        private val KEY_MCP_PORT = intPreferencesKey("host_mcp_port")

        private fun newToken(): String {
            val bytes = ByteArray(24)
            SecureRandom().nextBytes(bytes)
            return bytes.joinToString("") { "%02x".format(it) }
        }

        @Volatile
        private var instance: HostRuntimePrefs? = null

        fun getInstance(context: Context): HostRuntimePrefs {
            return instance ?: synchronized(this) {
                instance ?: HostRuntimePrefs(context.applicationContext).also { instance = it }
            }
        }
    }
}
