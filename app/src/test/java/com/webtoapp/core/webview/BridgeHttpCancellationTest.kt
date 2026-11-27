package com.webtoapp.core.webview

import com.google.common.truth.Truth.assertThat
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Test

class BridgeHttpCancellationTest {
    private val client = OkHttpClient()
    private val request = Request.Builder().url("https://example.com/").build()

    @Test
    fun `cancellation before the native call attaches prevents execution`() {
        val cancellation = BridgeHttpCancellation()
        val call = client.newCall(request)
        cancellation.close()
        assertThat(cancellation.attach(call)).isFalse()
        assertThat(call.isCanceled()).isTrue()
    }

    @Test
    fun `cancellation reaches an attached native call`() {
        val cancellation = BridgeHttpCancellation()
        val call = client.newCall(request)
        assertThat(cancellation.attach(call)).isTrue()
        cancellation.close()
        assertThat(call.isCanceled()).isTrue()
    }
}
