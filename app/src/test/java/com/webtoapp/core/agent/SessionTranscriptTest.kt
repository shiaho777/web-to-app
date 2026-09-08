package com.webtoapp.core.agent

import com.webtoapp.core.agent.session.AgentMessage
import com.webtoapp.core.agent.session.AgentSession
import com.webtoapp.core.agent.session.RecordedToolCall
import com.webtoapp.core.agent.session.ThinkingSegmentData
import com.webtoapp.core.agent.session.UserAttachment
import com.webtoapp.ui.agent.components.SessionTranscript
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionTranscriptTest {

    private fun session(vararg messages: AgentMessage) = AgentSession(
        id = "s-1",
        title = "My Session",
        messages = messages.toList(),
        createdAt = 1_700_000_000_000L,
        updatedAt = 1_700_000_600_000L
    )

    @Test
    fun `transcript includes title meta and both roles`() {
        val md = SessionTranscript.render(
            session(
                AgentMessage(id = "1", role = AgentMessage.Role.USER, content = "hello", timestamp = 1L),
                AgentMessage(id = "2", role = AgentMessage.Role.ASSISTANT, content = "hi there", timestamp = 2L)
            ),
            thinkingHeader = "💭 Thinking"
        )

        assertTrue(md.startsWith("# My Session"))
        assertTrue(md.contains("- ID: `s-1`"))
        assertTrue(md.contains("## 👤 User"))
        assertTrue(md.contains("hello"))
        assertTrue(md.contains("## 🤖 Assistant"))
        assertTrue(md.contains("hi there"))
    }

    @Test
    fun `thinking segments render as quoted blocks with the given header`() {
        val md = SessionTranscript.render(
            session(
                AgentMessage(
                    id = "2", role = AgentMessage.Role.ASSISTANT, content = "answer", timestamp = 2L,
                    thinkingSegments = listOf(ThinkingSegmentData(id = "th-1", content = "let me think"))
                )
            ),
            thinkingHeader = "💭 Thinking"
        )

        assertTrue(md.contains("> **💭 Thinking**"))
        assertTrue(md.contains("> let me think"))
    }

    @Test
    fun `tool calls render as fenced blocks and running sentinel is hidden`() {
        val md = SessionTranscript.render(
            session(
                AgentMessage(
                    id = "2", role = AgentMessage.Role.ASSISTANT, content = "", timestamp = 2L,
                    toolCalls = listOf(
                        RecordedToolCall("t1", "Read", """{"path":"a.kt"}""", "file body", ok = true),
                        RecordedToolCall("t2", "Glob", "{}", RecordedToolCall.RUNNING_SENTINEL, ok = true)
                    )
                )
            ),
            thinkingHeader = "💭 Thinking"
        )

        assertTrue(md.contains("### 🔧 Read"))
        assertTrue(md.contains("```json"))
        assertTrue(md.contains("file body"))
        assertFalse(md.contains("__running__"))
    }

    @Test
    fun `inline engine markers are stripped from prose`() {
        val md = SessionTranscript.render(
            session(
                AgentMessage(id = "2", role = AgentMessage.Role.ASSISTANT, content = "before⁣TC:t1⁣after", timestamp = 2L)
            ),
            thinkingHeader = "💭 Thinking"
        )

        assertTrue(md.contains("beforeafter"))
        assertFalse(md.contains("TC:t1"))
    }

    @Test
    fun `legacy message without structured fields exports safely`() {
        // Gson-era sessions may lack attachments/segments entirely; the safe
        // accessors must make the export non-crashing by construction.
        val md = SessionTranscript.render(
            session(
                AgentMessage(
                    id = "1", role = AgentMessage.Role.USER, content = "hi", timestamp = 1L,
                    attachments = emptyList(), userAttachments = emptyList(), mentionedFiles = emptyList()
                )
            ),
            thinkingHeader = "💭 Thinking"
        )
        assertTrue(md.contains("hi"))
    }

    @Test
    fun `user attachments are listed`() {
        val md = SessionTranscript.render(
            session(
                AgentMessage(
                    id = "1", role = AgentMessage.Role.USER, content = "see this", timestamp = 1L,
                    userAttachments = listOf(UserAttachment("/tmp/x.png", "x.png", "image/png", true))
                )
            ),
            thinkingHeader = "💭 Thinking"
        )
        assertTrue(md.contains("- 📎 x.png"))
    }
}
