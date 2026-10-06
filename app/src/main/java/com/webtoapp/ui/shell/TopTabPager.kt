package com.webtoapp.ui.shell

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * Page math for the multi-web top-tab pager.
 *
 * Position is in pages: 0 is the first site, and increasing it moves toward
 * the next site. One gesture stays on the page it started on or its immediate
 * neighbor — a fling coasts onto that neighbor instead of skipping sites, which
 * would pull extra WebViews into memory. Past that neighbor, or past either
 * end of the list, the finger meets rubber-band resistance.
 *
 * [layoutSign] is +1 in LTR (finger moving left advances) and -1 in RTL.
 */
internal object TopTabPager {
    const val FLING_VELOCITY_PX = 1000f
    const val OVERSCROLL_LIMIT = 0.18f

    fun follow(startPosition: Float, fingerPages: Float, maxPage: Int): Float {
        if (maxPage <= 0) return 0f
        val anchor = startPosition.roundToInt().coerceIn(0, maxPage)
        val min = (anchor - 1).coerceAtLeast(0).toFloat()
        val max = (anchor + 1).coerceAtMost(maxPage).toFloat()
        return rubber(startPosition + fingerPages, min, max)
    }

    fun targetPage(
        startPage: Int,
        position: Float,
        velocityXPx: Float,
        widthPx: Float,
        maxPage: Int,
        layoutSign: Float = 1f
    ): Int {
        if (maxPage <= 0) return 0
        val width = widthPx.coerceAtLeast(1f)
        val velocityPages = -velocityXPx / width * layoutSign
        val flingPages = FLING_VELOCITY_PX / width
        val direction = when {
            velocityPages >= flingPages -> 1
            velocityPages <= -flingPages -> -1
            else -> 0
        }
        val start = startPage.coerceIn(0, maxPage)
        val raw = when (direction) {
            1 -> {
                val advanced = ceil((position - 0.001f).toDouble()).toInt()
                if (advanced <= start) start + 1 else advanced
            }
            -1 -> {
                val retreated = floor((position + 0.001f).toDouble()).toInt()
                if (retreated >= start) start - 1 else retreated
            }
            else -> position.roundToInt()
        }
        return raw.coerceIn(0, maxPage).coerceIn(start - 1, start + 1)
    }

    fun visibleRange(position: Float, maxPage: Int): IntRange {
        if (maxPage < 0 || position.isNaN()) return IntRange.EMPTY
        val clamped = position.coerceIn(-1f, maxPage + 1f)
        val nearest = clamped.roundToInt()
        if (abs(clamped - nearest) <= 0.001f) {
            val page = nearest.coerceIn(0, maxPage)
            return page..page
        }
        val lower = floor(clamped.toDouble()).toInt().coerceIn(0, maxPage)
        val upper = ceil(clamped.toDouble()).toInt().coerceIn(0, maxPage)
        return lower..upper
    }

    fun translationX(index: Int, position: Float, widthPx: Int, layoutSign: Float): Int {
        if (widthPx <= 0 || position.isNaN()) return 0
        return ((index - position) * widthPx * layoutSign).roundToInt()
    }

    private fun rubber(raw: Float, min: Float, max: Float): Float {
        if (raw in min..max) return raw
        if (raw < min) {
            val over = min - raw
            return min - resisted(over)
        }
        return max + resisted(raw - max)
    }

    private fun resisted(over: Float): Float {
        if (over <= 0f) return 0f
        return OVERSCROLL_LIMIT * (1f - 1f / (1f + over / OVERSCROLL_LIMIT))
    }
}
