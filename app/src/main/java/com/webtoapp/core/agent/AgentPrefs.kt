package com.webtoapp.core.agent

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.agentPrefsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "aicoding_prefs"
)

class AgentPrefs(private val context: Context) {

    val autoApproveFlow: Flow<Boolean> = context.agentPrefsDataStore.data.map { prefs ->
        prefs[KEY_AUTO_APPROVE] ?: false
    }

    suspend fun setAutoApprove(enabled: Boolean) {
        context.agentPrefsDataStore.edit { it[KEY_AUTO_APPROVE] = enabled }
    }

    companion object {
        private val KEY_AUTO_APPROVE = booleanPreferencesKey("auto_approve")
    }
}
