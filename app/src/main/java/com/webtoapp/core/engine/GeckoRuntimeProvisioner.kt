package com.webtoapp.core.engine

import android.content.ComponentCallbacks
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.util.Log
import com.webtoapp.core.engine.download.EngineFileManager
import dalvik.system.BaseDexClassLoader
import dalvik.system.PathClassLoader
import java.io.File
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

object GeckoRuntimeProvisioner {

    private const val TAG = "GeckoRuntimeProvisioner"

    /** Container zip inside the engine dir, holding `assets/omni.ja`. */
    private const val OMNI_CONTAINER = "omni_container.zip"
    private const val OMNI_CONTAINER_META = "omni_container.meta"
    private const val OMNI_ENTRY = "assets/omni.ja"
    private const val WEB_EXTENSIONS_PATH = "assets/web_extensions/"

    private fun logI(msg: String) = runCatching { Log.i(TAG, msg) }
    private fun logW(msg: String, t: Throwable? = null) = runCatching { Log.w(TAG, msg, t) }
    private fun logE(msg: String, t: Throwable? = null) = runCatching { Log.e(TAG, msg, t) }

    @Volatile
    private var nativeLibsInjected = false

    @Volatile
    private var nativeLibsPreloaded = false

    @Volatile
    private var injectedAbiDir: File? = null

    fun ensureNativeLibsVisible(context: Context) {
        if (nativeLibsInjected) return
        val classLoader = context.classLoader as? BaseDexClassLoader ?: return
        if (classLoader.findLibrary("mozglue") != null) {
            nativeLibsInjected = true
            return
        }

        val libDir = resolveAbiLibDir(context) ?: return

        synchronized(this) {
            if (nativeLibsInjected) return
            if (classLoader.findLibrary("mozglue") != null) {
                nativeLibsInjected = true
                return
            }
            val injected = try {
                injectLibraryDir(classLoader, context.applicationInfo.sourceDir, libDir)
            } catch (t: Throwable) {
                logE("Failed to graft Gecko lib dir ${libDir.absolutePath}", t)
                false
            }
            if (injected) {
                nativeLibsInjected = true
                injectedAbiDir = libDir
                logI("Grafted Gecko native libs onto class loader: ${libDir.absolutePath}")
            }
        }
    }

    fun preloadDownloadedLibs(context: Context) {
        if (nativeLibsPreloaded) return
        ensureNativeLibsVisible(context)
        val libDir = injectedAbiDir ?: return

        synchronized(this) {
            if (nativeLibsPreloaded) return
            nativeLibsPreloaded = true
        }

        val pending = (libDir.listFiles { f -> f.isFile && f.extension == "so" }
            ?: emptyArray())
            .filter { it.name != "libxul.so" }
            .sortedBy { it.name != "libmozglue.so" }
            .toMutableList()

        var progress = true
        var passes = 0
        while (pending.isNotEmpty() && progress && passes < 8) {
            progress = false
            passes++
            val it = pending.iterator()
            while (it.hasNext()) {
                val so = it.next()
                try {
                    System.load(so.absolutePath)
                    it.remove()
                    progress = true
                } catch (t: Throwable) {
                    logW("Gecko dep load failed: ${so.name}: $t")
                }
            }
        }
        val stuck = pending.filter {
            it.name != "libplugin-container.so" && it.name != "libminidump_analyzer.so"
        }
        if (stuck.isNotEmpty()) {
            logW("Gecko native deps left unloaded: ${stuck.joinToString { it.name }}")
        }
    }

    fun prepareRuntime(context: Context): Context {
        preloadDownloadedLibs(context)
        return runtimeContext(context)
    }

    fun isGeckoChildProcess(): Boolean {
        val name = try {
            File("/proc/self/cmdline").readBytes().let {
                String(it, 0, it.indexOf(0).let { i -> if (i >= 0) i else it.size })
            }
        } catch (_: Throwable) {
            return false
        }
        val suffix = name.substringAfter(':', "")
        return suffix.isNotEmpty() && (
            suffix.startsWith("tab") || suffix in setOf(
                "gpu", "socket", "media", "gmplugin", "utility",
                "ipdlunittest", "rdd", "crashhelper"
            )
            )
    }

    private fun runtimeContext(context: Context): Context {
        return try {
            context.assets.open(EngineFileManager.GECKO_OMNI_JA).close()
            context
        } catch (_: Exception) {
            val fileManager = EngineFileManager(context)
            val omniJa = fileManager.getOmniJaFile(EngineType.GECKOVIEW)
            val container = ensureOmniContainer(omniJa, File(context.applicationInfo.sourceDir))
            if (container != null) {
                newPreviewContext(context, container.absolutePath)
            } else {
                context
            }
        }
    }

