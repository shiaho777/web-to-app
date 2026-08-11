package com.webtoapp.core.python

import com.google.common.truth.Truth.assertThat
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.StandardCharsets
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ElfInterpPatcherTest {

    @Rule @JvmField
    val tmp = TemporaryFolder()

    private val ABSOLUTE = "/lib/ld-musl-aarch64.so.1"
    private val RELATIVE = "\$ORIGIN/../lib/ld-musl-aarch64.so.1"

    @Test
    fun `currentInterp reads the interp string`() {
        val file = writeElf(ABSOLUTE)
        assertThat(ElfInterpPatcher.currentInterp(file)).isEqualTo(ABSOLUTE)
    }

    @Test
    fun `currentInterp recovers the full string when p_filesz truncates before the NUL`() {
        // The real python-build-standalone builds declare p_filesz=26 for the
        // 27-char path; the kernel would see a truncated string, but our reader
        // must detect the real interpreter to patch it.
        val file = writeElf(ABSOLUTE, pFilesz = 5)
        assertThat(ElfInterpPatcher.currentInterp(file)).isEqualTo(ABSOLUTE)
    }

    @Test
    fun `currentInterp returns null for non-ELF files`() {
        val file = tmp.newFile("plain.txt")
        file.writeText("hello")
        assertThat(ElfInterpPatcher.currentInterp(file)).isNull()
    }

    @Test
    fun `rewriteInterp writes in place when the new path fits`() {
        val file = writeElf(ABSOLUTE, pFilesz = 64)
        assertThat(ElfInterpPatcher.rewriteInterp(file, RELATIVE)).isTrue()
        assertThat(ElfInterpPatcher.currentInterp(file)).isEqualTo(RELATIVE)
    }

    @Test
    fun `rewriteInterp grows the segment when the new path is longer`() {
        val file = writeElf(ABSOLUTE) // p_filesz = 28
        assertThat(ElfInterpPatcher.rewriteInterp(file, RELATIVE)).isTrue()
        assertThat(ElfInterpPatcher.currentInterp(file)).isEqualTo(RELATIVE)
    }

    @Test
    fun `patchMuslInterp rewrites absolute musl interp and is idempotent`() {
        val file = writeElf(ABSOLUTE)
        assertThat(ElfInterpPatcher.patchMuslInterp(file, "ld-musl-aarch64.so.1")).isTrue()
        assertThat(ElfInterpPatcher.currentInterp(file)).isEqualTo(RELATIVE)
        // Second pass: already relative, nothing to do.
        assertThat(ElfInterpPatcher.patchMuslInterp(file, "ld-musl-aarch64.so.1")).isFalse()
        assertThat(ElfInterpPatcher.currentInterp(file)).isEqualTo(RELATIVE)
    }

    @Test
    fun `patchMuslInterp leaves non-musl interps alone`() {
        val file = writeElf("/lib/ld-linux-aarch64.so.1")
        assertThat(ElfInterpPatcher.patchMuslInterp(file, "ld-musl-aarch64.so.1")).isFalse()
        assertThat(ElfInterpPatcher.currentInterp(file)).isEqualTo("/lib/ld-linux-aarch64.so.1")
    }

    @Test
    fun `relocatableInterp builds an origin-relative path`() {
        assertThat(ElfInterpPatcher.relocatableInterp("ld-musl-aarch64.so.1"))
            .isEqualTo("\$ORIGIN/../lib/ld-musl-aarch64.so.1")
    }

    @Test
    fun `patchPythonBinaries scans the bin dir and reports the patched count`() {
        val pythonHome = tmp.newFolder("python")
        val binDir = File(pythonHome, "bin").apply { mkdirs() }
        val pythonBin = File(binDir, "python3.14").apply { writeBytes(ByteArray(1024 * 1024 + 1) { 0 }) }
        val other = File(binDir, "python3.14-config").apply { writeText("#!/bin/sh\necho hi") }

        // Overwrite the padding with a real ELF so patching applies. Real
        // binaries are tens of MB, so pad to clear the >1MB binary filter.
        pythonBin.writeBytes(buildElfBytes(ABSOLUTE) + ByteArray(1024 * 1024))

        val patched = ElfInterpPatcher.patchPythonBinaries(pythonHome, "ld-musl-aarch64.so.1")
        assertThat(patched).isEqualTo(1)
        assertThat(ElfInterpPatcher.currentInterp(pythonBin)).isEqualTo(RELATIVE)
        assertThat(other.readText()).contains("#!/bin/sh")
    }

    private fun writeElf(interp: String, pFilesz: Int = -1): File {
        val file = tmp.newFile("python-${System.nanoTime()}")
        file.writeBytes(buildElfBytes(interp, pFilesz))
        return file
    }

    private fun buildElfBytes(interp: String, pFilesz: Int = -1): ByteArray {
        val interpBytes = interp.toByteArray(StandardCharsets.UTF_8) + 0
        val filesz = if (pFilesz < 0) interpBytes.size else pFilesz

        val ehdr = ByteArray(64)
        ehdr[0] = 0x7F.toByte()
        ehdr[1] = 'E'.code.toByte()
        ehdr[2] = 'L'.code.toByte()
        ehdr[3] = 'F'.code.toByte()
        ehdr[4] = 2 // ELFCLASS64
        ehdr[5] = 1 // ELFDATA2LSB
        putU16(ehdr, 16, 2) // e_type ET_EXEC
        putU16(ehdr, 18, 183) // e_machine AArch64
        putU32(ehdr, 20, 1) // e_version
        putU64(ehdr, 24, 0x400000L) // e_entry
        putU64(ehdr, 32, 64) // e_phoff
        putU64(ehdr, 40, 0) // e_shoff
        putU32(ehdr, 48, 0) // e_flags
        putU16(ehdr, 52, 64) // e_ehsize
        putU16(ehdr, 54, 56) // e_phentsize
        putU16(ehdr, 56, 1) // e_phnum

        val phdr = ByteArray(56)
        putU32(phdr, 0, 3) // PT_INTERP
        putU32(phdr, 4, 4) // p_flags PF_R
        putU64(phdr, 8, 120) // p_offset (after header + phdr)
        putU64(phdr, 16, 0x400000L) // p_vaddr
        putU64(phdr, 24, 0x400000L) // p_paddr
        putU64(phdr, 32, filesz.toLong()) // p_filesz
        putU64(phdr, 40, filesz.toLong()) // p_memsz
        putU64(phdr, 48, 1) // p_align

        val out = ByteArrayOutputStream()
        out.write(ehdr)
        out.write(phdr)
        out.write(interpBytes)
        if (filesz > interpBytes.size) {
            out.write(ByteArray(filesz - interpBytes.size))
        }
        return out.toByteArray()
    }

    private fun putU16(bytes: ByteArray, offset: Int, value: Int) {
        bytes[offset] = (value and 0xFF).toByte()
        bytes[offset + 1] = ((value shr 8) and 0xFF).toByte()
    }

    private fun putU32(bytes: ByteArray, offset: Int, value: Int) {
        putU16(bytes, offset, value and 0xFFFF)
        putU16(bytes, offset + 2, (value shr 16) and 0xFFFF)
    }

    private fun putU64(bytes: ByteArray, offset: Int, value: Long) {
        putU32(bytes, offset, value.toInt())
        putU32(bytes, offset + 4, (value shr 32).toInt())
    }
}
