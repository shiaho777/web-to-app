package com.webtoapp.core.playstore.aab.axml

import com.google.common.truth.Truth.assertThat
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File
import java.util.zip.ZipFile

class AxmlRealManifestTest {

    @Test
    fun `convert real shell-template AndroidManifest without crashing`() {
        val template = File("src/main/assets/template/webview_shell.apk")
        assumeTrue(
            "shell template not built — run ':app:syncShellTemplateApk' first",
            template.exists()
        )

        val axmlBytes = ZipFile(template).use { zip ->
            val entry = zip.getEntry("AndroidManifest.xml")
                ?: error("AndroidManifest.xml missing from shell template")
            zip.getInputStream(entry).readBytes()
        }

        val node = AxmlToProtoXml.convert(axmlBytes)

        assertThat(node.hasElement()).isTrue()
        val manifest = node.element
        assertThat(manifest.name).isEqualTo("manifest")

        val androidNs = manifest.namespaceDeclarationList.firstOrNull {
            it.uri == "http://schemas.android.com/apk/res/android"
        }
        assertThat(androidNs).isNotNull()
        assertThat(androidNs!!.prefix).isEqualTo("android")

        val packageAttr = manifest.attributeList.firstOrNull { it.name == "package" }
        if (packageAttr != null) {
            assertThat(packageAttr.value).isNotEmpty()

            assertThat(packageAttr.value).contains("com.webtoapp")
        }

        val usesSdk = manifest.childList
            .filter { it.hasElement() }
            .map { it.element }
            .firstOrNull { it.name == "uses-sdk" }
        assertThat(usesSdk).isNotNull()
        val minSdkAttr = usesSdk!!.attributeList.firstOrNull { it.name == "minSdkVersion" }
        assertThat(minSdkAttr).isNotNull()

        assertThat(minSdkAttr!!.compiledItem.hasPrim()).isTrue()
        assertThat(minSdkAttr.compiledItem.prim.intDecimalValue).isAtLeast(1)

        val application = manifest.childList
            .filter { it.hasElement() }
            .map { it.element }
            .firstOrNull { it.name == "application" }
        assertThat(application).isNotNull()
        val activities = application!!.childList
            .filter { it.hasElement() }
            .map { it.element }
            .filter { it.name == "activity" }
        assertThat(activities).isNotEmpty()
    }
}
