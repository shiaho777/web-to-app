package com.webtoapp.core.network

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class GitHubMirrorTest {

    @Test
    fun `proxiedCn expands a github release asset with direct fallback`() {
        val urls = GitHubMirror.proxiedCn("https://github.com/oct/x/releases/download/v1/a.zip")
        assertThat(urls).hasSize(GitHubMirror.CN_PROXIES.size + 1)
        // No probe cache in tests: base order (proxies first), direct last.
        GitHubMirror.CN_PROXIES.forEachIndexed { i, proxy ->
            assertThat(urls[i]).isEqualTo(proxy + "https://github.com/oct/x/releases/download/v1/a.zip")
        }
        assertThat(urls.last()).isEqualTo("https://github.com/oct/x/releases/download/v1/a.zip")
    }

    @Test
    fun `proxiedCn passes non-github urls through untouched`() {
        assertThat(GitHubMirror.proxiedCn("https://wordpress.org/latest.tar.gz"))
            .containsExactly("https://wordpress.org/latest.tar.gz")
    }

    @Test
    fun `npmTarballUrls prefers npmmirror only in cn region`() {
        val url = "https://registry.npmjs.org/npm/-/npm-10.9.0.tgz"
        assertThat(GitHubMirror.npmTarballUrls(url, cn = true)).containsExactly(
            "https://registry.npmmirror.com/npm/-/npm-10.9.0.tgz",
            url
        ).inOrder()
        assertThat(GitHubMirror.npmTarballUrls(url, cn = false)).containsExactly(url)
    }

    @Test
    fun `npmTarballUrls leaves foreign hosts alone`() {
        assertThat(GitHubMirror.npmTarballUrls("https://example.com/x.tgz", cn = true))
            .containsExactly("https://example.com/x.tgz")
    }
}