    @Synchronized
    internal fun ensureOmniContainer(omniJa: File, hostApk: File): File? {
        if (!omniJa.isFile || omniJa.length() == 0L) return null
        val dir = omniJa.parentFile ?: return null
        val out = File(dir, OMNI_CONTAINER)
        val meta = File(dir, OMNI_CONTAINER_META)
        val omniStamp = "${omniJa.length()}:${omniJa.lastModified()}"
        val apkStamp = "${hostApk.length()}:${hostApk.lastModified()}:${hostApk.path}"
        val stamp = "$omniStamp:$apkStamp"
        if (out.isFile && meta.isFile && runCatching { meta.readText().trim() }.getOrNull() == stamp) {
            return out
        }

        val tmp = File(dir, "$OMNI_CONTAINER.tmp")
        return try {
            writeNestedOmniZip(tmp, omniJa, hostApk)
            if (!tmp.renameTo(out)) {
                tmp.copyTo(out, overwrite = true)
                tmp.delete()
            }
            meta.writeText(stamp)
            logI("Built $OMNI_CONTAINER (${omniJa.length()} bytes omni.ja)")
            out
        } catch (t: Throwable) {
            logE("Failed to build omni.ja container", t)
            tmp.delete()
            null
        }
    }

    private fun writeNestedOmniZip(out: File, omniJa: File, hostApk: File) {
        val crc = CRC32()
        omniJa.inputStream().use { input ->
            val buf = ByteArray(256 * 1024)
            while (true) {
                val n = input.read(buf)
                if (n < 0) break
                crc.update(buf, 0, n)
            }
        }
        ZipOutputStream(out.outputStream().buffered()).use { zos ->
            val entry = ZipEntry(OMNI_ENTRY).apply {
                method = ZipEntry.STORED
                size = omniJa.length()
                compressedSize = omniJa.length()
                this.crc = crc.value
                time = 0L
            }
            zos.putNextEntry(entry)
            omniJa.inputStream().use { it.copyTo(zos) }
            zos.closeEntry()
            ZipFile(hostApk).use { apk ->
                for (asset in apk.entries()) {
                    if (asset.isDirectory || !asset.name.startsWith(WEB_EXTENSIONS_PATH)) continue
                    zos.putNextEntry(ZipEntry(asset.name).apply { time = 0L })
                    apk.getInputStream(asset).use { it.copyTo(zos) }
                    zos.closeEntry()
                }
            }
        }
    }

    private fun resolveAbiLibDir(context: Context): File? {
        val fileManager = EngineFileManager(context)
        if (!fileManager.isEngineDownloaded(EngineType.GECKOVIEW)) return null
        val libRoot = File(fileManager.getEngineDir(EngineType.GECKOVIEW), "lib")
        for (abi in Build.SUPPORTED_ABIS) {
            val dir = File(libRoot, abi)
            if (dir.isDirectory && dir.listFiles()?.any { it.isFile && it.extension == "so" } == true) {
                return dir
            }
        }
        logW("Gecko engine marked downloaded but no ABI lib dir present")
        return null
    }

    private fun injectLibraryDir(
        classLoader: BaseDexClassLoader,
        dexPath: String,
        libDir: File
    ): Boolean {
        val helper = PathClassLoader(dexPath, libDir.absolutePath, classLoader)

        val pathListField = BaseDexClassLoader::class.java
            .getDeclaredField("pathList").apply { isAccessible = true }
        val appPathList = pathListField.get(classLoader)
        val elementsField = appPathList.javaClass
            .getDeclaredField("nativeLibraryPathElements").apply { isAccessible = true }

        val helperElements = elementsField.get(pathListField.get(helper))
        val appElements = elementsField.get(appPathList)
        when {
            appElements is Array<*> && helperElements is Array<*> -> {
                val merged = java.lang.reflect.Array.newInstance(
                    appElements.javaClass.componentType!!,
                    appElements.size + helperElements.size
                )
                for (i in appElements.indices) {
                    java.lang.reflect.Array.set(merged, i, appElements[i])
                }
                for (i in helperElements.indices) {
                    java.lang.reflect.Array.set(merged, appElements.size + i, helperElements[i])
                }
                elementsField.set(appPathList, merged)
            }
            appElements is MutableList<*> && helperElements is List<*> -> {
                @Suppress("UNCHECKED_CAST")
                (appElements as MutableList<Any?>).addAll(helperElements)
            }
            else -> return false
        }
        return true
    }

    internal fun newPreviewContext(base: Context, resourcePath: String): Context =
        GeckoPreviewContext(base, resourcePath)

    private class GeckoPreviewContext(
        base: Context,
        private val resourcePathOverride: String
    ) : ContextWrapper(base) {
        override fun getApplicationContext(): Context = this
        override fun getPackageResourcePath(): String = resourcePathOverride

        override fun registerComponentCallbacks(callback: ComponentCallbacks) {
            baseContext.applicationContext.registerComponentCallbacks(callback)
        }

        override fun unregisterComponentCallbacks(callback: ComponentCallbacks) {
            baseContext.applicationContext.unregisterComponentCallbacks(callback)
        }
    }
}
