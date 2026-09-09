package com.webtoapp.util

import com.webtoapp.core.logging.AppLogger
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * Shared Zip-Slip guard for archive extraction. Every place that unpacks a downloaded or
 * user-picked archive must resolve entry names through [safeChild] — raw
 * `File(destDir, entry.name)` lets a crafted `../../x` entry write outside the intended
 * directory (mirrors the lexical + canonical rules of DataBackupManager.resolveSafeChild).
 * Plain unpack loops should use [extractAll] (safeChild + zip-bomb caps in one place);
 * sites with custom per-entry handling wrap their loop in [EntryGuard] instead.
 */
object SafeZip {

    private const val TAG = "SafeZip"

    const val DEFAULT_MAX_ENTRIES = 20_000
    const val DEFAULT_MAX_TOTAL_BYTES = 2L * 1024 * 1024 * 1024

    /** Zip-bomb abort: entry-count or total-extracted-size cap exceeded. */
    class ZipBombException(kind: Kind, message: String) : IOException(message) {
        enum class Kind { ENTRY_COUNT, TOTAL_SIZE }
        val kind: Kind = kind
    }

    /**
     * Entry/size counting guard for archive extraction loops. Sites that extract
     * selectively (matched names only) or stage entries elsewhere wrap their loop with
     * this instead of using [extractAll]; the caps are the shared defense against
     * crafted archives that expand far beyond the plausible input size.
     */
    class EntryGuard(
        private val maxEntries: Int = DEFAULT_MAX_ENTRIES,
        private val maxTotalBytes: Long = DEFAULT_MAX_TOTAL_BYTES
    ) {
        var totalBytes = 0L
            private set

        fun onEntry() {
            entryCount++
            if (entryCount > maxEntries) {
                throw ZipBombException(ZipBombException.Kind.ENTRY_COUNT, "Archive entry count exceeds $maxEntries")
            }
        }

        fun onBytes(count: Int) {
            if (count <= 0) return
            totalBytes += count
            if (totalBytes > maxTotalBytes) {
                throw ZipBombException(
                    ZipBombException.Kind.TOTAL_SIZE,
                    "Archive extracted size exceeds ${maxTotalBytes / 1024 / 1024} MB"
                )
            }
        }

        /** Counting copy — replaces raw [InputStream.copyTo] inside extraction loops. */
        fun copyTo(input: InputStream, out: OutputStream) {
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                onBytes(read)
                out.write(buffer, 0, read)
            }
        }

        private var entryCount = 0
    }

    /**
     * Resolve [entryName] under [baseDir], null when the entry tries to escape it.
     * Absolute paths and `..` segments are rejected lexically; the result is additionally
     * verified against the canonical base so symlinked/suffixed variants fail closed.
     */
    fun safeChild(baseDir: File, entryName: String): File? {
        if (entryName.isBlank()) return null
        val normalized = entryName.replace('\\', '/')
        if (normalized.startsWith("/") || normalized.split('/').any { it == ".." }) {
            AppLogger.w(TAG, "Skip unsafe archive entry: $entryName")
            return null
        }
        val target = File(baseDir, normalized)
        val baseCanonical = baseDir.canonicalFile
        val targetCanonical = target.canonicalFile
        // startsWith(base + separator) — a bare prefix match would allow sibling dirs
        // (/data/baseEvil when base is /data/base) to pass.
        return if (targetCanonical.path.startsWith(baseCanonical.path + File.separator)) {
            targetCanonical
        } else {
            AppLogger.w(TAG, "Skip unsafe archive entry: $entryName")
            null
        }
    }

    /**
     * Extract every entry of [zis] into [destDir]: names resolved through [safeChild],
     * [filter] skips junk entries (macOS metadata etc.), [observe] runs for every raw
     * entry before filtering (e.g. top-level dir detection). Enforces zip-bomb caps
     * ([maxEntries] / [maxTotalBytes]). Returns the total extracted byte count.
     */
    fun extractAll(
        zis: ZipInputStream,
        destDir: File,
        maxEntries: Int = DEFAULT_MAX_ENTRIES,
        maxTotalBytes: Long = DEFAULT_MAX_TOTAL_BYTES,
        filter: (String) -> Boolean = { true },
        observe: (ZipEntry) -> Unit = {}
    ): Long {
        val guard = EntryGuard(maxEntries, maxTotalBytes)
        var entry = zis.nextEntry
        while (entry != null) {
            guard.onEntry()
            observe(entry)
            if (filter(entry.name)) {
                if (entry.isDirectory) {
                    safeChild(destDir, entry.name)?.mkdirs()
                } else {
                    val outFile = safeChild(destDir, entry.name)
                    if (outFile != null) {
                        outFile.parentFile?.mkdirs()
                        FileOutputStream(outFile).use { out -> guard.copyTo(zis, out) }
                    }
                }
            }
            zis.closeEntry()
            entry = zis.nextEntry
        }
        return guard.totalBytes
    }

    /** `unTar`-style mode helper: entry mode's owner-exec bit. */
    fun hasOwnerExecBit(mode: Long): Boolean = (mode and 0b001_000_000L) != 0L
}
