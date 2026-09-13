package com.webtoapp.core.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UpdateCheckerAuthorReposTest {

    @Test
    fun `parseAuthorRepos filters forks and the app repo itself`() {
        val json = """
            [
              {"name":"web-to-app","description":"self","stargazers_count":999,"forks_count":10,"language":"Kotlin","pushed_at":"2026-01-01T00:00:00Z","html_url":"https://github.com/shiaho777/web-to-app","fork":false},
              {"name":"some-fork","description":"forked","stargazers_count":5,"forks_count":0,"language":"Java","pushed_at":"2026-01-02T00:00:00Z","html_url":"https://github.com/shiaho777/some-fork","fork":true},
              {"name":"cool-tool","description":"A tool","stargazers_count":42,"forks_count":7,"language":"Kotlin","pushed_at":"2026-01-03T00:00:00Z","html_url":"https://github.com/shiaho777/cool-tool","fork":false},
              {"name":"no-desc","description":null,"stargazers_count":0,"forks_count":0,"language":null,"pushed_at":"2026-01-04T00:00:00Z","html_url":"https://github.com/shiaho777/no-desc","fork":false}
            ]
        """.trimIndent()

        val repos = UpdateChecker.parseAuthorRepos(json)

        assertEquals(listOf("cool-tool", "no-desc"), repos.map { it.name })
        assertEquals("A tool", repos[0].description)
        assertEquals(42L, repos[0].stars)
        assertEquals(7L, repos[0].forks)
        assertEquals("Kotlin", repos[0].language)
        assertEquals("https://github.com/shiaho777/cool-tool", repos[0].url)
        assertEquals("", repos[1].description)
        assertNull(repos[1].language)
    }

    @Test
    fun `parseAuthorRepos handles empty array`() {
        assertTrue(UpdateChecker.parseAuthorRepos("[]").isEmpty())
    }
}
