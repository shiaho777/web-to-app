package com.webtoapp.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.webtoapp.data.model.SwipeRefreshZone
import kotlin.math.roundToInt

class WebSwipeRefreshLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : SwipeRefreshLayout(context, attrs) {

    /**
     * Where a touch may start a pull. [SwipeRefreshZone.TOP_EDGE] arms the
     * gesture only inside a narrow band below the status-bar exclusion (the
     * pre-#515 EdgeSwipeRefreshLayout behaviour); [SwipeRefreshZone.ANYWHERE]
     * arms it across the whole content area (#515 behaviour).
     */
    var gestureZone: SwipeRefreshZone = SwipeRefreshZone.TOP_EDGE

    var topExclusionDp: Float = 12f
    var edgeBandDp: Float = 48f
    var indicatorTravelDp: Float = 96f
    var triggerDistanceDp: Float = 64f

    private var pullArmed = false
    private var lastAppliedStart = Int.MIN_VALUE
    private var lastAppliedEnd = Int.MIN_VALUE

    private val density: Float
        get() = resources.displayMetrics.density

    private val topExclusionPx: Float
        get() = topExclusionDp * density

    private val edgeBandPx: Float
        get() = edgeBandDp * density

    private val indicatorTravelPx: Float
        get() = indicatorTravelDp * density

    private val triggerDistancePx: Float
        get() = triggerDistanceDp * density

    init {
        setDistanceToTriggerSync(triggerDistancePx.roundToInt())
    }

    private fun topExclusionLowerBoundPx(): Float {
        val rootInsets = ViewCompat.getRootWindowInsets(this)
        val visibleTop = rootInsets?.getInsets(WindowInsetsCompat.Type.systemBars())?.top ?: 0
        val location = IntArray(2)
        getLocationOnScreen(location)
        val overlapTopInset = if (visibleTop > 0) {
            (visibleTop - location[1]).coerceAtLeast(0)
        } else {
            0
        }
        return overlapTopInset + topExclusionPx
    }

    private fun refreshIndicatorOffsetIfNeeded() {
        if (isRefreshing) return
        val lowerBound = topExclusionLowerBoundPx()
        val start = lowerBound.roundToInt()
        val end = (lowerBound + indicatorTravelPx).roundToInt()
        if (start == lastAppliedStart && end == lastAppliedEnd) return
        lastAppliedStart = start
        lastAppliedEnd = end
        setProgressViewOffset(false, start, end)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                refreshIndicatorOffsetIfNeeded()
                val lowerBound = topExclusionLowerBoundPx()
                pullArmed = when (gestureZone) {
                    SwipeRefreshZone.TOP_EDGE ->
                        ev.y in lowerBound..(lowerBound + edgeBandPx)
                    SwipeRefreshZone.ANYWHERE ->
                        ev.y >= lowerBound
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> pullArmed = false
        }
        if (!pullArmed) return false
        return super.onInterceptTouchEvent(ev)
    }
}
