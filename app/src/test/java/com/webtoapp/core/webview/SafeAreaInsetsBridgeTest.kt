package com.webtoapp.core.webview

import android.app.Activity
import com.google.common.truth.Truth.assertThat
import com.webtoapp.data.model.DisplayCutoutMode
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SafeAreaInsetsBridgeTest {

    @Test
    fun `insets serialize to the shape the document-start script expects`() {
        assertThat(SafeAreaInsets(12, 0, 24, 0).toJson())
            .isEqualTo("{\"top\":12,\"right\":0,\"bottom\":24,\"left\":0}")
    }

    @Test
    fun `document-start script declares all four variables and a retry path`() {
        val script = SafeAreaInsetsBridge.DOCUMENT_START_SCRIPT
        assertThat(script).contains("--wta-safe-area-inset-top:")
        assertThat(script).contains("--wta-safe-area-inset-right:")
        assertThat(script).contains("--wta-safe-area-inset-bottom:")
        assertThat(script).contains("--wta-safe-area-inset-left:")
        assertThat(script).contains(SafeAreaInsetsBridge.JS_BRIDGE_NAME)
        assertThat(script).contains("__wtaSafeAreaInstalled")
        assertThat(script).contains("readystatechange")
        assertThat(script).contains("DOMContentLoaded")
        assertThat(script).contains("__wtaSetSafeAreaInsets")
    }

    @Test
    fun `update script carries current values inside one guarded JS literal`() {
        val script = SafeAreaInsetsBridge.updateScript(SafeAreaInsets(8, 0, 16, 0))
        assertThat(script).contains("__wtaSetSafeAreaInsets")
        assertThat(script).contains("\"top\":8")
        assertThat(script).contains("\"bottom\":16")
        assertThat(script).contains("catch(e)")
    }

    @Test
    fun `publish broadcasts only when the insets change`() {
        SafeAreaInsetsBridge.publish(SafeAreaInsets(Int.MAX_VALUE, 0, 0, 0))
        SafeAreaInsetsBridge.publish(SafeAreaInsets(Int.MIN_VALUE, 0, 0, 0))
        val received = mutableListOf<SafeAreaInsets>()
        val unsubscribe = SafeAreaInsetsBridge.addListener { received.add(it) }
        try {
            val first = SafeAreaInsets(31, 0, 42, 0)
            SafeAreaInsetsBridge.publish(first)
            SafeAreaInsetsBridge.publish(first)
            val second = SafeAreaInsets(53, 0, 64, 0)
            SafeAreaInsetsBridge.publish(second)
            assertThat(received).containsExactly(first, second).inOrder()
            assertThat(SafeAreaInsetsBridge.currentJson).isEqualTo(second.toJson())
        } finally {
            unsubscribe()
        }
    }

    @Test
    fun `unsubscribe stops broadcasts`() {
        SafeAreaInsetsBridge.publish(SafeAreaInsets(Int.MAX_VALUE, 0, 0, 0))
        val received = mutableListOf<SafeAreaInsets>()
        val unsubscribe = SafeAreaInsetsBridge.addListener { received.add(it) }
        unsubscribe()
        SafeAreaInsetsBridge.publish(SafeAreaInsets(7, 0, 9, 0))
        assertThat(received).isEmpty()
    }

    @Test
    fun `never cutout mode reports zero insets`() {
        val activity = Robolectric.setupActivity(Activity::class.java)
        assertThat(SafeAreaInsetsBridge.read(activity, DisplayCutoutMode.NEVER, 2f))
            .isEqualTo(SafeAreaInsets.ZERO)
    }

    @Test
    fun `short edges mode reports zero when the window has no cutout insets`() {
        val activity = Robolectric.setupActivity(Activity::class.java)
        assertThat(SafeAreaInsetsBridge.read(activity, DisplayCutoutMode.SHORT_EDGES, 2f))
            .isEqualTo(SafeAreaInsets.ZERO)
    }

    @Test
    @Config(sdk = [27])
    fun `pre-P devices report zero insets`() {
        val activity = Robolectric.setupActivity(Activity::class.java)
        assertThat(SafeAreaInsetsBridge.read(activity, DisplayCutoutMode.SHORT_EDGES, 2f))
            .isEqualTo(SafeAreaInsets.ZERO)
    }

    @Test
    fun `non-positive density never divides`() {
        val activity = Robolectric.setupActivity(Activity::class.java)
        assertThat(SafeAreaInsetsBridge.read(activity, DisplayCutoutMode.SHORT_EDGES, 0f))
            .isEqualTo(SafeAreaInsets.ZERO)
    }
}
