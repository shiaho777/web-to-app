package com.webtoapp.core.linux

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class HostProcessLauncherTest {

    @Test
    fun `unrestricted builds spawn through ProcessBuilder and overlay the env`() {
        val app = RuntimeEnvironment.getApplication()
        app.applicationInfo.targetSdkVersion = 28
        val workdir = File(app.filesDir, "wd").apply { mkdirs() }
        val result = HostProcessLauncher.start(
            app,
            listOf("sh", "-c", "echo marker-\$WTA_TEST_FOO; pwd"),
            mapOf("WTA_TEST_FOO" to "42"),
            workdir
        )
        val out = result.process!!.inputStream.bufferedReader().readText()
        result.process!!.waitFor()
        assertThat(result.error).isNull()
        assertThat(out).contains("marker-42")
        assertThat(out.trim().endsWith(workdir.name)).isTrue()
    }

    @Test
    fun `restricted builds without the static bridge surface the blocked message`() {
        val app = RuntimeEnvironment.getApplication()
        app.applicationInfo.targetSdkVersion = 35
        File(app.applicationInfo.nativeLibraryDir, "libstatic_exec.so").delete()
        val result = HostProcessLauncher.start(
            app, listOf("sh", "-c", "echo never"), emptyMap(), null, "WordPress (PHP)"
        )
        assertThat(result.process).isNull()
        assertThat(result.error).contains("WordPress (PHP)")
        assertThat(result.error).contains("targetSdk")
    }
}
