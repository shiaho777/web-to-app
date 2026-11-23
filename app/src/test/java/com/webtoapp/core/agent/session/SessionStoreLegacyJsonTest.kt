package com.webtoapp.core.agent.session

import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.webtoapp.core.agent.files.ProjectFileManager
import com.webtoapp.util.GsonProvider
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SessionStoreLegacyJsonTest {

    private lateinit var store: SessionStore
    private lateinit var files: ProjectFileManager

    @Before
    fun setUp() = runBlocking {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        files = ProjectFileManager(ctx)
        store = SessionStore(ctx, files)
    }

    private fun legacyJson(): String =
        javaClass.getResourceAsStream("/legacy_agent_sessions_v1.json")!!
            .readBytes().decodeToString()

    @Test
    fun `decode of legacy session JSON does not crash and normalises missing lists`() = runBlocking {
        val sessions = store.decodeForTest(legacyJson())

        assertThat(sessions).hasSize(5)
        sessions.forEach { session ->
            session.config.builtApks
            session.config.contextAppIds
            session.config.contextModuleIds
            session.config.customRules
            session.messages.forEach { message ->
                message.userAttachments
                message.thinkingSegments
                message.toolCalls
                message.producedFiles
                message.attachments
                message.mentionedFiles
            }
        }
    }

    @Test
    fun `legacy messages expose empty userAttachments via safe accessor`() = runBlocking {
        val sessions = store.decodeForTest(legacyJson())
        val messages = sessions.flatMap { it.messages }
        assertThat(messages).isNotEmpty()
        messages.forEach { message ->
            assertThat(message.userAttachmentsSafe).isEmpty()
            assertThat(message.thinkingSegmentsSafe).isEmpty()
        }
    }

    @Test
    fun `legacy config exposes empty context and built-apk lists via safe accessors`() = runBlocking {
        val sessions = store.decodeForTest(legacyJson())
        sessions.forEach { session ->
            assertThat(session.config.contextAppIdsSafe).isEmpty()
            assertThat(session.config.contextModuleIdsSafe).isEmpty()
            assertThat(session.config.skillIdsSafe).isEmpty()
            assertThat(session.config.builtApksSafe).isEmpty()
        }
    }

    @Test
    fun `compaction preserves messages added after the snapshot`(): Unit = runBlocking {
        val session = store.create()
        val initial = AgentMessage(role = AgentMessage.Role.USER, content = "Initial request")
        val newer = AgentMessage(role = AgentMessage.Role.USER, content = "Newer request")
        val summary = AgentMessage(role = AgentMessage.Role.USER, content = "Summary")
        store.appendMessage(session.id, initial)
        val snapshot = store.get(session.id)!!
        store.appendMessage(session.id, newer)

        assertThat(store.replaceMessages(session.id, listOf(summary), snapshot.messages)).isNull()
        val latest = store.get(session.id)!!
        assertThat(latest.messages).containsExactly(initial, newer).inOrder()
        assertThat(store.replaceMessages(session.id, listOf(summary), latest.messages)?.messages)
            .containsExactly(summary)
    }

    @Test
    fun `decoded legacy blob re-serialises with normalised lists and decodes again`() = runBlocking {
        val sessions = store.decodeForTest(legacyJson())
        val raw = GsonProvider.gson.toJson(sessions)
        val again = store.decodeForTest(raw)
        assertThat(again).hasSize(sessions.size)
        again.flatMap { it.messages }.forEach { message ->
            assertThat(message.userAttachmentsSafe).isEmpty()
        }
    }
}
