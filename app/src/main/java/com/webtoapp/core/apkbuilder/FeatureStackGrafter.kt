package com.webtoapp.core.apkbuilder

import com.webtoapp.core.logging.AppLogger
import java.io.ByteArrayInputStream
import java.security.MessageDigest
import java.util.zip.ZipInputStream

/**
 * Registry mapping export config flags to feature-stack ids (issue #1115).
 *
 * Stack bundles live in the host's assets at `stacks/<id>.zip` (synced by
 * `:app:syncStackBundles` from `feature-stacks/<id>/build/outputs/stack/`).
 * A generated APK only carries a stack's DEX/resources/manifest entries when
 * the app config enables the feature — disabled features cost zero bytes.
 */
object FeatureStacks {
    const val ASSET_DIR = "stacks"

    /** Stack ids enabled by this export config, in deterministic order. */
    fun enabledFor(config: ApkConfig): List<String> = buildList {
        if (config.adsEnabled) add("admob")
    }
}

/**
 * Parsed feature-stack bundle (the zip produced by a `feature-stacks/<id>`
 * module's `bundle*Stack` task):
 *
 * ```
 * dex/classes*.dex  – feature code, multidex-ordered
 * stack.arsc        – resource package compiled at a fixed id (0x80+)
 * res/…             – compiled resource files referenced by stack.arsc
 * manifest.xml      – curated <fragment> document
 * stack.json        – {"id","version","packageId","resPrefix"}
 * ```
 */
class FeatureStackBundle private constructor(
    val id: String,
    val packageId: Int,
    val resPrefix: String,
    val dexes: List<Pair<String, ByteArray>>,
    val resFiles: Map<String, ByteArray>,
    val stackArsc: ByteArray?,
    val manifestXml: ByteArray?,
    val sha256: String
) {
    companion object {
        private const val TAG = "FeatureStackBundle"

        fun parse(bytes: ByteArray): FeatureStackBundle {
            var stackJson: ByteArray? = null
            var arsc: ByteArray? = null
            var manifest: ByteArray? = null
            val dexes = mutableListOf<Pair<String, ByteArray>>()
            val resFiles = linkedMapOf<String, ByteArray>()

            ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
                while (true) {
                    val e = zip.nextEntry ?: break
                    if (e.isDirectory) continue
                    val data = zip.readBytes()
                    when {
                        e.name == "stack.json" -> stackJson = data
                        e.name == "stack.arsc" -> arsc = data
                        e.name == "manifest.xml" -> manifest = data
                        e.name.startsWith("dex/") &&
                            e.name.removePrefix("dex/").matches(Regex("classes\\d*\\.dex")) ->
                            dexes += e.name.removePrefix("dex/") to data
                        e.name.startsWith("res/") -> resFiles[e.name] = data
                    }
                }
            }
            requireNotNull(stackJson) { "stack bundle missing stack.json" }
            require(dexes.isNotEmpty()) { "stack bundle has no dex entries" }

            val meta = parseStackJson(stackJson.decodeToString())
            val prefix = meta.resPrefix.ifEmpty { "res/" }
            require(prefix.endsWith("/")) { "resPrefix must end with '/'" }
            // Deterministic dex order: classes.dex, classes2.dex, …
            dexes.sortBy { it.first.removePrefix("classes").removeSuffix(".dex").toIntOrNull() ?: 1 }

            val sha = MessageDigest.getInstance("SHA-256")
                .digest(bytes).joinToString("") { "%02x".format(it) }
            AppLogger.d(TAG, "loaded stack '${meta.id}': ${dexes.size} dex, " +
                "${resFiles.size} res files, arsc=${arsc?.size ?: 0}B")
            return FeatureStackBundle(
                id = meta.id,
                packageId = meta.packageId.removePrefix("0x").toIntOrNull(16) ?: 0x80,
                resPrefix = prefix,
                dexes = dexes,
                resFiles = resFiles,
                stackArsc = arsc,
                manifestXml = manifest,
                sha256 = sha
            )
        }

        private data class StackMeta(val id: String, val packageId: String, val resPrefix: String)

        private fun parseStackJson(json: String): StackMeta {
            fun field(name: String): String =
                Regex(""""$name"\s*:\s*"([^"]*)"""").find(json)?.groupValues?.get(1) ?: ""
            return StackMeta(field("id"), field("packageId"), field("resPrefix"))
        }
    }
}

/**
 * Grafts [FeatureStackBundle]s into the generated APK during `modifyApk`:
 *
 * - manifest fragment → [AxmlRebuilder.mergeManifestFragment]
 * - stack.arsc        → [ArscPackageGrafter.graft]
 * - dex/res entries   → appended as new zip entries after the entry loop
 */
class FeatureStackGrafter(
    private val axmlRebuilder: AxmlRebuilder,
    private val arscGrafter: ArscPackageGrafter
) {
    private val rewrittenResPaths = mutableMapOf<String, Map<String, String>>()

    /**
     * Merge the bundle's manifest fragment; no-op without manifest.xml.
     * [vars] feeds `${key}` substitution on top of the built-in
     * `applicationId` variable.
     */
    fun graftManifest(
        axmlBytes: ByteArray,
        bundle: FeatureStackBundle,
        packageName: String,
        vars: Map<String, String> = emptyMap()
    ): ByteArray {
        val fragment = bundle.manifestXml ?: return axmlBytes
        return axmlRebuilder.mergeManifestFragment(
            axmlBytes, fragment, vars + ("applicationId" to packageName)
        )
    }

    /** Append the bundle's resource package; records res path rewrites. */
    fun graftResourceTable(arscBytes: ByteArray, bundle: FeatureStackBundle): ByteArray {
        val stackArsc = bundle.stackArsc ?: return arscBytes
        val result = arscGrafter.graft(arscBytes, stackArsc, bundle.resPrefix)
        rewrittenResPaths[bundle.id] = result.resPaths
        return result.arsc
    }

    /**
     * Zip entries to append after the main entry loop: stack dex files named
     * after the highest existing `classes*.dex`, then the res files under
     * their arsc-rewritten (namespaced) paths.
     */
    fun extraEntries(
        bundles: List<FeatureStackBundle>,
        existingEntryNames: Set<String>
    ): List<Pair<String, ByteArray>> {
        var nextDex = existingEntryNames
            .mapNotNull { DEX_NAME.matchEntire(it)?.groupValues?.get(1)?.toIntOrNull() ?: 1 }
            .maxOrNull()?.plus(1) ?: 2
        val out = mutableListOf<Pair<String, ByteArray>>()
        bundles.forEach { bundle ->
            bundle.dexes.forEach { (_, bytes) ->
                out += "classes$nextDex.dex" to bytes
                nextDex++
            }
            val resMap = rewrittenResPaths[bundle.id].orEmpty()
            bundle.resFiles.forEach { (srcPath, bytes) ->
                val dest = resMap[srcPath] ?: bundle.resPrefix + srcPath.removePrefix("res/")
                if (dest !in existingEntryNames) out += dest to bytes
            }
        }
        return out
    }

    companion object {
        private val DEX_NAME = Regex("""classes(\d*)\.dex""")
    }
}
