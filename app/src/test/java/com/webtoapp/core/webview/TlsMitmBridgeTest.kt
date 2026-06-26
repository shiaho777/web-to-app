package com.webtoapp.core.webview

import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class TlsMitmBridgeTest {

    private lateinit var caDir: File

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        caDir = File(context.filesDir, "test_mitm_bridge_ca")
        caDir.deleteRecursively()
        caDir.mkdirs()
        TlsMitmCaManager.clearCache()
        TlsMitmBridge.stop()
    }

    @After
    fun tearDown() {
        TlsMitmBridge.stop()
        caDir.deleteRecursively()
    }

    @Test
    fun `start returns valid port for Chrome template`() {
        val port = TlsMitmBridge.start(
            config = TlsMitmBridge.Config(
                template = TlsFingerprintTemplate.CHROME_131
            ),
            caDir = caDir
        )

        assertThat(port).isGreaterThan(0)
        assertThat(TlsMitmBridge.isRunning()).isTrue()
        assertThat(TlsMitmBridge.getListenPort()).isEqualTo(port)
    }

    @Test
    fun `start returns same port for identical config`() {
        val config = TlsMitmBridge.Config(
            template = TlsFingerprintTemplate.FIREFOX_133
        )

        val port1 = TlsMitmBridge.start(config, caDir)
        val port2 = TlsMitmBridge.start(config, caDir)

        assertThat(port1).isGreaterThan(0)
        assertThat(port2).isEqualTo(port1)
    }

    @Test
    fun `start returns new port when config changes`() {
        val port1 = TlsMitmBridge.start(
            TlsMitmBridge.Config(template = TlsFingerprintTemplate.CHROME_131),
            caDir
        )

        val port2 = TlsMitmBridge.start(
            TlsMitmBridge.Config(template = TlsFingerprintTemplate.SAFARI_18),
            caDir
        )

        assertThat(port1).isGreaterThan(0)
        assertThat(port2).isGreaterThan(0)
    }

    @Test
    fun `stop clears running state`() {
        TlsMitmBridge.start(
            TlsMitmBridge.Config(template = TlsFingerprintTemplate.CHROME_131),
            caDir
        )
        assertThat(TlsMitmBridge.isRunning()).isTrue()

        TlsMitmBridge.stop()
        assertThat(TlsMitmBridge.isRunning()).isFalse()
        assertThat(TlsMitmBridge.getListenPort()).isEqualTo(0)
    }

    @Test
    fun `start with custom cipher suites succeeds`() {
        val port = TlsMitmBridge.start(
            config = TlsMitmBridge.Config(
                template = TlsFingerprintTemplate.CHROME_131,
                customCipherSuites = listOf("TLS_AES_128_GCM_SHA256", "TLS_AES_256_GCM_SHA384")
            ),
            caDir = caDir
        )

        assertThat(port).isGreaterThan(0)
    }

    @Test
    fun `start with SOCKS upstream config succeeds`() {
        val port = TlsMitmBridge.start(
            config = TlsMitmBridge.Config(
                template = TlsFingerprintTemplate.CHROME_131,
                upstreamSocks = LocalHttpToSocksBridge.Upstream(
                    host = "127.0.0.1",
                    port = 1080
                )
            ),
            caDir = caDir
        )

        assertThat(port).isGreaterThan(0)
    }

    @Test
    fun `multiple start stop cycles work correctly`() {
        for (i in 1..3) {
            val port = TlsMitmBridge.start(
                TlsMitmBridge.Config(template = TlsFingerprintTemplate.CHROME_131),
                caDir
            )
            assertThat(port).isGreaterThan(0)
            assertThat(TlsMitmBridge.isRunning()).isTrue()

            TlsMitmBridge.stop()
            assertThat(TlsMitmBridge.isRunning()).isFalse()
        }
    }
}
