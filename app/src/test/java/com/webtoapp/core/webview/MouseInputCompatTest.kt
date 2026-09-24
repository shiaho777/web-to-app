package com.webtoapp.core.webview

import android.view.InputDevice
import android.view.MotionEvent
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MouseInputCompatTest {

    private val mouse = InputDevice.SOURCE_MOUSE
    private val tool = MotionEvent.TOOL_TYPE_MOUSE
    private val finger = MotionEvent.TOOL_TYPE_FINGER
    private val primary = MotionEvent.BUTTON_PRIMARY
    private val secondary = MotionEvent.BUTTON_SECONDARY

    private fun press(c: MouseInputCompat, source: Int = mouse, toolType: Int = tool, button: Int = primary) =
        c.translateButtonAction(source, toolType, MotionEvent.ACTION_BUTTON_PRESS, button)

    private fun release(c: MouseInputCompat, source: Int = mouse, toolType: Int = tool, button: Int = primary) =
        c.translateButtonAction(source, toolType, MotionEvent.ACTION_BUTTON_RELEASE, button)

    private fun touch(c: MouseInputCompat, action: Int, source: Int = mouse, toolType: Int = tool) =
        c.noteTouchEvent(source, toolType, action)

    @Test
    fun `quirky OEM press-release without DOWN synthesizes a tap`() {
        val c = MouseInputCompat()

        assertThat(press(c)).isEqualTo(MotionEvent.ACTION_DOWN)
        assertThat(release(c)).isEqualTo(MotionEvent.ACTION_UP)
    }

    @Test
    fun `button events after a real DOWN are left alone`() {
        val c = MouseInputCompat()
        touch(c, MotionEvent.ACTION_DOWN)

        assertThat(press(c)).isNull()
        assertThat(release(c)).isNull()
    }

    @Test
    fun `release without a synthesized press is not translated`() {
        val c = MouseInputCompat()

        assertThat(release(c)).isNull()
    }

    @Test
    fun `touchscreen fingers never translate`() {
        val c = MouseInputCompat()

        assertThat(
            press(c, source = InputDevice.SOURCE_TOUCHSCREEN, toolType = finger)
        ).isNull()
    }

    @Test
    fun `secondary button is never translated`() {
        val c = MouseInputCompat()

        assertThat(press(c, button = secondary)).isNull()
        assertThat(release(c, button = secondary)).isNull()
    }

    @Test
    fun `scroll and hover are never translated`() {
        val c = MouseInputCompat()

        assertThat(
            c.translateButtonAction(mouse, tool, MotionEvent.ACTION_SCROLL, primary)
        ).isNull()
        assertThat(
            c.translateButtonAction(mouse, tool, MotionEvent.ACTION_HOVER_MOVE, primary)
        ).isNull()
    }

    @Test
    fun `double press without release translates only once`() {
        val c = MouseInputCompat()

        assertThat(press(c)).isEqualTo(MotionEvent.ACTION_DOWN)
        assertThat(press(c)).isNull()
        assertThat(release(c)).isEqualTo(MotionEvent.ACTION_UP)
    }

    @Test
    fun `real DOWN arriving after a synthesized press does not double`() {
        val c = MouseInputCompat()
        assertThat(press(c)).isEqualTo(MotionEvent.ACTION_DOWN)

        // The injected DOWN passes through noteTouchEvent without becoming "real".
        touch(c, MotionEvent.ACTION_DOWN)
        assertThat(release(c)).isEqualTo(MotionEvent.ACTION_UP)
    }

    @Test
    fun `state resets after real UP and after CANCEL`() {
        val c = MouseInputCompat()
        touch(c, MotionEvent.ACTION_DOWN)
        touch(c, MotionEvent.ACTION_UP)

        assertThat(press(c)).isEqualTo(MotionEvent.ACTION_DOWN)
        assertThat(release(c)).isEqualTo(MotionEvent.ACTION_UP)

        touch(c, MotionEvent.ACTION_DOWN)
        touch(c, MotionEvent.ACTION_CANCEL)
        assertThat(press(c)).isEqualTo(MotionEvent.ACTION_DOWN)
    }
}
