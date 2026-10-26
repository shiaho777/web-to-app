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
                globalEnabledIds = listOf("global")
            )
        ).isEmpty()
    }

    @Test
    fun `explicit ids win and keep order`() {
        assertThat(
            resolveAttachedPluginIds(
                pluginsEnabled = true,
                attachedIds = listOf(" b ", "a", "b", ""),
                globalEnabledIds = listOf("global")
            )
        ).containsExactly("b", "a").inOrder()
    }

    @Test
    fun `empty attachment uses the enabled set`() {
        assertThat(
            resolveAttachedPluginIds(
                pluginsEnabled = true,
                attachedIds = listOf("  ", ""),
                globalEnabledIds = listOf(" dark ", "dark", "reader")
            )
        ).containsExactly("dark", "reader").inOrder()
    }

    @Test
    fun `enabled set edits keep order and ignore blanks`() {
        assertThat(updateGlobalEnabledIds(listOf("a", "b"), "c", enabled = true))
            .containsExactly("a", "b", "c").inOrder()
        assertThat(updateGlobalEnabledIds(listOf("a", "b"), "a", enabled = true))
            .containsExactly("a", "b").inOrder()
        assertThat(updateGlobalEnabledIds(listOf("a", "b"), "a", enabled = false))
            .containsExactly("b")
        assertThat(updateGlobalEnabledIds(listOf("a"), "  ", enabled = true))
            .containsExactly("a")
    }
}
