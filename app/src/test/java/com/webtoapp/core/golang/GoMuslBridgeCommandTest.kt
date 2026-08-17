package com.webtoapp.core.golang

import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class GoMuslBridgeCommandTest {

    @Test
    fun `bridge command routes the target through the native musl linker`() {
        val app = RuntimeEnvironment.getApplication()
        val linker = File(app.applicationInfo.nativeLibraryDir, "libmusl-linker.so")
        linker.parentFile?.mkdirs()
        linker.writeBytes(byteArrayOf(0x7f, 'E'.code.toByte(), 'L'.code.toByte(), 'F'.code.toByte()))
        try {
            val cmd = GoDependencyManager.buildMuslBridgeCommand(app, "/data/go_bins/server", listOf("-v"))
            assertThat(cmd).containsExactly(linker.absolutePath, "/data/go_bins/server", "-v").inOrder()
        } finally {
            linker.delete()
        }
    }

    @Test
    fun `bridge command keeps argv semantics of program-mode loader invocation`() {
        val app = ApplicationProvider.getApplicationContext<android.content.Context>()
        val cmd = GoDependencyManager.buildMuslBridgeCommand(app, "bin", emptyList())
        // Program mode: [linker, target, *args] — target sees itself as argv[0].
        assertThat(cmd.size).isEqualTo(2)
        assertThat(cmd[0]).endsWith("libmusl-linker.so")
        assertThat(cmd[1]).isEqualTo("bin")
    }
}
