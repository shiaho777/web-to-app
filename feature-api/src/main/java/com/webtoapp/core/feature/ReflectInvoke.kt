package com.webtoapp.core.feature

import java.lang.reflect.Modifier

object ReflectInvoke {
    fun call(className: String, method: String, vararg args: Any?): Any? {
        val clazz = FeatureLoader.loadClass(className) ?: return null
        return call(clazz, method, *args)
    }

    fun call(clazz: Class<*>, method: String, vararg args: Any?): Any? {
        tryInvoke(clazz, null, method, args)?.let { return it }

        val instance = runCatching { clazz.getField("INSTANCE").get(null) }.getOrNull()
        if (instance != null) {
            tryInvoke(clazz, instance, method, args)?.let { return it }
        }

        val companion = runCatching { clazz.getField("Companion").get(null) }.getOrNull()
        if (companion != null) {
            tryInvoke(companion.javaClass, companion, method, args)?.let { return it }
        }
        return null
    }

    fun callString(className: String, method: String, vararg args: Any?): String? =
        call(className, method, *args) as? String

    private fun tryInvoke(
        clazz: Class<*>,
        target: Any?,
        method: String,
        args: Array<out Any?>
    ): Any? {
        val candidates = clazz.methods.filter { it.name == method && it.parameterCount == args.size }
        for (m in candidates) {
            val staticMethod = Modifier.isStatic(m.modifiers)
            if (staticMethod && target != null) continue
            if (!staticMethod && target == null) continue
            try {
                m.isAccessible = true
                return m.invoke(if (staticMethod) null else target, *args)
            } catch (_: Exception) {
            }
        }
        return null
    }
}
