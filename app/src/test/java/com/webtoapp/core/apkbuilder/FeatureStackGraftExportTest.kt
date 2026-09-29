package com.webtoapp.core.apkbuilder

import androidx.test.core.app.ApplicationProvider
import com.android.aapt.Resources.XmlElement
import com.google.common.truth.Truth.assertThat
import com.webtoapp.core.playstore.aab.axml.AxmlToProtoXml
import com.webtoapp.data.model.AdConfig
import com.webtoapp.data.model.ApkExportConfig
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
 * End-to-end gate for the feature-stack injection pipeline (issue #1115):
 * a `WebApp` with ads enabled must produce an APK carrying the admob stack —
 * extra `classesN.dex`, namespaced `res/wtastack_admob/` files, a grafted
 * `resources.arsc` (second package at 0x80) and the fragment's manifest
 * components — while an ads-disabled build ships none of them.
 *
 * Both fixture assets are produced by build-time tasks; when either is absent
 * (e.g. `-PskipShellTemplateSync` / `-PskipStackBundlesSync` runs) the test
 * skips itself the same way [DemoApkExportTest] does.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FeatureStackGraftExportTest {

    private fun assetPresent(context: android.content.Context, path: String): Boolean =
        runCatching { context.assets.open(path).use { it.read() >= 0 } }.getOrDefault(false)

    private fun build(context: android.content.Context, packageName: String, ads: Boolean) =
        runBlocking {
            ApkBuilder(context).buildApk(
                WebApp(
                    name = "Stack graft $ads",
                    url = "https://example.com",
                    adsEnabled = ads,
                    adConfig = if (ads) AdConfig(
                        appId = "ca-app-pub-0000000000000000~0000000000",
                        testMode = true
                    ) else null,
                    apkExportConfig = ApkExportConfig(customPackageName = packageName)
                ),
                forceFullRebuild = true
            ) { _, _ -> }
        }

    private fun zipEntry(apk: File, name: String): ByteArray? =
        ZipFile(apk).use { zip ->
            zip.getEntry(name)?.let { zip.getInputStream(it).readBytes() }
        }

    private fun entryNames(apk: File): Set<String> =
        ZipFile(apk).use { zip -> zip.entries().asSequence().map { it.name }.toSet() }

    private fun templateEntries(context: android.content.Context): Set<String> =
        context.assets.open("template/webview_shell.apk").use { input ->
            val tmp = File(context.cacheDir, "stack-test-template.apk")
            tmp.outputStream().use { input.copyTo(it) }
            entryNames(tmp).also { tmp.delete() }
        }

    private fun manifestElements(apk: File): XmlElement =
        AxmlToProtoXml.convert(
            checkNotNull(zipEntry(apk, "AndroidManifest.xml"))
        ).element

    private fun elementsNamed(el: XmlElement, name: String): List<XmlElement> =
        el.childList.filter { it.hasElement() && it.element.name == name }
            .map { it.element }

    private fun XmlElement.attrValue(name: String): String? =
        attributeList.firstOrNull { it.name == name }?.value

    @Test
    fun `ads-enabled export grafts dex res arsc and manifest components`() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        assumeTrue("shell template missing", assetPresent(context, "template/webview_shell.apk"))
        assumeTrue("admob stack bundle missing", assetPresent(context, "stacks/admob.zip"))

        val packageName = "org.example.adson"
        val result = build(context, packageName, ads = true)
        assertThat(result).isInstanceOf(BuildResult.Success::class.java)
        val apk = (result as BuildResult.Success).apkFile
        val entries = entryNames(apk)

        // Stack DEX appended past the template's own dexes.
        val templateDexCount =
            templateEntries(context).count { it.matches(Regex("classes\\d*\\.dex")) }
        assertThat(entries.count { it.matches(Regex("classes\\d*\\.dex")) })
            .isGreaterThan(templateDexCount)
        // Namespaced resource files from the stack bundle.
        assertThat(entries.any { it.startsWith("res/wtastack_admob/") }).isTrue()

        // Grafted resource table now holds a second package at 0x80.
        val table = ArscPackageGrafter.ArscTable.parse(
            checkNotNull(zipEntry(apk, "resources.arsc"))
        )
        assertThat(table.packages.map { it.id }).contains(0x80)

        // Merged manifest: fragment components landed inside <application>,
        // ${applicationId} / ${admobAppId} vars substituted, theme refs typed.
        val manifest = manifestElements(apk)
        val app = elementsNamed(manifest, "application").single()

        val provider = elementsNamed(app, "provider")
            .single { it.attrValue("name") == "com.google.android.gms.ads.MobileAdsInitProvider" }
        assertThat(provider.attrValue("authorities"))
            .isEqualTo("$packageName.mobileadsinitprovider")

        assertThat(elementsNamed(app, "activity").map { it.attrValue("name") })
            .containsAtLeast(
                "com.google.android.gms.ads.AdActivity",
                "com.google.android.gms.ads.OutOfContextTestingActivity",
                "com.google.android.gms.ads.NotificationHandlerActivity"
            )
        assertThat(elementsNamed(app, "service").map { it.attrValue("name") })
            .contains("com.google.android.gms.ads.AdService")

        val adId = elementsNamed(app, "meta-data")
            .single { it.attrValue("name") == "com.google.android.gms.ads.APPLICATION_ID" }
        assertThat(adId.attrValue("value"))
            .isEqualTo("ca-app-pub-0000000000000000~0000000000")

        assertThat(
            elementsNamed(manifest, "uses-permission").map { it.attrValue("name") }
        ).containsAtLeast(
            "com.google.android.gms.permission.AD_ID",
            "android.permission.ACCESS_ADSERVICES_AD_ID"
        )
    }

    @Test
    fun `ads-disabled export carries no stack content`() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        assumeTrue("shell template missing", assetPresent(context, "template/webview_shell.apk"))

        val result = build(context, "org.example.adsoff", ads = false)
        assertThat(result).isInstanceOf(BuildResult.Success::class.java)
        val apk = (result as BuildResult.Success).apkFile
        val entries = entryNames(apk)

        val templateDexCount =
            templateEntries(context).count { it.matches(Regex("classes\\d*\\.dex")) }
        assertThat(entries.count { it.matches(Regex("classes\\d*\\.dex")) })
            .isEqualTo(templateDexCount)
        assertThat(entries.none { it.startsWith("res/wtastack_admob/") }).isTrue()

        val table = ArscPackageGrafter.ArscTable.parse(
            checkNotNull(zipEntry(apk, "resources.arsc"))
        )
        assertThat(table.packages.map { it.id }).doesNotContain(0x80)

        val manifest = manifestElements(apk)
        val app = elementsNamed(manifest, "application").single()
        assertThat(elementsNamed(app, "provider").map { it.attrValue("name") }
            .none { it == "com.google.android.gms.ads.MobileAdsInitProvider" }).isTrue()
        assertThat(
            elementsNamed(manifest, "uses-permission").map { it.attrValue("name") }
        ).doesNotContain("com.google.android.gms.permission.AD_ID")
    }
}
