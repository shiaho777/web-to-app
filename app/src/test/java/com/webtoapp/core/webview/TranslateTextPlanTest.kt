package com.webtoapp.core.webview

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Long pages (an AO3 chapter is the report in #1315) used to be joined into one
 * GET and split back on newlines. These checks pin the chunking that replaced that.
 */
class TranslateTextPlanTest {

    @Test
    fun `a chapter-length paragraph is cut into pieces that fit the Google budget`() {
        val paragraph = "The rain kept on. ".repeat(200)
        val pieces = TranslateTextPlan.piecesFor(listOf(paragraph), TranslateTextPlan.GOOGLE)

        assertThat(pieces.size).isGreaterThan(1)
        pieces.forEach { piece ->
            assertThat(piece.textIndex).isEqualTo(0)
            assertThat(TranslateTextPlan.within(piece.text, TranslateTextPlan.GOOGLE)).isTrue()
            assertThat(piece.text).doesNotContain("\n")
        }
        val batches = TranslateTextPlan.batches(pieces, TranslateTextPlan.GOOGLE)
        batches.forEach { batch ->
            val joined = TranslateTextPlan.join(pieces, batch)
            assertThat(TranslateTextPlan.within(joined, TranslateTextPlan.GOOGLE)).isTrue()
        }
    }

    @Test
    fun `newlines stay attached to their own paragraph`() {
        val texts = listOf("Alpha line.\n\nBeta line.")
        val pieces = TranslateTextPlan.piecesFor(texts, TranslateTextPlan.GOOGLE)
        val translated = pieces.map { piece ->
            if (piece.passthrough) "" else piece.text.uppercase()
        }

        assertThat(TranslateTextPlan.reassemble(texts.size, pieces, translated))
            .containsExactly("ALPHA LINE.\n\nBETA LINE.")
    }

    @Test
    fun `a dropped newline is rejected instead of shifting later paragraphs`() {
        assertThat(TranslateTextPlan.splitTranslated("one\ntwo", 2)).containsExactly("one", "two").inOrder()
        assertThat(TranslateTextPlan.splitTranslated("one two", 2)).isNull()
        assertThat(TranslateTextPlan.splitTranslated("only\nextra", 1)).containsExactly("only\nextra")
    }

    @Test
    fun `short strings share a request until the budget is full`() {
        val texts = List(10) { "Label $it" }
        val pieces = TranslateTextPlan.piecesFor(texts, TranslateTextPlan.MYMEMORY)
        val batches = TranslateTextPlan.batches(pieces, TranslateTextPlan.MYMEMORY)

        assertThat(batches).hasSize(1)
        assertThat(TranslateTextPlan.reassemble(
            texts.size,
            pieces,
            pieces.map { it.text }
        )).containsExactlyElementsIn(texts).inOrder()
    }

    @Test
    fun `proxy route publishes and clears an http endpoint`() {
        TranslateProxyRoute.clear()
        TranslateProxyRoute.useHttp("127.0.0.1", 18080)
        assertThat(TranslateProxyRoute.endpoint.kind).isEqualTo(TranslateProxyRoute.Kind.HTTP)
        assertThat(TranslateProxyRoute.endpoint.port).isEqualTo(18080)

        TranslateProxyRoute.useHttp("", 0)
        assertThat(TranslateProxyRoute.endpoint.kind).isEqualTo(TranslateProxyRoute.Kind.DIRECT)
    }
}
