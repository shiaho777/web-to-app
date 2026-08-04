package com.webtoapp.ui.viewmodel

import com.google.common.truth.Truth.assertThat
import com.webtoapp.data.model.WebViewConfig
import org.junit.Test

class HideBrowserToolbarToggleTest {

    @Test
    fun `first enable clears toolbar flags and marks customized`() {
        val config = WebViewConfig()
        val result = config.withHideBrowserToolbar(true)

        assertThat(result.hideBrowserToolbar).isTrue()
        assertThat(result.browserToolbarCustomized).isTrue()
        assertThat(result.toolbarShowTitle).isFalse()
        assertThat(result.toolbarShowUrl).isFalse()
        assertThat(result.toolbarShowBack).isFalse()
        assertThat(result.toolbarShowForward).isFalse()
        assertThat(result.toolbarShowRefresh).isFalse()
    }

    @Test
    fun `disable restores the full toolbar flag set`() {
        // Simulate a config that was polluted: hide toggle off but every toolbar flag false.
        val config = WebViewConfig(
            hideBrowserToolbar = false,
            toolbarShowTitle = false,
            toolbarShowUrl = false,
            toolbarShowBack = false,
            toolbarShowForward = false,
            toolbarShowRefresh = false,
            browserToolbarCustomized = true
        )
        val result = config.withHideBrowserToolbar(false)

        assertThat(result.hideBrowserToolbar).isFalse()
        assertThat(result.browserToolbarCustomized).isFalse()
        assertThat(result.toolbarShowTitle).isTrue()
        assertThat(result.toolbarShowUrl).isTrue()
        assertThat(result.toolbarShowBack).isTrue()
        assertThat(result.toolbarShowForward).isTrue()
        assertThat(result.toolbarShowRefresh).isTrue()
    }

    @Test
    fun `re-enable keeps customizations instead of re-clearing`() {
        val customized = WebViewConfig(
            hideBrowserToolbar = true,
            toolbarShowTitle = true,
            toolbarShowUrl = false,
            toolbarShowBack = true,
            toolbarShowForward = false,
            toolbarShowRefresh = false,
            browserToolbarCustomized = true
        )
        val result = customized.withHideBrowserToolbar(true)

        assertThat(result.hideBrowserToolbar).isTrue()
        assertThat(result.browserToolbarCustomized).isTrue()
        // Already-customized config is not wiped again.
        assertThat(result.toolbarShowTitle).isTrue()
        assertThat(result.toolbarShowUrl).isFalse()
        assertThat(result.toolbarShowBack).isTrue()
    }
}
