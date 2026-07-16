package com.webtoapp.core.feature

import android.content.Context
import java.lang.reflect.Modifier

object ShellFeatureRuntime {
    fun isAvailable(className: String): Boolean = FeatureLoader.loadClass(className) != null

    fun newWithContext(className: String, context: Context): Any? {
        val clazz = FeatureLoader.loadClass(className) ?: return null
        return runCatching {
            clazz.getConstructor(Context::class.java).newInstance(context)
        }.getOrNull()
    }

    fun call(target: Any?, method: String, vararg args: Any?): Any? {
        if (target == null) return null
        return runCatching {
            val candidates = target.javaClass.methods.filter {
                it.name == method && it.parameterCount == args.size
            }
            val m = candidates.firstOrNull() ?: return null
            m.isAccessible = true
            m.invoke(target, *args)
        }.getOrNull()
    }

    fun callStatic(className: String, method: String, vararg args: Any?): Any? {
        val clazz = FeatureLoader.loadClass(className) ?: return null
        return runCatching {
            val candidates = clazz.methods.filter {
                it.name == method &&
                    Modifier.isStatic(it.modifiers) &&
                    it.parameterCount == args.size
            }
            val m = candidates.firstOrNull() ?: return null
            m.isAccessible = true
            m.invoke(null, *args)
        }.getOrNull()
    }

    fun prop(target: Any?, name: String): Any? {
        if (target == null) return null
        val getter = "get" + name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        call(target, getter)?.let { return it }
        return runCatching {
            val f = target.javaClass.getDeclaredField(name)
            f.isAccessible = true
            f.get(target)
        }.getOrNull()
    }

    fun stateValue(target: Any?): Any? {
        val state = prop(target, "serverState") ?: call(target, "getServerState") ?: return null
        return prop(state, "value") ?: call(state, "getValue")
    }

    fun errorMessage(stateValue: Any?): String? {
        if (stateValue == null) return null
        val n = stateValue.javaClass.name
        if (!n.endsWith("\$Error") && !n.endsWith("Error")) return null
        return prop(stateValue, "message") as? String
            ?: call(stateValue, "getMessage") as? String
    }
}
