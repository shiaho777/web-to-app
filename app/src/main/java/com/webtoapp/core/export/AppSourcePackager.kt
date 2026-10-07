package com.webtoapp.core.export

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.webtoapp.core.apkbuilder.ApkBuilder
import com.webtoapp.core.apkbuilder.ApkConfigJsonFactory
import com.webtoapp.core.apkbuilder.NetworkSecurityConfigBuilder
import com.webtoapp.core.apkbuilder.toApkConfig
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.data.model.AppType
import com.webtoapp.data.model.NetworkTrustConfig
import com.webtoapp.data.model.WebApp
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Compilable Android project for one app.
 *
 * The zip is an Android Studio project: Gradle build files, a WebView
 * activity, the manifest, and the app's own files. It is not the WebToApp
 * shell runtime, and it does not include the signing keystore.
 */
class AppSourcePackager(private val context: Context) {

    fun pack(webApp: WebApp): File {
        val outDir = File(context.cacheDir, "source_export").apply { mkdirs() }
        val baseName = sanitize(webApp.name).ifBlank { "app" }
        val out = File(outDir, "$baseName-source.zip")
        val tmp = File(outDir, "$baseName-source-${System.nanoTime()}.zip.tmp")
        val writer = BundleWriter()
        try {
            ZipOutputStream(BufferedOutputStream(FileOutputStream(tmp))).use { zip ->
                writer.attach(zip)
                val packageName = ApkBuilder.resolvePackageName(webApp)
                val config = webApp.toApkConfig(packageName, context)
                writer.text("README.md", Strings.sourceBundleReadme(webApp.name))
                writer.androidProject(webApp, config)
                writer.config(config)
                writer.networkTrust(webApp)
                writer.content(webApp)
                if (writer.truncated) {
                    writer.text(
                        "app/src/main/assets/TRUNCATED.txt",
                        "Further files were omitted because the archive reached its size limit."
                    )
                }
            }
            if (out.exists() && !out.delete()) {
                AppLogger.w(TAG, "Replacing existing source zip failed: ${out.absolutePath}")
            }
            if (!tmp.renameTo(out)) {
                tmp.copyTo(out, overwrite = true)
                tmp.delete()
            }
            return out
        } catch (e: Exception) {
            tmp.delete()
            throw e
        }
    }

    /** Opens the system share sheet for a zip produced by [pack]. */
    fun share(file: File): Boolean {
        return try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "application/zip"
                putExtra(Intent.EXTRA_STREAM, uri)
                clipData = ClipData.newRawUri(file.name, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(send, Strings.exportAppSource)
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            true
        } catch (e: Exception) {
            AppLogger.e(TAG, "Share source zip failed", e)
            false
        }
    }

    private fun launchUrl(webApp: WebApp): String {
        if (webApp.appType == AppType.HTML || webApp.appType == AppType.FRONTEND) {
            val entry = webApp.htmlConfig?.getValidEntryFile() ?: "index.html"
            if (projectDir(webApp.htmlConfig?.projectId) != null) {
                return "file:///android_asset/www/$entry"
            }
        }
        val url = webApp.url.trim()
        if (url.startsWith("http://") || url.startsWith("https://")) return url
        return "about:blank"
    }

    private fun projectDir(projectId: String?): File? {
        val id = projectId?.takeIf { it.isNotBlank() } ?: return null
        val dir = File(context.filesDir, "html_projects/$id")
        return dir.takeIf { it.isDirectory }
    }

    private fun appBuildGradle(config: com.webtoapp.core.apkbuilder.ApkConfig): String {
        val targetSdk = (config.targetSdkOverride ?: 35).coerceIn(23, 36)
        return """
            plugins {
                id("com.android.application")
                id("org.jetbrains.kotlin.android")
            }

            android {
                namespace = "${kotlinString(config.packageName)}"
                compileSdk = $targetSdk

                defaultConfig {
                    applicationId = "${kotlinString(config.packageName)}"
                    minSdk = 23
                    targetSdk = $targetSdk
                    versionCode = ${config.versionCode}
                    versionName = "${kotlinString(config.versionName)}"
                }

                buildTypes {
                    release {
                        isMinifyEnabled = false
                    }
                }

                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }

                kotlinOptions {
                    jvmTarget = "17"
                }
            }

            dependencies {
                implementation("androidx.core:core-ktx:1.15.0")
            }
        """.trimIndent()
    }

