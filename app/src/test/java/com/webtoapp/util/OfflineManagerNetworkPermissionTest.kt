package com.webtoapp.util

import com.google.common.truth.Truth.assertThat
import com.webtoapp.core.announcement.DeniedNetworkStateContext
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * OfflineManager probes ConnectivityManager in its constructor. A generated
 * APK without ACCESS_NETWORK_STATE must survive that probe (#1294).
 */
@RunWith(RobolectricTestRunner::class)
class OfflineManagerNetworkPermissionTest {

    @After
    fun release() {
        OfflineManager.release()
    }

    @Test
    fun `getInstance without ACCESS_NETWORK_STATE does not crash`() {
        val manager = OfflineManager.getInstance(DeniedNetworkStateContext())
        assertThat(manager.isNetworkAvailable()).isFalse()
        assertThat(manager.getCurrentNetworkType()).isEqualTo(OfflineManager.NetworkType.NONE)
    }
}
