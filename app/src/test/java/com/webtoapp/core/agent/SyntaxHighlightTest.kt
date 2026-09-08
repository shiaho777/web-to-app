package com.webtoapp.core.agent

import com.webtoapp.ui.agent.components.SyntaxHighlight
import com.webtoapp.ui.theme.AppColors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SyntaxHighlightTest {

    private fun colorAt(annotated: androidx.compose.ui.text.AnnotatedString, offset: Int) =
        annotated.spanStyles.firstOrNull { offset >= it.start && offset < it.end }?.item?.color

    @Test
    fun `kotlin keywords strings and comments get distinct colors`() {
        val code = """fun greet(name: String) = "hi" // friendly"""
        val annotated = SyntaxHighlight.highlight(code, "kotlin")!!

        assertEquals(AppColors.CodeKeyword, colorAt(annotated, code.indexOf("fun")))
        assertEquals(AppColors.CodeString, colorAt(annotated, code.indexOf("\"hi\"")))
        assertEquals(AppColors.CodeComment, colorAt(annotated, code.indexOf("// friendly")))
        assertEquals(AppColors.CodeFunction, colorAt(annotated, code.indexOf("greet")))
    }

    @Test
    fun `json keys and values are distinguished`() {
        val code = """{"name": "value", "n": 42}"""
        val annotated = SyntaxHighlight.highlight(code, "json")!!

        assertEquals(AppColors.CodeKeyword, colorAt(annotated, code.indexOf("\"name\"")))
        assertEquals(AppColors.CodeString, colorAt(annotated, code.indexOf("\"value\"")))
        assertEquals(AppColors.CodeNumber, colorAt(annotated, code.indexOf("42")))
    }

    @Test
    fun `unknown language returns null so caller falls back to plain text`() {
        assertNull(SyntaxHighlight.highlight("some text", "brainfuck"))
        assertNull(SyntaxHighlight.highlight("some text", null))
    }

    @Test
    fun `oversized input is skipped for performance`() {
        val big = "a".repeat(SyntaxHighlight.MAX_HIGHLIGHT_CHARS + 1)
        assertNull(SyntaxHighlight.highlight(big, "kotlin"))
    }

    @Test
    fun `multiline input keeps line structure and supports each family`() {
        for (lang in listOf("kotlin", "python", "javascript", "bash", "sql", "css", "html")) {
            assertTrue("expected support for $lang", SyntaxHighlight.supports(lang))
        }
        val code = "#!/bin/bash\necho \"hi\" # say hi"
        val annotated = SyntaxHighlight.highlight(code, "bash")!!
        assertEquals(annotated.text, code)
        assertEquals(AppColors.CodeComment, colorAt(annotated, code.lastIndexOf("#")))
    }
}
