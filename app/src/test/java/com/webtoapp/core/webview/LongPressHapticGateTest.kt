package com.webtoapp.core.webview

import android.app.Activity
import android.view.HapticFeedbackConstants
import android.view.View
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class LongPressHapticGateTest {

    @Test
    fun `off leaves the framework buzz alone`() {
        val view = attachedView()
        val handled = LongPressHapticGate.finish(view, onActionOnly = false, handled = true)
        assertThat(handled).isTrue()
        assertThat(view.isHapticFeedbackEnabled).isTrue()
    }

    @Test
    fun `a press handed back to the page is not swallowed`() {
        val view = attachedView()
        val handled = LongPressHapticGate.finish(view, onActionOnly = true, handled = false)
        assertThat(handled).isFalse()
        assertThat(view.isHapticFeedbackEnabled).isTrue()
    }

    @Test
    fun `a handled press drops the framework buzz until the next turn`() {
        val view = attachedView()
        val handled = LongPressHapticGate.finish(view, onActionOnly = true, handled = true)
        assertThat(handled).isTrue()
        assertThat(view.isHapticFeedbackEnabled).isFalse()
        shadowOf(android.os.Looper.getMainLooper()).idle()
        assertThat(view.isHapticFeedbackEnabled).isTrue()
    }

    @Test
    fun `an opened menu replays the long-press haptic`() {
        val view = RecordingView(attachedActivity())
        LongPressHapticGate.onMenuShown(view, onActionOnly = true)
        assertThat(view.lastConstant).isEqualTo(HapticFeedbackConstants.LONG_PRESS)
        assertThat(view.lastFlags).isEqualTo(HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING)
    }

    @Test
    fun `an opened menu stays quiet when the option is off`() {
        val view = RecordingView(attachedActivity())
        LongPressHapticGate.onMenuShown(view, onActionOnly = false)
        assertThat(view.lastConstant).isEqualTo(-1)
    }

    private fun attachedActivity(): Activity =
        Robolectric.buildActivity(Activity::class.java).setup().get()

    private fun attachedView(): View {
        val activity = attachedActivity()
        val view = View(activity)
        activity.setContentView(view)
        return view
    }

    private class RecordingView(activity: Activity) : View(activity) {
        var lastConstant = -1
        var lastFlags = -1

        override fun performHapticFeedback(feedbackConstant: Int, flags: Int): Boolean {
            lastConstant = feedbackConstant
            lastFlags = flags
            return true
        }
    }
}
