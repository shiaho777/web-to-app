package com.webtoapp.core.backup

import android.app.Application
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.android.apksig.ApkVerifier
import com.google.common.truth.Truth.assertThat
import com.webtoapp.core.apkbuilder.ApkBuilder
import com.webtoapp.core.apkbuilder.BuildFailureCause
import com.webtoapp.core.apkbuilder.BuildResult
import com.webtoapp.core.apkbuilder.PerAppSigningIdentity
import com.webtoapp.core.i18n.Strings
import com.webtoapp.data.database.AppDatabase
import com.webtoapp.data.model.ApkExportConfig
import com.webtoapp.data.model.AppType
import com.webtoapp.data.model.HtmlConfig
import com.webtoapp.data.model.HtmlFile
import com.webtoapp.data.model.HtmlFileType
import com.webtoapp.data.model.HtmlLoadMode
import com.webtoapp.data.model.WebApp
import com.webtoapp.data.repository.WebAppRepository
import java.io.File
import java.util.zip.ZipFile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class)
class SigningMigrationTest {
    private val context: Application get() = ApplicationProvider.getApplicationContext()
    private val repository by lazy { WebAppRepository(AppDatabase.getInstance(context).webAppDao(), context) }
    private val backup by lazy { DataBackupManager(context) }
    private val password = "migration-test-password"

    @Before
    @After
    fun resetDatabase() {
        AppDatabase.closeDatabase()
    }

    @Test
    fun `encrypted backup preserves the signature of an exported APK across rename and restore`(): Unit = runBlocking {
        assumeTrue(runCatching { context.assets.open("template/webview_shell.apk").use { it.read() >= 0 } }
            .getOrDefault(false))
        val project = WebApp(
            name = "WTA Signing Migration Probe",
            url = "http://127.0.0.1:18742/",
            apkExportConfig = ApkExportConfig(perAppSigningEnabled = true)
        )
        val id = repository.createWebApp(project)
        val packageName = ApkBuilder.generatePackageName(project.name)
        val first = build(project.copy(id = id))
        val firstCertificate = signingCertificate(first)
        val published = checkNotNull(repository.getWebApp(id))
        assertThat(published.packageName).isEqualTo(packageName)
        val original = checkNotNull(PerAppSigningIdentity.readExisting(context, packageName))
        assertThat(published.apkExportConfig?.publishedPerAppSigning?.certificateSha256)
            .isEqualTo(original.certSha256Hex())

        repository.updateWebApp(project.copy(id = id, name = "WTA Signing Migration Renamed"))
        val renamed = checkNotNull(repository.getWebApp(id))
        assertThat(ApkBuilder.resolvePackageName(renamed)).isEqualTo(packageName)
        assertThat(renamed.apkExportConfig?.publishedPerAppSigning).isEqualTo(published.apkExportConfig?.publishedPerAppSigning)

        val archive = File(context.cacheDir, "protected-projects.zip")
        val uri = Uri.fromFile(archive)
        assertThat(backup.exportAllData(repository, uri, password).getOrThrow().signingKeyCount).isEqualTo(1)
        ZipFile(archive).use { zip ->
            assertThat(zip.getEntry(PerAppSigningBackup.ENTRY)).isNotNull()
            assertThat(zip.entries().asSequence().none {
                it.name.endsWith(original.storeFile.name) || it.name.contains(PerAppSigningIdentity.CREDENTIAL_FILE)
            }).isTrue()
        }

        repository.deleteWebAppById(id)
        removeLocalKey(original)
        assertThat(backup.importAllData(repository, uri).exceptionOrNull()).isInstanceOf(SigningBackupPasswordRequired::class.java)
        assertThat(backup.importAllData(repository, uri, "incorrect-password").exceptionOrNull())
            .isInstanceOf(SigningBackupUnreadable::class.java)
        assertThat(repository.getWebAppCount()).isEqualTo(0)

        val conflicting = PerAppSigningIdentity.identityFor(context, packageName)
        val conflict = backup.importAllData(repository, uri, password).exceptionOrNull()
        assertThat(conflict?.message).isEqualTo(Strings.signingBackupConflict)
        assertThat(PerAppSigningIdentity.identityFor(context, packageName).certSha256Hex())
            .isEqualTo(conflicting.certSha256Hex())
        assertThat(repository.getWebAppCount()).isEqualTo(0)
        assertThat(conflicting.storeFile.delete()).isTrue()

        assertThat(backup.importAllData(repository, uri, password).getOrThrow().missingSigningKeys).isEqualTo(0)
        val restored = repository.allWebApps.first().single()
        val second = build(restored.copy(
            apkExportConfig = checkNotNull(restored.apkExportConfig).copy(customVersionCode = 2, customVersionName = "2.0.0")
        ))

        assertThat(first.packageName).isEqualTo(packageName)
        assertThat(second.packageName).isEqualTo(packageName)
        assertThat(signingCertificate(second)).isEqualTo(firstCertificate)
        assertThat(backup.importAllData(repository, uri, password).getOrThrow().duplicateCount).isEqualTo(1)

        System.getenv("WTA_SIGNING_SMOKE_DIR")?.let { path ->
            val dir = File(path).apply { mkdirs() }
            first.apkFile.copyTo(File(dir, "migration-v1.apk"), overwrite = true)
            second.apkFile.copyTo(File(dir, "migration-v2.apk"), overwrite = true)
            File(dir, "package-name.txt").writeText(packageName)
            val offline = buildOfflineHtml()
            offline.apkFile.copyTo(File(dir, "offline-startup.apk"), overwrite = true)
            File(dir, "offline-package-name.txt").writeText(checkNotNull(offline.packageName))
        }
    }

