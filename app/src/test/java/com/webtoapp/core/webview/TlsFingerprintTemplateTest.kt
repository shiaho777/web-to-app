package com.webtoapp.core.webview

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TlsFingerprintTemplateTest {

    @Test
    fun `fromId returns correct template for known ids`() {
        assertThat(TlsFingerprintTemplate.fromId("CHROME_131")).isEqualTo(TlsFingerprintTemplate.CHROME_131)
        assertThat(TlsFingerprintTemplate.fromId("FIREFOX_133")).isEqualTo(TlsFingerprintTemplate.FIREFOX_133)
        assertThat(TlsFingerprintTemplate.fromId("SAFARI_18")).isEqualTo(TlsFingerprintTemplate.SAFARI_18)
    }

    @Test
    fun `fromId defaults to Chrome for unknown id`() {
        assertThat(TlsFingerprintTemplate.fromId("UNKNOWN")).isEqualTo(TlsFingerprintTemplate.CHROME_131)
        assertThat(TlsFingerprintTemplate.fromId("")).isEqualTo(TlsFingerprintTemplate.CHROME_131)
    }

    @Test
    fun `all templates have non-empty cipher suites`() {
        TlsFingerprintTemplate.entries.forEach { template ->
            assertThat(template.cipherSuites).isNotEmpty()
            assertThat(template.protocols).isNotEmpty()
            assertThat(template.alpn).isNotEmpty()
        }
    }

    @Test
    fun `all templates include TLS 1_3 cipher suites`() {
        TlsFingerprintTemplate.entries.forEach { template ->
            val hasTls13 = template.cipherSuites.any { it.startsWith("TLS_AES_") || it.startsWith("TLS_CHACHA20") }
            assertThat(hasTls13).isTrue()
        }
    }

    @Test
    fun `all templates include h2 in ALPN`() {
        TlsFingerprintTemplate.entries.forEach { template ->
            assertThat(template.alpn).contains("h2")
            assertThat(template.alpn).contains("http/1.1")
        }
    }

    @Test
    fun `isCustom returns true only for CUSTOM id`() {
        assertThat(TlsFingerprintTemplate.isCustom("CUSTOM")).isTrue()
        assertThat(TlsFingerprintTemplate.isCustom("CHROME_131")).isFalse()
        assertThat(TlsFingerprintTemplate.isCustom("FIREFOX_133")).isFalse()
    }

    @Test
    fun `template ids are unique`() {
        val ids = TlsFingerprintTemplate.entries.map { it.id }
        assertThat(ids.toSet().size).isEqualTo(ids.size)
    }

    @Test
    fun `Chrome template has more cipher suites than Firefox`() {
        assertThat(TlsFingerprintTemplate.CHROME_131.cipherSuites.size)
            .isAtLeast(TlsFingerprintTemplate.FIREFOX_133.cipherSuites.size)
    }

    @Test
    fun `Chrome includes legacy TLS versions but Firefox does not`() {
        assertThat(TlsFingerprintTemplate.CHROME_131.protocols).contains("TLSv1")
        assertThat(TlsFingerprintTemplate.CHROME_131.protocols).contains("TLSv1.1")
        assertThat(TlsFingerprintTemplate.FIREFOX_133.protocols).doesNotContain("TLSv1")
        assertThat(TlsFingerprintTemplate.FIREFOX_133.protocols).doesNotContain("TLSv1.1")
    }
}
