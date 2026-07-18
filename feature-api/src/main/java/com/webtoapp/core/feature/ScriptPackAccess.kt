package com.webtoapp.core.feature

object ScriptPackAccess {
    fun callStaticString(className: String, method: String, vararg args: Any?): String? {
        return ReflectInvoke.callString(className, method, *args)
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