    private fun mainActivity(packageName: String, launchUrl: String, javaScriptEnabled: Boolean): String {
        return """
            package $packageName

            import android.app.Activity
            import android.os.Bundle
            import android.webkit.WebView
            import android.webkit.WebViewClient

            class MainActivity : Activity() {
                private lateinit var webView: WebView

                override fun onCreate(savedInstanceState: Bundle?) {
                    super.onCreate(savedInstanceState)
                    webView = WebView(this)
                    webView.settings.javaScriptEnabled = $javaScriptEnabled
                    webView.settings.domStorageEnabled = true
                    webView.settings.allowFileAccess = true
                    webView.webViewClient = WebViewClient()
                    setContentView(webView)
                    webView.loadUrl("${kotlinString(launchUrl)}")
                }

                @Deprecated("Kept so the exported project builds without extra AndroidX callbacks.")
                override fun onBackPressed() {
                    if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
                }
            }
        """.trimIndent()
    }

    private inner class BundleWriter {
        private lateinit var zip: ZipOutputStream
        private val usedEntries = HashSet<String>()
        private val copiedFiles = HashSet<String>()
        private var bytes = 0L
        var truncated = false
            private set
        private var looseIndex = 0

        fun attach(zip: ZipOutputStream) {
            this.zip = zip
        }

        fun text(entryName: String, body: String) {
            val name = safeEntry(entryName) ?: return
            if (!usedEntries.add(name)) return
            val data = body.toByteArray(Charsets.UTF_8)
            zip.putNextEntry(ZipEntry(name))
            zip.write(data)
            zip.closeEntry()
        }

        fun androidProject(webApp: WebApp, config: com.webtoapp.core.apkbuilder.ApkConfig) {
            val packageName = config.packageName
            val packagePath = packageName.replace('.', '/')
            val launchUrl = launchUrl(webApp)
            val permissions = ApkBuilder(context).buildRequiredPermissions(config)
            val permissionXml = permissions.joinToString("\n") { name ->
                """    <uses-permission android:name="${xml(name)}" />"""
            }
            val iconName = copyIcon(webApp)
            val iconAttr = if (iconName == null) "" else """
        android:icon="@drawable/$iconName"
        android:roundIcon="@drawable/$iconName""""
            text("settings.gradle.kts", SETTINGS_GRADLE)
            text("build.gradle.kts", ROOT_BUILD_GRADLE)
            text("gradle.properties", GRADLE_PROPERTIES)
            text(
                "gradle/wrapper/gradle-wrapper.properties",
                "distributionUrl=https\\://services.gradle.org/distributions/gradle-8.11.1-bin.zip\n"
            )
            text("app/build.gradle.kts", appBuildGradle(config))
            text(
                "app/src/main/AndroidManifest.xml",
                """
                <?xml version="1.0" encoding="utf-8"?>
                <manifest xmlns:android="http://schemas.android.com/apk/res/android">
                $permissionXml
                    <application
                        android:label="@string/app_name"
                        android:networkSecurityConfig="@xml/network_security_config"
                        android:theme="@android:style/Theme.DeviceDefault.NoActionBar"$iconAttr>
                        <activity
                            android:name=".MainActivity"
                            android:exported="true">
                            <intent-filter>
                                <action android:name="android.intent.action.MAIN" />
                                <category android:name="android.intent.category.LAUNCHER" />
                            </intent-filter>
                        </activity>
                    </application>
                </manifest>
                """.trimIndent()
            )
            text(
                "app/src/main/res/values/strings.xml",
                """
                <?xml version="1.0" encoding="utf-8"?>
                <resources>
                    <string name="app_name">${xml(config.appName)}</string>
                </resources>
                """.trimIndent()
            )
            text("app/src/main/java/$packagePath/MainActivity.kt", mainActivity(packageName, launchUrl, config.javaScriptEnabled))
        }

        private fun copyIcon(webApp: WebApp): String? {
            val path = webApp.iconPath?.trim().orEmpty()
            if (path.isEmpty()) return null
            val source = localFile(path) ?: return null
            val extension = source.extension.lowercase().takeIf { it in setOf("png", "webp", "jpg", "jpeg") } ?: "png"
            file("app/src/main/res/drawable/ic_launcher.$extension", source)
            return "ic_launcher"
        }

        fun config(config: com.webtoapp.core.apkbuilder.ApkConfig) {
            try {
                text(
                    "app/src/main/assets/app_config.json",
                    ApkConfigJsonFactory.create(config)
                )
            } catch (e: Exception) {
                AppLogger.e(TAG, "app_config.json was not written", e)
                text(
                    "app/src/main/assets/app_config_error.txt",
                    e.message ?: "app_config.json could not be written"
                )
            }
        }

        fun networkTrust(webApp: WebApp) {
            val raw = webApp.apkExportConfig?.networkTrustConfig ?: NetworkTrustConfig()
            val effective = if (webApp.webViewConfig.antiCapture) {
                raw.copy(trustUserCa = false)
            } else {
                raw
            }
            text(
                "app/src/main/res/xml/network_security_config.xml",
                NetworkSecurityConfigBuilder.build(effective)
            )
            NetworkSecurityConfigBuilder.customRawEntries(effective).forEach { entry ->
                file("app/src/main/res/raw/${entry.resourceName}.cer", entry.sourceFile)
            }
        }

        fun content(webApp: WebApp) {
            when (webApp.appType) {
                AppType.HTML, AppType.FRONTEND -> {
                    projectDir(webApp.htmlConfig?.projectId)?.let { tree("app/src/main/assets/www", it) }
                }
                AppType.MULTI_WEB -> {
                    projectDir(webApp.multiWebConfig?.projectId)?.let { tree("app/src/main/assets/www", it) }
                    webApp.multiWebConfig?.sites.orEmpty().forEachIndexed { index, site ->
                        val segment = sanitize(site.id).ifBlank { "site-$index" }
                        projectDir(site.sourceProjectId)?.let { tree("app/src/main/assets/www/sites/$segment", it) }
                        localFile(site.localFilePath)?.let { file("app/src/main/assets/www/sites/$segment/local", it) }
                        if (site.inlineHtml.isNotBlank()) {
                            text("app/src/main/assets/www/sites/$segment/inline.html", site.inlineHtml)
                        }
                    }
                }
                AppType.GALLERY -> {
                    webApp.galleryConfig?.items.orEmpty().forEach { item ->
                        loose(item.path)
                        item.thumbnailPath?.let { loose(it) }
                    }
                }
                else -> Unit
            }
            webApp.htmlConfig?.files.orEmpty().forEach { loose(it.path) }
            if (webApp.splashEnabled) {
                webApp.splashConfig?.mediaPath?.let { loose(it) }
            }
            if (webApp.bgmEnabled) {
                webApp.bgmConfig?.playlist.orEmpty().forEach { item ->
                    loose(item.path)
                    item.coverPath?.let { loose(it) }
                    item.lrcPath?.let { loose(it) }
                }
            }
            webApp.iconPath?.let { loose(it) }
            webApp.announcement?.customIconPath?.let { loose(it) }
            loose(webApp.webViewConfig.errorPageConfig.customMediaPath)
        }

        private fun projectDir(projectId: String?): File? {
            val id = projectId?.takeIf { it.isNotBlank() } ?: return null
            val dir = File(context.filesDir, "html_projects/$id")
            return dir.takeIf { it.isDirectory }
        }

        private fun tree(entryRoot: String, dir: File) {
            val base = dir.canonicalFile
            dir.walkTopDown()
                .onEnter { child -> child.name != "node_modules" && child.name != ".git" }
                .filter { it.isFile }
                .forEach { file ->
                    val canonical = file.canonicalFile
                    val basePath = base.path + File.separator
                    if (canonical != base && !canonical.path.startsWith(basePath)) return@forEach
                    val relative = file.relativeTo(dir).invariantSeparatorsPath
                    file("$entryRoot/$relative", file)
                }
        }

        private fun loose(path: String?) {
            if (path.isNullOrBlank()) return
            val trimmed = path.trim()
            when {
                trimmed.startsWith("content://") -> contentUri(trimmed)
                else -> localFile(trimmed)?.let { file("app/src/main/assets/files/${looseIndex++}-${it.name}", it) }
            }
        }

        private fun localFile(path: String?): File? {
            val trimmed = path?.trim().orEmpty()
            if (trimmed.isEmpty()) return null
            if (trimmed.startsWith("http://") || trimmed.startsWith("https://") ||
                trimmed.startsWith("asset://") || trimmed.startsWith("data:") ||
                trimmed.startsWith("content://")
            ) {
                return null
            }
            val file = if (trimmed.startsWith("file://")) {
                File(Uri.parse(trimmed).path ?: return null)
            } else {
                File(trimmed)
            }
            return file.takeIf { it.isFile && it.canRead() }
        }

        private fun contentUri(uri: String) {
            if (truncated) return
            val name = Uri.parse(uri).lastPathSegment?.substringAfterLast('/') ?: "file"
            val entry = safeEntry("app/src/main/assets/files/${looseIndex++}-${sanitize(name)}") ?: return
            if (!usedEntries.add(entry)) return
            try {
                context.contentResolver.openInputStream(Uri.parse(uri))?.use { input ->
                    writeStream(entry, input, knownLength = -1)
                }
            } catch (e: Exception) {
                AppLogger.w(TAG, "Skipped unreadable content URI", e)
                usedEntries.remove(entry)
            }
        }

        private fun file(entryName: String, source: File) {
            if (truncated || !source.isFile || !source.canRead()) return
            val canonical = try {
                source.canonicalPath
            } catch (_: Exception) {
                return
            }
            if (!copiedFiles.add(canonical)) return
            val entry = uniqueEntry(entryName) ?: return
            if (source.length() > MAX_BYTES - bytes) {
                truncated = true
                copiedFiles.remove(canonical)
                return
            }
            try {
                source.inputStream().use { writeStream(entry, it, source.length()) }
            } catch (e: Exception) {
                AppLogger.w(TAG, "Skipped unreadable file $canonical", e)
                usedEntries.remove(entry)
                copiedFiles.remove(canonical)
            }
        }

        private fun writeStream(entry: String, input: InputStream, knownLength: Long) {
            if (knownLength >= 0 && knownLength > MAX_BYTES - bytes) {
                truncated = true
                usedEntries.remove(entry)
                return
            }
            zip.putNextEntry(ZipEntry(entry))
            val buffer = ByteArray(64 * 1024)
            var written = 0L
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                if (bytes + read > MAX_BYTES) {
                    truncated = true
                    break
                }
                zip.write(buffer, 0, read)
                bytes += read
                written += read
            }
            zip.closeEntry()
            if (written == 0L && truncated) {
                usedEntries.remove(entry)
            }
        }