    @Test
    fun `backup without keys remembers an existing signer and prevents generating a replacement`(): Unit = runBlocking {
        val project = WebApp(
            name = "Plain signing backup",
            url = "https://example.invalid",
            apkExportConfig = ApkExportConfig(perAppSigningEnabled = true)
        )
        val packageName = ApkBuilder.resolvePackageName(project)
        val original = PerAppSigningIdentity.identityFor(context, packageName)
        val id = repository.createWebApp(project)
        val archive = File(context.cacheDir, "plain-projects.zip")
        val uri = Uri.fromFile(archive)
        assertThat(backup.exportAllData(repository, uri).getOrThrow().signingKeyCount).isEqualTo(0)
        ZipFile(archive).use { zip ->
            assertThat(zip.getEntry(PerAppSigningBackup.ENTRY)).isNull()
        }
        repository.deleteWebAppById(id)
        removeLocalKey(original)
        assertThat(backup.importAllData(repository, uri).getOrThrow().missingSigningKeys).isEqualTo(1)
        val restored = repository.allWebApps.first().single()
        assertThat(restored.apkExportConfig?.publishedPerAppSigning?.certificateSha256)
            .isEqualTo(original.certSha256Hex())
        val result = ApkBuilder(context).buildApk(restored) as BuildResult.Error
        assertThat(result.diagnostic?.cause).isEqualTo(BuildFailureCause.SIGNING_EXCEPTION)
        assertThat(result.message).contains(Strings.signingBackupMissingKey)
        assertThat(PerAppSigningIdentity.readExisting(context, packageName)).isNull()
    }

    private suspend fun buildOfflineHtml(): BuildResult.Success {
        val html = File(context.cacheDir, "index.html").apply {
            writeText(
                """
                <!doctype html><html lang="en"><head><meta charset="utf-8"></head>
                <body><h1 id="state"></h1><script>
                document.getElementById('state').textContent = 'PACKAGED OFFLINE HTML STARTED';
                </script></body></html>
                """.trimIndent()
            )
        }
        return build(WebApp(
            name = "WTA Offline Startup Probe",
            url = "",
            appType = AppType.HTML,
            htmlConfig = HtmlConfig(
                files = listOf(HtmlFile(html.name, html.path, HtmlFileType.HTML)),
                loadMode = HtmlLoadMode.FILE
            ),
            apkExportConfig = ApkExportConfig(customPackageName = "org.example.wta.smoke.offline")
        ))
    }

    private suspend fun build(project: WebApp): BuildResult.Success {
        val result = ApkBuilder(context).buildApk(project)
        check(result is BuildResult.Success) { result.toString() }
        return result
    }

    private fun signingCertificate(build: BuildResult.Success): ByteArray {
        val verification = ApkVerifier.Builder(build.apkFile).build().verify()
        assertThat(verification.isVerified).isTrue()
        return verification.signerCertificates.single().encoded
    }

    private fun removeLocalKey(identity: PerAppSigningIdentity.Identity) {
        assertThat(identity.storeFile.delete()).isTrue()
        assertThat(File(context.filesDir, PerAppSigningIdentity.CREDENTIAL_FILE).delete()).isTrue()
    }
}
