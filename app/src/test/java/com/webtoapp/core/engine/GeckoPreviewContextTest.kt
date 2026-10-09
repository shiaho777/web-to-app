package com.webtoapp.core.engine

import android.app.Application
import android.content.ComponentCallbacks2
import android.content.res.Configuration
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Android 12's Context.registerComponentCallbacks delegates to
 * getApplicationContext(). The preview wrapper returns itself from that
 * method so Gecko keeps the omni.ja override; without its own callback
 * overrides, GeckoRuntime.create overflows the stack (#1274).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [31])
class GeckoPreviewContextTest {

    @Test
    fun `callback registration reaches the real application on Android 12`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val wrapper = GeckoRuntimeProvisioner.newPreviewContext(app, "/tmp/omni_container.zip")

        assertThat(wrapper.applicationContext).isSameInstanceAs(wrapper)
        assertThat(wrapper.packageResourcePath).isEqualTo("/tmp/omni_container.zip")

        var trims = 0
        val callback = object : ComponentCallbacks2 {
            override fun onConfigurationChanged(newConfig: Configuration) {}
            override fun onLowMemory() {}
            override fun onTrimMemory(level: Int) { trims++ }
        }

        wrapper.registerComponentCallbacks(callback)
        app.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW)
        assertThat(trims).isEqualTo(1)

        wrapper.unregisterComponentCallbacks(callback)
        app.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW)
        assertThat(trims).isEqualTo(1)
    }
}
