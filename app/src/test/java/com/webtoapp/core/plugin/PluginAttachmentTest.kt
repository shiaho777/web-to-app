package com.webtoapp.core.plugin

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PluginAttachmentTest {

    @Test
    fun `plugins switched off resolve to nothing`() {
        assertThat(
            resolveAttachedPluginIds(
                pluginsEnabled = false,
                attachedIds = listOf("explicit"),
                fallbackIds = listOf("installed")
            )
        ).isEmpty()
    }

    @Test
    fun `explicit ids win and keep order`() {
        assertThat(
            resolveAttachedPluginIds(
                pluginsEnabled = true,
                attachedIds = listOf(" b ", "a", "b", ""),
                fallbackIds = listOf("installed")
            )
        ).containsExactly("b", "a").inOrder()
    }

    @Test
    fun `empty attachment falls back to all installed`() {
        assertThat(
            resolveAttachedPluginIds(
                pluginsEnabled = true,
                attachedIds = listOf("  ", ""),
                fallbackIds = listOf(" dark ", "dark", "reader")
            )
        ).containsExactly("dark", "reader").inOrder()
    }
}