package com.webtoapp.core.apkbuilder

import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.webtoapp.data.model.ApkExportConfig
import com.webtoapp.data.model.FeatureStackConfig
import com.webtoapp.data.model.WebApp
import java.io.File
import java.util.zip.ZipFile
import kotlinx.coroutines.runBlocking
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.robolectric.annotation.RealObject
import org.robolectric.shadows.ShadowApplicationPackageManager
import org.robolectric.util.reflector.Direct
import org.robolectric.util.reflector.ForType
import org.robolectric.util.reflector.Reflector.reflector

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
@Config(sdk = [34], shadows = [DemoApkExportTest.ArchivePackageManager::class])
class DemoApkExportTest {

    // Robolectric's package-name shortcut returns host Resources for the shell
    // archive (both are com.webtoapp). Use Android's real archive-path handling
    // for external APKs, retaining the usual shadow for installed applications.
    @Implements(className = "android.app.ApplicationPackageManager")
    class ArchivePackageManager : ShadowApplicationPackageManager() {
        @RealObject lateinit var realPackageManager: android.content.pm.PackageManager

        @Implementation
        override fun getResourcesForApplication(app: android.content.pm.ApplicationInfo): android.content.res.Resources {
            val host = ApplicationProvider.getApplicationContext<android.content.Context>()
            val source = app.sourceDir
            if (source != null && source != host.applicationInfo.sourceDir && File(source).isFile) {
                return reflector(DirectPackageManager::class.java, realPackageManager)
                    .getResourcesForApplication(app)
            }
            return super.getResourcesForApplication(app)
        }
    }

    @ForType(className = "android.app.ApplicationPackageManager")
    interface DirectPackageManager {
        @Direct
        fun getResourcesForApplication(app: android.content.pm.ApplicationInfo): android.content.res.Resources
    }

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

        // Default-on: every feature-stack dex ships in the generated APK.
        ZipFile(apk).use { zip ->
            assertThat(zip.getEntry("assets/feature_stacks/google_signin.dex")).isNotNull()
            assertThat(zip.getEntry("assets/feature_stacks/fcm.dex")).isNotNull()
            assertThat(zip.getEntry("assets/feature_stacks/cronet.dex")).isNotNull()
        }

        assertThat(destApk.exists()).isTrue()
    }

    @Test
    @Suppress("DEPRECATION")
    fun `SAEP export resolves real plaintext raw resource including encrypted builds`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        assumeTrue("shell template missing - run ':app:syncShellTemplateApk' first", templateAssetPresent(context))
        for ((enabled, encrypted) in listOf(false to false, true to false, true to true)) {
            val packageName = "org.example.saepexport.e${enabled}.c${encrypted}"
            val app = WebApp(
                // Distinct archive paths prevent Android's ApkAssets path cache
                // from returning the previous case after its APK is overwritten.
                name = "SAEP export test $enabled $encrypted", url = "https://example.com",
                apkExportConfig = ApkExportConfig(
                    customPackageName = packageName, saepEnabled = enabled,
                    encryptionConfig = com.webtoapp.data.model.ApkEncryptionConfig(
                        enabled = encrypted, keyMode = "EMBEDDED"
                    )
                )
            )
            val result = ApkBuilder(context).buildApk(app, forceFullRebuild = true) { _, _ -> }
            assertThat(result).isInstanceOf(BuildResult.Success::class.java)
            val apk = (result as BuildResult.Success).apkFile
            val pm = context.packageManager
            val info = checkNotNull(pm.getPackageArchiveInfo(apk.absolutePath,
                android.content.pm.PackageManager.GET_META_DATA or android.content.pm.PackageManager.GET_ACTIVITIES))
            assertThat(info.packageName).isEqualTo(packageName)
            val appInfo = android.content.pm.ApplicationInfo(checkNotNull(info.applicationInfo)).apply {
                sourceDir = apk.absolutePath
                publicSourceDir = apk.absolutePath
            }
            assertThat(appInfo.metaData?.containsKey(SaepPolicy.TEMPLATE_METADATA) == true).isFalse()
            val id = appInfo.metaData?.getInt(SaepPolicy.POLICY_METADATA, 0) ?: 0
            if (!enabled) {
                assertThat(id).isEqualTo(0)
            } else {
                assertThat(id).isNotEqualTo(0)
                val resources = pm.getResourcesForApplication(appInfo)
                assertThat(resources.getResourceTypeName(id)).isEqualTo("raw")
                val bytes = resources.openRawResource(id).use { it.readBytes() }
                assertThat(bytes.size).isAtMost(SaepPolicy.MAX_BYTES)
                val policy = com.google.gson.JsonParser.parseString(bytes.toString(Charsets.UTF_8)).asJsonObject
                assertThat(policy.get("package").asString).isEqualTo(info.packageName)
                val activities = policy.getAsJsonObject("scope").getAsJsonObject("activities").keySet()
                assertThat(info.activities.orEmpty().map { it.name }).containsAtLeastElementsIn(activities)
                assertThat(activities).containsExactly(SaepPolicy.SHELL_ACTIVITY)
                val outDir = File(System.getProperty("wta.demoOutDir") ?: "build/outputs/demo").apply { mkdirs() }
                apk.copyTo(File(outDir, "WebToApp-SAEP-encrypted-$encrypted.apk"), overwrite = true)
            }
        }
    }

    @Test
    fun `disabled feature stacks drop their dex assets`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        assumeTrue(
            "shell template asset missing - run ':app:syncShellTemplateApk' first",
            templateAssetPresent(context)
        )

        val app = WebApp(
            name = "CI Demo Slim",
            url = "https://example.com",
            apkExportConfig = ApkExportConfig(
                featureStack = FeatureStackConfig(
                    googleSignIn = false,
                    fcm = false,
                    http3Engine = false
                )
            )
        )

        val result = ApkBuilder(context).buildApk(app) { _, _ -> }

        assertThat(result).isInstanceOf(BuildResult.Success::class.java)
        val apk = (result as BuildResult.Success).apkFile
        ZipFile(apk).use { zip ->
            assertThat(zip.getEntry("assets/feature_stacks/google_signin.dex")).isNull()
            assertThat(zip.getEntry("assets/feature_stacks/fcm.dex")).isNull()
            assertThat(zip.getEntry("assets/feature_stacks/cronet.dex")).isNull()
            // The APK is still structurally valid.
            assertThat(zip.getEntry("AndroidManifest.xml")).isNotNull()
            assertThat(zip.getEntry("assets/app_config.json")).isNotNull()
        }
    }
}
