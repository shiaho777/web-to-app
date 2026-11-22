package com.webtoapp.ui.shared

import android.app.Activity
import android.graphics.Color
import android.view.View
import com.google.common.truth.Truth.assertThat
import com.webtoapp.core.webview.PageEdgeColors
import com.webtoapp.core.webview.StatusBarPageColorSampler
import java.io.File
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
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
            .isEqualTo(PageEdgeColors("#AABBCC", "#AABBCC", "#AABBCC"))
        assertThat(
            StatusBarPageColorSampler.decodePageEdgeColors(
                """{"top":"#112233","bottom":"#445566","page":"#778899"}"""
            )
        ).isEqualTo(PageEdgeColors("#112233", "#445566", "#778899"))
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
    fun `normal transparent layout paints the sampled color and switches icons with it`() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        val light = statusBar(pageTop = "#E2EAEE")
        val dark = statusBar(pageTop = "#171923")

        light.applyTo(activity)
        assertThat(light.bandColor).isEqualTo(Color.parseColor("#E2EAEE"))
        assertThat(activity.window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR).isNotEqualTo(0)

        WindowHelper.applyImmersiveFullscreen(activity, enabled = false, statusBarAppearance = dark)
        assertThat(dark.bandColor).isEqualTo(Color.parseColor("#171923"))
        assertThat(activity.window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR).isEqualTo(0)
        assertThat(activity.window.statusBarColor).isEqualTo(Color.TRANSPARENT)
        assertThat(activity.window.isStatusBarContrastEnforced).isFalse()
    }

    @Test
    fun `missing and invalid page colors fall back to the rendered shell surface`() {
        for (sample in listOf(null, "not-a-color")) {
            val darkSurface = statusBar(pageTop = sample, themeColor = Color.parseColor("#121215"))
            assertThat(darkSurface.darkIcons).isFalse()
            assertThat(darkSurface.bandColor).isEqualTo(Color.parseColor("#121215"))

            val lightSurface = statusBar(pageTop = sample, themeColor = Color.WHITE)
            assertThat(lightSurface.darkIcons).isTrue()
            assertThat(lightSurface.bandColor).isEqualTo(Color.WHITE)
        }
    }

    @Test
    fun `fullscreen transparency and native toolbars use their visible backdrop`() {
        val fullscreen = statusBar(pageTop = "#171923", pageBehindBar = true)
        assertThat(fullscreen.bandColor).isNull()
        assertThat(fullscreen.darkIcons).isFalse()

        val withTabs = statusBar(pageTop = "#171923", topTabColor = "#FFFFFF")
        assertThat(withTabs.bandColor).isEqualTo(Color.WHITE)
        assertThat(withTabs.darkIcons).isTrue()

        val withToolbar = statusBar(pageTop = "#171923", browserToolbarVisible = true)
        assertThat(withToolbar.bandColor).isEqualTo(Color.WHITE)
        assertThat(withToolbar.darkIcons).isTrue()
    }

    @Test
    fun `theme mode and translucent colors use the rendered color for contrast`() {
        val themed = statusBar(mode = "THEME", pageTop = "#FFFFFF", themeColor = Color.BLACK)
        assertThat(themed.bandColor).isEqualTo(Color.BLACK)
        assertThat(themed.darkIcons).isFalse()

        val translucent = statusBar(mode = "CUSTOM", customColor = "#000000", backgroundAlpha = 0.2f)
        assertThat(translucent.bandColor).isEqualTo(Color.parseColor("#CCCCCC"))
        assertThat(translucent.darkIcons).isTrue()
        assertThat(statusBar(pageTop = "#777777").darkIcons).isTrue()
        assertThat(statusBar(pageTop = "#FFFFFF", darkIcons = false).darkIcons).isFalse()
    }

    @Test
    @Config(sdk = [29])
    fun `classic window uses a solid native bar and no Compose band`() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        val appearance = statusBar(pageTop = "#FFFFFF", themeColor = Color.BLACK, classicWindow = true)
        appearance.applyTo(activity)

        assertThat(appearance.bandColor).isNull()
        assertThat(activity.window.statusBarColor).isEqualTo(Color.BLACK)
        assertThat(appearance.darkIcons).isFalse()
    }

    @Test
    fun `preview and shell paint the margin bands and the navigation bar`() {
        val shellLayout = readSource("com/webtoapp/ui/shell/ShellScaffoldLayout.kt")
        val preview = readSource("com/webtoapp/ui/webview/WebViewActivity.kt")
        val shellActivity = readSource("com/webtoapp/ui/shell/ShellActivity.kt")
        assertThat(shellLayout).contains("pageEdgeBands(")
        assertThat(readSource("com/webtoapp/ui/shell/ShellScreen.kt")).contains("StatusBarOverlay(appearance")
        assertThat(preview).contains("StatusBarOverlay(appearance")
        assertThat(preview).contains("pageEdgeBands(")
        assertThat(shellActivity).contains("applyPageEdgeNavigation(")
        assertThat(preview).contains("applyPageEdgeNavigation(")
        assertThat(shellActivity).contains("if (!enabled || !shouldHideNavBar) applyPageEdgeNavigation")
        assertThat(preview).contains("if (!enabled || !shouldHideNavBar) applyPageEdgeNavigation")
    }

    @Test
    fun `focus changes reuse the appearance used to draw the band`() {
        val shellActivity = readSource("com/webtoapp/ui/shell/ShellActivity.kt")
        val focusChanged = shellActivity
            .substringAfter("override fun onWindowFocusChanged")
            .substringBefore("override fun onSaveInstanceState")
        assertThat(focusChanged).contains("refreshStatusBarAppearance()")
        assertThat(shellActivity).contains("statusBarAppearance?.applyTo(this)")
        assertThat(shellActivity).contains("statusBarAppearance = statusBarAppearance")
    }

    private fun statusBar(
        mode: String = "TRANSPARENT",
        customColor: String? = null,
        backgroundAlpha: Float = 1f,
        darkIcons: Boolean? = null,
        pageTop: String? = null,
        topTabColor: String? = null,
        themeColor: Int = Color.WHITE,
        pageBehindBar: Boolean = false,
        browserToolbarVisible: Boolean = false,
        classicWindow: Boolean = false
    ) = resolveStatusBarAppearance(
        mode, customColor, "COLOR", null, backgroundAlpha, darkIcons, pageTop, topTabColor,
        themeColor, pageBehindBar, browserToolbarVisible, classicWindow
    )

    private fun readSource(relativePath: String): String {
        val javaRoot = listOf("app/src/main/java", "src/main/java").asSequence()
            .map(::File).firstOrNull(File::exists)
            ?: error("Cannot locate java directory")
        val file = File(javaRoot, relativePath)
        assertThat(file.isFile).isTrue()
        return file.readText()
    }
}
