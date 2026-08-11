package com.webtoapp.core.apkbuilder

import com.google.common.truth.Truth.assertThat
import com.webtoapp.data.model.CustomCaCertificate
import com.webtoapp.data.model.NetworkTrustConfig
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class NetworkSecurityConfigBuilderTest {

    @get:Rule val tmp = TemporaryFolder()

    @Test
    fun `default config trusts system anchors only`() {
        val xml = NetworkSecurityConfigBuilder.build(NetworkTrustConfig())

        assertThat(xml).contains("""<certificates src="system" />""")
        assertThat(xml).doesNotContain("""<certificates src="user" />""")
        assertThat(xml).contains("cleartextTrafficPermitted=\"true\"")
    }

    @Test
    fun `disabling user ca removes user anchor`() {
        val xml = NetworkSecurityConfigBuilder.build(
            NetworkTrustConfig(trustUserCa = false)
        )

        assertThat(xml).contains("""<certificates src="system" />""")
        assertThat(xml).doesNotContain("""<certificates src="user" />""")
    }

    @Test
    fun `custom ca certificates map to stable raw resources`() {
        val caFile = tmp.newFile("dev-ca.cer")
        val xml = NetworkSecurityConfigBuilder.build(
            NetworkTrustConfig(
                customCaCertificates = listOf(
                    CustomCaCertificate(
                        id = "one",
                        displayName = "Dev CA",
                        filePath = caFile.absolutePath,
                        sha256 = "abc"
                    )
                )
            )
        )

        assertThat(xml).contains("""<certificates src="@raw/wta_custom_ca_1" />""")
    }

    @Test
    fun `missing middle cert does not desync anchor and raw entry indices`() {
        // Three certs, the middle one's file is missing. The anchor list and the raw entries
        // must both skip it and stay sequentially indexed (no @raw/wta_custom_ca_2 without a file).
        val first = tmp.newFile("a.cer")
        val third = tmp.newFile("c.cer")
        val config = NetworkTrustConfig(
            customCaCertificates = listOf(
                CustomCaCertificate(id = "a", displayName = "A", filePath = first.absolutePath, sha256 = "1"),
                CustomCaCertificate(id = "b", displayName = "B", filePath = "/does/not/exist.cer", sha256 = "2"),
                CustomCaCertificate(id = "c", displayName = "C", filePath = third.absolutePath, sha256 = "3")
            )
        )

        val xml = NetworkSecurityConfigBuilder.build(config)
        val entries = NetworkSecurityConfigBuilder.customRawEntries(config)

        // Missing cert dropped, remaining two re-indexed 1 and 2 — anchor list and entries agree.
        assertThat(entries.map { it.resourceName })
            .containsExactly("wta_custom_ca_1", "wta_custom_ca_2").inOrder()
        assertThat(xml).contains("""<certificates src="@raw/wta_custom_ca_1" />""")
        assertThat(xml).contains("""<certificates src="@raw/wta_custom_ca_2" />""")
        assertThat(xml).doesNotContain("""<certificates src="@raw/wta_custom_ca_3" />""")
    }
}

