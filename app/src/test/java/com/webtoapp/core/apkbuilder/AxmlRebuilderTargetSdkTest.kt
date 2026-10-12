package com.webtoapp.core.apkbuilder

import com.google.common.truth.Truth.assertThat
import com.webtoapp.core.playstore.aab.axml.AxmlToProtoXml
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File
import java.util.zip.ZipFile

/**
 * Verifies [AxmlRebuilder.expandAndModifyFull] rewrites the `<uses-sdk>`
 * attributes on the generated APK's manifest. The shell template ships
 * minSdk 23 / targetSdk 35; exports may pin other values (issues #503, #1295)
 * and the rewrite must land in the binary AXML that gets packaged into the APK.
 */
class AxmlRebuilderTargetSdkTest {

    private val rebuilder = AxmlRebuilder()

    private fun loadTemplateManifest(): ByteArray? {
        val template = File("src/main/assets/template/webview_shell.apk")
        if (!template.exists()) return null
        return ZipFile(template).use { zip ->
            val entry = zip.getEntry("AndroidManifest.xml") ?: return null
            zip.getInputStream(entry).readBytes()
        }
    }

    private fun usesSdkInt(axml: ByteArray, attrName: String): Int? {
        val node = AxmlToProtoXml.convert(axml)
        assertThat(node.hasElement()).isTrue()
        val manifest = node.element
        val usesSdk = manifest.childList
            .filter { it.hasElement() }
            .map { it.element }
            .firstOrNull { it.name == "uses-sdk" }
            ?: return null
        val attr = usesSdk.attributeList.firstOrNull { it.name == attrName }
            ?: return null
        assertThat(attr.compiledItem.hasPrim()).isTrue()
        return attr.compiledItem.prim.intDecimalValue
    }

    private fun targetSdkOf(axml: ByteArray): Int? = usesSdkInt(axml, "targetSdkVersion")

    private fun minSdkOf(axml: ByteArray): Int? = usesSdkInt(axml, "minSdkVersion")

    @Test
    fun `targetSdk rewrite raises uses-sdk value in binary AXML`() {
        val original = loadTemplateManifest()
        assumeTrue(
            "shell template not built — run :app:syncShellTemplateApk first",
            original != null
        )

        val before = targetSdkOf(original!!)
        assertThat(before).isEqualTo(35)

        val rewritten = rebuilder.expandAndModifyFull(
            original,
            originalPackage = "com.webtoapp",
            newPackage = "com.example.raised",
            versionCode = 1,
            versionName = "1.0.0",
            targetSdk = 36
        )

        val after = targetSdkOf(rewritten)
        assertThat(after).isEqualTo(36)
    }

    @Test
    fun `null targetSdk leaves uses-sdk untouched at template default`() {
        val original = loadTemplateManifest()
        assumeTrue(
            "shell template not built — run :app:syncShellTemplateApk first",
            original != null
        )

        val rewritten = rebuilder.expandAndModifyFull(
            original!!,
            originalPackage = "com.webtoapp",
            newPackage = "com.example.untouched",
            versionCode = 1,
            versionName = "1.0.0",
            targetSdk = null
        )

        assertThat(targetSdkOf(rewritten)).isEqualTo(35)
    }

    @Test
    fun `rewrite is idempotent across two passes`() {
        val original = loadTemplateManifest()
        assumeTrue(
            "shell template not built — run :app:syncShellTemplateApk first",
            original != null
        )

        val once = rebuilder.expandAndModifyFull(
            original!!,
            originalPackage = "com.webtoapp",
            newPackage = "com.example.once",
            versionCode = 1,
            versionName = "1.0.0",
            targetSdk = 34
        )
        // Re-run on already-patched bytes; result must remain stable and not corrupt the chunk.
        val twice = rebuilder.expandAndModifyFull(
            once,
            originalPackage = "com.webtoapp",
            newPackage = "com.example.twice",
            versionCode = 2,
            versionName = "2.0.0",
            targetSdk = 34
        )

        assertThat(targetSdkOf(twice)).isEqualTo(34)
    }

    @Test
    fun `minSdk rewrite raises uses-sdk value in binary AXML`() {
        val original = loadTemplateManifest()
        assumeTrue(
            "shell template not built — run :app:syncShellTemplateApk first",
            original != null
        )

        assertThat(minSdkOf(original!!)).isEqualTo(23)

        val rewritten = rebuilder.expandAndModifyFull(
            original,
            originalPackage = "com.webtoapp",
            newPackage = "com.example.minraised",
            versionCode = 1,
            versionName = "1.0.0",
            minSdk = 26
        )

        assertThat(minSdkOf(rewritten)).isEqualTo(26)
        assertThat(targetSdkOf(rewritten)).isEqualTo(35)
    }

    @Test
    fun `null minSdk leaves uses-sdk untouched at template default`() {
        val original = loadTemplateManifest()
        assumeTrue(
            "shell template not built — run :app:syncShellTemplateApk first",
            original != null
        )

        val rewritten = rebuilder.expandAndModifyFull(
            original!!,
            originalPackage = "com.webtoapp",
            newPackage = "com.example.minuntouched",
            versionCode = 1,
            versionName = "1.0.0",
            minSdk = null
        )

        assertThat(minSdkOf(rewritten)).isEqualTo(23)
    }

    @Test
    fun `minSdk and targetSdk rewrites land together`() {
        val original = loadTemplateManifest()
        assumeTrue(
            "shell template not built — run :app:syncShellTemplateApk first",
            original != null
        )

        val rewritten = rebuilder.expandAndModifyFull(
            original!!,
            originalPackage = "com.webtoapp",
            newPackage = "com.example.bothsdk",
            versionCode = 1,
            versionName = "1.0.0",
            targetSdk = 34,
            minSdk = 28
        )

        assertThat(minSdkOf(rewritten)).isEqualTo(28)
        assertThat(targetSdkOf(rewritten)).isEqualTo(34)
    }
}
