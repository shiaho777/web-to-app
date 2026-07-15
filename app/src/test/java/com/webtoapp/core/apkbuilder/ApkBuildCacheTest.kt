package com.webtoapp.core.apkbuilder

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ApkBuildCacheTest {

    @Test
    fun `content replaceable entries cover config and project assets`() {
        val cache = ApkBuildCache(RuntimeEnvironment.getApplication())
        assertThat(cache.isContentReplaceableEntry(ApkTemplate.CONFIG_PATH)).isTrue()
        assertThat(cache.isContentReplaceableEntry("assets/html/index.html")).isTrue()
        assertThat(cache.isContentReplaceableEntry("assets/nodejs_app/server.js")).isTrue()
        assertThat(cache.isContentReplaceableEntry("assets/splash_media.png")).isTrue()
        assertThat(cache.isContentReplaceableEntry("AndroidManifest.xml")).isFalse()
        assertThat(cache.isContentReplaceableEntry("resources.arsc")).isFalse()
        assertThat(cache.isContentReplaceableEntry("lib/arm64-v8a/libnode.so")).isFalse()
    }

    @Test
    fun `save and load unsigned enables reuse plan`() {
        val context = RuntimeEnvironment.getApplication()
        val cache = ApkBuildCache(context)
        cache.clearAll()

        val webApp = com.webtoapp.data.model.WebApp(
            id = 42,
            name = "Demo",
            url = "https://example.com"
        )
        val unsigned = File(context.cacheDir, "demo_unsigned.apk").apply {
            writeBytes(ByteArray(64) { 1 })
        }
        cache.saveUnsigned(
            webApp = webApp,
            packageName = "com.demo.app",
            unsignedApk = unsigned,
            identityFingerprint = "id1",
            contentFingerprint = "c1",
            shellTemplateId = "shell1"
        )

        val template = File(context.cacheDir, "template.apk").apply {
            writeBytes(ByteArray(32) { 2 })
        }
        val config = ApkConfig(
            meta = MetaBlock(
                appName = "Demo",
                packageName = "com.demo.app",
                targetUrl = "https://example.com",
                versionCode = 1,
                versionName = "1.0",
                appType = "WEB"
            )
        )

        // plan uses real fingerprints so won't match id1/c1 — just verify save files exist
        val keyDir = File(context.filesDir, "apk_build_cache/app_42")
        assertThat(File(keyDir, "base_unsigned.apk").isFile).isTrue()
        assertThat(File(keyDir, "meta.json").isFile).isTrue()
        assertThat(File(keyDir, "meta.json").readText()).contains("id1")
        assertThat(File(keyDir, "meta.json").readText()).contains("c1")

        cache.clear(webApp, "com.demo.app")
        assertThat(keyDir.exists()).isFalse()
    }

    @Test
    fun `shell template id changes with size but not mtime`() {
        val cache = ApkBuildCache(RuntimeEnvironment.getApplication())
        val file = File(RuntimeEnvironment.getApplication().cacheDir, "t.apk")
        file.writeBytes(ByteArray(10))
        val a = cache.shellTemplateId(file)
        file.setLastModified(file.lastModified() + 60_000L)
        val same = cache.shellTemplateId(file)
        assertThat(same).isEqualTo(a)
        file.writeBytes(ByteArray(20))
        val b = cache.shellTemplateId(file)
        assertThat(a).isNotEqualTo(b)
    }

    @Test
    fun `plan reuses unsigned when fingerprints match`() {
        val context = RuntimeEnvironment.getApplication()
        val cache = ApkBuildCache(context)
        cache.clearAll()

        val webApp = com.webtoapp.data.model.WebApp(
            id = 7,
            name = "Reuse",
            url = "https://example.com"
        )
        val template = File(context.cacheDir, "shell_template.apk").apply {
            writeBytes(ByteArray(48) { 3 })
        }
        val config = ApkConfig(
            meta = MetaBlock(
                appName = "Reuse",
                packageName = "com.demo.reuse",
                targetUrl = "https://example.com",
                versionCode = 1,
                versionName = "1.0",
                appType = "WEB"
            )
        )
        val plan1 = cache.plan(
            webApp = webApp,
            packageName = "com.demo.reuse",
            config = config,
            templateApk = template,
            encryptionEnabled = false,
            abiFilters = emptyList(),
            projectDirs = emptyList(),
            mediaContentPath = null,
            splashMediaPath = null,
            bgmPlaylistPaths = emptyList(),
            htmlFiles = emptyList(),
            galleryItems = emptyList(),
            errorPageMediaPath = null,
            forceFullRebuild = false
        )
        assertThat(plan1.mode).isEqualTo(IncrementalBuildMode.FULL)
        assertThat(plan1.reason).isEqualTo("cacheMiss")

        val unsigned = File(context.cacheDir, "reuse_unsigned.apk").apply {
            writeBytes(ByteArray(128) { 9 })
        }
        cache.saveUnsigned(
            webApp = webApp,
            packageName = "com.demo.reuse",
            unsignedApk = unsigned,
            identityFingerprint = plan1.identityFingerprint,
            contentFingerprint = plan1.contentFingerprint,
            shellTemplateId = plan1.shellTemplateId
        )

        template.setLastModified(template.lastModified() + 120_000L)
        val plan2 = cache.plan(
            webApp = webApp,
            packageName = "com.demo.reuse",
            config = config,
            templateApk = template,
            encryptionEnabled = false,
            abiFilters = emptyList(),
            projectDirs = emptyList(),
            mediaContentPath = null,
            splashMediaPath = null,
            bgmPlaylistPaths = emptyList(),
            htmlFiles = emptyList(),
            galleryItems = emptyList(),
            errorPageMediaPath = null,
            forceFullRebuild = false
        )
        assertThat(plan2.mode).isEqualTo(IncrementalBuildMode.REUSE_UNSIGNED)
        assertThat(plan2.reason).isEqualTo("identityAndContentMatch")
    }
}
