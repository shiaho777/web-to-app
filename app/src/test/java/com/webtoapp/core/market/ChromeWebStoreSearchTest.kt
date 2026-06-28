package com.webtoapp.core.market

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ChromeWebStoreSearchTest {

    private fun loadFixture(name: String): String {
        val stream = javaClass.classLoader!!.getResourceAsStream("cws/$name")
            ?: error("fixture not found: cws/$name")
        return stream.bufferedReader().use { it.readText() }
    }

    @Test
    fun `parses real adblock search response with name storeId and icon`() {
        val raw = loadFixture("search_adblock.txt")
        val results = ChromeWebStoreSearch.parseBatchExecute(raw)

        assertThat(results).isNotNull()
        assertThat(results!!).isNotEmpty()

        val byStoreId = results.associateBy { it.storeId }

        val adblockPlus = byStoreId["cfhdojbkjhnklbpkdaibdccddilifddb"]
        assertThat(adblockPlus).isNotNull()
        assertThat(adblockPlus!!.name).isEqualTo("Adblock Plus - free ad blocker")
        assertThat(adblockPlus.iconUrl).startsWith("https://lh3.googleusercontent.com/")

        val adguard = byStoreId["bgnkhhnnamicmpeenaelnjfhikgbkllg"]
        assertThat(adguard).isNotNull()
        assertThat(adguard!!.name).isEqualTo("AdGuard AdBlocker")
    }

    @Test
    fun `every parsed result has valid storeId shape`() {
        val raw = loadFixture("search_adblock.txt")
        val results = ChromeWebStoreSearch.parseBatchExecute(raw) ?: emptyList()

        for (result in results) {
            assertThat(result.storeId).hasLength(32)
            assertThat(result.storeId.all { it.isLetterOrDigit() }).isTrue()
            assertThat(result.name).isNotEmpty()
        }
    }

    @Test
    fun `skips malformed entries without crashing`() {
        val raw = loadFixture("search_adblock.txt")
        val results = ChromeWebStoreSearch.parseBatchExecute(raw) ?: emptyList()

        val names = results.map { it.name }
        assertThat(names).doesNotContain("AdBlock Max - ad blocker")
    }

    @Test
    fun `returns empty list for no_result query`() {
        val raw = loadFixture("search_empty.txt")
        val results = ChromeWebStoreSearch.parseBatchExecute(raw)

        assertThat(results).isNotNull()
        assertThat(results!!).isEmpty()
    }

    @Test
    fun `returns null on garbage input`() {
        val results = ChromeWebStoreSearch.parseBatchExecute("not a valid response")
        assertThat(results).isNull()
    }
}
