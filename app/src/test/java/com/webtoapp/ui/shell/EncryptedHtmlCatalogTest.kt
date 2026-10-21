package com.webtoapp.ui.shell

import com.google.common.truth.Truth.assertThat
import com.webtoapp.core.webview.safeSiteRelativePath
import org.junit.Test

class EncryptedHtmlCatalogTest {

    @Test
    fun `encrypted tree is addressed by the real file names`() {
        val tree = mapOf(
            "html" to arrayOf("index.html.enc", "css"),
            "html/css" to arrayOf("app.css.enc", "responsive.css.enc"),
            "html/index.html.enc" to arrayOf(),
            "html/css/app.css.enc" to arrayOf(),
            "html/css/responsive.css.enc" to arrayOf(),
        )
        val catalog = catalogAssetTree("html") { tree[it] }

        assertThat(catalog.encrypted).isTrue()
        assertThat(catalog.files).containsEntry("index.html", "html/index.html.enc")
        assertThat(catalog.files).containsEntry("css/app.css", "html/css/app.css.enc")
        assertThat(catalog.directories).contains("css")
        assertThat(catalog.resolveEntry("")).isEqualTo("index.html")
    }

    @Test
    fun `plaintext tree is left on the extract path`() {
        val tree = mapOf(
            "html" to arrayOf("index.html"),
            "html/index.html" to arrayOf(),
        )
        val catalog = catalogAssetTree("html") { tree[it] }
        assertThat(catalog.encrypted).isFalse()
        assertThat(catalog.files).containsEntry("index.html", "html/index.html")
    }

    @Test
    fun `worker and wasm files ask for cross-origin isolation`() {
        val tree = mapOf(
            "html" to arrayOf("app.js.enc", "app.worker.js.enc"),
            "html/app.js.enc" to arrayOf(),
            "html/app.worker.js.enc" to arrayOf(),
        )
        assertThat(catalogAssetTree("html") { tree[it] }.needsCrossOriginIsolation()).isTrue()
    }

    @Test
    fun `request paths cannot escape the site`() {
        assertThat(safeSiteRelativePath("/css/app.css")).isEqualTo("css/app.css")
        assertThat(safeSiteRelativePath("/")).isEqualTo("")
        assertThat(safeSiteRelativePath("/../data/files/html_shell_site/app.css")).isNull()
        assertThat(safeSiteRelativePath("/css/../../app.css")).isNull()
    }
}
