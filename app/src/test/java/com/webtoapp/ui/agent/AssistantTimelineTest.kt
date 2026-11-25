package com.webtoapp.ui.agent

import com.google.common.truth.Truth.assertThat
import com.webtoapp.core.agent.session.RecordedToolCall
import com.webtoapp.ui.agent.components.AssistantTimelinePart
import com.webtoapp.ui.agent.components.buildAssistantTimeline
import com.webtoapp.ui.agent.components.compactToolActivity
import org.junit.Test

class AssistantTimelineTest {
    @Test
    fun `only adjoining calls share a group and marker order wins`() {
        val read = call("read")
        val write = call("write")
        val submit = call("submit")
        val thought = ThinkingSegment("idea", "Plan", 0)
        val text = "${toolMarker("read")}${toolMarker("write")}\n已准备好。\n\u2063TH:idea\u2063${toolMarker("submit")}"

        assertThat(buildAssistantTimeline(text, listOf(write, read, submit), listOf(thought)))
            .containsExactly(
                AssistantTimelinePart.Tools(listOf(read, write)),
                AssistantTimelinePart.Prose("\n已准备好。"),
                AssistantTimelinePart.Thinking(thought),
                AssistantTimelinePart.Tools(listOf(submit))
            ).inOrder()
    }

    @Test
    fun `legacy tool-only replies render without frozen placeholder copy`() {
        val call = call("write")
        val parts = buildAssistantTimeline("(出力なし)", listOf(call), emptyList())

        assertThat(parts).containsExactly(AssistantTimelinePart.Tools(listOf(call)))
        assertThat(buildAssistantTimeline("(无输出)", emptyList(), emptyList()))
            .containsExactly(AssistantTimelinePart.Prose("(无输出)"))
    }

    @Test
    fun `duplicate markers display once and unmarked calls remain visible`() {
        val first = call("first")
        val second = call("second")
        val text = toolMarker("first") + toolMarker("first") + toolMarker("missing")

        assertThat(buildAssistantTimeline(text, listOf(first, second), emptyList()))
            .containsExactly(AssistantTimelinePart.Tools(listOf(first, second)))
    }

    @Test
    fun `compact groups retain the open row, active work, and failure in execution order`() {
        val failure = call("failure").copy(status = RecordedToolCall.Status.Failed)
        val running = call("running").copy(status = RecordedToolCall.Status.Running)
        val expanded = call("expanded")
        val latest = call("latest")
        val calls = listOf(failure, call("older"), expanded, running, latest)

        assertThat(compactToolActivity(calls, expanded.toolCallId, live = true))
            .containsExactly(failure, expanded, running).inOrder()
        assertThat(compactToolActivity(calls, expanded.toolCallId, live = false))
            .containsExactly(failure, expanded, latest).inOrder()
    }

    private fun toolMarker(id: String) = "\u2063TC:${id}\u2063"

    private fun call(id: String) = RecordedToolCall(id, "Write", "{}", "", true)
}
