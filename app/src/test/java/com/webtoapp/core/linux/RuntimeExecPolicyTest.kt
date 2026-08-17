package com.webtoapp.core.linux

import android.app.Application
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File

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

    @Test
    fun `exec bridge is absent without the native musl linker`() {
        val app = contextWithTargetSdk(35)
        val linker = File(app.applicationInfo.nativeLibraryDir, "libmusl-linker.so")
        linker.delete()
        assertThat(RuntimeExecPolicy.hasMuslExecBridge(app)).isFalse()
    }

    @Test
    fun `exec bridge unlocks a high targetSdk build when the native musl linker exists`() {
        val app = contextWithTargetSdk(35)
        val linker = File(app.applicationInfo.nativeLibraryDir, "libmusl-linker.so")
        linker.parentFile?.mkdirs()
        linker.writeBytes(byteArrayOf(0x7f, 'E'.code.toByte(), 'L'.code.toByte(), 'F'.code.toByte()))
        linker.setExecutable(true, false)
        try {
            assertThat(RuntimeExecPolicy.canExecAppDataBinaries(app)).isFalse()
            assertThat(RuntimeExecPolicy.hasMuslExecBridge(app)).isTrue()
        } finally {
            linker.delete()
        }
    }
}
