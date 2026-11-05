package com.webtoapp.core.extension

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class DeriveOriginUrlTest {

    @Test
    fun `first concrete host wins`() {
        val origin = deriveOriginUrl(
            listOf(
                UrlMatchRule(pattern = "<all_urls>"),
                UrlMatchRule(pattern = "https://www.instagram.com/*ext=tf*uuid=*"),
                UrlMatchRule(pattern = "https://example.com/*")
            ),
            extensionId = "gooibnpodkckifdenemmdmpfjhcbidib"
        )

        assertThat(origin).isEqualTo("https://www.instagram.com/")
    }

    @Test
    fun `excluded and regex rules do not supply the origin`() {
        val origin = deriveOriginUrl(
            listOf(
                UrlMatchRule(pattern = "https://example.com/*", exclude = true),
                UrlMatchRule(pattern = "https://.*\\.example\\.com/.*", isRegex = true)
            ),
            extensionId = "gooibnpodkckifdenemmdmpfjhcbidib"
        )

        assertThat(origin).isEqualTo("https://gooibnpodkckifdenemmdmpfjhcbidib.chromiumapp.org/")
    }

    @Test
    fun `no usable rule falls back to the extension origin`() {
        assertThat(deriveOriginUrl(emptyList(), extensionId = "ext-id"))
            .isEqualTo("https://ext-id.chromiumapp.org/")
        assertThat(deriveOriginUrl(emptyList()))
            .isEqualTo("about:blank")
    }
}
