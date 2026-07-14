package com.webtoapp.ui.components

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CodeEditorSearchTest {

    @Test
    fun `finds case-insensitive matches by default`() {
        val ranges = findMatchRanges("Hello hello HELLO", "hello", matchCase = false)
        assertThat(ranges).hasSize(3)
        assertThat(ranges[0].start).isEqualTo(0)
        assertThat(ranges[1].start).isEqualTo(6)
        assertThat(ranges[2].start).isEqualTo(12)
    }

    @Test
    fun `match case finds exact only`() {
        val ranges = findMatchRanges("Hello hello HELLO", "hello", matchCase = true)
        assertThat(ranges).hasSize(1)
        assertThat(ranges[0].start).isEqualTo(6)
    }

    @Test
    fun `replace all is case-insensitive when requested`() {
        val result = replaceAllMatches("Foo foo FOO", "foo", "bar", matchCase = false)
        assertThat(result).isEqualTo("bar bar bar")
    }

    @Test
    fun `replace all respects match case`() {
        val result = replaceAllMatches("Foo foo FOO", "foo", "bar", matchCase = true)
        assertThat(result).isEqualTo("Foo bar FOO")
    }
}
