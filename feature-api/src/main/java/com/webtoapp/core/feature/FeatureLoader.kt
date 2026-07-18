package com.webtoapp.core.feature

import android.app.Application
import android.content.Context
import android.util.Log
import android.os.Build
import dalvik.system.InMemoryDexClassLoader
import dalvik.system.PathClassLoader
import java.nio.ByteBuffer
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

object FeatureLoader {
    private const val TAG = "FeatureLoader"
    private const val ENABLED_PATH = "features/enabled.json"

    @Volatile
    private var loaded = false

    val registry = FeatureRegistry()

    private val featureLoaders = mutableListOf<ClassLoader>()
    private val installedIds = mutableSetOf<String>()

    fun isLoaded(): Boolean = loaded

    fun isInstalled(featureId: String): Boolean = synchronized(installedIds) {
        featureId in installedIds
    }

    fun loadClass(name: String): Class<*>? {
        try {
            return Class.forName(name)
        } catch (_: ClassNotFoundException) {
        } catch (_: NoClassDefFoundError) {
        }
        val loaders = synchronized(featureLoaders) { featureLoaders.toList() }
        for (loader in loaders) {
            try {
                return Class.forName(name, false, loader)
            } catch (_: ClassNotFoundException) {
            } catch (_: NoClassDefFoundError) {
            }
        }
        return null
    }

    @Synchronized
    fun loadEnabled(app: Application, configView: FeatureConfigView = EmptyFeatureConfigView) {
        if (loaded) return
        try {
            val enabled = readEnabled(app) ?: EnabledFeaturesFile()
            val items = enabled.features.mapNotNull { item ->
                val root = resolveFeatureRoot(app, item.dir) ?: return@mapNotNull null
                val manifest = readManifest(File(root, "feature.json")) ?: return@mapNotNull null
                Triple(item, root, manifest)
            }.sortedBy { it.third.loadOrder }
            for ((item, root, manifest) in items) {
                try {
                    loadOne(app, item, root, manifest, configView)
                } catch (t: Throwable) {
                    Log.e(TAG, "Feature pack failed: ${manifest.id}", t)
                }
            }
            loaded = true
            Log.i(TAG, "Loaded ${items.size} feature pack(s)")
        } catch (t: Throwable) {
            Log.e(TAG, "Feature load failed", t)
            loaded = true
        }
    }

    private fun loadOne(
        app: Application,
        item: EnabledFeature,
        root: File,
        manifest: FeatureManifest,
        configView: FeatureConfigView
    ) {
        if (manifest.minLiteApi > LITE_FEATURE_API) {
            throw IllegalStateException(
                "Feature ${manifest.id} requires liteApi=${manifest.minLiteApi}, have $LITE_FEATURE_API"
            )
        }
        val dexFiles = manifest.dex.map { File(root, it) }.filter { it.isFile }
        if (dexFiles.isEmpty()) {
            Log.w(TAG, "No dex for feature ${manifest.id}")
            return
        }
        val optimized = File(app.codeCacheDir, "features/${manifest.id}").apply { mkdirs() }
        val extracted = dexFiles.map { src ->
            val dest = File(optimized, src.name)
            if (!dest.isFile || dest.length() != src.length() || dest.lastModified() < src.lastModified()) {
                if (dest.exists()) {
                    dest.setWritable(true)
                }
                src.copyTo(dest, overwrite = true)
            }
            ensureDexNotWritable(dest)
            dest
        }
        val loader = createDexClassLoader(extracted, app.classLoader)
        synchronized(featureLoaders) {
            featureLoaders.add(loader)
        }
        for (lib in manifest.nativeLibs) {
            runCatching { System.loadLibrary(lib) }
                .onFailure { Log.w(TAG, "loadLibrary($lib) failed: ${it.message}") }
        }
        val module = runCatching {
            val moduleClass = Class.forName(manifest.entryClass, true, loader)
            if (!FeatureModule::class.java.isAssignableFrom(moduleClass)) {
                null
            } else {
                moduleClass.getDeclaredConstructor().newInstance() as FeatureModule
            }
        }.onFailure {
            Log.w(TAG, "Feature entry not a FeatureModule: ${manifest.entryClass}")
        }.getOrNull()

        if (module != null) {
            val files = object : FeatureFileAccess {
                override fun featureRoot(featureId: String): File = root
                override fun openAsset(path: String) = app.assets.open(path)
            }
            val ctx = FeatureContext(app, registry, configView, files)
            module.install(ctx)
            synchronized(installedIds) {
                installedIds.add(module.id)
                installedIds.add(manifest.id)
                installedIds.add(item.id)
            }
            Log.i(TAG, "Installed feature ${module.id} v${module.version}")
        } else {
            synchronized(installedIds) {
                installedIds.add(manifest.id)
                installedIds.add(item.id)
            }
            Log.i(TAG, "Registered soft-load pack ${manifest.id}")
        }
    }

    private fun resolveFeatureRoot(app: Context, dir: String): File? {
        val direct = File(app.filesDir, dir)
        if (direct.isDirectory) return direct
        val assetBase = dir.removePrefix("/").removePrefix("assets/")
        val cacheRoot = File(app.codeCacheDir, assetBase)
        if (cacheRoot.isDirectory && cacheRoot.list()?.isNotEmpty() == true) {
            return cacheRoot
        }
        return try {
            extractAssetDir(app, assetBase, cacheRoot)
            cacheRoot.takeIf { it.isDirectory }
        } catch (e: Exception) {
            Log.w(TAG, "extract feature assets failed: $assetBase", e)
            null
        }
    }

