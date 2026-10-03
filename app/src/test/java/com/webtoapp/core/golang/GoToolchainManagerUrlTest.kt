package com.webtoapp.core.golang

import com.google.common.truth.Truth.assertThat
import com.webtoapp.core.i18n.AppLanguage
import java.util.Locale
import org.junit.After
import org.junit.Test

class GoToolchainManagerUrlTest {

    private val expectedOfficialPath =
        "/go/go1.26.4.linux-arm64.tar.gz"

    private val expectedUstcPath =
        "/golang/go1.26.4.linux-arm64.tar.gz"
    private val originalLocale: Locale = Locale.getDefault()

    @After
    fun restoreLocale() {
        Locale.setDefault(originalLocale)
    }

    @Test
    fun `prefer china mirror true yields CN mirrors first then official fallback`() {
        val urls = GoToolchainManager.selectGoArchiveUrls(preferChinaMirror = true)

        assertThat(urls).isNotEmpty()
        assertThat(urls.first()).contains("ustc.edu.cn")
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
    fun `chinese app language is enough to prefer china mirror`() {
        Locale.setDefault(Locale.US)
        assertThat(GoToolchainManager.shouldPreferChinaMirror(AppLanguage.CHINESE)).isTrue()
    }

    @Test
    fun `system zh-CN locale is enough to prefer china mirror even with english app lang`() {
        Locale.setDefault(Locale.SIMPLIFIED_CHINESE)
        assertThat(GoToolchainManager.shouldPreferChinaMirror(AppLanguage.ENGLISH)).isTrue()
    }

    @Test
    fun `english app and english system locale skips china mirror`() {
        Locale.setDefault(Locale.US)
        assertThat(GoToolchainManager.shouldPreferChinaMirror(AppLanguage.ENGLISH)).isFalse()
    }

    @Test
    fun `arabic app and arabic system locale skips china mirror`() {
        Locale.setDefault(Locale("ar"))
        assertThat(GoToolchainManager.shouldPreferChinaMirror(AppLanguage.ARABIC)).isFalse()
    }

    @Test
    fun `ustc mirror URL contains expected golang path`() {
        val urls = GoToolchainManager.selectGoArchiveUrls(preferChinaMirror = true)

        assertThat(urls).isNotEmpty()
        val ustc = urls.first { it.contains("ustc.edu.cn") }
        assertThat(ustc).endsWith(expectedUstcPath)
    }

    @Test
    fun `official source URL contains expected go path`() {
        val urls = GoToolchainManager.selectGoArchiveUrls(preferChinaMirror = true)
        val official = urls.first { it.contains("dl.google.com") }
        assertThat(official).endsWith(expectedOfficialPath)
    }
}
