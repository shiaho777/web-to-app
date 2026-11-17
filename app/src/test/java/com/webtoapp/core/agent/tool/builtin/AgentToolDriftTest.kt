package com.webtoapp.core.agent.tool.builtin

import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.google.gson.JsonObject
import com.webtoapp.core.agent.files.ProjectFileManager
import com.webtoapp.core.agent.permission.PermissionChecker
import com.webtoapp.core.agent.permission.PermissionMode
import com.webtoapp.core.agent.permission.PermissionPrompter
import com.webtoapp.core.agent.todo.TodoManager
import com.webtoapp.core.agent.tool.ToolContext
import com.webtoapp.core.engine.EngineType
import com.webtoapp.data.dao.WebAppDao
import com.webtoapp.data.dao.WebAppStartupCandidate
import com.webtoapp.data.dao.WebAppSummary
import com.webtoapp.data.model.AiModel
import com.webtoapp.data.model.AiProvider
import com.webtoapp.data.model.ApiKeyConfig
import com.webtoapp.data.model.AppType
import com.webtoapp.data.model.ModelCapability
import com.webtoapp.data.model.SavedModel
import com.webtoapp.data.model.WebApp
import com.webtoapp.data.repository.WebAppRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AgentToolDriftTest {

    @Test
    fun `ExportApp honors the documented template default when format is omitted`() = runBlocking {
        val repo = FakeRepo(seed = WebApp(id = 7, name = "Docs", url = "https://example.com", appType = AppType.WEB))
        val result = ExportAppTool().execute(
            JsonObject().apply { addProperty("appId", 7L) },
            context(repo)
        )
        assertThat(result.isError).isFalse()
        assertThat(result.text).startsWith("Exported to:")
    }

    @Test
    fun `ExportApp config format exports a config json path`() = runBlocking {
        val repo = FakeRepo(seed = WebApp(id = 7, name = "Docs", url = "https://example.com", appType = AppType.WEB))
        val result = ExportAppTool().execute(
            JsonObject().apply {
                addProperty("appId", 7L)
                addProperty("format", "config")
            },
            context(repo)
        )
        assertThat(result.isError).isFalse()
        assertThat(result.text).contains("_config_")
    }

    @Test
    fun `ExportApp rejects an unknown format instead of silently exporting`() = runBlocking {
        val repo = FakeRepo(seed = WebApp(id = 7, name = "Docs", url = "https://example.com", appType = AppType.WEB))
        val result = ExportAppTool().execute(
            JsonObject().apply {
                addProperty("appId", 7L)
                addProperty("format", "apk")
            },
            context(repo)
        )
        assertThat(result.isError).isTrue()
        assertThat(result.text).contains("unknown format")
    }

    @Test
    fun `ClearRuntimeCache description warns it uninstalls runtimes`() {
        val tool = ClearRuntimeCacheTool()
        assertThat(tool.isReadOnly()).isFalse()
        assertThat(tool.description).contains("Uninstall")
        assertThat(tool.description).contains("InstallRuntime")
        assertThat(tool.description).contains("not ready")
    }

    @Test
    fun `SelectEngine description uses the real GeckoView size constant`() {
        assertThat(SelectEngineTool().description)
            .contains("~${EngineType.GECKOVIEW.estimatedSizeMb}MB")
    }

    @Test
    fun `ExportAab description reflects the always-build behavior`() {
        assertThat(ExportAabTool().description).contains("Always produces a fresh incremental APK")
    }

    @Test
    fun `KillPort reports no managed allocation truthfully instead of claiming a kill`() = runBlocking {
        com.webtoapp.core.port.PortManager.releaseAll()
        val result = KillPortTool().execute(
            JsonObject().apply { addProperty("port", 59999) },
            context(FakeRepo())
        )
        assertThat(result.isError).isFalse()
        assertThat(result.text).contains("No WebToApp-managed service")
        assertThat(result.text).contains("nothing was killed")
    }

    @Test
    fun `KillPort stops a managed allocation and names the owner`() = runBlocking {
        com.webtoapp.core.port.PortManager.releaseAll()
        val port = com.webtoapp.core.port.PortManager.allocate(
            com.webtoapp.core.port.PortManager.PortRange.NODEJS, "nodejs:drift-test"
        )
        val result = KillPortTool().execute(
            JsonObject().apply { addProperty("port", port) },
            context(FakeRepo())
        )
        assertThat(result.isError).isFalse()
        assertThat(result.text).contains("nodejs:drift-test")
        assertThat(com.webtoapp.core.port.PortManager.isAllocated(port)).isFalse()
    }

    @Test
    fun `preview always asks because even a snapshot opens a task and tags the page`() {
        val tool = PreviewTool()
        assertThat(tool.isReadOnly()).isFalse()
        assertThat(tool.isReadOnly(JsonObject().apply { addProperty("appId", 1L) })).isFalse()
        assertThat(tool.isReadOnly(JsonObject().apply {
            addProperty("appId", 1L)
            addProperty("script", "return 1")
        })).isFalse()
        assertThat(tool.isReadOnly(JsonObject().apply {
            addProperty("appId", 1L)
            addProperty("script", "   ")
        })).isFalse()
    }

    @Test
    fun `preview script execution is denied in dream mode`() = runBlocking {
        val checker = PermissionChecker(PermissionPrompter(), initialMode = PermissionMode.Dream)
        val decision = checker.check(
            PreviewTool(),
            JsonObject().apply {
                addProperty("appId", 1L)
                addProperty("script", "return document.title")
            },
            context(FakeRepo())
        )
        assertThat(decision).isEqualTo(com.webtoapp.core.agent.permission.PermissionDecision.Deny)
    }

    @Test
    fun `BatchImportApps template mode restores an exported template`() {
        runBlocking {
        val repo = FakeRepo()
        val ctx = context(repo)
        val service = com.webtoapp.core.stats.BatchImportService(ctx.androidContext, ctx.appRepository)
        val template = service.exportAsTemplate(
            WebApp(
                id = 0, name = "Templated", url = "https://templated.example", appType = AppType.WEB,
                adBlockEnabled = true, adBlockRules = listOf("||ads.example.test^"),
                pluginIds = listOf("ext-1"), pluginsEnabled = true
            )
        )

        val result = BatchImportAppsTool().execute(
            JsonObject().apply {
                addProperty("text", template)
                addProperty("mode", "template")
            },
            ctx
        )

        assertThat(result.isError).isFalse()
        assertThat(result.text).contains("Imported template")
        val created = repo.apps.value.firstOrNull { it.url == "https://templated.example" }
        assertThat(created).isNotNull()
        assertThat(created!!.name).isEqualTo("Templated")
        assertThat(created.adBlockEnabled).isTrue()
        assertThat(created.adBlockRules).containsExactly("||ads.example.test^")
        assertThat(created.pluginIds).containsExactly("ext-1")
        }
    }

    @Test
    fun `BatchImportApps template mode rejects invalid json`() = runBlocking {
        val result = BatchImportAppsTool().execute(
            JsonObject().apply {
                addProperty("text", "not json")
                addProperty("mode", "template")
            },
            context(FakeRepo())
        )
        assertThat(result.isError).isTrue()
        assertThat(result.text).contains("not a valid app template")
    }

    private fun context(repo: FakeRepo): ToolContext {
        val appCtx = ApplicationProvider.getApplicationContext<android.content.Context>()
        return ToolContext(
            androidContext = appCtx,
            sessionId = "drift-test",
            fileManager = ProjectFileManager(appCtx),
            textModel = SavedModel(
                model = AiModel(id = "m", name = "m", provider = AiProvider.OPENAI),
                apiKeyId = "k",
                capabilities = listOf(ModelCapability.TEXT)
            ),
            textApiKey = ApiKeyConfig(provider = AiProvider.OPENAI, apiKey = "key"),
            prompter = PermissionPrompter(),
            todos = TodoManager(),
            appRepository = WebAppRepository(repo)
        )
    }

    private class FakeRepo(seed: WebApp? = null) : WebAppDao {
        val apps = kotlinx.coroutines.flow.MutableStateFlow(seed?.let { listOf(it) } ?: emptyList<WebApp>())
        private val nextId = kotlinx.coroutines.flow.MutableStateFlow(seed?.id?.plus(1) ?: 1L)

        override fun getAllWebApps(): Flow<List<WebApp>> = apps
        override fun getAllWebAppSummaries(): Flow<List<WebAppSummary>> = flowOf(emptyList())
        override fun getHttpWebApps(appType: AppType): Flow<List<WebApp>> = flowOf(emptyList())
        override suspend fun getStartupCandidatesWithoutIcons(appType: AppType, limit: Int): List<WebAppStartupCandidate> = emptyList()
        override suspend fun getWebAppById(id: Long): WebApp? = apps.value.firstOrNull { it.id == id }
        override fun getWebAppByIdFlow(id: Long): Flow<WebApp?> = flowOf(apps.value.firstOrNull { it.id == id })
        override suspend fun insert(webApp: WebApp): Long {
            val id = nextId.value
            nextId.value = id + 1
            apps.value = apps.value + webApp.copy(id = id)
            return id
        }
        override suspend fun update(webApp: WebApp) {
            apps.value = apps.value.map { if (it.id == webApp.id) webApp else it }
        }
        override suspend fun delete(webApp: WebApp) {}
        override suspend fun deleteById(id: Long) {}
        override suspend fun updateActivationStatus(id: Long, activated: Boolean) {}
        override suspend fun upgradeRemoteHttpUrls(appType: AppType, updatedAt: Long): Int = 0
        override suspend fun getCount(): Int = apps.value.size
        override fun searchWebApps(query: String): Flow<List<WebApp>> = flowOf(emptyList())
        override suspend fun insertAll(webApps: List<WebApp>): List<Long> = webApps.map { insert(it) }
        override suspend fun updateAll(webApps: List<WebApp>) {}
        override suspend fun deleteAll(webApps: List<WebApp>) {}
        override suspend fun deleteByIds(ids: List<Long>) {}
        override suspend fun updateActivationStatusBatch(ids: List<Long>, activated: Boolean) {}
        override suspend fun getWebAppsByIds(ids: List<Long>): List<WebApp> = apps.value.filter { it.id in ids }
        override fun getActivatedWebApps(): Flow<List<WebApp>> = flowOf(emptyList())
        override fun getRecentWebApps(limit: Int): Flow<List<WebApp>> = flowOf(emptyList())
        override suspend fun countByName(name: String, excludeId: Long): Int = 0
        override suspend fun clearCategoryId(categoryId: Long) {}
        override suspend fun getAllUrls(): List<String> = apps.value.map { it.url }
        override suspend fun updateHomeSortIndex(id: Long, index: Int) = Unit
    }
}