    private fun extractAssetDir(app: Context, assetDir: String, dest: File) {
        dest.mkdirs()
        val am = app.assets
        val children = try {
            am.list(assetDir) ?: emptyArray()
        } catch (_: Exception) {
            emptyArray()
        }
        if (children.isEmpty()) {
            copyAssetFile(app, assetDir, dest)
            return
        }
        for (name in children) {
            val childAsset = if (assetDir.isEmpty()) name else "$assetDir/$name"
            val childDest = File(dest, name)
            val sub = try {
                am.list(childAsset)
            } catch (_: Exception) {
                null
            }
            if (sub != null && sub.isNotEmpty()) {
                extractAssetDir(app, childAsset, childDest)
            } else {
                childDest.parentFile?.mkdirs()
                am.open(childAsset).use { input ->
                    FileOutputStream(childDest).use { output -> input.copyTo(output) }
                }
            }
        }
    }

    private fun copyAssetFile(app: Context, assetPath: String, destDir: File) {
        val name = assetPath.substringAfterLast('/')
        if (name.isBlank()) return
        destDir.mkdirs()
        val dest = File(destDir, name)
        app.assets.open(assetPath).use { input ->
            FileOutputStream(dest).use { output -> input.copyTo(output) }
        }
    }

    private fun readEnabled(app: Context): EnabledFeaturesFile? {
        return try {
            app.assets.open(ENABLED_PATH).bufferedReader().use { reader ->
                parseEnabled(JSONObject(reader.readText()))
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun parseEnabled(json: JSONObject): EnabledFeaturesFile {
        val arr = json.optJSONArray("features") ?: JSONArray()
        val features = mutableListOf<EnabledFeature>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            features += EnabledFeature(
                id = o.getString("id"),
                version = o.optInt("version", 1),
                dir = o.optString("dir", "features/${o.getString("id")}")
            )
        }
        return EnabledFeaturesFile(
            liteApi = json.optInt("liteApi", LITE_FEATURE_API),
            features = features
        )
    }

    private fun readManifest(file: File): FeatureManifest? {
        if (!file.isFile) return null
        return try {
            parseManifest(JSONObject(file.readText()))
        } catch (e: Exception) {
            Log.w(TAG, "parse feature.json failed: ${file.path}", e)
            null
        }
    }

    private fun parseManifest(json: JSONObject): FeatureManifest {
        val depends = mutableListOf<String>()
        val depArr = json.optJSONArray("dependsOn")
        if (depArr != null) {
            for (i in 0 until depArr.length()) {
                depends += depArr.getString(i)
            }
        }
        val dex = mutableListOf<String>()
        val dexArr = json.optJSONArray("dex")
        if (dexArr != null) {
            for (i in 0 until dexArr.length()) {
                dex += dexArr.getString(i)
            }
        } else {
            dex += "classes.dex"
        }
        val nativeLibs = mutableListOf<String>()
        val nativeArr = json.optJSONArray("nativeLibs")
        if (nativeArr != null) {
            for (i in 0 until nativeArr.length()) {
                nativeLibs += nativeArr.getString(i)
            }
        }
        return FeatureManifest(
            id = json.getString("id"),
            version = json.optInt("version", 1),
            minLiteApi = json.optInt("minLiteApi", LITE_FEATURE_API),
            dependsOn = depends,
            entryClass = json.getString("entryClass"),
            loadOrder = json.optInt("loadOrder", 100),
            dex = dex,
            nativeLibs = nativeLibs,
            manifestDelta = json.optString("manifestDelta")
                .takeIf { it.isNotBlank() && it != "null" },
            assetsPrefix = json.optString("assetsPrefix")
                .takeIf { it.isNotBlank() && it != "null" }
        )
    }


    private fun ensureDexNotWritable(file: File) {
        if (!file.isFile) return
        if (file.canWrite()) {
            file.setWritable(false)
        }
        if (file.canWrite()) {
            file.setReadOnly()
        }
        if (file.canWrite()) {
            Log.w(TAG, "DEX still writable after harden: ${file.absolutePath}")
        }
    }

    private fun createDexClassLoader(dexFiles: List<File>, parent: ClassLoader): ClassLoader {
        if (Build.VERSION.SDK_INT >= 27) {
            try {
                val buffers = dexFiles.map { file ->
                    ByteBuffer.wrap(file.readBytes())
                }.toTypedArray()
                return InMemoryDexClassLoader(buffers, parent)
            } catch (e: Exception) {
                Log.w(TAG, "InMemoryDexClassLoader failed, fallback PathClassLoader: ${e.message}")
            }
        }
        for (file in dexFiles) {
            ensureDexNotWritable(file)
        }
        val dexPath = dexFiles.joinToString(File.pathSeparator) { it.absolutePath }
        return PathClassLoader(dexPath, parent)
    }

    private object EmptyFeatureConfigView : FeatureConfigView {
        override fun appType(): String = "WEB"
        override fun engineType(): String = "SYSTEM_WEBVIEW"
        override fun rawJson(): String? = null
        override fun <T> get(key: String, clazz: Class<T>): T? = null
    }
}
