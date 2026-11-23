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
import com.webtoapp.core.agent.tool.ToolContext
import com.webtoapp.core.agent.tool.ToolRegistry
import com.webtoapp.data.dao.WebAppDao
import com.webtoapp.data.dao.WebAppStartupCandidate
import com.webtoapp.data.dao.WebAppSummary
import com.webtoapp.core.ai.ApiKeyRotation
import com.webtoapp.core.ai.LimitScope
import com.webtoapp.data.model.AiModel
import com.webtoapp.data.model.AiProvider
import com.webtoapp.data.model.ApiKeyConfig
import com.webtoapp.data.model.AppType
import com.webtoapp.data.model.ModelCapability
import com.webtoapp.data.model.SavedModel
import com.webtoapp.data.model.WebApp
import com.webtoapp.data.repository.WebAppRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.atomic.AtomicInteger

@RunWith(RobolectricTestRunner::class)
class AgentEngineStreamTest {

    @Test(timeout = 15_000)
    fun `a turn subscribes the LLM stream exactly once and completes`() = runBlocking {
        val producerRuns = AtomicInteger(0)
        val gateway = object : LlmGateway {
            override fun chatStream(req: ChatRequest): Flow<LlmEvent> = callbackFlow {
                producerRuns.incrementAndGet()
                trySend(LlmEvent.Started)
                trySend(LlmEvent.TextDelta("Hello"))
                trySend(LlmEvent.TextDelta(", world"))
                trySend(LlmEvent.Done(FinishReason.STOP))
                close()
                awaitClose { }
            }
        }

        val events = engine(gateway).run(input()).toList(mutableListOf())

        assertEquals("one request attempt must subscribe the stream once", 1, producerRuns.get())
        assertEquals("Hello, world", events.filterIsInstance<AgentEvent.TextDelta>().joinToString("") { it.delta })
        assertEquals(
            "the last delta must carry the FULL accumulated text",
            "Hello, world",
            events.filterIsInstance<AgentEvent.TextDelta>().last().accumulated
        )
        val completed = events.last()
        assertTrue(completed is AgentEvent.Completed)
        assertEquals("Hello, world", (completed as AgentEvent.Completed).summary)
    }

    @Test(timeout = 15_000)
    fun `thinking marker and body text accumulate into one interleaved buffer`() = runBlocking {
        val gateway = object : LlmGateway {
            override fun chatStream(req: ChatRequest): Flow<LlmEvent> = callbackFlow {
                trySend(LlmEvent.Started)
                trySend(LlmEvent.ThinkingDelta("let me think"))
                trySend(LlmEvent.TextDelta("Hello"))
                trySend(LlmEvent.TextDelta(", "))
                trySend(LlmEvent.TextDelta("world"))
                trySend(LlmEvent.Done(FinishReason.STOP))
                close()
                awaitClose { }
            }
        }

        val events = engine(gateway).run(input()).toList(mutableListOf())

        val last = events.filterIsInstance<AgentEvent.TextDelta>().last()
        assertEquals(
            "marker must survive the body deltas that follow it",
            "\u2063TH:th-turn-1\u2063Hello, world",
            last.accumulated
        )
    }

    @Test(timeout = 15_000)
    fun `prose before a tool call marker is not truncated`() = runBlocking {
        val producerRuns = AtomicInteger(0)
        val gateway = object : LlmGateway {
            override fun chatStream(req: ChatRequest): Flow<LlmEvent> = callbackFlow {
                if (producerRuns.incrementAndGet() == 1) {
                    trySend(LlmEvent.Started)
                    trySend(LlmEvent.TextDelta("before the call"))
                    trySend(LlmEvent.ToolCallBegin("call_1", "noop"))
                    trySend(LlmEvent.ToolCallEnd("call_1", "noop", "{}"))
                    trySend(LlmEvent.Done(FinishReason.TOOL_CALLS))
                } else {
                    trySend(LlmEvent.Started)
                    trySend(LlmEvent.TextDelta("done"))
                    trySend(LlmEvent.Done(FinishReason.STOP))
                }
                close()
                awaitClose { }
            }
        }

        val events = engine(gateway).run(input()).toList(mutableListOf())

        val firstStreamAccumulated = events.filterIsInstance<AgentEvent.TextDelta>()
            .first { it.accumulated.contains("TC:call_1") }
            .accumulated
        assertTrue(
            "the marker event must still carry the prose that preceded it",
            firstStreamAccumulated.startsWith("before the call\u2063TC:call_1\u2063")
        )
    }

