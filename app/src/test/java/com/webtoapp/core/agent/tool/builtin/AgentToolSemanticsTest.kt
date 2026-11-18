package com.webtoapp.core.agent.tool.builtin

import androidx.test.core.app.ApplicationProvider
import com.google.gson.JsonObject
import com.webtoapp.core.agent.files.ProjectFileManager
import com.webtoapp.core.agent.permission.PermissionPrompter
import com.webtoapp.core.agent.todo.TodoManager
import com.webtoapp.core.agent.tool.ToolContext
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.io.RandomAccessFile

@RunWith(RobolectricTestRunner::class)
class AgentToolSemanticsTest {

    private fun context(repo: FakeRepo = FakeRepo()): ToolContext {
        val appCtx = ApplicationProvider.getApplicationContext<android.content.Context>()
        return ToolContext(
            androidContext = appCtx,
            sessionId = "semantics-test",
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

    @Test
    fun `UpdateApp does not report unknown patch keys as changed`() = runTest {
        val repo = FakeRepo(seed = WebApp(id = 7, name = "Old", url = "https://old.example", appType = AppType.WEB))
        val result = UpdateAppTool().execute(
            JsonObject().apply {
                addProperty("appId", 7L)
                addProperty("patch", """{"totallyBogusKey":"x"}""")
            },
            context(repo)
        )
        assertTrue("a patch of only unknown keys must be rejected", result.isError)
        assertEquals(null, repo.updatedApp)
    }

    @Test
    fun `UpdateApp still reports real changes alongside unknown keys`() = runTest {
        val repo = FakeRepo(seed = WebApp(id = 8, name = "Old", url = "https://old.example", appType = AppType.WEB))
        val result = UpdateAppTool().execute(
            JsonObject().apply {
                addProperty("appId", 8L)
                addProperty("patch", """{"name":"New","totallyBogusKey":"x"}""")
            },
            context(repo)
        )
        assertFalse(result.isError)
        assertEquals(listOf("name"), result.appChange!!.changedFields)
        assertTrue(result.text.contains("totallyBogusKey"))
    }

    @Test
    fun `ReadAppFile windows text without loading the whole file`() = runTest {
        val ctx = context()
        val dir = File(ctx.androidContext.filesDir, "html_projects/semantics").apply { mkdirs() }
        val file = File(dir, "lines.txt")
        file.bufferedWriter().use { w ->
            repeat(5000) { i -> w.write("line-$i"); w.newLine() }
        }
        try {
            val result = ReadAppFileTool().execute(
                JsonObject().apply {
                    addProperty("path", "html_projects/semantics/lines.txt")
                    addProperty("offset", 10)
                    addProperty("limit", 5)
                },
                ctx
            )
            assertFalse(result.isError)
            assertTrue(result.text.contains("11\tline-10"))
            assertTrue(result.text.contains("15\tline-14"))
            assertFalse(result.text.contains("line-15"))
            assertTrue(result.text.contains("4985 more lines"))
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    fun `ReadAppFile refuses oversized text and image files`() = runTest {
        val ctx = context()
        val dir = File(ctx.androidContext.filesDir, "html_projects/semantics-big").apply { mkdirs() }
        val text = File(dir, "big.txt")
        val image = File(dir, "big.png")
        try {
            RandomAccessFile(text, "rw").use { it.setLength(9L * 1024 * 1024) }
            RandomAccessFile(image, "rw").use { it.setLength(21L * 1024 * 1024) }
            val textResult = ReadAppFileTool().execute(
                JsonObject().apply { addProperty("path", "html_projects/semantics-big/big.txt") },
                ctx
            )
            assertTrue(textResult.isError)
            assertTrue(textResult.text.contains("too large"))
            val imageResult = ReadAppFileTool().execute(
                JsonObject().apply { addProperty("path", "html_projects/semantics-big/big.png") },
                ctx
            )
            assertTrue(imageResult.isError)
            assertTrue(imageResult.text.contains("too large"))
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    fun `tool descriptions match actual behavior`() {
        val glob = GlobTool().description
        assertFalse("Glob must not advertise unsupported brace alternation", glob.contains("*.{js,ts}"))
        val export = ExportAppTool().description
        assertFalse("ExportApp config writes a file, not inline JSON", export.contains("as a JSON string"))
        val init = InitializeBuildEnvTool().description
        assertFalse("component is required, there is no default", init.contains("(default)"))
        val clear = ClearRuntimeCacheTool().description
        assertTrue("ClearRuntimeCache must disclose the bundled-node exception", clear.contains("bundled"))
    }

    private class FakeRepo(seed: WebApp? = null) : WebAppDao {
        private val nextId = MutableStateFlow(seed?.id?.plus(1) ?: 1L)
        private val apps = MutableStateFlow(seed?.let { listOf(it) } ?: emptyList<WebApp>())
        var updatedApp: WebApp? = null
            private set

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
            updatedApp = webApp
            apps.value = apps.value.map { if (it.id == webApp.id) webApp else it }
        }
        override suspend fun delete(webApp: WebApp) {}
        override suspend fun deleteById(id: Long) {}
        override suspend fun updateActivationStatus(id: Long, activated: Boolean) {}
        override suspend fun upgradeRemoteHttpUrls(appType: AppType, updatedAt: Long): Int = 0
        override suspend fun getCount(): Int = apps.value.size
        override fun searchWebApps(query: String): Flow<List<WebApp>> = flowOf(emptyList())
        override suspend fun insertAll(webApps: List<WebApp>): List<Long> = emptyList()
        override suspend fun updateAll(webApps: List<WebApp>) {}
        override suspend fun deleteAll(webApps: List<WebApp>) {}
        override suspend fun deleteByIds(ids: List<Long>) {}
        override suspend fun updateActivationStatusBatch(ids: List<Long>, activated: Boolean) {}
        override suspend fun getWebAppsByIds(ids: List<Long>): List<WebApp> = emptyList()
        override fun getActivatedWebApps(): Flow<List<WebApp>> = flowOf(emptyList())
        override fun getRecentWebApps(limit: Int): Flow<List<WebApp>> = flowOf(emptyList())
        override suspend fun countByName(name: String, excludeId: Long): Int = 0
        override suspend fun clearCategoryId(categoryId: Long) {}
        override suspend fun getAllUrls(): List<String> = emptyList()
        override suspend fun updateHomeSortIndex(id: Long, index: Int) = Unit
    }
}
