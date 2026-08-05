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
        assertThat(visibility.showOverflowButton).isTrue()
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
        assertThat(visibility.showConsoleButton).isTrue() // at least one item checked
        assertThat(visibility.showOverflowButton).isTrue()
    }

    @Test
    fun `customized slim mode with all items unchecked hides the console button too`() {
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
        assertThat(visibility.showConsoleButton).isFalse()
        assertThat(visibility.showOverflowButton).isFalse()
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
        assertThat(visibility.showOverflowButton).isTrue()
    }
}
