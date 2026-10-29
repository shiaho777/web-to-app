package com.webtoapp.data.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ToolbarVisibilityTest {

    @Test
    fun `enabled toolbar mirrors every item flag`() {
        val visibility = resolveToolbarButtons(
            toolbarEnabled = true,
            toolbarShowTitle = true,
            toolbarShowUrl = false,
            toolbarShowBack = true,
            toolbarShowForward = false,
            toolbarShowRefresh = true,
            toolbarShowConsole = false,
            toolbarShowFind = true
        )

        assertThat(visibility.showTitle).isTrue()
        assertThat(visibility.showUrl).isFalse()
        assertThat(visibility.showBack).isTrue()
        assertThat(visibility.showForward).isFalse()
        assertThat(visibility.showRefresh).isTrue()
        assertThat(visibility.showConsoleButton).isFalse()
        assertThat(visibility.showFind).isTrue()
    }

    @Test
    fun `disabled toolbar hides every button regardless of flags`() {
        val visibility = resolveToolbarButtons(
            toolbarEnabled = false,
            toolbarShowTitle = true,
            toolbarShowUrl = true,
            toolbarShowBack = true,
            toolbarShowForward = true,
            toolbarShowRefresh = true,
            toolbarShowConsole = true,
            toolbarShowFind = true
        )

        assertThat(visibility.showTitle).isFalse()
        assertThat(visibility.showUrl).isFalse()
        assertThat(visibility.showBack).isFalse()
        assertThat(visibility.showForward).isFalse()
        assertThat(visibility.showRefresh).isFalse()
        assertThat(visibility.showConsoleButton).isFalse()
        assertThat(visibility.showFind).isFalse()
    }

    @Test
    fun `toolbar trimmed down to console only still has content`() {
        // Regression: the toolbar gate in both the preview and the exported shell used
        // to count only some items, so a toolbar customized down to a single button
        // rendered as a completely hidden top bar.
        val hasContent = hasAnyToolbarItem(
            toolbarShowTitle = false,
            toolbarShowUrl = false,
            toolbarShowBack = false,
            toolbarShowForward = false,
            toolbarShowRefresh = false,
            toolbarShowConsole = true,
            toolbarShowFind = false
        )

        assertThat(hasContent).isTrue()
    }

    @Test
    fun `toolbar with every item off is empty`() {
        val hasContent = hasAnyToolbarItem(
            toolbarShowTitle = false,
            toolbarShowUrl = false,
            toolbarShowBack = false,
            toolbarShowForward = false,
            toolbarShowRefresh = false,
            toolbarShowConsole = false,
            toolbarShowFind = false
        )

        assertThat(hasContent).isFalse()
    }

    @Test
    fun `fullscreen does not hide an enabled toolbar`() {
        assertThat(isBrowserToolbarVisible(toolbarEnabled = true, hasAnyItem = true)).isTrue()
        assertThat(isBrowserToolbarVisible(toolbarEnabled = false, hasAnyItem = true)).isFalse()
        assertThat(isBrowserToolbarVisible(toolbarEnabled = true, hasAnyItem = false)).isFalse()
    }

    @Test
    fun `drawer site bar is omitted while the browser toolbar is visible`() {
        assertThat(drawerSiteBarVisible(browserToolbarVisible = true)).isFalse()
        assertThat(drawerSiteBarVisible(browserToolbarVisible = false)).isTrue()
    }

    @Test
    fun `chrome address drops the scheme and the query`() {
        assertThat("https://example.com/path?q=1#top".shortenForBrowserChrome())
            .isEqualTo("example.com/path")
        assertThat("http://example.com/".shortenForBrowserChrome()).isEqualTo("example.com/")
    }
}
