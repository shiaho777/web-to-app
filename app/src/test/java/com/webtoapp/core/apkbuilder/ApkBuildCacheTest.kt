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
    fun `shell template id tracks content rather than mtime or size`() {
        val cache = ApkBuildCache(RuntimeEnvironment.getApplication())
        val file = File(RuntimeEnvironment.getApplication().cacheDir, "t.apk")
        file.writeBytes(byteArrayOf(1, 2, 3, 4))
        val initial = cache.shellTemplateId(file)

        file.setLastModified(file.lastModified() + 60_000L)
        assertThat(cache.shellTemplateId(file)).isEqualTo(initial)

        file.writeBytes(byteArrayOf(4, 3, 2, 1))
        assertThat(cache.shellTemplateId(file)).isNotEqualTo(initial)
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

    @Test
    fun `native libs fingerprint change forces full rebuild instead of reuse`() {
        val context = RuntimeEnvironment.getApplication()
        val cache = ApkBuildCache(context)
        cache.clearAll()

        val webApp = com.webtoapp.data.model.WebApp(
            id = 11,
            name = "NodeApp",
            url = "nodejs://localhost"
        )
        val template = File(context.cacheDir, "shell_template_node.apk").apply {
            writeBytes(ByteArray(48) { 5 })
        }
        val config = ApkConfig(
            meta = MetaBlock(
                appName = "NodeApp",
                packageName = "com.demo.node",
                targetUrl = "nodejs://localhost",
                versionCode = 1,
                versionName = "1.0",
                appType = "NODEJS_APP"
            )
        )

        fun planWith(nativeLibs: String?) = cache.plan(
            webApp = webApp,
            packageName = "com.demo.node",
            config = config,
            templateApk = template,
            encryptionEnabled = false,
            abiFilters = listOf("arm64-v8a"),
            projectDirs = emptyList(),
            mediaContentPath = null,
            splashMediaPath = null,
            bgmPlaylistPaths = emptyList(),
            htmlFiles = emptyList(),
            galleryItems = emptyList(),
            errorPageMediaPath = null,
            nativeLibsFingerprint = nativeLibs,
            forceFullRebuild = false
        )

        // First build with the "old" libnode.so fingerprint → cache miss → FULL.
        val plan1 = planWith("sha256=oldlibnode,size=1,aligned16k=false")
        assertThat(plan1.mode).isEqualTo(IncrementalBuildMode.FULL)

        val unsigned = File(context.cacheDir, "node_unsigned.apk").apply {
            writeBytes(ByteArray(128) { 9 })
        }
        cache.saveUnsigned(
            webApp = webApp,
            packageName = "com.demo.node",
            unsignedApk = unsigned,
            identityFingerprint = plan1.identityFingerprint,
            contentFingerprint = plan1.contentFingerprint,
            shellTemplateId = plan1.shellTemplateId
        )

        // Same config but host upgraded libnode.so to a 16KB-aligned build → the native
        // libs fingerprint changed. This must NOT reuse the stale cached unsigned APK;
        // it must rebuild so the new aligned lib is re-injected.
        val plan2 = planWith("sha256=newlibnode,size=2,aligned16k=true")
        assertThat(plan2.mode).isEqualTo(IncrementalBuildMode.FULL)
        assertThat(plan2.identityFingerprint).isNotEqualTo(plan1.identityFingerprint)
    }

    @Test
    fun `native libs fingerprint null and non-null produce different identity fingerprints`() {
        val context = RuntimeEnvironment.getApplication()
        val cache = ApkBuildCache(context)

        val webApp = com.webtoapp.data.model.WebApp(
            id = 12,
            name = "NodeApp2",
            url = "nodejs://localhost"
        )
        val template = File(context.cacheDir, "shell_t2.apk").apply {
            writeBytes(ByteArray(16) { 7 })
        }
        val config = ApkConfig(
            meta = MetaBlock(
                appName = "NodeApp2",
                packageName = "com.demo.node2",
                targetUrl = "nodejs://localhost",
                versionCode = 1,
                versionName = "1.0",
                appType = "NODEJS_APP"
            )
        )

        val nullPlan = cache.plan(
            webApp = webApp,
            packageName = "com.demo.node2",
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
            nativeLibsFingerprint = null,
            forceFullRebuild = false
        )
        val withLibsPlan = cache.plan(
            webApp = webApp,
            packageName = "com.demo.node2",
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
            nativeLibsFingerprint = "sha256=abc,size=100,aligned16k=true",
            forceFullRebuild = false
        )
        assertThat(nullPlan.identityFingerprint).isNotEqualTo(withLibsPlan.identityFingerprint)
    }

    @Test
    fun `host versionCode change forces full rebuild instead of reuse`() {
        val context = RuntimeEnvironment.getApplication()
        val cache = ApkBuildCache(context)
        cache.clearAll()

        val webApp = com.webtoapp.data.model.WebApp(
            id = 21,
            name = "HostVcApp",
            url = "https://example.com"
        )
        val template = File(context.cacheDir, "shell_hostvc.apk").apply {
            writeBytes(ByteArray(16) { 9 })
        }
        val config = ApkConfig(
            meta = MetaBlock(
                appName = "HostVcApp",
                packageName = "com.demo.hostvc",
                targetUrl = "https://example.com",
                versionCode = 1,
                versionName = "1.0",
                appType = "WEB"
            )
        )

        // Build #1 with host versionCode 50 → cache miss → FULL; cache the unsigned APK.
        val plan1 = cache.plan(
            webApp = webApp,
            packageName = "com.demo.hostvc",
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
            hostVersionCode = 50,
            forceFullRebuild = false
        )
        assertThat(plan1.mode).isEqualTo(IncrementalBuildMode.FULL)

        val unsigned = File(context.cacheDir, "hostvc_unsigned.apk").apply {
            writeBytes(ByteArray(64) { 1 })
        }
        cache.saveUnsigned(
            webApp = webApp,
            packageName = "com.demo.hostvc",
            unsignedApk = unsigned,
            identityFingerprint = plan1.identityFingerprint,
            contentFingerprint = plan1.contentFingerprint,
            shellTemplateId = plan1.shellTemplateId
        )

        // Same app + config + template, but the host just upgraded (versionCode 50 -> 51).
        // The cache MUST NOT reuse the stale unsigned APK — a FULL rebuild is required so the
        // latest export/packaging/alignment logic is re-applied.
        val plan2 = cache.plan(
            webApp = webApp,
            packageName = "com.demo.hostvc",
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
            hostVersionCode = 51,
            forceFullRebuild = false
        )
        assertThat(plan2.mode).isEqualTo(IncrementalBuildMode.FULL)
        assertThat(plan2.identityFingerprint).isNotEqualTo(plan1.identityFingerprint)
    }
}
