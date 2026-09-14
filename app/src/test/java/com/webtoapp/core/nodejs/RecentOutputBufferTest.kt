package com.webtoapp.core.nodejs

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecentOutputBufferTest {

    @Test
    fun `snapshot joins lines and is empty before any add`() {
        val buffer = RecentOutputBuffer()
        assertEquals("", buffer.snapshot())

        buffer.add("first")
        buffer.add("second")
        assertEquals("first\nsecond", buffer.snapshot())
    }

    @Test
    fun `buffer drops oldest lines beyond maxLines`() {
        val buffer = RecentOutputBuffer(maxLines = 3, maxChars = 10_000)
        repeat(5) { buffer.add("line$it") }
        assertEquals("line2\nline3\nline4", buffer.snapshot())
    }

    @Test
    fun `snapshot trims from the front to maxChars at a line boundary`() {
        val buffer = RecentOutputBuffer(maxLines = 100, maxChars = 10)
        buffer.add("aaaa") // 4
        buffer.add("bbbb") // 4
        buffer.add("cc")   // 2  -> total "aaaa\nbbbb\ncc" = 12 chars
        val snapshot = buffer.snapshot()
        // cut=2 lands inside "aaaa"; trimming resumes after the first newline (index 4).
        assertEquals("…bbbb\ncc", snapshot)
    }

    @Test
    fun `contains finds bootstrap failure marker`() {
        val buffer = RecentOutputBuffer()
        buffer.add("listening soon")
        assertFalse(buffer.contains("[wta-bootstrap] entry failed"))
        buffer.add("[wta-bootstrap] entry failed: Error: Cannot find module 'x'")
        assertTrue(buffer.contains("[wta-bootstrap] entry failed"))
    }

    @Test
    fun `clear resets the buffer`() {
        val buffer = RecentOutputBuffer()
        buffer.add("noise")
        buffer.clear()
        assertEquals("", buffer.snapshot())
        assertFalse(buffer.contains("noise"))
    }
}
