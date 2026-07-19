package com.webtoapp.core.market

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class GreasyForkSearchTest {

    @Test
    fun `parses array search response with author and stats`() {
        val raw = """
            [
              {
                "id": 12345,
                "name": "Dark Mode Helper",
                "description": "Applies a gentle dark theme",
                "version": "1.2.3",
                "code_url": "https://greasyfork.org/scripts/12345/code.user.js",
                "url": "https://greasyfork.org/scripts/12345-dark-mode-helper",
                "users": [
                  {"name": "alice", "url": "https://greasyfork.org/users/1"}
                ],
                "fan_score": "98.5",
                "total_installs": 1500,
                "daily_installs": 12,
                "good_ratings": 10,
                "ok_ratings": 1,
                "bad_ratings": 0,
                "code_updated_at": "2026-01-01T00:00:00Z",
                "license": "MIT",
                "locale": "en",
                "code_size": 2048
              }
            ]
        """.trimIndent()

        val results = GreasyForkSearch.parseSearchResponse(raw)
        assertThat(results).hasSize(1)

        val script = results.first()
        assertThat(script.id).isEqualTo(12345L)
        assertThat(script.name).isEqualTo("Dark Mode Helper")
        assertThat(script.description).contains("dark theme")
        assertThat(script.version).isEqualTo("1.2.3")
        assertThat(script.codeUrl).endsWith("code.user.js")
        assertThat(script.pageUrl).contains("12345")
        assertThat(script.author).isEqualTo("alice")
        assertThat(script.authorUrl).isEqualTo("https://greasyfork.org/users/1")
        assertThat(script.fanScore).isWithin(0.01).of(98.5)
        assertThat(script.totalInstalls).isEqualTo(1500L)
        assertThat(script.dailyInstalls).isEqualTo(12L)
        assertThat(script.ratingsTotal).isEqualTo(11L)
        assertThat(script.license).isEqualTo("MIT")
        assertThat(script.codeSize).isEqualTo(2048L)
    }

    @Test
    fun `parses object envelope with query array`() {
        val raw = """
            {
              "query": [
                {
                  "id": 9,
                  "name": "Envelope Script",
                  "description": "",
                  "version": "0.1",
                  "code_url": "https://example.com/a.user.js",
                  "url": "https://example.com/a",
                  "users": [],
                  "fan_score": 1,
                  "total_installs": 1,
                  "daily_installs": 0,
                  "good_ratings": 0,
                  "ok_ratings": 0,
                  "bad_ratings": 0,
                  "code_updated_at": "",
                  "license": "null",
                  "locale": "zh-CN",
                  "code_size": 10
                }
              ]
            }
        """.trimIndent()

        val results = GreasyForkSearch.parseSearchResponse(raw)
        assertThat(results).hasSize(1)
        assertThat(results.first().name).isEqualTo("Envelope Script")
        assertThat(results.first().license).isEmpty()
        assertThat(results.first().author).isEmpty()
    }

    @Test
    fun `skips entries without name and does not crash on garbage`() {
        val mixed = """
            [
              {"id": 1, "name": "", "code_url": "x"},
              {"id": 2, "name": "Keep Me", "code_url": "https://x/y.user.js", "url": "https://x/y",
               "users": null, "fan_score": null, "total_installs": 0, "daily_installs": 0,
               "good_ratings": 0, "ok_ratings": 0, "bad_ratings": 0, "code_updated_at": "",
               "license": "", "locale": "", "code_size": 0, "description": "", "version": "1"}
            ]
        """.trimIndent()

        val results = GreasyForkSearch.parseSearchResponse(mixed)
        assertThat(results).hasSize(1)
        assertThat(results.first().id).isEqualTo(2L)
        assertThat(results.first().name).isEqualTo("Keep Me")

        assertThat(GreasyForkSearch.parseSearchResponse("")).isEmpty()
        assertThat(GreasyForkSearch.parseSearchResponse("not-json")).isEmpty()
        assertThat(GreasyForkSearch.parseSearchResponse("{}")).isEmpty()
    }

    @Test
    fun `format helpers render install counts and scores`() {
        assertThat(GreasyForkSearch.formatInstallCount(999)).isEqualTo("999")
        assertThat(GreasyForkSearch.formatInstallCount(1500)).isEqualTo("1.5K")
        assertThat(GreasyForkSearch.formatInstallCount(2_500_000)).isEqualTo("2.5M")
        assertThat(GreasyForkSearch.formatScore(0.0)).isEqualTo("--")
        assertThat(GreasyForkSearch.formatScore(9.25)).isEqualTo("9.3")
    }

    @Test
    fun `favorite snapshot keeps installable fields`() {
        val result = GfSearchResult(
            id = 77,
            name = "Fav Script",
            description = "desc",
            version = "2.0",
            codeUrl = "https://greasyfork.org/scripts/77/code.user.js",
            pageUrl = "https://greasyfork.org/scripts/77",
            author = "bob",
            authorUrl = null,
            fanScore = 10.0,
            totalInstalls = 100,
            dailyInstalls = 3,
            goodRatings = 2,
            okRatings = 0,
            badRatings = 0,
            codeUpdatedAt = "2026-02-01T00:00:00Z",
            license = "MIT",
            locale = "en",
            codeSize = 100
        )
        val fav = GfFavorite.fromResult(result)
        assertThat(fav.scriptId).isEqualTo(77L)
        assertThat(fav.name).isEqualTo("Fav Script")
        assertThat(fav.codeUrl).isEqualTo(result.codeUrl)
        assertThat(fav.pageUrl).isEqualTo(result.pageUrl)
        assertThat(fav.version).isEqualTo("2.0")
        assertThat(fav.author).isEqualTo("bob")
        assertThat(fav.totalInstalls).isEqualTo(100L)
        assertThat(fav.savedAt).isGreaterThan(0L)
    }
}
