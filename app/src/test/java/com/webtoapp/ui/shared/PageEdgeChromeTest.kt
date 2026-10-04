package com.webtoapp.ui.shared

import com.google.common.truth.Truth.assertThat
import com.webtoapp.core.webview.PageEdgeColors
import com.webtoapp.core.webview.StatusBarPageColorSampler
import java.io.File
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class PageEdgeChromeTest {

    @Test
    fun `transparent and follow-page bands use the sampled page`() {
        assertThat(PageEdgeChrome.topBandHex("TRANSPARENT", "#112233", "#FF0000", false)).isEqualTo("#112233")
        assertThat(PageEdgeChrome.topBandHex("PAGE_TOP", null, "#FF0000", false)).isNull()
        assertThat(
            PageEdgeChrome.bottomBandHex("PAGE_TOP", null, "#112233", "#FF0000", false)
        ).isEqualTo("#112233")
        assertThat(
            PageEdgeChrome.bottomBandHex("TRANSPARENT", "#445566", "#112233", null, true)
        ).isEqualTo("#445566")
        assertThat(PageEdgeChrome.bottomBandHex("TRANSPARENT", null, null, "#FF0000", false)).isNull()
    }

    @Test
    fun `theme and custom bands stay on the configured color`() {
        assertThat(PageEdgeChrome.topBandHex("THEME", "#112233", "#FF0000", false)).isEqualTo(PageEdgeChrome.LIGHT)
        assertThat(PageEdgeChrome.bottomBandHex("THEME", "#112233", "#445566", "#FF0000", true))
            .isEqualTo(PageEdgeChrome.DARK)
        assertThat(PageEdgeChrome.topBandHex("CUSTOM", "#112233", "#ABCDEF", false)).isEqualTo("#ABCDEF")
        assertThat(PageEdgeChrome.bottomBandHex("CUSTOM", "#112233", "#445566", null, false))
            .isEqualTo(PageEdgeChrome.LIGHT)
    }

    @Test
    fun `navigation bar follows the same mode`() {
        assertThat(PageEdgeChrome.navigationBarHex("TRANSPARENT", "#112233", "#445566", null, false)).isNull()
        assertThat(PageEdgeChrome.navigationBarHex("PAGE_TOP", "#112233", "#445566", null, false)).isEqualTo("#112233")
        assertThat(PageEdgeChrome.navigationBarHex("PAGE_TOP", null, "#445566", null, false)).isEqualTo("#445566")
        assertThat(PageEdgeChrome.navigationBarHex("PAGE_TOP", "  ", null, null, true)).isEqualTo(PageEdgeChrome.DARK)
        assertThat(PageEdgeChrome.navigationBarHex("CUSTOM", null, null, "#ABCDEF", false)).isEqualTo("#ABCDEF")
        assertThat(PageEdgeChrome.navigationBarHex("THEME", "#112233", "#445566", "#ABCDEF", false))
            .isEqualTo(PageEdgeChrome.LIGHT)
    }

    @Test
    fun `sampler accepts a hex and a top-bottom object`() {
        assertThat(StatusBarPageColorSampler.decodePageEdgeColors(null)).isEqualTo(PageEdgeColors(null, null))
        assertThat(StatusBarPageColorSampler.decodePageEdgeColors("null")).isEqualTo(PageEdgeColors(null, null))
        assertThat(StatusBarPageColorSampler.decodePageEdgeColors("\"#AABBCC\""))
            .isEqualTo(PageEdgeColors("#AABBCC", "#AABBCC"))
        assertThat(
            StatusBarPageColorSampler.decodePageEdgeColors(
                "\"{\\\"top\\\":\\\"#112233\\\",\\\"bottom\\\":\\\"#445566\\\"}\""
            )
        ).isEqualTo(PageEdgeColors("#112233", "#445566"))
        assertThat(
            StatusBarPageColorSampler.decodePageEdgeColors("""{"top":null,"bottom":"#445566"}""")
        ).isEqualTo(PageEdgeColors(null, "#445566"))
        assertThat(StatusBarPageColorSampler.decodePageEdgeColors("not-a-color"))
            .isEqualTo(PageEdgeColors(null, null))
    }

    @Test
    fun `preview and shell paint the margin bands and the navigation bar`() {
        val shellLayout = readSource("com/webtoapp/ui/shell/ShellScaffoldLayout.kt")
        val preview = readSource("com/webtoapp/ui/webview/WebViewActivity.kt")
        val shellActivity = readSource("com/webtoapp/ui/shell/ShellActivity.kt")
        assertThat(shellLayout).contains("pageEdgeBands(")
        assertThat(preview).contains("pageEdgeBands(")
        assertThat(shellActivity).contains("applyPageEdgeNavigation(")
        assertThat(preview).contains("applyPageEdgeNavigation(")
        assertThat(shellActivity).contains("if (!enabled || !shouldHideNavBar) applyPageEdgeNavigation")
        assertThat(preview).contains("if (!enabled || !shouldHideNavBar) applyPageEdgeNavigation")
    }

    private fun readSource(relativePath: String): String {
        val javaRoot = listOf("app/src/main/java", "src/main/java").asSequence()
            .map(::File).firstOrNull(File::exists)
            ?: error("Cannot locate java directory")
        val file = File(javaRoot, relativePath)
        assertThat(file.isFile).isTrue()
        return file.readText()
    }
}
