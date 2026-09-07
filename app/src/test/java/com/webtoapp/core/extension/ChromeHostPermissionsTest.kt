package com.webtoapp.core.extension

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ChromeHostPermissionsTest {

    @Test
    fun `all urls pattern matches everything`() {
        assertThat(ChromeHostPermissions.matches("<all_urls>", "https://any.example.com/path")).isTrue()
        assertThat(ChromeHostPermissions.matches("<all_urls>", "http://127.0.0.1:8080/x")).isTrue()
    }

    @Test
    fun `wildcard host and path match any host under the pattern`() {
        assertThat(ChromeHostPermissions.matches("https://*/*", "https://example.com/")).isTrue()
        assertThat(ChromeHostPermissions.matches("https://*.example.com/*", "https://a.example.com/x")).isTrue()
        assertThat(ChromeHostPermissions.matches("https://*.example.com/*", "https://example.com/x")).isTrue()
        // Only one level of subdomain for the explicit non-wildcard prefix is NOT how
        // Chrome treats `*.example.com` for host permissions (it includes the bare domain
        // and any depth), but deep subdomains must match too.
        assertThat(ChromeHostPermissions.matches("https://*.example.com/*", "https://a.b.example.com/x")).isTrue()
    }

    @Test
    fun `non-matching hosts schemes and paths are rejected`() {
        assertThat(ChromeHostPermissions.matches("https://*.example.com/*", "https://evil.com/")).isFalse()
        assertThat(ChromeHostPermissions.matches("https://example.com/*", "https://other.com/")).isFalse()
        assertThat(ChromeHostPermissions.matches("https://example.com/*", "http://example.com/")).isFalse()
        assertThat(ChromeHostPermissions.matches("https://example.com/exact/*", "https://example.com/other/x")).isFalse()
    }

    @Test
    fun `star scheme matches both http and https`() {
        assertThat(ChromeHostPermissions.matches("*://example.com/*", "http://example.com/")).isTrue()
        assertThat(ChromeHostPermissions.matches("*://example.com/*", "https://example.com/")).isTrue()
    }

    @Test
    fun `declared patterns read host permissions and url entries in permissions`() {
        val manifest = """
            {
              "permissions": ["storage", "https://api.example.com/*"],
              "host_permissions": ["*://*.service.io/*", "https://strict.site/allow/*"]
            }
        """.trimIndent()
        val patterns = ChromeHostPermissions.declaredPatterns(manifest)
        assertThat(patterns).containsExactly(
            "https://api.example.com/*",
            "*://*.service.io/*",
            "https://strict.site/allow/*"
        )
        // API-only strings (no scheme, no <all_urls>) are not host patterns.
        assertThat(patterns).doesNotContain("storage")
    }

    @Test
    fun `declared patterns tolerate broken manifests`() {
        assertThat(ChromeHostPermissions.declaredPatterns("not json")).isEmpty()
        assertThat(ChromeHostPermissions.declaredPatterns("{}")).isEmpty()
    }
}
