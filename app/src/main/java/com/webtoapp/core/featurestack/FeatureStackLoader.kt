package com.webtoapp.core.featurestack

import android.content.Context
import com.webtoapp.core.featurestack.api.FeatureStack
import com.webtoapp.core.logging.AppLogger
import dalvik.system.DexClassLoader
import java.io.File
import java.io.FileNotFoundException
import java.util.concurrent.ConcurrentHashMap

/**
 * Loads optional feature-stack implementations.
 *
 * The heavy stacks (Google sign-in / Credential Manager, FCM / Firebase, Cronet
 * HTTP/3) are compiled into `assets/feature_stacks/<id>.dex` inside the shell
 * template instead of living in the main DEX, so ApkBuilder can leave a stack
 * out of a generated APK entirely when the corresponding build option is off —
 * a real size reduction rather than a config-only flag.
 *
 * Resolution order:
 *  1. The app class loader (host preview keeps the impls on the main classpath).
 *  2. `assets/feature_stacks/<id>.dex`, extracted to the code-cache dir and
 *     loaded through [DexClassLoader]. Absent asset → the stack was disabled at
 *     build time → null, and callers fail soft.
 *
 * Implementations are keyed by [stackId] and cached for the process lifetime.
 */
object FeatureStackLoader {

    private const val TAG = "FeatureStackLoader"
    private const val ASSET_DIR = "feature_stacks"

    private class Entry(val assetFile: String, val implClass: String)

    private val entries = mapOf(
        STACK_GOOGLE_SIGN_IN to Entry("google_signin.dex", "com.webtoapp.featurestack.signin.GoogleSignInStackImpl"),
        STACK_FCM to Entry("fcm.dex", "com.webtoapp.featurestack.fcm.FcmStackImpl"),
        STACK_CRONET to Entry("cronet.dex", "com.webtoapp.featurestack.cronet.CronetStackImpl")
    )

    const val STACK_GOOGLE_SIGN_IN = "google_signin"
    const val STACK_FCM = "fcm"
    const val STACK_CRONET = "cronet"

    /** `AssetManager` paths are relative to assets/ — no `assets/` prefix there. */
    const val ASSET_PREFIX = "$ASSET_DIR/"

    /** Zip entry prefix inside an APK (`ApkBuilder` filtering sees this form). */
    const val ZIP_ENTRY_PREFIX = "assets/$ASSET_DIR/"

    /** Stack id for a packaged APK zip-entry name, or null for non-stack entries. */
    fun stackIdForAsset(entryName: String): String? {
        if (!entryName.startsWith(ZIP_ENTRY_PREFIX)) return null
        val file = entryName.substring(ZIP_ENTRY_PREFIX.length)
        val id = file.removeSuffix(".dex")
        return if (entries.containsKey(id)) id else null
    }

    private sealed class Slot {
        class Ready(val impl: FeatureStack) : Slot()
        object Missing : Slot()
    }

    private val slots = ConcurrentHashMap<String, Slot>()

    @Suppress("UNCHECKED_CAST")
    fun <T : FeatureStack> load(context: Context, stackId: String): T? {
        when (val slot = slots[stackId]) {
            is Slot.Ready -> return slot.impl as? T
            Slot.Missing -> return null
            null -> Unit
        }
        val appContext = context.applicationContext
        val impl = synchronized(stackId.intern()) {
            when (val again = slots[stackId]) {
                is Slot.Ready -> return again.impl as? T
                Slot.Missing -> return null
                null -> Unit
            }
            val resolved = resolve(appContext, stackId)
            slots[stackId] = if (resolved != null) Slot.Ready(resolved) else Slot.Missing
            resolved
        }
        return impl as? T
    }

    fun isAvailable(context: Context, stackId: String): Boolean =
        load<FeatureStack>(context, stackId) != null

    /** Returns the already-loaded impl without triggering a load (for stop paths). */
    fun peek(stackId: String): FeatureStack? =
        (slots[stackId] as? Slot.Ready)?.impl

    private fun resolve(context: Context, stackId: String): FeatureStack? {
        val entry = entries[stackId] ?: return null
        // Host preview: the impl sits on the main classpath.
        try {
            val impl = Class.forName(entry.implClass).getDeclaredConstructor().newInstance()
                as? FeatureStack
            if (impl != null) {
                impl.init(context, MainFeatureRuntime)
                return impl
            }
        } catch (_: Throwable) {
        }
        // Generated APK: load the packaged dex asset.
        val dexFile = extractAsset(context, entry.assetFile) ?: return null
        return try {
            val optimizedDir = File(context.codeCacheDir, "feature_stacks_out").apply { mkdirs() }
            val loader = DexClassLoader(
                dexFile.absolutePath,
                optimizedDir.absolutePath,
                null,
                context.classLoader
            )
            val impl = loader.loadClass(entry.implClass).getDeclaredConstructor().newInstance()
                as? FeatureStack
            impl?.init(context, MainFeatureRuntime)
            impl
        } catch (t: Throwable) {
            AppLogger.e(TAG, "Failed to load feature stack $stackId", t)
            null
        }
    }

    /**
     * Extracts `feature_stacks/<assetFile>` to the code-cache dir, skipping the copy
     * when the extracted file already matches the asset size (template upgrades
     * change the bytes and therefore re-extract). Returns null when the asset is
     * absent — i.e. the stack was disabled at build time.
     */
    private fun extractAsset(context: Context, assetFile: String): File? {
        val destDir = File(context.codeCacheDir, ASSET_DIR).apply { mkdirs() }
        val dest = File(destDir, assetFile)
        val tmp = File(destDir, "$assetFile.tmp")
        try {
            context.assets.open("$ASSET_DIR/$assetFile").use { input ->
                tmp.outputStream().use { out -> input.copyTo(out) }
            }
            if (dest.isFile && dest.length() == tmp.length()) {
                tmp.delete()
                return dest
            }
            if (dest.exists() && !dest.delete()) return null
            if (!tmp.renameTo(dest)) {
                tmp.copyTo(dest, overwrite = true)
                tmp.delete()
            }
            return dest
        } catch (e: FileNotFoundException) {
            return null
        } catch (e: java.io.IOException) {
            return null
        } catch (t: Throwable) {
            AppLogger.e(TAG, "Failed to extract feature stack asset $assetFile", t)
            return null
        } finally {
            tmp.delete()
        }
    }
}
