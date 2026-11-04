package com.webtoapp.core.webview

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PwaOfflineSupportScriptTest {

    private val script = PwaOfflineSupport.generateSwScriptBody(
        PwaOfflineSupport.OfflineConfig(enabled = true)
    )

    @Test
    fun `navigation responses are cacheable and served offline in the default strategy`() {
        assertThat(script).contains("request.mode === 'navigate' || request.destination === 'document'")
        assertThat(script).contains("if (isNavigation(request)) return true;")
        assertThat(script).contains("return offlineFallback(request);")
        assertThat(script).contains("STRATEGY = 'network-first'")
        assertThat(script).doesNotContain("if (!isCacheable(request.url) && !request.url.endsWith('/')) return;")
    }

    @Test
    fun `extensionless cdn assets are cached through request destination`() {
        assertThat(script).contains("destination === 'script'")
        assertThat(script).contains("destination === 'style'")
        assertThat(script).contains("if (!isNavigation(request) && !mayBeCacheable(request)) return;")
        assertThat(script).doesNotContain("request.url.endsWith('/')")
    }

    @Test
    fun `activation only removes caches owned by this worker`() {
        assertThat(script).contains("CACHE_PREFIX = 'wta-offline-'")
        assertThat(script).contains("n.indexOf(CACHE_PREFIX) === 0 && n !== CACHE_NAME")
        assertThat(script).doesNotContain("names.filter(n => n !== CACHE_NAME)")
    }

    @Test
    fun `offline fallback tries the cached document before returning 503`() {
        assertThat(script).contains("caches.match(request, { ignoreSearch: true })")
        assertThat(script).contains("new URL('./', request.url).href")
        assertThat(script).contains("new Response('Offline', { status: 503")
    }

    @Test
    fun `registration skips a site that already owns a service worker`() {
        val registration = PwaOfflineSupport.generateRegistrationScript()
        assertThat(registration).contains("r.active || r.installing || r.waiting")
        assertThat(registration).contains("script.indexOf('__wta_offline_sw__') === -1")
        assertThat(registration).contains("skipping injected offline worker")
        assertThat(registration).doesNotContain("{ scope: '/' }")
        assertThat(registration).contains("new URL('./', location.href).pathname")
    }

    @Test
    fun `strategy selection is wired into the generated worker`() {
        val cacheFirst = PwaOfflineSupport.generateSwScriptBody(
            PwaOfflineSupport.OfflineConfig(
                strategy = PwaOfflineSupport.CacheStrategy.CACHE_FIRST
            )
        )
        assertThat(cacheFirst).contains("STRATEGY = 'cache-first'")
        val stale = PwaOfflineSupport.generateSwScriptBody(
            PwaOfflineSupport.OfflineConfig(
                strategy = PwaOfflineSupport.CacheStrategy.STALE_WHILE_REVALIDATE
            )
        )
        assertThat(stale).contains("STRATEGY = 'stale-while-revalidate'")
    }
}
