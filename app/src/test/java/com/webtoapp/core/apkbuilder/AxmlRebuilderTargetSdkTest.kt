package com.webtoapp.core.apkbuilder

import com.google.common.truth.Truth.assertThat
import com.webtoapp.core.playstore.aab.axml.AxmlToProtoXml
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File
import java.util.zip.ZipFile

/**
 * Verifies [AxmlRebuilder.expandAndModifyFull] rewrites the `<uses-sdk
 * android:targetSdkVersion>` attribute on the generated APK's manifest. The shell template
 * ships targetSdk 28; WebView-only app types may raise it (issue #503) and the rewrite must
 * land in the binary AXML that gets packaged into the APK.
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

    private fun targetSdkOf(axml: ByteArray): Int? {
        val node = AxmlToProtoXml.convert(axml)
        assertThat(node.hasElement()).isTrue()
        val manifest = node.element
        val usesSdk = manifest.childList
            .filter { it.hasElement() }
            .map { it.element }
            .firstOrNull { it.name == "uses-sdk" }
            ?: return null
        val attr = usesSdk.attributeList.firstOrNull { it.name == "targetSdkVersion" }
            ?: return null
        assertThat(attr.compiledItem.hasPrim()).isTrue()
        return attr.compiledItem.prim.intDecimalValue
    }

    @Test
    fun `targetSdk rewrite raises uses-sdk value in binary AXML`() {
        val original = loadTemplateManifest()
        assumeTrue(
            "shell template not built — run :app:syncShellTemplateApk first",
            original != null
        )

        val before = targetSdkOf(original!!)
        assertThat(before).isEqualTo(28)

        val rewritten = rebuilder.expandAndModifyFull(
            original,
            originalPackage = "com.webtoapp",
            newPackage = "com.example.raised",
            versionCode = 1,
            versionName = "1.0.0",
            targetSdk = 35
        )

        val after = targetSdkOf(rewritten)
        assertThat(after).isEqualTo(35)
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

        assertThat(targetSdkOf(rewritten)).isEqualTo(28)
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
}