    @Test(timeout = 15_000)
    fun `recoverable stream error retries with a fresh request`() = runBlocking {
        val producerRuns = AtomicInteger(0)
        val sessionIds = mutableListOf<String>()
        val gateway = object : LlmGateway {
            override fun chatStream(req: ChatRequest): Flow<LlmEvent> = callbackFlow {
                val attempt = producerRuns.incrementAndGet()
                sessionIds += req.sessionId
                trySend(LlmEvent.Started)
                if (attempt == 1) {
                    trySend(LlmEvent.Error("stream interrupted", recoverable = true))
                } else {
                    trySend(LlmEvent.TextDelta("recovered"))
                    trySend(LlmEvent.Done(FinishReason.STOP))
                }
                close()
                awaitClose { }
            }
        }

        val events = engine(gateway).run(input()).toList(mutableListOf())

        assertEquals("the retry must re-request through the gateway", 2, producerRuns.get())
        assertEquals(listOf("test-session", "test-session"), sessionIds)
        assertTrue(events.filterIsInstance<AgentEvent.Notice>().isNotEmpty())
        assertTrue(events.filterIsInstance<AgentEvent.TextDelta>().any { it.delta == "recovered" })
        assertTrue(events.last() is AgentEvent.Completed)
    }

    @Test(timeout = 15_000)
    fun `an empty stream completes with an explicit empty-response summary`() = runBlocking {
        val gateway = object : LlmGateway {
            override fun chatStream(req: ChatRequest): Flow<LlmEvent> = callbackFlow {
                trySend(LlmEvent.Started)
                trySend(LlmEvent.Done(FinishReason.STOP))
                close()
                awaitClose { }
            }
        }

        val events = engine(gateway).run(input()).toList(mutableListOf())

        val completed = events.last()
        assertTrue(completed is AgentEvent.Completed)
        assertEquals(
            com.webtoapp.core.i18n.Strings.agentEmptyResponse,
            (completed as AgentEvent.Completed).summary
        )
    }

    @Test(timeout = 15_000)
    fun `a hard stream error fails the turn with the provider message`() = runBlocking {
        val gateway = object : LlmGateway {
            override fun chatStream(req: ChatRequest): Flow<LlmEvent> = callbackFlow {
                trySend(LlmEvent.Started)
                trySend(LlmEvent.Error("API key invalid or expired (401)"))
                close()
                awaitClose { }
            }
        }

        val events = engine(gateway).run(input()).toList(mutableListOf())

        val failed = events.last()
        assertTrue(failed is AgentEvent.Failed)
        assertEquals("API key invalid or expired (401)", (failed as AgentEvent.Failed).message)
    }

    @Test(timeout = 15_000)
    fun `a rate-limited key switches to the next one without waiting out a long cooldown`() = runBlocking {
        val keyA = ApiKeyConfig(id = "a", provider = AiProvider.GOOGLE, apiKey = "key-a-1111")
        val keyB = ApiKeyConfig(id = "b", provider = AiProvider.GOOGLE, apiKey = "key-b-2222")
        val seen = mutableListOf<String>()
        val gateway = object : LlmGateway {
            override fun chatStream(req: ChatRequest): Flow<LlmEvent> = callbackFlow {
                seen += req.apiKey.id
                trySend(LlmEvent.Started)
                if (req.apiKey.id == "a") {
                    trySend(LlmEvent.TextDelta("partial"))
                    trySend(LlmEvent.Error(
                        "Rate limited (429)",
                        recoverable = true,
                        retryAfterMs = 86_000_000,
                        httpCode = 429,
                        limitScope = LimitScope.MODEL
                    ))
                } else {
                    trySend(LlmEvent.TextDelta("ok"))
                    trySend(LlmEvent.Done(FinishReason.STOP))
                }
                close()
                awaitClose { }
            }
        }

        val events = AgentEngine(
            gateway,
            PermissionChecker(PermissionPrompter()),
            keyRotation = ApiKeyRotation()
        ).run(input(key = keyA, pool = listOf(keyA, keyB))).toList(mutableListOf())

        assertEquals(listOf("a", "b"), seen)
        assertTrue(events.filterIsInstance<AgentEvent.Notice>().any {
            it.message.contains("2222")
        })
        val completed = events.last()
        assertTrue(completed is AgentEvent.Completed)
        assertEquals("ok", (completed as AgentEvent.Completed).summary)
        assertEquals(
            "ok",
            events.filterIsInstance<AgentEvent.TextDelta>().last().accumulated
        )
    }

