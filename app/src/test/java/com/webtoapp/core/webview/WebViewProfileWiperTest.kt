package com.webtoapp.core.webview

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File

class WebViewProfileWiperTest {

    @Test
    fun `cold profile wipe removes cookies code cache and the legacy webview database`() {
        val dataDir = tempDir()
        val cacheDir = tempDir()
        val cookie = File(dataDir, "app_webview/Default/Cookies").also {
            it.parentFile!!.mkdirs()
            it.writeText("pc_session=v1.leftover")
        }
        val codeCache = File(dataDir, "app_webview/Default/Code Cache/js/index").also {
            it.parentFile!!.mkdirs()
            it.writeText("cached-bytecode")
        }
        val serviceWorker = File(dataDir, "app_webview/Default/Service Worker/Database/CURRENT").also {
            it.parentFile!!.mkdirs()
            it.writeText("sw")
        }
        val legacy = File(dataDir, "databases/webview.db").also {
            it.parentFile!!.mkdirs()
            it.writeText("legacy")
        }
        val httpCache = File(cacheDir, "WebView/Cache/entry").also {
            it.parentFile!!.mkdirs()
            it.writeText("cached-response")
        }
        // The shell Room database must survive. Only the WebView files go.
        val appDb = File(dataDir, "databases/webtoapp.db").also {
            it.parentFile!!.mkdirs()
            it.writeText("app")
        }

        val wiped = WebViewProfileWiper.wipe(dataDir, cacheDir, providerInitialized = false)

        assertThat(wiped).isTrue()
        assertThat(cookie.exists()).isFalse()
        assertThat(codeCache.exists()).isFalse()
        assertThat(serviceWorker.exists()).isFalse()
        assertThat(File(dataDir, "app_webview").exists()).isFalse()
        assertThat(legacy.exists()).isFalse()
        assertThat(httpCache.exists()).isFalse()
        assertThat(appDb.readText()).isEqualTo("app")
    }

    @Test
    fun `an already-live WebView provider keeps the profile directory`() {
        val dataDir = tempDir()
        val cacheDir = tempDir()
        val cookie = File(dataDir, "app_webview/Default/Cookies").also {
            it.parentFile!!.mkdirs()
            it.writeText("open-db")
        }

        val wiped = WebViewProfileWiper.wipe(dataDir, cacheDir, providerInitialized = true)

        assertThat(wiped).isFalse()
        assertThat(cookie.readText()).isEqualTo("open-db")
    }

    @Test
    fun `a process with no profile yet counts as wiped`() {
        val wiped = WebViewProfileWiper.wipe(tempDir(), tempDir(), providerInitialized = false)
        assertThat(wiped).isTrue()
    }

    private fun tempDir(): File =
        File(System.getProperty("java.io.tmpdir"), "wta-profile-" + System.nanoTime()).apply { mkdirs() }
}
