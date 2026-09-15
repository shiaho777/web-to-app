package com.webtoapp.ui.shared

import android.app.Activity
import android.graphics.Color
import android.view.View
import android.view.WindowManager
import com.google.common.truth.Truth.assertThat
import com.webtoapp.data.model.KeyboardAdjustMode
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private fun isEdgeToEdge(activity: Activity): Boolean {
    val vis = activity.window.decorView.systemUiVisibility
    return vis and View.SYSTEM_UI_FLAG_LAYOUT_STABLE != 0 ||
        vis and View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN != 0 ||
        vis and View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION != 0
}

/**
 * Regression coverage for issue #613 and its first, ineffective fix (#634).
 *
 * Below API 30 the platform has no native IME-inset dispatch, and a window laid out
 * edge-to-edge (decor not fitting system windows) is never resized for the keyboard —
 * so RESIZE mode must keep the decor fitting system windows and rely on the classic
 * SOFT_INPUT_ADJUST_RESIZE path. #634 only flipped the softInputMode bits while leaving
 * the edge-to-edge layout flags in place, which is why the keyboard still covered the
 * input on Android 10 and lower. These tests assert both the softInputMode AND the
 * edge-to-edge flags, plus the solid status-bar fallback that the classic path needs.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29])
class WindowHelperKeyboardModePreApi30Test {

    @Test
    fun `resize mode below API 30 uses system resize and drops edge-to-edge`() {
        val activity = Robolectric.setupActivity(Activity::class.java)

        WindowHelper.applyImmersiveFullscreen(
            activity,
            enabled = false,
            keyboardAdjustMode = KeyboardAdjustMode.RESIZE
        )

        val mode = activity.window.attributes.softInputMode
        assertThat(mode and WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE).isNotEqualTo(0)
        assertThat(isEdgeToEdge(activity)).isFalse()
    }

    @Test
    fun `resize mode below API 30 downgrades transparent status bar to solid theme`() {
        val activity = Robolectric.setupActivity(Activity::class.java)

        WindowHelper.applyImmersiveFullscreen(
            activity,
            enabled = false,
            statusBarColorMode = "TRANSPARENT",
            keyboardAdjustMode = KeyboardAdjustMode.RESIZE
        )

        // Nothing draws behind the bars on the classic path; a transparent bar would
        // only expose the window background.
        assertThat(Color.alpha(activity.window.statusBarColor)).isEqualTo(255)
        assertThat(activity.window.navigationBarColor)
            .isEqualTo(activity.window.statusBarColor)
    }

    @Test
    fun `switching from nothing mode to resize mode clears stale edge-to-edge flags`() {
        val activity = Robolectric.setupActivity(Activity::class.java)

        WindowHelper.applyImmersiveFullscreen(
            activity,
            enabled = false,
            keyboardAdjustMode = KeyboardAdjustMode.NOTHING
        )
        assertThat(isEdgeToEdge(activity)).isTrue()

        WindowHelper.applyImmersiveFullscreen(
            activity,
            enabled = false,
            keyboardAdjustMode = KeyboardAdjustMode.RESIZE
        )

        assertThat(isEdgeToEdge(activity)).isFalse()
        val mode = activity.window.attributes.softInputMode
        assertThat(mode and WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE).isNotEqualTo(0)
    }

    @Test
    fun `nothing mode keeps adjust nothing and edge-to-edge below API 30`() {
        val activity = Robolectric.setupActivity(Activity::class.java)

        WindowHelper.applyImmersiveFullscreen(
            activity,
            enabled = false,
            keyboardAdjustMode = KeyboardAdjustMode.NOTHING
        )

        val mode = activity.window.attributes.softInputMode
        assertThat(mode and WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING).isNotEqualTo(0)
        assertThat(isEdgeToEdge(activity)).isTrue()
    }

    @Test
    fun `fullscreen status-bar hide on the classic path sets the window fullscreen flag`() {
        val activity = Robolectric.setupActivity(Activity::class.java)

        WindowHelper.applyImmersiveFullscreen(
            activity,
            enabled = true,
            showStatusBar = false,
            keyboardAdjustMode = KeyboardAdjustMode.RESIZE
        )

        // The decor-flag hide only *requests* a relayout — on ROMs that never run that
        // pass the status-bar strip stays as bare window background even though the bar
        // is hidden (#924 follow-up). FLAG_FULLSCREEN is a window attribute the system
        // cannot clear and forces the decor to full height through relayoutWindow.
        assertThat(activity.window.attributes.flags and WindowManager.LayoutParams.FLAG_FULLSCREEN)
            .isNotEqualTo(0)
    }

    @Test
    fun `window fullscreen flag clears when the status bar shows or fullscreen exits`() {
        val activity = Robolectric.setupActivity(Activity::class.java)

        WindowHelper.applyImmersiveFullscreen(
            activity,
            enabled = true,
            showStatusBar = false,
            keyboardAdjustMode = KeyboardAdjustMode.RESIZE
        )

        WindowHelper.applyImmersiveFullscreen(
            activity,
            enabled = true,
            showStatusBar = true,
            keyboardAdjustMode = KeyboardAdjustMode.RESIZE
        )
        assertThat(activity.window.attributes.flags and WindowManager.LayoutParams.FLAG_FULLSCREEN)
            .isEqualTo(0)

        WindowHelper.applyImmersiveFullscreen(
            activity,
            enabled = true,
            showStatusBar = false,
            keyboardAdjustMode = KeyboardAdjustMode.RESIZE
        )
        WindowHelper.applyImmersiveFullscreen(
            activity,
            enabled = false,
            keyboardAdjustMode = KeyboardAdjustMode.RESIZE
        )
        assertThat(activity.window.attributes.flags and WindowManager.LayoutParams.FLAG_FULLSCREEN)
            .isEqualTo(0)
    }

    @Test
    fun `classic system bars window reports true below API 30 in resize mode`() {
        val activity = Robolectric.setupActivity(Activity::class.java)

        WindowHelper.applyImmersiveFullscreen(
            activity,
            enabled = false,
            keyboardAdjustMode = KeyboardAdjustMode.RESIZE
        )

        // Issue #683: Compose callers key their status-bar padding / overlay decisions off
        // this flag, because WindowInsets.statusBars reports 0 on this path whether or not a
        // bar is drawn — a fabricated 24dp fallback band is what left the blank strip.
        assertThat(WindowHelper.isClassicSystemBarsWindow(activity)).isTrue()
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class WindowHelperKeyboardModeApi30Test {

    @Test
    fun `resize mode keeps manual ime padding path on API 30+`() {
        val activity = Robolectric.setupActivity(Activity::class.java)

        WindowHelper.applyImmersiveFullscreen(
            activity,
            enabled = false,
            keyboardAdjustMode = KeyboardAdjustMode.RESIZE
        )

        val mode = activity.window.attributes.softInputMode
        assertThat(mode and WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING).isNotEqualTo(0)
        assertThat(isEdgeToEdge(activity)).isTrue()
    }

    @Test
    fun `edge-to-edge fullscreen hide does not set the window fullscreen flag`() {
        val activity = Robolectric.setupActivity(Activity::class.java)

        WindowHelper.applyImmersiveFullscreen(
            activity,
            enabled = true,
            showStatusBar = false,
            keyboardAdjustMode = KeyboardAdjustMode.RESIZE
        )

        // API 30+ hides bars through the insets controller on an edge-to-edge window;
        // the legacy window flag must stay clear so transient swipe-to-peek keeps working.
        assertThat(activity.window.attributes.flags and WindowManager.LayoutParams.FLAG_FULLSCREEN)
            .isEqualTo(0)
    }

    @Test
    fun `classic system bars window reports false on API 30 and above`() {
        val activity = Robolectric.setupActivity(Activity::class.java)

        WindowHelper.applyImmersiveFullscreen(
            activity,
            enabled = false,
            keyboardAdjustMode = KeyboardAdjustMode.RESIZE
        )

        assertThat(WindowHelper.isClassicSystemBarsWindow(activity)).isFalse()
    }
}
