package com.webtoapp.core.nodejs

import android.content.Context
import android.os.Build
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.download.DependencyDownloadEngine
import com.webtoapp.core.download.DependencyDownloadNotification
import com.webtoapp.core.logging.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.Locale

object NodeDependencyManager {

    private const val TAG = "NodeDependencyManager"

    const val NODE_VERSION = "18.20.4"

    enum class MirrorRegion { CN, GLOBAL }

    private val GITHUB_CN_PROXIES = listOf(
        "https://ghfast.top/",
        "https://gh-proxy.com/"
    )

    private val NODE_GITHUB_URL = "https://github.com/nodejs-mobile/nodejs-mobile/releases/download/v${NODE_VERSION}/nodejs-mobile-v${NODE_VERSION}-android.zip"

    data class MirrorConfig(

        val nodeUrls: List<String>
    )

    private fun buildCnMirror(): MirrorConfig {
        val orderedProxies = com.webtoapp.core.network.CnMirrorProbe.getOrderedProxies(GITHUB_CN_PROXIES)
        return MirrorConfig(
            nodeUrls = orderedProxies.map { proxy -> "${proxy}${NODE_GITHUB_URL}" } + NODE_GITHUB_URL
        )
    }

    private val GLOBAL_MIRROR = MirrorConfig(
        nodeUrls = listOf(NODE_GITHUB_URL)
    )

    private const val MAX_RETRY_PER_URL = 2

    private const val RETRY_DELAY_MS = 2000L

    sealed class DownloadState {
        object Idle : DownloadState()
        data class Downloading(val progress: Float, val currentFile: String, val bytesDownloaded: Long, val totalBytes: Long) : DownloadState()
        data class Verifying(val fileName: String) : DownloadState()
        data class Extracting(val fileName: String) : DownloadState()
        object Complete : DownloadState()
        data class Error(val message: String, val retryable: Boolean = true) : DownloadState()

        data class Paused(val progress: Float, val currentFile: String, val bytesDownloaded: Long, val totalBytes: Long) : DownloadState()
    }

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState
    private val runtimeDownloadMutex = Mutex()

    private var _userMirrorRegion: MirrorRegion? = null

    fun setMirrorRegion(region: MirrorRegion?) {
        _userMirrorRegion = region
    }

    fun getMirrorRegion(): MirrorRegion {
        _userMirrorRegion?.let { return it }
        val lang = Locale.getDefault().language
        return if (lang == "zh") MirrorRegion.CN else MirrorRegion.GLOBAL
    }

    fun getMirrorConfig(): MirrorConfig {
        return when (getMirrorRegion()) {
            MirrorRegion.CN -> buildCnMirror()
            MirrorRegion.GLOBAL -> GLOBAL_MIRROR
        }
    }

    fun getDepsDir(context: Context): File {
        return File(context.filesDir, "nodejs_deps").also { it.mkdirs() }
    }

    fun getNodeDir(context: Context): File {
        val abi = getDeviceAbi()
        return File(getDepsDir(context), "node/$abi").also { it.mkdirs() }
    }

    fun getNodeProjectsDir(context: Context): File {
        return File(context.filesDir, "nodejs_projects").also { it.mkdirs() }
    }

    const val NODE_BINARY_NAME = "libnode.so"

    fun isNodeReady(context: Context): Boolean {
        val nativeNode = File(context.applicationInfo.nativeLibraryDir, NODE_BINARY_NAME)
        if (nativeNode.exists()) return true
        val downloadedNode = File(getNodeDir(context), NODE_BINARY_NAME)
        return downloadedNode.exists() && downloadedNode.length() > 0
    }

