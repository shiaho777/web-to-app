package com.webtoapp.core.golang

import com.google.common.truth.Truth.assertThat
import java.io.File
import java.nio.file.Files
import org.junit.Test

class GoArchiveTest {

    @Test
    fun `appendObjects matches the go pack byte layout`() {
        val dir = Files.createTempDirectory("wta-artest").toFile()
        try {
            val archive = File(dir, "_pkg_.a")
            archive.writeBytes("!<arch>\n".toByteArray(Charsets.US_ASCII))
            val member = File(dir, "compare_arm64.o")
            val body = ByteArray(2673) { (it and 0xFF).toByte() }
            member.writeBytes(body)

            assertThat(GoArchive.appendObjects(archive, listOf(member))).isTrue()

            val out = archive.readBytes()
            // magic + 60B header + 2673 body + 1 pad byte
            assertThat(out.size).isEqualTo(8 + 60 + 2673 + 1)
            assertThat(out.sliceArray(0 until 8).decodeToString()).isEqualTo("!<arch>\n")
            val header = out.sliceArray(8 until 68).decodeToString()
            // bare space-padded name (15 chars + 1 space), zero mtime/uid/gid,
            // octal 644 mode, decimal size, backtick-LF magic.
            assertThat(header).isEqualTo(
                "compare_arm64.o 0           0     0     644     2673      `\n"
            )
            assertThat(out.sliceArray(68 until 68 + 2673)).isEqualTo(body)
            assertThat(out[68 + 2673]).isEqualTo('\n'.code.toByte())
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    fun `appendObjects truncates long names to 16 bytes`() {
        val dir = Files.createTempDirectory("wta-artest").toFile()
        try {
            val archive = File(dir, "_pkg_.a")
            archive.writeBytes("!<arch>\n".toByteArray(Charsets.US_ASCII))
            // Real toolchain output for this exact member reads
            // `indexbyte_arm64.` (first 16 bytes of `indexbyte_arm64.o/`).
            val member = File(dir, "indexbyte_arm64.o")
            member.writeBytes(ByteArray(100))
            assertThat(GoArchive.appendObjects(archive, listOf(member))).isTrue()
            val header = archive.readBytes().sliceArray(8 until 68).decodeToString()
            assertThat(header.substring(0, 16)).isEqualTo("indexbyte_arm64.")
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    fun `appendObjects rejects bad magic`() {
        val dir = Files.createTempDirectory("wta-artest").toFile()
        try {
            val archive = File(dir, "_pkg_.a")
            archive.writeBytes("garbage!".toByteArray(Charsets.US_ASCII))
            val member = File(dir, "a.o").also { it.writeBytes(byteArrayOf(1, 2, 3)) }
            assertThat(GoArchive.appendObjects(archive, listOf(member))).isFalse()
        } finally {
            dir.deleteRecursively()
        }
    }
}
