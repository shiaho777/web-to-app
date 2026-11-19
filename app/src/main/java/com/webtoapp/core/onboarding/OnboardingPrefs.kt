package com.webtoapp.core.onboarding

import android.annotation.SuppressLint
import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.onboardingDataStore by preferencesDataStore(name = "onboarding_settings")

@SuppressLint("StaticFieldLeak")
class OnboardingPrefs(private val context: Context) {

    companion object {
        private val COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")

        @Volatile
        private var instance: OnboardingPrefs? = null

        fun getInstance(context: Context): OnboardingPrefs {
            return instance ?: synchronized(this) {
                instance ?: OnboardingPrefs(context.applicationContext).also { instance = it }
            }
        }
    }

    val completedFlow: Flow<Boolean> = context.onboardingDataStore.data.map { prefs ->
        prefs[COMPLETED_KEY] ?: false
    }

    suspend fun setCompleted(completed: Boolean) {
        context.onboardingDataStore.edit { it[COMPLETED_KEY] = completed }
    }
}
