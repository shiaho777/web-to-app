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
import java.util.zip.ZipOutputStream

/**
 * Makes the on-demand Gecko runtime loadable inside the host app.
 *
 * The host APK ships the GeckoView Java classes but none of its native payload:
 * every gecko `.so` plus `omni.ja` is excluded at packaging time and downloaded
 * on demand into `files/gecko_engine/` (exported APKs get them re-injected into
 * `lib/<abi>/` + `assets/` by ApkBuilder, so they need nothing from here).
 *
 * Three gaps have to be bridged before `GeckoRuntime.create` can work:
 *
 * 1. `GeckoLoader` resolves `libmozglue.so` exclusively through
 *    `BaseDexClassLoader.findLibrary` — it never looks at arbitrary
 *    directories. The downloaded ABI dir is grafted onto the process class
 *    loader's native library path via [ensureNativeLibsVisible].
 *
 * 2. `dlopen(libxul.so)` resolves its DT_NEEDED deps (`libgkcodecs.so`, …)
 *    by soname against libraries already loaded in the linker namespace —
 *    the class loader graft does not extend that namespace's search path.
 *    [preloadDownloadedLibs] therefore `System.load`s every downloaded `.so`
 *    (except `libxul.so` itself, which mozglue's own loader must open) so the
 *    deps are resident when Gecko performs its native load.
 *
 * 3. `GeckoThread` hard-codes `-greomni` to
 *    `Context.getPackageResourcePath()` — normally the base APK, whose nested
 *    `assets/omni.ja` entry `Omnijar::InitOne` unwraps and opens. The host APK
 *    has no such entry, and pointing `-greomni` at the bare `omni.ja` file
 *    does NOT work (the XPCOM resource canary fails and XRE_main aborts with
 *    "The installation seems to be incomplete"). [runtimeContext] therefore
 *    points the resource path at a container zip that reproduces the APK
 *    layout — a single STORED `assets/omni.ja` entry — which Gecko opens
 *    exactly like the bundled case.
 *
 * Gecko's `:tab*`/`:gpu`/`:socket` etc. children start through Android's
 * service machinery with a fresh class loader and namespace, which is why both
 * steps are driven from `Application.attachBaseContext` (injection always,
 * preload only in gecko child processes) rather than lazily at engine
 * creation.
 *
 * Logging note: attachBaseContext runs before AppLogger.init, so this file
 * logs straight to logcat — these are exactly the breadcrumbs needed when a
 * native load fails inside a Gecko child process.
 */
object GeckoRuntimeProvisioner {

    private const val TAG = "GeckoRuntimeProvisioner"

    /** Container zip inside the engine dir, holding `assets/omni.ja`. */
    private const val OMNI_CONTAINER = "omni_container.zip"
    private const val OMNI_CONTAINER_META = "omni_container.meta"
    private const val OMNI_ENTRY = "assets/omni.ja"

    // android.util.Log throws "not mocked" under host-side unit tests — wrap
    // so the diagnostics still reach logcat on device without breaking tests.
    private fun logI(msg: String) = runCatching { Log.i(TAG, msg) }
    private fun logW(msg: String, t: Throwable? = null) = runCatching { Log.w(TAG, msg, t) }
    private fun logE(msg: String, t: Throwable? = null) = runCatching { Log.e(TAG, msg, t) }

    @Volatile
    private var nativeLibsInjected = false

    @Volatile
    private var nativeLibsPreloaded = false

    @Volatile
    private var injectedAbiDir: File? = null