        private fun uniqueEntry(raw: String): String? {
            val safe = safeEntry(raw) ?: return null
            if (usedEntries.add(safe)) return safe
            val dot = safe.lastIndexOf('.')
            val stem = if (dot > safe.lastIndexOf('/')) safe.substring(0, dot) else safe
            val ext = if (dot > safe.lastIndexOf('/')) safe.substring(dot) else ""
            var n = 2
            while (n < 10_000) {
                val candidate = "$stem-$n$ext"
                if (usedEntries.add(candidate)) return candidate
                n++
            }
            return null
        }
    }

    companion object {
        private const val TAG = "AppSourcePackager"
        private const val MAX_BYTES = 1536L * 1024 * 1024

        private fun sanitize(name: String): String =
            name.replace(Regex("[^a-zA-Z0-9._\\-\\u4e00-\\u9fa5]"), "_").take(60)

        private fun xml(text: String): String = text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")

        private fun kotlinString(text: String): String = text
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("$", "\${'$'}")
            .replace("\n", "\\n")

        private val SETTINGS_GRADLE = """
            pluginManagement {
                repositories {
                    google()
                    mavenCentral()
                    gradlePluginPortal()
                }
            }

            dependencyResolutionManagement {
                repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
                repositories {
                    google()
                    mavenCentral()
                }
            }

            rootProject.name = "app"
            include(":app")
        """.trimIndent()

        private val ROOT_BUILD_GRADLE = """
            plugins {
                id("com.android.application") version "8.7.3" apply false
                id("org.jetbrains.kotlin.android") version "2.0.21" apply false
            }
        """.trimIndent()

        private val GRADLE_PROPERTIES = """
            android.useAndroidX=true
            org.gradle.jvmargs=-Xmx2048m
            kotlin.code.style=official
        """.trimIndent()

        private fun safeEntry(raw: String): String? {
            val normalized = raw.replace('\\', '/').trim('/')
            if (normalized.isEmpty()) return null
            val parts = normalized.split('/')
            if (parts.any { it.isEmpty() || it == "." || it == ".." }) return null
            return parts.joinToString("/")
        }
    }
}
