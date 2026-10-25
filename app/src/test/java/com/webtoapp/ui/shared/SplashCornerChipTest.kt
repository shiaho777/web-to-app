package com.webtoapp.ui.shared

import com.google.common.truth.Truth.assertThat
import java.io.File
import org.junit.Test

class SplashCornerChipTest {

    @Test
    fun `chip stays hidden when countdown and skip are both off`() {
        assertThat(splashCornerChipVisible(showCountdown = false, displayTimeSeconds = 3, skipAvailable = false))
            .isFalse()
        assertThat(splashCornerChipVisible(showCountdown = true, displayTimeSeconds = 0, skipAvailable = false))
            .isFalse()
    }

    @Test
    fun `chip stays when either the seconds or the skip label is shown`() {
        assertThat(splashCornerChipVisible(showCountdown = true, displayTimeSeconds = 3, skipAvailable = false))
            .isTrue()
        assertThat(splashCornerChipVisible(showCountdown = false, displayTimeSeconds = 3, skipAvailable = true))
            .isTrue()
        assertThat(splashCornerChipVisible(showCountdown = true, displayTimeSeconds = 0, skipAvailable = true))
            .isTrue()
    }

    @Test
    fun `every splash chip is gated by the same visibility rule`() {
        val roots = listOf(File("."), File(".."))
        fun source(path: String): String = roots.asSequence()
            .map { File(it, path) }
            .firstOrNull { it.isFile }
            ?.readText()
            ?: error("Cannot locate $path")

        listOf(
            "app/src/main/java/com/webtoapp/ui/webview/WebViewSplashOverlay.kt",
            "app/src/main/java/com/webtoapp/ui/shell/ShellMediaContent.kt",
            "app/src/main/java/com/webtoapp/ui/splash/SplashLauncherActivity.kt",
        ).forEach { path ->
            assertThat(source(path)).contains("splashCornerChipVisible(")
        }
    }
}
