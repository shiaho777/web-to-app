package com.webtoapp.util

import com.webtoapp.core.logging.AppLogger
import java.io.File

/**
 * Shared Zip-Slip guard for archive extraction. Every place that unpacks a downloaded or
 * user-picked archive must resolve entry names through [safeChild] — raw
 * `File(destDir, entry.name)` lets a crafted `../../x` entry write outside the intended
 * directory (mirrors the lexical + canonical rules of DataBackupManager.resolveSafeChild).
 */
object SafeZip {

    private const val TAG = "SafeZip"

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

    /** `unTar`-style mode helper: entry mode's owner-exec bit. */
    fun hasOwnerExecBit(mode: Long): Boolean = (mode and 0b001_000_000L) != 0L
}
