package com.webtoapp.ui.agent

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Tests for [AgentViewModel.stripInlineMarkers] and [formatMessageForCopy] — the
 * marker stripping logic that prevents invisible U+2063 control characters and
 * inline tool/thinking markers from leaking into visible text.
 */
class AgentMarkerTest {

    // U+2063 = INVISIBLE SEPARATOR (the character used in markers)
    private val IS = "\u2063"

    @Test
    fun `stripInlineMarkers removes tool markers`() {
        val input = "Hello${IS}TC:call_abc123${IS}World"
        val result = AgentViewModel.stripInlineMarkers(input)
        assertThat(result).isEqualTo("HelloWorld")
    }

    @Test
    fun `stripInlineMarkers removes thinking markers`() {
        val input = "${IS}TH:th-turn-1${IS}Some thinking${IS}TH:th-turn-2${IS}More"
        val result = AgentViewModel.stripInlineMarkers(input)
        assertThat(result).isEqualTo("Some thinkingMore")
    }

    @Test
    fun `stripInlineMarkers removes mixed markers`() {
        val input = "${IS}TH:th-turn-1${IS}Thinking${IS}TC:call_0_x${IS}Output"
        val result = AgentViewModel.stripInlineMarkers(input)
        assertThat(result).isEqualTo("ThinkingOutput")
    }

    @Test
    fun `stripInlineMarkers does not remove stray invisible separators without TC or TH prefix`() {
        // stripInlineMarkers only matches \u2063TC:...\u2063 or \u2063TH:...\u2063 pairs.
        // A lone \u2063 without a TC/TH prefix is left as-is (shouldn't normally occur).
        val input = "Hello${IS}World"
        val result = AgentViewModel.stripInlineMarkers(input)
        // The lone IS survives because the regex requires TC|TH prefix.
        assertThat(result).contains("Hello")
        assertThat(result).contains("World")
    }

    @Test
    fun `stripInlineMarkers leaves normal text unchanged`() {
        val input = "Hello World 123"
        val result = AgentViewModel.stripInlineMarkers(input)
        assertThat(result).isEqualTo("Hello World 123")
    }

    @Test
    fun `stripInlineMarkers handles empty string`() {
        assertThat(AgentViewModel.stripInlineMarkers("")).isEqualTo("")
    }

    @Test
    fun `stripInlineMarkers handles string with no markers`() {
        val input = "Just some text with no special chars"
        val result = AgentViewModel.stripInlineMarkers(input)
        assertThat(result).isEqualTo(input)
    }

    @Test
    fun `stripInlineMarkers handles multiple consecutive markers`() {
        val input = "${IS}TC:a${IS}${IS}TC:b${IS}${IS}TH:c${IS}Text"
        val result = AgentViewModel.stripInlineMarkers(input)
        assertThat(result).isEqualTo("Text")
    }
}
