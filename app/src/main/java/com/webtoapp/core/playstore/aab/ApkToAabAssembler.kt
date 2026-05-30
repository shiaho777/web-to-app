package com.webtoapp.core.playstore.aab

import com.webtoapp.core.logging.AppLogger
import com.webtoapp.core.playstore.aab.arsc.ArscReader
import com.webtoapp.core.playstore.aab.arsc.ArscToProtoTable
import com.webtoapp.core.playstore.aab.axml.AxmlToProtoXml
import com.webtoapp.core.playstore.aab.axml.ProtoManifestRewriter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.Deflater
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

class ApkToAabAssembler {

    companion object {
        private const val TAG = "ApkToAabAssembler"
    }

    fun assemble(
        sourceApk: File,
        outputAab: File,
        targetSdkOverride: Int? = null
    ): AssembleStats {
        require(sourceApk.exists()) { "Source APK not found: ${sourceApk.absolutePath}" }
        outputAab.parentFile?.mkdirs()

        var manifestEntries = 0
        var resourceXmlConverted = 0
        var resourceXmlSkipped = 0
        var resourceVerbatim = 0
        var dexCount = 0
        var assetCount = 0
        var nativeLibCount = 0
        var rootCount = 0

        val abis = mutableSetOf<String>()
        val assetDirs = mutableSetOf<String>()

        ZipFile(sourceApk).use { zip ->
            ZipOutputStream(FileOutputStream(outputAab)).use { out ->

                out.setLevel(Deflater.BEST_COMPRESSION)

                val manifestEntry = zip.getEntry("AndroidManifest.xml")
                    ?: throw IllegalArgumentException("APK missing AndroidManifest.xml")
                val manifestBytes = zip.getInputStream(manifestEntry).readBytes()
                val protoManifestRaw = AxmlToProtoXml.convert(manifestBytes)

                val protoManifest = if (targetSdkOverride != null) {
                    val before = ProtoManifestRewriter.extractTargetSdkVersion(protoManifestRaw)
                    val rewritten = ProtoManifestRewriter.rewriteTargetSdk(
                        protoManifestRaw,
                        targetSdkOverride
                    )
                    AppLogger.d(
                        TAG,
                        "Rewrote manifest targetSdkVersion: " +
                            "before=${before ?: "<absent>"}, after=$targetSdkOverride"
                    )
                    rewritten
                } else {
                    protoManifestRaw
                }
                writeEntry(out, "base/manifest/AndroidManifest.xml", protoManifest.toByteArray())
                manifestEntries++

                val arscEntry = zip.getEntry("resources.arsc")
                    ?: throw IllegalArgumentException("APK missing resources.arsc")
                val arscBytes = zip.getInputStream(arscEntry).readBytes()
                val table = ArscReader(arscBytes).read()
                val protoTable = ArscToProtoTable.convert(table)
                writeEntry(out, "base/resources.pb", protoTable.toByteArray())

                val referencedResources = table.collectReferencedResourceFiles()
                AppLogger.d(TAG, "Resource table references ${referencedResources.size} res/ paths")

                val entries = zip.entries().toList().sortedBy { it.name }
                for (entry in entries) {
                    if (entry.isDirectory) continue
                    val name = entry.name

                    if (name == "AndroidManifest.xml" || name == "resources.arsc") continue

                    if (name.startsWith("META-INF/")) {
                        val isSignature = name == "META-INF/MANIFEST.MF" ||
                            name.endsWith(".SF") ||
                            name.endsWith(".RSA") ||
                            name.endsWith(".DSA") ||
                            name.endsWith(".EC")
                        if (isSignature) continue

                    }

                    if (name == "stamp-cert-sha256") continue

                    when {
                        name.startsWith("res/") && name.endsWith(".xml") -> {

                            if (name !in referencedResources) {
                                AppLogger.d(TAG, "Skipping orphan res XML: $name")
                                continue
                            }

                            try {
                                val xmlBytes = zip.getInputStream(entry).readBytes()
                                val proto = AxmlToProtoXml.convert(xmlBytes)
                                writeEntry(out, "base/$name", proto.toByteArray())
                                resourceXmlConverted++
                            } catch (e: Exception) {

                                AppLogger.e(TAG, "Failed to convert $name to proto XML", e)
                                resourceXmlSkipped++
                                throw IllegalStateException(
                                    "Cannot convert $name: ${e.message}", e
                                )
                            }
                        }

                        name.startsWith("res/") -> {

                            if (name !in referencedResources) {
                                AppLogger.d(TAG, "Skipping orphan res file: $name")
                                continue
                            }

                            copyEntryVerbatim(zip, entry, out, "base/$name")
                            resourceVerbatim++
                        }

                        name.startsWith("lib/") -> {

                            val abi = name.removePrefix("lib/").substringBefore('/')
                            if (abi.isNotEmpty()) abis.add(abi)
                            copyEntryVerbatim(zip, entry, out, "base/$name")
                            nativeLibCount++
                        }

                        name.startsWith("assets/") -> {
                            val rel = name.removePrefix("assets/")
                            val dir = if (rel.contains('/')) {
                                rel.substringBeforeLast('/')
                            } else ""

                            assetDirs.add(if (dir.isEmpty()) "assets" else "assets/$dir")
                            copyEntryVerbatim(zip, entry, out, "base/$name")
                            assetCount++
                        }

                        name.matches(Regex("classes\\d*\\.dex")) -> {

                            copyEntryVerbatim(zip, entry, out, "base/dex/$name")
                            dexCount++
                        }

                        else -> {

                            copyEntryVerbatim(zip, entry, out, "base/root/$name")
                            rootCount++
                        }
                    }
                }

                val nativeProto = AabFilesProtoFactory.buildNativeLibraries(abis)
                if (nativeProto.directoryCount > 0) {
                    writeEntry(out, "base/native.pb", nativeProto.toByteArray())
                }
                val assetsProto = AabFilesProtoFactory.buildAssets(assetDirs)
                if (assetsProto.directoryCount > 0) {
                    writeEntry(out, "base/assets.pb", assetsProto.toByteArray())
                }

                val bundleConfig = AabBundleConfigFactory.build()
                writeEntry(out, "BundleConfig.pb", bundleConfig.toByteArray())
            }
        }

        val stats = AssembleStats(
            outputBytes = outputAab.length(),
            manifestConverted = manifestEntries,
            resourceXmlConverted = resourceXmlConverted,
            resourceXmlSkipped = resourceXmlSkipped,
            resourceVerbatimCopied = resourceVerbatim,
            assetCount = assetCount,
            nativeLibCount = nativeLibCount,
            dexCount = dexCount,
            rootCount = rootCount,
            abis = abis.toList().sorted(),
            assetDirCount = assetDirs.size
        )
        AppLogger.d(TAG, "Assembled AAB: $stats")
        return stats
    }

