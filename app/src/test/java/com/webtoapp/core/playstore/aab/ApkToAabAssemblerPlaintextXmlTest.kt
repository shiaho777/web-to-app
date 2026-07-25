package com.webtoapp.core.playstore.aab

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Regression guard for issue #272: the AAB assembler must recognize plain-text res XML files (the
 * AGP resource-shrinker's `tools:keep` / `tools:discard` marker, left uncompiled under `res/`) and
 * skip them instead of handing them to the binary AXML parser, which throws `Not an AXML file`.
 *
 * These tests lock the format-detection contract independently of the real shell template (whose
 * contents shift over time and which CI may not have built).
 */
class ApkToAabAssemblerPlaintextXmlTest {

    @Test
    fun `detects tools keep marker as plaintext`() {
        // Exact content of res/qF.xml from the shell template (Firebase / AGP shrinker output).
        val keepXml = (
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<resources xmlns:tools=\"http://schemas.android.com/tools\" " +
                "tools:keep=\"@string/google_app_id,@string/gcm_defaultSenderId\" />"
        ).toByteArray(Charsets.UTF_8)

        assertThat(ApkToAabAssembler.isPlaintextXml(keepXml)).isTrue()
    }

    @Test
    fun `detects generic plaintext xml by leading angle bracket`() {
        val plain = "<root/>".toByteArray()
        assertThat(ApkToAabAssembler.isPlaintextXml(plain)).isTrue()
    }

    @Test
    fun `binary AXML is not flagged as plaintext`() {
        // Binary AXML chunk header: type=0x0003 (RES_XML_TYPE), headerSize=0x0008, little-endian.
        val binaryAxml = byteArrayOf(0x03, 0x00, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00)
        assertThat(ApkToAabAssembler.isPlaintextXml(binaryAxml)).isFalse()
    }

    @Test
    fun `empty or too-short input is not flagged`() {
        assertThat(ApkToAabAssembler.isPlaintextXml(byteArrayOf())).isFalse()
        assertThat(ApkToAabAssembler.isPlaintextXml(byteArrayOf(0x3c))).isFalse()
    }
}
