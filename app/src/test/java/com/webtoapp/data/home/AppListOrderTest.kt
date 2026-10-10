package com.webtoapp.data.home

import com.google.common.truth.Truth.assertThat
import com.webtoapp.data.dao.WebAppSummary
import com.webtoapp.data.model.AppType
import org.junit.Test

class AppListOrderTest {

    @Test
    fun `updated order is newest first and breaks ties by newer id`() {
        val rows = listOf(
            row(id = 1, name = "A", createdAt = 1, updatedAt = 10, homeSortIndex = 5),
            row(id = 2, name = "B", createdAt = 9, updatedAt = 10, homeSortIndex = 0),
            row(id = 3, name = "C", createdAt = 3, updatedAt = 4, homeSortIndex = 1),
        )

        assertThat(rows.sortedForHome(AppListSort.UPDATED_DESC).map { it.id })
            .containsExactly(2L, 1L, 3L)
            .inOrder()
    }

    @Test
    fun `created order is newest first`() {
        val rows = listOf(
            row(id = 1, name = "A", createdAt = 5, updatedAt = 100, homeSortIndex = 0),
            row(id = 4, name = "B", createdAt = 5, updatedAt = 1, homeSortIndex = 3),
            row(id = 2, name = "C", createdAt = 9, updatedAt = 1, homeSortIndex = 2),
        )

        assertThat(rows.sortedForHome(AppListSort.CREATED_DESC).map { it.id })
            .containsExactly(2L, 4L, 1L)
            .inOrder()
    }

    @Test
    fun `name order ignores case and breaks ties by smaller id`() {
        val rows = listOf(
            row(id = 3, name = "beta", createdAt = 1, updatedAt = 1, homeSortIndex = 0),
            row(id = 2, name = "Alpha", createdAt = 1, updatedAt = 1, homeSortIndex = 0),
            row(id = 1, name = "alpha", createdAt = 1, updatedAt = 1, homeSortIndex = 0),
        )

        assertThat(rows.sortedForHome(AppListSort.NAME_ASC).map { it.id })
            .containsExactly(1L, 2L, 3L)
            .inOrder()
    }

    @Test
    fun `custom order uses the saved index then the newer id`() {
        val rows = listOf(
            row(id = 1, name = "old top", createdAt = 1, updatedAt = 50, homeSortIndex = 0),
            row(id = 8, name = "new", createdAt = 9, updatedAt = 1, homeSortIndex = 0),
            row(id = 3, name = "later", createdAt = 2, updatedAt = 80, homeSortIndex = 1),
        )

        assertThat(rows.sortedForHome(AppListSort.CUSTOM).map { it.id })
            .containsExactly(8L, 1L, 3L)
            .inOrder()
    }

    @Test
    fun `merge keeps apps outside the visible category in their slots`() {
        val merged = mergeVisibleOrder(
            fullOrder = listOf(1, 2, 3, 4, 5),
            visibleOrder = listOf(4, 2),
        )

        assertThat(merged).containsExactly(1L, 4L, 3L, 2L, 5L).inOrder()
    }

    @Test
    fun `merge of the whole list replaces the order`() {
        assertThat(mergeVisibleOrder(listOf(1, 2, 3), listOf(3, 1, 2)))
            .containsExactly(3L, 1L, 2L)
            .inOrder()
    }

    @Test
    fun `merge ignores ids the full list does not contain`() {
        assertThat(mergeVisibleOrder(listOf(1, 2, 3), listOf(9, 2)))
            .containsExactly(1L, 2L, 3L)
            .inOrder()
        assertThat(mergeVisibleOrder(listOf(1, 2), emptyList())).containsExactly(1L, 2L).inOrder()
    }

    @Test
    fun `drag step swaps only after the neighbor midpoint and keeps the finger offset`() {
        val slots = listOf(
            HomeDragSlot(id = 1, top = 0, height = 100),
            HomeDragSlot(id = 2, top = 110, height = 100),
            HomeDragSlot(id = 3, top = 220, height = 100),
        )

        assertThat(stepCustomDrag(listOf(1, 2, 3), dragId = 1, dragDy = 60f, slots = slots))
            .isNull()

        val down = stepCustomDrag(listOf(1, 2, 3), dragId = 1, dragDy = 120f, slots = slots)
        assertThat(down!!.order).containsExactly(2L, 1L, 3L).inOrder()
        assertThat(down.dragDy).isWithin(0.01f).of(10f)

        val up = stepCustomDrag(listOf(1, 2, 3), dragId = 2, dragDy = -120f, slots = slots)
        assertThat(up!!.order).containsExactly(2L, 1L, 3L).inOrder()
        assertThat(up.dragDy).isWithin(0.01f).of(-10f)
    }

    @Test
    fun `a drag from another sort can pin the visible list as custom order`() {
        val recentlyUpdated = listOf(8L, 1L, 3L)
        val previousCustom = listOf(1L, 3L, 8L)
        assertThat(mergeVisibleOrder(previousCustom, recentlyUpdated))
            .containsExactly(8L, 1L, 3L)
            .inOrder()
    }

    @Test
    fun `stored sort falls back to recently updated`() {
        assertThat(AppListSort.fromStored(null)).isEqualTo(AppListSort.UPDATED_DESC)
        assertThat(AppListSort.fromStored("nope")).isEqualTo(AppListSort.UPDATED_DESC)
        assertThat(AppListSort.fromStored("CUSTOM")).isEqualTo(AppListSort.CUSTOM)
    }

    private fun row(
        id: Long,
        name: String,
        createdAt: Long,
        updatedAt: Long,
        homeSortIndex: Int,
    ) = WebAppSummary(
        id = id,
        name = name,
        url = "https://$id.example",
        iconPath = null,
        appType = AppType.WEB,
        updatedAt = updatedAt,
        categoryId = null,
        activationEnabled = false,
        adBlockEnabled = false,
        announcementEnabled = false,
        createdAt = createdAt,
        homeSortIndex = homeSortIndex,
    )
}
