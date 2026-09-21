package com.webtoapp.core.apkbuilder

import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.webtoapp.data.model.WebApp
import java.io.File
import java.util.zip.ZipFile
import kotlinx.coroutines.runBlocking
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * End-to-end smoke test for the whole export pipeline: template → AXML/ARSC patch →
 * config injection → zipalign → apksig sign → verify → metadata sidecar.
 *
 * Every other test in this package exercises one stage in isolation; this one proves they
 * compose into an installable APK, which is what CI's `package-apks` job publishes as the
 * "generated demo app" artifact. When the shell template asset is absent (plain
 * `testStandardDebugUnitTest` runs that pass `-PskipShellTemplateSync`), the test skips
 * itself instead of failing.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DemoApkExportTest {

    private fun templateAssetPresent(context: android.content.Context): Boolean = runCatching {
        context.assets.open("template/webview_shell.apk").use { it.read() >= 0 }
    }.getOrDefault(false)

    @Test
    fun `web app builds into a signed verifiable apk`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        assumeTrue(
            "shell template asset missing - run ':app:syncShellTemplateApk' first",
            templateAssetPresent(context)
        )

        val app = WebApp(
            name = "CI Demo",
            url = "https://example.com"
        )

        val result = ApkBuilder(context).buildApk(app) { _, _ -> }

        assertThat(result).isInstanceOf(BuildResult.Success::class.java)
        result as BuildResult.Success

        val apk = result.apkFile
        assertThat(apk.exists()).isTrue()
        assertThat(apk.length()).isGreaterThan(100_000L)

        // The output must be a structurally valid signed APK: manifest + config + at least
        // one DEX + a signature block. This is the same set of invariants the verifier
        // enforces, checked here against the real artifact.
        ZipFile(apk).use { zip ->
            assertThat(zip.getEntry("AndroidManifest.xml")).isNotNull()
            assertThat(zip.getEntry("assets/app_config.json")).isNotNull()
            assertThat(zip.entries().asSequence().any { it.name.endsWith(".dex") }).isTrue()
            assertThat(zip.entries().asSequence().any { it.name == "META-INF/CERT.RSA" }).isTrue()
        }

        // Publish the demo APK (plus its release-metadata sidecar) where CI's artifact
        // upload step expects it.
        val outDir = File(System.getProperty("wta.demoOutDir") ?: "build/outputs/demo")
        outDir.mkdirs()
        val destApk = File(outDir, "WebToApp-Demo.apk")
        apk.copyTo(destApk, overwrite = true)
        result.metadataPath?.let { File(it) }
            ?.takeIf { it.exists() }
            ?.copyTo(File(outDir, "WebToApp-Demo.build.json"), overwrite = true)

        assertThat(destApk.exists()).isTrue()
    }
}