    private fun engine(gateway: LlmGateway) =
        AgentEngine(gateway, PermissionChecker(PermissionPrompter()))

    private fun input(
        key: ApiKeyConfig = ApiKeyConfig(id = "key-1", provider = AiProvider.OPENAI, apiKey = "test-key"),
        pool: List<ApiKeyConfig> = emptyList()
    ): AgentEngine.Input {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val toolContext = ToolContext(
            androidContext = ctx,
            sessionId = "test-session",
            fileManager = ProjectFileManager(ctx),
            textModel = SavedModel(
                model = AiModel(id = "test-model", name = "Test Model", provider = AiProvider.OPENAI),
                apiKeyId = "key-1",
                capabilities = listOf(ModelCapability.TEXT)
            ),
            textApiKey = key,
            textKeyPool = pool,
            prompter = PermissionPrompter(),
            todos = TodoManager(),
            appRepository = WebAppRepository(FakeWebAppDao())
        )
        return AgentEngine.Input(
            systemPrompt = "test",
            history = emptyList(),
            userMessage = "hi",
            toolContext = toolContext,
            registry = ToolRegistry(emptyList())
        )
    }

    private class FakeWebAppDao : WebAppDao {
        override fun getAllWebApps(): Flow<List<WebApp>> = flowOf(emptyList())
        override fun getAllWebAppSummaries(): Flow<List<WebAppSummary>> = flowOf(emptyList())
        override fun getHttpWebApps(appType: AppType): Flow<List<WebApp>> = flowOf(emptyList())
        override suspend fun getStartupCandidatesWithoutIcons(appType: AppType, limit: Int): List<WebAppStartupCandidate> = throw NotImplementedError()
        override suspend fun getWebAppById(id: Long): WebApp? = throw NotImplementedError()
        override fun getWebAppByIdFlow(id: Long): Flow<WebApp?> = throw NotImplementedError()
        override suspend fun insert(webApp: WebApp): Long = throw NotImplementedError()
        override suspend fun update(webApp: WebApp) = throw NotImplementedError()
        override suspend fun delete(webApp: WebApp) = throw NotImplementedError()
        override suspend fun deleteById(id: Long) = throw NotImplementedError()
        override suspend fun updateActivationStatus(id: Long, activated: Boolean) = throw NotImplementedError()
        override suspend fun upgradeRemoteHttpUrls(appType: AppType, updatedAt: Long): Int = throw NotImplementedError()
        override suspend fun getCount(): Int = throw NotImplementedError()
        override fun searchWebApps(query: String): Flow<List<WebApp>> = throw NotImplementedError()
        override suspend fun insertAll(webApps: List<WebApp>): List<Long> = throw NotImplementedError()
        override suspend fun updateAll(webApps: List<WebApp>) = throw NotImplementedError()
        override suspend fun deleteAll(webApps: List<WebApp>) = throw NotImplementedError()
        override suspend fun deleteByIds(ids: List<Long>) = throw NotImplementedError()
        override suspend fun updateActivationStatusBatch(ids: List<Long>, activated: Boolean) = throw NotImplementedError()
        override suspend fun getWebAppsByIds(ids: List<Long>): List<WebApp> = throw NotImplementedError()
        override fun getActivatedWebApps(): Flow<List<WebApp>> = throw NotImplementedError()
        override fun getRecentWebApps(limit: Int): Flow<List<WebApp>> = throw NotImplementedError()
        override suspend fun countByName(name: String, excludeId: Long): Int = throw NotImplementedError()
        override suspend fun clearCategoryId(categoryId: Long) = throw NotImplementedError()
        override suspend fun getAllUrls(): List<String> = throw NotImplementedError()
        override suspend fun updateHomeSortIndex(id: Long, index: Int) = throw NotImplementedError()
    }
}
