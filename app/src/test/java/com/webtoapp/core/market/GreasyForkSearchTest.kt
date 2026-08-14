package com.webtoapp.core.market

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class GreasyForkSearchTest {

    @Test
    fun `scripts url targets api host and keeps query and sort params`() {
        val url = GreasyForkSearch.buildScriptsUrl("youtube downloader", "en", GfSort.TOTAL)

        assertThat(url).isEqualTo(
            "https://api.greasyfork.org/en/scripts.json?q=youtube+downloader&sort=total_installs"
        )
    }

    @Test
    fun `scripts url maps locale and omits q for browse`() {
        val url = GreasyForkSearch.buildScriptsUrl(null, "zh", GfSort.DAILY)

        assertThat(url).isEqualTo(
            "https://api.greasyfork.org/zh-CN/scripts.json?sort=daily_installs"
        )
    }

    @Test
    fun `browse category rides along as q param`() {
        val url = GreasyForkSearch.buildScriptsUrl(
            GfBrowseCategory.AD_BLOCKING.apiQuery,
            "en",
            GfSort.SCORE
        )

        assertThat(url).isEqualTo(
            "https://api.greasyfork.org/en/scripts.json?q=adblock&sort=fan_score"
        )
    }

    @Test
    fun `parses object-wrapped query array from the api`() {
        val raw = """
            {
              "query": [
                {
                  "id": 473330,
                  "name": "YouTube CPU Tamer by AnimationFrame",
                  "description": "Reduce CPU usage",
                  "version": "2.4.1",
                  "code_url": "https://update.greasyfork.org/scripts/473330/script.user.js",
                  "url": "https://greasyfork.org/en/scripts/473330-youtube-cpu-tamer-by-animationframe",
                  "users": [
                    {"id": 371179, "name": "𝖢𝖸 𝖥𝗎𝗇𝗀", "url": "https://greasyfork.org/users/371179"}
                  ],
                  "fan_score": 1.5,
                  "total_installs": 57210,
                  "daily_installs": 348,
                  "good_ratings": 5,
                  "ok_ratings": 0,
                  "bad_ratings": 1,
                  "code_updated_at": "2025-06-01T00:00:00.000Z",
                  "license": "MIT",
                  "locale": "en",
                  "code_size": 12345
                }
              ]
            }
        """.trimIndent()

        val results = GreasyForkSearch.parseSearchResponse(raw)

        assertThat(results).hasSize(1)
        val script = results.first()
        assertThat(script.id).isEqualTo(473330L)
        assertThat(script.name).isEqualTo("YouTube CPU Tamer by AnimationFrame")
        assertThat(script.author).isEqualTo("𝖢𝖸 𝖥𝗎𝗇𝗀")
        assertThat(script.codeUrl)
            .isEqualTo("https://update.greasyfork.org/scripts/473330/script.user.js")
        assertThat(script.totalInstalls).isEqualTo(57210L)
        assertThat(script.ratingsTotal).isEqualTo(6L)
        assertThat(script.license).isEqualTo("MIT")
    }

    @Test
    fun `parses legacy bare array response`() {
        val raw = """
            [
              {"id": 1, "name": "Legacy entry", "code_url": "https://example.org/a.user.js"}
            ]
        """.trimIndent()

        val results = GreasyForkSearch.parseSearchResponse(raw)

        assertThat(results).hasSize(1)
        assertThat(results.first().name).isEqualTo("Legacy entry")
    }

    @Test
    fun `empty query payloads parse to empty lists`() {
        assertThat(GreasyForkSearch.parseSearchResponse("""{"query": []}""")).isEmpty()
        assertThat(GreasyForkSearch.parseSearchResponse("[]")).isEmpty()
    }

    @Test
    fun `garbage input parses to empty list without throwing`() {
        assertThat(GreasyForkSearch.parseSearchResponse("not json at all")).isEmpty()
    }

    @Test
    fun `search with blank query short-circuits to empty success`() {
        val result = runBlocking { GreasyForkSearch.search("   ") }

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEmpty()
    }
}
