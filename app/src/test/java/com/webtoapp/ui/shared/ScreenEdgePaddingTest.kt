package com.webtoapp.ui.shared

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import java.io.File
import org.junit.Test

class ScreenEdgePaddingTest {

    @Test
    fun `bottom padding stays on the screen edge and disappears under the keyboard`() {
        assertThat(ScreenEdgePadding.effectiveBottomPx(120, 0)).isEqualTo(120)
        assertThat(ScreenEdgePadding.effectiveBottomPx(120, 40)).isEqualTo(80)
        assertThat(ScreenEdgePadding.effectiveBottomPx(120, 800)).isEqualTo(0)
        assertThat(ScreenEdgePadding.effectiveBottomPx(0, 800)).isEqualTo(0)
        assertThat(ScreenEdgePadding.effectiveBottomPx(-4, 0)).isEqualTo(0)
        assertThat(ScreenEdgePadding.effectiveBottomPx(120, -10)).isEqualTo(120)
    }

    @Test
    fun `edge-to-edge occlusion is the IME inset alone`() {
        assertThat(
            ScreenEdgePadding.imeOcclusionPx(
                imeInsetBottomPx = 800,
                belowDecorPx = 1400,
                navigationBarPx = 48,
                classicResize = false
            )
        ).isEqualTo(800)
        assertThat(
            ScreenEdgePadding.imeOcclusionPx(
                imeInsetBottomPx = 0,
                belowDecorPx = 1400,
                navigationBarPx = 48,
                classicResize = false
            )
        ).isEqualTo(0)
    }

    @Test
    fun `classic occlusion counts the keyboard and ignores the nav bar`() {
        assertThat(
            ScreenEdgePadding.imeOcclusionPx(
                imeInsetBottomPx = 0,
                belowDecorPx = 48,
                navigationBarPx = 48,
                classicResize = true
            )
        ).isEqualTo(0)
        assertThat(
            ScreenEdgePadding.imeOcclusionPx(
                imeInsetBottomPx = 0,
                belowDecorPx = 900,
                navigationBarPx = 48,
                classicResize = true
            )
        ).isEqualTo(852)
    }

    @Test
    fun `fullscreen bottom padding is anchored at every call site`() {
        listOf(
            "com/webtoapp/ui/shell/ShellScaffoldLayout.kt",
            "com/webtoapp/ui/shell/MultiWebShellMode.kt",
            "com/webtoapp/ui/webview/WebViewActivity.kt"
        ).forEach { path ->
            assertWithMessage(path)
                .that(readSanitized(path))
                .contains("effectiveBottomContentPadding(")
        }
    }

    private fun readSanitized(relativePath: String): String {
        val javaRoot = listOf("app/src/main/java", "src/main/java").asSequence()
            .map(::File).firstOrNull(File::exists)
            ?: error("Cannot locate java directory")
        val file = File(javaRoot, relativePath)
        assertWithMessage("missing $relativePath").that(file.isFile).isTrue()
        return file.readText().lines().joinToString("\n") { it.substringBefore("//") }
    }
}
