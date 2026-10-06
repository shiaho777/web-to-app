package com.webtoapp.ui.shell

import android.view.MotionEvent
import android.view.VelocityTracker
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.Job

internal enum class TopTabMove { IGNORE, CLAIM, DRAG }

/**
 * Bridge from the WebView / GeckoView touch listener to the top-tab pager.
 * The listener returns false until a horizontal gesture is claimed, so a
 * vertical read still scrolls the page. Null while bottom tabs, cards, or a
 * single site are showing.
 */
internal object SiteTabSwipe {
    var handler: TopTabSwipeHandler? = null
}

internal interface TopTabSwipeHandler {
    fun onDown(event: MotionEvent)
    fun onMove(event: MotionEvent, touchSlop: Int): TopTabMove
    fun onUp(event: MotionEvent): Boolean
    fun onCancel()
}

/**
 * Finger position for [TabsMode] when the bar is on top.
 *
 * Not a [androidx.compose.foundation.pager.HorizontalPager]: paging items
 * would dispose the WebView once it left the viewport, and a GeckoView
 * SurfaceView only follows a real layout position (graphics-layer translation
 * leaves the surface behind). Visited sites stay composed; this object only
 * moves the ones currently on screen.
 *
 * Coordinates are raw screen pixels. The page is translating under the finger,
 * so the view-local x would change even when the finger is still and the page
 * would run away from the hand.
 */
internal class TopTabPagerController(firstPosition: Float) : TopTabSwipeHandler {
    var position by mutableFloatStateOf(firstPosition)
    var pageCount: Int = 1
    var widthPx: Int = 0
    var layoutSign: Float = 1f
    var pagingEnabled: Boolean = false
    var settledPage: Int = firstPosition.roundToInt()
    var holding: Boolean = false
    var epoch: Int = 0
    var pendingTarget: Int? = null
    var gestureStartPage: Int = settledPage
    var settleJob: Job? = null
    var onCommit: (Int) -> Unit = {}
    var requestSettle: (target: Int, velocityPages: Float, fromDrag: Boolean) -> Unit =
        { target, _, _ ->
            position = target.toFloat()
            pendingTarget = null
            onCommit(target)
        }

    private var tracker: VelocityTracker? = VelocityTracker.obtain()
    private var tracking = false
    private var claimed = false
    private var decidedScroll = false
    private var originRawX = 0f
    private var originRawY = 0f
    private var positionAtDown = firstPosition

    fun close() {
        settleJob?.cancel()
        settleJob = null
        tracker?.recycle()
        tracker = null
    }

    fun nextTicket(): Int {
        epoch += 1
        return epoch
    }

    fun isPageVisible(index: Int): Boolean {
        if (!pagingEnabled) return index == settledPage
        val max = (pageCount - 1).coerceAtLeast(0)
        return index == settledPage || index in TopTabPager.visibleRange(position, max)
    }

    fun jumpTo(index: Int) {
        val max = (pageCount - 1).coerceAtLeast(0)
        val target = index.coerceIn(0, max)
        val anchor = position.roundToInt().coerceIn(0, max)
        if (pagingEnabled && abs(target - position) <= 1.01f && abs(target - anchor) <= 1) {
            requestSettle(target, 0f, false)
        } else {
            holding = false
            epoch += 1
            settleJob?.cancel()
            position = target.toFloat()
            pendingTarget = null
            onCommit(target)
        }
    }

    override fun onDown(event: MotionEvent) {
        if (tracker == null) return
        settleJob?.cancel()
        epoch += 1
        holding = true
        tracking = true
        claimed = false
        decidedScroll = false
        originRawX = event.rawX
        originRawY = event.rawY
        positionAtDown = position
        gestureStartPage = position.roundToInt().coerceIn(0, (pageCount - 1).coerceAtLeast(0))
        tracker?.clear()
        track(event)
    }

    override fun onMove(event: MotionEvent, touchSlop: Int): TopTabMove {
        if (!tracking || tracker == null) return TopTabMove.IGNORE
        track(event)
        if (decidedScroll) return TopTabMove.IGNORE
        val totalDx = event.rawX - originRawX
        val totalDy = event.rawY - originRawY
        if (!claimed) {
            if (abs(totalDx) < touchSlop && abs(totalDy) < touchSlop) return TopTabMove.IGNORE
            if (!pagingEnabled || widthPx <= 0 || pageCount < 2 ||
                abs(totalDx) <= abs(totalDy) * 1.2f
            ) {
                decidedScroll = true
                return TopTabMove.IGNORE
            }
            claimed = true
            applyFinger(totalDx)
            return TopTabMove.CLAIM
        }
        applyFinger(totalDx)
        return TopTabMove.DRAG
    }

    override fun onUp(event: MotionEvent): Boolean {
        if (!tracking) return false
        tracking = false
        holding = false
        track(event)
        val wasClaimed = claimed
        claimed = false
        if (!wasClaimed) {
            pendingTarget?.let { requestSettle(it, 0f, false) }
            return false
        }
        val velocityX = velocityX()
        val max = (pageCount - 1).coerceAtLeast(0)
        val target = TopTabPager.targetPage(
            startPage = gestureStartPage,
            position = position,
            velocityXPx = velocityX,
            widthPx = widthPx.toFloat(),
            maxPage = max,
            layoutSign = layoutSign
        )
        val velocityPages = if (widthPx > 0) -velocityX / widthPx * layoutSign else 0f
        requestSettle(target, velocityPages, true)
        return true
    }

    override fun onCancel() {
        if (!tracking) return
        tracking = false
        holding = false
        val wasClaimed = claimed
        claimed = false
        if (wasClaimed) {
            requestSettle(gestureStartPage, 0f, true)
        } else {
            pendingTarget?.let { requestSettle(it, 0f, false) }
        }
    }

    private fun applyFinger(totalDx: Float) {
        val fingerPages = -totalDx / widthPx.toFloat() * layoutSign
        position = TopTabPager.follow(positionAtDown, fingerPages, pageCount - 1)
    }

    private fun track(event: MotionEvent) {
        val pool = tracker ?: return
        val copy = MotionEvent.obtain(event)
        copy.setLocation(event.rawX, event.rawY)
        pool.addMovement(copy)
        copy.recycle()
    }

    private fun velocityX(): Float {
        val pool = tracker ?: return 0f
        pool.computeCurrentVelocity(1000)
        return pool.xVelocity
    }
}
