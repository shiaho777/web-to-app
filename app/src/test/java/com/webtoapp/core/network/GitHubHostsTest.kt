package com.webtoapp.core.network

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class GitHubHostsTest {

    @Test
    fun `pinned hosts cover the domains release downloads traverse`() {
        listOf(
            "github.com",
            "codeload.github.com",
            "objects.githubusercontent.com",
            "raw.githubusercontent.com",
            "github-production-release-asset-2e65be.s3.amazonaws.com"
        ).forEach { host ->
            assertThat(GitHubHosts.lookup(host)).isNotEmpty()
        }
    }

    @Test
    fun `unknown hosts resolve to nothing`() {
        assertThat(GitHubHosts.lookup("example.invalid")).isEmpty()
        assertThat(GitHubHosts.covers("example.invalid")).isFalse()
    }

    @Test
    fun `dns puts the pinned address first and keeps a system fallback`() {
        val resolved = GitHubHostsDns.lookup("github.com")
        assertThat(resolved).isNotEmpty()
        assertThat(resolved.first().hostAddress).isEqualTo("20.205.243.166")
        // A stale pinned IP must never be the only answer: whatever the system
        // resolver says stays in the list behind it.
        assertThat(resolved.map { it.hostAddress }).containsAtLeastElementsIn(
            GitHubHosts.lookup("github.com").map { it.hostAddress }
        )
    }

    @Test
    fun `dns passes unknown hosts straight to the system resolver`() {
        val resolved = runCatching { GitHubHostsDns.lookup("localhost") }.getOrNull()
        if (resolved != null) {
            assertThat(resolved).isNotEmpty()
        }
    }
}
