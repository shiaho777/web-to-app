package com.webtoapp.core.webview

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SiteWebProfilesTest {

    @Test
    fun `profile name is prefixed and keeps a stable site id`() {
        assertThat(SiteWebProfiles.profileNameFor("site_b")).isEqualTo("mw_site_b")
        assertThat(SiteWebProfiles.profileNameFor("550e8400-e29b-41d4-a716-446655440000"))
            .isEqualTo("mw_550e8400-e29b-41d4-a716-446655440000")
    }

    @Test
    fun `blank or unusable site ids yield no profile`() {
        assertThat(SiteWebProfiles.profileNameFor("")).isNull()
        assertThat(SiteWebProfiles.profileNameFor("   ")).isNull()
        assertThat(SiteWebProfiles.profileNameFor("///...")).isNull()
    }

    @Test
    fun `unsafe characters are stripped so the name stays filesystem safe`() {
        assertThat(SiteWebProfiles.profileNameFor("a b/c:d")).isEqualTo("mw_abcd")
        assertThat(SiteWebProfiles.profileNameFor("../escape")).isEqualTo("mw_escape")
    }

    @Test
    fun `distinct sites map to distinct profiles`() {
        assertThat(SiteWebProfiles.profileNameFor("site_a"))
            .isNotEqualTo(SiteWebProfiles.profileNameFor("site_b"))
    }
}