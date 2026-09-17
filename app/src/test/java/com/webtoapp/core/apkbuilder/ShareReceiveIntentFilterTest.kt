package com.webtoapp.core.apkbuilder

import com.android.aapt.Resources
import com.google.common.truth.Truth.assertThat
import com.webtoapp.core.playstore.aab.axml.AxmlToProtoXml
import java.io.File
import java.util.zip.ZipFile
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Verifies the inbound share sheet manifest injection (issue #943).
 *
 * This is the half of the feature that cannot be exercised at runtime by a unit test — it
 * rewrites `AndroidManifest.xml` at the binary AXML level, appending new string-pool entries
 * and resource-map slots. A wrong string index or a mis-sized chunk produces a manifest that
 * either fails to install or silently never matches `ACTION_SEND`, and neither shows up in
 * host preview.
 *
 * The assertions read the rebuilt manifest back through [AxmlToProtoXml], so they check the
 * decoded structure rather than the byte layout — which is what the platform sees.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ShareReceiveIntentFilterTest {

    private val shellActivity = "com.webtoapp.ui.shell.ShellActivity"

    private fun shellManifest(): ByteArray? {
        val template = File("src/main/assets/template/webview_shell.apk")
        if (!template.exists()) return null
        return ZipFile(template).use { zip ->
            val entry = zip.getEntry("AndroidManifest.xml") ?: return null
            zip.getInputStream(entry).readBytes()
        }
    }

    private fun rebuild(
        axml: ByteArray,
        shareReceiveMimeTypes: List<String>
    ): ByteArray = AxmlRebuilder().expandAndModifyFull(
        axmlData = axml,
        originalPackage = "com.webtoapp",
        newPackage = "com.example.sharetest",
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
        shareReceiveMimeTypes = shareReceiveMimeTypes
    )

    /** All intent-filters declared on ShellActivity, decoded. */
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

    private fun Resources.XmlElement.mimeTypes(): List<String> = childList
        .filter { it.hasElement() }
        .map { it.element }
        .filter { it.name == "data" }
        .mapNotNull { element -> element.attributeList.firstOrNull { it.name == "mimeType" }?.value }

    @Test
    fun `image share registers send and send_multiple with the mime filter`() {
        val axml = shellManifest()
        assumeTrue("shell template not built - run ':app:syncShellTemplateApk' first", axml != null)

        val filters = shellActivityFilters(rebuild(axml!!, listOf("image/*")))

        val sendFilters = filters.filter {
            "android.intent.action.SEND" in it.actions() ||
                "android.intent.action.SEND_MULTIPLE" in it.actions()
        }
        assertThat(sendFilters).hasSize(2)

        val actions = sendFilters.flatMap { it.actions() }
        assertThat(actions).contains("android.intent.action.SEND")
        assertThat(actions).contains("android.intent.action.SEND_MULTIPLE")

        for (filter in sendFilters) {
            // DEFAULT is what makes the activity a candidate at all; BROWSABLE would also
            // expose the share target to any web page, which is not what sharing means.
            assertThat(filter.categories()).contains("android.intent.category.DEFAULT")
            assertThat(filter.categories()).doesNotContain("android.intent.category.BROWSABLE")
            assertThat(filter.mimeTypes()).contains("image/*")
        }
    }

    @Test
    fun `text shares add a text filter alongside the image one`() {
        val axml = shellManifest()
        assumeTrue("shell template not built - run ':app:syncShellTemplateApk' first", axml != null)

        val filters = shellActivityFilters(rebuild(axml!!, listOf("image/*", "text/plain")))
        val sendFilters = filters.filter { "android.intent.action.SEND" in it.actions() }

        assertThat(sendFilters).hasSize(1)
        assertThat(sendFilters.single().mimeTypes())
            .containsExactly("image/*", "text/plain")
    }

    @Test
    fun `an empty mime list leaves the manifest share free`() {
        val axml = shellManifest()
        assumeTrue("shell template not built - run ':app:syncShellTemplateApk' first", axml != null)

        val filters = shellActivityFilters(rebuild(axml!!, emptyList()))

        // Nothing share-related appears when the feature is off, which is the guarantee that a
        // build without share-receive carries the same manifest as one made before it existed.
        assertThat(filters.flatMap { it.actions() })
            .containsNoneOf("android.intent.action.SEND", "android.intent.action.SEND_MULTIPLE")
    }

    @Test
    fun `existing launcher filter survives the injection`() {
        val axml = shellManifest()
        assumeTrue("shell template not built - run ':app:syncShellTemplateApk' first", axml != null)

        val filters = shellActivityFilters(rebuild(axml!!, listOf("image/*")))
        val launcher = filters.filter { "android.intent.action.MAIN" in it.actions() }

        // The launcher filter must still be there; the injection only appends. (The exact
        // count is left alone here — `rewireLauncherToShellActivity` may also add one, and
        // that is not this feature's concern.)
        assertThat(launcher).isNotEmpty()
        assertThat(launcher.flatMap { it.categories() }).contains("android.intent.category.LAUNCHER")
    }
}
