package com.webtoapp.core.webview

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Test

class FileChooserLifetimeTest {

    @After
    fun reset() {
        FileChooserLifetime.markClosed()
        repeat(4) { FileChooserLifetime.endPermissionDialog() }
        repeat(4) { FileChooserLifetime.consumeResumeProbeSkip() }
    }

    @Test
    fun `nothing is waiting to reattach before a chooser opens`() {
        assertThat(FileChooserLifetime.takeDetachedWebView()).isNull()
        assertThat(FileChooserLifetime.callback()).isNull()
    }

    @Test
    fun `chooser stays open until the result is delivered`() {
        assertThat(FileChooserLifetime.isOpen()).isFalse()
        FileChooserLifetime.markOpen()
        assertThat(FileChooserLifetime.isOpen()).isTrue()
        FileChooserLifetime.markClosed()
        assertThat(FileChooserLifetime.isOpen()).isFalse()
    }

    @Test
    fun `permission dialog holds the webview and skips one resume probe`() {
        assertThat(
            FileChooserLifetime.runPermissionDialog { }
        ).isTrue()
        assertThat(FileChooserLifetime.isOpen()).isTrue()

        FileChooserLifetime.endPermissionDialog()
        assertThat(FileChooserLifetime.isOpen()).isFalse()
        assertThat(FileChooserLifetime.consumeResumeProbeSkip()).isTrue()
        assertThat(FileChooserLifetime.consumeResumeProbeSkip()).isFalse()
    }

    @Test
    fun `failed permission dialog releases the hold and does not skip the probe`() {
        assertThat(
            FileChooserLifetime.runPermissionDialog { throw IllegalStateException("not in manifest") }
        ).isFalse()
        assertThat(FileChooserLifetime.isOpen()).isFalse()
        assertThat(FileChooserLifetime.consumeResumeProbeSkip()).isFalse()
    }
}
