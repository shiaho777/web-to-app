package com.webtoapp.core.golang

import com.webtoapp.core.logging.AppLogger
import java.io.File
import java.io.RandomAccessFile

/**
 * Minimal Go-archive (.a) writer: appends object files to the archive produced
 * by `compile -pack`, byte-for-byte compatible with what `go tool pack r`
 * used to do (Go 1.26 ships no `pack` tool binary anymore; the go command
 * folds objects in internally).
 *
 * Layout (verified against real toolchain output): magic `!<arch>\n`, 60-byte
 * member headers, bare space-padded names truncated to 16 bytes (e.g. the
 * 18-char `indexbyte_arm64.o/` lands as `indexbyte_arm64.`), zero
 * mtime/uid/gid, mode 644, backtick-LF magic, LF pad to even sizes.
 */
object GoArchive {

    private const val TAG = "GoArchive"
    private const val MAGIC = "!<arch>\n"

    fun appendObjects(archive: File, objects: List<File>): Boolean {
        if (objects.isEmpty()) return true
        return try {
            RandomAccessFile(archive, "rw").use { raf ->
                if (raf.length() < 8) {
                    AppLogger.e(TAG, "archive too small: ${archive.absolutePath}")
                    return false
                }
                val magic = ByteArray(8)
                raf.readFully(magic)
                if (magic.decodeToString() != MAGIC) {
                    AppLogger.e(TAG, "bad archive magic: ${archive.absolutePath}")
                    return false
                }
                raf.seek(raf.length())
                for (obj in objects) {
                    if (!appendOne(raf, obj)) return false
                }
            }
            true
        } catch (e: Exception) {
            AppLogger.e(TAG, "ar append failed: ${archive.absolutePath}", e)
            false
        }
    }

    private fun appendOne(raf: RandomAccessFile, obj: File): Boolean {
        return try {
            val rawName = obj.name.take(16).padEnd(16, ' ')
            // %-16s %-12d %-6d %-6d %-8o %-10d + backtick-LF, exactly 60 bytes.
            val header = "%-16s%-12d%-6d%-6d%-8o%-10d`\n".format(
                rawName, 0, 0, 0, 420, obj.length()
            )
            val headerBytes = header.toByteArray(Charsets.US_ASCII)
            if (headerBytes.size != 60) {
                AppLogger.e(TAG, "header size ${headerBytes.size} != 60 for ${obj.name}")
                return false
            }
            raf.write(headerBytes)
            obj.inputStream().use { input ->
                val buf = ByteArray(65536)
                while (true) {
                    val r = input.read(buf)
                    if (r <= 0) break
                    raf.write(buf, 0, r)
                }
            }
            if (obj.length() and 1L == 1L) raf.writeByte('\n'.code)
            true
        } catch (e: Exception) {
            AppLogger.e(TAG, "ar append member failed: ${obj.absolutePath}", e)
            false
        }
    }
}
