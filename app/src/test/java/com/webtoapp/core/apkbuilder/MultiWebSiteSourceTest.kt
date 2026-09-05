package com.webtoapp.core.apkbuilder

import com.google.common.truth.Truth.assertThat
import com.webtoapp.data.model.AppType
import com.webtoapp.data.model.WebApp
import org.junit.Test

class MultiWebSiteSourceTest {

    private fun app(id: Long, type: AppType) = WebApp(id = id, name = "app-$id", url = "https://example.com/$id", appType = type)

    @Test
    fun `normal source resolves`() {
        val source = app(12, AppType.WEB)
        assertThat(resolveMultiWebSiteSource(source, parentAppId = 7, siteName = "s")).isEqualTo(source)
    }

    @Test
    fun `nested multi-web source degrades to null`() {
        assertThat(resolveMultiWebSiteSource(app(12, AppType.MULTI_WEB), parentAppId = 7, siteName = "s")).isNull()
    }

    @Test
    fun `self reference degrades to null`() {
        assertThat(resolveMultiWebSiteSource(app(7, AppType.WEB), parentAppId = 7, siteName = "s")).isNull()
    }

    @Test
    fun `deleted source degrades to null`() {
        assertThat(resolveMultiWebSiteSource(null, parentAppId = 7, siteName = "s")).isNull()
    }

    @Test
    fun `unsaved parent cannot self reference`() {
        // id 0 means "not persisted yet": only the type guard applies.
        val source = app(12, AppType.WEB)
        assertThat(resolveMultiWebSiteSource(source, parentAppId = 0, siteName = "s")).isEqualTo(source)
    }
}
