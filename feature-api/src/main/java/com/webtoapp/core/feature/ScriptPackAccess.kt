package com.webtoapp.core.feature

object ScriptPackAccess {
    fun callStaticString(className: String, method: String, vararg args: Any?): String? {
        return runCatching {
            val clazz = FeatureLoader.loadClass(className) ?: return null
            val candidates = clazz.methods.filter {
                it.name == method && java.lang.reflect.Modifier.isStatic(it.modifiers)
            }
            for (m in candidates) {
                if (m.parameterCount != args.size) continue
                m.isAccessible = true
                val result = m.invoke(null, *args)
                if (result is String) return@runCatching result
            }
            null
        }.getOrNull()
    }

    fun chromeMobileCompat(): String =
        callStaticString(
            "com.webtoapp.core.extension.ChromeExtensionMobileCompat",
            "generateCompatScript"
        ).orEmpty()

    fun chromePolyfill(extensionId: String, manifestJson: String, isBackground: Boolean = false): String =
        callStaticString(
            "com.webtoapp.core.extension.ChromeExtensionPolyfill",
            "generatePolyfill",
            extensionId,
            manifestJson,
            isBackground
        ).orEmpty()

    fun userscriptWindowManager(): String =
        callStaticString(
            "com.webtoapp.core.extension.UserScriptWindowScript",
            "getWindowManagerScript"
        ).orEmpty()

    fun browserDisguiseJs(config: Any): String? =
        callStaticString(
            "com.webtoapp.core.appearance.BrowserDisguiseJsGenerator",
            "generate",
            config
        )
}
