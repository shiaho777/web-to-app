package com.webtoapp.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlin.math.roundToInt

class EdgeSwipeRefreshLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : SwipeRefreshLayout(context, attrs) {

    var edgeStartOffsetDp: Float = 12f
    var edgeThresholdDp: Float = 48f
    var indicatorTravelDp: Float = 64f

    private var touchStartedInEdge = false

    private val edgeStartOffsetPx: Float
        get() = edgeStartOffsetDp * resources.displayMetrics.density

    private val edgeThresholdPx: Float
        get() = edgeThresholdDp * resources.displayMetrics.density

    private val indicatorTravelPx: Float
        get() = indicatorTravelDp * resources.displayMetrics.density

    private fun edgeStartPx(): Float {
        val systemTopInset = ViewCompat.getRootWindowInsets(this)
            ?.getInsets(WindowInsetsCompat.Type.systemBars())
            ?.top
            ?: 0
        val location = IntArray(2)
        getLocationOnScreen(location)
        val overlapTopInset = (systemTopInset - location[1]).coerceAtLeast(0)
        return overlapTopInset + edgeStartOffsetPx
    }

    private fun updateIndicatorOffset(startPx: Float) {
        val start = startPx.roundToInt()
        val end = (startPx + indicatorTravelPx).roundToInt()
        setProgressViewOffset(false, start, end)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val startPx = edgeStartPx()
                updateIndicatorOffset(startPx)
                touchStartedInEdge = ev.y in startPx..(startPx + edgeThresholdPx)
            }
        }

        if (!touchStartedInEdge) return false
        return super.onInterceptTouchEvent(ev)
    }
}
