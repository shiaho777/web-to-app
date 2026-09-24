package com.webtoapp.core.webview

import android.view.InputDevice
import android.view.MotionEvent

/**
 * Normalizes external-pointer input for WebView hosts (#1031).
 *
 * Some OEM ROMs deliver a mouse's primary button as raw
 * [MotionEvent.ACTION_BUTTON_PRESS] / [MotionEvent.ACTION_BUTTON_RELEASE]
 * generic-motion events instead of the usual ACTION_DOWN/UP touch pair. The
 * view tree only routes the secondary button (context menu) and scroll events
 * through that path — a primary BUTTON_PRESS silently drops. The result: the
 * pointer moves (hover works), clicks do nothing, and since the click never
 * lands, the WebView never takes focus so a hardware keyboard goes nowhere too.
 *
 * The host activity feeds [noteTouchEvent] from dispatchTouchEvent and routes
 * dispatchGenericMotionEvent through [translateButtonAction]; a non-null return
 * is the ACTION_* the event should be re-dispatched as via dispatchTouchEvent
 * (build the converted event with [copyWithAction]). Devices that deliver the
 * normal DOWN/UP pair are unaffected — the duplicated button events are left
 * untouched and the real stream wins.
 */
class MouseInputCompat {

    /** A real ACTION_DOWN for the primary mouse button is in flight. */
    private var realPrimaryDown = false

    /** We injected a synthetic ACTION_DOWN and owe the stream an ACTION_UP. */
    private var synthesizedPrimaryDown = false

    /**
     * Observe the touch stream so [translateButtonAction] can tell a lone
     * BUTTON_PRESS (quirky OEM dispatch) apart from one that merely echoes a
     * real ACTION_DOWN.
     */
    fun noteTouchEvent(source: Int, toolType: Int, actionMasked: Int) {
        if (!isMouse(source, toolType)) return
        when (actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                // A synthesized DOWN we injected also passes through here; it
                // must not flip the stream into "real down" state.
                if (!synthesizedPrimaryDown) realPrimaryDown = true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                realPrimaryDown = false
                synthesizedPrimaryDown = false
            }
        }
    }

    /**
     * Decide whether a generic-motion event is a stray primary-button press
     * that needs converting into a touch event. Returns the ACTION_* to
     * dispatch (DOWN or UP), or null to leave the event on its default path.
     */
    fun translateButtonAction(
        source: Int,
        toolType: Int,
        actionMasked: Int,
        actionButton: Int
    ): Int? {
        if (!isMouse(source, toolType)) return null
        if (actionButton != MotionEvent.BUTTON_PRIMARY) return null
        return when (actionMasked) {
            MotionEvent.ACTION_BUTTON_PRESS ->
                if (realPrimaryDown || synthesizedPrimaryDown) {
                    // Real DOWN already delivered (normal path) or one is
                    // already in flight — leave the duplicate alone.
                    null
                } else {
                    synthesizedPrimaryDown = true
                    MotionEvent.ACTION_DOWN
                }
            MotionEvent.ACTION_BUTTON_RELEASE ->
                if (synthesizedPrimaryDown) {
                    synthesizedPrimaryDown = false
                    MotionEvent.ACTION_UP
                } else {
                    null
                }
            else -> null
        }
    }

    companion object {

        fun isMouse(source: Int, toolType: Int): Boolean =
            (source and InputDevice.SOURCE_MOUSE) == InputDevice.SOURCE_MOUSE ||
                toolType == MotionEvent.TOOL_TYPE_MOUSE

        /**
         * Clone [event] with a different action, preserving source/tool type/
         * button state/coords so the re-dispatched event is indistinguishable
         * from a genuine primary-button DOWN/UP.
         */
        fun copyWithAction(event: MotionEvent, newAction: Int): MotionEvent {
            val count = event.pointerCount
            val properties = Array(count) { i ->
                MotionEvent.PointerProperties().also { event.getPointerProperties(i, it) }
            }
            val coords = Array(count) { i ->
                MotionEvent.PointerCoords().also { event.getPointerCoords(i, it) }
            }
            return MotionEvent.obtain(
                event.downTime,
                event.eventTime,
                newAction,
                count,
                properties,
                coords,
                event.metaState,
                event.buttonState,
                event.xPrecision,
                event.yPrecision,
                event.deviceId,
                event.edgeFlags,
                event.source,
                event.flags
            )
        }
    }
}
