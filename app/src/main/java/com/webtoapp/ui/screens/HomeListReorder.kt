package com.webtoapp.ui.screens

import androidx.compose.foundation.gestures.awaitEachGesture
import kotlinx.coroutines.Job
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.util.fastFirstOrNull
import com.webtoapp.core.i18n.Strings
import com.webtoapp.data.dao.WebAppSummary
import com.webtoapp.data.home.AppListSort
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.abs
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Drag session for the home list. Held in one object so the pointer callback
 * keeps reading the latest order after the gesture coroutine has started.
 */
class HomeListDragState {
    var id by mutableStateOf<Long?>(null)
    var dy by mutableFloatStateOf(0f)
    var order by mutableStateOf<List<WebAppSummary>?>(null)
    var tracking by mutableStateOf(false)
    var settleJob: Job? = null

    fun clear() {
        settleJob?.cancel()
        settleJob = null
        id = null
        dy = 0f
        order = null
        tracking = false
    }
}

/**
 * Long-press, then vertical drag. Available on the home list whenever search
 * is empty; the first drag from another sort mode pins that list as custom
 * order. The listener runs on the initial pass and does not consume the
 * pointer until the long-press lands, so a tap still opens the app and a
 * horizontal swipe can still delete.
 *
 * [onDrag] receives the vertical delta in pixels. [onEnd] receives velocity
 * in pixels per second.
 */
fun Modifier.homeReorderDrag(
    enabled: Boolean,
    onStart: () -> Unit,
    onDrag: (dy: Float) -> Unit,
    onEnd: (velocityY: Float) -> Unit,
    onCancel: () -> Unit,
): Modifier {
    if (!enabled) return this
    return pointerInput(enabled) {
        val tracker = VelocityTracker()
        try {
            awaitEachGesture {
                val down = awaitFirstDown(
                    requireUnconsumed = false,
                    pass = PointerEventPass.Initial,
                )
                tracker.resetTracking()
                // Null means the long-press time elapsed with the finger still down.
                val longPressed = withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val change = event.changes.fastFirstOrNull { it.id == down.id }
                            ?: return@withTimeoutOrNull false
                        if (!change.pressed) return@withTimeoutOrNull false
                        val moved = change.position - down.position
                        if (abs(moved.x) > viewConfiguration.touchSlop ||
                            abs(moved.y) > viewConfiguration.touchSlop
                        ) {
                            return@withTimeoutOrNull false
                        }
                    }
                    @Suppress("UNREACHABLE_CODE")
                    false
                } == null
                if (!longPressed) return@awaitEachGesture
                onStart()
                tracker.addPosition(down.uptimeMillis, down.position)
                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                    val change = event.changes.fastFirstOrNull { it.id == down.id }
                    if (change == null || !change.pressed) {
                        change?.consume()
                        onEnd(tracker.calculateVelocity().y)
                        break
                    }
                    val delta = change.positionChange()
                    if (delta != Offset.Zero) {
                        change.consume()
                        tracker.addPosition(change.uptimeMillis, change.position)
                        onDrag(delta.y)
                    }
                }
            }
        } catch (cancel: CancellationException) {
            onCancel()
            throw cancel
        }
    }
}

fun AppListSort.homeMenuLabel(): String = when (this) {
    AppListSort.UPDATED_DESC -> Strings.appListSortUpdated
    AppListSort.CREATED_DESC -> Strings.appListSortCreated
    AppListSort.NAME_ASC -> Strings.appListSortName
    AppListSort.CUSTOM -> Strings.appListSortCustom
}
