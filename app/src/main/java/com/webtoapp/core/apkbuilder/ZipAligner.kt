package com.webtoapp.core.apkbuilder

import com.webtoapp.core.logging.AppLogger
import java.io.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

object ZipAligner {

    private const val TAG = "ZipAligner"

    private const val LFH_FIXED_SIZE = 30

    private const val DEFAULT_ALIGNMENT = 4L
    private const val NATIVE_LIB_ALIGNMENT = 16 * 1024L

    private const val COPY_BUFFER_SIZE = 64 * 1024

    private fun computeCrc(zipIn: ZipFile, entry: ZipEntry): Long {
        val crc = CRC32()
        val buffer = ByteArray(COPY_BUFFER_SIZE)
        zipIn.getInputStream(entry).use { input ->
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                crc.update(buffer, 0, read)
            }
        }
        return crc.value
    }

    fun alignInPlace(apkFile: File): Boolean {
        // Fast path: cached/REUSE outputs are already aligned — header-only scan
        // is far cheaper than a full rewrite. Skip when native libs pass.
        try {
            if (verifyNativeLibAlignment(apkFile)) {
                AppLogger.d(TAG, "APK already 16KB-aligned, skipping rewrite")
                return true
            }
        } catch (e: Exception) {
            AppLogger.w(TAG, "Pre-align check failed, proceeding with full align: ${e.message}")
        }
        val tempFile = File(apkFile.parent, apkFile.name + ".aligned")
        return try {
            val result = align(apkFile, tempFile)
            if (result && isValidZip(tempFile)) {
                apkFile.delete()
                tempFile.renameTo(apkFile)
                true
            } else {
                if (result) {
                    AppLogger.e(TAG, "Aligned APK is not a valid zip; keeping original unaligned APK")
                }
                tempFile.delete()
                false
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "ZipAlign failed", e)
            tempFile.delete()
            false
        }
    }

    private fun isValidZip(file: File): Boolean {
        if (!file.exists() || file.length() == 0L) return false
        return try {
            ZipFile(file).use { zip ->
                zip.entries().hasMoreElements()
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Aligned APK validation failed: ${e.message}")
            false
        }
    }


    /**
     * Manual zip writer: java.util.zip.ZipOutputStream silently drops `entry.extra`
     * set on STORED entries with known sizes, which deletes the alignment padding
     * this class exists to inject. Entries are therefore written as raw bytes:
     * local file headers (with padding extras), data, then a central directory
     * assembled in memory. Deflated entries stream through a raw deflate
     * (nowrap) compressor so no zlib wrapper lands in the file.
     */
    private class ManualZipWriter(out: OutputStream) : Closeable {
        private val stream = java.io.BufferedOutputStream(out, COPY_BUFFER_SIZE)
        private val entries = mutableListOf<CentralDirRecord>()
        private var offset = 0L

        private class CentralDirRecord(
            val name: ByteArray,
            val crc: Long,
            val size: Long,
            val compressedSize: Long,
            val method: Int,
            val extra: ByteArray?,
            val lfhOffset: Long
        )

        private fun write(buf: ByteArray, from: Int = 0, length: Int = buf.size) {
            stream.write(buf, from, length)
            offset += length
        }

        private fun localHeader(
            name: ByteArray,
            crc: Long,
            size: Long,
            compressedSize: Long,
            method: Int,
            extra: ByteArray?
        ) {
            val hdr = ByteBuffer.allocate(LFH_FIXED_SIZE).order(ByteOrder.LITTLE_ENDIAN)
            hdr.putInt(0x04034b50)
            hdr.putShort(20)                        // version needed
            hdr.putShort(0)                         // flags: no data descriptor (sizes known)
            hdr.putShort(method.toShort())
            hdr.putShort(0)                         // mod time
            hdr.putShort(0)                         // mod date
            hdr.putInt(crc.toInt())
            hdr.putInt(compressedSize.toInt())
            hdr.putInt(size.toInt())
            hdr.putShort(name.size.toShort())
            hdr.putShort((extra?.size ?: 0).toShort())
            write(hdr.array())
            write(name)
            if (extra != null && extra.isNotEmpty()) write(extra)
        }

        /** STORED entry with an explicit extra field (the alignment padding). */
        fun addStored(name: String, data: InputStream, size: Long, crc: Long, extra: ByteArray?) {
            val nameBytes = name.toByteArray(Charsets.UTF_8)
            val lfhOffset = offset
            localHeader(nameBytes, crc, size, size, ZipEntry.STORED, extra)
            val buffer = ByteArray(COPY_BUFFER_SIZE)
            while (true) {
                val read = data.read(buffer)
                if (read < 0) break
                write(buffer, 0, read)
            }
            entries += CentralDirRecord(nameBytes, crc, size, size, ZipEntry.STORED, extra, lfhOffset)
        }

        /** DEFLATED entry streamed through a raw deflate compressor. */
        fun addDeflated(name: String, data: InputStream) {
            val nameBytes = name.toByteArray(Charsets.UTF_8)
            val lfhOffset = offset
            localHeader(nameBytes, 0, 0, 0, ZipEntry.DEFLATED, null)
            val deflater = java.util.zip.Deflater(
                java.util.zip.Deflater.DEFAULT_COMPRESSION, true
            )
            val crc = CRC32()
            var raw = 0L
            var compressed = 0L
            val input = ByteArray(COPY_BUFFER_SIZE)
            val buf = ByteArray(COPY_BUFFER_SIZE)
            try {
                while (true) {
                    val read = data.read(input)
                    if (read < 0) break
                    crc.update(input, 0, read)
                    raw += read
                    deflater.setInput(input, 0, read)
                    while (!deflater.needsInput()) {
                        val n = deflater.deflate(buf)
                        if (n == 0) break
                        write(buf, 0, n)
                        compressed += n
                    }
                }
                deflater.finish()
                while (!deflater.finished()) {
                    val n = deflater.deflate(buf)
                    if (n == 0) break
                    write(buf, 0, n)
                    compressed += n
                }
            } finally {
                deflater.end()
            }
            // Sizes/crc were unknown when the header was written (they are not known
            // until the stream is fully consumed); remember the real header bytes and
            // applyHeaderPatches() rewrites them in place once the file is complete.
            val patch = ByteBuffer.allocate(LFH_FIXED_SIZE).order(ByteOrder.LITTLE_ENDIAN)
            patch.putInt(0x04034b50)
            patch.putShort(20)
            patch.putShort(0)
            patch.putShort(ZipEntry.DEFLATED.toShort())
            patch.putShort(0)
            patch.putShort(0)
            patch.putInt(crc.value.toInt())
            patch.putInt(compressed.toInt())
            patch.putInt(raw.toInt())
            patch.putShort(nameBytes.size.toShort())
            patch.putShort(0)
            entries += CentralDirRecord(nameBytes, crc.value, raw, compressed, ZipEntry.DEFLATED, null, lfhOffset)
            pendingHeaderPatches += lfhOffset to patch.array()
        }

        private val pendingHeaderPatches = mutableListOf<Pair<Long, ByteArray>>()
        val offsetOfEntry: Long get() = offset

        /** Empty directory entry. */
        fun addDirectory(name: String) {
            val nameBytes = name.toByteArray(Charsets.UTF_8)
            val lfhOffset = offset
            localHeader(nameBytes, 0, 0, 0, ZipEntry.STORED, null)
            entries += CentralDirRecord(nameBytes, 0, 0, 0, ZipEntry.STORED, null, lfhOffset)
        }

        private fun centralDirectoryEntry(e: CentralDirRecord): ByteArray {
            val out = java.io.ByteArrayOutputStream()
            val hdr = ByteBuffer.allocate(46).order(ByteOrder.LITTLE_ENDIAN)
            hdr.putInt(0x02014b50)
            hdr.putShort(20)                        // version made by
            hdr.putShort(20)                        // version needed
            hdr.putShort(0)                         // flags
            hdr.putShort(e.method.toShort())
            hdr.putShort(0)                         // mod time
            hdr.putShort(0)                         // mod date
            hdr.putInt(e.crc.toInt())
            hdr.putInt(e.compressedSize.toInt())
            hdr.putInt(e.size.toInt())
            hdr.putShort(e.name.size.toShort())
            hdr.putShort((e.extra?.size ?: 0).toShort())
            hdr.putShort(0)                         // comment
            hdr.putShort(0)                         // disk
            hdr.putShort(0)                         // internal attrs
            hdr.putInt(0)                           // external attrs
            hdr.putInt(e.lfhOffset.toInt())
            out.write(hdr.array())
            out.write(e.name)
            if (e.extra != null) out.write(e.extra)
            return out.toByteArray()
        }

        override fun close() {
            val cdStart = offset
            val cdBytes = java.io.ByteArrayOutputStream()
            entries.forEach { cdBytes.write(centralDirectoryEntry(it)) }
            val cd = cdBytes.toByteArray()
            write(cd)
            val eocd = ByteBuffer.allocate(22).order(ByteOrder.LITTLE_ENDIAN)
            eocd.putInt(0x06054b50)
            eocd.putShort(0)
            eocd.putShort(0)
            eocd.putShort(entries.size.toShort())
            eocd.putShort(entries.size.toShort())
            eocd.putInt(cd.size)
            eocd.putInt(cdStart.toInt())
            eocd.putShort(0)
            write(eocd.array())
            stream.flush()
            stream.close()
        }

        /** Apply remembered local-header patches after the file is complete. */
        fun applyHeaderPatches(file: File) {
            if (pendingHeaderPatches.isEmpty()) return
            RandomAccessFile(file, "rw").use { raf ->
                pendingHeaderPatches.forEach { (at, bytes) ->
                    raf.seek(at)
                    raf.write(bytes)
                }
            }
        }
    }

    fun align(input: File, output: File): Boolean {
        if (!input.exists()) {
            AppLogger.e(TAG, "Input file does not exist: ${input.absolutePath}")
            return false
        }

        var alignedCount = 0
        var totalStored = 0

        return try {
            ZipFile(input).use { zipIn ->
                FileOutputStream(output).use { fos ->
                    val writer = ManualZipWriter(fos)

                    val entries = zipIn.entries().toList()
                        .sortedWith(
                            compareByDescending<ZipEntry> { it.name == "resources.arsc" }
                                .thenBy { it.name.startsWith("META-INF/") }
                                .thenBy { it.name }
                        )

                    for (entry in entries) {
                        if (entry.isDirectory) {
                            writer.addDirectory(entry.name)
                            continue
                        }

                        if (entry.method == ZipEntry.STORED || entry.name == "resources.arsc") {
                            totalStored++

                            val entrySize = entry.size
                            val entryCrc = if (entry.crc != -1L) entry.crc else computeCrc(zipIn, entry)
                            val nameBytes = entry.name.toByteArray(Charsets.UTF_8)

                            // dataOffset = current offset + fixed header + name; pad with
                            // an extra field so the data starts at an alignment multiple.
                            val currentOffset = writer.offsetOfEntry
                            val dataOffsetNoExtra = currentOffset + LFH_FIXED_SIZE + nameBytes.size
                            val alignment = getEntryAlignment(entry.name)
                            val remainder = dataOffsetNoExtra % alignment
                            val padding = if (remainder == 0L) 0 else (alignment - remainder).toInt()

                            if (padding > 0) alignedCount++

                            zipIn.getInputStream(entry).use { stream ->
                                writer.addStored(entry.name, stream, entrySize, entryCrc,
                                    if (padding > 0) ByteArray(padding) else null)
                            }
                        } else {
                            zipIn.getInputStream(entry).use { stream ->
                                writer.addDeflated(entry.name, stream)
                            }
                        }
                    }

                    writer.close()
                    writer.applyHeaderPatches(output)
                }
            }

            AppLogger.d(TAG, "ZipAlign complete: $alignedCount/$totalStored STORED entries aligned")
            isValidZip(output)
        } catch (e: Exception) {
            AppLogger.e(TAG, "ZipAlign failed: ${e.message}", e)
            false
        }
    }

    fun verifyAlignment(apkFile: File): Boolean {
        try {
            RandomAccessFile(apkFile, "r").use { raf ->
                val entries = readCentralDirectory(raf) ?: run {
                    AppLogger.e(TAG, "Cannot parse zip central directory: ${apkFile.name}")
                    return false
                }
                val arsc = entries.firstOrNull { it.name == "resources.arsc" } ?: run {
                    AppLogger.w(TAG, "resources.arsc not found in APK")
                    return false
                }
                val isStored = arsc.method == ZipEntry.STORED
                val dataOffset = entryDataOffset(raf, arsc.localHeaderOffset)
                val isAligned = dataOffset % DEFAULT_ALIGNMENT == 0L
                AppLogger.d(TAG, "resources.arsc: stored=$isStored, dataOffset=$dataOffset, " +
                        "aligned=$isAligned (${dataOffset % DEFAULT_ALIGNMENT})")
                return isStored && isAligned
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Alignment verification failed: ${e.message}")
            return false
        }
    }

    fun verifyNativeLibAlignment(apkFile: File, alignment: Long = NATIVE_LIB_ALIGNMENT): Boolean {
        try {
            RandomAccessFile(apkFile, "r").use { raf ->
                val entries = readCentralDirectory(raf) ?: run {
                    AppLogger.e(TAG, "Cannot parse zip central directory: ${apkFile.name}")
                    return false
                }
                var nativeLibCount = 0
                var storedCount = 0
                for (entry in entries) {
                    if (!isNativeLibraryEntry(entry.name)) continue
                    nativeLibCount++
                    // DEFLATED libs can never be mmap'd from the zip: the
                    // manifest ships extractNativeLibs=true (the template is
                    // packaged with jniLibs.useLegacyPackaging), so the OS
                    // extracts them to nativeLibraryDir at install and the
                    // data offset is irrelevant. Only STORED libs — the
                    // injected runtime binaries like libnode.so — may be
                    // mapped in place and must sit on a page boundary.
                    if (entry.method != ZipEntry.STORED) continue
                    storedCount++
                    val dataOffset = entryDataOffset(raf, entry.localHeaderOffset)
                    if (dataOffset % alignment != 0L) {
                        AppLogger.w(TAG, "Native lib is not ${alignment / 1024}KB zip-aligned: ${entry.name} dataOffset=$dataOffset remainder=${dataOffset % alignment}")
                        return false
                    }
                }
                AppLogger.d(TAG, "Native lib zip alignment verified: $storedCount stored / $nativeLibCount total entries")
                return true
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Native lib alignment verification failed: ${e.message}", e)
            return false
        }
    }

    private class CentralDirectoryEntry(
        val name: String,
        val method: Int,
        val localHeaderOffset: Long
    )

    /**
     * Parse the zip central directory. The verifiers used to walk local file headers
     * sequentially, advancing by the LFH compressedSize plus an optional 16-byte data
     * descriptor — but entries written through java.util.zip with unknown sizes carry
     * compressedSize=0 in the LFH (bit 3 set), so the scan landed inside the compressed
     * stream, missed the next entry signature and bailed out with a false "verified"
     * long before it ever reached lib/. The central directory holds the real sizes and
     * local-header offsets, so indexing from it cannot desynchronize.
     */
    private fun readCentralDirectory(raf: RandomAccessFile): List<CentralDirectoryEntry>? {
        val length = raf.length()
        if (length < 22L) return null
        val maxBack = minOf(length, 22L + 65535L).toInt()
        val tail = ByteArray(maxBack)
        raf.seek(length - maxBack)
        raf.readFully(tail)

        var eocdAt = -1
        for (i in maxBack - 22 downTo 0) {
            if (tail[i] == 0x50.toByte() && tail[i + 1] == 0x4B.toByte() &&
                tail[i + 2] == 0x05.toByte() && tail[i + 3] == 0x06.toByte()
            ) {
                eocdAt = i
                break
            }
        }
        if (eocdAt < 0) return null

        // .slice(): absolute getters on a wrap(array, offset, len) buffer index the
        // backing array from 0, not from offset — without the slice every field below
        // reads misaligned garbage.
        val eocd = ByteBuffer.wrap(tail, eocdAt, 22).slice().order(ByteOrder.LITTLE_ENDIAN)
        val entryCount = eocd.getShort(10).toInt() and 0xFFFF
        val cdSize = eocd.getInt(12).toLong() and 0xFFFFFFFFL
        val cdOffset = eocd.getInt(16).toLong() and 0xFFFFFFFFL
        // zip64 markers — this pipeline never produces one; refuse to claim verification.
        if (entryCount == 0xFFFF || cdSize == 0xFFFFFFFFL || cdOffset == 0xFFFFFFFFL) return null
        if (cdOffset < 0 || cdOffset + cdSize > length) return null

        val cd = ByteArray(cdSize.toInt())
        raf.seek(cdOffset)
        raf.readFully(cd)

        val entries = ArrayList<CentralDirectoryEntry>(entryCount)
        var p = 0
        repeat(entryCount) {
            if (p + 46 > cd.size) return null
            // slice(): see the EOCD note — absolute getters index the backing array.
            val e = ByteBuffer.wrap(cd, p, 46).slice().order(ByteOrder.LITTLE_ENDIAN)
            if (e.getInt(0) != 0x02014b50) return null
            val method = e.getShort(10).toInt() and 0xFFFF
            val nameLen = e.getShort(28).toInt() and 0xFFFF
            val extraLen = e.getShort(30).toInt() and 0xFFFF
            val commentLen = e.getShort(32).toInt() and 0xFFFF
            val lfhOffset = e.getInt(42).toLong() and 0xFFFFFFFFL
            if (p + 46 + nameLen > cd.size) return null
            val name = String(cd, p + 46, nameLen, Charsets.UTF_8)
            entries += CentralDirectoryEntry(name, method, lfhOffset)
            p += 46 + nameLen + extraLen + commentLen
        }
        return entries
    }

    /** Entry data offset = local header offset + fixed LFH + name + extra field lengths. */
    private fun entryDataOffset(raf: RandomAccessFile, lfhOffset: Long): Long {
        val lfh = ByteArray(LFH_FIXED_SIZE)
        raf.seek(lfhOffset)
        raf.readFully(lfh)
        val buf = ByteBuffer.wrap(lfh).order(ByteOrder.LITTLE_ENDIAN)
        val nameLen = buf.getShort(26).toInt() and 0xFFFF
        val extraLen = buf.getShort(28).toInt() and 0xFFFF
        return lfhOffset + LFH_FIXED_SIZE + nameLen + extraLen
    }

    private fun getEntryAlignment(name: String): Long {
        return if (isNativeLibraryEntry(name)) NATIVE_LIB_ALIGNMENT else DEFAULT_ALIGNMENT
    }

    private fun isNativeLibraryEntry(name: String): Boolean {
        return name.startsWith("lib/") && name.endsWith(".so")
    }
}
