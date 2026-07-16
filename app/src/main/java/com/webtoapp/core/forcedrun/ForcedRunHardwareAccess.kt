package com.webtoapp.core.forcedrun

import android.app.Activity
import android.content.Context
import com.webtoapp.core.feature.FeatureLoader

object ForcedRunHardwareAccess {
    private const val CLASS = "com.webtoapp.core.forcedrun.ForcedRunHardwareController"

    private fun resolve(): Class<*>? = FeatureLoader.loadClass(CLASS)

    fun isAvailable(): Boolean = resolve() != null

    fun getInstance(context: Context): Any? {
        return runCatching {
            val c = resolve() ?: return null
            c.getMethod("getInstance", Context::class.java).invoke(null, context)
        }.getOrNull()
    }

    fun setTargetActivity(instance: Any?, activity: Activity?) {
        if (instance == null) return
        runCatching {
            instance.javaClass.getMethod("setTargetActivity", Activity::class.java)
                .invoke(instance, activity)
        }
    }

    fun isBlockVolumeKeys(instance: Any?): Boolean = boolProp(instance, "isBlockVolumeKeys")

    fun isBlockPowerKey(instance: Any?): Boolean = boolProp(instance, "isBlockPowerKey")

    fun isBlockTouch(instance: Any?): Boolean = boolProp(instance, "isBlockTouch")

    private fun boolProp(instance: Any?, name: String): Boolean {
        if (instance == null) return false
        return runCatching {
            val m = instance.javaClass.methods.firstOrNull {
                it.name == name || it.name == "get" + name.replaceFirstChar { c -> c.uppercase() }
            }
            if (m != null && m.parameterCount == 0) {
                return@runCatching (m.invoke(instance) as? Boolean) == true
            }
            val f = instance.javaClass.getDeclaredField(name.removePrefix("is").replaceFirstChar { it.lowercase() })
            f.isAccessible = true
            (f.get(instance) as? Boolean) == true
        }.getOrDefault(false)
    }
}
