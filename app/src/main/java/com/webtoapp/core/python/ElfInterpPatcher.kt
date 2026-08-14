package com.webtoapp.core.python

import com.webtoapp.core.logging.AppLogger
import java.io.File
import java.io.RandomAccessFile
import java.nio.charset.StandardCharsets

/**
 * Rewrites the PT_INTERP (ELF program interpreter) of executables so direct
 * kernel execs resolve the on-device musl dynamic linker.
 *
 * The python-build-standalone musl builds ship with an absolute interpreter
 * path such as `/lib/ld-musl-aarch64.so.1`, which does not exist on Android.
 * Launching through the explicit loader (`ld-musl-* --library-path ... python3`)
 * works, but **any subprocess spawned by Python itself** (pip's PEP 517
 * build-isolation venv pythons) is exec'd directly by the kernel, which
 * resolves PT_INTERP verbatim and fails with `ENOENT`.
 *
 * The interpreter is therefore patched to the **absolute path of the bundled
 * loader on this device** (`<pythonHome>/lib/ld-musl-<arch>.so.1`). An
 * absolute path is required rather than an `$ORIGIN`-relative one because:
 *
 *  - the kernel does not expand `$ORIGIN` in PT_INTERP (it would be resolved
 *    as a literal relative path against the process CWD), and
 *  - `packaging`'s musl version probe reads PT_INTERP of `sys.executable` and
 *    executes that string verbatim (`subprocess.run([ld])`) to compute the
 *    `musllinux_*` platform tags — an absolute, existing path makes the probe
 *    succeed so compiled wheels match without any source build.
 *
 * The baked path lives under the app's `filesDir`, which is stable across app
 * updates (unlike `nativeLibraryDir`, which is randomized per install and
 * must never be embedded).
 */
object ElfInterpPatcher {

    private const val TAG = "ElfInterpPatcher"

    private const val EI_MAGIC0 = 0
    private const val EI_MAGIC1 = 1
    private const val EI_MAGIC2 = 2
    private const val EI_MAGIC3 = 3
    private const val EI_CLASS = 4
    private const val EI_DATA = 5

    private const val ELFCLASS32 = 1
    private const val ELFCLASS64 = 2
    private const val ELFDATA2LSB = 1

    private const val PT_INTERP = 3

    private const val SIZEOF_EHDR64 = 64
    private const val SIZEOF_PHDR64 = 56

    private const val SIZEOF_EHDR32 = 52
    private const val SIZEOF_PHDR32 = 32

    private const val ORIGIN_PREFIX = "\$ORIGIN/"

    /**
     * True when [interp] points at a musl dynamic loader that this device
     * cannot resolve verbatim: the stock absolute `/lib/ld-musl-<arch>.so.1`,
     * the `$ORIGIN`-relative form written by the 2.4.5 patcher (the kernel
     * does not expand `$ORIGIN` in PT_INTERP), or a stale absolute path baked
     * by an earlier heal after the runtime moved.
     */
    fun isUnresolvedMuslInterp(interp: String, resolvedInterp: String): Boolean {
        if (interp == resolvedInterp) return false
        if (!interp.startsWith("/") && !interp.startsWith(ORIGIN_PREFIX)) return false
        val base = interp.substringAfterLast('/')
        return base.startsWith("ld-musl-") && base.endsWith(".so.1")
    }

