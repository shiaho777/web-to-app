package com.webtoapp.core.aicoding

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.aiCodingPrefsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "aicoding_prefs"
)

class AiCodingPrefs(private val context: Context) {

    val autoApproveFlow: Flow<Boolean> = context.aiCodingPrefsDataStore.data.map { prefs ->
        prefs[KEY_AUTO_APPROVE] ?: false
    }

    suspend fun setAutoApprove(enabled: Boolean) {
        context.aiCodingPrefsDataStore.edit { it[KEY_AUTO_APPROVE] = enabled }
    }

    companion object {
        private val KEY_AUTO_APPROVE = booleanPreferencesKey("auto_approve")
    }
}
