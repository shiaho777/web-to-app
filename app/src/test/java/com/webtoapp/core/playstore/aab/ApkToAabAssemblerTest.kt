package com.webtoapp.core.playstore.aab

import com.android.aapt.Resources
import com.android.bundle.Config
import com.google.common.truth.Truth.assertThat
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.zip.ZipFile

class ApkToAabAssemblerTest {

    @get:Rule
    val temp = TemporaryFolder()

    @Test
    fun `assemble real shell-template into AAB`() {
        val template = File("src/main/assets/template/webview_shell.apk")
        assumeTrue(
            "shell template not built — run ':app:syncShellTemplateApk' first",
            template.exists()
        )

        val output = temp.newFile("out.aab")
        val stats = ApkToAabAssembler().assemble(template, output)

        assertThat(stats.outputBytes).isGreaterThan(1_000_000L)
        assertThat(stats.manifestConverted).isEqualTo(1)
        assertThat(stats.resourceXmlSkipped).isEqualTo(0)
        assertThat(stats.resourceXmlConverted).isGreaterThan(50)
        assertThat(stats.dexCount).isAtLeast(1)
        assertThat(stats.nativeLibCount).isAtLeast(0)
        if (stats.nativeLibCount > 0) {
            assertThat(stats.abis).isNotEmpty()
        } else {
            assertThat(stats.abis).isEmpty()
        }

        val entryNames = ZipFile(output).use { zip ->
            zip.entries().toList().map { it.name }.toSet()
        }
        assertThat(entryNames).contains("BundleConfig.pb")
        assertThat(entryNames).contains("base/manifest/AndroidManifest.xml")
        assertThat(entryNames).contains("base/resources.pb")
        if (stats.nativeLibCount > 0) {
            assertThat(entryNames).contains("base/native.pb")
        } else {
            assertThat(entryNames).doesNotContain("base/native.pb")
        }

        assertThat(entryNames).doesNotContain("AndroidManifest.xml")
        assertThat(entryNames).doesNotContain("resources.arsc")

        assertThat(entryNames.none { it.startsWith("META-INF/") }).isTrue()

        val convertedResXmlCount = entryNames.count { it.startsWith("base/res/") && it.endsWith(".xml") }
        assertThat(convertedResXmlCount).isAtLeast(50)

        val libCount = entryNames.count { it.startsWith("base/lib/") && it.endsWith(".so") }
        assertThat(libCount).isEqualTo(stats.nativeLibCount)

        val bundleConfigBytes = ZipFile(output).use { zip ->
            zip.getInputStream(zip.getEntry("BundleConfig.pb")).readBytes()
        }
        val bundleConfig = Config.BundleConfig.parseFrom(bundleConfigBytes)
        assertThat(bundleConfig.bundletool.version).isNotEmpty()
        assertThat(bundleConfig.type).isEqualTo(Config.BundleConfig.BundleType.REGULAR)
        assertThat(bundleConfig.optimizations.splitsConfig.splitDimensionCount).isAtLeast(3)

        val manifestBytes = ZipFile(output).use { zip ->
            zip.getInputStream(zip.getEntry("base/manifest/AndroidManifest.xml")).readBytes()
        }
        val manifestNode = Resources.XmlNode.parseFrom(manifestBytes)
        assertThat(manifestNode.hasElement()).isTrue()
        assertThat(manifestNode.element.name).isEqualTo("manifest")

        val applicationChild = manifestNode.element.childList
            .firstOrNull { it.hasElement() && it.element.name == "application" }
        assertThat(applicationChild).isNotNull()

        val resourcesBytes = ZipFile(output).use { zip ->
            zip.getInputStream(zip.getEntry("base/resources.pb")).readBytes()
        }
        val resourceTable = Resources.ResourceTable.parseFrom(resourcesBytes)
        assertThat(resourceTable.packageCount).isEqualTo(1)
        assertThat(resourceTable.getPackage(0).packageId.id).isEqualTo(0x7f)

        assertThat(resourceTable.getPackage(0).typeCount).isAtLeast(5)
    }

    @Test
    fun `assemble with targetSdkOverride rewrites manifest targetSdkVersion`() {
        val template = File("src/main/assets/template/webview_shell.apk")
        assumeTrue(
            "shell template not built — run ':app:syncShellTemplateApk' first",
            template.exists()
        )

        val originalAab = temp.newFile("original.aab")
        ApkToAabAssembler().assemble(template, originalAab, targetSdkOverride = null)
        val originalManifest = ZipFile(originalAab).use { zip ->
            Resources.XmlNode.parseFrom(
                zip.getInputStream(zip.getEntry("base/manifest/AndroidManifest.xml"))
            )
        }
        assertThat(
            com.webtoapp.core.playstore.aab.axml.ProtoManifestRewriter
                .extractTargetSdkVersion(originalManifest)
        ).isEqualTo(28)

        val rewrittenAab = temp.newFile("rewritten.aab")
        ApkToAabAssembler().assemble(template, rewrittenAab, targetSdkOverride = 35)
        val rewrittenManifest = ZipFile(rewrittenAab).use { zip ->
            Resources.XmlNode.parseFrom(
                zip.getInputStream(zip.getEntry("base/manifest/AndroidManifest.xml"))
            )
        }
        assertThat(
            com.webtoapp.core.playstore.aab.axml.ProtoManifestRewriter
                .extractTargetSdkVersion(rewrittenManifest)
        ).isEqualTo(35)
    }
}
