package com.webtoapp.core.crypto

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class EmbeddedKeyTest {

    @Test
    fun `encode-decode round-trips the raw key`() {
        val key = EmbeddedKey.generate()

        val decoded = EmbeddedKey.decode(EmbeddedKey.encode(key, "com.example.app"), "com.example.app")

        assertThat(decoded).isNotNull()
        assertThat(decoded!!.contentEquals(key)).isTrue()
    }

    @Test
    fun `stored form never contains the raw key bytes`() {
        val key = EmbeddedKey.generate()
        val encoded = EmbeddedKey.encode(key, "com.example.app")

        // Hex of raw key must not appear verbatim; masked output differs.
        assertThat(encoded).isNotEqualTo(key.joinToString("") { "%02x".format(it) })
        assertThat(encoded).hasLength(64)
    }

    @Test
    fun `different package name yields a different mask`() {
        val key = EmbeddedKey.generate()

        val a = EmbeddedKey.encode(key, "com.a.app")
        val b = EmbeddedKey.encode(key, "com.b.app")

        assertThat(a).isNotEqualTo(b)
        // Each still decodes under its own package.
        assertThat(EmbeddedKey.decode(a, "com.a.app")!!.contentEquals(key)).isTrue()
        assertThat(EmbeddedKey.decode(b, "com.b.app")!!.contentEquals(key)).isTrue()
    }

    @Test
    fun `decode rejects malformed input`() {
        assertThat(EmbeddedKey.decode("zz", "com.example.app")).isNull()
        assertThat(EmbeddedKey.decode("abcd", "com.example.app")).isNull()
        assertThat(EmbeddedKey.decode("", "com.example.app")).isNull()
    }
}
