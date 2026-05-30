package com.webtoapp.core.apkbuilder

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.webtoapp.data.model.AppType
import com.webtoapp.data.model.GalleryConfig
import com.webtoapp.data.model.GalleryItem
import com.webtoapp.data.model.GalleryItemType
import com.webtoapp.data.model.GoAppConfig
import com.webtoapp.data.model.HtmlConfig
import com.webtoapp.data.model.HtmlFile
import com.webtoapp.data.model.HtmlFileType
import com.webtoapp.data.model.MediaConfig
import com.webtoapp.data.model.MultiWebConfig
import com.webtoapp.data.model.MultiWebSite
import com.webtoapp.data.model.NodeJsConfig
import com.webtoapp.data.model.PhpAppConfig
import com.webtoapp.data.model.PythonAppConfig
import com.webtoapp.data.model.WebApp
import com.webtoapp.data.model.WordPressConfig
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class AppTypePreflightSmokeTest {

    @Rule @JvmField
    val koinRule = com.webtoapp.util.KoinCleanupRule()

    @get:Rule
    val temp = TemporaryFolder()

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @Test
    fun `every app type completes preflight without exceptions`() {
        val results = mutableMapOf<AppType, ApkExportPreflightReport>()
        for (type in AppType.values()) {
            val app = makeApp(type)
            val report = runCatching { ApkExportPreflight.check(context, app) }
            assertThat(report.isFailure).isFalse()
            results[type] = report.getOrThrow()
        }

        println("==== Preflight smoke matrix ====")
        results.forEach { (type, report) ->
            println(
                "  $type -> passed=${report.passed}, " +
                    "errors=${report.errors.size}, " +
                    "warnings=${report.warnings.size}"
            )
            report.errors.forEach { e -> println("    error[${e.key}]: ${e.message}") }
        }

        assertThat(results.keys).containsExactlyElementsIn(AppType.values().toList())
    }

    private fun makeApp(type: AppType): WebApp {
        val packageName = "com.example.${type.name.lowercase()}"
        return when (type) {
            AppType.WEB -> WebApp(name = "Web", url = "https://example.com", appType = type)

            AppType.IMAGE, AppType.VIDEO -> {
                val media = temp.newFile("${type.name.lowercase()}.bin").apply { writeBytes(ByteArray(64)) }
                WebApp(
                    name = type.name,
                    url = "",
                    appType = type,
                    mediaConfig = MediaConfig(mediaPath = media.absolutePath)
                )
            }

            AppType.HTML -> WebApp(
                name = "Html",
                url = "",
                appType = type,
                htmlConfig = HtmlConfig(entryFile = "index.html")
            )

            AppType.GALLERY -> {
                val img = temp.newFile("g.jpg").apply { writeBytes(ByteArray(64)) }
                WebApp(
                    name = "Gallery",
                    url = "",
                    appType = type,
                    galleryConfig = GalleryConfig(
                        items = listOf(
                            GalleryItem(
                                path = img.absolutePath,
                                type = GalleryItemType.IMAGE
                            )
                        )
                    )
                )
            }

            AppType.FRONTEND -> {
                val index = temp.newFile("front-${packageName}.html").apply { writeText("<html></html>") }
                WebApp(
                    name = "Frontend",
                    url = "",
                    appType = type,
                    htmlConfig = HtmlConfig(
                        entryFile = "index.html",
                        files = listOf(HtmlFile("index.html", index.absolutePath, HtmlFileType.HTML))
                    )
                )
            }

            AppType.WORDPRESS -> WebApp(
                name = "WordPress",
                url = "",
                appType = type,
                wordpressConfig = WordPressConfig(
                    projectId = "wp-smoke",
                    projectName = "WP",
                    siteTitle = "Site",
                    adminUser = "admin",
                    adminEmail = "admin@example.com",
                    adminPassword = "p4ssword!",
                    sourceType = "SAMPLE"
                )
            )

            AppType.NODEJS_APP -> WebApp(
                name = "Node",
                url = "",
                appType = type,
                nodejsConfig = NodeJsConfig(projectId = "node-smoke", projectName = "Node")
            )

            AppType.PHP_APP -> WebApp(
                name = "PHP",
                url = "",
                appType = type,
                phpAppConfig = PhpAppConfig(projectId = "php-smoke", projectName = "PHP")
            )

            AppType.PYTHON_APP -> WebApp(
                name = "Py",
                url = "",
                appType = type,
                pythonAppConfig = PythonAppConfig(projectId = "py-smoke", projectName = "Py")
            )

            AppType.GO_APP -> WebApp(
                name = "Go",
                url = "",
                appType = type,
                goAppConfig = GoAppConfig(projectId = "go-smoke", projectName = "Go")
            )

            AppType.MULTI_WEB -> WebApp(
                name = "MultiWeb",
                url = "",
                appType = type,
                multiWebConfig = MultiWebConfig(
                    sites = listOf(MultiWebSite(id = "s1", name = "Example"))
                )
            )
        }
    }
}