    /**
     * Reads the current PT_INTERP of [file], or null when the file is not a
     * supported little-endian ELF with an interpreter.
     */
    fun currentInterp(file: File): String? {
        if (!file.isFile || file.length() < SIZEOF_EHDR64) return null
        return try {
            RandomAccessFile(file, "r").use { raf ->
                val ident = ByteArray(16)
                raf.seek(0)
                raf.readFully(ident)
                if (!isElf(ident)) return null
                if (ident[EI_CLASS].toInt() != ELFCLASS64 && ident[EI_CLASS].toInt() != ELFCLASS32) return null
                if (ident[EI_DATA].toInt() != ELFDATA2LSB) return null

                val is64 = ident[EI_CLASS] == ELFCLASS64.toByte()
                val ehdrSize = if (is64) SIZEOF_EHDR64 else SIZEOF_EHDR32
                val header = ByteArray(ehdrSize)
                raf.seek(0)
                raf.readFully(header)
                val phOff = if (is64) readU32(header, 32).toLong() else readU32(header, 28).toLong()
                val phEntSize = if (is64) readU16(header, 54) else readU16(header, 42)
                val phNum = if (is64) readU16(header, 56) else readU16(header, 44)
                if (phEntSize < (if (is64) SIZEOF_PHDR64 else SIZEOF_PHDR32)) return null

                for (i in 0 until phNum) {
                    val ph = ByteArray(phEntSize)
                    raf.seek(phOff + i * phEntSize.toLong())
                    raf.readFully(ph)
                    if (readU32(ph, 0) != PT_INTERP) continue
                    val pOffset = if (is64) readU64(ph, 8) else readU32(ph, 4).toLong()
                    val pFilesz = if (is64) readU64(ph, 32) else readU32(ph, 16).toLong()
                    if (pFilesz <= 0 || pFilesz > 4096) return null
                    // Some builds truncate p_filesz before the NUL terminator
                    // (e.g. 26 bytes for a 27-char path). Peek past the segment
                    // so we detect the real interpreter; the region is file-only
                    // padding, never mapped.
                    val readLen = minOf(pFilesz + 64, file.length() - pOffset).toInt()
                    if (readLen <= 0) return null
                    raf.seek(pOffset)
                    val bytes = ByteArray(readLen)
                    raf.readFully(bytes)
                    val end = bytes.indexOf(0).let { if (it < 0) bytes.size else it }
                    return String(bytes, 0, end, StandardCharsets.UTF_8)
                }
                null
            }
        } catch (e: Exception) {
            AppLogger.d(TAG, "Failed to read PT_INTERP of ${file.absolutePath}: ${e.message}")
            null
        }
    }

    /**
     * Rewrites the PT_INTERP of [file] to [newInterp]. Grows the segment in
     * place when the new path does not fit, which is safe because the kernel
     * only reads the interpreter string from the segment and it lives inside
     * the ELF header page (load segments start at the next page boundary).
     *
     * @return true when the file was modified.
     */
    fun rewriteInterp(file: File, newInterp: String): Boolean {
        val newBytes = newInterp.toByteArray(StandardCharsets.UTF_8) + 0
        return try {
            RandomAccessFile(file, "rw").use { raf ->
                val ident = ByteArray(16)
                raf.seek(0)
                raf.readFully(ident)
                if (!isElf(ident)) return false
                if (ident[EI_CLASS].toInt() != ELFCLASS64 && ident[EI_CLASS].toInt() != ELFCLASS32) return false
                if (ident[EI_DATA].toInt() != ELFDATA2LSB) return false

                val is64 = ident[EI_CLASS] == ELFCLASS64.toByte()
                val ehdrSize = if (is64) SIZEOF_EHDR64 else SIZEOF_EHDR32
                val header = ByteArray(ehdrSize)
                raf.seek(0)
                raf.readFully(header)
                val phOff = if (is64) readU32(header, 32).toLong() else readU32(header, 28).toLong()
                val phEntSize = if (is64) readU16(header, 54) else readU16(header, 42)
                val phNum = if (is64) readU16(header, 56) else readU16(header, 44)
                if (phEntSize < (if (is64) SIZEOF_PHDR64 else SIZEOF_PHDR32)) return false

                for (i in 0 until phNum) {
                    val ph = ByteArray(phEntSize)
                    raf.seek(phOff + i * phEntSize.toLong())
                    raf.readFully(ph)
                    if (readU32(ph, 0) != PT_INTERP) continue
                    val pOffset = if (is64) readU64(ph, 8) else readU32(ph, 4).toLong()
                    val pFilesz = if (is64) readU64(ph, 32) else readU32(ph, 16).toLong()
                    if (pOffset <= 0 || pFilesz <= 0 || pFilesz > 4096) return false

                    if (newBytes.size <= pFilesz) {
                        raf.seek(pOffset)
                        raf.write(newBytes)
                        val padding = ByteArray((pFilesz - newBytes.size).toInt())
                        raf.write(padding)
                        AppLogger.i(TAG, "Rewrote PT_INTERP in place (${file.name})")
                        return true
                    }

                    // Grow in place. The interpreter lives in the first page with
                    // the ELF header; the next load segment starts at the next
                    // page boundary, so there is slack as long as the new path
                    // stays within this page.
                    val pageStart = pOffset - (pOffset % 4096)
                    if (pOffset + newBytes.size > pageStart + 4096) {
                        AppLogger.w(TAG, "New interpreter does not fit in header page of ${file.name}; skipping")
                        return false
                    }
                    raf.seek(pOffset)
                    raf.write(newBytes)
                    raf.seek(phOff + i * phEntSize.toLong() + if (is64) 32 else 16)
                    if (is64) {
                        writeU64(raf, newBytes.size.toLong())
                    } else {
                        writeU32(raf, newBytes.size)
                    }
                    AppLogger.i(TAG, "Rewrote PT_INTERP (grew segment to ${newBytes.size} bytes, ${file.name})")
                    return true
                }
                false
            }
        } catch (e: Exception) {
            AppLogger.w(TAG, "Failed to rewrite PT_INTERP of ${file.absolutePath}: ${e.message}")
            false
        }
    }

