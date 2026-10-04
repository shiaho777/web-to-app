package com.webtoapp.core.privacy

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FingerprintGeneratorTest {

    @Test
    fun `generateFingerprint is deterministic for same seed`() {
        val first = FingerprintGenerator.generateFingerprint(seed = "demo-seed")
        val second = FingerprintGenerator.generateFingerprint(seed = "demo-seed")

        assertThat(first).isEqualTo(second)
    }

    @Test
    fun `generateFingerprint values stay in expected ranges`() {
        val fp = FingerprintGenerator.generateFingerprint(seed = "range-seed")

        assertThat(fp.screenWidth).isIn(360..430)
        assertThat(fp.screenHeight).isGreaterThan(fp.screenWidth)
        assertThat(fp.identifiesAsPhone()).isTrue()
        assertThat(listOf(24, 32)).contains(fp.colorDepth)
        assertThat(listOf(4, 6, 8, 10, 12, 16, 20, 24)).contains(fp.hardwareConcurrency)
        assertThat(listOf(4, 8, 16, 32)).contains(fp.deviceMemory)
        assertThat(fp.canvasNoise).isAtLeast(0f)
        assertThat(fp.canvasNoise).isLessThan(0.0001f)
        assertThat(fp.audioNoise).isAtLeast(0f)
        assertThat(fp.audioNoise).isLessThan(0.0001f)
    }

    @Test
    fun `every generated fingerprint asks sites for the mobile page`() {
        val fingerprints = (0 until 64).map { FingerprintGenerator.generateFingerprint("phone-$it") }

        assertThat(fingerprints.map { it.userAgent }.distinct().size).isGreaterThan(8)
        fingerprints.forEach { fp ->
            assertThat(fp.identifiesAsPhone()).isTrue()
            assertThat(fp.userAgent).doesNotContain("Windows NT")
            assertThat(fp.userAgent).doesNotContain("Macintosh")
            assertThat(fp.screenWidth).isLessThan(500)
            assertThat(fp.screenHeight).isGreaterThan(fp.screenWidth)
            assertThat(fp.maxTouchPoints).isAtLeast(1)
            if (fp.chUa.isNotEmpty()) {
                assertThat(fp.chUaMobile).isEqualTo("?1")
                assertThat(fp.chUaPlatform).isEqualTo("\"Android\"")
            }
            val hints = com.webtoapp.core.kernel.UserAgentProfileDeriver.derive(fp.userAgent)
            assertThat(hints).isNotNull()
            assertThat(hints!!.mobile).isTrue()
        }
    }

}
