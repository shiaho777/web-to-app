package com.webtoapp.ui.agent

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AgentFileMentionsTest {
    @Test
    fun replacesTheTokenAtTheCaretAndPreservesTheRestOfTheDraft() {
        val draft = "check @indeZZ before @other.txt"
        val query = AgentFileMentions.atCaret(draft, "check @inde".length)!!

        assertThat(query.value).isEqualTo("inde")
        val edited = AgentFileMentions.insert(draft, query, "site/index.html")
        assertThat(edited.text).isEqualTo("check @site/index.html before @other.txt")
        assertThat(edited.caret).isEqualTo("check @site/index.html ".length)
    }

    @Test
    fun serializesUnicodeAndQuotedFilenamesWithoutLosingPaths() {
        val draft = "检查 @"
        val query = AgentFileMentions.atCaret(draft, draft.length)!!
        val quoted = "页面/\"测试 页面\".html"
        val text = AgentFileMentions.insert(draft, query, quoted).text + "@页面/首页.md"
        val files = setOf(quoted, "页面/首页.md")

        assertThat(AgentFileMentions.paths(text, files::contains))
            .containsExactly(quoted, "页面/首页.md").inOrder()
    }

    @Test
    fun replacesACompleteQuotedTokenFromInsideTheQuote() {
        val draft = "review @\"old page.html\" this week"
        val query = AgentFileMentions.atCaret(draft, "review @\"old".length)!!

        assertThat(query.value).isEqualTo("old")
        assertThat(AgentFileMentions.insert(draft, query, "new page.html").text)
            .isEqualTo("review @\"new page.html\" this week")
    }

    @Test
    fun acceptsPunctuationInProseAndKeepsExactFilenamesFirst() {
        val files = setOf("file.txt", "with-dot.")
        assertThat(AgentFileMentions.paths("@file.txt, @with-dot. @file.txt @missing", files::contains))
            .containsExactly("file.txt", "with-dot.").inOrder()
        assertThat(AgentFileMentions.atCaret("dev@example.com", 15)).isNull()
        assertThat(AgentFileMentions.paths("@\"unclosed file", setOf("unclosed file")::contains)).isEmpty()
    }
}
