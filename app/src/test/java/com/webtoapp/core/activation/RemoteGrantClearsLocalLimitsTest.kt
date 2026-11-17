package com.webtoapp.core.activation

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class RemoteGrantClearsLocalLimitsTest {

    private lateinit var manager: ActivationManager
    private lateinit var context: android.content.Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        manager = ActivationManager(context)
        runBlocking { manager.resetActivation(APP_ID) }
    }

    @Test
    fun `remote grant clears stale local expiry and usage limits`() = runBlocking {
        context.activationDataStore.edit { prefs ->
            prefs[longPreferencesKey("expire_time_$APP_ID")] = System.currentTimeMillis() - 86_400_000L
            prefs[intPreferencesKey("usage_limit_$APP_ID")] = 3
            prefs[intPreferencesKey("usage_count_$APP_ID")] = 3
            prefs[stringPreferencesKey("code_type_$APP_ID")] = "USAGE_LIMITED"
            prefs[stringPreferencesKey("used_code_$APP_ID")] = "OLD-CARD"
        }

        manager.saveActivationStatus(APP_ID, true)

        val status = manager.getActivationStatus(APP_ID)
        assertThat(status.isActivated).isTrue()
        assertThat(status.isValid).isTrue()
        assertThat(status.expireTime).isNull()
        assertThat(status.usageLimit).isNull()
        assertThat(manager.isActivated(APP_ID).first()).isTrue()
    }

    @Test
    fun `remote grant keeps device binding record`() = runBlocking {
        context.activationDataStore.edit { prefs ->
            prefs[stringPreferencesKey("device_id_$APP_ID")] = "device-abc"
        }

        manager.saveActivationStatus(APP_ID, true)

        val prefs = context.activationDataStore.data.first()
        assertThat(prefs[stringPreferencesKey("device_id_$APP_ID")]).isEqualTo("device-abc")
    }

    private companion object {
        const val APP_ID = 987_654L
    }
}
