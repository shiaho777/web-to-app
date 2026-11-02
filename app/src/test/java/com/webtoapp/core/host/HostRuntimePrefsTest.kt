package com.webtoapp.core.host

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class HostRuntimePrefsTest {

    @Test
    fun `separate tasks flow and cache update without waiting for the datastore write`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = HostRuntimePrefs.getInstance(context)

        prefs.setSeparateTasksEnabled(true)
        assertThat(prefs.separateTasksFlow.value).isTrue()
        assertThat(prefs.isSeparateTasksEnabledBlocking()).isTrue()

        prefs.setSeparateTasksEnabled(false)
        assertThat(prefs.separateTasksFlow.value).isFalse()
        assertThat(prefs.isSeparateTasksEnabledBlocking()).isFalse()
    }
}
