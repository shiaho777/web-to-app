package com.webtoapp.core.update

import android.annotation.SuppressLint
import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.updatePrefsDataStore by preferencesDataStore(name = "update_settings")

@SuppressLint("StaticFieldLeak")
class UpdatePrefs(private val context: Context) {

    companion object {
        private val AUTO_CHECK_KEY = booleanPreferencesKey("auto_check_updates")
        private val SKIPPED_VERSION_KEY = stringPreferencesKey("skipped_version")
        private val LAST_SEEN_VERSION_CODE_KEY = longPreferencesKey("last_seen_version_code")

        @Volatile
        private var instance: UpdatePrefs? = null

        fun getInstance(context: Context): UpdatePrefs {
            return instance ?: synchronized(this) {
                instance ?: UpdatePrefs(context.applicationContext).also { instance = it }
            }
        }
    }

    val autoCheckFlow: Flow<Boolean> = context.updatePrefsDataStore.data.map { prefs ->
        prefs[AUTO_CHECK_KEY] ?: true
    }

    val skippedVersionFlow: Flow<String> = context.updatePrefsDataStore.data.map { prefs ->
        prefs[SKIPPED_VERSION_KEY] ?: ""
    }

    val lastSeenVersionCodeFlow: Flow<Long> = context.updatePrefsDataStore.data.map { prefs ->
        prefs[LAST_SEEN_VERSION_CODE_KEY] ?: 0L
    }

    suspend fun autoCheckEnabled(): Boolean = autoCheckFlow.first()

    suspend fun setAutoCheckEnabled(enabled: Boolean) {
        context.updatePrefsDataStore.edit { it[AUTO_CHECK_KEY] = enabled }
    }

    suspend fun skippedVersion(): String = skippedVersionFlow.first()

    suspend fun setSkippedVersion(version: String) {
        context.updatePrefsDataStore.edit { it[SKIPPED_VERSION_KEY] = version }
    }

    suspend fun lastSeenVersionCode(): Long = lastSeenVersionCodeFlow.first()

    suspend fun setLastSeenVersionCode(code: Long) {
        context.updatePrefsDataStore.edit { it[LAST_SEEN_VERSION_CODE_KEY] = code }
    }
}
