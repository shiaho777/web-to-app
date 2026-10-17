package com.webtoapp.core.webview

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Test

class FileChooserLifetimeTest {

    @After
    fun reset() {
        FileChooserLifetime.markClosed()
    }

    @Test
    fun `chooser stays open until the result is delivered`() {
        assertThat(FileChooserLifetime.isOpen()).isFalse()
        FileChooserLifetime.markOpen()
        assertThat(FileChooserLifetime.isOpen()).isTrue()
        FileChooserLifetime.markClosed()
        assertThat(FileChooserLifetime.isOpen()).isFalse()
    }
}
