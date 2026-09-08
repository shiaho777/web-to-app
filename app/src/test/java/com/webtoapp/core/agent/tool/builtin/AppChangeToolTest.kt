package com.webtoapp.core.agent.tool.builtin

import androidx.test.core.app.ApplicationProvider
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.webtoapp.core.agent.files.ProjectFileManager
import com.webtoapp.core.agent.permission.PermissionChecker
import com.webtoapp.core.agent.permission.PermissionPrompter
import com.webtoapp.core.agent.todo.TodoManager
import com.webtoapp.core.agent.tool.AppChange
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * CreateApp/UpdateApp must attach an [AppChange] to every successful result so the
 * agent UI can surface the "app changes" review card after a turn — without it the
 * turn ends with zero indication that an app was created or modified.
 */
@RunWith(RobolectricTestRunner::class)
class AppChangeToolTest {

    @Test
    fun `CreateApp manifest path reports a CREATE app change`() = runTest {
        val repo = FakeRepo()
        val tool = CreateAppTool()

        val result = tool.execute(
            JsonObject().apply {
                addProperty("appType", "WEB")
                addProperty("name", "Docs Site")
                addProperty("manifest", """{"url":"https://example.com"}""")
            },
            context(repo)
        )

        assertFalse(result.isError)
        val change = result.appChange
        assertNotNull("a created app must carry appChange for the review card", change)
        change!!
        assertEquals(AppChange.Kind.CREATE, change.kind)
        assertEquals("Docs Site", change.appName)
        assertEquals("WEB", change.appType)
        assertEquals(repo.lastInsertedId, change.appId)
    }

    @Test
    fun `UpdateApp reports an UPDATE app change with the patched top-level fields`() = runTest {
        val repo = FakeRepo(seed = WebApp(id = 7, name = "Old", url = "https://old.example", appType = AppType.WEB))
        val tool = UpdateAppTool()

        val result = tool.execute(
            JsonObject().apply {
                addProperty("appId", 7L)
                addProperty("patch", """{"name":"New Name","url":"https://new.example"}""")
            },
            context(repo)
        )

        assertFalse(result.isError)
        val change = result.appChange
        assertNotNull("an updated app must carry appChange for the review card", change)
        change!!
        assertEquals(AppChange.Kind.UPDATE, change.kind)
        assertEquals(7L, change.appId)
        assertEquals("New Name", change.appName)
        // The patch's top-level keys are what the review card shows as "what changed".
        assertEquals(listOf("name", "url"), change.changedFields)
        assertEquals("New Name", repo.updatedApp!!.name)
    }

    @Test
    fun `failed tool calls carry no app change`() = runTest {
        val repo = FakeRepo()
        val create = CreateAppTool().execute(
            JsonObject().apply {
                addProperty("appType", "WEB")
                addProperty("name", "No Manifest")
            },
            context(repo)
        )
        assertTrue(create.isError)
        assertEquals(null, create.appChange)

        val update = UpdateAppTool().execute(
            JsonObject().apply {
                addProperty("appId", 404L)
                addProperty("patch", """{"name":"x"}""")
            },
            context(repo)
        )
        assertTrue(update.isError)
        assertEquals(null, update.appChange)
    }

    private fun context(repo: FakeRepo): ToolContext {
        val appCtx = ApplicationProvider.getApplicationContext<android.content.Context>()
        return ToolContext(
            androidContext = appCtx,
            sessionId = "test-session",
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
        private val nextId = MutableStateFlow(seed?.id?.plus(1) ?: 1L)
        private val apps = MutableStateFlow(seed?.let { listOf(it) } ?: emptyList<WebApp>())
        var lastInsertedId: Long = -1
            private set
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
            lastInsertedId = id
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
    }
}
