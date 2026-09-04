package com.webtoapp.core.apkbuilder

import com.webtoapp.core.logging.AppLogger
import java.io.File
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

object ZipUtils {

    fun writeEntryDeflated(zipOut: ZipOutputStream, name: String, data: ByteArray) {
        val entry = ZipEntry(name)
        entry.method = ZipEntry.DEFLATED
        zipOut.putNextEntry(entry)
        zipOut.write(data)
        zipOut.closeEntry()
    }

    fun writeEntryStoredSimple(zipOut: ZipOutputStream, name: String, data: ByteArray) {
        val entry = ZipEntry(name)
        entry.method = ZipEntry.STORED
        entry.size = data.size.toLong()
        entry.compressedSize = data.size.toLong()

        val crc = CRC32()
        crc.update(data)
        entry.crc = crc.value

        zipOut.putNextEntry(entry)
        zipOut.write(data)
        zipOut.closeEntry()
    }

    fun writeEntryStored(zipOut: ZipOutputStream, name: String, data: ByteArray) {
        val entry = ZipEntry(name)
        entry.method = ZipEntry.STORED
        entry.size = data.size.toLong()
        entry.compressedSize = data.size.toLong()

        if (name == "resources.arsc") {
            val nameBytes = name.toByteArray(Charsets.UTF_8)
            val baseHeaderSize = 30
            val base = baseHeaderSize + nameBytes.size

            val padLen = (4 - (base + 4) % 4) % 4
            if (padLen > 0) {

                val extra = ByteArray(4 + padLen)
                extra[0] = 0xFF.toByte()
                extra[1] = 0xFF.toByte()

                extra[2] = (padLen and 0xFF).toByte()
                extra[3] = ((padLen shr 8) and 0xFF).toByte()

                entry.extra = extra
            }
        }

        val crc = CRC32()
        crc.update(data)
        entry.crc = crc.value

        zipOut.putNextEntry(entry)
        zipOut.write(data)
        zipOut.closeEntry()
    }

    fun writeEntryStoredStreaming(zipOut: ZipOutputStream, name: String, file: File) {
        val fileSize = file.length()

        val crc = CRC32()
        val buffer = ByteArray(8192)
        file.inputStream().buffered().use { input ->
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                crc.update(buffer, 0, bytesRead)
            }
        }

        val entry = ZipEntry(name)
        entry.method = ZipEntry.STORED
        entry.size = fileSize
        entry.compressedSize = fileSize
        entry.crc = crc.value

        zipOut.putNextEntry(entry)
        file.inputStream().buffered().use { input ->
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                zipOut.write(buffer, 0, bytesRead)
            }
        }
        zipOut.closeEntry()

        AppLogger.d("ZipUtils", "Large file streaming embedded(STORED): $name (${fileSize / 1024} KB)")
    }

    fun writeEntryDeflatedStreaming(zipOut: ZipOutputStream, name: String, file: File) {
        val entry = ZipEntry(name)
        entry.method = ZipEntry.DEFLATED

        zipOut.putNextEntry(entry)
        file.inputStream().buffered().use { input ->
            input.copyTo(zipOut)
        }
        zipOut.closeEntry()

        AppLogger.d("ZipUtils", "Large file streaming embedded(DEFLATED): $name (${file.length() / 1024} KB raw)")
    }

    fun copyEntry(zipIn: ZipFile, zipOut: ZipOutputStream, entry: ZipEntry) {
        // Large entries (>2MB) stream to avoid heap pressure from readBytes().
        if (entry.size > 2L * 1024 * 1024) {
            val newEntry = ZipEntry(entry.name)
            newEntry.method = ZipEntry.DEFLATED
            zipOut.putNextEntry(newEntry)
            zipIn.getInputStream(entry).use { it.copyTo(zipOut, 64 * 1024) }
            zipOut.closeEntry()
            return
        }
        val data = zipIn.getInputStream(entry).readBytes()
        writeEntryDeflated(zipOut, entry.name, data)
    }

    fun copyEntryPreserveMethod(zipIn: ZipFile, zipOut: ZipOutputStream, entry: ZipEntry) {
        // Fast path: large entries stream without readBytes(). STORED entries can
        // reuse size/crc from the source central directory (bytes unchanged).
        if (entry.size > 2L * 1024 * 1024) {
            if (entry.method == ZipEntry.STORED && entry.size != -1L && entry.crc != -1L) {
                val newEntry = ZipEntry(entry.name)
                newEntry.method = ZipEntry.STORED
                newEntry.size = entry.size
                newEntry.compressedSize = entry.size
                newEntry.crc = entry.crc
                // Preserve alignment padding for resources.arsc if present.
                if (entry.extra != null && entry.extra!!.isNotEmpty()) {
                    newEntry.extra = entry.extra
                }
                zipOut.putNextEntry(newEntry)
                zipIn.getInputStream(entry).use { it.copyTo(zipOut, 64 * 1024) }
                zipOut.closeEntry()
                return
            }
            if (entry.method != ZipEntry.STORED) {
                val newEntry = ZipEntry(entry.name)
                newEntry.method = ZipEntry.DEFLATED
                zipOut.putNextEntry(newEntry)
                zipIn.getInputStream(entry).use { it.copyTo(zipOut, 64 * 1024) }
                zipOut.closeEntry()
                return
            }
        }
        val data = zipIn.getInputStream(entry).readBytes()
        if (entry.method == ZipEntry.STORED) {

            if (entry.name == "resources.arsc") {
                writeEntryStored(zipOut, entry.name, data)
            } else {
                writeEntryStoredSimple(zipOut, entry.name, data)
            }
        } else {
            writeEntryDeflated(zipOut, entry.name, data)
        }
    }
}
