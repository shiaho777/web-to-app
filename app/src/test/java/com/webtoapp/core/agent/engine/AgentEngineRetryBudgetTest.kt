package com.webtoapp.core.agent.engine

import androidx.test.core.app.ApplicationProvider
import com.webtoapp.core.agent.files.ProjectFileManager
import com.webtoapp.core.agent.llm.ChatRequest
import com.webtoapp.core.agent.llm.FinishReason
import com.webtoapp.core.agent.llm.LlmEvent
import com.webtoapp.core.agent.llm.LlmGateway
import com.webtoapp.core.agent.permission.PermissionChecker
import com.webtoapp.core.agent.permission.PermissionPrompter
import com.webtoapp.core.agent.todo.TodoManager
import com.webtoapp.core.agent.tool.Tool
import com.webtoapp.core.agent.tool.ToolContext
import com.webtoapp.core.agent.tool.ToolRegistry
import com.webtoapp.core.agent.tool.ToolResult
import com.webtoapp.core.ai.ApiKeyRotation
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
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.atomic.AtomicInteger

@RunWith(RobolectricTestRunner::class)
class AgentEngineRetryBudgetTest {

    private class TickTool : Tool {
        val calls = AtomicInteger(0)
        override val name = "tick"
        override val description = "Advance the test turn."
        override val parametersSchema: JsonElement = JsonObject()
        override fun isReadOnly() = false
        override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
            calls.incrementAndGet()
            return ToolResult.ok("tick")
        }
    }

    @Test(timeout = 30_000)
    fun `the same-key retry budget resets each turn so a later transient error recovers`() = runBlocking {
        val tool = TickTool()
        val attempts = AtomicInteger(0)
        val gateway = object : LlmGateway {
            override fun chatStream(req: ChatRequest): Flow<LlmEvent> = callbackFlow {
                val attempt = attempts.incrementAndGet()
                trySend(LlmEvent.Started)
                when (attempt) {
                    1 -> trySend(LlmEvent.Error("transient idle", recoverable = true, retryAfterMs = 0L))
                    2 -> {
                        trySend(LlmEvent.ToolCallBegin("c1", "tick"))
                        trySend(LlmEvent.ToolCallEnd("c1", "tick", "{}"))
                        trySend(LlmEvent.Done(FinishReason.TOOL_CALLS))
                    }
                    3 -> trySend(LlmEvent.Error("transient idle again", recoverable = true, retryAfterMs = 0L))
                    else -> {
                        trySend(LlmEvent.TextDelta("recovered"))
                        trySend(LlmEvent.Done(FinishReason.STOP))
                    }
                }
                close()
                awaitClose { }
            }
        }

        val events = AgentEngine(
            gateway,
            PermissionChecker(PermissionPrompter(), initialMode = com.webtoapp.core.agent.permission.PermissionMode.AutoApprove),
            keyRotation = ApiKeyRotation()
        ).run(input(tool)).toList(mutableListOf())

        assertTrue("the first turn must recover from its transient error", attempts.get() >= 2)
        assertTrue("the tool must run once between turns", tool.calls.get() == 1)
        assertTrue("the second turn must recover from its own transient error", events.last() is AgentEvent.Completed)
        assertTrue(events.none { it is AgentEvent.Failed })
        assertTrue(events.filterIsInstance<AgentEvent.TextDelta>().any { it.delta == "recovered" })
    }

    private fun input(tool: Tool): AgentEngine.Input {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val toolContext = ToolContext(
            androidContext = ctx,
            sessionId = "retry-budget-test",
            fileManager = ProjectFileManager(ctx),
            textModel = SavedModel(
                model = AiModel(id = "test-model", name = "Test Model", provider = AiProvider.OPENAI),
                apiKeyId = "key-1",
                capabilities = listOf(ModelCapability.TEXT)
            ),
            textApiKey = ApiKeyConfig(id = "key-1", provider = AiProvider.OPENAI, apiKey = "test-key"),
            prompter = PermissionPrompter(),
            todos = TodoManager(),
            appRepository = WebAppRepository(FakeWebAppDao())
        )
        return AgentEngine.Input(
            systemPrompt = "test",
            history = emptyList(),
            userMessage = "hi",
            toolContext = toolContext,
            registry = ToolRegistry(listOf(tool))
        )
    }

    private class FakeWebAppDao : WebAppDao {
        override fun getAllWebApps(): Flow<List<WebApp>> = flowOf(emptyList())
        override fun getAllWebAppSummaries(): Flow<List<WebAppSummary>> = flowOf(emptyList())
        override fun getHttpWebApps(appType: AppType): Flow<List<WebApp>> = flowOf(emptyList())
        override suspend fun getStartupCandidatesWithoutIcons(appType: AppType, limit: Int): List<WebAppStartupCandidate> = emptyList()
        override suspend fun getWebAppById(id: Long): WebApp? = null
        override fun getWebAppByIdFlow(id: Long): Flow<WebApp?> = flowOf(null)
        override suspend fun insert(webApp: WebApp): Long = 1L
        override suspend fun update(webApp: WebApp) {}
        override suspend fun delete(webApp: WebApp) {}
        override suspend fun deleteById(id: Long) {}
        override suspend fun updateActivationStatus(id: Long, activated: Boolean) {}
        override suspend fun upgradeRemoteHttpUrls(appType: AppType, updatedAt: Long): Int = 0
        override suspend fun getCount(): Int = 0
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