    /**
     * Patches [file] when its interpreter is a musl loader path this device
     * cannot resolve verbatim.
     *
     * @param resolvedInterp absolute on-device loader path to bake in (see
     * [PythonDependencyManager.resolvedMuslInterpPath]).
     * @return true when the file was modified.
     */
    fun patchMuslInterp(file: File, resolvedInterp: String): Boolean {
        val current = currentInterp(file) ?: return false
        if (!isUnresolvedMuslInterp(current, resolvedInterp)) return false
        return rewriteInterp(file, resolvedInterp)
    }

    /**
     * Patches every ELF executable under `<pythonHome>/bin/` whose musl
     * interpreter cannot be resolved on this device.
     *
     * @return number of binaries patched.
     */
    fun patchPythonBinaries(pythonHome: File, resolvedInterp: String): Int {
        val binDir = File(pythonHome, "bin")
        if (!binDir.isDirectory) return 0
        var patched = 0
        binDir.listFiles()?.forEach { file ->
            if (file.isFile && file.length() > 1024 * 1024) {
                if (patchMuslInterp(file, resolvedInterp)) patched++
            }
        }
        return patched
    }

    private fun isElf(ident: ByteArray): Boolean =
        ident.size >= 16 &&
            ident[EI_MAGIC0] == 0x7F.toByte() &&
            ident[EI_MAGIC1] == 'E'.code.toByte() &&
            ident[EI_MAGIC2] == 'L'.code.toByte() &&
            ident[EI_MAGIC3] == 'F'.code.toByte()

    private fun readU16(bytes: ByteArray, offset: Int): Int =
        (bytes[offset].toInt() and 0xFF) or ((bytes[offset + 1].toInt() and 0xFF) shl 8)

    private fun readU32(bytes: ByteArray, offset: Int): Int =
        readU16(bytes, offset) or (readU16(bytes, offset + 2) shl 16)

    private fun readU64(bytes: ByteArray, offset: Int): Long =
        (readU32(bytes, offset).toLong() and 0xFFFFFFFFL) or
            ((readU32(bytes, offset + 4).toLong() and 0xFFFFFFFFL) shl 32)

    private fun writeU32(raf: RandomAccessFile, value: Int) {
        raf.write(value and 0xFF)
        raf.write((value shr 8) and 0xFF)
        raf.write((value shr 16) and 0xFF)
        raf.write((value shr 24) and 0xFF)
    }

    private fun writeU64(raf: RandomAccessFile, value: Long) {
        writeU32(raf, value.toInt())
        writeU32(raf, (value shr 32).toInt())
    }
}
