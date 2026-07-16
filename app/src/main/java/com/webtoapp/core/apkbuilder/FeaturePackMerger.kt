package com.webtoapp.core.apkbuilder

import com.webtoapp.core.feature.EnabledFeature
import com.webtoapp.core.feature.EnabledFeaturesFile
import com.webtoapp.core.feature.FeatureIds
import com.webtoapp.core.feature.LITE_FEATURE_API
import com.webtoapp.core.logging.AppLogger
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.zip.ZipOutputStream

object FeaturePackMerger {
    private const val TAG = "FeaturePackMerger"

    data class MergeResult(
        val enabledJson: ByteArray,
        val extraEntries: List<Pair<String, ByteArray>>,
        val missingFeatures: List<String>
    )

    fun prepare(
        context: android.content.Context,
        plan: CapabilityPlan,
        logger: BuildLogger? = null
    ): MergeResult {
        val extra = ArrayList<Pair<String, ByteArray>>()
        val missing = ArrayList<String>()
        val enabledFeatures = ArrayList<EnabledFeature>()

        for (id in plan.features) {
            val packDir = resolvePackDir(context, id)
            if (packDir == null || !packDir.isDirectory) {
                if (id == FeatureIds.COMPAT) {
                    logger?.log("feature-compat pack not packaged yet; shell still embeds full runtime (transition)")
                    AppLogger.i(TAG, "compat pack missing — transitional fat shell path")
                    continue
                }
                missing += id
                logger?.warn("Missing feature pack: $id")
                continue
            }
            val manifestFile = File(packDir, "feature.json")
            if (!manifestFile.isFile) {
                missing += id
                continue
            }
            val assetDir = "features/$id"
            packDir.walkTopDown().filter { it.isFile }.forEach { file ->
                val rel = file.relativeTo(packDir).invariantSeparatorsPath
                val bytes = file.readBytes()
                extra += "$assetDir/$rel" to bytes
            }
            enabledFeatures += EnabledFeature(
                id = id,
                version = 1,
                dir = assetDir
            )
            logger?.log("Feature pack staged: $id (${packDir.listFiles()?.size ?: 0} files)")
        }

        val enabled = EnabledFeaturesFile(
            liteApi = LITE_FEATURE_API,
            features = enabledFeatures
        )
        return MergeResult(
            enabledJson = toEnabledJson(enabled).toByteArray(Charsets.UTF_8),
            extraEntries = extra,
            missingFeatures = missing
        )
    }

    fun shouldStripBloatEntry(entryName: String, plan: CapabilityPlan): Boolean {
        if (entryName.startsWith("org/bouncycastle/pqc/")) return true
        if (entryName.contains("lowmcL") && entryName.endsWith(".properties")) return true
        if (plan.liteOnly) {
            if (entryName.startsWith("lib/") && (
                    entryName.endsWith("libnode_bridge.so") ||
                        entryName.endsWith("libnode_launcher.so") ||
                        entryName.endsWith("libgo_exec_loader.so") ||
                        entryName.endsWith("libphp.so") ||
                        entryName.endsWith("libpython3.so") ||
                        entryName.endsWith("libmusl-linker.so") ||
                        entryName.endsWith("libapk_optimizer.so") ||
                        entryName.endsWith("libcrypto_optimized.so") ||
                        entryName.endsWith("libperf_engine.so") ||
                        entryName.endsWith("libbrowser_kernel.so") ||
                        entryName.endsWith("libsys_optimizer.so") ||
                        entryName.endsWith("libhardware_control.so")
                    )
            ) {
                return true
            }
        }
        return false
    }

    private fun resolvePackDir(context: android.content.Context, id: String): File? {
        val cached = File(context.filesDir, "feature_packs/$id")
        if (cached.isDirectory && File(cached, "feature.json").isFile) return cached
        val assetDir = "features/$id"
        return try {
            val out = File(context.cacheDir, "feature_packs_assets/$id")
            if (extractAssetDir(context, assetDir, out)) out else null
        } catch (_: Exception) {
            null
        }
    }

    private fun extractAssetDir(
        context: android.content.Context,
        assetDir: String,
        dest: File
    ): Boolean {
        val am = context.assets
        val children = try {
            am.list(assetDir)
        } catch (_: Exception) {
            null
        } ?: return false
        if (children.isEmpty()) return false
        dest.mkdirs()
        for (name in children) {
            val childAsset = "$assetDir/$name"
            val childDest = File(dest, name)
            val sub = try {
                am.list(childAsset)
            } catch (_: Exception) {
                null
            }
            if (sub != null && sub.isNotEmpty()) {
                extractAssetDir(context, childAsset, childDest)
            } else {
                childDest.parentFile?.mkdirs()
                am.open(childAsset).use { input ->
                    childDest.outputStream().use { output -> input.copyTo(output) }
                }
            }
        }
        return File(dest, "feature.json").isFile || dest.listFiles()?.isNotEmpty() == true
    }

    private fun toEnabledJson(enabled: EnabledFeaturesFile): String {
        val root = JSONObject()
        root.put("liteApi", enabled.liteApi)
        val arr = JSONArray()
        for (f in enabled.features) {
            arr.put(
                JSONObject()
                    .put("id", f.id)
                    .put("version", f.version)
                    .put("dir", f.dir)
            )
        }
        root.put("features", arr)
        return root.toString()
    }

    fun writeEntries(zipOut: ZipOutputStream, entries: List<Pair<String, ByteArray>>) {
        for ((name, data) in entries) {
            ZipUtils.writeEntryDeflated(zipOut, name, data)
        }
    }
}
