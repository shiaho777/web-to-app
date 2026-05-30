package com.webtoapp.core.backup

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.webtoapp.core.stats.AppUsageStats
import com.webtoapp.data.database.AppDatabase
import com.webtoapp.data.model.AppType
import com.webtoapp.data.model.NodeJsConfig
import com.webtoapp.data.model.WebApp
import com.webtoapp.data.repository.WebAppRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class DataBackupManagerTest {

    @Rule @JvmField
    val koinRule = com.webtoapp.util.KoinCleanupRule()

    private lateinit var context: Context
    private lateinit var manager: DataBackupManager
    private lateinit var repository: WebAppRepository
    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        db = AppDatabase.getInstance(context)
        repository = WebAppRepository(db.webAppDao())
        manager = DataBackupManager(context)
    }

    @After
    fun tearDown() {
        AppDatabase.closeDatabase()
        File(context.filesDir, "nodejs_projects").deleteRecursively()
        File(context.filesDir, "backup_icons").deleteRecursively()
        context.cacheDir.listFiles()?.forEach { it.deleteRecursively() }
    }

    private fun backupFileUri(): Pair<File, Uri> {
        val file = File(context.cacheDir, "test_backup_${System.nanoTime()}.zip")
        return file to Uri.fromFile(file)
    }

    private suspend fun wipeApps() {
        repository.allWebApps.first().forEach { repository.deleteWebApp(it) }
    }

    @Test
    fun `nodejs project files survive export and import`() = runTest {

        val projectId = "proj-node-1"
        val projectDir = File(context.filesDir, "nodejs_projects/$projectId").apply { mkdirs() }
        File(projectDir, "server.js").writeText("console.log('hi')")
        File(projectDir, "node_modules/dep/index.js").apply {
            parentFile?.mkdirs()
            writeText("module.exports = 1")
        }

        val app = WebApp(
            name = "My Node App",
            url = "http://127.0.0.1:3000",
            appType = AppType.NODEJS_APP,
            nodejsConfig = NodeJsConfig(projectId = projectId, projectName = "My Node App")
        )
        repository.createWebApp(app)

        val (file, uri) = backupFileUri()
        val exportResult = manager.exportAllData(repository, uri)
        assertThat(exportResult.isSuccess).isTrue()
        assertThat(file.exists()).isTrue()

        wipeApps()
        projectDir.deleteRecursively()
        assertThat(File(context.filesDir, "nodejs_projects/$projectId").exists()).isFalse()

        val importResult = manager.importAllData(repository, uri)
        assertThat(importResult.isSuccess).isTrue()
        assertThat(importResult.getOrThrow().importedCount).isEqualTo(1)

        val restored = repository.allWebApps.first().single { it.appType == AppType.NODEJS_APP }
        assertThat(restored.nodejsConfig?.projectId).isEqualTo(projectId)

        val restoredProject = File(context.filesDir, "nodejs_projects/$projectId")
        assertThat(File(restoredProject, "server.js").readText()).isEqualTo("console.log('hi')")
        assertThat(File(restoredProject, "node_modules/dep/index.js").exists()).isTrue()
    }

    @Test
    fun `usage stats are remapped to the new app id`() = runTest {
        wipeApps()
        val originalId = repository.createWebApp(
            WebApp(name = "Tracked", url = "https://example.com", appType = AppType.WEB)
        )
        db.appUsageStatsDao().insert(
            AppUsageStats(appId = originalId, launchCount = 7, totalUsageMs = 123_456L)
        )

        val (_, uri) = backupFileUri()
        assertThat(manager.exportAllData(repository, uri).isSuccess).isTrue()

        wipeApps()
        assertThat(db.appUsageStatsDao().getAllStats().first()).isEmpty()

        assertThat(manager.importAllData(repository, uri).isSuccess).isTrue()

        val newApp = repository.allWebApps.first().single { it.name == "Tracked" }
        val stats = db.appUsageStatsDao().getStatsByAppId(newApp.id)
        assertThat(stats).isNotNull()
        assertThat(stats!!.launchCount).isEqualTo(7)
        assertThat(stats.totalUsageMs).isEqualTo(123_456L)

        assertThat(stats.appId).isEqualTo(newApp.id)
    }

    @Test
    fun `legacy v3 backup without usageStats still imports`() = runTest {
        wipeApps()
        repository.createWebApp(
            WebApp(name = "Legacy App", url = "https://legacy.example", appType = AppType.WEB)
        )

        val (_, uri) = backupFileUri()
        assertThat(manager.exportAllData(repository, uri).isSuccess).isTrue()

        wipeApps()

        val result = manager.importAllData(repository, uri)
        assertThat(result.isSuccess).isTrue()
        assertThat(repository.allWebApps.first().single().name).isEqualTo("Legacy App")
    }
}