    fun getNodeExecutablePath(context: Context): String {
        val nativeNode = File(context.applicationInfo.nativeLibraryDir, NODE_BINARY_NAME)
        if (nativeNode.exists()) {
            AppLogger.d(TAG, "Using nativeLibraryDir Node: ${nativeNode.absolutePath}")
            return nativeNode.absolutePath
        }
        val downloadedNode = File(getNodeDir(context), NODE_BINARY_NAME)
        if (downloadedNode.exists()) {
            AppLogger.d(TAG, "Using downloaded Node: ${downloadedNode.absolutePath}")
            return downloadedNode.absolutePath
        }

        AppLogger.d(TAG, "Node placeholder path (not installed): ${downloadedNode.absolutePath}")
        return downloadedNode.absolutePath
    }

    fun getNodeLibraryPath(context: Context): String? {
        val nativeNode = File(context.applicationInfo.nativeLibraryDir, NODE_BINARY_NAME)
        if (nativeNode.exists()) {
            AppLogger.d(TAG, "libnode.so path (nativeLibraryDir): ${nativeNode.absolutePath}")
            return nativeNode.absolutePath
        }
        val downloadedNode = File(getNodeDir(context), NODE_BINARY_NAME)
        if (downloadedNode.exists() && downloadedNode.length() > 0) {
            ensureLibnodePageAligned(downloadedNode)
            AppLogger.d(TAG, "libnode.so path (download cache): ${downloadedNode.absolutePath}")
            return downloadedNode.absolutePath
        }
        AppLogger.w(TAG, "libnode.so is missing from both nativeLibraryDir and download cache")
        return null
    }

    private fun ensureLibnodePageAligned(lib: File) {
        if (!lib.exists() || lib.length() <= 0L) return
        if (isElfPageAlignmentCompatible(lib)) return
        val pageSize = systemPageSize().toLong()
        AppLogger.i(TAG, "libnode.so is incompatible with system page size ${pageSize}B; rewriting PT_LOAD…")
        val ok = tryPatchPageAlignment(lib, pageSize)
        if (!ok) {
            AppLogger.e(TAG, "libnode.so ELF page-align patch failed; dlopen will still fail")
        } else if (!isElfPageAlignmentCompatible(lib)) {
            AppLogger.e(TAG, "libnode.so patch finished but a second pass still flags incompatibility (suspect patcher bug)")
        } else {
            AppLogger.i(TAG, "libnode.so re-packaged to ${pageSize}B alignment")
        }
    }

    suspend fun downloadNodeRuntime(context: Context): Boolean = withContext(Dispatchers.IO) {
        runtimeDownloadMutex.withLock {
            DependencyDownloadNotification.getInstance(context)
            if (isNodeReady(context)) {
                markComplete()
                return@withLock true
            }
            try {
                _downloadState.value = DownloadState.Idle
                DependencyDownloadEngine.reset()
                val mirror = getMirrorConfig()

                if (!isNodeReady(context)) {
                    val success = downloadNode(context, mirror)
                    if (!success) return@withLock false
                }

                markComplete()
                AppLogger.i(TAG, "Node.js runtime download complete")
                true
            } catch (e: Exception) {
                AppLogger.e(TAG, "Failed to download Node.js runtime", e)
                markError(e.message ?: "未知错误")
                false
            }
        }
    }

    fun clearCache(context: Context) {
        getDepsDir(context).deleteRecursively()
        AppLogger.i(TAG, "Node.js Dependency cache cleared")
    }

    fun getCacheSize(context: Context): Long {
        return getDepsDir(context).walkTopDown().filter { it.isFile }.sumOf { it.length() }
    }

    fun getDeviceAbi(): String {
        return Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a"
    }

