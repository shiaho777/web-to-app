package com.webtoapp.core.webview

import android.app.Activity
import android.os.Looper
import android.view.HapticFeedbackConstants
import android.view.View
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class LongPressHapticGateTest {

    @Test
    fun `off leaves the framework buzz alone`() {
        val view = attachedView()
        val handled = LongPressHapticGate.finish(view, onActionOnly = false, handled = true)
        assertThat(handled).isTrue()
        assertThat(view.lastConstant).isEqualTo(-1)
        assertThat(view.isHapticFeedbackEnabled).isTrue()
    }

    @Test
    fun `a press handed back to the page is not swallowed`() {
        val view = attachedView()
        val handled = LongPressHapticGate.finish(view, onActionOnly = true, handled = false)
        assertThat(handled).isFalse()
        assertThat(view.lastConstant).isEqualTo(-1)
        assertThat(view.isHapticFeedbackEnabled).isTrue()
    }

    @Test
    fun `a handled press plays the haptic immediately`() {
        val view = attachedView()
        val handled = LongPressHapticGate.finish(view, onActionOnly = true, handled = true)
        assertThat(handled).isTrue()
        assertThat(view.lastConstant).isEqualTo(HapticFeedbackConstants.LONG_PRESS)
    }

    @Test
    fun `a handled press drops the framework buzz until the next turn`() {
        val view = attachedView()
        LongPressHapticGate.finish(view, onActionOnly = true, handled = true)
        assertThat(view.isHapticFeedbackEnabled).isFalse()
        shadowOf(Looper.getMainLooper()).idle()
        assertThat(view.isHapticFeedbackEnabled).isTrue()
    }

    private fun attachedView(): RecordingView {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        val view = RecordingView(activity)
        activity.setContentView(view)
        return view
    }

    private class RecordingView(activity: Activity) : View(activity) {
        var lastConstant = -1

        override fun performHapticFeedback(feedbackConstant: Int): Boolean {
            lastConstant = feedbackConstant
            return true
        }

        override fun performHapticFeedback(feedbackConstant: Int, flags: Int): Boolean {
            lastConstant = feedbackConstant
            return true
        }
    }
}
