package com.webtoapp.core.agent.session

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * The attachment note is the contract that keeps uploads as path references:
 * it must name paths + sizes while never carrying file content into the prompt.
 */
class AttachmentNoteTest {

    @Test
    fun `build renders file and folder references without content`() {
        val atts = listOf(
            UserAttachment(
                path = "uploads/big.zip",
                displayName = "big.zip",
                mimeType = "application/zip",
                isImage = false,
                sizeBytes = 1L shl 30
            ),
            UserAttachment(
                path = "uploads/site/",
                displayName = "site/",
                mimeType = "",
                isImage = false,
                sizeBytes = 18L * 1024 * 1024,
                entryCount = 47
            )
        )

        val note = AttachmentNote.build(atts) { null }

        assertThat(note).contains("uploads/big.zip")
        assertThat(note).contains("1.0GB")
        assertThat(note).contains("application/zip")
        assertThat(note).contains("uploads/site/")
        assertThat(note).contains("folder")
        assertThat(note).contains("47 files")
        assertThat(note).contains("18MB")
        assertThat(note).contains("NOT inlined")
    }

    @Test
    fun `build falls back to live stat for legacy attachments without size`() {
        val legacy = UserAttachment(
            path = "uploads/old.bin",
            displayName = "old.bin",
            mimeType = "application/octet-stream",
            isImage = false
        )
        val fake = java.io.File.createTempFile("att", ".bin").apply {
            writeBytes(ByteArray(2048))
            deleteOnExit()
        }

        val note = AttachmentNote.build(listOf(legacy)) { fake }

        assertThat(note).contains("2KB")
    }

    @Test
    fun `empty attachment list produces no note`() {
        assertThat(AttachmentNote.build(emptyList()) { null }).isEmpty()
    }
}
