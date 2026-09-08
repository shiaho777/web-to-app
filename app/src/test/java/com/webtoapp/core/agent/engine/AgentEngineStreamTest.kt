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

/**
 * End-to-end regression tests for the agent turn loop. The #742 collector
 * re-subscribed the cold gateway flow per event, so a turn never received any
 * content (UI stuck on "thinking") while the provider was spammed with
 * duplicate requests. The gateway fake below is a cold callbackFlow just like
 * the real providers and counts how many times its producer runs.
 */
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
                close() // real providers close right after the terminal event
                awaitClose { }
            }
        }

        val events = engine(gateway).run(input()).toList(mutableListOf())

        assertEquals("one request attempt must subscribe the stream once", 1, producerRuns.get())
        assertEquals("Hello, world", events.filterIsInstance<AgentEvent.TextDelta>().joinToString("") { it.delta })
        // The accumulated buffer must grow monotonically — #749 briefly rebuilt it per
        // delta, leaving only the newest fragment (body streamed in replacing chunks
        // and the persisted message ended up one token long).
        assertEquals(
            "the last delta must carry the FULL accumulated text",
            "Hello, world",
            events.filterIsInstance<AgentEvent.TextDelta>().last().accumulated
        )
        val completed = events.last()
        assertTrue(completed is AgentEvent.Completed)
        assertEquals("Hello, world", (completed as AgentEvent.Completed).summary)
    }

    /**
     * Thinking arrives first and anchors a TH marker in the accumulated text; body
     * deltas must append AFTER the marker, keeping both intact across the stream.
     */
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

    /**
     * A tool call that follows streamed prose must not truncate that prose: the TC
     * marker is appended after it in the accumulated text.
     */
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
        val gateway = object : LlmGateway {
            override fun chatStream(req: ChatRequest): Flow<LlmEvent> = callbackFlow {
                val attempt = producerRuns.incrementAndGet()
                trySend(LlmEvent.Started)
                if (attempt == 1) {
                    trySend(LlmEvent.Error("stream interrupted", recoverable = true))
                } else {
                    trySend(LlmEvent.TextDelta("recovered"))
                    trySend(LlmEvent.Done(FinishReason.STOP))
                }
                close() // real providers close right after the terminal event
                awaitClose { }
            }
        }

        val events = engine(gateway).run(input()).toList(mutableListOf())

        assertEquals("the retry must re-request through the gateway", 2, producerRuns.get())
        assertTrue(events.filterIsInstance<AgentEvent.Notice>().isNotEmpty())
        assertTrue(events.filterIsInstance<AgentEvent.TextDelta>().any { it.delta == "recovered" })
        assertTrue(events.last() is AgentEvent.Completed)
    }

    /**
     * A stream that opens, completes with zero deltas, and closes is a distinct
     * outcome from a request error. The Completed summary must say so explicitly —
     * an empty summary previously made the whole turn vanish (nothing persisted).
     */
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

    /**
     * A hard (non-recoverable) stream error must fail the turn with the provider's
     * message — the UI and the session both rely on this message being present.
     */
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

    private fun engine(gateway: LlmGateway) =
        AgentEngine(gateway, PermissionChecker(PermissionPrompter()))

    private fun input(): AgentEngine.Input {
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
            textApiKey = ApiKeyConfig(provider = AiProvider.OPENAI, apiKey = "test-key"),
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

    /** The repository eagerly reads three Flow properties at construction; the rest is never called in tool-free turns. */
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
    }
}
