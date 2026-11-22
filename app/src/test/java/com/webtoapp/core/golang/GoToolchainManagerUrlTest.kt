package com.webtoapp.core.golang

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class GoToolchainManagerUrlTest {

    private val expectedOfficialPath =
        "/go/go1.26.10.linux-arm64.tar.gz"

    private val expectedHustPath =
        "/golang/go1.26.10.linux-arm64.tar.gz"
    @Test
    fun `prefer china mirror true yields CN mirrors first then official fallback`() {
        val urls = GoToolchainManager.selectGoArchiveUrls(preferChinaMirror = true)

        assertThat(urls).isNotEmpty()
        assertThat(urls.first()).contains("mirrors.hust.edu.cn")
        val official = "https://dl.google.com" + expectedOfficialPath
        assertThat(urls).contains(official)
        assertThat(urls.indexOfFirst { it.contains("dl.google.com") })
            .isEqualTo(urls.size - 1)
    }

    @Test
    fun `prefer china mirror false yields official source only`() {
        val urls = GoToolchainManager.selectGoArchiveUrls(preferChinaMirror = false)

        assertThat(urls).hasSize(1)
        assertThat(urls.first()).contains("dl.google.com")
    }

    @Test
    fun `hust mirror URL contains expected golang path`() {
        val urls = GoToolchainManager.selectGoArchiveUrls(preferChinaMirror = true)

        assertThat(urls).isNotEmpty()
        val hust = urls.first { it.contains("mirrors.hust.edu.cn") }
        assertThat(hust).endsWith(expectedHustPath)
    }

    @Test
    fun `official source URL contains expected go path`() {
        val urls = GoToolchainManager.selectGoArchiveUrls(preferChinaMirror = true)
        val official = urls.first { it.contains("dl.google.com") }
        assertThat(official).endsWith(expectedOfficialPath)
    }

    @Test
    fun `cached toolchain matches only the pinned VERSION line`() {
        val pinned = "go${GoToolchainManager.GO_VERSION}\ntime 2026-10-20T01:27:56Z\n"
        assertThat(GoToolchainManager.goReleaseMatches(pinned)).isTrue()
        assertThat(GoToolchainManager.goReleaseMatches("go1.26.4\n")).isFalse()
        assertThat(GoToolchainManager.goReleaseMatches(null)).isFalse()
        assertThat(GoToolchainManager.goReleaseMatches("\n")).isFalse()
    }
}
