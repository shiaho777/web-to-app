package com.webtoapp.core.webview

import android.net.Uri
import android.webkit.PermissionRequest
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class CapturePermissionBridgeTest {

    @Test
    fun `a request that survives the dialog is granted without a reload`() {
        val bridge = CapturePermissionBridge()
        var granted = false
        bridge.remember(request(onGrant = { granted = true }))
        assertThat(bridge.onRuntimeResult(runtimeGranted = true)).isFalse()
        assertThat(granted).isTrue()
    }

    @Test
    fun `a request cancelled by the dialog reloads after the user allows`() {
        val bridge = CapturePermissionBridge()
        val request = request(onGrant = { throw IllegalStateException("cancelled") })
        bridge.remember(request)
        bridge.onCanceled(request)
        assertThat(bridge.onRuntimeResult(runtimeGranted = true)).isTrue()
    }

    @Test
    fun `a grant that throws reloads because the page already lost that request`() {
        val bridge = CapturePermissionBridge()
        bridge.remember(request(onGrant = { throw IllegalStateException("already handled") }))
        assertThat(bridge.onRuntimeResult(runtimeGranted = true)).isTrue()
    }

    @Test
    fun `a denial does not reload`() {
        val bridge = CapturePermissionBridge()
        val request = request(onGrant = { throw IllegalStateException("cancelled") })
        bridge.remember(request)
        bridge.onCanceled(request)
        assertThat(bridge.onRuntimeResult(runtimeGranted = false)).isFalse()
    }

    @Test
    fun `a result with no capture in flight does not reload`() {
        assertThat(CapturePermissionBridge().onRuntimeResult(runtimeGranted = true)).isFalse()
    }

    private fun request(onGrant: () -> Unit): PermissionRequest {
        return object : PermissionRequest() {
            override fun getOrigin(): Uri = Uri.parse("https://face.example")
            override fun getResources(): Array<String> = arrayOf(PermissionRequest.RESOURCE_VIDEO_CAPTURE)
            override fun grant(resources: Array<String>?) = onGrant()
            override fun deny() {}
        }
    }
}
