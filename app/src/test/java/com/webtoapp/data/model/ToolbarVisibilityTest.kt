package com.webtoapp.data.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ToolbarVisibilityTest {

    @Test
    fun `normal mode always shows the full button set even when toolbar flags are all false`() {
        // A fresh app in normal mode never has the toolbar flags hit; but a config that
        // was polluted by toggling "hide toolbar" on then off leaves every flag false.
        // The renderer must still show the full toolbar in normal (non-hide) mode.
        val visibility = resolveToolbarButtons(
            hideBrowserToolbar = false,
            browserToolbarCustomized = true,
            toolbarShowTitle = false,
            toolbarShowUrl = false,
            toolbarShowBack = false,
            toolbarShowForward = false,
            toolbarShowRefresh = false
        )

        assertThat(visibility.showTitle).isTrue()
        assertThat(visibility.showUrl).isTrue()
        assertThat(visibility.showBack).isTrue()
        assertThat(visibility.showForward).isTrue()
        assertThat(visibility.showRefresh).isTrue()
        assertThat(visibility.showConsoleButton).isTrue()
        assertThat(visibility.showZoom).isTrue()
    }

    @Test
    fun `customized slim mode applies the toolbar flags`() {
        val visibility = resolveToolbarButtons(
            hideBrowserToolbar = true,
            browserToolbarCustomized = true,
            toolbarShowTitle = true,
            toolbarShowUrl = false,
            toolbarShowBack = false,
            toolbarShowForward = true,
            toolbarShowRefresh = false
        )

        assertThat(visibility.showTitle).isTrue()
        assertThat(visibility.showUrl).isFalse()
        assertThat(visibility.showBack).isFalse()
        assertThat(visibility.showForward).isTrue()
        assertThat(visibility.showRefresh).isFalse()
        assertThat(visibility.showConsoleButton).isTrue()
        assertThat(visibility.showZoom).isTrue()
    }

    @Test
    fun `slim mode keeps console and zoom on their own toggles`() {
        // The five navigation toggles off no longer force console/zoom off — each has
        // its own switch now.
        val visibility = resolveToolbarButtons(
            hideBrowserToolbar = true,
            browserToolbarCustomized = true,
            toolbarShowTitle = false,
            toolbarShowUrl = false,
            toolbarShowBack = false,
            toolbarShowForward = false,
            toolbarShowRefresh = false
        )

        assertThat(visibility.showTitle).isFalse()
        assertThat(visibility.showUrl).isFalse()
        assertThat(visibility.showBack).isFalse()
        assertThat(visibility.showForward).isFalse()
        assertThat(visibility.showRefresh).isFalse()
        assertThat(visibility.showConsoleButton).isTrue()
        assertThat(visibility.showZoom).isTrue()
    }

    @Test
    fun `slim mode hides console and zoom when their toggles are off`() {
        val visibility = resolveToolbarButtons(
            hideBrowserToolbar = true,
            browserToolbarCustomized = true,
            toolbarShowTitle = true,
            toolbarShowUrl = true,
            toolbarShowBack = true,
            toolbarShowForward = true,
            toolbarShowRefresh = true,
            toolbarShowConsole = false,
            toolbarShowZoom = false
        )

        assertThat(visibility.showConsoleButton).isFalse()
        assertThat(visibility.showZoom).isFalse()
        assertThat(visibility.showTitle).isTrue()
    }

    @Test
    fun `normal mode ignores the console and zoom toggles`() {
        val visibility = resolveToolbarButtons(
            hideBrowserToolbar = false,
            browserToolbarCustomized = false,
            toolbarShowTitle = false,
            toolbarShowUrl = false,
            toolbarShowBack = false,
            toolbarShowForward = false,
            toolbarShowRefresh = false,
            toolbarShowConsole = false,
            toolbarShowZoom = false
        )

        assertThat(visibility.showConsoleButton).isTrue()
        assertThat(visibility.showZoom).isTrue()
    }

    @Test
    fun `hide toolbar on but not customized behaves as normal full toolbar`() {
        // hideBrowserToolbar = true but browserToolbarCustomized = false: the slim
        // toolbar is not shown (showSlimToolbar is false), so the full button set wins.
        val visibility = resolveToolbarButtons(
            hideBrowserToolbar = true,
            browserToolbarCustomized = false,
            toolbarShowTitle = false,
            toolbarShowUrl = false,
            toolbarShowBack = false,
            toolbarShowForward = false,
            toolbarShowRefresh = false
        )

        assertThat(visibility.showTitle).isTrue()
        assertThat(visibility.showBack).isTrue()
        assertThat(visibility.showRefresh).isTrue()
        assertThat(visibility.showConsoleButton).isTrue()
        assertThat(visibility.showZoom).isTrue()
    }
}
