package com.webtoapp.ui.plugin

import androidx.compose.ui.unit.IntOffset
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

internal const val PLUGIN_FAN_PER_RING = 7

internal enum class PluginHandleClick {
    OPEN_SHEET,
    ACTIVATE_ONLY,
    FAN_OUT
}

/** One plugin opens directly; many fan out in place; none still opens the sheet. */
internal fun pluginHandleClickAction(entryCount: Int): PluginHandleClick = when {
    entryCount <= 0 -> PluginHandleClick.OPEN_SHEET
    entryCount == 1 -> PluginHandleClick.ACTIVATE_ONLY
    else -> PluginHandleClick.FAN_OUT
}

/**
 * PUBG-style radial offsets from the hub. Y grows downward.
 * [fanRight] is true when the hub sits on the left half of the screen, so
 * the arc opens up and toward the free space.
 */
internal fun pluginFanOffsets(
    count: Int,
    radiusPx: Float,
    fanRight: Boolean,
    perRing: Int = PLUGIN_FAN_PER_RING
): List<IntOffset> {
    if (count <= 0 || radiusPx <= 0f) return emptyList()
    val ringCap = perRing.coerceAtLeast(1)
    return List(count) { i ->
        val ring = i / ringCap
        val indexOnRing = i % ringCap
        val n = minOf(ringCap, count - ring * ringCap)
        val r = radiusPx * (1f + ring * 0.85f)
        val startDeg = if (fanRight) -115.0 else -65.0
        val sweepDeg = if (fanRight) 100.0 else -100.0
        val t = if (n <= 1) 0.5 else indexOnRing / (n - 1).toDouble()
        val rad = Math.toRadians(startDeg + sweepDeg * t)
        IntOffset((cos(rad) * r).roundToInt(), (sin(rad) * r).roundToInt())
    }
}