    private suspend fun downloadWithRetry(
        urls: List<String>,
        destFile: File,
        displayName: String,
        context: Context?
    ): Boolean {
        for ((urlIndex, url) in urls.withIndex()) {
            val sourceName = if (urls.size > 1) "$displayName [源${urlIndex + 1}/${urls.size}]" else displayName
            AppLogger.i(TAG, "Attempting to download $sourceName: $url")

            for (attempt in 1..MAX_RETRY_PER_URL) {
                val success = DependencyDownloadEngine.downloadFile(url, destFile, sourceName, context)
                if (success) return true

                if (attempt < MAX_RETRY_PER_URL) {
                    AppLogger.i(TAG, "$sourceName download failed, retrying in ${RETRY_DELAY_MS / 1000}s ($attempt/$MAX_RETRY_PER_URL)")
                    kotlinx.coroutines.delay(RETRY_DELAY_MS)
                    DependencyDownloadEngine._state.value = DependencyDownloadEngine.State.Idle
                }
            }

            if (urlIndex < urls.lastIndex) {
                val tmpFile = File(destFile.parentFile, "${destFile.name}.tmp")
                tmpFile.delete()
                AppLogger.i(TAG, "$sourceName failed, switching to next source...")
                DependencyDownloadEngine._state.value = DependencyDownloadEngine.State.Idle
            }
        }
        return false
    }

    private suspend fun downloadNode(context: Context, mirror: MirrorConfig): Boolean {
        val abi = getDeviceAbi()
        val nodeUrls = mirror.nodeUrls
        val fileName = nodeUrls.first().substringAfterLast("/")
        val destDir = getNodeDir(context)
        val archiveFile = File(getDepsDir(context), fileName)

        AppLogger.i(TAG, "Downloading Node.js runtime (${nodeUrls.size} sources)")

        val downloaded = downloadWithRetry(nodeUrls, archiveFile, "Node.js $NODE_VERSION ($abi)", context)
        syncEngineState()
        if (!downloaded) return false

        _downloadState.value = DownloadState.Extracting("Node.js")
        DependencyDownloadEngine._state.value = DependencyDownloadEngine.State.Extracting("Node.js")
        try {
            extractNodeZip(archiveFile, destDir, abi)

            val nodeLib = File(destDir, NODE_BINARY_NAME)
            if (nodeLib.exists()) {
                nodeLib.setExecutable(true, false)
                AppLogger.i(TAG, "Node.js runtime ready: ${nodeLib.absolutePath} (${nodeLib.length()} bytes)")
            } else {
                AppLogger.e(TAG, "Not found after extraction: $NODE_BINARY_NAME (ABI: $abi)")
                markError("解压后未找到 Node.js 运行时 (ABI: $abi)")
                return false
            }

            ensureLibnodePageAligned(nodeLib)

            archiveFile.delete()
            return true
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to extract Node.js", e)
            markError("解压 Node.js 失败: ${e.message}")
            return false
        }
    }

    private fun extractNodeZip(zipFile: File, destDir: File, abi: String) {
        val zipInput = java.util.zip.ZipInputStream(zipFile.inputStream().buffered())
        var foundLib = false

        zipInput.use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {

                if (!entry.isDirectory && entry.name.contains(abi) && entry.name.endsWith("libnode.so")) {
                    val outFile = File(destDir, NODE_BINARY_NAME)
                    outFile.parentFile?.mkdirs()
                    FileOutputStream(outFile).use { fos ->
                        zis.copyTo(fos)
                    }
                    outFile.setExecutable(true, false)
                    foundLib = true
                    AppLogger.i(TAG, "Extracting ${entry.name} -> ${outFile.absolutePath}")
                } else if (!entry.isDirectory && entry.name.endsWith(".so") && entry.name.contains(abi)) {

                    val soName = entry.name.substringAfterLast("/")
                    val outFile = File(destDir, soName)
                    outFile.parentFile?.mkdirs()
                    FileOutputStream(outFile).use { fos ->
                        zis.copyTo(fos)
                    }
                }
                entry = zis.nextEntry
            }
        }

