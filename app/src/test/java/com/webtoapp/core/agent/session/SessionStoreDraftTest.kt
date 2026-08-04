package com.webtoapp.core.agent.session

import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.webtoapp.core.agent.files.ProjectFileManager
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Tests [SessionStore] draft lifecycle (upsert / finalize / dropDraft) — the
 * persistence path that keeps in-flight Agent output from being lost when the
 * UI is destroyed mid-turn.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SessionStoreDraftTest {

    private lateinit var store: SessionStore
    private lateinit var files: ProjectFileManager
    private var sessionId: String = ""

    @Before
    fun setUp() = runBlocking {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        files = ProjectFileManager(ctx)
        store = SessionStore(ctx, files)
        val session = store.create("test-session")
        sessionId = session.id
    }

    private fun makeMessage(id: String, content: String, thinking: String? = null): AgentMessage {
        return AgentMessage(
            id = id,
            role = AgentMessage.Role.ASSISTANT,
            content = content,
            thinking = thinking
        )
    }

    @Test
    fun `upsertAssistantDraft appends when no prior draft`() = runBlocking {
        val draft = makeMessage("draft-1", "Partial output")
        store.upsertAssistantDraft(sessionId, draft)

        val session = store.get(sessionId)!!
        // Should have the user message from create() + the draft = 2 messages
        // (create() adds an empty session with no messages, so just the draft)
        val assistantMsgs = session.messages.filter { it.role == AgentMessage.Role.ASSISTANT }
        assertThat(assistantMsgs).hasSize(1)
        assertThat(assistantMsgs[0].content).isEqualTo("Partial output")
    }

    @Test
    fun `upsertAssistantDraft replaces existing draft with same id`() = runBlocking {
        val draftId = "draft-1"
        store.upsertAssistantDraft(sessionId, makeMessage(draftId, "First version"))
        store.upsertAssistantDraft(sessionId, makeMessage(draftId, "Updated version"))

        val session = store.get(sessionId)!!
        val assistantMsgs = session.messages.filter { it.role == AgentMessage.Role.ASSISTANT }
        assertThat(assistantMsgs).hasSize(1)
        assertThat(assistantMsgs[0].content).isEqualTo("Updated version")
    }

    @Test
    fun `upsertAssistantDraft keeps different draft ids as separate messages`() = runBlocking {
        store.upsertAssistantDraft(sessionId, makeMessage("draft-1", "First"))
        store.upsertAssistantDraft(sessionId, makeMessage("draft-2", "Second"))

        val session = store.get(sessionId)!!
        val assistantMsgs = session.messages.filter { it.role == AgentMessage.Role.ASSISTANT }
        assertThat(assistantMsgs).hasSize(2)
    }

    @Test
    fun `finalizeDraft replaces draft with same id`() = runBlocking {
        val draftId = "draft-1"
        store.upsertAssistantDraft(sessionId, makeMessage(draftId, "Draft content"))
        val final = makeMessage(draftId, "Final content")
        store.finalizeDraft(sessionId, final)

        val session = store.get(sessionId)!!
        val assistantMsgs = session.messages.filter { it.role == AgentMessage.Role.ASSISTANT }
        assertThat(assistantMsgs).hasSize(1)
        assertThat(assistantMsgs[0].content).isEqualTo("Final content")
    }

    @Test
    fun `finalizeDraft appends when no matching draft`() = runBlocking {
        store.upsertAssistantDraft(sessionId, makeMessage("draft-1", "Draft"))
        store.finalizeDraft(sessionId, makeMessage("final-1", "Final, no matching draft"))

        val session = store.get(sessionId)!!
        val assistantMsgs = session.messages.filter { it.role == AgentMessage.Role.ASSISTANT }
        assertThat(assistantMsgs).hasSize(2)
    }

    @Test
    fun `dropDraft removes draft with matching id`() = runBlocking {
        val draftId = "draft-1"
        store.upsertAssistantDraft(sessionId, makeMessage(draftId, "To be dropped"))
        store.dropDraft(sessionId, draftId)

        val session = store.get(sessionId)!!
        val assistantMsgs = session.messages.filter { it.role == AgentMessage.Role.ASSISTANT }
        assertThat(assistantMsgs).isEmpty()
    }

    @Test
    fun `dropDraft is no-op when no matching draft`() = runBlocking {
        store.upsertAssistantDraft(sessionId, makeMessage("draft-1", "Keep me"))
        store.dropDraft(sessionId, "nonexistent-id")

        val session = store.get(sessionId)!!
        val assistantMsgs = session.messages.filter { it.role == AgentMessage.Role.ASSISTANT }
        assertThat(assistantMsgs).hasSize(1)
    }

    @Test
    fun `PersistedApk survives session round-trip via Gson`() = runBlocking {
        val apk = PersistedApk(
            appId = 42L,
            apkName = "test.APK",
            apkPath = "/tmp/test.APK",
            sizeBytes = 1024L,
            buildMode = "FULL",
            packageName = "com.example.test",
            versionName = "1.0.0"
        )
        val session = store.get(sessionId)!!
        store.updateConfig(sessionId, session.config.copy(builtApks = listOf(apk)))

        val reloaded = store.get(sessionId)!!
        assertThat(reloaded.config.builtApksSafe).hasSize(1)
        val restored = reloaded.config.builtApksSafe[0]
        assertThat(restored.appId).isEqualTo(42L)
        assertThat(restored.apkName).isEqualTo("test.APK")
        assertThat(restored.packageName).isEqualTo("com.example.test")
    }

    @Test
    fun `builtApksSafe returns empty list for sessions without the field`() = runBlocking {
        val session = store.get(sessionId)!!
        // New session has no builtApks → builtApksSafe should be empty, not null
        assertThat(session.config.builtApksSafe).isNotNull()
        assertThat(session.config.builtApksSafe).isEmpty()
    }
}
