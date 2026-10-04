package com.webtoapp.data.home

import com.webtoapp.data.dao.WebAppSummary
import com.webtoapp.data.model.WebApp
import java.util.Locale

/**
 * How the home app list is ordered. [UPDATED_DESC] matches the historical
 * query, so existing installs do not jump when this ships.
 */
enum class AppListSort {
    UPDATED_DESC,
    CREATED_DESC,
    NAME_ASC,
    CUSTOM,
    ;

    companion object {
        fun fromStored(raw: String?): AppListSort =
            entries.firstOrNull { it.name == raw } ?: UPDATED_DESC
    }
}

@JvmName("sortedSummariesForHome")
fun List<WebAppSummary>.sortedForHome(sort: AppListSort): List<WebAppSummary> =
    sortedForHome(
        sort = sort,
        id = { it.id },
        name = { it.name },
        createdAt = { it.createdAt },
        updatedAt = { it.updatedAt },
        homeSortIndex = { it.homeSortIndex },
    )

@JvmName("sortedWebAppsForHome")
fun List<WebApp>.sortedForHome(sort: AppListSort): List<WebApp> =
    sortedForHome(
        sort = sort,
        id = { it.id },
        name = { it.name },
        createdAt = { it.createdAt },
        updatedAt = { it.updatedAt },
        homeSortIndex = { it.homeSortIndex },
    )

/**
 * Splices [visibleOrder] back into [fullOrder]. Hidden ids keep their slots,
 * so reordering one category does not scramble apps in the others.
 *
 * Ids in [visibleOrder] that are not in [fullOrder] are ignored.
 */
fun mergeVisibleOrder(fullOrder: List<Long>, visibleOrder: List<Long>): List<Long> {
    if (visibleOrder.isEmpty() || fullOrder.isEmpty()) return fullOrder
    val inFull = fullOrder.toSet()
    val queue = ArrayDeque(visibleOrder.filter { it in inFull })
    if (queue.isEmpty()) return fullOrder
    val visible = queue.toSet()
    return fullOrder.map { id ->
        if (id in visible && queue.isNotEmpty()) queue.removeFirst() else id
    }
}

/** One laid-out row, in list pixels. */
data class HomeDragSlot(val id: Long, val top: Int, val height: Int)

data class HomeDragStep(val order: List<Long>, val dragDy: Float)

/**
 * Moves [dragId] by one neighbor when the finger has crossed that neighbor's
 * midpoint. Returns null when the row should stay put. Layout slots are stale
 * after a swap, so this takes a single step; the caller asks again next frame.
 *
 * [dragDy] is adjusted by the slot jump so the card stays under the finger.
 */
fun stepCustomDrag(
    order: List<Long>,
    dragId: Long,
    dragDy: Float,
    slots: List<HomeDragSlot>,
): HomeDragStep? {
    if (dragDy == 0f) return null
    val from = order.indexOf(dragId)
    if (from < 0) return null
    val dragged = slots.firstOrNull { it.id == dragId } ?: return null
    if (dragged.height <= 0) return null
    val neighborId = when {
        dragDy > 0f && from < order.lastIndex -> order[from + 1]
        dragDy < 0f && from > 0 -> order[from - 1]
        else -> return null
    }
    val neighbor = slots.firstOrNull { it.id == neighborId } ?: return null
    val center = dragged.top + dragged.height / 2f + dragDy
    val mid = neighbor.top + neighbor.height / 2f
    val crossed = if (dragDy > 0f) center > mid else center < mid
    if (!crossed) return null
    val to = order.indexOf(neighborId)
    val moved = order.toMutableList()
    val item = moved.removeAt(from)
    moved.add(to, item)
    val delta = (neighbor.top - dragged.top).toFloat()
    return HomeDragStep(moved, dragDy - delta)
}

private fun <T> List<T>.sortedForHome(
    sort: AppListSort,
    id: (T) -> Long,
    name: (T) -> String,
    createdAt: (T) -> Long,
    updatedAt: (T) -> Long,
    homeSortIndex: (T) -> Int,
): List<T> {
    val comparator = when (sort) {
        AppListSort.UPDATED_DESC ->
            compareByDescending<T> { updatedAt(it) }.thenByDescending(id)
        AppListSort.CREATED_DESC ->
            compareByDescending<T> { createdAt(it) }.thenByDescending(id)
        AppListSort.NAME_ASC ->
            compareBy<T> { name(it).lowercase(Locale.ROOT) }.thenBy(id)
        AppListSort.CUSTOM ->
            compareBy<T> { homeSortIndex(it) }.thenByDescending(id)
    }
    return sortedWith(comparator)
}