        if (!foundLib) {
            throw IllegalStateException(Strings.nodeLibNotFoundInZip.format(abi))
        }
    }

    private fun syncEngineState() {
        when (val es = DependencyDownloadEngine.state.value) {
            is DependencyDownloadEngine.State.Downloading -> {
                _downloadState.value = DownloadState.Downloading(
                    progress = es.progress,
                    currentFile = es.displayName,
                    bytesDownloaded = es.bytesDownloaded,
                    totalBytes = es.totalBytes
                )
            }
            is DependencyDownloadEngine.State.Paused -> {
                _downloadState.value = DownloadState.Paused(
                    progress = es.progress,
                    currentFile = es.displayName,
                    bytesDownloaded = es.bytesDownloaded,
                    totalBytes = es.totalBytes
                )
            }
            is DependencyDownloadEngine.State.Error -> {
                _downloadState.value = DownloadState.Error(es.message)
            }
            else -> {}
        }
    }

    private fun markComplete() {
        _downloadState.value = DownloadState.Complete
        DependencyDownloadEngine._state.value = DependencyDownloadEngine.State.Complete
    }

    private fun markError(message: String, retryable: Boolean = true) {
        _downloadState.value = DownloadState.Error(message, retryable = retryable)
        DependencyDownloadEngine._state.value = DependencyDownloadEngine.State.Error(message)
    }

    fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().buffered().use { input ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun systemPageSize(): Int {
        return runCatching {
            val sysconfClass = Class.forName("android.system.Os")
            val sysconf = sysconfClass.getMethod("sysconf", Int::class.javaPrimitiveType)

            val value = sysconf.invoke(null, 39) as Long
            value.toInt().takeIf { it > 0 } ?: 4096
        }.getOrDefault(4096)
    }

    private fun readElfMinLoadAlign(file: File): Long? {
        return runCatching {
            java.io.RandomAccessFile(file, "r").use { raf ->
                if (raf.length() < 64) return null

                val magic = ByteArray(4)
                raf.readFully(magic)
                if (magic[0] != 0x7f.toByte() || magic[1] != 'E'.code.toByte() ||
                    magic[2] != 'L'.code.toByte() || magic[3] != 'F'.code.toByte()) {
                    return null
                }
                val elfClass = raf.readByte().toInt()
                val elfData = raf.readByte().toInt()
                if (elfClass != 2 || elfData != 1) return null

                raf.seek(32)
                val phoff = readLeLong(raf)
                raf.seek(54)
                val phentsize = readLeShort(raf)
                val phnum = readLeShort(raf)
                if (phentsize <= 0 || phnum <= 0) return null

                var minAlign = Long.MAX_VALUE
                var found = false
                for (i in 0 until phnum) {
                    raf.seek(phoff + i.toLong() * phentsize)
                    val pType = readLeInt(raf)
                    if (pType == 1) {

                        raf.seek(phoff + i.toLong() * phentsize + 48)
                        val pAlign = readLeLong(raf)
                        if (pAlign > 0L && pAlign < minAlign) minAlign = pAlign
                        found = true
                    }
                }
                if (!found || minAlign == Long.MAX_VALUE) null else minAlign
            }
        }.getOrNull()
    }

    private fun readLeShort(raf: java.io.RandomAccessFile): Int {
        val a = raf.readUnsignedByte()
        val b = raf.readUnsignedByte()
        return (b shl 8) or a
    }

    private fun readLeInt(raf: java.io.RandomAccessFile): Int {
        val a = raf.readUnsignedByte()
        val b = raf.readUnsignedByte()
        val c = raf.readUnsignedByte()
        val d = raf.readUnsignedByte()
        return (d shl 24) or (c shl 16) or (b shl 8) or a
    }

    private fun readLeLong(raf: java.io.RandomAccessFile): Long {
        var v = 0L
        for (i in 0 until 8) {
            v = v or (raf.readUnsignedByte().toLong() shl (8 * i))
        }
        return v
    }

    private fun isElfPageAlignmentCompatible(lib: File): Boolean {
        val align = readElfMinLoadAlign(lib) ?: return false
        val pageSize = systemPageSize().toLong()
        return align >= pageSize
    }

    private fun tryPatchPageAlignment(lib: File, targetAlign: Long): Boolean {
        if (targetAlign <= 0 || (targetAlign and (targetAlign - 1)) != 0L) {
            AppLogger.e(TAG, "tryPatchPageAlignment: targetAlign must be a power of two: $targetAlign")
            return false
        }
        return runCatching {
            patchElfPageSize(lib, targetAlign)
        }.getOrElse { e ->
            AppLogger.e(TAG, "ELF patch exception", e)
            false
        }
    }

    private data class PtLoad(
        val phdrOffset: Long,
        val pOffset: Long,
        val pVaddr: Long,
        val pFilesz: Long,
        val pMemsz: Long,
    )

    private data class Shdr(
        val index: Int,
        val phdrOffset: Long,
        val shName: Int,
        val shType: Int,
        val shFlags: Long,
        val shAddr: Long,
        val shOffset: Long,
        val shSize: Long,
        val shLink: Int,
        val shInfo: Int,
        val shAddralign: Long,
        val shEntsize: Long,
    )

    private fun patchElfPageSize(lib: File, pageSize: Long): Boolean {

        val src = lib.readBytes()
        if (src.size < 64) return false
        if (src[0] != 0x7f.toByte() || src[1] != 'E'.code.toByte() ||
            src[2] != 'L'.code.toByte() || src[3] != 'F'.code.toByte()
        ) return false
        if (src[4].toInt() != 2 || src[5].toInt() != 1) {
            AppLogger.e(TAG, "patchElfPageSize: only ELF64 LSB is supported")
            return false
        }

        val ePhoff = readLeLongAt(src, 32)
        val eShoff = readLeLongAt(src, 40)
        val ePhentsize = readLeShortAt(src, 54)
        val ePhnum = readLeShortAt(src, 56)
        val eShentsize = readLeShortAt(src, 58)
        val eShnum = readLeShortAt(src, 60)
        if (ePhnum <= 0 || ePhentsize <= 0) return false

        val phdrs = ArrayList<LongArray>(ePhnum)
        for (i in 0 until ePhnum) {
            val base = ePhoff + i.toLong() * ePhentsize
            phdrs += longArrayOf(
                base,
                readLeIntAt(src, base.toInt() + 0).toLong() and 0xffffffffL,
                readLeIntAt(src, base.toInt() + 4).toLong() and 0xffffffffL,
                readLeLongAt(src, base.toInt() + 8),
                readLeLongAt(src, base.toInt() + 16),
                readLeLongAt(src, base.toInt() + 24),
                readLeLongAt(src, base.toInt() + 32),
                readLeLongAt(src, base.toInt() + 40),
                readLeLongAt(src, base.toInt() + 48),
            )
        }

        val ptLoads = phdrs.filter { it[1] == 1L }.map {
            PtLoad(
                phdrOffset = it[0],
                pOffset = it[3],
                pVaddr = it[4],
                pFilesz = it[6],
                pMemsz = it[7],
            )
        }.sortedBy { it.pVaddr }

        if (ptLoads.isEmpty()) {
            AppLogger.w(TAG, "patchElfPageSize: no PT_LOAD segment found")
            return false
        }

        val shdrs = ArrayList<Shdr>(eShnum)
        for (i in 0 until eShnum) {
            val base = eShoff + i.toLong() * eShentsize
            shdrs += Shdr(
                index = i,
                phdrOffset = base,
                shName = readLeIntAt(src, base.toInt() + 0),
                shType = readLeIntAt(src, base.toInt() + 4),
                shFlags = readLeLongAt(src, base.toInt() + 8),
                shAddr = readLeLongAt(src, base.toInt() + 16),
                shOffset = readLeLongAt(src, base.toInt() + 24),
                shSize = readLeLongAt(src, base.toInt() + 32),
                shLink = readLeIntAt(src, base.toInt() + 40),
                shInfo = readLeIntAt(src, base.toInt() + 44),
                shAddralign = readLeLongAt(src, base.toInt() + 48),
                shEntsize = readLeLongAt(src, base.toInt() + 56),
            )
        }

        val mask = pageSize - 1

        val newOffsets = HashMap<Long, Long>()
        var prevEnd = 0L
        for (s in ptLoads) {
            if (s.pOffset == 0L && s.pVaddr == 0L) {
                newOffsets[s.phdrOffset] = 0L
                if (s.pFilesz > prevEnd) prevEnd = s.pFilesz
                continue
            }
            val targetMod = s.pVaddr and mask
            val rounded = (prevEnd + mask) and mask.inv()
            var newOff = rounded + targetMod
            if (newOff < prevEnd) newOff += pageSize
            newOffsets[s.phdrOffset] = newOff
            prevEnd = newOff + s.pFilesz
        }

        val needsWrite = ptLoads.any { newOffsets[it.phdrOffset] != it.pOffset } ||
            phdrs.any { it[8] < pageSize && it[1] == 1L }
        if (!needsWrite) {
            AppLogger.d(TAG, "patchElfPageSize: file is already ${pageSize}-aligned, no rewrite needed")
            return true
        }

        val newShoff = (prevEnd + mask) and mask.inv()
        val sectHdrTableSize = eShentsize.toLong() * eShnum

        val nonLoadSections = shdrs.filter {
            it.shType != 0 && it.shSize > 0L && (it.shFlags and 0x2L) == 0L
        }
        var cursor = newShoff + sectHdrTableSize
        val newShOffsets = HashMap<Int, Long>()
        for (sh in nonLoadSections) {
            val align = if (sh.shAddralign < 1L) 1L else sh.shAddralign
            cursor = ((cursor + align - 1L) / align) * align
            newShOffsets[sh.index] = cursor
            cursor += sh.shSize
        }
        val finalSize = cursor

        if (finalSize < 0L || finalSize > Int.MAX_VALUE.toLong()) {
            AppLogger.e(TAG, "patchElfPageSize: computed target size is too large: $finalSize")
            return false
        }
        val out = ByteArray(finalSize.toInt())

        System.arraycopy(src, 0, out, 0, 64)

        System.arraycopy(
            src, ePhoff.toInt(),
            out, ePhoff.toInt(),
            (ePhnum.toLong() * ePhentsize).toInt()
        )

        for (s in ptLoads) {
            val newOff = newOffsets[s.phdrOffset]!!
            if (s.pFilesz > 0L) {
                System.arraycopy(
                    src, s.pOffset.toInt(),
                    out, newOff.toInt(),
                    s.pFilesz.toInt()
                )
            }

            writeLeLongAt(out, (s.phdrOffset + 8).toInt(), newOff)
            writeLeLongAt(out, (s.phdrOffset + 48).toInt(), pageSize)
        }

        for (ph in phdrs) {
            val pType = ph[1]
            if (pType == 1L) continue
            val pOff = ph[3]
            for (s in ptLoads) {
                if (pOff in s.pOffset until (s.pOffset + s.pFilesz)) {
                    val delta = newOffsets[s.phdrOffset]!! - s.pOffset
                    writeLeLongAt(out, (ph[0] + 8).toInt(), pOff + delta)
                    break
                }
            }
        }

        for (sh in shdrs) {
            var newOff = sh.shOffset
            if (sh.shType == 0) {
                newOff = 0L
            } else if ((sh.shFlags and 0x2L) != 0L) {

                for (s in ptLoads) {
                    if (sh.shOffset in s.pOffset until (s.pOffset + s.pFilesz)) {
                        newOff = sh.shOffset + (newOffsets[s.phdrOffset]!! - s.pOffset)
                        break
                    }
                }
            } else if (newShOffsets.containsKey(sh.index)) {
                newOff = newShOffsets[sh.index]!!
            }

            val dst = (newShoff + sh.index.toLong() * eShentsize).toInt()
            writeLeIntAt(out, dst + 0, sh.shName)
            writeLeIntAt(out, dst + 4, sh.shType)
            writeLeLongAt(out, dst + 8, sh.shFlags)
            writeLeLongAt(out, dst + 16, sh.shAddr)
            writeLeLongAt(out, dst + 24, newOff)
            writeLeLongAt(out, dst + 32, sh.shSize)
            writeLeIntAt(out, dst + 40, sh.shLink)
            writeLeIntAt(out, dst + 44, sh.shInfo)
            writeLeLongAt(out, dst + 48, sh.shAddralign)
            writeLeLongAt(out, dst + 56, sh.shEntsize)
        }

        for (sh in nonLoadSections) {
            val newOff = newShOffsets[sh.index]!!
            if (sh.shSize > 0L) {
                System.arraycopy(
                    src, sh.shOffset.toInt(),
                    out, newOff.toInt(),
                    sh.shSize.toInt()
                )
            }
        }

        writeLeLongAt(out, 40, newShoff)

        val newFile = File(lib.parentFile, lib.name + ".16k.new")
        newFile.writeBytes(out)
        if (!newFile.renameTo(lib)) {

            newFile.copyTo(lib, overwrite = true)
            newFile.delete()
        }
        lib.setExecutable(true, false)

        val verifyBytes = lib.readBytes()
        var verifiedCount = 0
        var corruptCount = 0
        for (s in ptLoads) {
            val phEntry = s.phdrOffset.toInt()
            val onDisk = readLeLongAt(verifyBytes, phEntry + 48)
            if (onDisk == pageSize) {
                verifiedCount++
            } else {
                corruptCount++
                AppLogger.e(
                    TAG,
                    "patchElfPageSize: PT_LOAD@0x${java.lang.Long.toHexString(s.phdrOffset)} on-disk p_align=0x${java.lang.Long.toHexString(onDisk)} doesn't match expected 0x${java.lang.Long.toHexString(pageSize)}"
                )
            }
        }
        AppLogger.i(
            TAG,
            "patchElfPageSize: rewrite complete ${src.size} bytes -> ${out.size} bytes" +
                " (page=$pageSize, PT_LOAD=${ptLoads.size}, " +
                "verified=$verifiedCount/${ptLoads.size}, " +
                "SHT@0x${java.lang.Long.toHexString(newShoff)})"
        )
        if (corruptCount > 0) {
            AppLogger.e(
                TAG,
                "patchElfPageSize: $corruptCount PT_LOAD entries didn't persist p_align; dlopen will fail"
            )
            return false
        }
        return true
    }

    private fun readLeShortAt(src: ByteArray, off: Int): Int {
        return ((src[off].toInt() and 0xff)) or ((src[off + 1].toInt() and 0xff) shl 8)
    }

    private fun readLeIntAt(src: ByteArray, off: Int): Int {
        return ((src[off].toInt() and 0xff)) or
            ((src[off + 1].toInt() and 0xff) shl 8) or
            ((src[off + 2].toInt() and 0xff) shl 16) or
            ((src[off + 3].toInt() and 0xff) shl 24)
    }

    private fun readLeLongAt(src: ByteArray, off: Int): Long {
        var v = 0L
        for (i in 0 until 8) {
            v = v or ((src[off + i].toLong() and 0xffL) shl (8 * i))
        }
        return v
    }

    private fun writeLeIntAt(dst: ByteArray, off: Int, value: Int) {
        for (i in 0 until 4) {
            dst[off + i] = ((value ushr (8 * i)) and 0xff).toByte()
        }
    }

    private fun writeLeLongAt(dst: ByteArray, off: Int, value: Long) {
        for (i in 0 until 8) {
            dst[off + i] = ((value ushr (8 * i)) and 0xffL).toByte()
        }
    }
}
