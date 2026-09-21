package com.webtoapp.core.apkbuilder

import com.android.aapt.Resources
import com.google.common.truth.Truth.assertThat
import com.webtoapp.core.playstore.aab.axml.AxmlToProtoXml
import com.webtoapp.core.share.ShareReceiveContract
import java.io.File
import java.util.zip.ZipFile
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Verifies the "open with" manifest injection (`WebViewConfig.openWithEnabled`).
 *
 * The feature appends two `ACTION_VIEW` intent-filters to `ShellActivity` at the binary
 * AXML level — one mime-based, one `pathPattern`-based — so a generated APK shows up in the
 * system file-open sheet for text/config/code files. Structure matters here: declaring
 * mimeTypes and pathPatterns in ONE filter would AND the dimensions and never match, so the
 * tests pin down that the two filters exist separately.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class OpenWithIntentFilterTest {

    private val shellActivity = "com.webtoapp.ui.shell.ShellActivity"

    private fun shellManifest(): ByteArray? {
        val template = File("src/main/assets/template/webview_shell.apk")
        if (!template.exists()) return null
        return ZipFile(template).use { zip ->
            val entry = zip.getEntry("AndroidManifest.xml") ?: return null
            zip.getInputStream(entry).readBytes()
        }
    }

    private fun rebuild(axml: ByteArray, openWithEnabled: Boolean): ByteArray =
        AxmlRebuilder().expandAndModifyFull(
            axmlData = axml,
            originalPackage = "com.webtoapp",
            newPackage = "com.example.openwithtest",
            versionCode = 1,
            versionName = "1.0",
            permissions = listOf(
                "android.permission.INTERNET",
                "android.permission.ACCESS_NETWORK_STATE"
            ),
            requiredComponents = setOf(
                "com.webtoapp.WebToAppApplication",
                "com.webtoapp.ui.MainActivity",
                shellActivity,
                "androidx.core.content.FileProvider"
            ),
            openWithEnabled = openWithEnabled
        )

    private fun shellActivityFilters(axml: ByteArray): List<Resources.XmlElement> {
        val application = AxmlToProtoXml.convert(axml)
            .element
            .childList
            .filter { it.hasElement() }
            .map { it.element }
            .single { it.name == "application" }

        val activity = application.childList
            .filter { it.hasElement() }
            .map { it.element }
            .filter { it.name == "activity" }
            .single { element ->
                element.attributeList.any { it.name == "name" && it.value == shellActivity }
            }

        return activity.childList
            .filter { it.hasElement() }
            .map { it.element }
            .filter { it.name == "intent-filter" }
    }

    private fun Resources.XmlElement.actions(): List<String> = childList
        .filter { it.hasElement() }
        .map { it.element }
        .filter { it.name == "action" }
        .mapNotNull { element -> element.attributeList.firstOrNull { it.name == "name" }?.value }

    private fun Resources.XmlElement.categories(): List<String> = childList
        .filter { it.hasElement() }
        .map { it.element }
        .filter { it.name == "category" }
        .mapNotNull { element -> element.attributeList.firstOrNull { it.name == "name" }?.value }

    private fun Resources.XmlElement.dataElements(): List<Resources.XmlElement> = childList
        .filter { it.hasElement() }
        .map { it.element }
        .filter { it.name == "data" }

    private fun Resources.XmlElement.attr(name: String): String? =
        attributeList.firstOrNull { it.name == name }?.value

    private fun Resources.XmlElement.mimeTypes(): List<String> =
        dataElements().mapNotNull { it.attr("mimeType") }

    private fun Resources.XmlElement.pathPatterns(): List<String> =
        dataElements().mapNotNull { it.attr("pathPattern") }

    @Test
    fun `open-with registers mime and pathPattern view filters`() {
        val axml = shellManifest()
        assumeTrue("shell template not built - run ':app:syncShellTemplateApk' first", axml != null)

        val filters = shellActivityFilters(rebuild(axml!!, openWithEnabled = true))
        val viewFilters = filters.filter { "android.intent.action.VIEW" in it.actions() }

        // One mime filter + one extension filter — deliberately separate (see class doc).
        assertThat(viewFilters).hasSize(2)

        val mimeFilter = viewFilters.single { it.mimeTypes().isNotEmpty() }
        val patternFilter = viewFilters.single { it.pathPatterns().isNotEmpty() }

        for (filter in viewFilters) {
            assertThat(filter.categories()).contains("android.intent.category.DEFAULT")
            // Same reasoning as the share filter: BROWSABLE would expose the file handler
            // to any web page, which is not what "open with" means.
            assertThat(filter.categories()).doesNotContain("android.intent.category.BROWSABLE")
        }

        assertThat(mimeFilter.mimeTypes())
            .containsAtLeastElementsIn(ShareReceiveContract.OPEN_WITH_MIME_TYPES)

        val patterns = patternFilter.pathPatterns()
        // file + content scheme per declared extension.
        assertThat(patterns).hasSize(ShareReceiveContract.OPEN_WITH_EXTENSIONS.size * 2)
        assertThat(patterns).contains(".*\\.txt")
        assertThat(patterns).contains(".*\\.json")
        assertThat(patterns).contains(".*\\.kt")

        // Every pattern data element must carry scheme+host+pathPattern together — a
        // scheme-only <data> would turn the filter into a generic file handler.
        for (data in patternFilter.dataElements()) {
            if (data.attr("pathPattern") == null) continue
            assertThat(data.attr("scheme")).isIn(listOf("file", "content"))
            assertThat(data.attr("host")).isEqualTo("*")
        }
    }

    @Test
    fun `disabled open-with leaves the manifest without view filters`() {
        val axml = shellManifest()
        assumeTrue("shell template not built - run ':app:syncShellTemplateApk' first", axml != null)

        val filters = shellActivityFilters(rebuild(axml!!, openWithEnabled = false))
        val viewFilters = filters.filter { "android.intent.action.VIEW" in it.actions() }

        // A disabled build produces the same manifest surface as before the feature
        // existed — no VIEW filters, no file associations.
        assertThat(viewFilters.flatMap { it.mimeTypes() + it.pathPatterns() }).isEmpty()
    }

    @Test
    fun `open-with filters survive alongside share filters`() {
        val axml = shellManifest()
        assumeTrue("shell template not built - run ':app:syncShellTemplateApk' first", axml != null)

        val rebuilt = AxmlRebuilder().expandAndModifyFull(
            axmlData = axml!!,
            originalPackage = "com.webtoapp",
            newPackage = "com.example.openwithtest",
            versionCode = 1,
            versionName = "1.0",
            permissions = listOf("android.permission.INTERNET"),
            requiredComponents = setOf(
                "com.webtoapp.WebToAppApplication",
                "com.webtoapp.ui.MainActivity",
                shellActivity,
                "androidx.core.content.FileProvider"
            ),
            shareReceiveMimeTypes = listOf("image/*"),
            openWithEnabled = true
        )
        val filters = shellActivityFilters(rebuilt)

        assertThat(filters.filter { "android.intent.action.SEND" in it.actions() }).isNotEmpty()
        assertThat(filters.filter { "android.intent.action.VIEW" in it.actions() }).hasSize(2)
    }
}
