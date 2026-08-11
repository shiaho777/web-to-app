package com.webtoapp.core.engine

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.mozilla.geckoview.StorageController

class GeckoClientCertificatePolicyTest {

    @Test
    fun `enabled client certificate auth clears remembered Gecko site security decisions`() {
        assertThat(GeckoViewEngine.clientCertificateClearFlags(enabled = true))
            .isEqualTo(StorageController.ClearFlags.SITE_SETTINGS)
        assertThat(GeckoViewEngine.clientCertificateClearFlags(enabled = false))
            .isEqualTo(0L)
    }
}
