package com.webtoapp.core.host

import android.annotation.SuppressLint
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking

private val Context.hostRuntimeDataStore: DataStore<Preferences> by preferencesDataStore(name = "host_runtime_prefs")

@SuppressLint("StaticFieldLeak")
class HostRuntimePrefs private constructor(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    @Volatile
    private var cachedSeparateTasks: Boolean? = null

    val separateTasksFlow: StateFlow<Boolean> = context.hostRuntimeDataStore.data.map { prefs ->
        prefs[KEY_SEPARATE_TASKS] ?: false
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = false
    )

    fun isSeparateTasksEnabledBlocking(): Boolean {
        cachedSeparateTasks?.let { return it }
        return try {
            runBlocking {
                val value = context.hostRuntimeDataStore.data.first()[KEY_SEPARATE_TASKS] ?: false
                cachedSeparateTasks = value
                value
            }
        } catch (_: Exception) {
            separateTasksFlow.value
        }
    }

    suspend fun setSeparateTasksEnabled(enabled: Boolean) {
        context.hostRuntimeDataStore.edit { prefs ->
            prefs[KEY_SEPARATE_TASKS] = enabled
        }
        cachedSeparateTasks = enabled
    }

    companion object {
        private val KEY_SEPARATE_TASKS = booleanPreferencesKey("webapp_separate_tasks")

        @Volatile
        private var instance: HostRuntimePrefs? = null

        fun getInstance(context: Context): HostRuntimePrefs {
            return instance ?: synchronized(this) {
                instance ?: HostRuntimePrefs(context.applicationContext).also { instance = it }
            }
        }
    }
}
