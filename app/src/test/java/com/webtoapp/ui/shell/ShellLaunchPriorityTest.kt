package com.webtoapp.ui.shell

import com.google.common.truth.Truth.assertThat
import com.webtoapp.core.shell.FloatingWindowShellConfig
import com.webtoapp.core.shell.ShellConfig
import com.webtoapp.core.shell.WebViewShellConfig
import org.junit.Test

class ShellLaunchPriorityTest {

    @Test
    fun `floating window opens even when fullscreen mode is also on`() {
        val config = shell(floating = true, fullscreen = true)

        assertThat(ShellActivityInit.opensAsFloatingWindow(config)).isTrue()
        assertThat(ShellActivityInit.appliesFullscreenLaunch(config)).isFalse()
    }

    @Test
    fun `fullscreen mode applies only when the floating window is off`() {
        val fullscreen = shell(floating = false, fullscreen = true)
        val neither = shell(floating = false, fullscreen = false)

        assertThat(ShellActivityInit.opensAsFloatingWindow(fullscreen)).isFalse()
        assertThat(ShellActivityInit.appliesFullscreenLaunch(fullscreen)).isTrue()
        assertThat(ShellActivityInit.appliesFullscreenLaunch(neither)).isFalse()
    }

    private fun shell(floating: Boolean, fullscreen: Boolean) = ShellConfig(
        webViewConfig = WebViewShellConfig(
            hideToolbar = fullscreen,
            floatingWindowConfig = FloatingWindowShellConfig(enabled = floating)
        )
    )
}