    private fun writeEntry(out: ZipOutputStream, name: String, data: ByteArray) {

        val entry = ZipEntry(name)
        out.putNextEntry(entry)
        out.write(data)
        out.closeEntry()
    }

    private fun copyEntryVerbatim(
        sourceZip: ZipFile,
        sourceEntry: ZipEntry,
        out: ZipOutputStream,
        targetName: String
    ) {

        val entry = ZipEntry(targetName)
        out.putNextEntry(entry)
        sourceZip.getInputStream(sourceEntry).use { input ->
            input.copyTo(out)
        }
        out.closeEntry()
    }

    data class AssembleStats(
        val outputBytes: Long,
        val manifestConverted: Int,
        val resourceXmlConverted: Int,
        val resourceXmlSkipped: Int,
        val resourceVerbatimCopied: Int,
        val assetCount: Int,
        val nativeLibCount: Int,
        val dexCount: Int,
        val rootCount: Int,
        val abis: List<String>,
        val assetDirCount: Int
    ) {
        override fun toString(): String = buildString {
            append("AAB(")
            append("size=${outputBytes}B")
            append(", manifest=$manifestConverted")
            append(", resXml=$resourceXmlConverted")
            if (resourceXmlSkipped > 0) append(", resXmlFail=$resourceXmlSkipped")
            append(", resBin=$resourceVerbatimCopied")
            append(", assets=$assetCount")
            append(", nativeLibs=$nativeLibCount(${abis.joinToString(",")})")
            append(", dex=$dexCount")
            append(", root=$rootCount")
            append(")")
        }
    }
}
