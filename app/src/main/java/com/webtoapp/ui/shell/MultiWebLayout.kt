package com.webtoapp.ui.shell

internal const val MULTI_WEB_DEFAULT_CARD_COLUMNS = 2
internal const val MULTI_WEB_MAX_CARD_COLUMNS = 4
internal const val MULTI_WEB_DEFAULT_DRAWER_COLUMNS = 1
internal const val MULTI_WEB_MAX_DRAWER_COLUMNS = 3
internal const val MULTI_WEB_DEFAULT_CARD_ASPECT = 1.2f
internal const val MULTI_WEB_MIN_CARD_ASPECT = 0.9f
internal const val MULTI_WEB_MAX_CARD_ASPECT = 3.2f

/**
 * A missing key keeps the Kotlin default. A raw 0, from a stricter decoder,
 * is not the historical layout. Non-positive values keep the old look:
 * two card columns, one drawer column.
 */
internal fun resolvedGridColumns(raw: Int, default: Int, max: Int): Int {
    val value = if (raw <= 0) default else raw
    return value.coerceIn(1, max)
}

/** Higher aspect is a shorter card. Missing or invalid values stay at the original 1.2. */
internal fun resolvedCardAspect(raw: Float): Float {
    if (raw.isNaN() || raw <= 0f) return MULTI_WEB_DEFAULT_CARD_ASPECT
    return raw.coerceIn(MULTI_WEB_MIN_CARD_ASPECT, MULTI_WEB_MAX_CARD_ASPECT)
}

internal fun drawerSheetWidthDp(columns: Int): Int = when (
    resolvedGridColumns(columns, MULTI_WEB_DEFAULT_DRAWER_COLUMNS, MULTI_WEB_MAX_DRAWER_COLUMNS)
) {
    1 -> 300
    2 -> 440
    else -> 560
}

internal data class SiteCardMetrics(
    val iconDp: Int,
    val paddingDp: Int,
    val cornerDp: Int,
    val emojiSp: Int
)

/** More columns or a shorter card shrinks the icon and padding so the tile stays readable. */
internal fun siteCardMetrics(columns: Int, aspect: Float): SiteCardMetrics {
    val cols = resolvedGridColumns(columns, MULTI_WEB_DEFAULT_CARD_COLUMNS, MULTI_WEB_MAX_CARD_COLUMNS)
    val ratio = resolvedCardAspect(aspect)
    val compact = cols >= 3 || ratio >= 2f
    val tight = ratio >= 2.2f || cols >= 4
    return SiteCardMetrics(
        iconDp = if (tight) 28 else if (compact) 34 else 44,
        paddingDp = if (tight) 10 else if (compact) 12 else 16,
        cornerDp = if (compact) 14 else 20,
        emojiSp = if (tight) 16 else if (compact) 18 else 22
    )
}

internal fun <T> moveListItem(items: List<T>, fromIndex: Int, toIndex: Int): List<T> {
    if (fromIndex == toIndex) return items
    if (fromIndex !in items.indices || toIndex !in items.indices) return items
    val next = items.toMutableList()
    val moved = next.removeAt(fromIndex)
    next.add(toIndex, moved)
    return next
}
