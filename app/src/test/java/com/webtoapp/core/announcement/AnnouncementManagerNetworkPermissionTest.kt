package com.webtoapp.core.announcement

import android.content.Context
import android.content.ContextWrapper
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * File-scheme HTML exports omit ACCESS_NETWORK_STATE (#1254). The shell still
 * constructs [AnnouncementManager] in Application.onCreate, so a missing
 * permission must not crash the process (#1294).
 */
@RunWith(RobolectricTestRunner::class)
class AnnouncementManagerNetworkPermissionTest {

    @Test
    fun `constructing the manager without ACCESS_NETWORK_STATE does not crash`() {
        val manager = AnnouncementManager(DeniedNetworkStateContext())
        assertThat(manager.isNetworkAvailable.value).isFalse()
    }

    @Test
    fun `startNetworkMonitoring without ACCESS_NETWORK_STATE does not crash`() {
        val manager = AnnouncementManager(DeniedNetworkStateContext())
        manager.startNetworkMonitoring()
        manager.stopNetworkMonitoring()
        assertThat(manager.isNetworkAvailable.value).isFalse()
    }
}

internal class DeniedNetworkStateContext(
    base: Context = ApplicationProvider.getApplicationContext()
) : ContextWrapper(base) {
    override fun getApplicationContext(): Context = this

    override fun getSystemService(name: String): Any? {
        if (name == Context.CONNECTIVITY_SERVICE) {
            throw SecurityException(
                "Neither user nor current process has android.permission.ACCESS_NETWORK_STATE."
            )
        }
        return super.getSystemService(name)
    }
}
