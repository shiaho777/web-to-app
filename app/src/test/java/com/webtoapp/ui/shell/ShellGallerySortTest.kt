package com.webtoapp.ui.shell

import com.google.common.truth.Truth.assertThat
import com.webtoapp.core.shell.GalleryShellItem
import org.junit.Test

class ShellGallerySortTest {

    private val items = listOf(
        GalleryShellItem(id = "a", name = "alpha", type = "IMAGE", sortIndex = 2, createdAt = 300L),
        GalleryShellItem(id = "b", name = "Beta", type = "VIDEO", sortIndex = 0, createdAt = 100L),
        GalleryShellItem(id = "c", name = "gamma", type = "IMAGE", sortIndex = 1, createdAt = 200L)
    )

    @Test
    fun `custom order follows sortIndex`() {
        assertThat(sortedShellGalleryItems(items, "CUSTOM").map { it.id })
            .containsExactly("b", "c", "a").inOrder()
    }

    @Test
    fun `unknown order falls back to custom`() {
        assertThat(sortedShellGalleryItems(items, "WHATEVER").map { it.id })
            .containsExactly("b", "c", "a").inOrder()
    }

    @Test
    fun `name sorts are case-insensitive`() {
        assertThat(sortedShellGalleryItems(items, "NAME_ASC").map { it.id })
            .containsExactly("a", "b", "c").inOrder()
        assertThat(sortedShellGalleryItems(items, "NAME_DESC").map { it.id })
            .containsExactly("c", "b", "a").inOrder()
    }

    @Test
    fun `date sorts use createdAt`() {
        assertThat(sortedShellGalleryItems(items, "DATE_ASC").map { it.id })
            .containsExactly("b", "c", "a").inOrder()
        assertThat(sortedShellGalleryItems(items, "DATE_DESC").map { it.id })
            .containsExactly("a", "c", "b").inOrder()
    }

    @Test
    fun `type sort puts images before videos`() {
        assertThat(sortedShellGalleryItems(items, "TYPE").map { it.id })
            .containsExactly("a", "c", "b").inOrder()
    }
}
