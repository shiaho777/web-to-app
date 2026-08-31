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
        val asset = "https://github.com/oct/x/releases/download/v1/a.zip"
        val urls = GitHubMirror.proxiedCn(asset)
        assertThat(urls).hasSize(GitHubMirror.CN_PROXIES.size + 1)
        // No probe cache in tests: declaration order (proxies first), direct last.
        GitHubMirror.CN_PROXIES.forEachIndexed { i, proxy ->
            assertThat(urls[i]).isEqualTo(proxy.rewrite(asset))
        }
        assertThat(urls.last()).isEqualTo(asset)
    }

    @Test
    fun `direct channel leaves the url untouched`() {
        val asset = "https://github.com/oct/x/releases/download/v1/a.zip"
        assertThat(GitHubMirror.MirrorChannel.DIRECT.rewrite(asset)).isEqualTo(asset)
    }

    @Test
    fun `every proxy channel rewrites to a prefix url`() {
        val asset = "https://github.com/oct/x/releases/download/v1/a.zip"
        GitHubMirror.CN_PROXIES.forEach { channel ->
            val rewritten = channel.rewrite(asset)
            assertThat(rewritten).startsWith(channel.prefix)
            assertThat(rewritten).endsWith(asset)
        }
    }

    @Test
    fun `channel pool has no duplicate ids`() {
        val ids = GitHubMirror.ALL_CHANNELS.map { it.id }
        assertThat(ids).containsNoDuplicates()
        assertThat(ids).contains("direct")
    }

    @Test
    fun `proxiedCn passes non-github urls through untouched`() {
        assertThat(GitHubMirror.proxiedCn("https://wordpress.org/latest.tar.gz"))
            .containsExactly("https://wordpress.org/latest.tar.gz")
    }

    @Test
    fun `api and raw hosts expand like release assets do`() {
        listOf(
            "https://api.github.com/repos/shiaho777/web-to-app/releases/latest",
            "https://raw.githubusercontent.com/shiaho777/web-to-app/main/modules/registry.json",
            "https://github.com/shiaho777/web-to-app/releases/download/v1/a.apk"
        ).forEach { url ->
            val urls = GitHubMirror.proxiedCnGitHubHost(url)
            assertThat(urls).hasSize(GitHubMirror.CN_PROXIES.size + 1)
            // The plain URL is always last so there is somewhere to fall back to.
            assertThat(urls.last()).isEqualTo(url)
        }
    }

    @Test
    fun `cdn hosts are not treated as github hosts`() {
        val url = "https://cdn.jsdelivr.net/gh/shiaho777/web-to-app@main/modules/registry.json"
        assertThat(GitHubMirror.proxiedCnGitHubHost(url)).containsExactly(url)
    }

    @Test
    fun `release downloads keep using the untouched proxiedCn path`() {
        // Widen proxiedCn to api.github.com later and this fails: the release
        // path was measured and shipped, so it should not drift on its own.
        val api = "https://api.github.com/repos/shiaho777/web-to-app/releases/latest"
        assertThat(GitHubMirror.proxiedCn(api)).containsExactly(api)
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
