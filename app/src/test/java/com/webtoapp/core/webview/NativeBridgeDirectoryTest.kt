package com.webtoapp.core.webview

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.webtoapp.data.model.NativeBridgeCapabilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NativeBridgeDirectoryTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `directory methods stay closed until the capability is on`() {
        val bridge = bridge(directory = false, pageUrl = "file:///android_asset/index.html")
        assertThat(JSONObject(bridge.directoryInfo()).getString("error")).isEqualTo("DISABLED")
        assertThat(JSONObject(bridge.listDirectory("")).getString("error")).isEqualTo("DISABLED")
        assertThat(JSONObject(bridge.pickDirectory("req")).getString("error")).isEqualTo("DISABLED")
    }

    @Test
    fun `a foreign page cannot use a granted folder`() {
        val bridge = bridge(directory = true, pageUrl = "https://evil.test/embed", origin = "https://app.test")
        assertThat(JSONObject(bridge.listDirectory("")).getString("error")).isEqualTo("CALLER_NOT_ALLOWED")
        assertThat(JSONObject(bridge.readDirectoryFile("a.txt", 0, 16)).getString("error")).isEqualTo("CALLER_NOT_ALLOWED")
        assertThat(JSONObject(bridge.writeDirectoryFile("a.txt", "", false)).getString("error")).isEqualTo("CALLER_NOT_ALLOWED")
    }

    @Test
    fun `the app origin and packaged pages may ask and get no grant yet`() {
        val remote = bridge(directory = true, pageUrl = "https://app.test/editor", origin = "https://app.test")
        val info = JSONObject(remote.directoryInfo())
        assertThat(info.getBoolean("ok")).isTrue()
        assertThat(info.getBoolean("granted")).isFalse()

        val local = bridge(directory = true, pageUrl = "file:///data/index.html", origin = "")
        assertThat(JSONObject(local.listDirectory("")).getString("error")).isEqualTo("NO_GRANT")
    }

    @Test
    fun `pick reports that this window has no folder picker`() {
        DirectoryPickerRegistry.host?.let { DirectoryPickerRegistry.uninstall(it) }
        val bridge = bridge(directory = true, pageUrl = "file:///data/index.html", origin = "")
        val result = JSONObject(bridge.pickDirectory("req-1"))
        assertThat(result.getString("error")).isEqualTo("NO_ACTIVITY")
    }

    private fun bridge(directory: Boolean, pageUrl: String, origin: String = ""): NativeBridge {
        return NativeBridge(
            context = context,
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined),
            capabilities = NativeBridgeCapabilities(directory = directory),
            appOriginUrl = origin,
            callerPageUrlProvider = { pageUrl },
            directoryGrantKey = "bridge-test-${pageUrl.hashCode()}"
        )
    }
}
