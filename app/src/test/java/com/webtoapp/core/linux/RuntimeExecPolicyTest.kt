package com.webtoapp.core.linux

import android.app.Application
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class RuntimeExecPolicyTest {

    private fun contextWithTargetSdk(target: Int): Application {
        val app = RuntimeEnvironment.getApplication()
        app.applicationInfo.targetSdkVersion = target
        return app
    }

    @Test
    fun `low targetSdk builds may exec app data binaries`() {
        assertThat(RuntimeExecPolicy.canExecAppDataBinaries(contextWithTargetSdk(28))).isTrue()
    }

    @Test
    fun `the W-X gate starts exactly at targetSdk 29`() {
        assertThat(RuntimeExecPolicy.canExecAppDataBinaries(contextWithTargetSdk(29))).isFalse()
    }

    @Test
    fun `high targetSdk builds are blocked from exec`() {
        assertThat(RuntimeExecPolicy.canExecAppDataBinaries(contextWithTargetSdk(35))).isFalse()
        assertThat(RuntimeExecPolicy.hostPreviewBlockedMessage("Python")).contains("Python")
        assertThat(RuntimeExecPolicy.restrictionNote()).isNotEmpty()
    }
}