    /**
     * Grafts the downloaded Gecko ABI dir onto this process's class loader so
     * `findLibrary("mozglue")` resolves. Idempotent and a no-op when the APK
     * already carries the libs (generated apps) or the engine was never
     * downloaded. Safe to call from attachBaseContext: it touches no
     * Application instance state and loads nothing into the linker.
     */
    fun ensureNativeLibsVisible(context: Context) {
        if (nativeLibsInjected) return
        val classLoader = context.classLoader as? BaseDexClassLoader ?: return
        if (classLoader.findLibrary("mozglue") != null) {
            nativeLibsInjected = true
            return
        }

        // attachBaseContext runs before the Application is fully bound —
        // applicationContext is still null there, so always use the context
        // itself (filesDir/classLoader/applicationInfo are all valid on it).
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

    /**
     * `System.load`s the downloaded dependency `.so`s so later `dlopen`s inside
     * Gecko resolve DT_NEEDED sonames from the resident set. Only meaningful
     * when the libs come from the download dir; bundled APKs resolve deps
     * through the regular native library path.
     */
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
            // Everything pulls in mozglue — get it resident first.
            .sortedBy { it.name != "libmozglue.so" }
            .toMutableList()

        // Retry passes resolve the dep DAG without us hard-coding load order.
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
                    // A DT_NEEDED dep may not be resident yet — retry next pass.
                    logW("Gecko dep load failed: ${so.name}: $t")
                }
            }
        }
        // libplugin-container/libminidump_analyzer are standalone executables,
        // not dlopen-able libs — never loading is expected for them.
        val stuck = pending.filter {
            it.name != "libplugin-container.so" && it.name != "libminidump_analyzer.so"
        }
        if (stuck.isNotEmpty()) {
            logW("Gecko native deps left unloaded: ${stuck.joinToString { it.name }}")
        }
    }

    /**
     * Full provisioning for the process that is about to `GeckoRuntime.create`:
     * lib path graft + dependency preload, then the `-greomni` context.
     */
    fun prepareRuntime(context: Context): Context {
        preloadDownloadedLibs(context)
        return runtimeContext(context)
    }

    /**
     * True when this process is one of Gecko's own children (`:tab*`, `:gpu`,
     * `:socket`, `:media`, `:gmplugin`, `:utility`, `:ipdlunittest`, `:rdd`) or
     * the `:crashhelper` service — each of them dlopens gecko natives on a
     * fresh namespace, so they need the preload from attachBaseContext.
     */
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

    /**
     * The context `GeckoRuntime.create` should see. Plain context when the APK
     * bundles `assets/omni.ja`; otherwise a wrapper whose
     * `getPackageResourcePath()` points at a zip containing a STORED
     * `assets/omni.ja` entry — the same nested layout Gecko unwraps from an
     * APK — so the native `-greomni` argument resolves to a readable omnijar.
     */
    private fun runtimeContext(context: Context): Context {
        return try {
            context.assets.open(EngineFileManager.GECKO_OMNI_JA).close()
            context
        } catch (_: Exception) {
            val fileManager = EngineFileManager(context)
            val omniJa = fileManager.getOmniJaFile(EngineType.GECKOVIEW)
            val container = ensureOmniContainer(omniJa)
            if (container != null) {
                newPreviewContext(context, container.absolutePath)
            } else {
                context
            }
        }
    }

    /**
     * Repacks `files/gecko_engine/omni.ja` into `omni_container.zip` holding a
     * STORED `assets/omni.ja` entry — uncompressed so nsZipArchive can mmap the
     * inner archive in place. Rebuilt whenever the source omni.ja changes
     * (tracked via a length+mtime sidecar next to it).
     */
    internal fun ensureOmniContainer(omniJa: File): File? {
        if (!omniJa.isFile || omniJa.length() == 0L) return null
        val dir = omniJa.parentFile ?: return null
        val out = File(dir, OMNI_CONTAINER)
        val meta = File(dir, OMNI_CONTAINER_META)
        val stamp = "${omniJa.length()}:${omniJa.lastModified()}"
        try {
            if (out.isFile && meta.isFile && meta.readText().trim() == stamp) {
                return out
            }
        } catch (_: Exception) {
            // fall through and rebuild
        }

        val tmp = File(dir, "$OMNI_CONTAINER.tmp")
        return try {
            writeNestedOmniZip(tmp, omniJa)
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

    internal fun writeNestedOmniZip(out: File, omniJa: File) {
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

    /**
     * Appends [libDir] to the class loader's native library path elements.
     * `DexPathList` element types are package-private, so correctly-typed
     * instances are obtained by letting a throwaway `PathClassLoader` create
     * them: its own native search path is [libDir], which yields exactly the
     * directory element(s) we can move onto the real loader.
     */
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

    /**
     * Returns the container zip path for `getPackageResourcePath()` — the
     * value GeckoThread bakes into `-greomni` — while delegating everything
     * else to the real context. `getApplicationContext()` returns the wrapper
     * itself so `GeckoAppShell.sApplicationContext` keeps these overrides for
     * the lifetime of the runtime.
     *
     * That self-return is unsafe for [registerComponentCallbacks] on Android 12
     * and below: the framework method is `getApplicationContext().register…`,
     * so GeckoRuntime.create's memory-callback registration recurses until the
     * stack overflows (#1274). Forward those two calls to the real Application,
     * which stores the callback itself.
     */
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
